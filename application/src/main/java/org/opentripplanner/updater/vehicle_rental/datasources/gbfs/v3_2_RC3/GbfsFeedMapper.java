package org.opentripplanner.updater.vehicle_rental.datasources.gbfs.v3_2_RC3;

import static java.util.stream.Collectors.toMap;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.jspecify.annotations.NonNull;
import org.mobilitydata.gbfs.v3_2_RC3.geofencing_zones.GBFSGeofencingZones;
import org.mobilitydata.gbfs.v3_2_RC3.station_information.GBFSStationInformation;
import org.mobilitydata.gbfs.v3_2_RC3.station_status.GBFSStation;
import org.mobilitydata.gbfs.v3_2_RC3.station_status.GBFSStationStatus;
import org.mobilitydata.gbfs.v3_2_RC3.system_information.GBFSSystemInformation;
import org.mobilitydata.gbfs.v3_2_RC3.vehicle_availability.GBFSVehicle;
import org.mobilitydata.gbfs.v3_2_RC3.vehicle_availability.GBFSVehicleAvailability;
import org.mobilitydata.gbfs.v3_2_RC3.vehicle_status.GBFSVehicleStatus;
import org.mobilitydata.gbfs.v3_2_RC3.vehicle_types.GBFSVehicleType;
import org.mobilitydata.gbfs.v3_2_RC3.vehicle_types.GBFSVehicleTypes;
import org.mobilitydata.gbfs.v3_2_RC3.virtual_station_probabilites.GBFSVirtualStation;
import org.mobilitydata.gbfs.v3_2_RC3.virtual_station_probabilites.GBFSVirtualStationProbabilites;
import org.opentripplanner.core.model.i18n.I18NString;
import org.opentripplanner.core.model.i18n.TranslatedString;
import org.opentripplanner.framework.application.OTPFeature;
import org.opentripplanner.service.vehiclerental.model.GeofencingZone;
import org.opentripplanner.service.vehiclerental.model.RentalVehicleType;
import org.opentripplanner.service.vehiclerental.model.VehicleRentalPlace;
import org.opentripplanner.service.vehiclerental.model.VehicleRentalStation;
import org.opentripplanner.service.vehiclerental.model.VehicleRentalSystem;
import org.opentripplanner.service.vehiclerental.model.VirtualRentalStation;
import org.opentripplanner.street.model.RentalFormFactor;
import org.opentripplanner.updater.vehicle_rental.datasources.params.GbfsVehicleRentalDataSourceParameters;
import org.opentripplanner.updater.vehicle_rental.datasources.params.RentalPickupType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GbfsFeedMapper
  implements org.opentripplanner.updater.vehicle_rental.datasources.gbfs.GbfsFeedMapper {

  private static final Logger LOG = LoggerFactory.getLogger(GbfsFeedMapper.class);

  private final GbfsFeedLoader loader;
  private final GbfsVehicleRentalDataSourceParameters params;
  private List<GeofencingZone> geofencingZones = List.of();
  private boolean logGeofencingZonesDoesNotExistWarning = true;

  public GbfsFeedMapper(GbfsFeedLoader loader, GbfsVehicleRentalDataSourceParameters params) {
    this.loader = loader;
    this.params = params;
  }

  @Override
  public List<VehicleRentalPlace> getUpdates() {
    // Get system information
    GBFSSystemInformation systemInformation = loader.getFeed(GBFSSystemInformation.class);
    GbfsSystemInformationMapper systemInformationMapper = new GbfsSystemInformationMapper();
    VehicleRentalSystem system = systemInformationMapper.mapSystemInformation(
      systemInformation.getData(),
      params.network()
    );

    // Get vehicle types
    final Map<String, RentalVehicleType> vehicleTypes = getVehicleTypes(system);

    List<VehicleRentalPlace> stations = new LinkedList<>();
    if (params.allowRentalType(RentalPickupType.STATION)) {
      // Both station information and status are required for all systems using stations
      var stationInformation = loader.getFeed(GBFSStationInformation.class);
      var stationStatus = loader.getFeed(GBFSStationStatus.class);
      var vehicleAvailability = loader.getFeed(GBFSVehicleAvailability.class);
      if (stationInformation != null && stationStatus != null) {
        // Index all the station status entries on their station ID.
        // in case of duplicates entries (stations with identical unique id),
        // only the first occurrence is kept.
        Map<String, GBFSStation> statusLookup = stationStatus
          .getData()
          .getStations()
          .stream()
          .collect(
            Collectors.toMap(
              GBFSStation::getStationId,
              Function.identity(),
              (gbfsStation1, gbfsStation2) -> gbfsStation1
            )
          );
        GbfsStationStatusMapper stationStatusMapper = new GbfsStationStatusMapper(
          statusLookup,
          vehicleTypes
        );
        GbfsStationInformationMapper stationInformationMapper = new GbfsStationInformationMapper(
          system,
          vehicleTypes,
          params.allowKeepingRentedVehicleAtDestination(),
          params.overloadingAllowed()
        );

        Map<String, List<GBFSVirtualStation>> virtualByStationsById = Map.of();
        var virtualStations = loader.getFeed(GBFSVirtualStationProbabilites.class);
        if (virtualStations != null) {
          virtualByStationsById = getVirtualStationsById(virtualStations);
          GbfsVirtualRentalStationMapper virtualRentalStationMapper =
            getGbfsVirtualRentalStationMapper(virtualByStationsById, system);
          Stream<VirtualRentalStation> virtualStationStream = stationInformation
            .getData()
            .getStations()
            .stream()
            .map(virtualRentalStationMapper::mapStationInformation);
          stations.addAll(virtualStationStream.toList());
        }

        // Iterate over all known stations, and if we have any status information add it to those station objects.
        // todo prevent this copy e.g. by creating method for virtual station stuff
        Map<String, List<GBFSVirtualStation>> finalVirtualByStationsById = virtualByStationsById;
        Stream<VehicleRentalStation> stationStream = stationInformation
          .getData()
          .getStations()
          .stream()
          .filter(gbfsStation ->
            !finalVirtualByStationsById.containsKey(gbfsStation.getStationId())
          )
          .map(stationInformationMapper::mapStationInformation)
          .filter(Objects::nonNull)
          .map(stationStatusMapper::mapStationStatus);

        // add vehicle availability information, if offered.
        if (vehicleAvailability != null) {
          GbfsVehicleAvailabilityMapper availabilityMapper = getGbfsVehicleAvailabilityMapper(
            vehicleAvailability
          );
          stationStream = stationStream.map(availabilityMapper::mapStationAvailabilities);
        }

        stations.addAll(stationStream.toList());
      }
    }

    // Append the floating bike stations.
    if (OTPFeature.FloatingBike.isOn() && params.allowRentalType(RentalPickupType.FREE_FLOATING)) {
      var freeBikeStatus = loader.getFeed(GBFSVehicleStatus.class);
      if (freeBikeStatus != null) {
        GbfsVehicleStatusMapper freeVehicleStatusMapper = new GbfsVehicleStatusMapper(
          system,
          vehicleTypes
        );
        stations.addAll(
          freeBikeStatus
            .getData()
            .getVehicles()
            .stream()
            .map(freeVehicleStatusMapper::mapVehicleStatus)
            .filter(Objects::nonNull)
            .toList()
        );
      }
    }

    if (params.geofencingZones()) {
      var zones = loader.getFeed(GBFSGeofencingZones.class);
      if (zones != null) {
        var mapper = new GbfsGeofencingZoneMapper(system.systemId());
        this.geofencingZones = mapper.mapGeofencingZone(zones);
      } else {
        if (logGeofencingZonesDoesNotExistWarning) {
          LOG.warn(
            "GeofencingZones is enabled in OTP, but no zones exist for network: {}",
            params.network()
          );
        }
        logGeofencingZonesDoesNotExistWarning = false;
      }
    }
    return stations;
  }

  private static GbfsVehicleAvailabilityMapper getGbfsVehicleAvailabilityMapper(
    GBFSVehicleAvailability vehicleAvailability
  ) {
    Map<String, List<GBFSVehicle>> gbfsVehiclesByStationId = vehicleAvailability
      .getData()
      .getVehicles()
      .stream()
      .collect(Collectors.groupingBy(GBFSVehicle::getStationId));

    return new GbfsVehicleAvailabilityMapper(gbfsVehiclesByStationId);
  }

  private static GbfsVirtualRentalStationMapper getGbfsVirtualRentalStationMapper(
    Map<String, List<GBFSVirtualStation>> virtualByStationsById,
    VehicleRentalSystem system
  ) {
    //todo formfactor should by dynamic by gbfs data
    return new GbfsVirtualRentalStationMapper(
      system,
      RentalFormFactor.BICYCLE,
      virtualByStationsById
    );
  }

  private static @NonNull Map<String, List<GBFSVirtualStation>> getVirtualStationsById(
    GBFSVirtualStationProbabilites virtualStationProbabilities
  ) {
    return virtualStationProbabilities
      .getData()
      .getVirtualStations()
      .stream()
      .collect(Collectors.groupingBy(GBFSVirtualStation::getStationId));
  }

  @Override
  public List<GeofencingZone> getGeofencingZones() {
    return this.geofencingZones;
  }

  public static Map<String, RentalVehicleType> mapVehicleTypes(
    GbfsVehicleTypeMapper vehicleTypeMapper,
    List<GBFSVehicleType> gbfsVehicleTypes
  ) {
    return gbfsVehicleTypes
      .stream()
      .map(vehicleTypeMapper::mapRentalVehicleType)
      .distinct()
      .collect(Collectors.toMap(v -> v.id().getId(), Function.identity()));
  }

  private Map<String, RentalVehicleType> getVehicleTypes(VehicleRentalSystem system) {
    var rawVehicleTypes = loader.getFeed(GBFSVehicleTypes.class);
    if (rawVehicleTypes != null) {
      GbfsVehicleTypeMapper vehicleTypeMapper = new GbfsVehicleTypeMapper(system.systemId());
      List<GBFSVehicleType> gbfsVehicleTypes = rawVehicleTypes.getData().getVehicleTypes();
      return mapVehicleTypes(vehicleTypeMapper, gbfsVehicleTypes);
    }
    return Map.of();
  }

  static <X> @Nullable I18NString optionalLocalizedString(
    @Nullable List<X> name,
    Function<X, String> language,
    Function<X, String> text
  ) {
    if (name == null || name.isEmpty()) {
      return null;
    }

    return TranslatedString.getI18NString(name.stream().collect(toMap(language, text)), false);
  }

  static <X> I18NString localizedString(
    List<X> name,
    Function<X, String> language,
    Function<X, String> text
  ) {
    var converted = optionalLocalizedString(name, language, text);

    if (converted == null) {
      throw new IllegalArgumentException("Empty localized field");
    }

    return converted;
  }
}
