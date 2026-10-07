# Morphe Patches template

Template repository for Morphe Patches.

## About

Patches for apps I like.

## 🩹 Patches list

<!-- PATCHES_START EXPANDED -->
> **[v1.82.0](https://github.com/Entree3k/test-morphe-patches/releases/tag/v1.82.0)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;38 patches total
<details open>
<summary>📦 Gboard&nbsp;&nbsp;•&nbsp;&nbsp;7 patches</summary>
<br>

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Always incognito mode](#always-incognito-mode) | Always opens Gboard in incognito mode to disable typing-history collection and personalization, while keeping clipboard and voice typing working in incognito. |  |
| [Block tracking and analytics](#block-tracking-and-analytics) | Redirects Gboard's known tracking, analytics, and ad host to 0.0.0.0. Note: telemetry sent through Google Play Services is not affected; use the always-incognito patch for that. | • Wildcard blocking |
| [Change package name](#change-package-name) | Installs Gboard as a clone by appending ".clone" to the package name (configurable), so it installs next to the stock Gboard. Changing an app's package name can lead to unexpected issues. | • Package name<br>• Update permissions<br>• Update other permissions<br>• Update content providers |
| [Disable telemetry & federated learning](#disable-telemetry-federated-learning) | Forces Gboard's "Improve Gboard" / usage-statistics and federated-learning flags off at startup, regardless of the in-app settings, so your typing is not used for training or metrics. Best combined with the "Network privacy" patch. |  |
| [Network privacy](#network-privacy) | Controls what Gboard is allowed to talk to. Choose a network level: block core telemetry, block ads & telemetry more aggressively (all typing features still work), or block all internet access. | • Network level<br>• Wildcard blocking |
| [Remove promotional banners](#remove-promotional-banners) | Hides Gboard's in-keyboard promotional / "try this feature" banners by forcing their promo flags off. Does not disable the underlying features, only their nag banners. |  |
| [Toggle feature flags](#toggle-feature-flags) | Turn Gboard features on with individual switches. Each switch maps to a Gboard feature flag; flip it on to enable that feature. Unknown or already-default flags are skipped safely. | • Email suggestions (from device accounts)<br>• Android Autofill in keyboard<br>• Number row<br>• Fast access bar (symbols row)<br>• Grammar checker<br>• Multilingual typing<br>• Settings search<br>• AI writing tools<br>• Emojify (text to emoji)<br>• Semantic emoji search<br>• Proactive Emoji Kitchen<br>• Expression moment stickers<br>• Sticker predictions while typing<br>• Dynamic art stickers<br>• Trending GIFs<br>• Text conversion (CJK)<br>• Split keyboard (large tablet)<br>• Custom flags to enable (advanced)<br>• Custom flags to disable (advanced) |

</details>

<details open>
<summary>📦 DynamicSpot&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**🎯 Supported versions:**

| 2.01 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Enable Premium](#enable-premium) | Unlocks DynamicSpot premium |  |

</details>

<details open>
<summary>📦 FlowStack&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**🎯 Supported versions:**

| 2.0.5 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Enable Premium](#enable-premium) | Unlocks FlowStack premium by forcing RevenueCat to report an active entitlement. |  |

</details>

<details open>
<summary>📦 Hobi&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**🎯 Supported versions:**

| 3.4.0 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Enable Premium](#enable-premium) | Unlocks Hobi Pro. |  |

</details>

<details open>
<summary>📦 Ling&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**🎯 Supported versions:**

| 8.9.0 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Enable Premium](#enable-premium) | Unlocks Ling Pro |  |

</details>

<details open>
<summary>📦 Momentum&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**🎯 Supported versions:**

| 1.8.3-play |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Enable Premium](#enable-premium) | Unlocks Momentum Plus by forcing the RevenueCat "plus" entitlement check to true. |  |

</details>

<details open>
<summary>📦 NotiGuy&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**🎯 Supported versions:**

| 2.7.6 | 2.7.8 |
| :---: | :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Enable Premium](#enable-premium) | Unlocks NotiGuy premium. Use with Spoof Install Source |  |

</details>

<details open>
<summary>📦 Prompter Pal&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**🎯 Supported versions:**

| 7.0.1 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Enable Premium](#enable-premium) | Unlocks Prompter Pal Premium |  |

</details>

<details open>
<summary>📦 Super Status Bar&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**🎯 Supported versions:**

| 2.13.0 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Enable Premium](#enable-premium) | Unlocks Super Status Bar Premium. Use with Spoof Install Source |  |

</details>

<details open>
<summary>📦 Tide&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**🎯 Supported versions:**

| 5.6.2 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Enable Premium](#enable-premium) | Unlocks Tide VIP membership on the client. |  |

</details>

<details open>
<summary>📦 Gradient Weather&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**🎯 Supported versions:**

| 1.2.0 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Enable Premium (dev)](#enable-premium-dev) | Unlocks Gradient Weather Premium. Use With Spoof Install Source |  |

</details>

<details open>
<summary>📦 Fylo — File Manager&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**🎯 Supported versions:**

| 2.1 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Enable Pro](#enable-pro) | Unlocks Fylo File Manager Pro |  |

</details>

<details open>
<summary>📦 Send Files To TV&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**🎯 Supported versions:**

| 1.4.22 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Remove Ads](#remove-ads) | Removes ads |  |

</details>

<details open>
<summary>📦 OldRoll&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**🎯 Supported versions:**

| 6.5.2 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock All Cameras](#unlock-all-cameras) | Unlocks every OldRoll camera and lifetime Pro, spoofs the app's signature/license verdict to "genuine", and disables the modified-app (anti-piracy) popup that otherwise closes the re-signed build on launch. |  |

</details>

<details open>
<summary>🌐 Universal&nbsp;&nbsp;•&nbsp;&nbsp;18 patches</summary>
<br>

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Block tracking hosts](#block-tracking-hosts) | Redirects known tracking, analytics, and ad host literals to 0.0.0.0 using a built-in blocklist, optionally extended with your own hosts file. Only affects hosts that appear as literal strings in the app; telemetry sent through Google Play Services is not affected. | • Wildcard blocking<br>• Additional hosts file |
| [Bypass battery optimization nag](#bypass-battery-optimization-nag) | Makes apps believe they are already exempt from battery optimization so they stop prompting you to disable it. Only affects what the app sees, not the real system setting. |  |
| [Change package name](#change-package-name) | Renames the app (default: append ".entree") so it installs as a clone next to the original. The name is configurable. Changing a package name can lead to unexpected issues. | • Package name<br>• Update permissions<br>• Update other permissions<br>• Update content providers |
| [Disable Pairip protection](#disable-pairip-protection) | Neutralizes Pairip's client-side signature and license checks (including the LicenseActivity paywall/close-app enforcement) so the re-signed APK launches and stays open. Optionally guts the Pairip VM as well. Does not bypass server-side Play Integrity. | • Gut Pairip VM |
| [Disable analytics & ad tracking](#disable-analytics-ad-tracking) | Turns off Firebase / Google Analytics collection and removes the advertising-ID and ad-services permissions. Blocks ad personalization and analytics opt-in without cutting off the app's legitimate network features. Pair with a host blocker to also stop the traffic. |  |
| [Disable clipboard access](#disable-clipboard-access) | Blocks apps from clipboard access |  |
| [GmsCore support (MicroG)](#gmscore-support-microg) | Routes Google Play Services calls through MicroG instead of real GPS.<br><br>Works for: Google apps (YouTube, Maps, News, Photos) and third-party apps using classic Google Sign-In (Android 13 and below).<br><br>Does not work for: Android 14+ Credential Manager sign-in (most modern third-party apps), Play Integrity / SafetyNet checks, or apps with custom auth.<br><br>Requires MicroG RE installed. Apply with the original app certificate patch. | • MicroG package name<br>• Main activity class (optional)<br>• Custom package name (optional)<br>• Spoofed signing certificate SHA-256 (optional) |
| [Provide original app certificate](#provide-original-app-certificate) | Extracts and Base64-encodes the original app's signing certificate. Applied automatically by 'Spoof signature verification'; you normally do not need to touch it. Use 'Certificate source' to control where the certificate comes from. | • Certificate source<br>• Original APK file |
| [Remove internet permission](#remove-internet-permission) | Removes the INTERNET permission so the app cannot access the network at all |  |
| [Reset trial period](#reset-trial-period) | Makes apps see themselves as freshly installed (spoofs the install/update time to the current time) so time-limited trials that count days since install never expire. Does not affect trials validated on a server or stored in the app's own saved timestamp. |  |
| [Spoof SIM provider](#spoof-sim-provider) | Spoofs TelephonyManager SIM/network provider values. | • Country ISO<br>• Operator code<br>• Operator name |
| [Spoof Wi-Fi connection](#spoof-wi-fi-connection) | Spoof Wi-Fi connection |  |
| [Spoof Wi-Fi identifiers](#spoof-wi-fi-identifiers) | Spoofs Wi-Fi SSID, BSSID, and MAC address reads. | • SSID<br>• BSSID<br>• MAC address |
| [Spoof install source](#spoof-install-source) | Makes the app think it was installed from a specific store (default: Google Play) | • Store to impersonate |
| [Spoof signature verification](#spoof-signature-verification) | Spoofs the signature verification | • Package name<br>• Base64-encoded signature |
| [Spoof telephony IDs](#spoof-telephony-ids) | Spoofs IMEI, MEID, subscriber ID, SIM serial, and line number reads. | • IMEI<br>• MEID<br>• Subscriber ID<br>• SIM serial<br>• Line number |
| [Unlock RevenueCat](#unlock-revenuecat) | Premium patch for apps that use RevenueCat |  |
| [Unlock encrypted Pro flag](#unlock-encrypted-pro-flag) | (TESTING) Unlocks apps that gate Pro behind an AES-decrypted "yes" flag emitted through a Kotlin Flow. No-ops on apps that don't use this scheme. Probably Won't Work Most Apps |  |

</details>

<!-- PATCHES_END -->

#### How to use these patches

Click here to add these patches to Morphe: https://morphe.software/add-source?github=Entree3k/Morning-Entree-Patches

Or manually add this repository url as a patch source in Morphe: https://github.com/Entree3k/Morning-Entree-Patches

### 🛠️ Building

To build UserXYZ Patches,
you can follow the [Morphe documentation](https://github.com/MorpheApp/morphe-documentation).

### Disclaimer

This is for educational purposes, bypassing features of apps is against terms and services

## 📜 License

Morning Entreee Patches are licensed under the [GNU General Public License v3.0](LICENSE)
