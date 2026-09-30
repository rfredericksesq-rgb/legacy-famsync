package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FirestoreConfigRuleTest {

    @Test
    fun verifyFirestoreDatabaseIdConfigured() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbId = context.getString(R.string.firestore_database_id)
        assertNotNull(dbId)
        assertTrue(dbId.isNotEmpty())
        assertTrue(dbId.startsWith("ai-studio-android-"))
    }

    @Test
    fun verifyDefaultWebClientIdConfigured() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val clientId = context.getString(R.string.default_web_client_id)
        assertNotNull(clientId)
        assertTrue(clientId.isNotEmpty())
        assertTrue(clientId.endsWith(".apps.googleusercontent.com"))
    }
}
