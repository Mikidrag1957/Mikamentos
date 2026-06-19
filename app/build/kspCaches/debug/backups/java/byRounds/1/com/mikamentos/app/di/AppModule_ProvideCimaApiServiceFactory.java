package com.mikamentos.app.di;

import com.mikamentos.app.data.network.CimaApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
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
public final class AppModule_ProvideCimaApiServiceFactory implements Factory<CimaApiService> {
  private final Provider<Retrofit> retrofitProvider;

  public AppModule_ProvideCimaApiServiceFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public CimaApiService get() {
    return provideCimaApiService(retrofitProvider.get());
  }

  public static AppModule_ProvideCimaApiServiceFactory create(Provider<Retrofit> retrofitProvider) {
    return new AppModule_ProvideCimaApiServiceFactory(retrofitProvider);
  }

  public static CimaApiService provideCimaApiService(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideCimaApiService(retrofit));
  }
}
