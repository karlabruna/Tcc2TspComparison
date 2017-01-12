package dataaccess;

import com.koloboke.collect.map.hash.HashIntLongMaps;
import common.Timer;
import instrumentation.ObjectSizeFetcher;
import model.graph.Graph;
import model.graph.Node;
import model.graph.Relationship;
import model.graph.cache.GraphCache;
import model.graph.cache.GraphCacheFactory;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Graph implementation that retrieves data from Neo4j graph database.
 */
public class Neo4jGraphEmbedded implements Graph {
  private static final String DATABASE_PATH_PREFIX =
      "C:/Users/Karla/Documents/ninha/tcc/neo4j/neo4j-enterprise-3.0.6/data/databases/";
  private static final String DATABASE_TEMPLATE = "graph.%s.db";
  private static final String CONFIG_PATH =
      "C:/Users/Karla/Documents/ninha/tcc/neo4j/neo4j-enterprise-3.0.6/conf/neo4j.conf";
  private static final String ID_PROPERTY = "id";
  private static final String COST_PROPERTY = "cost";

  private final GraphCache graphCache;
  private final GraphDatabaseService graphDb;

  // Only used when accessMode is ON_DEMAND.
  private static Map<Integer, Long> nodeIdMap = HashIntLongMaps.newMutableMap();

  // Only used when accessMode is ALL.
  private static double graphInMemoryRatio = 0.05;

  private int counter = 0;

  public Neo4jGraphEmbedded(AccessMode accessMode, Country graphCountry) {
    String databaseName =
        DATABASE_PATH_PREFIX + String.format(DATABASE_TEMPLATE, graphCountry.toString().toLowerCase());
    graphDb = new org.neo4j.graphdb.factory.GraphDatabaseFactory()
        .newEmbeddedDatabaseBuilder(new File(databaseName))
        .loadPropertiesFromFile(CONFIG_PATH)
        .setConfig(GraphDatabaseSettings.read_only, "true")
        .newGraphDatabase();
    registerShutdownHook(graphDb);
    switch (accessMode) {
      case ALL:
        graphCache = GraphCacheFactory.create(GraphCache.Type.FAST, false);
        // Load all nodes and relationships on graph.
        Timer graphLoaderTimer = new Timer();
        graphLoaderTimer.startCounter();
        try (Transaction tx = graphDb.beginTx()) {
          for (org.neo4j.graphdb.Node neo4jNode : graphDb.getAllNodes().stream().sorted(
              (org.neo4j.graphdb.Node n1, org.neo4j.graphdb.Node n2) -> getId(n1) - getId(n2))
              .collect(Collectors.toList())) {
            nodeIdMap.put(getId(neo4jNode), neo4jNode.getId());
            if (graphInMemoryRatio == 1 || graphCache.size() < graphCountry.getSize() * graphInMemoryRatio) {
              storeNodeAndRelationships(neo4jNode);
            } else {
              for (org.neo4j.graphdb.Relationship neo4jRelationship : neo4jNode.getRelationships(Direction.OUTGOING)) {
                neo4jRelationship.getPropertyKeys();
                neo4jRelationship.getEndNode();
              }
            }
          }
          // Limit capacity after loading maximum ratio of graph.
          if (graphCache.size() >= graphCountry.getSize() * graphInMemoryRatio) {
            graphCache.limitCapacity();
          }
          tx.success();
        }
        System.out.println(
            String.format("Loaded all nodes and relationships on graph in: %ss", graphLoaderTimer.endCounter()));
        break;
      case ON_DEMAND:
        // No initial loading needed, but warm the database cache.
        warmUp();
        graphCache = GraphCacheFactory.create(GraphCache.Type.FAST, true);
        break;
      default:
        throw new IllegalArgumentException(String.format("Unsupported access type for graph: %s", accessMode));
    }
  }

  private void warmUp() {
    Timer warmUpTimer = new Timer();
    warmUpTimer.startCounter();
    try (Transaction tx = graphDb.beginTx()) {
      for (org.neo4j.graphdb.Node n : graphDb.getAllNodes()) {
        nodeIdMap.put(getId(n), n.getId());
        for (org.neo4j.graphdb.Relationship relationship : n.getRelationships(Direction.OUTGOING)) {
          relationship.getPropertyKeys();
          relationship.getEndNode();
        }
      }
      tx.success();
    }
    System.out.println(
        String.format("Warmed cache database cache in: %ss", warmUpTimer.endCounter()));
  }

  @Override
  public Node getNode(int nodeId) {
    // Load node and relationships and return.
    if (!graphCache.containsKey(nodeId)) {
      try (Transaction tx = graphDb.beginTx()) {
        storeNodeAndRelationships(getNodeById(nodeId));
        tx.success();
      }
    }
    return graphCache.get(nodeId);
  }

  @Override
  public void close() {
    graphDb.shutdown();
  }

  @Override
  public String getCacheFootprint() {
    return ObjectSizeFetcher.getObjectFootprint(graphCache);
  }

  @Override
  public int getCacheSize() {
    return graphCache.size();
  }

  private org.neo4j.graphdb.Node getNodeById(int nodeId) {
    return graphDb.getNodeById(nodeIdMap.get(nodeId));
  }

  private void storeNodeAndRelationships(org.neo4j.graphdb.Node neo4jNode) {
    int nodeId = getId(neo4jNode);
    if (!graphCache.containsKey(nodeId)) {
      graphCache.put(retrieveNode(nodeId, neo4jNode));
    }
  }

  private Node retrieveNode(int nodeId, org.neo4j.graphdb.Node neo4jNode) {
    Set<Relationship> relationships = new HashSet<>();
    for (org.neo4j.graphdb.Relationship neo4jRelationship : neo4jNode.getRelationships(Direction.OUTGOING)) {
      int targetId = getId(neo4jRelationship.getEndNode());
      if (nodeId != targetId) {
        relationships.add(new Relationship(getCost(neo4jRelationship), targetId));
      }
    }
    return new Node(nodeId, relationships.stream().sorted(
        (Relationship r1, Relationship r2) -> r1.getEndNodeId() - r2.getEndNodeId())
        .collect(Collectors.toList()));
  }

  private int getId(org.neo4j.graphdb.Node node) {
    return (int) (long) node.getProperty(ID_PROPERTY);
//    return Integer.parseInt((String) node.getProperty(ID_PROPERTY));
  }

  private double getCost(org.neo4j.graphdb.Relationship relationship) {
    return (double) relationship.getProperty(COST_PROPERTY);
  }

  private void registerShutdownHook(final GraphDatabaseService graphDb) {
    // Registers a shutdown hook for the Neo4j instance so that it
    // shuts down nicely when the VM exits (even if you "Ctrl-C" the
    // running application).
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        graphDb.shutdown();
      }
    });
  }
}
