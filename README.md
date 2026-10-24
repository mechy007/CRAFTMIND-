# CraftMind

CraftMind is the separate AI brain for CraftNode. This first milestone provides a transport-neutral Kotlin/JVM core, a versioned `craftmind.bridge.v1` contract, and safety-first validation of tool proposals.

## Current milestone
- Kotlin/JVM targeting Java 17.
- Deterministic offline demo provider; no API key or network required.
- Read-only tools only: `server.status`, `server.logs.explain`, `world.list`, `world.get`, and `players.list`.
- Tests for protocol validation, allowlists, oversized input, provider failure, and attempted write-tool injection.
- **Not connected to CraftNode yet and does not control a live server.** CraftNode must execute tools and verify the actual state.

## Build
Requires JDK 17+ and Gradle 8.10+:

```sh
gradle test
```

## Safety boundary
The core never executes shell or server-console commands, never claims a server action succeeded, and never stores provider credentials. CraftNode remains authoritative for IDs, capabilities, confirmation, execution, and verification. Future providers must use user-configured credentials, not a shared secret embedded in the APK.

## Next milestone
Add a provider adapter behind `MindProvider`, then implement CraftNode's in-process bridge against real server controllers. Do not enable write tools until each action has controller tests and explicit confirmation.
