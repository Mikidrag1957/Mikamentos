package com.mikamentos.app.data.network;

import com.mikamentos.app.data.repository.MedicationRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class DrugSearchRepository_Factory implements Factory<DrugSearchRepository> {
  private final Provider<FdaApiService> fdaApiProvider;

  private final Provider<CimaApiService> cimaApiProvider;

  private final Provider<EmaApiService> emaApiProvider;

  private final Provider<TranslationService> translationApiProvider;

  private final Provider<MedicationRepository> repositoryProvider;

  public DrugSearchRepository_Factory(Provider<FdaApiService> fdaApiProvider,
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
    return newInstance(fdaApiProvider.get(), cimaApiProvider.get(), emaApiProvider.get(), translationApiProvider.get(), repositoryProvider.get());
  }

  public static DrugSearchRepository_Factory create(Provider<FdaApiService> fdaApiProvider,
      Provider<CimaApiService> cimaApiProvider, Provider<EmaApiService> emaApiProvider,
      Provider<TranslationService> translationApiProvider,
      Provider<MedicationRepository> repositoryProvider) {
    return new DrugSearchRepository_Factory(fdaApiProvider, cimaApiProvider, emaApiProvider, translationApiProvider, repositoryProvider);
  }

  public static DrugSearchRepository newInstance(FdaApiService fdaApi, CimaApiService cimaApi,
      EmaApiService emaApi, TranslationService translationApi, MedicationRepository repository) {
    return new DrugSearchRepository(fdaApi, cimaApi, emaApi, translationApi, repository);
  }
}
