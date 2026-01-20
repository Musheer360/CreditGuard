package com.creditguard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.creditguard.data.db.AppDatabase
import com.creditguard.ui.MainViewModel
import com.creditguard.ui.screens.DashboardScreen
import com.creditguard.ui.screens.SettingsScreen
import com.creditguard.ui.theme.CreditGuardTheme
import com.creditguard.ui.theme.PureBlack
import com.creditguard.ui.theme.SecondaryText

class MainActivity : ComponentActivity() {
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        requestPermissions()
        
        val dao = AppDatabase.getInstance(this).transactionDao()
        val viewModel = MainViewModel(dao)
        
        setContent {
            CreditGuardTheme {
                MainApp(viewModel)
            }
        }
    }
    
    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        val needed = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (needed.isNotEmpty()) {
            permissionLauncher.launch(needed.toTypedArray())
        }
    }
}

@Composable
fun MainApp(viewModel: MainViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val unpaidTotal by viewModel.unpaidTotal.collectAsStateWithLifecycle()
    val monthlySpend by viewModel.monthlySpend.collectAsStateWithLifecycle()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .systemBarsPadding()
    ) {
        // Content
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            modifier = Modifier.fillMaxSize().padding(bottom = 56.dp)
        ) { tab ->
            when (tab) {
                0 -> DashboardScreen(
                    transactions = transactions,
                    unpaidTotal = unpaidTotal,
                    monthlySpend = monthlySpend,
                    onPayClick = { },
                    onMarkPaid = viewModel::markPaid,
                    onMarkAllPaid = viewModel::markAllPaid
                )
                1 -> SettingsScreen()
            }
        }
        
        // Minimal bottom nav
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 48.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MinimalNavItem("Home", selectedTab == 0) { selectedTab = 0 }
            MinimalNavItem("Settings", selectedTab == 1) { selectedTab = 1 }
        }
    }
}

@Composable
fun MinimalNavItem(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        color = if (selected) Color.White else SecondaryText,
        fontSize = 14.sp,
        fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 12.dp)
    )
}
