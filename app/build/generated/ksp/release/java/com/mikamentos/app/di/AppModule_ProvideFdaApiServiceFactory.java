package com.mikamentos.app.di;

import com.mikamentos.app.data.network.FdaApiService;
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
public final class AppModule_ProvideFdaApiServiceFactory implements Factory<FdaApiService> {
  private final Provider<Retrofit> retrofitProvider;

  public AppModule_ProvideFdaApiServiceFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public FdaApiService get() {
    return provideFdaApiService(retrofitProvider.get());
  }

  public static AppModule_ProvideFdaApiServiceFactory create(Provider<Retrofit> retrofitProvider) {
    return new AppModule_ProvideFdaApiServiceFactory(retrofitProvider);
  }

  public static FdaApiService provideFdaApiService(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideFdaApiService(retrofit));
  }
}
