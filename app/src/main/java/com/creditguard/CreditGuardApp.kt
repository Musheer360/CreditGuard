package com.creditguard

import android.app.Application
import com.creditguard.data.db.AppDatabase
import com.creditguard.util.NotificationHelper

class CreditGuardApp : Application() {
    
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
    }
}
