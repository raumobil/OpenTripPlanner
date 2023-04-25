package org.opentripplanner.datastore.api;

import java.net.URI;
import java.util.Map;

public record DownloadResource(URI uri, Map<String, String> headers) {
}
