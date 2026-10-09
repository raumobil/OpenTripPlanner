package org.opentripplanner.apis.gtfs.datafetchers;

import static org.opentripplanner.framework.graphql.GraphQLUtils.getLocale;

import graphql.relay.Relay;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.opentripplanner.apis.gtfs.generated.GraphQLDataFetchers;
import org.opentripplanner.service.vehiclerental.model.VirtualRentalStation;

public class VirtualRentalStationImpl implements GraphQLDataFetchers.GraphQLVirtualRentalStation {

  @Override
  public DataFetcher<Relay.ResolvedGlobalId> id() {
    return environment ->
      new Relay.ResolvedGlobalId("VirtualRentalStation", getSource(environment).id().toString());
  }

  @Override
  public DataFetcher<Double> lat() {
    return environment -> getSource(environment).latitude();
  }

  @Override
  public DataFetcher<Double> lon() {
    return environment -> getSource(environment).longitude();
  }

  @Override
  public DataFetcher<String> name() {
    return environment -> getSource(environment).name().toString(getLocale(environment));
  }

  @Override
  public DataFetcher<Integer> probability() {
    return environment -> getSource(environment).probability();
  }

  @Override
  public DataFetcher<String> stationId() {
    return environment -> getSource(environment).id().toString();
  }

  private VirtualRentalStation getSource(DataFetchingEnvironment environment) {
    return environment.getSource();
  }
}
