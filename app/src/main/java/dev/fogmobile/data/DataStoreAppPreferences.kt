package dev.fogmobile.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.fogmobile.core.NetworkKind
import dev.fogmobile.domain.AppPreferences
import dev.fogmobile.domain.PreferencesState
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("fog_mobile")

class DataStoreAppPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) : AppPreferences {
    override val state: Flow<PreferencesState> = context.dataStore.data.map { preferences ->
        PreferencesState(
            onboardingComplete = preferences[Keys.onboardingComplete] ?: false,
            autoCheckAfterNetworkChange = preferences[Keys.autoCheckAfterNetworkChange] ?: true,
            startProtectionOnLaunch = preferences[Keys.startProtectionOnLaunch] ?: false,
            lastWifiProfileId = preferences[Keys.lastWifiProfileId],
            lastMobileProfileId = preferences[Keys.lastMobileProfileId],
        )
    }

    override suspend fun setOnboardingComplete(value: Boolean) {
        context.dataStore.edit { it[Keys.onboardingComplete] = value }
    }

    override suspend fun setAutoCheckAfterNetworkChange(value: Boolean) {
        context.dataStore.edit { it[Keys.autoCheckAfterNetworkChange] = value }
    }

    override suspend fun setStartProtectionOnLaunch(value: Boolean) {
        context.dataStore.edit { it[Keys.startProtectionOnLaunch] = value }
    }

    override suspend fun rememberProfile(kind: NetworkKind, profileId: String) {
        context.dataStore.edit {
            when (kind) {
                NetworkKind.WIFI -> it[Keys.lastWifiProfileId] = profileId
                NetworkKind.MOBILE -> it[Keys.lastMobileProfileId] = profileId
                else -> Unit
            }
        }
    }

    private object Keys {
        val onboardingComplete = booleanPreferencesKey("onboarding_complete")
        val autoCheckAfterNetworkChange = booleanPreferencesKey("auto_check_after_network_change")
        val startProtectionOnLaunch = booleanPreferencesKey("start_protection_on_launch")
        val lastWifiProfileId = stringPreferencesKey("last_wifi_profile")
        val lastMobileProfileId = stringPreferencesKey("last_mobile_profile")
    }
}
