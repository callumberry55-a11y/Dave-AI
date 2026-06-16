package com.example.daveai.ui.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
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
    val developerCode: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val isDeleted: Boolean = false,
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

    fun logout() {
        auth?.signOut()
        _uiState.update { AuthUiState() }
    }

    fun setReferralData(data: Map<String, String?>) {
        this.referralData = data
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, error = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, error = null) }
    }

    fun onDeveloperCodeChanged(code: String) {
        _uiState.update { it.copy(developerCode = code, error = null) }
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
                    val isDev = state.developerCode == "1798"
                    viewModelScope.launch {
                        userStatsRepository.trackUserLogin(user?.uid ?: "", user?.email, referralData, isDev)
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
                    val isDev = state.developerCode == "1798"
                    viewModelScope.launch {
                        userStatsRepository.trackUserLogin(user?.uid ?: "", user?.email, referralData, isDev)
                        _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = task.exception?.message ?: "Login failed") }
                }
            }
    }

    fun loginAsReviewer() {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            _uiState.update { it.copy(error = "Firebase not initialized") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        
        // Dedicated reviewer account
        val reviewerEmail = "reviewer@daveai.com"
        val reviewerPassword = "DaveAIReviewer2026!"

        firebaseAuth.signInWithEmailAndPassword(reviewerEmail, reviewerPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    viewModelScope.launch {
                        userStatsRepository.trackUserLogin(user?.uid ?: "", user?.email, referralData, false)
                        _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    }
                } else {
                    // If the account doesn't exist, try to create it for the reviewer
                    firebaseAuth.createUserWithEmailAndPassword(reviewerEmail, reviewerPassword)
                        .addOnCompleteListener { createCtx ->
                            if (createCtx.isSuccessful) {
                                val user = firebaseAuth.currentUser
                                viewModelScope.launch {
                                    userStatsRepository.trackUserLogin(user?.uid ?: "", user?.email, referralData, false)
                                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                                }
                            } else {
                                _uiState.update { it.copy(isLoading = false, error = "Reviewer access unavailable. Please check internet connection.") }
                            }
                        }
                }
            }
    }

    fun signInWithGoogle(context: Context) {
        val firebaseAuth = auth ?: run {
            _uiState.update { it.copy(error = "Firebase not initialized") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context, request)
                
                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    firebaseAuth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = firebaseAuth.currentUser
                                viewModelScope.launch {
                                    userStatsRepository.trackUserLogin(user?.uid ?: "", user?.email, referralData, false)
                                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                                }
                            } else {
                                _uiState.update { it.copy(isLoading = false, error = task.exception?.message ?: "Google login failed at Firebase") }
                            }
                        }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Unexpected credential type") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Google login failed") }
            }
        }
    }

    fun deleteAccount(context: Context) {
        val firebaseAuth = auth ?: return
        val user = firebaseAuth.currentUser ?: return
        val uid = user.uid

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                // 1. Delete from Firestore
                userStatsRepository.deleteUserData(uid)

                // 2. Wipe Local Database
                val db = com.example.daveai.data.db.DaveDatabase.getDatabase(context)
                db.clearAllTables()

                // 3. Delete Firebase Auth Account
                user.delete().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _uiState.update { it.copy(isLoading = false, isDeleted = true) }
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = task.exception?.message ?: "Failed to delete auth account") }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Partial deletion failure: ${e.message}") }
            }
        }
    }
}
