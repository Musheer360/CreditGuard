package com.creditguard.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [Index("isPaid"), Index("timestamp")]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "amount") val amount: Long,
    val merchant: String,
    val cardLast4: String,
    val bank: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPaid: Boolean = false,
    val rawSms: String
)
