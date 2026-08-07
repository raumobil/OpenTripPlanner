package org.opentripplanner.updater.vehicle_rental.datasources.gbfs.v3_1_RC3;

import static org.mobilitydata.gbfs.v3_1_RC3.gbfs.GBFSFeed.Name.VEHICLE_AVAILABILITY;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.mobilitydata.gbfs.v3_1_RC3.gbfs.GBFSFeed;
import org.mobilitydata.gbfs.v3_1_RC3.gbfs.GBFSFeedName;
import org.mobilitydata.gbfs.v3_1_RC3.gbfs.GBFSGbfs;
import org.mobilitydata.gbfs.v3_1_RC3.vehicle_availability.GBFSVehicleAvailability;
import org.opentripplanner.framework.io.OtpHttpClient;
import org.opentripplanner.updater.spi.HttpHeaders;
import org.opentripplanner.updater.spi.UpdaterConstructionException;
import org.opentripplanner.updater.vehicle_rental.datasources.gbfs.GbfsFeedDetails;
import org.opentripplanner.updater.vehicle_rental.datasources.gbfs.GbfsFeedLoaderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for managing the state and loading of complete GBFS version 3.1 datasets, and updating them
 * according to individual feed's TTL rules.
 */
public class GbfsFeedLoader
  extends GbfsFeedLoaderImpl<GBFSFeed.Name, GbfsFeedLoader.GBFSFeedV31Details> {

  private static final Logger LOG = LoggerFactory.getLogger(GbfsFeedLoader.class);

  public GbfsFeedLoader(String url, HttpHeaders httpHeaders, OtpHttpClient otpHttpClient) {
    super(fetchFeedInfo(url, httpHeaders, otpHttpClient), httpHeaders, otpHttpClient);
  }

  private static List<GBFSFeedV31Details> fetchFeedInfo(
    String url,
    HttpHeaders httpHeaders,
    OtpHttpClient otpHttpClient
  ) {
    URI uri;
    try {
      uri = new URI(url);
    } catch (URISyntaxException e) {
      throw new UpdaterConstructionException("Invalid url " + url);
    }

    // Fetch autoconfiguration file
    GBFSGbfs data = fetchFeed(uri, httpHeaders, otpHttpClient, GBFSGbfs.class);
    if (data == null) {
      if (!url.endsWith("gbfs.json")) {
        LOG.warn(
          "GBFS autoconfiguration url {} does not end with gbfs.json. Make sure it follows the specification, if you get any errors using it.",
          url
        );
      }
      throw new UpdaterConstructionException(
        "Could not fetch the feed auto-configuration file from " + uri
      );
    }

    return data.getData().getFeeds().stream().map(GBFSFeedV31Details::new).toList();
  }

  @Override
  protected <T> GBFSFeed.Name nameForClass(Class<T> feed) {
    //todo remove this workaround, if mobilitydata has added the mapping inside GBFSFeedName in lib.
    if (feed == GBFSVehicleAvailability.class) {
      return VEHICLE_AVAILABILITY;
    }

    return GBFSFeedName.fromClass(feed);
  }

  @Override
  protected <T> Class<T> classForName(GBFSFeed.Name name) {
    //todo remove this workaround, if mobilitydata has added the mapping inside GBFSFeedName in lib.
    if (name == VEHICLE_AVAILABILITY) {
      return (Class<T>) GBFSVehicleAvailability.class;
    }

    return (Class<T>) GBFSFeedName.implementingClass(name);
  }

  /* private static classes */

  protected static class GBFSFeedV31Details implements GbfsFeedDetails<GBFSFeed.Name> {

    private final GBFSFeed feed;

    private GBFSFeedV31Details(GBFSFeed feed) {
      this.feed = feed;
    }

    @Override
    public GBFSFeed.Name getName() {
      return feed.getName();
    }

    @Override
    public URI getUrl() {
      return URI.create(feed.getUrl());
    }
  }
}
