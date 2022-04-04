package org.opentripplanner.gtfs.mapping;

import static java.util.Objects.requireNonNullElse;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.opentripplanner.model.BoardingArea;
import org.opentripplanner.util.MapUtils;

/** Responsible for mapping GTFS Boarding areas into the OTP model. */
class BoardingAreaMapper {
  static final String DEFAULT_NAME = "Boarding area";

  private final Map<org.onebusaway.gtfs.model.Stop, BoardingArea> mappedBoardingAreas = new HashMap<>();

  Collection<BoardingArea> map(Collection<org.onebusaway.gtfs.model.Stop> allBoardingAreas) {
    return MapUtils.mapToList(allBoardingAreas, this::map);
  }

  /** Map from GTFS to OTP model, {@code null} safe. */
  BoardingArea map(org.onebusaway.gtfs.model.Stop orginal) {
    return orginal == null ? null : mappedBoardingAreas.computeIfAbsent(orginal, this::doMap);
  }

  private BoardingArea doMap(org.onebusaway.gtfs.model.Stop gtfsStop) {
    if (gtfsStop.getLocationType() != org.onebusaway.gtfs.model.Stop.LOCATION_TYPE_BOARDING_AREA) {
      throw new IllegalArgumentException(
          "Expected type " + org.onebusaway.gtfs.model.Stop.LOCATION_TYPE_BOARDING_AREA
              + ", but got " + gtfsStop.getLocationType());
    }

    StopMappingWrapper base = new StopMappingWrapper(gtfsStop);

    return new BoardingArea(
        base.getId(),
        requireNonNullElse(base.getName(), DEFAULT_NAME),
        base.getCode(),
        base.getDescription(),
        base.getCoordinate(),
        base.getWheelchairBoarding(),
        base.getLevel()
    );
  }
}
