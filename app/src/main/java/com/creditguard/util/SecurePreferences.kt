package com.creditguard.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecurePreferences {
    
    private const val PREFS_NAME = "creditguard_secure"
    
    @Volatile
    private var cachedPreferences: SharedPreferences? = null
    
    fun getSecurePreferences(context: Context): SharedPreferences {
        cachedPreferences?.let { return it }
        return synchronized(this) {
            cachedPreferences ?: run {
                try {
                    val masterKey = MasterKey.Builder(context.applicationContext)
                        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                        .build()
                    EncryptedSharedPreferences.create(
                        context.applicationContext,
                        PREFS_NAME,
                        masterKey,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                    )
                } catch (_: Exception) {
                    // Fallback to regular prefs if Keystore is corrupted
                    context.applicationContext.getSharedPreferences(PREFS_NAME + "_fallback", Context.MODE_PRIVATE)
                }.also { cachedPreferences = it }
            }
        }
    }
}