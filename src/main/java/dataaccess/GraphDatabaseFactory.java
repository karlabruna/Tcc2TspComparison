package dataaccess;

import model.graph.Graph;

/**
 * Returns graph implementation to use based on database.
 */
public class GraphDatabaseFactory {

  public enum DatabaseType {
    POSTGRES,
    NEO4J,
    NEO4J_EMBEDDED,
  }

  public static Graph create(DatabaseType type, Graph.AccessMode accessMode, Graph.Country graphCountry) {
    switch (type) {
      case POSTGRES:
        return new PostgresGraph(accessMode, graphCountry);
      case NEO4J:
        return new Neo4jGraph(accessMode, graphCountry);
      case NEO4J_EMBEDDED:
        return new Neo4jGraphEmbedded(accessMode, graphCountry);
      default:
        throw new IllegalArgumentException("Unsupported database type");
    }
  }
}
