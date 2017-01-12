package dataaccess;

import common.Timer;
import instrumentation.ObjectSizeFetcher;
import model.graph.Graph;
import model.graph.Node;
import model.graph.Relationship;
import model.graph.cache.GraphCache;
import model.graph.cache.GraphCacheFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Graph implementation that retrieves data from PostgreSQL relational database.
 */
public class PostgresGraph implements Graph {
  private static final String EDGES_TABLE_NAME_TEMPLATE = "edges_%s";

  private final Connection connection;
  private final GraphCache graphCache;
  private final String edgesTableName;

  private static double graphInMemoryRatio = 1;

  public PostgresGraph(AccessMode accessMode, Country graphCountry) {
    edgesTableName = String.format(EDGES_TABLE_NAME_TEMPLATE, graphCountry.toString().toLowerCase());

    // Boilerplate code for creating JDBC connection.
    try {
      Class.forName("org.postgresql.Driver");
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("Error loading PosgreSQL driver.", e);
    }
    try {
      connection = DriverManager.getConnection("jdbc:postgresql://localhost:5433/postgres", "postgres", "postgres");
    } catch (SQLException e) {
      throw new IllegalStateException("Error connecting to PosgreSQL database.", e);
    }

    switch (accessMode) {
      case ALL:
        graphCache = GraphCacheFactory.create(GraphCache.Type.FAST, false);
        // Load all nodes and relationships on graph.
        try {
          Timer graphLoaderTimer = new Timer();
          graphLoaderTimer.startCounter();
          Statement statement = connection.createStatement();
          storeNodeAndRelationships(statement.executeQuery(
              String.format(
                  "SELECT DISTINCT source, target, cost "
                      + "FROM %s "
                      + "WHERE source != target "
                      + "ORDER BY source "
                      + "LIMIT %s",
                  edgesTableName, graphCountry.getRelationships() * graphInMemoryRatio)),
              graphCountry);
          statement.close();
          System.out.println(
              String.format("Loaded all nodes and relationships on graph in: %ss", graphLoaderTimer.endCounter()));
          graphCache.limitCapacity();
        } catch (SQLException e) {
          throw new IllegalStateException("Error executing query in PosgreSQL database.", e);
        }
        break;
      case ON_DEMAND:
        graphCache = GraphCacheFactory.create(GraphCache.Type.FAST, true);
        // No initial loading needed.
        break;
      default:
        throw new IllegalArgumentException(String.format("Unsupported access type for graph: %s", accessMode));
    }
  }

  @Override
  public Node getNode(int nodeId) {
    // Load node and relationships and return.
    if (!graphCache.containsKey(nodeId)) {
      try {
        Statement statement = connection.createStatement();
        storeNodeAndRelationshipsByDemand(
            nodeId,
            statement.executeQuery(
                String.format(
                    "SELECT DISTINCT target, cost FROM %s WHERE source = %s AND source != target ORDER BY target",
                    edgesTableName, nodeId)));
        statement.close();
      } catch (SQLException e) {
        throw new IllegalStateException("Error executing query in PosgreSQL database.", e);
      }
    }
    return graphCache.get(nodeId);
  }

  @Override
  public void close() {
    try {
      connection.close();
    } catch (SQLException e) {
      throw new IllegalStateException("Error closing connection to PosgreSQL database.", e);
    }
  }

  @Override
  public String getCacheFootprint() {
    return ObjectSizeFetcher.getObjectFootprint(graphCache);
  }

  @Override
  public int getCacheSize() {
    return graphCache.size();
  }

  /**
   * Handles sql query
   * SELECT DISTINCT source, target, cost
   * FROM edges_table
   * WHERE source != target
   * ORDER BY source
   */
  private void storeNodeAndRelationships(ResultSet resultSet, Country graphCountry) {
    try {
      while (resultSet.next()) {
        if (graphInMemoryRatio != 1 && graphCache.size() >= graphCountry.getSize() * graphInMemoryRatio) {
          resultSet.close();
          return;
        }
        int sourceId = resultSet.getInt(1);
        int targetId = resultSet.getInt(2);
        double cost = resultSet.getDouble(3);
        Relationship relationship = new Relationship(cost, targetId);
        if (!graphCache.containsKey(sourceId)) {
          graphCache.put(new Node(sourceId, relationship));
        } else {
          graphCache.addRelationship(sourceId, relationship);
        }
      }
      resultSet.close();
    } catch (SQLException e) {
      throw new IllegalStateException("Error while reading results from query.", e);
    }
  }

  /**
   * Handles sql query
   * SELECT DISTINCT target, cost
   * FROM edges_table
   * WHERE source = %s AND source != target
   * ORDER BY target
   */
  private void storeNodeAndRelationshipsByDemand(int sourceId, ResultSet resultSet) {
    List<Relationship> relationships = new ArrayList<>();
    try {
      while (resultSet.next()) {
        int targetId = resultSet.getInt(1);
        double cost = resultSet.getDouble(2);
        relationships.add(new Relationship(cost, targetId));
      }
      resultSet.close();
    } catch (SQLException e) {
      throw new IllegalStateException("Error while reading results from query.", e);
    }
    if (!graphCache.containsKey(sourceId)) {
      graphCache.put(new Node(sourceId, relationships));
    }
  }
}
