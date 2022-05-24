package com.google.cloud.healthcare.etl.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import javax.annotation.Generated;

@Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_QueryOptions extends QueryOptions {

  private final Map<String, String> queries;

  private AutoValue_QueryOptions(
      Map<String, String> queries) {
    this.queries = queries;
  }

  @JsonProperty
  @Override
  public Map<String, String> queries() {
    return queries;
  }

  @Override
  public String toString() {
    return "QueryOptions{"
         + "queries=" + queries
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof QueryOptions) {
      QueryOptions that = (QueryOptions) o;
      return this.queries.equals(that.queries());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h$ = 1;
    h$ *= 1000003;
    h$ ^= queries.hashCode();
    return h$;
  }

  static final class Builder extends QueryOptions.Builder {
    private Map<String, String> queries;
    Builder() {
    }
    @Override
    QueryOptions.Builder setQueries(Map<String, String> queries) {
      if (queries == null) {
        throw new NullPointerException("Null queries");
      }
      this.queries = queries;
      return this;
    }
    @Override
    QueryOptions build() {
      String missing = "";
      if (this.queries == null) {
        missing += " queries";
      }
      if (!missing.isEmpty()) {
        throw new IllegalStateException("Missing required properties:" + missing);
      }
      return new AutoValue_QueryOptions(
          this.queries);
    }
  }

}
