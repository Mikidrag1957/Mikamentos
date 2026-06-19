package com.mikamentos.app.ui.editor;

import androidx.lifecycle.SavedStateHandle;
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
public final class MedicationEditorViewModel_Factory implements Factory<MedicationEditorViewModel> {
  private final Provider<MedicationRepository> repositoryProvider;

  private final Provider<AlarmScheduler> alarmSchedulerProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public MedicationEditorViewModel_Factory(Provider<MedicationRepository> repositoryProvider,
      Provider<AlarmScheduler> alarmSchedulerProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.repositoryProvider = repositoryProvider;
    this.alarmSchedulerProvider = alarmSchedulerProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public MedicationEditorViewModel get() {
    return newInstance(repositoryProvider.get(), alarmSchedulerProvider.get(), savedStateHandleProvider.get());
  }

  public static MedicationEditorViewModel_Factory create(
      Provider<MedicationRepository> repositoryProvider,
      Provider<AlarmScheduler> alarmSchedulerProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new MedicationEditorViewModel_Factory(repositoryProvider, alarmSchedulerProvider, savedStateHandleProvider);
  }

  public static MedicationEditorViewModel newInstance(MedicationRepository repository,
      AlarmScheduler alarmScheduler, SavedStateHandle savedStateHandle) {
    return new MedicationEditorViewModel(repository, alarmScheduler, savedStateHandle);
  }
}
