package io.github.barcodehub.focusanki.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.barcodehub.focusanki.domain.model.PointsTransaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val transactions by viewModel.transactions.collectAsState()

    Scaffold { padding ->
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Todavía no hay movimientos.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(transactions, key = { it.id }) { transaction ->
                    TransactionRow(transaction)
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(transaction: PointsTransaction) {
    val formatter = remember(transaction.id) {
        SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(transaction.reason, style = MaterialTheme.typography.bodyMedium)
                Text(
                    formatter.format(Date(transaction.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            val sign = if (transaction.delta >= 0) "+" else ""
            Text(
                text = "$sign${transaction.delta}",
                fontWeight = FontWeight.Bold,
                color = if (transaction.delta >= 0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
            )
        }
    }
}
