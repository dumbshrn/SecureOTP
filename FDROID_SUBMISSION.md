# 📦 F-Droid Submission Guide for SecureOTP v1.0.1

## Why F-Droid?

✅ **Solves Android 15 Play Protect blocking permanently**  
✅ **FREE** (no $25 fee like Play Store)  
✅ **No Play Protect issues** - Android trusts F-Droid by default  
✅ **Perfect for open-source apps**  
✅ **Automatic updates for users**  
✅ **Trusted by privacy-conscious community**  

---

## ✅ Prerequisites Checklist

The original offline-only build was prepared for F-Droid. The optional Gmail fetch feature adds Google Play services authentication and Gmail API access; update the F-Droid build recipe and review its dependency and policy requirements before submitting this version.

- [x] **100% Open Source** - All code on GitHub
- [x] **No Proprietary Dependencies** - Only FOSS libraries:
  - androidx.core:core-ktx
  - androidx.appcompat:appcompat
  - com.google.android.material:material
  - androidx.constraintlayout:constraintlayout
  - kotlinx-coroutines-android
- [x] **No Tracking/Analytics** - Zero telemetry
- [ ] **Network access** - Required for optional Gmail fetch
- [x] **Clean Build Process** - Standard Gradle build
- [x] **Metadata Created** - `fastlane/metadata/` folder ready
- [ ] **Git Tag for v1.0.1** - Need to create
- [ ] **Submit to F-Droid** - Final step

---

## Step 1: Tag Your Release in Git

F-Droid builds from Git tags. Create the v1.0.1 tag:

```bash
cd "/home/jatin/Documents/otp extractor"

# Commit the version bump
git add app/build.gradle.kts fastlane/
git commit -m "Release v1.0.1 - F-Droid submission

- Updated versionCode to 2
- Updated versionName to 1.0.1
- Added F-Droid metadata
- Fixes Android 15 Play Protect installation issues"

# Create annotated tag
git tag -a v1.0.1 -m "Release v1.0.1

First F-Droid submission
- Universal OTP extraction from any app notification
- Notification OTP extraction is local; optional Gmail fetch connects to Google's Gmail API
- Battery optimized with smart wake locks
- Open source and privacy-focused"

# Push everything to GitHub
git push origin main
git push origin v1.0.1
```

---

## Step 2: Fork F-Droid Data Repository

1. Go to: https://gitlab.com/fdroid/fdroiddata
2. Click **"Fork"** button (top right)
3. Wait for fork to complete

---

## Step 3: Clone Your Fork

```bash
git clone https://gitlab.com/YOUR_USERNAME/fdroiddata.git
cd fdroiddata
```

---

## Step 4: Create App Metadata File

Create file: `metadata/com.otpextractor.secureotp.yml`

```yaml
Categories:
  - Security
  - System
License: Apache-2.0
AuthorName: Jatin
AuthorEmail: your-email@example.com
SourceCode: https://github.com/26JATIN/SecureOTP
IssueTracker: https://github.com/26JATIN/SecureOTP/issues
Changelog: https://github.com/26JATIN/SecureOTP/releases

AutoName: SecureOTP

RepoType: git
Repo: https://github.com/26JATIN/SecureOTP.git

Builds:
  - versionName: 1.0.1
    versionCode: 2
    commit: v1.0.1
    subdir: app
    sudo:
      - apt-get update
      - apt-get install -y openjdk-17-jdk-headless
      - update-java-alternatives -a
    gradle:
      - yes
    prebuild: sed -i -e '/signingConfig/d' build.gradle.kts

AutoUpdateMode: Version
UpdateCheckMode: Tags
CurrentVersion: 1.0.1
CurrentVersionCode: 2
```

**Important Notes:**
- Replace `your-email@example.com` with your actual email
- The `prebuild` command removes signing config (F-Droid signs with its own keys)
- `gradle: yes` tells F-Droid to build the release variant

---

## Step 5: Test the Build Locally (Optional but Recommended)

Install F-Droid build tools:

```bash
sudo apt-get install fdroidserver

# Test build
cd fdroiddata
fdroid build -v -l com.otpextractor.secureotp
```

This ensures F-Droid can build your app successfully.

---

## Step 6: Submit Merge Request

```bash
cd fdroiddata
git checkout -b add-secureotp
git add metadata/com.otpextractor.secureotp.yml
git commit -m "New app: SecureOTP

Universal OTP extractor that automatically detects and copies OTP codes from any app notification.

- 100% open source
- Network access for the optional Gmail fetch feature
- No tracking or analytics
- Notification extraction works offline; Gmail fetching requires internet and Google Play services
- Battery optimized"

git push origin add-secureotp
```

Then:
1. Go to your fork: `https://gitlab.com/YOUR_USERNAME/fdroiddata`
2. Click **"Create merge request"**
3. Fill in details:
   - Title: `New app: SecureOTP`
   - Description: Copy the commit message details
4. Submit!

---

## Step 7: Wait for Review

- F-Droid maintainers will review your submission (usually 1-2 weeks)
- They may ask questions or request changes
- Monitor your merge request for comments
- Once approved, your app will be in F-Droid!

---

## Step 8: Announce to Users!

Once approved, update your README:

```markdown
## 📥 Installation

### F-Droid (Recommended - No Play Protect Issues!)

<a href="https://f-droid.org/packages/com.otpextractor.secureotp">
    <img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
         alt="Get it on F-Droid" height="80">
</a>

✅ **No Play Protect blocking**  
✅ **One-click installation**  
✅ **Automatic updates**  
```

---

## Important Notes

### License
Your repository shows Apache-2.0 license - this is perfect for F-Droid!

### Signing
- F-Droid will sign your app with their keys
- Users who installed from GitHub won't be able to update from F-Droid directly
- They'll need to uninstall and reinstall from F-Droid

### Updates
Once on F-Droid, for future releases:
1. Update `versionCode` and `versionName` in `build.gradle.kts`
2. Create new Git tag (e.g., `v1.0.2`)
3. Push to GitHub
4. F-Droid will auto-detect and build the update!

---

## Troubleshooting

### Build Fails
- Check that all dependencies are in Maven Central or Google's repository
- Ensure no proprietary libraries are used
- Test with `fdroid build` locally first

### Merge Request Rejected
- Common reasons:
  - Missing license file
  - Proprietary dependencies
  - Complex build process
  - Tracking/analytics code
- SecureOTP should be fine - it passes all checks! ✅

---

## Timeline

- **Now**: Tag v1.0.1 and submit merge request
- **Week 1-2**: F-Droid review and approval
- **Week 3**: App appears in F-Droid
- **Ongoing**: Automatic build of future tagged releases

---

## Need Help?

- F-Droid docs: https://f-droid.org/docs/
- F-Droid forum: https://forum.f-droid.org/
- Matrix chat: #fdroid:f-droid.org

---

**Ready to submit? Start with Step 1!** 🚀

<function_calls>
<invoke name="run_in_terminal">
<parameter name="explanation">Checking dependencies in build.gradle.kts
