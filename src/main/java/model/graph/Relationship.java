package model.graph;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Definition for a graph relationship.
 */
public class Relationship {
  private double cost;
  private int endNodeId;

  public Relationship(double cost, int endNodeId) {
    this.cost = cost;
    this.endNodeId = endNodeId;
  }

  public double getCost() {
    return cost;
  }

  public int getEndNodeId() {
    return endNodeId;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 31) // two randomly chosen prime numbers
        // if deriving: appendSuper(super.hashCode())
        .append(cost)
        .append(endNodeId)
        .toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Relationship)) {
      return false;
    }
    if (obj == this) {
      return true;
    }

    Relationship relationship = (Relationship) obj;
    return new EqualsBuilder()
        // if deriving: appendSuper(super.equals(obj))
        .append(cost, relationship.cost)
        .append(endNodeId, relationship.endNodeId)
        .isEquals();
  }
}
