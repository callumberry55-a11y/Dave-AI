package com.example.daveai.ui.auth

import android.Manifest
import android.util.Base64
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.core.Preview
import com.example.daveai.DaveApplication
import com.example.daveai.ui.components.NeuralCard
import com.example.daveai.ui.components.NeuralTopBar
import com.example.daveai.util.HardwareAccelerator
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun IdentityVerificationScreen(
    onBack: () -> Unit,
    onVerificationComplete: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as DaveApplication
    val hardwareAccelerator = app.chatRepository.getHardwareAccelerator()
    val lifecycleOwner = LocalLifecycleOwner.current
    
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf("Position ID card within the frame") }
    var verificationSuccess by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(cameraPermissionState.status.isGranted) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            NeuralTopBar(
                title = "IDENTITY VERIFICATION",
                onNavigationClick = onBack,
                navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (cameraPermissionState.status.isGranted) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }
                            val capture = ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                .build()
                            
                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    capture
                                )
                                imageCapture = capture
                            } catch (e: Exception) {
                                android.util.Log.e("IDVerify", "Camera bind failed", e)
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Overlay UI
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                NeuralCard(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isProcessing) "NEURAL SCANNING..." else "GDPR COMPLIANCE LAYER",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = resultMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        
                        if (isProcessing) {
                            Spacer(Modifier.height(16.dp))
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }

                        if (verificationSuccess != null) {
                            Spacer(Modifier.height(16.dp))
                            Icon(
                                imageVector = if (verificationSuccess == true) Icons.Rounded.CheckCircle else Icons.Rounded.Error,
                                contentDescription = null,
                                tint = if (verificationSuccess == true) Color.Green else Color.Red,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }

                if (verificationSuccess == null) {
                    IconButton(
                        onClick = {
                            val capture = imageCapture ?: return@IconButton
                            isProcessing = true
                            resultMessage = "Scanning for Holographic PASS mark..."
                            
                            capture.takePicture(
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(image: ImageProxy) {
                                        val buffer = image.planes[0].buffer
                                        val bytes = ByteArray(buffer.remaining())
                                        buffer[bytes]
                                        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                                        image.close()

                                        CoroutineScope(Dispatchers.IO).launch {
                                            val result = hardwareAccelerator.verifyIdentityDocument(base64)
                                            withContext(Dispatchers.Main) {
                                                isProcessing = false
                                                verificationSuccess = result.success
                                                resultMessage = result.message
                                                if (result.success) {
                                                    kotlinx.coroutines.delay(2000)
                                                    onVerificationComplete(true)
                                                }
                                            }
                                        }
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        isProcessing = false
                                        resultMessage = "Optical Failure: ${exception.message}"
                                    }
                                }
                            )
                        },
                        enabled = !isProcessing,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(if (isProcessing) Color.Gray else MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Rounded.Camera, contentDescription = "Scan ID", tint = Color.Black, modifier = Modifier.size(40.dp))
                    }
                }
            }
            
            // Frame Overlay (Static)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Transparent)
                    .padding(2.dp)
            ) {
                // Just a visual hint for the user
            }
        }
    }
}
