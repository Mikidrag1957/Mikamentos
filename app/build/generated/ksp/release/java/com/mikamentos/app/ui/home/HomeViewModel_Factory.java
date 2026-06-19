package com.mikamentos.app.ui.home;

import com.mikamentos.app.data.network.DrugSearchRepository;
import com.mikamentos.app.data.repository.MedicationRepository;
import com.mikamentos.app.service.AlarmScheduler;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<MedicationRepository> repositoryProvider;

  private final Provider<AlarmScheduler> alarmSchedulerProvider;

  private final Provider<DrugSearchRepository> drugSearchRepositoryProvider;

  public HomeViewModel_Factory(Provider<MedicationRepository> repositoryProvider,
      Provider<AlarmScheduler> alarmSchedulerProvider,
      Provider<DrugSearchRepository> drugSearchRepositoryProvider) {
    this.repositoryProvider = repositoryProvider;
    this.alarmSchedulerProvider = alarmSchedulerProvider;
    this.drugSearchRepositoryProvider = drugSearchRepositoryProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(repositoryProvider.get(), alarmSchedulerProvider.get(), drugSearchRepositoryProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<MedicationRepository> repositoryProvider,
      Provider<AlarmScheduler> alarmSchedulerProvider,
      Provider<DrugSearchRepository> drugSearchRepositoryProvider) {
    return new HomeViewModel_Factory(repositoryProvider, alarmSchedulerProvider, drugSearchRepositoryProvider);
  }

  public static HomeViewModel newInstance(MedicationRepository repository,
      AlarmScheduler alarmScheduler, DrugSearchRepository drugSearchRepository) {
    return new HomeViewModel(repository, alarmScheduler, drugSearchRepository);
  }
}
