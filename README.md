# Sentry Multi-DSN Android Example

This is a minimal Android application that demonstrates how to use the Sentry Java SDK with multiple DSNs in a single application using the Hub API.

## Overview

The app creates two isolated Sentry instances using the Hub API, each configured with a different DSN:

- **Old Version DSN**: Events from the "Old Version UI" section are sent to this DSN
- **New Version DSN**: Events from the "New Version UI" section are sent to this DSN

## Architecture

The implementation uses a single Sentry instance initialized with the old DSN, and routes events to different DSNs using a `beforeSend` callback. When events are tagged with "NewVersion", they are manually sent to the new DSN using reflection to create a separate `SentryClient`.

### Key Components

1. **SentryScopeManager**: A singleton object that manages routing events to different DSNs:
   - Initializes Sentry once with the old DSN
   - Uses a `beforeSend` callback to check for the "hub" tag
   - Routes "NewVersion" events to the new DSN using reflection to create a `SentryClient`
   - Provides wrapper hubs for both old and new versions

2. **MainActivity**: The main activity with two UI sections:
   - Old Version UI (top section)
   - New Version UI (bottom section)
   
   Each section has buttons to trigger errors or capture messages, which are sent to the respective DSN.

## How It Works

1. When the app starts, `SentryScopeManager.initialize()` initializes Sentry once with the old DSN.
2. The `beforeSend` callback checks for a "hub" tag on each event:
   - If tag is "NewVersion" → manually sends to new DSN using reflection to create a `SentryClient`
   - Otherwise → sends to old DSN (default)
3. The new version hub wrapper sets the "NewVersion" tag before capturing events.
4. Each event is tagged with a "hub" tag ("OldVersion" or "NewVersion") to identify which DSN it was sent to.

## Building and Running

1. Open the project in Android Studio
2. Sync Gradle files
3. Build and run on an Android device or emulator (API 24+)

## Testing

1. Tap "Trigger Error" in the Old Version section → Error sent to Old DSN
2. Tap "Capture Message" in the Old Version section → Message sent to Old DSN
3. Tap "Trigger Error" in the New Version section → Error sent to New DSN
4. Tap "Capture Message" in the New Version section → Message sent to New DSN

Check your Sentry projects to verify that events are being received in the correct projects.

## References

- [GitHub Issue: Multi DSN per Java Application support](https://github.com/getsentry/sentry-java/issues/2736)
- [Sentry Documentation: Using Sentry within an SDK](https://docs.sentry.io/platforms/android/configuration/shared-environments/)

