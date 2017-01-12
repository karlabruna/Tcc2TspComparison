package dataaccess;

import common.Timer;
import instrumentation.ObjectSizeFetcher;
import model.graph.Graph;
import model.graph.Node;
import model.graph.Relationship;
import model.graph.cache.GraphCache;
import model.graph.cache.GraphCacheFactory;
import org.neo4j.driver.v1.*;

import java.util.*;

import static org.neo4j.driver.v1.Values.parameters;

/**
 * Graph implementation that retrieves data from Neo4j graph database.
 */
public class Neo4jGraph implements Graph {
  private final Driver driver;
  private final GraphCache graphCache;
  private final Session session;

  private static double graphInMemoryRatio = 1;

  private int counter = 0;

  public Neo4jGraph(AccessMode accessMode, Country graphCountry) {
    // As Neo4j database is already running at this point, we cannot change the graph size anymore.
    driver = GraphDatabase.driver("bolt://localhost", AuthTokens.basic("neo4j", "tcc2"));
    session = driver.session();
    switch (accessMode) {
      case ALL:
        graphCache = GraphCacheFactory.create(GraphCache.Type.FAST, false);
        // Load all nodes and relationships on graph.
        Timer graphLoaderTimer = new Timer();
        graphLoaderTimer.startCounter();
        storeNodeAndRelationships(session.run(
            "MATCH (s:Node) WITH s WHERE s.id < {limit} "
                + "MATCH (s)-[e:EDGE]->(t:Node) WHERE s.id <> t.id RETURN DISTINCT s.id, e.cost, t.id "
                + "ORDER BY s.id LIMIT {limit}",
            parameters("limit", graphCountry.getRelationships() * graphInMemoryRatio)),
            graphCountry);
//        storeNodeAndRelationships(session.run(
//            "MATCH (s:Node)-[e:EDGE]->(t:Node) "
//                + "WHERE s.id <> t.id "
//                + "RETURN DISTINCT s.id, e.cost, t.id "
//                + "ORDER BY s.id "
//                + "LIMIT {limit}",
//                parameters("limit", graphCountry.getRelationships() * graphInMemoryRatio)),
//            graphCountry);
        System.out.println(
            String.format("Loaded all nodes and relationships on graph in: %ss", graphLoaderTimer.endCounter()));
        graphCache.limitCapacity();
        break;
      case ON_DEMAND:
        // No initial loading needed.
        graphCache = GraphCacheFactory.create(GraphCache.Type.FAST, true);
        break;
      default:
        throw new IllegalArgumentException(String.format("Unsupported access type for graph: %s", accessMode));
    }
  }

  @Override
  public Node getNode(int nodeId) {
    // Load node and relationships and return.
    if (!graphCache.containsKey(nodeId)) {
      storeNodeAndRelationshipsByDemand(
          nodeId,
          session.run(
//              "CALL getNodeById({sourceId}) YIELD cost, target RETURN DISTINCT cost, target ORDER BY target",
              "MATCH (s:Node {id: {sourceId}})-[e:EDGE]->(t:Node) "
                  + "WHERE s.id <> t.id RETURN DISTINCT e.cost, t.id ORDER BY t.id",
              parameters("sourceId", nodeId)));
//              parameters("sourceId", String.valueOf(nodeId))));
      counter++;
    }
    return graphCache.get(nodeId);
  }

  @Override
  public void close() {
    session.close();
    driver.close();
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
   * Handles cypher query
   * MATCH (s:Node)-[e:EDGE]->(t:Node)
   * WHERE s.id <> t.id
   * RETURN DISTINCT s.id, e.cost, t.id
   * ORDER BY s.id
   */
  private void storeNodeAndRelationships(StatementResult result, Country graphCountry) {
    while (result.hasNext()) {
      if (graphInMemoryRatio != 1 && graphCache.size() >= graphCountry.getSize() * graphInMemoryRatio) {
        return;
      }
      List<Value> values = result.next().values();
      int sourceId = values.get(0).asInt();
//      int sourceId = Integer.valueOf(values.get(0).asString());
      double cost = values.get(1).asDouble();
      int targetId = values.get(2).asInt();
//      int targetId = Integer.valueOf(values.get(2).asString());
      Relationship relationship = new Relationship(cost, targetId);
      if (!graphCache.containsKey(sourceId)) {
        graphCache.put(new Node(sourceId, relationship));
      } else {
        graphCache.addRelationship(sourceId, relationship);
      }
    }
  }

  /**
   * Handles cypher query
   * MATCH (s:Node {id: {sourceId}})-[e:EDGE]->(t:Node)
   * WHERE s.id <> t.id
   * RETURN DISTINCT e.cost, t.id
   * ORDER BY t.id
   */
  private void storeNodeAndRelationshipsByDemand(int sourceId, StatementResult result) {
    List<Relationship> relationships = new ArrayList<>();
    while (result.hasNext()) {
      List<Value> values = result.next().values();
      double cost = values.get(0).asDouble();
      int targetId = values.get(1).asInt();
//      int targetId = Integer.valueOf(values.get(1).asString());
      relationships.add(new Relationship(cost, targetId));
    }
    if (!graphCache.containsKey(sourceId)) {
      graphCache.put(new Node(sourceId, relationships));
    }
  }
}
