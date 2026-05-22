package com.example.daveai.ui.chat

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Newspaper
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.json.JSONObject

@Composable
fun FinanceWidget(data: JSONObject) {
    val symbol = data.optString("symbol", "BTC")
    val price = data.optString("price", "$0.00")
    val change = data.optString("change", "+0.0%")
    val points = listOf(10f, 25f, 15f, 40f, 30f, 55f, 45f, 70f) // Mock trend data

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.ShowChart, contentDescription = null, tint = Color.Green)
                Spacer(Modifier.width(8.dp))
                Text(symbol, fontWeight = FontWeight.Bold)
            }
            Text(change, color = Color.Green, style = MaterialTheme.typography.labelMedium)
        }
        
        Spacer(Modifier.height(8.dp))
        Text(price, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        
        Spacer(Modifier.height(16.dp))
        
        Canvas(modifier = Modifier.fillMaxWidth().height(40.dp)) {
            val path = Path()
            val stepX = size.width / (points.size - 1)
            val maxY = points.maxOrNull() ?: 100f
            
            points.forEachIndexed { index, value ->
                val x = index * stepX
                val y = size.height - (value / maxY * size.height)
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            
            drawPath(path, color = Color.Green, style = Stroke(width = 2.dp.toPx()))
        }
    }
}

@Composable
fun FitnessWidget(data: JSONObject) {
    val steps = data.optInt("steps", 0)
    val goal = data.optInt("goal", 10000)
    val calories = data.optInt("calories", 0)
    val progress = if (goal > 0) steps.toFloat() / goal else 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.FitnessCenter, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
            Spacer(Modifier.width(12.dp))
            Text("Activity Pulse", fontWeight = FontWeight.Bold)
        }
        
        Spacer(Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Steps", style = MaterialTheme.typography.labelSmall)
                Text("$steps / $goal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Calories", style = MaterialTheme.typography.labelSmall)
                Text("$calories kcal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(Modifier.height(12.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Black.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.tertiary)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsWidget(data: JSONObject) {
    val articles = data.optJSONArray("articles") ?: return
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Newspaper, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text("Pulse: Global Feed", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
        
        Spacer(Modifier.height(16.dp))
        
        for (i in 0 until articles.length()) {
            val article = articles.getJSONObject(i)
            val title = article.optString("title")
            val source = article.optString("source")

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = source.uppercase(),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (i < articles.length() - 1) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}

@Composable
fun SpotifyWidget(data: JSONObject) {
    val name = data.optString("name")
    val artist = data.optString("artist")
    val imageUrl = data.optString("imageUrl")
    val energy = data.optDouble("energy", 0.0).toFloat()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.width(8.dp))
            Text("Sonic Intelligence", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
        
        Spacer(Modifier.height(16.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (!imageUrl.isNullOrEmpty()) {
                    coil.compose.AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Rounded.MusicNote, contentDescription = null)
                }
            }
            
            Spacer(Modifier.width(12.dp))
            
            Column {
                Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(artist, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
            }
        }
        
        Spacer(Modifier.height(12.dp))
        
        Text("DAVE'S PULSE RATING: ${(energy * 100).toInt()}% ENERGY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(energy)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.secondary)
            )
        }
    }
}

@Composable
fun CalendarWidget(data: JSONObject) {
    val events = data.optJSONArray("events") ?: return
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text("Upcoming Intel", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
        
        Spacer(Modifier.height(16.dp))
        
        for (i in 0 until minOf(events.length(), 4)) {
            val event = events.getJSONObject(i)
            val title = event.optString("title")
            val start = event.optLong("start")
            val time = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(start))

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(time, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.width(12.dp))
                Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun UsageWidget(data: JSONObject) {
    val stats = data.optJSONArray("stats") ?: return
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.PieChart, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.width(8.dp))
            Text("Focus Audit", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
        
        Spacer(Modifier.height(16.dp))
        
        for (i in 0 until stats.length()) {
            val stat = stats.getJSONObject(i)
            val pkg = stat.optString("package").substringAfterLast(".")
            val time = stat.optLong("time")
            val minutes = time / (1000 * 60)
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(pkg.uppercase(), style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                Text("${minutes}m", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun FileWidget(data: JSONObject) {
    val files = data.optJSONArray("files") ?: return
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text("Mainframe Search Results", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
        
        Spacer(Modifier.height(16.dp))
        
        for (i in 0 until files.length()) {
            val file = files.getJSONObject(i)
            val name = file.optString("name")
            val size = file.optLong("size")
            val path = file.optString("path")
            val sizeKb = size / 1024

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when {
                        name.endsWith(".pdf") -> Icons.Rounded.PictureAsPdf
                        name.endsWith(".kt") || name.endsWith(".java") -> Icons.Rounded.Code
                        else -> Icons.Rounded.InsertDriveFile
                    },
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${sizeKb}KB • $path", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            if (i < files.length() - 1) {
                HorizontalDivider(modifier = Modifier.graphicsLayer { alpha = 0.3f })
            }
        }
    }
}
