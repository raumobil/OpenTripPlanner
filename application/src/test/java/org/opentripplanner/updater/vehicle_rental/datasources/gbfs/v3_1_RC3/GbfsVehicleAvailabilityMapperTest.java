package org.opentripplanner.updater.vehicle_rental.datasources.gbfs.v3_1_RC3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.opentripplanner.transit.model._data.TimetableRepositoryForTest.id;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mobilitydata.gbfs.v3_1_RC3.vehicle_availability.GBFSAvailability;
import org.mobilitydata.gbfs.v3_1_RC3.vehicle_availability.GBFSVehicle;
import org.opentripplanner.core.model.id.FeedScopedId;
import org.opentripplanner.service.vehiclerental.model.RentalAvailability;
import org.opentripplanner.service.vehiclerental.model.VehicleRentalStation;

public class GbfsVehicleAvailabilityMapperTest {

  public static final LocalDate FROM_AVAILABILITY = LocalDate.of(2026, Month.MARCH, 1);
  public static final LocalDate UNTIL_AVAILABILITY = LocalDate.of(2026, Month.MARCH, 2);
  public static final String STATION1_ID = "station1";
  public static final String STATION_2_ID = "station2";
  Map<String, List<GBFSVehicle>> gbfsVehiclesByStationId = Map.of(
    STATION1_ID,
    List.of(
      new GBFSVehicle()
        .withVehicleId("vehicle1")
        .withStationId(STATION1_ID)
        .withAvailabilities(getAvailabilities())
    )
  );

  @Test
  void mapStationAvailabilities() {
    var mapper = new GbfsVehicleAvailabilityMapper(gbfsVehiclesByStationId);

    final VehicleRentalStation station = VehicleRentalStation.of().withId(id(STATION1_ID)).build();

    VehicleRentalStation rentalStation = mapper.mapStationAvailabilities(station);

    assertNotNull(rentalStation.id());
    assertEquals(STATION1_ID, rentalStation.id().getId());
    var vehicles = rentalStation.vehiclesOnStation();
    assertNotNull(vehicles);
    assertEquals(1, vehicles.size());
    var availabilities = vehicles.getFirst().availabilities();
    assertNotNull(availabilities);
    assertEquals(2, availabilities.size());
    RentalAvailability availability = availabilities.getFirst();
    assertEquals(toInstant(FROM_AVAILABILITY), availability.from());
    assertEquals(toInstant(UNTIL_AVAILABILITY), availability.until());
    assertNull(availabilities.get(1).until());
  }

  @Test
  void mapStationAvailabilities_noAvailabilities() {
    var mapper = new GbfsVehicleAvailabilityMapper(gbfsVehiclesByStationId);

    final VehicleRentalStation station = VehicleRentalStation.of().withId(id(STATION_2_ID)).build();

    VehicleRentalStation rentalStation = mapper.mapStationAvailabilities(station);

    FeedScopedId rentalStationId = rentalStation.id();
    assertNotNull(rentalStationId);
    assertEquals(STATION_2_ID, rentalStationId.getId());
    assertNull(rentalStation.vehiclesOnStation());
  }

  private static List<GBFSAvailability> getAvailabilities() {
    var availability1 = new GBFSAvailability();
    availability1.setFrom(toDate(FROM_AVAILABILITY));
    availability1.setUntil(toDate(UNTIL_AVAILABILITY));

    var availability2 = new GBFSAvailability();
    Date from2 = toDate(LocalDate.of(2026, Month.MARCH, 4));
    availability2.setFrom(from2);

    return List.of(availability1, availability2);
  }

  private static Date toDate(LocalDate date) {
    return Date.from(toInstant(date));
  }

  private static Instant toInstant(LocalDate date) {
    return date.atStartOfDay().toInstant(ZoneOffset.UTC);
  }
}
