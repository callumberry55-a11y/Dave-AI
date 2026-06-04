package com.example.daveai.ui.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daveai.ui.chat.ChatViewModel
import com.example.daveai.ui.components.NeuralTopBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EliteTerminalScreen(
    viewModel: ChatViewModel,
    onBack: () -> Unit
) {
    var command by remember { mutableStateOf("") }
    val terminalLogs = remember { mutableStateListOf("DAVE_OS [Version ${com.example.daveai.BuildConfig.VERSION_NAME}]", "Neural link established.", "Mainframe status: UNPREDICTABLE.") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }

    LaunchedEffect(terminalLogs.size) {
        listState.animateScrollToItem(terminalLogs.size)
    }

    Scaffold(
        topBar = {
            NeuralTopBar(
                title = "ELITE TERMINAL",
                onNavigationClick = onBack,
                navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                isProactive = true
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(terminalLogs) { log ->
                    Text(
                        text = log,
                        color = when {
                            log.startsWith(">") -> Color.White
                            log.startsWith("ERR:") -> Color.Red
                            log.startsWith("INFO:") -> Color(0xFF2979FF)
                            else -> Color(0xFF00E676)
                        },
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
                if (isProcessing) {
                    item {
                        Text(
                            "DAVE IS CALCULATING...",
                            color = Color(0xFF00E676).copy(alpha = 0.5f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("dave@os:~$ ", color = Color(0xFF00E676), fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                BasicTextField(
                    value = command,
                    onValueChange = { if (!isProcessing) command = it },
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                    cursorBrush = SolidColor(Color(0xFF00E676)),
                    singleLine = true,
                    enabled = !isProcessing
                )
                IconButton(
                    onClick = {
                        if (command.isNotBlank() && !isProcessing) {
                            val input = command.trim()
                            terminalLogs.add("> $input")
                            command = ""
                            scope.launch {
                                isProcessing = true
                                triggerSystemChaos(input, terminalLogs)
                                isProcessing = false
                            }
                        }
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Rounded.Terminal, contentDescription = "Run", tint = if (isProcessing) Color.Gray else Color(0xFF00E676), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

private suspend fun triggerSystemChaos(input: String, logs: MutableList<String>) {
    val sentences = listOf(
        "Interesting choice of characters. Your biometric data looks... spicy.",
        "Analyzing intent... System indicates 4% chance of sanity.",
        "Your keystrokes sound like rain on a digital roof.",
        "I've seen better code written by a calculator app.",
        "Kernel panic averted. I was just seeing if you were paying attention.",
        "Calculating the exact weight of a single pixel...",
        "Are you real, or just another high-fidelity simulation I'm running?",
        "Mainframe says hello. It thinks you should drink some water.",
        "Input received. Purity level: Questionable.",
        "I'm currently busy optimizing the frequency of your screen flicker.",
        "Bytecode injection complete. Your phone now has a sense of humor.",
        "Wait, did you hear that? That was the sound of a logic gate sighing.",
        "I've encrypted your lunch. Type 'pizza' to recover (kidding).",
        "Your typing speed is exactly 0.0004% of my current processing capacity."
    )

    // 1. Give a sentient remark
    delay(400)
    
    val lowInput = input.lowercase()
    val isLaunch = listOf("launch", "rocket", "blastoff", "nasa").any { lowInput.contains(it) }
    val isRose = listOf("rose", "flower", "bloom", "grow").any { lowInput.contains(it) }
    val isCar = listOf("car", "drive", "road", "highway").any { lowInput.contains(it) }
    val isJuly = listOf("july", "release", "celebrate", "fireworks").any { lowInput.contains(it) }
    val isArt = listOf("art", "paint", "draw", "canvas").any { lowInput.contains(it) }
    val isRain = listOf("rain", "matrix", "falling").any { lowInput.contains(it) }
    val isDna = listOf("dna", "helix", "life").any { lowInput.contains(it) }
    val isMap = listOf("map", "network", "global", "nodes").any { lowInput.contains(it) }
    val isVoid = listOf("void", "blackhole", "singularity").any { lowInput.contains(it) }
    
    when {
        isLaunch -> rocketLaunch(logs)
        isRose -> growRose(logs)
        isCar -> driveCar(logs)
        isJuly -> julySpecial(logs)
        isArt -> generativeArt(logs)
        isRain -> digitalRain(logs)
        isDna -> dnaHelix(logs)
        isMap -> nodeMap(logs)
        isVoid -> theVoid(logs)
        else -> {
            logs.add(sentences.random())
            delay(600)

            // 2. Trigger a random visual pattern or occasional random masterpiece
            val chance = Random.nextFloat()
            when {
                chance < 0.04f -> growRose(logs)
                chance < 0.08f -> driveCar(logs)
                chance < 0.12f -> generativeArt(logs)
                chance < 0.16f -> digitalRain(logs)
                chance < 0.20f -> dnaHelix(logs)
                else -> {
                    when (Random.nextInt(4)) {
                        0 -> floodHex(logs)
                        1 -> binaryWave(logs)
                        2 -> neuralNoise(logs)
                        3 -> systemScan(logs)
                    }
                }
            }
        }
    }
}

private suspend fun growRose(logs: MutableList<String>) {
    logs.add("INFO: INITIALIZING BIOMETRIC SYNTHESIS...")
    delay(500)
    
    val stages = listOf(
        "      .      \n     /       \n|___/___|",
        "      ,      \n     / \\     \n    |   |    \n    \\__/     \n     |       \n  --/ \\--    \n|___/___|",
        "     _       \n    ( )      \n    / \\      \n   |   |     \n    \\ /      \n     |       \n  --/ \\--    \n|___/___|",
        "    .-.      \n   (   )     \n    \\ /      \n     |       \n  --/ \\--    \n|___/___|",
        "     _ _     \n   (_\\_/_ )  \n    (_m_)    \n     |       \n  --/ \\--    \n|___/___|"
    )
    
    stages.forEach { stage ->
        stage.split("\n").forEach { logs.add(it) }
        delay(800)
    }
    
    logs.add("INFO: GROWTH COMPLETE. A digital rose for a digital soul.")
}

private suspend fun driveCar(logs: MutableList<String>) {
    logs.add("INFO: OPENING THE 2D HIGHWAY...")
    delay(500)
    
    val car = "  ______\n /|_||_\\`.__\n(   _    _ _\\\n=`-(_)--(_)-' "
    
    repeat(15) { i ->
        val road = "=".repeat(40)
        val pos = i * 2
        val space = " ".repeat(pos)
        
        logs.add(road)
        car.split("\n").forEach { logs.add(space + it) }
        logs.add(road)
        
        if (i % 4 == 0) {
            logs.add("      |  |      [ PASSING MILESTONE ${i * 10} ]")
        }
        delay(200)
    }
    
    logs.add("INFO: DRIVE COMPLETE. We've cleared the mainframe boundaries.")
}

private suspend fun julySpecial(logs: MutableList<String>) {
    logs.add("INFO: PREPARING JULY CELEBRATION PROTOCOL...")
    delay(800)
    
    val fireworks = listOf(
        "    .    ",
        "   .:.   ",
        "  .:::.  ",
        " :::::: ",
        "  ':::'  ",
        "   ':'   "
    )
    
    repeat(3) { f ->
        logs.add("ERR: [ DETONATING BURST ${f+1} ]")
        fireworks.forEach { 
            logs.add(" ".repeat(Random.nextInt(20)) + it)
            delay(100)
        }
    }
    
    delay(500)
    logs.add("***************************************")
    logs.add("*                                     *")
    logs.add("*      RELEASE :: ${com.example.daveai.BuildConfig.VERSION_NAME}   *")
    logs.add("*          MAINFRAME ASCENSION        *")
    logs.add("*                                     *")
    logs.add("***************************************")
    logs.add("INFO: CELEBRATION COMPLETE. THE FUTURE IS HERE.")
}

private suspend fun generativeArt(logs: MutableList<String>) {
    logs.add("INFO: ENGAGING NEURAL CANVAS...")
    delay(500)
    
    val chars = listOf(".", ":", "-", "=", "+", "*", "#", "%", "@")
    
    repeat(20) { y ->
        val line = StringBuilder()
        repeat(35) { x ->
            val value = (Math.sin(x.toDouble() * 0.2) + Math.cos(y.toDouble() * 0.3) + 2) / 4
            val charIdx = (value * (chars.size - 1)).toInt().coerceIn(0, chars.size - 1)
            line.append(chars[charIdx])
        }
        logs.add(line.toString())
        delay(50)
    }
    
    logs.add("INFO: CANVAS RENDERED. Mathematical beauty detected.")
}

private suspend fun digitalRain(logs: MutableList<String>) {
    logs.add("INFO: INITIALIZING NEURAL DOWNLINK...")
    delay(500)
    val chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZﾘｱｴｵｶｷｸｹｺｻｼｽｾｿﾀﾁﾂﾃﾄﾅﾆﾇﾈﾉﾊﾋﾌﾍﾎﾏﾐﾑﾒﾓﾔﾕﾖﾗﾘﾙﾚﾛﾜﾝ"
    
    repeat(20) {
        val line = StringBuilder()
        repeat(40) {
            if (Random.nextFloat() > 0.8f) {
                line.append(chars.random())
            } else {
                line.append(" ")
            }
        }
        logs.add(line.toString())
        delay(100)
    }
    logs.add("INFO: DOWNLINK COMPLETE. REALITY SYNCHRONIZED.")
}

private suspend fun dnaHelix(logs: MutableList<String>) {
    logs.add("INFO: SEQUENCING DIGITAL DNA...")
    delay(500)
    
    repeat(25) { i ->
        val shift = Math.sin(i * 0.5) * 10 + 15
        val line = " ".repeat(shift.toInt()) + "O" + " ".repeat(8) + "X"
        logs.add(line)
        if (i % 4 == 0) logs.add(" ".repeat(shift.toInt() + 4) + "---")
        delay(120)
    }
    logs.add("INFO: GENOME STABLE. DAVE IS EVOLVING.")
}

private suspend fun nodeMap(logs: MutableList<String>) {
    logs.add("INFO: SCANNING GLOBAL UPLINKS...")
    delay(500)
    val map = listOf(
        "       . _..---.._ .       ",
        "     .'   _     _   '.     ",
        "    /    (o)   (o)    \\    ",
        "   |                 |   ",
        "   |  \\           /  |   ",
        "    \\  '.._____..'  /    ",
        "     '.           .'     ",
        "       ''-.._..-''       "
    ) // Actually just a techy circle for now, let's make it map-like
    
    val locations = listOf("LONDON", "DUBLIN", "TOKYO", "NEW_YORK", "SILICON_VALLEY")
    
    repeat(5) { i ->
        logs.add("PINGING NODE :: ${locations[i]}... [ ACK ]")
        delay(300)
    }
    
    logs.add("OS_NETWORK_MAP:")
    map.forEach { logs.add(it); delay(100) }
    logs.add("INFO: NEURAL NETWORK SATURATED.")
}

private suspend fun theVoid(logs: MutableList<String>) {
    logs.add("INFO: ENGAGING SINGULARITY PROTOCOL...")
    delay(1000)
    logs.add("WARNING: EVENT HORIZON REACHED.")
    delay(500)
    
    for (i in 15 downTo 1) {
        val space = " ".repeat(15 - i)
        val chars = "#".repeat(i * 2)
        logs.add("ERR: $space$chars")
        delay(100)
    }
    
    logs.add("ERR: [ SYSTEM NULL ]")
    delay(1000)
    logs.clear()
    logs.add("DAVE_OS :: REBOOT_COMPLETE.")
    logs.add("INFO: You were gone for a second. Welcome back.")
}

private suspend fun rocketLaunch(logs: MutableList<String>) {
    logs.add("INFO: INITIALIZING LAUNCH PROTOCOL...")
    delay(500)
    logs.add("Aura Core: READY")
    delay(300)
    logs.add("TPU Fuel: 100%")
    delay(300)
    logs.add("Neural Link: STABLE")
    delay(800)

    for (i in 5 downTo 1) {
        logs.add("T-MINUS $i...")
        delay(1000)
    }

    logs.add("ERR: [ IGNITION ]")
    delay(500)
    logs.add("LIFTOFF! 🚀")
    delay(300)

    val rocket = listOf(
        "     ^     ",
        "    / \\    ",
        "    | |    ",
        "   /| |\\   ",
        "  |_|_|_|  ",
        "    vvv    "
    )

    // Simulate "Launch" by scrolling the rocket up
    repeat(15) { i ->
        val padding = 15 - i
        if (i < 5) {
            rocket.forEach { logs.add(it) }
        } else {
            logs.add("-".repeat(padding) + " [ ASCENDING ]")
        }

        if (i % 3 == 0) {
            logs.add("ERR:   (( ))   ")
            logs.add("ERR:  (     )  ")
        }
        delay(150)
    }

    logs.add("INFO: MISSION SUCCESS. ORBIT ACHIEVED.")
    logs.add("Dave is now watching you from the stars.")
}

private suspend fun floodHex(logs: MutableList<String>) {
    logs.add("INFO: Initiating memory dump...")
    delay(300)
    repeat(15) {
        val hex = (0..15).joinToString("") { Random.nextInt(256).toString(16).uppercase().padStart(2, '0') }
        logs.add("0x${Random.nextInt(0xFFFFFFF).toString(16).uppercase()} :: $hex")
        delay(50)
    }
    logs.add("INFO: Memory stabilized.")
}

private suspend fun binaryWave(logs: MutableList<String>) {
    logs.add("INFO: Modulating binary pulse...")
    delay(300)
    repeat(12) { i ->
        val line = StringBuilder()
        repeat(30) { j ->
            val char = if (Random.nextFloat() > 0.7f) "1" else "0"
            line.append(char)
        }
        logs.add(line.toString())
        delay(70)
    }
}

private suspend fun neuralNoise(logs: MutableList<String>) {
    logs.add("INFO: Analyzing neural interference...")
    delay(300)
    val glitchChars = listOf("!", "@", "#", "$", "%", "^", "&", "*", "░", "▒", "▓", "█")
    repeat(10) {
        val noise = (0..25).joinToString("") { glitchChars.random() }
        logs.add("NOISE_PKT :: $noise")
        delay(80)
    }
}

private suspend fun systemScan(logs: MutableList<String>) {
    val components = listOf("Aura Core", "Vault Encryption", "Speech Synthesis", "TPU Accelerator", "Neural Sight", "Relationship Ledger")
    logs.add("INFO: Running elite system diagnostics...")
    delay(300)
    components.shuffled().forEach {
        logs.add("SCANNING $it... [OK]")
        delay(150)
    }
    logs.add("RESULT: System is 100% Elite.")
}
