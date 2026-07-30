package dev.fogmobile.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.fogmobile.data.ConnectionManagerImpl
import dev.fogmobile.data.DataStoreAppPreferences
import dev.fogmobile.data.ProfileManagerImpl
import dev.fogmobile.data.ServiceHealthCheckerImpl
import dev.fogmobile.domain.AppPreferences
import dev.fogmobile.domain.ConnectionManager
import dev.fogmobile.domain.ConnectivityObserver
import dev.fogmobile.domain.ProfileManager
import dev.fogmobile.domain.ServiceHealthChecker
import dev.fogmobile.network.AndroidConnectivityObserver
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    @Singleton
    abstract fun bindPreferences(impl: DataStoreAppPreferences): AppPreferences

    @Binds
    @Singleton
    abstract fun bindConnectivityObserver(impl: AndroidConnectivityObserver): ConnectivityObserver

    @Binds
    @Singleton
    abstract fun bindHealthChecker(impl: ServiceHealthCheckerImpl): ServiceHealthChecker

    @Binds
    @Singleton
    abstract fun bindProfileManager(impl: ProfileManagerImpl): ProfileManager

    @Binds
    @Singleton
    abstract fun bindConnectionManager(impl: ConnectionManagerImpl): ConnectionManager
}
