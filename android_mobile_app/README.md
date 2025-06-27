# WakeMind - Smart Alarm App

A minimal Android alarm app that wakes users up by making them solve simple math or pattern recognition challenges.

## Features

- **Fixed Daily Alarms**: Automatically schedules alarms at 12:00 AM and 12:15 AM every day
- **Challenge-Based Dismissal**: Users must solve a random challenge to dismiss the alarm
- **Silent Mode Override**: Alarms ring even when the device is in silent mode
- **Optional Vibration**: Supports vibration alongside alarm sound
- **Auto-Rescheduling**: Alarms automatically reschedule for the next day
- **Challenge Pool**: 10 daily challenges (5 math, 5 pattern recognition)
- **Minimal UI**: Clean, dark-themed interface with one-tap activation
- **Lock Screen Support**: Challenge appears even on locked devices

## How to Use

1. **Start Alarms**: Tap the "Start Daily Alarms" button on the main screen
2. **Daily Wake-Up**: The app will ring at 12:00 AM and 12:15 AM each day
3. **Solve Challenge**: When the alarm rings, solve the displayed challenge:
   - **Math Challenge**: Select the correct answer from three options
   - **Pattern Challenge**: Tap the pattern sequence in the correct order
4. **Alarm Dismissed**: Once solved correctly, the alarm stops and reschedules for tomorrow

## Technical Details

- **Minimum SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 14 (API 34)
- **Permissions**: Wake lock, vibration, exact alarms, foreground service
- **Architecture**: Activity-based with broadcast receivers and foreground service

## Challenge Types

### Math Challenges
- Simple arithmetic: addition, subtraction, multiplication, division
- Examples: "5 + 5", "10 - 3", "6 × 2", "15 ÷ 3"

### Pattern Challenges
- Tap sequence patterns on a 3x3 grid
- Examples: Linear patterns, corners, center-focused sequences

## Installation

1. Build the project using Android Studio or Gradle
2. Install the APK on your Android device
3. Grant necessary permissions when prompted
4. Tap "Start Daily Alarms" to activate

## Notes

- The app requires exact alarm permissions on Android 12+
- Wake lock permissions ensure the alarm works even in doze mode
- Foreground service keeps the alarm running reliably
- Boot receiver reschedules alarms after device restart
```
>>>>>>> REPLACE
