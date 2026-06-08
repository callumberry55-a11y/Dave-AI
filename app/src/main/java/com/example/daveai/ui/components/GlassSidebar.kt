package com.example.daveai.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Flare
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Store
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.daveai.ui.theme.glassBlack
import com.example.daveai.ui.theme.glassBlackBorder
import com.example.daveai.ui.theme.glassWhite
import com.example.daveai.ui.theme.glassWhiteBorder

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
    val bgColor = if (isDark) glassBlack else glassWhite
    val borderColor = if (isDark) glassBlackBorder else glassWhiteBorder

    ModalDrawerSheet(
        drawerContainerColor = bgColor,
        drawerTonalElevation = 0.dp,
        modifier = modifier
            .width(320.dp)
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, borderColor)
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
            // Header: User Profile
            userProfile?.let { profile ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
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
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = profile.role ?: "Elite User",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item { SidebarSectionHeader("NEURAL CORE") }
                item {
                    SidebarItem(
                        icon = Icons.Rounded.Add,
                        label = "Initialize New Chat",
                        onClick = onCreateNewChat,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                items(sessions.take(5)) { session ->
                    NavigationDrawerItem(
                        label = {
                            Text(
                                text = session.title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        selected = session.sessionId == currentSessionId,
                        onClick = { onSessionSelected(session.sessionId) },
                        shape = RoundedCornerShape(16.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier.height(48.dp)
                    )
                }

                item { Spacer(Modifier.height(16.dp)) }

                item { SidebarSectionHeader("INTELLIGENCE") }
                item {
                    SidebarItem(
                        icon = Icons.Rounded.Memory,
                        label = "Digital Vault",
                        onClick = onEnterVault
                    )
                }
                item {
                    SidebarItem(
                        icon = Icons.Rounded.Speed,
                        label = "The Sanctum",
                        onClick = onEnterSanctum
                    )
                }

                item { Spacer(Modifier.height(24.dp)) }

                // Section: GAMES & TASKS
                item { SidebarSectionHeader("GAMES & TASKS") }
                item {
                    SidebarItem(
                        icon = Icons.Rounded.AutoAwesome,
                        label = "The Riddle Vault",
                        onClick = onEnterRiddleRoom,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                item { Spacer(Modifier.height(24.dp)) }

                // Section: SYSTEM
                item { SidebarSectionHeader("AURA CONFIGURATION") }

                item {
                    SidebarItem(
                        icon = Icons.Rounded.Store,
                        label = "Aura Marketplace",
                        onClick = onEnterMarketplace,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    SidebarItem(
                        icon = Icons.Rounded.Face,
                        label = "Digital Persona",
                        onClick = onEnterPersonaEditor,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                item {
                    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Flare, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                            Spacer(Modifier.width(8.dp))
                            Text("GLOW INTENSITY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontWeight = FontWeight.Black)
                        }
                        Slider(
                            value = glowStrength,
                            onValueChange = onUpdateGlowStrength,
                            colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                }

                item {
                    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.BlurOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f))
                            Spacer(Modifier.width(8.dp))
                            Text("NEURAL BLUR", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontWeight = FontWeight.Black)
                        }
                        Slider(
                            value = blurIntensity,
                            onValueChange = onUpdateBlurIntensity,
                            colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.tertiary, activeTrackColor = MaterialTheme.colorScheme.tertiary)
                        )
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }

                item {
                    SidebarItem(
                        icon = Icons.Rounded.Terminal,
                        label = "Neural Terminal",
                        onClick = onEnterTerminal
                    )
                }
                item {
                    SidebarItem(
                        icon = Icons.AutoMirrored.Rounded.Logout,
                        label = "Sever Link (Logout)",
                        onClick = onLogout,
                        color = Color.Red.copy(alpha = 0.6f)
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
fun SidebarSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
        fontWeight = FontWeight.Black,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 12.dp)
    )
}

@Composable
fun SidebarItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    NavigationDrawerItem(
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = color.copy(alpha = 0.7f))
                Spacer(Modifier.width(16.dp))
                Text(text = label, style = MaterialTheme.typography.bodyMedium, color = color)
            }
        },
        selected = false,
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
        modifier = Modifier.height(48.dp)
    )
}
