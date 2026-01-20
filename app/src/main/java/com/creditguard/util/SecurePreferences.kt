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
        // Return cached instance if available
        cachedPreferences?.let { return it }
        
        // Thread-safe double-checked locking
        return synchronized(this) {
            cachedPreferences ?: run {
                val masterKey = MasterKey.Builder(context.applicationContext)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                
                EncryptedSharedPreferences.create(
                    context.applicationContext,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                ).also { cachedPreferences = it }
            }
        }
    }
}