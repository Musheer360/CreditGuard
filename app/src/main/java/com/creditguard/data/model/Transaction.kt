package com.creditguard.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val merchant: String,
    val cardLast4: String,
    val bank: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPaid: Boolean = false,
    val rawSms: String
)
