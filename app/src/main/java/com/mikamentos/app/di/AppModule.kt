package com.mikamentos.app.di

import com.mikamentos.app.data.network.CimaApiService
import com.mikamentos.app.data.network.DrugSearchRepository
import com.mikamentos.app.data.network.EmaApiService
import com.mikamentos.app.data.network.FdaApiService
import com.mikamentos.app.data.network.TranslationService
import com.mikamentos.app.data.repository.MedicationRepository
import com.mikamentos.app.service.AlarmScheduler
import com.mikamentos.app.service.TtsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import android.content.Context
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @Named("fda")
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.fda.gov/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("cima")
    fun provideCimaRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://cima.aemps.es/cima/rest/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("ema")
    fun provideEmaRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://epi.developer.ema.europa.eu/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("translation")
    fun provideTranslationRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.mymemory.translated.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideFdaApiService(@Named("fda") retrofit: Retrofit): FdaApiService {
        return retrofit.create(FdaApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideCimaApiService(@Named("cima") retrofit: Retrofit): CimaApiService {
        return retrofit.create(CimaApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideEmaApiService(@Named("ema") retrofit: Retrofit): EmaApiService {
        return retrofit.create(EmaApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideTranslationService(@Named("translation") retrofit: Retrofit): TranslationService {
        return retrofit.create(TranslationService::class.java)
    }

    @Provides
    @Singleton
    fun provideDrugSearchRepository(
        fdaApi: FdaApiService,
        cimaApi: CimaApiService,
        emaApi: EmaApiService,
        translationApi: TranslationService,
        repository: MedicationRepository
    ): DrugSearchRepository {
        return DrugSearchRepository(fdaApi, cimaApi, emaApi, translationApi, repository)
    }

    @Provides
    @Singleton
    fun provideMedicationRepository(@ApplicationContext context: Context): MedicationRepository {
        return MedicationRepository(context)
    }

    @Provides
    @Singleton
    fun provideAlarmScheduler(@ApplicationContext context: Context): AlarmScheduler {
        return AlarmScheduler(context)
    }

    @Provides
    @Singleton
    fun provideTtsManager(@ApplicationContext context: Context): TtsManager {
        return TtsManager(context)
    }
}
