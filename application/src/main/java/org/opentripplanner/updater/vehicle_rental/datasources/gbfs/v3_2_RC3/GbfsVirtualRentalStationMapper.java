package org.opentripplanner.updater.vehicle_rental.datasources.gbfs.v3_2_RC3;

import static org.opentripplanner.updater.vehicle_rental.datasources.gbfs.v3_2_RC3.GbfsFeedMapper.localizedString;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import org.mobilitydata.gbfs.v3_2_RC3.station_information.GBFSName;
import org.mobilitydata.gbfs.v3_2_RC3.station_information.GBFSRentalUris;
import org.mobilitydata.gbfs.v3_2_RC3.station_information.GBFSStation;
import org.mobilitydata.gbfs.v3_2_RC3.virtual_station_probabilites.GBFSVirtualStation;
import org.opentripplanner.core.model.id.FeedScopedId;
import org.opentripplanner.service.vehiclerental.model.VehicleRentalStationUris;
import org.opentripplanner.service.vehiclerental.model.VehicleRentalSystem;
import org.opentripplanner.service.vehiclerental.model.VirtualRentalStation;
import org.opentripplanner.street.model.RentalFormFactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maps a GBFS station_information entry together with its virtual station to a
 * {@link VirtualRentalStation}. Virtual stations have no real-time status, so there is no
 * station_status counterpart to this mapper.
 */
class GbfsVirtualRentalStationMapper {

  private static final Logger LOG = LoggerFactory.getLogger(GbfsVirtualRentalStationMapper.class);

  private final VehicleRentalSystem system;
  private final RentalFormFactor formFactor;
  private final Map<String, List<GBFSVirtualStation>> virtualByStationsById;

  /**
   * @param virtualByStationsById virtual stations keyed by the station_id of the corresponding
   *                              station_information entry.
   */
  public GbfsVirtualRentalStationMapper(
    VehicleRentalSystem system,
    RentalFormFactor formFactor,
    Map<String, List<GBFSVirtualStation>> virtualByStationsById
  ) {
    this.system = Objects.requireNonNull(system);
    this.formFactor = Objects.requireNonNull(formFactor);
    this.virtualByStationsById = Objects.requireNonNull(virtualByStationsById);
  }

  /**
   * Returns a {@link VirtualRentalStation} for the given station, or {@code null} if the station
   * is invalid or has no virtual station with a valid probability. If there are several virtual
   * stations for the station, the first one with a valid probability is used.
   */
  @Nullable
  public VirtualRentalStation mapStationInformation(GBFSStation station) {
    if (!GbfsStationInformationMapper.isValid(station)) {
      LOG.debug(
        "GBFS station for {} system has issues with required fields: \n{}",
        system.systemId(),
        station
      );
      return null;
    }

    var virtualStations = virtualByStationsById.get(station.getStationId());
    if (virtualStations == null || virtualStations.isEmpty()) {
      return null;
    }

    Integer probability = null;
    for (var virtualStation : virtualStations) {
      var candidate = virtualStation.getProbability();
      if (isValidProbability(candidate)) {
        probability = candidate;
        break;
      }
      LOG.debug(
        "Virtual station of GBFS station {} in {} system has an invalid probability: {}",
        station.getStationId(),
        system.systemId(),
        candidate
      );
    }
    if (probability == null) {
      return null;
    }

    return new VirtualRentalStation(
      new FeedScopedId(system.systemId(), station.getStationId()),
      localizedString(station.getName(), GBFSName::getLanguage, GBFSName::getText),
      station.getLon(),
      station.getLat(),
      formFactor,
      probability,
      system,
      mapRentalUris(station.getRentalUris())
    );
  }

  private static boolean isValidProbability(@Nullable Integer probability) {
    return (probability != null && probability >= 0 && probability <= 100);
  }

  @Nullable
  private static VehicleRentalStationUris mapRentalUris(@Nullable GBFSRentalUris rentalUris) {
    if (rentalUris == null) {
      return null;
    }
    return VehicleRentalStationUris.of()
      .withAndroid(rentalUris.getAndroid())
      .withIos(rentalUris.getIos())
      .withWeb(rentalUris.getWeb())
      .build();
  }
}
