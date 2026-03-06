package org.opentripplanner.updater.vehicle_rental.datasources.gbfs.v3_1_RC2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mobilitydata.gbfs.v3_1_RC2.geofencing_zones.GBFSFeature;
import org.mobilitydata.gbfs.v3_1_RC2.geofencing_zones.GBFSGeofencingZones;
import org.mobilitydata.gbfs.v3_1_RC2.geofencing_zones.GBFSGeofencingZones__1;
import org.mobilitydata.gbfs.v3_1_RC2.station_information.GBFSStationInformation;
import org.mobilitydata.gbfs.v3_1_RC2.station_status.GBFSStationStatus;
import org.mobilitydata.gbfs.v3_1_RC2.system_information.GBFSSystemInformation;
import org.mobilitydata.gbfs.v3_1_RC2.vehicle_availability.GBFSVehicleAvailability;
import org.mobilitydata.gbfs.v3_1_RC2.vehicle_types.GBFSVehicleType;
import org.mobilitydata.gbfs.v3_1_RC2.vehicle_types.GBFSVehicleTypes;
import org.opentripplanner.framework.io.OtpHttpClient;
import org.opentripplanner.framework.io.OtpHttpClientFactory;
import org.opentripplanner.updater.spi.HttpHeaders;
import org.slf4j.LoggerFactory;

/**
 * This tests that
 * {@link org.opentripplanner.updater.vehicle_rental.datasources.gbfs.v3_1_RC2.GbfsFeedLoader}
 * handles loading of different versions of GBFS correctly, that the optional language parameter
 * works correctly, and that the different files in a GBFS bundle are all included, with all
 * information in them.
 */
class GbfsFeedLoaderTest {

  private static final OtpHttpClient OTP_HTTP_CLIENT = new OtpHttpClientFactory().create(
    LoggerFactory.getLogger(GbfsFeedLoaderTest.class)
  );

  @Test
  void getV31Feed() {
    GbfsFeedLoader loader = new GbfsFeedLoader(
      "file:src/test/resources/gbfs/karlsruhe-v3_1/gbfs.json",
      HttpHeaders.empty(),
      OTP_HTTP_CLIENT
    );

    assertTrue(loader.update());

    var systemInformation = loader.getFeed(GBFSSystemInformation.class);
    assertNotNull(systemInformation);
    var systemInfos = systemInformation.getData();
    assertEquals("testfeed_3_1", systemInfos.getSystemId());
    assertEquals("Europe/Berlin", systemInfos.getTimezone().value());
    assertEquals("info@example-carsharing.de", systemInfos.getEmail());
    assertNull(systemInfos.getOperator());
    assertEquals("+49 123 456 789 0", systemInfos.getPhoneNumber());
    assertNull(systemInfos.getShortName());
    assertEquals("https://www.example-carsharing.de", systemInfos.getUrl());
    assertEquals("de", systemInfos.getName().getFirst().getLanguage());
    assertEquals("Example CarSharing", systemInfos.getName().getFirst().getText());

    var vehicleTypes = loader.getFeed(GBFSVehicleTypes.class);
    assertNotNull(vehicleTypes);
    List<GBFSVehicleType> vehicleTypeData = vehicleTypes.getData().getVehicleTypes();
    assertEquals(3, vehicleTypeData.size());
    var vehicleType = vehicleTypeData.getFirst();
    assertEquals("VT_COMPACT_COMBUSTION", vehicleType.getVehicleTypeId());
    assertEquals(GBFSVehicleType.FormFactor.CAR, vehicleType.getFormFactor());
    assertEquals(GBFSVehicleType.PropulsionType.COMBUSTION, vehicleType.getPropulsionType());
    assertNotNull(vehicleType.getMaxRangeMeters());

    var stationStatus = loader.getFeed(GBFSStationStatus.class);
    assertNotNull(stationStatus);
    assertEquals(4, stationStatus.getData().getStations().size());

    var stationInfos = loader.getFeed(GBFSStationInformation.class);
    assertNotNull(stationInfos);
    assertEquals(4, stationInfos.getData().getStations().size());

    var geofencingZones = loader.getFeed(GBFSGeofencingZones.class);
    assertNotNull(geofencingZones);
    GBFSGeofencingZones__1 geofencingZoneData = geofencingZones.getData().getGeofencingZones();
    assertNotNull(geofencingZoneData);
    List<GBFSFeature> geoFeatures = geofencingZoneData.getFeatures();
    assertNotNull(geoFeatures);
    assertEquals(16, geoFeatures.size());

    var availability = loader.getFeed(GBFSVehicleAvailability.class);
    assertNotNull(availability);
    assertEquals(5, availability.getData().getVehicles().size());
  }
}
