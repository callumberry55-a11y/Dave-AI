package com.example.daveai

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.daveai.data.db.SecurityEventDao
import com.example.daveai.data.repository.SecurityRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SecurityRepositoryTest {

    private lateinit var context: Context
    private lateinit var securityEventDao: SecurityEventDao
    private lateinit var securityRepository: SecurityRepository

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
        securityEventDao = mockk(relaxed = true)
        
        // Mocking EncryptedSharedPreferences because it's hard to test with Robolectric
        mockkStatic(EncryptedSharedPreferences::class)
        mockkStatic(MasterKey.Builder::class)
        
        val mockPrefs = mockk<SharedPreferences>(relaxed = true)
        val mockEditor = mockk<SharedPreferences.Editor>(relaxed = true)
        
        every { EncryptedSharedPreferences.create(any(), any(), any(), any(), any()) } returns mockPrefs
        every { mockPrefs.edit() } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor
        every { mockEditor.remove(any()) } returns mockEditor
        
        // Capture the value put into prefs
        val slot = slot<String>()
        every { mockEditor.putString("user_claude_api_key", capture(slot)) } answers {
            every { mockPrefs.getString("user_claude_api_key", null) } returns slot.captured
            mockEditor
        }

        securityRepository = SecurityRepository(context, securityEventDao)
    }

    @Test
    fun `test encrypt and decrypt API key`() {
        val testKey = "sk-test-123"
        securityRepository.setEncryptedString(SecurityRepository.KEY_CLAUDE_API, testKey)
        
        val retrieved = securityRepository.getEncryptedString(SecurityRepository.KEY_CLAUDE_API)
        assertEquals(testKey, retrieved)
    }

    @Test
    fun `test vault code update logs event`() = runTest {
        securityRepository.setVaultSecurityCode("1234")
        
        // Verify that a security event was logged
        coVerify { securityEventDao.insertEvent(match { it.eventType == "VAULT_CODE_UPDATED" }) }
    }
}
