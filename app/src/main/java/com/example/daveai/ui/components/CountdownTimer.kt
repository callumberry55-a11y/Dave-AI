package com.example.daveai.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daveai.ui.theme.DaveAITheme
import kotlinx.coroutines.delay
import java.util.Calendar

@Composable
fun CountdownTimer(
    targetTimestamp: Long,
    onExpire: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val remaining = (targetTimestamp - currentTime).coerceAtLeast(0L)

    LaunchedEffect(targetTimestamp) {
        while (System.currentTimeMillis() < targetTimestamp) {
            currentTime = System.currentTimeMillis()
            delay(1000)
        }
        currentTime = System.currentTimeMillis()
        onExpire()
    }

    val days = remaining / (24 * 60 * 60 * 1000)
    val hours = (remaining % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000)
    val minutes = (remaining % (60 * 60 * 1000)) / (60 * 1000)
    val seconds = (remaining % (60 * 1000)) / 1000

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TimeUnitDisplay("DAYS", days.toString().padStart(2, '0'))
        Spacer(Modifier.width(8.dp))
        TimeSeparator()
        Spacer(Modifier.width(8.dp))
        TimeUnitDisplay("HOURS", hours.toString().padStart(2, '0'))
        Spacer(Modifier.width(8.dp))
        TimeSeparator()
        Spacer(Modifier.width(8.dp))
        TimeUnitDisplay("MINS", minutes.toString().padStart(2, '0'))
        Spacer(Modifier.width(8.dp))
        TimeSeparator()
        Spacer(Modifier.width(8.dp))
        TimeUnitDisplay("SECS", seconds.toString().padStart(2, '0'))
    }
}

@Composable
private fun TimeUnitDisplay(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        NeuralCard(
            modifier = Modifier.size(width = 64.dp, height = 72.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun TimeSeparator() {
    val infiniteTransition = rememberInfiniteTransition(label = "separator")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        modifier = Modifier.height(72.dp).padding(bottom = 20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun CountdownTimerPreview() {
    val target = remember {
        Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 20)
            add(Calendar.HOUR_OF_DAY, 5)
        }.timeInMillis
    }
    DaveAITheme {
        Box(Modifier.padding(24.dp)) {
            CountdownTimer(
                targetTimestamp = target,
                onExpire = {}
            )
        }
    }
}
