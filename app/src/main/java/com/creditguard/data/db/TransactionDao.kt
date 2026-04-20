package com.creditguard.data.db

import androidx.room.*
import com.creditguard.data.model.Transaction
import kotlinx.coroutines.flow.Flow

data class TransactionSummary(
    val id: Long,
    val amount: Long,
    val merchant: String,
    val cardLast4: String,
    val bank: String,
    val timestamp: Long,
    val isPaid: Boolean
)

@Dao
interface TransactionDao {
    @Query("SELECT id, amount, merchant, cardLast4, bank, timestamp, isPaid FROM transactions ORDER BY timestamp DESC")
    fun getAll(): Flow<List<TransactionSummary>>

    @Query("SELECT id, amount, merchant, cardLast4, bank, timestamp, isPaid FROM transactions WHERE isPaid = 0 ORDER BY timestamp DESC")
    fun getUnpaid(): Flow<List<TransactionSummary>>

    @Query("SELECT SUM(amount) FROM transactions WHERE isPaid = 0")
    fun getTotalUnpaid(): Flow<Long?>

    @Query("SELECT SUM(amount) FROM transactions WHERE timestamp >= :startTime")
    fun getTotalSpentSince(startTime: Long): Flow<Long?>

    @Insert
    suspend fun insert(transaction: Transaction): Long

    @Query("UPDATE transactions SET isPaid = 1 WHERE id = :id")
    suspend fun markPaid(id: Long)

    @Query("UPDATE transactions SET isPaid = 1")
    suspend fun markAllPaid()

    @Query("UPDATE transactions SET isPaid = 0 WHERE id = :id")
    suspend fun markUnpaid(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(transaction: Transaction)
}
