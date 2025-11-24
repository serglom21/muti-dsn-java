package com.example.sentrymultidsn

import io.sentry.*
import io.sentry.android.core.SentryAndroid
import io.sentry.android.core.SentryAndroidOptions

/**
 * Manages routing Sentry events to different DSNs.
 * Uses a single Sentry instance and manually creates separate hubs for each DSN.
 */
object SentryScopeManager {
    
    // Old version DSN
    private const val OLD_DSN = "https://514d5e38a4003c2cf958c53c77679925@o4508236363464704.ingest.us.sentry.io/4508847444918272"
    
    // New version DSN
    private const val NEW_DSN = "https://b65dc9ea7dcf8a6783495ff281d34451@o4508236363464704.ingest.us.sentry.io/4510420977123328"
    
    private var oldVersionHub: IHub? = null
    private var newVersionHub: IHub? = null
    
    /**
     * Initialize both Sentry hubs with their respective DSNs
     */
    fun initialize(applicationContext: android.content.Context) {
        android.util.Log.d("SentryScopeManager", "Initializing Sentry hubs...")
        
        // Initialize Sentry once with old DSN
        // We'll handle routing to new DSN in beforeSend
        SentryAndroid.init(applicationContext) { options ->
            options.dsn = OLD_DSN
            options.isEnableUncaughtExceptionHandler = false
            options.setBeforeSend { event, hint ->
                event.tags = event.tags ?: mutableMapOf()
                val hubTag = event.tags!!["hub"]
                
                if (hubTag == "NewVersion") {
                    // Send to new DSN by creating a separate client
                    android.util.Log.d("SentryScopeManager", "Routing event to NEW DSN")
                    sendToNewDSN(event, hint)
                    // Return null to prevent sending to old DSN
                    return@setBeforeSend null
                } else {
                    // Send to old DSN (default)
                    event.tags!!["hub"] = "OldVersion"
                    android.util.Log.d("SentryScopeManager", "Sending event to OLD DSN")
                    return@setBeforeSend event
                }
            }
        }
        
        // Get the initialized hub
        oldVersionHub = Sentry.getCurrentHub()
        
        // Create a wrapper hub for new version that sets the tag
        newVersionHub = oldVersionHub?.let { mainHub ->
            NewVersionHubWrapper(mainHub)
        } ?: NoOpHub.getInstance()
        
        android.util.Log.i("SentryScopeManager", "Both hubs initialized")
    }
    
    /**
     * Manually send event to the new DSN using reflection
     */
    private fun sendToNewDSN(event: SentryEvent, hint: Hint) {
        try {
            val newOptions = io.sentry.android.core.SentryAndroidOptions().apply {
                dsn = NEW_DSN
                isEnableUncaughtExceptionHandler = false
            }
            
            // Create client using reflection since constructor is package-private
            val clientConstructor = SentryClient::class.java.getDeclaredConstructor(SentryOptions::class.java)
            clientConstructor.isAccessible = true
            val client = clientConstructor.newInstance(newOptions) as SentryClient
            
            val scope = Scope(newOptions)
            scope.setTag("hub", "NewVersion")
            client.captureEvent(event, scope, hint)
            android.util.Log.d("SentryScopeManager", "Event sent to new DSN")
        } catch (e: Exception) {
            android.util.Log.e("SentryScopeManager", "Failed to send to new DSN", e)
        }
    }
    
    /**
     * Wrapper hub that sets the "NewVersion" tag before capturing
     */
    class NewVersionHubWrapper(
        private val delegate: IHub
    ) : IHub by delegate {
        
        fun captureExceptionWithTag(throwable: Throwable, hint: Hint = Hint()): Any {
            delegate.configureScope { scope ->
                scope.setTag("hub", "NewVersion")
            }
            return delegate.captureException(throwable, hint)
        }
        
        fun captureMessageWithTag(message: String, level: SentryLevel = SentryLevel.INFO): Any {
            delegate.configureScope { scope ->
                scope.setTag("hub", "NewVersion")
            }
            return delegate.captureMessage(message, level)
        }
    }
    
    /**
     * Get the Hub instance for the old version UI
     */
    fun getOldVersionHub(): IHub? = oldVersionHub
    
    /**
     * Get the Hub instance for the new version UI
     */
    fun getNewVersionHub(): IHub? = newVersionHub
    
    /**
     * Clean up resources when done
     */
    fun close() {
        oldVersionHub?.close()
        newVersionHub?.close()
        oldVersionHub = null
        newVersionHub = null
    }
}

