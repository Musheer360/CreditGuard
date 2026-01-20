package com.creditguard.data.db

import androidx.room.*
import com.creditguard.data.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAll(): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE isPaid = 0 ORDER BY timestamp DESC")
    fun getUnpaid(): Flow<List<Transaction>>
    
    @Query("SELECT SUM(amount) FROM transactions WHERE isPaid = 0")
    fun getTotalUnpaid(): Flow<Double?>
    
    @Query("SELECT SUM(amount) FROM transactions WHERE timestamp >= :startTime")
    fun getTotalSpentSince(startTime: Long): Flow<Double?>
    
    @Insert
    suspend fun insert(transaction: Transaction)
    
    @Query("UPDATE transactions SET isPaid = 1 WHERE id = :id")
    suspend fun markPaid(id: Long)
    
    @Query("UPDATE transactions SET isPaid = 1")
    suspend fun markAllPaid()
    
    @Delete
    suspend fun delete(transaction: Transaction)
}
