package org.opentripplanner.service.vehiclerental.model;

import java.time.Instant;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record RentalAvailability(Instant from, Instant until) {
  public RentalAvailability(@Nonnull Instant from, @Nullable Instant until) {
    this.from = from;
    this.until = until;
  }
}
