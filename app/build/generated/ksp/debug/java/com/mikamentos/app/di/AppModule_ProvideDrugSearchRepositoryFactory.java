package com.mikamentos.app.di;

import com.mikamentos.app.data.network.CimaApiService;
import com.mikamentos.app.data.network.DrugSearchRepository;
import com.mikamentos.app.data.network.EmaApiService;
import com.mikamentos.app.data.network.FdaApiService;
import com.mikamentos.app.data.network.TranslationService;
import com.mikamentos.app.data.repository.MedicationRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
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
public final class AppModule_ProvideDrugSearchRepositoryFactory implements Factory<DrugSearchRepository> {
  private final Provider<FdaApiService> fdaApiProvider;

  private final Provider<CimaApiService> cimaApiProvider;

  private final Provider<EmaApiService> emaApiProvider;

  private final Provider<TranslationService> translationApiProvider;

  private final Provider<MedicationRepository> repositoryProvider;

  public AppModule_ProvideDrugSearchRepositoryFactory(Provider<FdaApiService> fdaApiProvider,
      Provider<CimaApiService> cimaApiProvider, Provider<EmaApiService> emaApiProvider,
      Provider<TranslationService> translationApiProvider,
      Provider<MedicationRepository> repositoryProvider) {
    this.fdaApiProvider = fdaApiProvider;
    this.cimaApiProvider = cimaApiProvider;
    this.emaApiProvider = emaApiProvider;
    this.translationApiProvider = translationApiProvider;
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public DrugSearchRepository get() {
    return provideDrugSearchRepository(fdaApiProvider.get(), cimaApiProvider.get(), emaApiProvider.get(), translationApiProvider.get(), repositoryProvider.get());
  }

  public static AppModule_ProvideDrugSearchRepositoryFactory create(
      Provider<FdaApiService> fdaApiProvider, Provider<CimaApiService> cimaApiProvider,
      Provider<EmaApiService> emaApiProvider, Provider<TranslationService> translationApiProvider,
      Provider<MedicationRepository> repositoryProvider) {
    return new AppModule_ProvideDrugSearchRepositoryFactory(fdaApiProvider, cimaApiProvider, emaApiProvider, translationApiProvider, repositoryProvider);
  }

  public static DrugSearchRepository provideDrugSearchRepository(FdaApiService fdaApi,
      CimaApiService cimaApi, EmaApiService emaApi, TranslationService translationApi,
      MedicationRepository repository) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideDrugSearchRepository(fdaApi, cimaApi, emaApi, translationApi, repository));
  }
}
