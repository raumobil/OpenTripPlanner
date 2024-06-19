package org.opentripplanner.ext.geocoder.configure;

import dagger.Module;
import dagger.Provides;
import jakarta.inject.Singleton;
import javax.annotation.Nullable;
import org.opentripplanner.ext.geocoder.LuceneIndex;
import org.opentripplanner.ext.stopconsolidation.StopConsolidationService;
import org.opentripplanner.framework.application.OTPFeature;
import org.opentripplanner.transit.service.TransitService;

/**
 * This module builds the Lucene geocoder based on whether the feature flag is on or off.
 */
@Module
public class GeocoderModule {

  @Provides
  @Singleton
  @Nullable
  LuceneIndex luceneIndex(
    TransitService service,
    @Nullable StopConsolidationService stopConsolidationService
  ) {
    if (OTPFeature.SandboxAPIGeocoder.isOn()) {
      return new LuceneIndex(service, stopConsolidationService);
    } else {
      return null;
    }
  }
}
