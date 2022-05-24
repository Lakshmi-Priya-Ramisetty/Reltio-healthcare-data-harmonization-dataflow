package org.apache.beam.sdk.io.gcp.healthcare;

import javax.annotation.Generated;
import org.apache.beam.sdk.options.ValueProvider;

@Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_HL7v2IO_Write extends HL7v2IO.Write {

  private final ValueProvider<String> HL7v2Store;

  private final HL7v2IO.Write.WriteMethod writeMethod;

  private AutoValue_HL7v2IO_Write(
      ValueProvider<String> HL7v2Store,
      HL7v2IO.Write.WriteMethod writeMethod) {
    this.HL7v2Store = HL7v2Store;
    this.writeMethod = writeMethod;
  }

  @Override
  ValueProvider<String> getHL7v2Store() {
    return HL7v2Store;
  }

  @Override
  HL7v2IO.Write.WriteMethod getWriteMethod() {
    return writeMethod;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof HL7v2IO.Write) {
      HL7v2IO.Write that = (HL7v2IO.Write) o;
      return this.HL7v2Store.equals(that.getHL7v2Store())
          && this.writeMethod.equals(that.getWriteMethod());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h$ = 1;
    h$ *= 1000003;
    h$ ^= HL7v2Store.hashCode();
    h$ *= 1000003;
    h$ ^= writeMethod.hashCode();
    return h$;
  }

  static final class Builder extends HL7v2IO.Write.Builder {
    private ValueProvider<String> HL7v2Store;
    private HL7v2IO.Write.WriteMethod writeMethod;
    Builder() {
    }
    @Override
    HL7v2IO.Write.Builder setHL7v2Store(ValueProvider<String> HL7v2Store) {
      if (HL7v2Store == null) {
        throw new NullPointerException("Null HL7v2Store");
      }
      this.HL7v2Store = HL7v2Store;
      return this;
    }
    @Override
    HL7v2IO.Write.Builder setWriteMethod(HL7v2IO.Write.WriteMethod writeMethod) {
      if (writeMethod == null) {
        throw new NullPointerException("Null writeMethod");
      }
      this.writeMethod = writeMethod;
      return this;
    }
    @Override
    HL7v2IO.Write build() {
      String missing = "";
      if (this.HL7v2Store == null) {
        missing += " HL7v2Store";
      }
      if (this.writeMethod == null) {
        missing += " writeMethod";
      }
      if (!missing.isEmpty()) {
        throw new IllegalStateException("Missing required properties:" + missing);
      }
      return new AutoValue_HL7v2IO_Write(
          this.HL7v2Store,
          this.writeMethod);
    }
  }

}
