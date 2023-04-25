package org.opentripplanner.datastore.https;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.apache.http.Header;
import org.opentripplanner.datastore.api.CompositeDataSource;
import org.opentripplanner.datastore.api.DataSource;
import org.opentripplanner.datastore.api.FileType;
import org.opentripplanner.datastore.base.DataSourceRepository;
import org.opentripplanner.datastore.base.SourceParameter;
import org.opentripplanner.datastore.file.ZipStreamDataSourceDecorator;
import org.opentripplanner.framework.io.HttpUtils;

/**
 * This data store accesses files in read-only mode over HTTPS.
 */
public class HttpsDataSourceRepository implements DataSourceRepository {

  private static final Duration HTTP_HEAD_REQUEST_TIMEOUT = Duration.ofSeconds(20);

  @Override
  public String description() {
    return "HTTPS";
  }

  @Override
  public void open() {}

  @Override
  public DataSource findSource(@Nonnull SourceParameter sourceParameter) {
    if (sourceParameter.uri() != null && skipUri(sourceParameter.uri())) {
      return null;
    }
    return createSource(sourceParameter);
  }

  @Override
  public CompositeDataSource findCompositeSource(@Nonnull SourceParameter sourceParameter) {
    if (skipUri(sourceParameter.uri())) {
      return null;
    }
    return createCompositeSource(sourceParameter);
  }

  /* private methods */

  private static boolean skipUri(URI uri) {
    return !"https".equals(uri.getScheme());
  }

  private DataSource createSource(SourceParameter sourceParameter) {
    HttpsDataSourceMetadata httpsDataSourceMetadata = new HttpsDataSourceMetadata(
      getHttpHeaders(sourceParameter.uri())
    );
    return new HttpsFileDataSource(sourceParameter, httpsDataSourceMetadata);
  }

  private CompositeDataSource createCompositeSource(SourceParameter sourceParameter) {
    HttpsDataSourceMetadata httpsDataSourceMetadata = new HttpsDataSourceMetadata(
      getHttpHeaders(sourceParameter.uri())
    );

    if (httpsDataSourceMetadata.isZipContentType() || sourceParameter.uri().getPath().endsWith(".zip")) {
      DataSource httpsSource = new HttpsFileDataSource(sourceParameter, httpsDataSourceMetadata);
      return new ZipStreamDataSourceDecorator(httpsSource);
    } else {
      throw new UnsupportedOperationException(
        "Only ZIP archives are supported as composite sources for the HTTPS data source"
      );
    }
  }

  protected List<Header> getHttpHeaders(URI uri) {
    return HttpUtils.getHeaders(uri, HTTP_HEAD_REQUEST_TIMEOUT, Map.of());
  }
}
