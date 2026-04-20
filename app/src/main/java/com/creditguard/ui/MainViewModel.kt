package com.creditguard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.creditguard.data.db.TransactionDao
import com.creditguard.data.db.TransactionSummary
import com.creditguard.data.model.Transaction
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class MainViewModel(private val dao: TransactionDao) : ViewModel() {

    val transactions: StateFlow<List<TransactionSummary>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unpaidTotal: StateFlow<Long> = dao.getTotalUnpaid()
        .map { it ?: 0L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val monthlySpend: StateFlow<Long> = dao.getTotalSpentSince(getMonthStart())
        .map { it ?: 0L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun markPaid(id: Long) = viewModelScope.launch { dao.markPaid(id) }

    fun markUnpaid(id: Long) = viewModelScope.launch { dao.markUnpaid(id) }

    fun markAllPaid() = viewModelScope.launch { dao.markAllPaid() }

    fun delete(transaction: Transaction) = viewModelScope.launch { dao.delete(transaction) }

    fun clearHistory() = viewModelScope.launch { dao.deleteAll() }

    private fun getMonthStart(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    companion object {
        class Factory(private val dao: TransactionDao) : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(dao) as T
            }
        }
    }
}
