# Call History - Start.io test banner diagnostics

## What changed
- Removed Start.io ads from the cross-app Phone/Dialer overlay. Ads are now tested only inside the app's own Activity.
- Added an in-app `Test Start.io banner ad` screen.
- Uses Start.io's official demo App ID `205489527` and `StartAppSDK.setTestAdsEnabled(true)`.
- Uses the official `BannerRequest` / `BannerCreator` banner flow from Start.io's Android SDK demo.
- Displays SDK initialization, request result, load error text, view creation errors, and measured banner dimensions in an on-screen diagnostics log and Logcat (`StartIoBannerTest`).
- The test shortcut is available even before call permissions are granted.

## Build
Push the ZIP contents to GitHub and run the existing GitHub Actions workflow (`Build APK`). It creates the Android scaffold, adds `com.startapp:inapp-sdk:5.+`, and builds the release APK.

## Test on Android
1. Install and open the app.
2. Tap **Test Start.io banner ad**.
3. Wait for SDK initialization and the banner request.
4. Read the diagnostics on screen. A successful load should show `LOAD SUCCESS` and a measured banner view size.
5. If it fails, the screen records the SDK/request error. Share that exact error or Logcat lines tagged `StartIoBannerTest`.

This uses Start.io's public demo/test App ID only. It does not validate the user's production App ID or guarantee that a live ad will be served. The final result must be tested on a real Android device with network access.
