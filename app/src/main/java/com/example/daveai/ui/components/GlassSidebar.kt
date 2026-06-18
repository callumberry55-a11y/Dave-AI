package com.example.daveai.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daveai.BuildConfig
import com.example.daveai.data.db.ChatSessionEntity
import com.example.daveai.data.repository.UserProfile
import com.example.daveai.ui.theme.*

@Composable
fun GlassSidebar(
    userProfile: UserProfile?,
    sessions: List<ChatSessionEntity>,
    currentSessionId: String?,
    glowStrength: Float,
    blurIntensity: Float,
    onSessionSelected: (String) -> Unit,
    onCreateNewChat: () -> Unit,
    onEnterVault: () -> Unit,
    onEnterSanctum: () -> Unit,
    onEnterRiddleRoom: () -> Unit,
    onEnterTerminal: () -> Unit,
    onEnterMarketplace: () -> Unit,
    onEnterPersonaEditor: () -> Unit,
    onUpdateGlowStrength: (Float) -> Unit,
    onUpdateBlurIntensity: (Float) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) ObsidianDeep else backgroundLight

    ModalDrawerSheet(
        drawerContainerColor = bgColor,
        drawerTonalElevation = 0.dp,
        modifier = modifier
            .width(320.dp)
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ),
                shape = RoundedCornerShape(0.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // ZONE 0: IDENTITY
            SidebarIdentityZone(userProfile)

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ZONE 1: CHANNELS
                item { SidebarSectionZone("CHANNELS") }
                item {
                    SidebarLiquidItem(
                        icon = Icons.Rounded.Add,
                        label = "Initialize Link",
                        onClick = onCreateNewChat,
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                }

                items(sessions.take(5)) { session ->
                    val isSelected = session.sessionId == currentSessionId
                    SidebarLiquidItem(
                        icon = Icons.Rounded.ChatBubbleOutline,
                        label = session.title,
                        onClick = { onSessionSelected(session.sessionId) },
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        isCompact = true
                    )
                }

                item { Spacer(Modifier.height(16.dp)) }

                // ZONE 2: INTELLIGENCE
                item { SidebarSectionZone("INTELLIGENCE") }
                item {
                    SidebarLiquidItem(
                        icon = Icons.Rounded.Memory,
                        label = "Digital Vault",
                        onClick = onEnterVault
                    )
                }
                item {
                    SidebarLiquidItem(
                        icon = Icons.Rounded.Speed,
                        label = "The Sanctum",
                        onClick = onEnterSanctum
                    )
                }
                item {
                    SidebarLiquidItem(
                        icon = Icons.Rounded.AutoAwesome,
                        label = "Riddle Vault",
                        onClick = onEnterRiddleRoom,
                        contentColor = MaterialTheme.colorScheme.tertiary
                    )
                }

                item { Spacer(Modifier.height(16.dp)) }

                // ZONE 3: SYSTEM
                item { SidebarSectionZone("SYSTEM") }
                item {
                    SidebarLiquidItem(
                        icon = Icons.Rounded.Store,
                        label = "Aura Marketplace",
                        onClick = onEnterMarketplace
                    )
                }
                item {
                    SidebarLiquidItem(
                        icon = Icons.Rounded.Face,
                        label = "Digital Persona",
                        onClick = onEnterPersonaEditor
                    )
                }
                item {
                    SidebarLiquidItem(
                        icon = Icons.Rounded.Dashboard,
                        label = "Dev Dashboard",
                        onClick = onEnterTerminal
                    )
                }

                item { Spacer(Modifier.height(16.dp)) }

                // ZONE 4: CONTROLS
                item { SidebarSectionZone("CONTROLS") }
                item {
                    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                        SidebarControlSlider(
                            icon = Icons.Rounded.Flare,
                            label = "GLOW",
                            value = glowStrength,
                            onValueChange = onUpdateGlowStrength,
                            activeColor = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        SidebarControlSlider(
                            icon = Icons.Rounded.BlurOn,
                            label = "BLUR",
                            value = blurIntensity,
                            onValueChange = onUpdateBlurIntensity,
                            activeColor = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                item {
                    SidebarLiquidItem(
                        icon = Icons.AutoMirrored.Rounded.Logout,
                        label = "Sever Link",
                        onClick = onLogout,
                        contentColor = Color.Red.copy(alpha = 0.8f)
                    )
                }
            }

            Text(
                text = "DAVE OS :: ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                modifier = Modifier.padding(24.dp).align(Alignment.CenterHorizontally),
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun SidebarIdentityZone(userProfile: UserProfile?) {
    userProfile?.let { profile ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = profile.displayName?.take(1)?.uppercase() ?: "D",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = profile.displayName ?: "Explorer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = profile.role ?: "Elite User",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SidebarSectionZone(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
        fontWeight = FontWeight.Black,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun SidebarLiquidItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    containerColor: Color = Color.Transparent,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    isCompact: Boolean = false
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isCompact) 40.dp else 52.dp),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(if (isCompact) 18.dp else 22.dp))
            Spacer(Modifier.width(12.dp))
            Text(
                text = label,
                style = if (isCompact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                fontWeight = if (isCompact) FontWeight.Normal else FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SidebarControlSlider(
    icon: ImageVector,
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    activeColor: Color
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(12.dp), tint = activeColor.copy(alpha = 0.6f))
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = activeColor,
                activeTrackColor = activeColor,
                inactiveTrackColor = activeColor.copy(alpha = 0.1f)
            ),
            modifier = Modifier.height(32.dp)
        )
    }
}
