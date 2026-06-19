package com.mikamentos.app;

import com.mikamentos.app.data.repository.MedicationRepository;
import com.mikamentos.app.service.AlarmScheduler;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class MikamentosApp_MembersInjector implements MembersInjector<MikamentosApp> {
  private final Provider<MedicationRepository> repositoryProvider;

  private final Provider<AlarmScheduler> schedulerProvider;

  public MikamentosApp_MembersInjector(Provider<MedicationRepository> repositoryProvider,
      Provider<AlarmScheduler> schedulerProvider) {
    this.repositoryProvider = repositoryProvider;
    this.schedulerProvider = schedulerProvider;
  }

  public static MembersInjector<MikamentosApp> create(
      Provider<MedicationRepository> repositoryProvider,
      Provider<AlarmScheduler> schedulerProvider) {
    return new MikamentosApp_MembersInjector(repositoryProvider, schedulerProvider);
  }

  @Override
  public void injectMembers(MikamentosApp instance) {
    injectRepository(instance, repositoryProvider.get());
    injectScheduler(instance, schedulerProvider.get());
  }

  @InjectedFieldSignature("com.mikamentos.app.MikamentosApp.repository")
  public static void injectRepository(MikamentosApp instance, MedicationRepository repository) {
    instance.repository = repository;
  }

  @InjectedFieldSignature("com.mikamentos.app.MikamentosApp.scheduler")
  public static void injectScheduler(MikamentosApp instance, AlarmScheduler scheduler) {
    instance.scheduler = scheduler;
  }
}
