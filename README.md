# CreditGuard 🛡️

**Your personal credit card spend guardian** - Never fall into credit card debt again!

## What it does

CreditGuard instantly detects when you spend on your credit card and prompts you to set aside that exact amount into your "repayment vault" via UPI. When your bill comes, you already have the money ready!

### Flow
1. 💳 You swipe your credit card
2. 📱 Bank sends SMS: "Rs. 2,499 spent..."
3. 🔔 CreditGuard notification: "Set aside ₹2,499 now?"
4. 👆 Tap → UPI app opens → Amount pre-filled → Done!
5. 🎉 Bill comes → Money already saved!

## Features

- **Real-time SMS Detection** - Catches credit card spend SMS from all major Indian banks
- **Instant UPI Payment** - One-tap to set aside money via any UPI app
- **Transaction History** - Track all your credit card spends
- **Beautiful UI** - Material 3 design with Jetpack Compose
- **Privacy First** - All data stays on your device

## Supported Banks

HDFC, ICICI, SBI, Axis, Kotak, Citi, Amex, IndusInd, Yes Bank, RBL, IDFC First

## Build from CLI (No Android Studio needed)

### Prerequisites

1. **Java 17+**
   ```bash
   sudo apt install openjdk-17-jdk
   ```

2. **Android SDK** (command-line tools)
   ```bash
   # Download from https://developer.android.com/studio#command-tools
   mkdir -p ~/Android/Sdk/cmdline-tools
   unzip commandlinetools-linux-*.zip -d ~/Android/Sdk/cmdline-tools/
   mv ~/Android/Sdk/cmdline-tools/cmdline-tools ~/Android/Sdk/cmdline-tools/latest
   
   export ANDROID_HOME=$HOME/Android/Sdk
   export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
   
   # Accept licenses and install required packages
   sdkmanager --licenses
   sdkmanager "platforms;android-34" "build-tools;34.0.0" "platform-tools"
   ```

### Build

```bash
chmod +x build.sh
./build.sh
```

Or manually:
```bash
./gradlew assembleDebug
```

APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

### Install

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Setup in App

1. Open CreditGuard
2. Grant SMS and Notification permissions
3. Go to Settings → Enter your UPI ID (your savings account)
4. Done! Start using your credit card worry-free

## Architecture

```
com.creditguard/
├── data/
│   ├── db/          # Room database
│   └── model/       # Transaction entity
├── receiver/        # SMS BroadcastReceiver
├── ui/
│   ├── screens/     # Compose screens
│   └── theme/       # Material 3 theme
└── util/            # SMS parser, UPI helper, Notifications
```

## Tech Stack

- Kotlin
- Jetpack Compose + Material 3
- Room Database
- Coroutines + Flow
- ViewModel

## Permissions

- `RECEIVE_SMS` - Detect credit card transaction SMS
- `READ_SMS` - Parse transaction details
- `POST_NOTIFICATIONS` - Show spend alerts

## Privacy

- ✅ All SMS processing happens locally on device
- ✅ No data sent to any server
- ✅ No analytics or tracking
- ✅ Transaction history stored only in local database

## License

MIT
