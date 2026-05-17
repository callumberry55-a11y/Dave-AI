package com.example.daveai.ui.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.daveai.BuildConfig
import com.example.daveai.data.repository.UserStatsRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
)

class AuthViewModel : ViewModel() {
    private val userStatsRepository = UserStatsRepository()
    private val auth by lazy { 
        try {
            FirebaseAuth.getInstance() 
        } catch (_: Exception) {
            null
        }
    }
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var referralData: Map<String, String?> = emptyMap()

    fun setReferralData(data: Map<String, String?>) {
        this.referralData = data
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, error = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, error = null) }
    }

    fun signUp() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) return
        
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            _uiState.update { it.copy(error = "Firebase not initialized. Add google-services.json!") }
            return
        }
        
        _uiState.update { it.copy(isLoading = true, error = null) }
        firebaseAuth.createUserWithEmailAndPassword(state.email, state.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    viewModelScope.launch {
                        userStatsRepository.trackUserLogin(user?.uid ?: "", user?.email, referralData)
                        _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = task.exception?.message ?: "Signup failed") }
                }
            }
    }

    fun login() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) return

        val firebaseAuth = auth
        if (firebaseAuth == null) {
            _uiState.update { it.copy(error = "Firebase not initialized. Add google-services.json!") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        firebaseAuth.signInWithEmailAndPassword(state.email, state.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    viewModelScope.launch {
                        userStatsRepository.trackUserLogin(user?.uid ?: "", user?.email, referralData)
                        _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = task.exception?.message ?: "Login failed") }
                }
            }
    }

    fun signInWithGoogle(context: Context) {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            _uiState.update { it.copy(error = "Firebase not initialized!") }
            return
        }

        if (BuildConfig.GOOGLE_CLIENT_ID.isBlank()) {
            _uiState.update { it.copy(error = "Google Client ID is missing in local.properties!") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val credentialManager = CredentialManager.create(context)
            
            val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts = false)
                .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
                .setAutoSelectEnabled(autoSelectEnabled = true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            try {
                val result = credentialManager.getCredential(
                    context = context,
                    request = request,
                )
                handleGoogleSignInResult(result)
            } catch (e: GetCredentialException) {
                Log.e("AuthViewModel", "Google Sign-In failed", e)
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error during Google Sign-In", e)
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun handleGoogleSignInResult(result: GetCredentialResponse) {
        val credential = result.credential
        if (credential is GoogleIdTokenCredential) {
            val googleIdToken = credential.idToken
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
            
            auth?.signInWithCredential(firebaseCredential)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth?.currentUser
                        viewModelScope.launch {
                            userStatsRepository.trackUserLogin(user?.uid ?: "", user?.email, referralData)
                            _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = task.exception?.message ?: "Firebase Auth failed") }
                    }
                }
        } else {
            _uiState.update { it.copy(isLoading = false, error = "Unexpected credential type") }
        }
    }
}
