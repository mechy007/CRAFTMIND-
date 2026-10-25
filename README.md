# CraftMind Studio

CraftMind Studio is an Android-first, standalone Minecraft Bedrock add-on creator. It is being developed separately from CraftNode; integration can be added later through a versioned bridge.

## MVP (0.1.0)
- Native Android app built with Kotlin and Jetpack Compose.
- Offline prompt-to-template flow for a starter custom item.
- Exports a Bedrock .mcaddon containing behavior and resource packs.
- Generates a simple 16×16 item texture locally.
- No account, API key, or paid AI provider required for this prototype.

## Important limitations
- This first version is a deterministic template generator, not yet an LLM-powered AI creator.
- The initial item template maps a few words to item stats. It does not implement arbitrary behavior such as lightning strikes, custom mobs, animations, or scripts yet.
- Generated packs must be tested in the user's target Minecraft Bedrock version. A successful APK build is not proof that every pack imports or behaves correctly.
- AI providers will be optional and replaceable. Never embed a shared private API key in the APK.
- This project does not control or modify a live CraftNode server.

## Build
Requires JDK 17+, Android SDK 35, and Gradle 8.10.2 or compatible.

    gradle test
    gradle :app:assembleDebug

The debug APK is produced at app/build/outputs/apk/debug/app-debug.apk.

## Roadmap
1. Build and install the native app; verify export/import on Bedrock Android.
2. Add pack validation, deterministic IDs, and tests for generated archive structure.
3. Add more item templates and genuine Script API behavior.
4. Add an optional provider interface with user-supplied credentials/free-tier choices.
5. Add prompt planning, code generation, schema validation, and iterative repair.
6. Add preview, saved projects, import/export management, and a versioned CraftNode bridge after the standalone app is reliable.
