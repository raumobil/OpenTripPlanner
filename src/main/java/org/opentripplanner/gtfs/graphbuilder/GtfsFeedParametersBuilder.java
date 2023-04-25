package org.opentripplanner.gtfs.graphbuilder;

import java.net.URI;
import org.opentripplanner.transit.model.site.StopTransferPriority;
import org.opentripplanner.updater.spi.HttpHeaders;

/**
 * Configure a GTFS feed.
 */
public class GtfsFeedParametersBuilder {

  private URI source;
  private HttpHeaders headers;
  private String feedId;
  private boolean removeRepeatedStops;
  private StopTransferPriority stationTransferPreference;
  private boolean discardMinTransferTimes;
  private boolean blockBasedInterlining;
  private int maxInterlineDistance;

  public GtfsFeedParametersBuilder() {
    this.removeRepeatedStops = GtfsFeedParameters.DEFAULT_REMOVE_REPEATED_STOPS;
    this.stationTransferPreference = GtfsFeedParameters.DEFAULT_STATION_TRANSFER_PREFERENCE;
    this.discardMinTransferTimes = GtfsFeedParameters.DEFAULT_DISCARD_MIN_TRANSFER_TIMES;
    this.blockBasedInterlining = GtfsFeedParameters.DEFAULT_BLOCK_BASED_INTERLINING;
    this.maxInterlineDistance = GtfsFeedParameters.DEFAULT_MAX_INTERLINE_DISTANCE;
  }

  public GtfsFeedParametersBuilder(GtfsFeedParameters original) {
    this.removeRepeatedStops = original.removeRepeatedStops();
    this.stationTransferPreference = original.stationTransferPreference();
    this.discardMinTransferTimes = original.discardMinTransferTimes();
    this.blockBasedInterlining = original.blockBasedInterlining();
    this.maxInterlineDistance = original.maxInterlineDistance();
  }

  public GtfsFeedParametersBuilder withFeedId(String feedId) {
    this.feedId = feedId;
    return this;
  }

  String feedId() {
    return feedId;
  }

  public GtfsFeedParametersBuilder withStationTransferPreference(
    StopTransferPriority stationTransferPreference
  ) {
    this.stationTransferPreference = stationTransferPreference;
    return this;
  }

  StopTransferPriority stationTransferPreference() {
    return stationTransferPreference;
  }

  public GtfsFeedParametersBuilder withSource(URI source) {
    this.source = source;
    return this;
  }

  URI source() {
    return source;
  }

  public GtfsFeedParametersBuilder withHeaders(HttpHeaders headers) {
    this.headers = headers;
    return this;
  }

  HttpHeaders headers() {
    return headers;
  }

  public GtfsFeedParametersBuilder withRemoveRepeatedStops(boolean value) {
    this.removeRepeatedStops = value;
    return this;
  }

  boolean removeRepeatedStops() {
    return removeRepeatedStops;
  }

  public GtfsFeedParametersBuilder withDiscardMinTransferTimes(boolean value) {
    this.discardMinTransferTimes = value;
    return this;
  }

  boolean discardMinTransferTimes() {
    return discardMinTransferTimes;
  }

  public GtfsFeedParametersBuilder withBlockBasedInterlining(boolean value) {
    this.blockBasedInterlining = value;
    return this;
  }

  boolean blockBasedInterlining() {
    return blockBasedInterlining;
  }

  public GtfsFeedParametersBuilder withMaxInterlineDistance(int value) {
    this.maxInterlineDistance = value;
    return this;
  }

  int maxInterlineDistance() {
    return maxInterlineDistance;
  }

  public GtfsFeedParameters build() {
    return new GtfsFeedParameters(this);
  }
}
