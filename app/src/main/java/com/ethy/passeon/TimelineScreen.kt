package com.ethy.passeon

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// ✨ [修改] TimelineScreen 現在非常乾淨，不再有 Scaffold 和 FAB
@Composable
fun TimelineScreen(tickets: List<Ticket>) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(tickets) { ticket ->
            TicketCard(ticket = ticket)
        }
    }
}

