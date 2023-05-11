package org.opentripplanner.ext.geocoder;

import java.util.Collection;
import javax.annotation.Nullable;
import org.opentripplanner.transit.model.framework.FeedScopedId;

/**
 * A result of a fuzzy stop cluster geocoding search. A cluster is defined as a group of stops that
 * are related to one another.
 * <p>
 * Specifically this means that:
 * <p>
 *  - if a stop has a parent station only the parent is returned
 *  - if stops are closer than 10 meters to each and have an identical name, only one is returned
 */
record StopCluster(
  FeedScopedId id,
  @Nullable String code,
  String name,
  Coordinate coordinate,
  Collection<String> modes
) {
  /**
   * Easily serializable version of a coordinate
   */
  public record Coordinate(double lat, double lon) {}
}
