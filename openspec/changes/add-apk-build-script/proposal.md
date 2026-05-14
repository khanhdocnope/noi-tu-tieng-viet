## Why

The user wants to be able to compile the Android app into an `.apk` file locally ("biên dịch ra file exe" then "thành apk"). Currently, the repository relies entirely on GitHub Actions for compiling the APK, and lacks the Gradle Wrapper scripts (`gradlew`, `gradlew.bat`, and `gradle/wrapper/`) needed to compile the project locally without a global Gradle installation.

## What Changes

1. Add the Gradle Wrapper to the `AndroidKeyboard` project so anyone can build the APK locally.
2. Update the `README.md` to include instructions on how to compile the Android app to an APK on a local machine.

## Capabilities

### New Capabilities
- `local-apk-build`: The ability to run local gradle commands to build the Android application.

### Modified Capabilities
- `documentation`: Update `README.md` to reflect the new local build process.

## Impact

- Adds new files: `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`, `gradle/wrapper/gradle-wrapper.properties` to `AndroidKeyboard/`.
- Modifies: `README.md`.
