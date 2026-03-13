package org.opentripplanner.service.vehiclerental.model;

import java.util.List;
import javax.annotation.Nonnull;

public record RentalVehicleOnStation(@Nonnull String vehicleId, @Nonnull List<RentalAvailability> availabilities) {
}
