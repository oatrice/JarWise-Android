@file:OptIn(ExperimentalMaterial3Api::class)

package com.oatrice.jarwise.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.oatrice.jarwise.data.model.GraphDataPointDto
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseGraph(
    data: List<GraphDataPointDto>,
    period: String,
    onPeriodChange: (String) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    var chartModelProducer by remember { mutableStateOf<ChartEntryModelProducer?>(null) }

    LaunchedEffect(data) {
        if (data.isNotEmpty()) {
            val entries = data.mapIndexed { index, point ->
                entryOf(index.toFloat(), point.amount.toFloat())
            }
            chartModelProducer = ChartEntryModelProducer(entries)
        } else {
             // Generate empty graph data so the chart structure is visible
             val emptyEntries = List(7) { index -> entryOf(index.toFloat(), 0f) }
             chartModelProducer = ChartEntryModelProducer(emptyEntries)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Expense Trends",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        // Period Selection
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            val periods = listOf("weekly", "monthly", "yearly")
            periods.forEachIndexed { index, p ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = periods.size),
                    onClick = { onPeriodChange(p) },
                    selected = period == p,
                    label = { Text(p.capitalize()) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
             Text(text = "Loading...", style = MaterialTheme.typography.bodySmall)
        } else if (chartModelProducer == null) {
             Text(text = "Preparing graph...", style = MaterialTheme.typography.bodySmall)
        } else {
            Chart(
                chart = columnChart(),
                chartModelProducer = chartModelProducer!!,
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

private fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
