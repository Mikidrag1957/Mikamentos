package com.mikamentos.app.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import retrofit2.Retrofit;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("javax.inject.Named")
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
public final class AppModule_ProvideEmaRetrofitFactory implements Factory<Retrofit> {
  @Override
  public Retrofit get() {
    return provideEmaRetrofit();
  }

  public static AppModule_ProvideEmaRetrofitFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static Retrofit provideEmaRetrofit() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideEmaRetrofit());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideEmaRetrofitFactory INSTANCE = new AppModule_ProvideEmaRetrofitFactory();
  }
}
