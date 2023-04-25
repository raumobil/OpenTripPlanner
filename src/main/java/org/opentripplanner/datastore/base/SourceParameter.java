package org.opentripplanner.datastore.base;

import java.net.URI;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentripplanner.datastore.api.FileType;

public record SourceParameter(@Nullable URI uri, @Nonnull FileType type, Map<String, String> headers) {
}
