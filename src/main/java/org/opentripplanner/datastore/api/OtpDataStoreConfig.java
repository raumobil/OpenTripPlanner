package org.opentripplanner.datastore.api;

import java.net.URI;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opentripplanner.datastore.OtpDataStore;
import org.opentripplanner.datastore.base.SourceParameter;

/**
 * The {@link OtpDataStore} config, needed to create a store. This should be provided by the OTP
 * application.
 */
public interface OtpDataStoreConfig {
  /**
   * Save the build issue report to this location (URI). If {@code null} the {@code baseDirectory}
   * + {@code /report} is used.
   */
  SourceParameter reportDirectory();

  /**
   * Local file system path to Google Cloud Platform service accounts credentials file. The
   * credentials is used to access GCS urls. When using GCS from outside of the bucket cluster you
   * need to provide a path the the service credentials.
   * <p>
   * This is a path to a file on the local file system, not an URI.
   * <p>
   * Optional. May return {@code null}.
   */
  String gsCredentials();

  /**
   * Array of URIs to the open street map pbf files (the pbf format is the only one supported).
   * <p>
   * This parameter is optional. If {@code null} OSM files are loaded from {@code baseDirectory}.
   */
  List<SourceParameter> osmFiles();

  /**
   * Array of URIs to elevation data files.
   * <p>
   * This parameter is optional. If {@code null} DEM files are loaded from {@code baseDirectory}.
   */
  List<SourceParameter> demFiles();

  /**
   * Array of URIs to GTFS data files .
   * <p>
   * This parameter is optional. If {@code null} GTFS files are loaded from {@code baseDirectory}.
   */
  @Nonnull
  List<SourceParameter> gtfsDownloadResources();

  /**
   * Array of URIs to Netex data files.
   * <p>
   * This parameter is optional. If {@code null} Netex files are loaded from {@code baseDirectory}.
   */
  @Nonnull
  List<SourceParameter> netexFiles();

  /**
   * The URI to the graph object file to load and/or save.
   */
  SourceParameter graph();

  /**
   * The URI to the street graph object file to load and/or save.
   */
  SourceParameter streetGraph();

  /**
   *
   * A pattern to lookup local GTFS files
   */
  Pattern gtfsLocalFilePattern();

  /**
   * A pattern to lookup local NeTEx files.
   */
  Pattern netexLocalFilePattern();

  /**
   * A pattern to lookup local Open Street Map extracts.
   */
  Pattern osmLocalFilePattern();

  /**
   * A pattern to lookup local DEM files.
   */
  Pattern demLocalFilePattern();
}
