package org.opentripplanner.astar.strategy;

import java.util.function.Predicate;
import org.opentripplanner.astar.spi.AStarEdge;
import org.opentripplanner.astar.spi.AStarState;
import org.opentripplanner.astar.spi.SkipEdgeStrategy;

/**
 * Skips Edges when the available battery distance of a vehicle is less than the accumulated driving
 * distance of the same vehicle
 */
public class BatteryDistanceSkipEdgeStrategy<
  State extends AStarState<State, Edge, ?>, Edge extends AStarEdge<State, Edge, ?>
>
  implements SkipEdgeStrategy<State, Edge> {

  private final Predicate<State> shouldSkip;

  public BatteryDistanceSkipEdgeStrategy(Predicate<State> shouldSkip) {
    this.shouldSkip = shouldSkip;
  }

  @Override
  public boolean shouldSkipEdge(State current, Edge edge) {
    return shouldSkip.test(current);
  }
}
