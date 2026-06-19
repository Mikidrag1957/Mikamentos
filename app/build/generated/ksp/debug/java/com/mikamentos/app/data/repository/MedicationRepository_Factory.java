package com.mikamentos.app.data.repository;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class MedicationRepository_Factory implements Factory<MedicationRepository> {
  private final Provider<Context> contextProvider;

  public MedicationRepository_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public MedicationRepository get() {
    return newInstance(contextProvider.get());
  }

  public static MedicationRepository_Factory create(Provider<Context> contextProvider) {
    return new MedicationRepository_Factory(contextProvider);
  }

  public static MedicationRepository newInstance(Context context) {
    return new MedicationRepository(context);
  }
}
