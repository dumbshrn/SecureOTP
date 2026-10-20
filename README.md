# SecureOTP - Universal OTP Extractor for Android

A smart Android utility app built with Kotlin that detects OTPs in app notifications and can fetch recent OTP emails from Gmail on demand.

## ✨ Features

### 🌐 Universal App Support
- **SMS Messages**: Auto-extracts OTPs from all messaging apps
- **Email**: Gmail, Outlook, Yahoo Mail, ProtonMail, and more
- **Banking Apps**: All major banks and fintech apps  
- **Social Media**: WhatsApp, Instagram, Facebook, Twitter, Telegram
- **Payment Apps**: PayPal, Paytm, PhonePe, Google Pay, and more
- **Any App**: Works with any app that sends OTP notifications!
- **Gmail Fetch**: Connect Gmail and tap the Android Quick Settings tile to scan recent inbox messages

### 🎯 Smart Detection
- Intelligently identifies OTP codes even when notifications contain multiple numbers
- Filters out phone numbers, dates, and tracking numbers
- Context-aware pattern matching for maximum accuracy
- Supports various OTP formats:
  - "OTP for 2310990533 is 4279" → Copies `4279`
  - "Your verification code: 123456"  
  - "Use code 5678 to login"
  - "Authentication code is 9012"
  - And many more patterns!

### 🔋 Battery Optimized
- Efficient background processing with minimal battery impact
- Smart wake lock usage (max 3 seconds per notification)
- Uses coroutines for non-blocking operations
- No polling, no API calls, no 24/7 background services
- Hardware accelerated for smooth performance

### 🔒 Privacy & Security
- **On-device extraction**: Gmail message text is processed locally and is not saved by SecureOTP
- **On-demand Gmail access**: Gmail is contacted only after you connect an account and tap Fetch OTPs
- **No app backend**: SecureOTP does not send mail or OTP data to its own servers
- **Open Source**: Fully transparent code you can audit
- **No Permissions Abuse**: Only uses NotificationListenerService

### ⚡ Performance
- Instant OTP detection as notifications arrive
- Auto-copy to clipboard - ready to paste immediately
- Minimal memory footprint
- Optimized for low-end devices

## Requirements

- Android 8.0 (API 26) or higher
- Internet access is used only for the optional Gmail fetch feature
- No account setup required
- Works with all apps that send notifications

## Setup Instructions

### 1. Install Via APK

1. Download release apk from release section
2. before installing u need to disable google play protect as google wont allow notification permission from a non verified developer
3. go to play store, click on profile button go to play protect and turn off it!!
4. click the apk and install the app.

### 2. Using the App

1. **Launch SecureOTP**
2. **Grant Notification Access**: Tap "Grant Notification Access" button
   - You'll be taken to Settings
   - Find and enable "SecureOTP" in the notification access list
3. **Enable OTP Detection**: Toggle the switch to ON
4. **Done!** The app will now automatically:
   - Monitor all app notifications for OTPs
   - Extract the OTP code intelligently
   - Copy it to your clipboard
   - Show you a notification with the copied OTP

### Fetch OTPs directly from Gmail

1. In SecureOTP, tap **Connect Gmail** and grant the requested read-only Gmail access.
2. Open Android Quick Settings, tap **Edit**, and add the **Fetch OTPs** tile.
3. Tap **Fetch OTPs** when you want SecureOTP to scan up to 20 inbox messages from the last day.
4. A detected OTP is copied to the clipboard. Gmail content is processed on the device and not stored by the app.

The GitHub Actions debug APK uses package name `com.otpextractor.secureotp.debug`; its signing SHA-1 is included in the `build-info.txt` artifact. Create an Android OAuth client with those exact values to use Gmail with that APK. GitHub-hosted debug signing keys can change between runs, so use the fingerprint from the exact APK you install. Release builds use package name `com.otpextractor.secureotp` and need a client configured with the release signing certificate fingerprint. Enable the Gmail API in the same Google Cloud project. During development, add your account as an OAuth test user. Public distribution with `gmail.readonly` requires Google's restricted-scope verification. Gmail sign-in also requires Google Play services on the device.

### 3. Testing

Send yourself a test OTP via:
- SMS
- Email (Gmail, Outlook, etc.)
- Banking app notification
- Any app that sends OTPs

The OTP will be automatically copied to your clipboard!

## How It Works

1. **Notification Monitoring**: Uses Android's `NotificationListenerService` to monitor notifications from all apps
2. **Smart Filtering**: Filters out system notifications and non-relevant apps
3. **Pattern Matching**: Uses advanced regex patterns to identify OTP codes
4. **Context Analysis**: Understands OTP-related keywords to distinguish OTPs from other numbers
5. **Battery Efficient**: Processes notifications asynchronously with minimal wake lock usage
6. **Auto-Copy**: Immediately copies the detected OTP to clipboard

## Supported OTP Patterns

The app recognizes various OTP formats including:
- Standard: "OTP is 123456"
- SMS style: "123456 is your verification code"
- Banking: "Use code 123456"
- Email: "Your authentication code: 123456"
- With phone numbers: "OTP for 9876543210 is 123456"
- Formatted: "Code: 12-34-56"
- And many more!

## Privacy & Permissions

### Permissions Used:
- **Notification Access**: To read notifications for OTP extraction
- **Post Notifications**: To show you confirmation when OTP is copied
- **Wake Lock**: For efficient background processing (minimal usage)

### Privacy:
- ✅ All processing happens locally on your device
- ✅ Gmail access is optional and user-triggered
- ✅ No email content or access token is saved by SecureOTP
- ✅ No app backend, data collection, or analytics
- ✅ No ads or trackers
- ✅ Open source - you can verify yourself!

## Battery Optimization

SecureOTP is designed for minimal battery impact:
- Uses event-driven architecture (no polling)
- Coroutines for non-blocking operations  
- Wake lock held for max 3 seconds per notification
- Efficient memory management
- No background services when not needed

## Architecture

Built with modern Android development practices:
- **Language**: Kotlin
- **Architecture**: Service-based with coroutines
- **UI**: Material Design 3 components
- **Background Processing**: NotificationListenerService + Coroutines
- **Minimum SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is open source. Feel free to use and modify as needed.

## Disclaimer

This app is for personal use to improve your OTP entry experience. Always ensure you're using it on your personal device and following your organization's security policies.
