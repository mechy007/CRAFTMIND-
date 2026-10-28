# CraftMind and CraftMind Studio

This repository contains two related but separate pieces: the original CraftMind core for a future CraftNode bridge, and the new standalone Android app prototype, CraftMind Studio.

## CraftMind core
The transport-neutral Kotlin/JVM core provides a versioned `craftmind.bridge.v1` contract and safety-first validation of tool proposals.

- Kotlin/JVM targeting Java 17.
- Deterministic offline demo provider; no API key or network required.
- Read-only tools only: `server.status`, `server.logs.explain`, `world.list`, `world.get`, and `players.list`.
- Tests for protocol validation, allowlists, oversized input, provider failure, and attempted write-tool injection.
- Not connected to CraftNode yet and does not control a live server. CraftNode must execute tools and verify actual state.

## CraftMind Studio Android MVP (0.1.0)
- Native Android app built with Kotlin and Jetpack Compose.
- Offline prompt-to-template flow for a starter custom Bedrock item.
- Exports a .mcaddon containing separate behavior and resource .mcpack archives.
- Generates a simple 16×16 item texture locally.
- No account, API key, or paid AI provider required for this prototype.

### Important limitations
- This first Studio version is a deterministic template generator, not yet an LLM-powered AI creator.
- The initial item template maps a few words to item stats. It does not implement arbitrary behavior such as lightning strikes, custom mobs, animations, or scripts yet.
- Generated packs must be tested in the user's target Minecraft Bedrock version. A successful APK build is not proof that every pack imports or behaves correctly.
- AI providers will be optional and replaceable. Never embed a shared private API key in the APK.
- Studio does not control or modify a live CraftNode server.

## Build
Requires JDK 17+, Android SDK 35, and Gradle 8.10.2 or compatible.

    gradle test
    gradle :app:assembleDebug

The debug APK is produced at app/build/outputs/apk/debug/app-debug.apk. GitHub Actions uploads it as an artifact when the Android build passes.

## Safety boundary
The CraftMind core never executes shell or server-console commands, never claims a server action succeeded, and never stores provider credentials. CraftNode remains authoritative for IDs, capabilities, confirmation, execution, and verification. Future providers must use user-configured credentials, not a shared secret embedded in the APK.

## Roadmap
1. Build and install the native app; verify export/import on Bedrock Android.
2. Add pack validation, deterministic IDs, and tests for generated archive structure.
3. Add more item templates and genuine Script API behavior.
4. Add an optional provider interface with user-supplied credentials/free-tier choices.
5. Add prompt planning, code generation, schema validation, and iterative repair.
6. Add preview, saved projects, import/export management, and a versioned CraftNode bridge after the standalone app is reliable.
