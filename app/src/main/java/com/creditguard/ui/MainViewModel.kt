package com.creditguard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creditguard.data.db.TransactionDao
import com.creditguard.data.model.Transaction
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class MainViewModel(private val dao: TransactionDao) : ViewModel() {
    
    val transactions: StateFlow<List<Transaction>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val unpaidTotal: StateFlow<Double> = dao.getTotalUnpaid()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    
    val monthlySpend: StateFlow<Double> = dao.getTotalSpentSince(getMonthStart())
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    
    fun markPaid(id: Long) = viewModelScope.launch { dao.markPaid(id) }
    
    fun markAllPaid() = viewModelScope.launch { dao.markAllPaid() }
    
    fun delete(transaction: Transaction) = viewModelScope.launch { dao.delete(transaction) }
    
    private fun getMonthStart(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
