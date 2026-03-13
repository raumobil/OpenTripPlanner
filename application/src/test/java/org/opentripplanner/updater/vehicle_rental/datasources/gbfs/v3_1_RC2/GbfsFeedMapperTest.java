package org.opentripplanner.updater.vehicle_rental.datasources.gbfs.v3_1_RC2;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mobilitydata.gbfs.v3_1_RC2.vehicle_types.GBFSVehicleType;
import org.opentripplanner.core.model.i18n.TranslatedString;
import org.opentripplanner.framework.io.OtpHttpClientFactory;
import org.opentripplanner.service.vehiclerental.model.GeofencingZone;
import org.opentripplanner.service.vehiclerental.model.RentalVehicleType;
import org.opentripplanner.service.vehiclerental.model.VehicleRentalPlace;
import org.opentripplanner.service.vehiclerental.model.VehicleRentalStation;
import org.opentripplanner.street.model.RentalFormFactor;
import org.opentripplanner.updater.spi.HttpHeaders;
import org.opentripplanner.updater.vehicle_rental.datasources.gbfs.GbfsVehicleRentalDataSource;
import org.opentripplanner.updater.vehicle_rental.datasources.params.GbfsVehicleRentalDataSourceParameters;
import org.opentripplanner.updater.vehicle_rental.datasources.params.RentalPickupType;
import org.slf4j.LoggerFactory;

/**
 * This tests the mapping between data coming from a {@link GbfsFeedLoader} to OTP station models.
 */
class GbfsFeedMapperTest {

  private static final String SYSTEM_ID = "testfeed_3_1";

  @Test
  void makeStationFromv3_1_RC20() {
    var params = new GbfsVehicleRentalDataSourceParameters(
      "file:src/test/resources/gbfs/karlsruhe-v3_1/gbfs.json",
      null,
      false,
      HttpHeaders.empty(),
      null,
      false,
      false,
      RentalPickupType.ALL
    );
    var otpHttpClient = new OtpHttpClientFactory().create(
      LoggerFactory.getLogger(GbfsFeedMapperTest.class)
    );
    var loader = new GbfsFeedLoader(params.url(), params.httpHeaders(), otpHttpClient);
    var mapper = new GbfsFeedMapper(loader, params);

    assertTrue(loader.update());

    List<VehicleRentalPlace> stations = mapper.getUpdates();
    assertEquals(4, stations.size());

    assertTrue(
      stations
        .stream()
        .allMatch(vehicleRentalPlace ->
          vehicleRentalPlace.availablePickupFormFactors(true).equals(Set.of(RentalFormFactor.CAR))
        )
    );
    assertTrue(stations.stream().noneMatch(VehicleRentalPlace::isFloatingVehicle));
    assertTrue(stations.stream().allMatch(VehicleRentalPlace::isCarStation));
    assertTrue(stations.stream().noneMatch(VehicleRentalPlace::overloadingAllowed));
    assertTrue(
      stations
        .stream()
        .allMatch(vehicleRentalStation -> vehicleRentalStation.network().equals(SYSTEM_ID))
    );
    assertTrue(
      stations
        .stream()
        .noneMatch(vehicleRentalStation ->
          vehicleRentalStation.isArrivingInRentalVehicleAtDestinationAllowed()
        )
    );

    assertEquals(4, stations.stream().filter(VehicleRentalPlace::allowPickupNow).count());
    assertEquals(4, stations.stream().filter(VehicleRentalPlace::isAllowPickup).count());
    assertEquals(4, stations.stream().filter(VehicleRentalPlace::allowDropoffNow).count());
    assertEquals(4, stations.stream().filter(VehicleRentalPlace::isAllowDropoff).count());
    assertEquals("Hauptbahnhof KA Süd", stations.getFirst().name().toString(Locale.GERMAN));

    var vehicleRentalStation = (VehicleRentalStation) stations.get(0);
    var vehiclesOnStation = vehicleRentalStation.vehiclesOnStation();
    assertNotNull(vehiclesOnStation);
    var availabilities = vehiclesOnStation.getFirst().availabilities();
    assertNotNull(availabilities);
    Instant expectedFrom = LocalDateTime.of(
      LocalDate.of(2026, 3, 3),
      LocalTime.of(14, 4, 19)
    ).toInstant(ZoneOffset.UTC);
    Instant expectedUntil = LocalDateTime.of(
      LocalDate.of(2026, 3, 3),
      LocalTime.of(16, 4, 19)
    ).toInstant(ZoneOffset.UTC);
    var availabilityV1 = availabilities.getFirst();
    assertEquals(expectedFrom, availabilityV1.from());
    assertEquals(expectedUntil, availabilityV1.until());

    var vehicleRentalStation2 = (VehicleRentalStation) stations.get(1);
    var availabilityV2 = vehicleRentalStation2
      .vehiclesOnStation()
      .getFirst()
      .availabilities()
      .getFirst();
    assertNull(availabilityV2.until());

    var vehicleRentalStation3 = (VehicleRentalStation) stations.get(2);
    assertEquals(2, vehicleRentalStation3.vehiclesOnStation().size());
    var availabilitiesV3 = vehicleRentalStation3.vehiclesOnStation().getFirst().availabilities();
    assertEquals(2, availabilitiesV3.size());

    var system = stations.getFirst().vehicleRentalSystem();
    assertEquals("testfeed_3_1", system.systemId());
    assertEquals(
      TranslatedString.getI18NString(Map.of("de", "Example CarSharing"), false),
      system.name()
    );
    assertNull(system.shortName());
    assertNull(system.operator());
    assertEquals("https://www.example-carsharing.de", system.url());
  }

  @Test
  void getEmptyListOfVehicleTypes() {
    GbfsVehicleTypeMapper vehicleTypeMapper = new GbfsVehicleTypeMapper(SYSTEM_ID);
    Map<String, RentalVehicleType> vehicleTypes = GbfsFeedMapper.mapVehicleTypes(
      vehicleTypeMapper,
      Collections.emptyList()
    );
    assertTrue(vehicleTypes.isEmpty());
  }

  @Test
  void duplicatedVehicleTypesDoNotThrowException() {
    var vehicleTypeMapper = new GbfsVehicleTypeMapper(SYSTEM_ID);

    List<GBFSVehicleType> vehicleTypes = getDuplicatedGbfsVehicleTypes();

    assertDoesNotThrow(() -> {
      GbfsFeedMapper.mapVehicleTypes(vehicleTypeMapper, vehicleTypes);
    });
  }

  @Test
  void getOneVehicleTypeOfDuplicatedVehicleTypes() {
    var vehicleTypeMapper = new GbfsVehicleTypeMapper(SYSTEM_ID);

    List<GBFSVehicleType> duplicatedVehicleTypes = getDuplicatedGbfsVehicleTypes();

    Map<String, RentalVehicleType> vehicleTypes = GbfsFeedMapper.mapVehicleTypes(
      vehicleTypeMapper,
      duplicatedVehicleTypes
    );
    assertEquals(1, vehicleTypes.size());
  }

  @Test
  void geofencing() {
    var dataSource = new GbfsVehicleRentalDataSource(
      new GbfsVehicleRentalDataSourceParameters(
        "file:src/test/resources/gbfs/karlsruhe-v3_1/gbfs.json",
        "en",
        false,
        HttpHeaders.empty(),
        null,
        true,
        false,
        RentalPickupType.ALL
      ),
      new OtpHttpClientFactory()
    );

    dataSource.setup();

    assertTrue(dataSource.update());

    dataSource.getUpdates();

    var zones = dataSource.getGeofencingZones();

    assertEquals(14, zones.size());

    var hubBergnet = zones
      .stream()
      .filter(z -> z.name().toString().equals("Hub Bergnet"))
      .findFirst()
      .get();

    assertTrue(hubBergnet.dropOffBanned());
    assertFalse(hubBergnet.traversalBanned());

    var almereHaven = zones
      .stream()
      .filter(z -> z.name().toString().equals("Almere Haven"))
      .findFirst()
      .get();

    assertFalse(almereHaven.dropOffBanned());
    assertTrue(almereHaven.traversalBanned());

    var businessAreas = zones.stream().filter(GeofencingZone::isBusinessArea).toList();

    assertEquals(12, businessAreas.size());
    var almereStad = zones
      .stream()
      .filter(z -> z.name().toString().equals("Almere Stad"))
      .findFirst()
      .get();

    assertEquals("Almere Stad", almereStad.name().toString(Locale.forLanguageTag("en")));
    assertEquals("Almere Stad (nl)", almereStad.name().toString(Locale.forLanguageTag("nl")));
    assertEquals("testfeed_3_1:fb345775", almereStad.id().toString());
  }

  @Test
  void duplicatedStationsDoNotThrowException() {
    var params = new GbfsVehicleRentalDataSourceParameters(
      "file:src/test/resources/gbfs/duplicate-stations-v3_1/gbfs.json",
      null,
      false,
      HttpHeaders.empty(),
      null,
      false,
      false,
      RentalPickupType.ALL
    );
    var otpHttpClient = new OtpHttpClientFactory().create(
      LoggerFactory.getLogger(GbfsFeedMapperTest.class)
    );
    var loader = new GbfsFeedLoader(params.url(), params.httpHeaders(), otpHttpClient);
    var mapper = new GbfsFeedMapper(loader, params);

    assertTrue(loader.update());

    assertDoesNotThrow(() -> {
      mapper.getUpdates();
    });
  }

  @Test
  void duplicatedStationsKeepFirstOccurrence() {
    var params = new GbfsVehicleRentalDataSourceParameters(
      "file:src/test/resources/gbfs/duplicate-stations-v3_1/gbfs.json",
      null,
      false,
      HttpHeaders.empty(),
      null,
      false,
      false,
      RentalPickupType.ALL
    );
    var otpHttpClient = new OtpHttpClientFactory().create(
      LoggerFactory.getLogger(GbfsFeedMapperTest.class)
    );
    var loader = new GbfsFeedLoader(params.url(), params.httpHeaders(), otpHttpClient);
    var mapper = new GbfsFeedMapper(loader, params);

    assertTrue(loader.update());

    List<VehicleRentalPlace> stations = mapper.getUpdates();

    // Should have 3 stations (station_1, station_duplicate, station_3)
    // even though station_status has 4 entries (with duplicate station_duplicate)
    assertEquals(3, stations.size());

    // Verify the duplicate station uses the first occurrence data (10 vehicles available)
    var duplicateStation = stations
      .stream()
      .filter(s -> s.id().getId().contains("station_duplicate"))
      .findFirst()
      .orElseThrow();
    assertEquals(10, duplicateStation.vehiclesAvailable());
  }

  private static List<GBFSVehicleType> getDuplicatedGbfsVehicleTypes() {
    GBFSVehicleType gbfsVehicleType1 = new GBFSVehicleType();
    gbfsVehicleType1.setVehicleTypeId("sameId");
    gbfsVehicleType1.setFormFactor(GBFSVehicleType.FormFactor.BICYCLE);
    gbfsVehicleType1.setPropulsionType(GBFSVehicleType.PropulsionType.HUMAN);

    GBFSVehicleType gbfsVehicleType2 = new GBFSVehicleType();
    gbfsVehicleType2.setVehicleTypeId("sameId");
    gbfsVehicleType2.setFormFactor(GBFSVehicleType.FormFactor.BICYCLE);
    gbfsVehicleType2.setPropulsionType(GBFSVehicleType.PropulsionType.HUMAN);

    return List.of(gbfsVehicleType1, gbfsVehicleType2);
  }
}
