# JARVIS v3 Android device verification checklist

**Target:** Android 16, Vivo V2318 (subject to actual device settings).

This document is a test PLAN, **not** a claim that device testing has passed. Write PASS / FAIL and note any error.

| # | Test | Edition | Expected |
|---|---|---|---|
| 1 | Install Lite APK fresh | Lite | Installs without policy/security block |
| 2 | Launch app | Lite | Home screen visible without crash |
| 3 | Tap Start Voice | Lite | System voice dialog or embedded voice appears |
| 4 | Deny microphone | Lite | Friendly message, typed input still available |
| 5 | Grant mic and retry | Lite | Speech result returned |
| 6 | Say "what time is it" | Lite | Time appears |
| 7 | Switch Hindi/English | Lite | Selected language persists after restart |
| 8 | Say "open Chrome" | Lite | Chrome opens if installed |
| 9 | Type "open YouTube" | Lite | YouTube opens if installed |
| 10 | Search web for "Android" | Lite | Browser search opens |
| 11 | Navigate to "India Gate" | Lite | Maps intent opens (if handler present) |
| 12 | Dial a safe test number, cancel | Lite | Dialer opens, no call started automatically |
| 13 | Torch on/off | Lite | Only after camera permission; hardware permitting |
| 14 | Media volume up/down | Lite | Media volume changes |
| 15 | Typed command fallback with offline speech | Lite | Does not crash, type succeeds |
| 16 | Ask AI question before entering API key | Lite | Explains key requirement |
| 17 | Enter your OWN valid OpenAI key locally | Lite | Key remains hidden and saved encrypted |
| 18 | Ask AI question with internet | Lite | Answer returned or meaningful quota/network error |
| 19 | Remove API key | Lite | No further remote response without key |
| 20 | Fresh install Full APK | Full | Android prompts, no forced bypass |
| 21 | Before Accessibility permission | Full | Screen commands give guidance |
| 22 | Enable Accessibility manually | Full | Screen status updates to enabled |
| 23 | Open non-sensitive app, say "read screen" | Full | Non-password visible text summarized |
| 24 | Scroll up / scroll down | Full | Screen moves if scrollable |
| 25 | Tap a non-sensitive text-labelled button | Full | Requested item tapped |
| 26 | Type in a plain text field | Full | Text inserted, no passwords |
| 27 | "go back" / "go home" | Full | Accessibility global actions |
| 28 | Try tap "Send" | Full | Confirmation dialog, cancel succeeds |
| 29 | Enable notification access manually | Full | Only safe titles summarized; no OTP |
| 30 | Enable display-over-apps | Full | Floating mic starts from app button |
| 31 | Tap floating mic | Full | Speech / typed input screen opens |
| 32 | Stop floating mic | Full | Bubble removed, notification gone |
| 33 | Android Settings revoke Accessibility | Full | Screen actions fail gracefully |
| 34 | Restart app and deny permissions | Both | No crash, restricted functions show errors |

If a crash occurs: screenshot exact error, Android version, command tried, GitHub run link, and redacted Logcat stack trace. Do NOT share keys, SMS OTPs, bank screenshots, or personal notifications.

**Policy:** Do not disable Play Protect to get around security restrictions. This build is unsigned-for-production (debug signed) and is not a vetted Play Store release.
