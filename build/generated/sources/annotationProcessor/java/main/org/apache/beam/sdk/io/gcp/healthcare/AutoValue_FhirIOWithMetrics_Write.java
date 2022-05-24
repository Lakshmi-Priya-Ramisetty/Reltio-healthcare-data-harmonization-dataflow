package org.apache.beam.sdk.io.gcp.healthcare;

import javax.annotation.Generated;
import org.apache.beam.sdk.options.ValueProvider;

@Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_FhirIOWithMetrics_Write extends FhirIOWithMetrics.Write {

  private final ValueProvider<String> fhirStore;

  private final FhirIOWithMetrics.Write.WriteMethod writeMethod;

  private AutoValue_FhirIOWithMetrics_Write(
      ValueProvider<String> fhirStore,
      FhirIOWithMetrics.Write.WriteMethod writeMethod) {
    this.fhirStore = fhirStore;
    this.writeMethod = writeMethod;
  }

  @Override
  ValueProvider<String> getFhirStore() {
    return fhirStore;
  }

  @Override
  FhirIOWithMetrics.Write.WriteMethod getWriteMethod() {
    return writeMethod;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof FhirIOWithMetrics.Write) {
      FhirIOWithMetrics.Write that = (FhirIOWithMetrics.Write) o;
      return this.fhirStore.equals(that.getFhirStore())
          && this.writeMethod.equals(that.getWriteMethod());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h$ = 1;
    h$ *= 1000003;
    h$ ^= fhirStore.hashCode();
    h$ *= 1000003;
    h$ ^= writeMethod.hashCode();
    return h$;
  }

  static final class Builder extends FhirIOWithMetrics.Write.Builder {
    private ValueProvider<String> fhirStore;
    private FhirIOWithMetrics.Write.WriteMethod writeMethod;
    Builder() {
    }
    @Override
    FhirIOWithMetrics.Write.Builder setFhirStore(ValueProvider<String> fhirStore) {
      if (fhirStore == null) {
        throw new NullPointerException("Null fhirStore");
      }
      this.fhirStore = fhirStore;
      return this;
    }
    @Override
    FhirIOWithMetrics.Write.Builder setWriteMethod(FhirIOWithMetrics.Write.WriteMethod writeMethod) {
      if (writeMethod == null) {
        throw new NullPointerException("Null writeMethod");
      }
      this.writeMethod = writeMethod;
      return this;
    }
    @Override
    FhirIOWithMetrics.Write build() {
      String missing = "";
      if (this.fhirStore == null) {
        missing += " fhirStore";
      }
      if (this.writeMethod == null) {
        missing += " writeMethod";
      }
      if (!missing.isEmpty()) {
        throw new IllegalStateException("Missing required properties:" + missing);
      }
      return new AutoValue_FhirIOWithMetrics_Write(
          this.fhirStore,
          this.writeMethod);
    }
  }

}
