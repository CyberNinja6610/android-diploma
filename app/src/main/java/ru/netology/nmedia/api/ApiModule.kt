package ru.netology.nmedia.api

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.auth.AppAuth
import javax.inject.Singleton

private const val BASE_URL = "${BuildConfig.BASE_URL}/api/"

fun okhttp(vararg interceptors: Interceptor): OkHttpClient = OkHttpClient.Builder()
    .apply {
        interceptors.forEach {
            this.addInterceptor(it)
        }
    }
    .build()

fun retrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .client(client)
    .build()

@InstallIn(SingletonComponent::class)
@Module
class ApiModule {

    @Singleton
    @Provides
    fun providePostApi(auth: AppAuth):PostApi = retrofit(okhttp(loggingInterceptor(), authInterceptor(auth))).create(PostApi::class.java)

    @Singleton
    @Provides
    fun provideEventApi(auth: AppAuth):EventApi = retrofit(okhttp(loggingInterceptor(), authInterceptor(auth))).create(EventApi::class.java)

    @Singleton
    @Provides
    fun provideJobApi(auth: AppAuth): JobApi = retrofit(okhttp(loggingInterceptor(), authInterceptor(auth))).create(JobApi::class.java)

    @Singleton
    @Provides
    fun provideUserApi(auth: AppAuth): UserApi = retrofit(okhttp(loggingInterceptor(), authInterceptor(auth))).create(UserApi::class.java)

    @Singleton
    @Provides
    fun provideMediaApi(auth: AppAuth): MediaApi = retrofit(okhttp(loggingInterceptor(), authInterceptor(auth))).create(MediaApi::class.java)
}