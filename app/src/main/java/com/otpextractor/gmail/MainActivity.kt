package com.otpextractor.secureotp

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.IntentSenderRequest
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.otpextractor.secureotp.databinding.ActivityMainBinding
import com.otpextractor.secureotp.service.OtpListener
import com.otpextractor.secureotp.utils.GmailOtpFetcher
import com.otpextractor.secureotp.utils.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefManager: PreferenceManager
    private var wasListenerEnabledOnPause = false
    private var fetchAfterAuthorization = false

    private val gmailAuthorizationLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            try {
                val authorizationResult = Identity.getAuthorizationClient(this)
                    .getAuthorizationResultFromIntent(result.data)
                onGmailAuthorized(authorizationResult.accessToken)
            } catch (e: Exception) {
                showGmailError(e)
            }
        } else {
            fetchAfterAuthorization = false
            Toast.makeText(this, "Gmail access was not granted", Toast.LENGTH_SHORT).show()
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefManager = PreferenceManager(this)
        
        // Initialize the state on first load
        wasListenerEnabledOnPause = isNotificationListenerEnabled()

        setupUI()
        updateUI()
        if (intent?.action == ACTION_FETCH_GMAIL_OTP) {
            handleFetchIntent(intent)
        } else {
            checkPermissions()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        // Remember the state when leaving the activity
        wasListenerEnabledOnPause = isNotificationListenerEnabled()
    }
    
    override fun onResume() {
        super.onResume()
        
        // Check if listener was just enabled
        val isNowEnabled = isNotificationListenerEnabled()
        
        // If listener was just enabled (wasn't enabled when we left, but is now)
        if (!wasListenerEnabledOnPause && isNowEnabled) {
            // Show success feedback
            showPermissionGrantedFeedback()
            wasListenerEnabledOnPause = true
        }
        
        updateUI()
        updateGmailStatus()
        checkAndShowGmailWarning()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleFetchIntent(intent)
    }
    
    private fun showPermissionGrantedFeedback() {
        // Haptic feedback
        try {
            binding.root.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)
        } catch (e: Exception) {
            // Ignore if haptic not available
        }
        
        // Show success snackbar with action
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            "🎉 Notification access granted! OTP monitoring is now active.",
            com.google.android.material.snackbar.Snackbar.LENGTH_LONG
        ).setAction("Got it") {
            // Dismiss
        }.setBackgroundTint(ContextCompat.getColor(this, R.color.success))
        .setTextColor(ContextCompat.getColor(this, android.R.color.white))
        .show()
    }

    private fun setupUI() {
        binding.btnEnableService.setOnClickListener {
            openNotificationListenerSettings()
        }

        binding.switchService.setOnCheckedChangeListener { _, isChecked ->
            prefManager.setServiceEnabled(isChecked)
            if (isChecked && !isNotificationListenerEnabled()) {
                // Don't allow enabling if permission not granted
                binding.switchService.isChecked = false
            } else {
                updateStatusText()
            }
        }

        binding.btnOptimizeGmail.setOnClickListener {
            openGmailOptimization()
        }

        binding.btnConnectGmail.setOnClickListener {
            authorizeGmail(fetchLatest = false)
        }

        setupWarningBanner()
    }

    private fun setupWarningBanner() {
        binding.warningBanner.root.findViewById<android.widget.ImageButton>(R.id.btnCloseWarning).setOnClickListener {
            hideWarningBanner()
        }

        binding.warningBanner.root.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnDontShowAgain).setOnClickListener {
            prefManager.setGmailWarningDismissed(true)
            hideWarningBanner()
            Toast.makeText(this, "Gmail warning disabled", Toast.LENGTH_SHORT).show()
        }

        binding.warningBanner.root.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnFixGmailWarning).setOnClickListener {
            openGmailOptimization()
        }
    }

    private fun checkAndShowGmailWarning() {
        // Don't show if user dismissed it
        if (prefManager.isGmailWarningDismissed()) {
            hideWarningBanner()
            return
        }

        // Check if Gmail has restrictions
        val hasRestrictions = com.otpextractor.secureotp.utils.BackgroundRestrictionChecker.hasGmailRestrictions(this)
        
        if (hasRestrictions) {
            showWarningBanner()
        } else {
            hideWarningBanner()
        }
    }

    private fun showWarningBanner() {
        binding.warningBanner.root.visibility = android.view.View.VISIBLE
    }

    private fun hideWarningBanner() {
        binding.warningBanner.root.visibility = android.view.View.GONE
    }

    private fun openGmailOptimization() {
        val intent = Intent(this, GmailOptimizationActivity::class.java)
        startActivity(intent)
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun updateUI() {
        val isListenerEnabled = isNotificationListenerEnabled()
        val isServiceEnabled = prefManager.isServiceEnabled()

        binding.switchService.isEnabled = isListenerEnabled
        binding.switchService.isChecked = isServiceEnabled

        if (isListenerEnabled) {
            // Green checkmark when granted
            binding.tvStatus.text = "✅ " + getString(R.string.access_granted)
            binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.success))
            binding.btnEnableService.isEnabled = false
            binding.btnEnableService.alpha = 0.5f
        } else {
            // Red X when not granted
            binding.tvStatus.text = "❌ " + getString(R.string.access_required)
            binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.error))
            binding.btnEnableService.isEnabled = true
            binding.btnEnableService.alpha = 1.0f
        }

        updateStatusText()
        updateGmailStatus()
    }

    private fun updateGmailStatus() {
        val accountEmail = prefManager.getGmailAccountEmail()
        binding.tvGmailStatus.text = if (accountEmail == null) {
            getString(R.string.gmail_not_connected)
        } else {
            getString(R.string.gmail_connected_as, accountEmail)
        }
    }

    private fun handleFetchIntent(intent: Intent?) {
        if (intent?.action == ACTION_FETCH_GMAIL_OTP) {
            intent.action = null
            authorizeGmail(fetchLatest = true)
        }
    }

    private fun authorizeGmail(fetchLatest: Boolean) {
        fetchAfterAuthorization = fetchLatest
        val request = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(GMAIL_READONLY_SCOPE)))
            .build()

        Identity.getAuthorizationClient(this)
            .authorize(request)
            .addOnSuccessListener { authorizationResult ->
                if (authorizationResult.hasResolution()) {
                    val pendingIntent = authorizationResult.pendingIntent
                    if (pendingIntent == null) {
                        showGmailError(IllegalStateException("Google authorization could not be started"))
                        return@addOnSuccessListener
                    }
                    gmailAuthorizationLauncher.launch(
                        IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                    )
                } else {
                    onGmailAuthorized(authorizationResult.accessToken)
                }
            }
            .addOnFailureListener { error ->
                fetchAfterAuthorization = false
                showGmailError(error)
            }
    }

    private fun onGmailAuthorized(accessToken: String?) {
        if (accessToken.isNullOrBlank()) {
            fetchAfterAuthorization = false
            Toast.makeText(this, "Google did not return Gmail access", Toast.LENGTH_LONG).show()
            return
        }

        val shouldFetch = fetchAfterAuthorization
        fetchAfterAuthorization = false
        binding.btnConnectGmail.isEnabled = false

        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    GmailOtpFetcher.fetch(accessToken, shouldFetch)
                }
                prefManager.setGmailAccountEmail(result.accountEmail)
                updateGmailStatus()
                if (!shouldFetch) {
                    Toast.makeText(this@MainActivity, "Gmail connected", Toast.LENGTH_SHORT).show()
                } else if (result.otp == null) {
                    Toast.makeText(this@MainActivity, "No recent OTP found in Gmail", Toast.LENGTH_LONG).show()
                } else {
                    val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("OTP", result.otp))
                    Toast.makeText(this@MainActivity, "OTP copied from Gmail", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                showGmailError(e)
            } finally {
                binding.btnConnectGmail.isEnabled = true
            }
        }
    }

    private fun showGmailError(error: Throwable) {
        val message = error.message?.takeIf { it.isNotBlank() } ?: "Unable to access Gmail"
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun updateStatusText() {
        val isListenerEnabled = isNotificationListenerEnabled()
        val isServiceEnabled = prefManager.isServiceEnabled()

        binding.tvServiceStatus.text = when {
            !isListenerEnabled -> getString(R.string.service_inactive)
            !isServiceEnabled -> getString(R.string.service_disabled)
            else -> getString(R.string.service_active)
        }
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val cn = ComponentName(this, OtpListener::class.java)
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat != null && flat.contains(cn.flattenToString())
    }

    private fun openNotificationListenerSettings() {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            startActivity(intent)
            
            // Show instructions in a toast
            Toast.makeText(
                this,
                "📱 Step 1: Find 'SecureOTP' in the list\n" +
                "📱 Step 2: Toggle it ON\n" +
                "📱 Step 3: Press Back to return here",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open settings", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val ACTION_FETCH_GMAIL_OTP = "com.otpextractor.secureotp.action.FETCH_GMAIL_OTP"
        private const val GMAIL_READONLY_SCOPE = "https://www.googleapis.com/auth/gmail.readonly"
    }


}
