# JARVIS Mobile v3 — native Android rebuild (beta)

**Status:** Beta, not security audited or tested on a physical Vivo device. Android compilation and GitHub CI results must be reviewed before installation. This is original new source, not an amalgamation of third-party code.

## Two Android flavors
- **Lite**: native Java UI; Hindi / English push-to-talk using device Android speech UI, fallback to embedded Android recognizer and typed command; app launching; Maps / web search; dialer; time; torch; volume; optional AI Q&A. Requires microphone, internet for online AI, and camera permission for torch. No accessibility or notification listener in the Lite manifest.
- **Full**: Lite plus user-controlled Accessibility screen reading, back/home, tap-by-text, type into non-password fields, scroll; notification summary; optional manually started floating microphone. Enabling sensitive services is always manual in Android settings. Automation is best effort and may be blocked by protected apps.

**Not implemented**: always-on offline wake word, local multi-GB Gemma model, Wear OS client, OpenClaw gateway, general LLM tool-use agent, arbitrary Gmail/WhatsApp message sending, unattended schedules, screen screenshot analysis, zero-permission phone control. Those require additional integrations, security reviews, and real device testing. The LLM Q&A does not trigger phone actions.

## Build
JDK 17, Android SDK API 35, Android Gradle Plugin 8.9.2, Gradle 8.11.1:
```sh
gradle :app:assembleLiteDebug :app:assembleFullDebug
```
From this folder (jarvis-v3). Or use the **JARVIS v3 Native Android - Tests and APK** workflow at the GitHub Actions tab on branch `jarvis-v3-rebuild`. Download the two artifacts after CI passes.

## Security and privacy
- NO API key is present in the repository.
- Optional OpenAI API key is encrypted using AndroidKeyStore AES-GCM, stored only on device. Sent to official HTTPS API only for AI questions.
- All phone controls are local. No root, no ADB, no bypass of Play Protect or restricted settings.
- Mic is user-initiated, no always-on listening. Bubble must be manually enabled and stopped.
- Calls only open dialer; the user initiates the call.
- Banking/password fields are excluded from screen reading and typing, but OS accessibility data is powerful: enable only after inspecting code.
- Tap commands with high-impact action labels ask for explicit confirmation.
- No third-party code has been copied from referenced projects.

## Prior project references (architecture inspiration, *not imported source*)
- Deft — https://github.com/bedda-tech/deft (MIT)
- OpenClaw Assistant — https://github.com/yuga-hashimoto/openclaw-assistant (MIT)
- MobileClaw — https://github.com/ChenKuanSun/MobileClaw (MIT)
- DroidRun/Mobilerun — https://github.com/appwiz/droidrun (MIT)
- X-PLUG MobileAgent — https://github.com/X-PLUG/MobileAgent (MIT)
- Official OpenClaw — https://github.com/openclaw/openclaw (MIT)
- Dicio — https://github.com/DicioTeam/dicio-android (GPLv3; design-level reference only)
- Sanna — https://github.com/sannabotdev/sannabotapp (check upstream terms for any future reuse)

Start with Lite on device. See DEVICE_TEST_CHECKLIST.md.
