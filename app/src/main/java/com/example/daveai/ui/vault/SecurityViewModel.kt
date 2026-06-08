package com.example.daveai.ui.vault

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.daveai.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SecurityUiState(
    val isBiometricAvailable: Boolean = false,
    val useBiometrics: Boolean = false,
    val hasSecurityCode: Boolean = false,
    val isAuthSuccessful: Boolean = false,
    val errorMessage: String? = null,
    val setupCode: String = "",
    val confirmCode: String = "",
    val authInput: String = ""
)

class SecurityViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SecurityUiState())
    val uiState: StateFlow<SecurityUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val useBio = settingsRepository.useBiometricsForVault.first()
            val code = settingsRepository.vaultSecurityCode.first()
            _uiState.update { it.copy(
                useBiometrics = useBio,
                hasSecurityCode = !code.isNullOrBlank()
            ) }
        }
    }

    fun checkBiometricAvailability(context: Context) {
        val biometricManager = BiometricManager.from(context)
        val canAuthenticate = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        _uiState.update { it.copy(isBiometricAvailable = canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) }
    }

    fun onSetupCodeChanged(code: String) {
        _uiState.update { it.copy(setupCode = code, errorMessage = null) }
    }

    fun onConfirmCodeChanged(code: String) {
        _uiState.update { it.copy(confirmCode = code, errorMessage = null) }
    }

    fun onAuthInputChanged(input: String) {
        _uiState.update { it.copy(authInput = input, errorMessage = null) }
    }

    fun saveSecuritySetup() {
        val state = _uiState.value
        if (state.setupCode.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Code cannot be empty") }
            return
        }
        if (state.setupCode != state.confirmCode) {
            _uiState.update { it.copy(errorMessage = "Codes do not match") }
            return
        }

        viewModelScope.launch {
            settingsRepository.setVaultSecurityCode(state.setupCode)
            settingsRepository.setUseBiometricsForVault(state.useBiometrics)
            _uiState.update { it.copy(hasSecurityCode = true, isAuthSuccessful = true) }
        }
    }

    fun toggleBiometrics(enabled: Boolean) {
        _uiState.update { it.copy(useBiometrics = enabled) }
    }

    fun authenticateWithCode() {
        viewModelScope.launch {
            val savedCode = settingsRepository.vaultSecurityCode.first()
            if (_uiState.value.authInput == savedCode) {
                settingsRepository.securityRepository.logSecurityEvent(
                    type = "VAULT_AUTH_SUCCESS",
                    details = "Code authentication successful"
                )
                _uiState.update { it.copy(isAuthSuccessful = true) }
            } else {
                settingsRepository.securityRepository.logSecurityEvent(
                    type = "VAULT_AUTH_FAILURE",
                    details = "Incorrect code attempt",
                    severity = "WARNING"
                )
                _uiState.update { it.copy(errorMessage = "Incorrect security code") }
            }
        }
    }

    fun authenticateWithBiometrics(activity: FragmentActivity) {
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    _uiState.update { it.copy(errorMessage = "Auth error: $errString") }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    settingsRepository.securityRepository.logSecurityEvent(
                        type = "VAULT_AUTH_SUCCESS",
                        details = "Biometric authentication successful"
                    )
                    _uiState.update { it.copy(isAuthSuccessful = true) }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    settingsRepository.securityRepository.logSecurityEvent(
                        type = "VAULT_AUTH_FAILURE",
                        details = "Biometric authentication failed",
                        severity = "WARNING"
                    )
                    _uiState.update { it.copy(errorMessage = "Authentication failed") }
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Vault Authentication")
            .setSubtitle("Authenticate to access your digital assets")
            .setNegativeButtonText("Use Security Code")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    fun clearAuthState() {
        _uiState.update { it.copy(isAuthSuccessful = false, authInput = "", errorMessage = null) }
    }
    
    fun disableSecurity() {
        viewModelScope.launch {
            settingsRepository.setVaultSecurityCode(null)
            settingsRepository.setUseBiometricsForVault(false)
            _uiState.update { it.copy(hasSecurityCode = false, useBiometrics = false) }
        }
    }
}
