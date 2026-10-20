package com.otpextractor.secureotp.service

import android.content.Intent
import android.service.quicksettings.TileService
import com.otpextractor.secureotp.MainActivity

class FetchOtpTileService : TileService() {

    override fun onClick() {
        super.onClick()
        val intent = Intent(this, MainActivity::class.java).apply {
            action = MainActivity.ACTION_FETCH_GMAIL_OTP
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivityAndCollapse(intent)
    }
}
