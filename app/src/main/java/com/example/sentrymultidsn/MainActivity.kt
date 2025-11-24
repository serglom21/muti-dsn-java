package com.example.sentrymultidsn

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize Sentry scopes with error handling
        try {
            SentryScopeManager.initialize(applicationContext)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Failed to initialize Sentry hubs", e)
            Toast.makeText(this, "Failed to initialize Sentry: ${e.message}", Toast.LENGTH_LONG).show()
            // Don't crash - just show error and continue
        }
        
        // Old Version UI buttons
        findViewById<Button>(R.id.btnOldError).setOnClickListener {
            triggerOldVersionError()
        }
        
        findViewById<Button>(R.id.btnOldMessage).setOnClickListener {
            captureOldVersionMessage()
        }
        
        // New Version UI buttons
        findViewById<Button>(R.id.btnNewError).setOnClickListener {
            triggerNewVersionError()
        }
        
        findViewById<Button>(R.id.btnNewMessage).setOnClickListener {
            captureNewVersionMessage()
        }
    }
    
    /**
     * Trigger an error in the old version UI section
     * This will send the error to the old DSN
     */
    private fun triggerOldVersionError() {
        try {
            // Simulate an error
            throw IllegalStateException("Error from Old Version UI")
        } catch (e: Exception) {
            SentryScopeManager.getOldVersionHub()?.let { hub ->
                hub.captureException(e)
                Toast.makeText(this, "Error sent to Old DSN", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(this, "Old Version Hub not initialized", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * Capture a message from the old version UI section
     * This will send the message to the old DSN
     */
    private fun captureOldVersionMessage() {
        SentryScopeManager.getOldVersionHub()?.let { hub ->
            hub.captureMessage("Message from Old Version UI")
            Toast.makeText(this, "Message sent to Old DSN", Toast.LENGTH_SHORT).show()
        } ?: run {
            Toast.makeText(this, "Old Version Hub not initialized", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Trigger an error in the new version UI section
     * This will send the error to the new DSN
     */
    private fun triggerNewVersionError() {
        try {
            // Simulate an error
            throw RuntimeException("Error from New Version UI")
        } catch (e: Exception) {
            val hub = SentryScopeManager.getNewVersionHub()
            if (hub == null) {
                Toast.makeText(this, "New Version Hub not initialized", Toast.LENGTH_SHORT).show()
            } else if (hub is SentryScopeManager.NewVersionHubWrapper) {
                hub.captureExceptionWithTag(e)
                Toast.makeText(this, "Error sent to New DSN", Toast.LENGTH_SHORT).show()
            } else {
                hub.captureException(e)
                Toast.makeText(this, "Error sent to New DSN", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * Capture a message from the new version UI section
     * This will send the message to the new DSN
     */
    private fun captureNewVersionMessage() {
        val hub = SentryScopeManager.getNewVersionHub()
        if (hub == null) {
            Toast.makeText(this, "New Version Hub not initialized", Toast.LENGTH_SHORT).show()
        } else if (hub is SentryScopeManager.NewVersionHubWrapper) {
            hub.captureMessageWithTag("Message from New Version UI")
            Toast.makeText(this, "Message sent to New DSN", Toast.LENGTH_SHORT).show()
        } else {
            hub.captureMessage("Message from New Version UI")
            Toast.makeText(this, "Message sent to New DSN", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Clean up Sentry scopes when the activity is destroyed
        SentryScopeManager.close()
    }
}

