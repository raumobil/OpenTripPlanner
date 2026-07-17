package org.opentripplanner.updater.vehicle_rental.datasources.gbfs.v3_1_RC3;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.mobilitydata.gbfs.v3_1_RC3.vehicle_availability.GBFSAvailability;
import org.mobilitydata.gbfs.v3_1_RC3.vehicle_availability.GBFSVehicle;
import org.opentripplanner.service.vehiclerental.model.RentalAvailability;
import org.opentripplanner.service.vehiclerental.model.RentalVehicleOnStation;
import org.opentripplanner.service.vehiclerental.model.VehicleRentalStation;

public class GbfsVehicleAvailabilityMapper {

  private final Map<String, List<GBFSVehicle>> gbfsVehiclesByStationId;

  public GbfsVehicleAvailabilityMapper(Map<String, List<GBFSVehicle>> gbfsVehiclesByStationId) {
    this.gbfsVehiclesByStationId = gbfsVehiclesByStationId;
  }

  public VehicleRentalStation mapStationAvailabilities(VehicleRentalStation station) {
    List<GBFSVehicle> gbfsVehicles = gbfsVehiclesByStationId.get(station.stationId());
    if (gbfsVehicles == null) {
      return station;
    }
    List<RentalVehicleOnStation> vehicles = gbfsVehicles
      .stream()
      .map(this::mapVehicleAvailability)
      .toList();
    return station.copyOf().withVehiclesOnStation(vehicles).build();
  }

  private RentalVehicleOnStation mapVehicleAvailability(GBFSVehicle vehicle) {
    List<RentalAvailability> availabilities = vehicle
      .getAvailabilities()
      .stream()
      .map(this::mapAvailability)
      .toList();
    return new RentalVehicleOnStation(vehicle.getVehicleId(), availabilities);
  }

  private RentalAvailability mapAvailability(GBFSAvailability gbfsAvailability) {
    Instant from = gbfsAvailability.getFrom().toInstant();
    Date untilDate = gbfsAvailability.getUntil();
    Instant until = untilDate != null ? untilDate.toInstant() : null;
    return new RentalAvailability(from, until);
  }
}
