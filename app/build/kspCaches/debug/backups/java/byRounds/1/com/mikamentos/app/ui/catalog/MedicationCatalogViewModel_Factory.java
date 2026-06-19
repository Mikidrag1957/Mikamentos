package com.mikamentos.app.ui.catalog;

import com.mikamentos.app.data.network.DrugSearchRepository;
import com.mikamentos.app.data.repository.MedicationRepository;
import com.mikamentos.app.service.TtsManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class MedicationCatalogViewModel_Factory implements Factory<MedicationCatalogViewModel> {
  private final Provider<MedicationRepository> repositoryProvider;

  private final Provider<DrugSearchRepository> drugSearchRepositoryProvider;

  private final Provider<TtsManager> ttsManagerProvider;

  public MedicationCatalogViewModel_Factory(Provider<MedicationRepository> repositoryProvider,
      Provider<DrugSearchRepository> drugSearchRepositoryProvider,
      Provider<TtsManager> ttsManagerProvider) {
    this.repositoryProvider = repositoryProvider;
    this.drugSearchRepositoryProvider = drugSearchRepositoryProvider;
    this.ttsManagerProvider = ttsManagerProvider;
  }

  @Override
  public MedicationCatalogViewModel get() {
    return newInstance(repositoryProvider.get(), drugSearchRepositoryProvider.get(), ttsManagerProvider.get());
  }

  public static MedicationCatalogViewModel_Factory create(
      Provider<MedicationRepository> repositoryProvider,
      Provider<DrugSearchRepository> drugSearchRepositoryProvider,
      Provider<TtsManager> ttsManagerProvider) {
    return new MedicationCatalogViewModel_Factory(repositoryProvider, drugSearchRepositoryProvider, ttsManagerProvider);
  }

  public static MedicationCatalogViewModel newInstance(MedicationRepository repository,
      DrugSearchRepository drugSearchRepository, TtsManager ttsManager) {
    return new MedicationCatalogViewModel(repository, drugSearchRepository, ttsManager);
  }
}
