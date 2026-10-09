package org.opentripplanner.service.vehiclerental.model;

import static java.util.Locale.ROOT;

import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import org.opentripplanner.core.model.i18n.I18NString;
import org.opentripplanner.core.model.id.FeedScopedId;
import org.opentripplanner.street.model.RentalFormFactor;

/**
 * A virtual vehicle rental station. In contrast to {@link VehicleRentalStation} it has no real-time
 * data: availability and capacity are static. Instead, it carries a {@link #probability()} that at
 * least one vehicle is available at the station.
 * <p>
 * The station is pickup-only: vehicles can be rented here, but not returned.
 */
public final class VirtualRentalStation implements VehicleRentalPlace {

  private final FeedScopedId id;
  private final I18NString name;
  private final double longitude;
  private final double latitude;
  private final RentalFormFactor formFactor;
  private final int probability;
  private final VehicleRentalSystem system;
  private final VehicleRentalStationUris rentalUris;

  /**
   * @param probability probability in the range [0, 1] that at least one vehicle is available.
   */
  public VirtualRentalStation(
    FeedScopedId id,
    I18NString name,
    double longitude,
    double latitude,
    RentalFormFactor formFactor,
    int probability,
    @Nullable VehicleRentalSystem system,
    @Nullable VehicleRentalStationUris rentalUris
  ) {
    this.id = Objects.requireNonNull(id);
    this.name = name;
    this.longitude = longitude;
    this.latitude = latitude;
    this.formFactor = Objects.requireNonNull(formFactor);
    this.probability = probability;
    this.system = system;
    this.rentalUris = rentalUris;
  }

  /** Probability (0..1) that at least one vehicle is available at this station. */
  public int probability() {
    return probability;
  }

  @Override
  public FeedScopedId id() {
    return id;
  }

  @Override
  public String stationId() {
    return id.getId();
  }

  @Override
  public String network() {
    return id.getFeedId();
  }

  @Override
  public I18NString name() {
    return name;
  }

  @Override
  public double longitude() {
    return longitude;
  }

  @Override
  public double latitude() {
    return latitude;
  }

  /** Static: a virtual station always counts as having one vehicle. */
  @Override
  public int vehiclesAvailable() {
    return 1;
  }

  /** Static: vehicles cannot be returned to a virtual station. */
  @Override
  public int spacesAvailable() {
    return 0;
  }

  /** Static: always 1. */
  @Override
  public Integer capacity() {
    return 1;
  }

  @Override
  public boolean isAllowDropoff() {
    return false;
  }

  @Override
  public boolean overloadingAllowed() {
    return false;
  }

  @Override
  public boolean isAllowPickup() {
    return true;
  }

  @Override
  public boolean allowPickupNow() {
    return true;
  }

  @Override
  public boolean allowDropoffNow() {
    return false;
  }

  @Override
  public boolean isFloatingVehicle() {
    return false;
  }

  @Override
  public boolean isCarStation() {
    return RentalFormFactor.CAR.equals(formFactor);
  }

  @Override
  public Set<RentalFormFactor> availablePickupFormFactors(boolean includeRealtimeAvailability) {
    return Set.of(formFactor);
  }

  @Override
  public Set<RentalFormFactor> availableDropoffFormFactors(boolean includeRealtimeAvailability) {
    return Set.of();
  }

  @Override
  public boolean canDropOffFormFactor(
    RentalFormFactor formFactor,
    boolean includeRealtimeAvailability
  ) {
    return false;
  }

  @Override
  public boolean isArrivingInRentalVehicleAtDestinationAllowed() {
    return false;
  }

  @Override
  public boolean isRealTimeData() {
    return false;
  }

  @Override
  @Nullable
  public VehicleRentalStationUris rentalUris() {
    return rentalUris;
  }

  @Override
  @Nullable
  public VehicleRentalSystem vehicleRentalSystem() {
    return system;
  }

  @Override
  public String toString() {
    return String.format(
      ROOT,
      "Virtual vehicle rental station %s at %.6f, %.6f (probability %.2f)",
      name,
      latitude,
      longitude,
      probability
    );
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    VirtualRentalStation that = (VirtualRentalStation) o;
    return (
      Double.compare(that.longitude, longitude) == 0 &&
        Double.compare(that.latitude, latitude) == 0 &&
        Double.compare(that.probability, probability) == 0 &&
        formFactor == that.formFactor &&
        Objects.equals(id, that.id) &&
        Objects.equals(name, that.name) &&
        Objects.equals(system, that.system) &&
        Objects.equals(rentalUris, that.rentalUris)
    );
  }

  @Override
  public int hashCode() {
    return Objects.hash(
      id,
      name,
      longitude,
      latitude,
      formFactor,
      probability,
      system,
      rentalUris
    );
  }
}
