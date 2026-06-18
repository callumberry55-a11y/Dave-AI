package com.example.daveai.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daveai.ui.theme.ObsidianDeep

@Composable
fun NeuralLinkDialog(
    pairingCode: String?,
    partnerName: String? = null,
    onGenerateCode: () -> Unit,
    onLinkPartner: (String) -> Unit,
    onUnlinkPartner: () -> Unit = {},
    onDismiss: () -> Unit
) {
    var inputCode by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            if (partnerName == null) {
                FluidButton(
                    onClick = {
                        if (inputCode.isNotBlank()) {
                            onLinkPartner(inputCode)
                            onDismiss()
                        }
                    },
                    enabled = inputCode.isNotBlank()
                ) {
                    Text("Link Partner")
                }
            } else {
                FluidButton(
                    onClick = {
                        onUnlinkPartner()
                        onDismiss()
                    },
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Text("Sever Link")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Rounded.Sync, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                Text("Neural Pairing", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    if (partnerName != null) "Currently linked to $partnerName. Intelligence sync active."
                    else "Sync Dave's intelligence with a partner to share memories and real-time context.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(Modifier.height(24.dp))
                
                if (partnerName == null) {
                    if (pairingCode == null) {
                        FluidButton(
                            onClick = onGenerateCode,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Show My Pairing Code")
                        }
                    } else {
                        NeuralCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("YOUR CODE", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    pairingCode,
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 4.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    
                    Text("ENTER PARTNER CODE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputCode,
                        onValueChange = { inputCode = it.uppercase().take(6) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text("6-Digit Code") },
                        singleLine = true,
                    )
                }
            }
        },
        shape = RoundedCornerShape(32.dp),
        containerColor = ObsidianDeep
    )
}
