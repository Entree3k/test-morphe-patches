package morningentree.morphe.patches.all.detection.signature.pms

import app.morphe.patcher.patch.rawResourcePatch
import app.morphe.patcher.patch.stringOption
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Base64
import java.util.logging.Logger
import java.util.zip.ZipInputStream

/**
 * Base64-encoded DER certificate of the original, unmodified app.
 * Populated by [encodeCertificatePatch] and consumed by [spoofSignatureVerificationPatch].
 */
internal var signature: String? = null
    private set

private val log = Logger.getLogger("SpoofSignatureCertExtractor")

private fun ByteArray.toBase64(): String = Base64.getEncoder().encodeToString(this)

private fun parseFirstX509(stream: InputStream): ByteArray? =
    CertificateFactory.getInstance("X.509")
        .generateCertificates(stream)
        .filterIsInstance<X509Certificate>()
        .firstOrNull()
        ?.encoded

private fun isCertEntry(name: String): Boolean =
    name.startsWith("META-INF/") && name.substringAfterLast('.') in setOf("RSA", "DSA", "EC")

/** v1 (JAR) signature: read the cert from META-INF/*.RSA|DSA|EC inside the APK zip. */
private fun certFromApkBytes(apkBytes: ByteArray): ByteArray? {
    ZipInputStream(ByteArrayInputStream(apkBytes)).use { zis ->
        while (true) {
            val entry = zis.nextEntry ?: break
            if (!entry.isDirectory && isCertEntry(entry.name)) {
                parseFirstX509(zis)?.let { return it }
            }
        }
    }
    return null
}

/**
 * v2/v3 signature: locate the "APK Sig Block 42" and read the signer certificate.
 * Works even when META-INF signature files are absent (v2/v3-only signed apps).
 */
private fun extractFromSigningBlock(apkBytes: ByteArray): ByteArray? {
    val buf = ByteBuffer.wrap(apkBytes).order(ByteOrder.LITTLE_ENDIAN)
    var eocdOffset = -1
    for (i in apkBytes.size - 22 downTo maxOf(0, apkBytes.size - 65557)) {
        if (buf.getInt(i) == 0x06054b50) { eocdOffset = i; break }
    }
    if (eocdOffset < 0) return null
    val cdOffset = buf.getInt(eocdOffset + 16)
    val blockEnd = cdOffset
    if (blockEnd < 32) return null
    val magic = byteArrayOf(
        0x41, 0x50, 0x4b, 0x20, 0x53, 0x69, 0x67, 0x20,
        0x42, 0x6c, 0x6f, 0x63, 0x6b, 0x20, 0x34, 0x32,
    )
    if (!apkBytes.copyOfRange(blockEnd - 16, blockEnd).contentEquals(magic)) return null
    val blockSize = buf.getLong(blockEnd - 24)
    val blockStart = blockEnd - blockSize.toInt() - 8
    if (blockStart < 0) return null
    var pos = blockStart + 8
    val pairsEnd = blockEnd - 24
    while (pos < pairsEnd - 12) {
        val pairLen = buf.getLong(pos).toInt()
        val pairId = buf.getInt(pos + 8)
        val valueStart = pos + 12
        val valueEnd = pos + 8 + pairLen
        // 0x7109871a = APK Signature Scheme v2 Block ID, 0xf05368c0 = v3.
        if (pairId == 0x7109871a || pairId == 0xf05368c0.toInt()) {
            if (valueEnd > valueStart + 28) {
                try {
                    val v = ByteBuffer.wrap(apkBytes, valueStart, valueEnd - valueStart)
                        .order(ByteOrder.LITTLE_ENDIAN)
                    v.int; v.int; v.int
                    val digestsLen = v.int; v.position(v.position() + digestsLen)
                    v.int
                    val certLen = v.int
                    if (certLen > 0 && certLen < apkBytes.size) {
                        val certBytes = ByteArray(certLen); v.get(certBytes)
                        val cert = CertificateFactory.getInstance("X.509")
                            .generateCertificate(ByteArrayInputStream(certBytes)) as? X509Certificate
                        if (cert != null) return cert.encoded
                    }
                } catch (_: Exception) {
                }
            }
        }
        pos = valueStart + pairLen - 4
        if (pairLen <= 4) break
    }
    return null
}

/**
 * Reads a certificate from an APK (or `.apks`/`.xapk` bundle → base.apk) file, preferring the
 * v2/v3 signing block and falling back to v1 META-INF. Returns true if [signature] was set.
 */
private fun extractFromFile(file: File): Boolean {
    val fileBytes = file.readBytes()
    var apkBytes: ByteArray? = null
    ZipInputStream(ByteArrayInputStream(fileBytes)).use { outer ->
        while (true) {
            val entry = outer.nextEntry ?: break
            if (!entry.isDirectory && (entry.name == "base.apk" || entry.name.endsWith("/base.apk"))) {
                apkBytes = outer.readBytes(); break
            }
        }
    }
    val targetBytes = apkBytes ?: fileBytes
    extractFromSigningBlock(targetBytes)?.let { signature = it.toBase64(); return true }
    certFromApkBytes(targetBytes)?.let { signature = it.toBase64(); return true }
    return false
}

val encodeCertificatePatch = rawResourcePatch(
    name = "Provide original app certificate",
    description = "Extracts and Base64-encodes the original app's signing certificate " +
        "(installed app on the device, then the v2/v3 signing block, then v1 META-INF). " +
        "Applied automatically by 'Spoof signature verification'; you normally do not need " +
        "to touch it unless the original app is not installed.",
    default = false,
) {
    val originalApkPath by stringOption(
        key = "originalApkPath",
        default = null,
        title = "Path to original APK (if uninstalled)",
        description = "Only needed if the original, unmodified app is NOT installed on the device " +
            "and auto-extraction fails. Full path to the original APK or .apks/.xapk bundle, " +
            "e.g. /sdcard/Download/app.apk",
        required = false,
    ) { path -> path == null || File(path).let { it.exists() && it.isFile } }

    execute {
        // 1) Explicit APK path provided by the user.
        originalApkPath?.takeIf { it.isNotBlank() }?.let { path ->
            val file = File(path)
            if (file.exists() && extractFromFile(file)) {
                log.info("Cert extracted from provided APK: ${file.name}")
                return@execute
            }
            log.warning("Could not extract a certificate from provided path: $path")
        }

        // 2) The original app currently installed on the device (Morphe Manager runs on-device).
        try {
            // Prefer the package name resolved by packageNamePatch (DOM-parsed); fall back to a
            // regex over the manifest text if that patch has not populated it yet.
            val pkgName = runCatching { appPackageName }.getOrNull()?.takeIf { it.isNotBlank() }
                ?: Regex("""package="([^"]+)"""")
                    .find(get("AndroidManifest.xml").readText())?.groupValues?.get(1)
                ?: throw Exception("package name not found in AndroidManifest.xml")
            val ctx = Class.forName("android.app.ActivityThread")
                .getDeclaredMethod("currentApplication").apply { isAccessible = true }
                .invoke(null) ?: throw Exception("no current application context")
            val pm = ctx.javaClass.getMethod("getPackageManager").invoke(ctx)
                ?: throw Exception("no PackageManager")
            val pkgInfo = pm.javaClass
                .getMethod("getPackageInfo", String::class.java, Int::class.java)
                .invoke(pm, pkgName, 0) ?: throw Exception("no PackageInfo for $pkgName")
            val appInfo = try {
                pkgInfo.javaClass.getMethod("getApplicationInfo").invoke(pkgInfo)
            } catch (_: Exception) {
                pkgInfo.javaClass.getField("applicationInfo").get(pkgInfo)
            } ?: throw Exception("no ApplicationInfo")
            val sourceDir = (
                try {
                    appInfo.javaClass.getMethod("getSourceDir").invoke(appInfo)
                } catch (_: Exception) {
                    appInfo.javaClass.getField("sourceDir").get(appInfo)
                }
                ) as? String ?: throw Exception("no sourceDir")
            if (extractFromFile(File(sourceDir))) {
                log.info("Cert extracted from installed app: $sourceDir")
                return@execute
            }
        } catch (e: Exception) {
            log.info("Installed-app cert strategy unavailable: ${e.message}")
        }

        // 3) Fallback: cert files left in the decoded resources' META-INF folder.
        val metaInf = get("META-INF")
        val certFile = metaInf.listFiles()?.firstOrNull { it.isFile && isCertEntry("META-INF/${it.name}") }
        if (certFile != null) {
            certFile.inputStream().use { parseFirstX509(it) }?.let {
                signature = it.toBase64()
                log.info("Cert extracted from decoded META-INF/${certFile.name}")
                return@execute
            }
        }

        log.warning(
            "No signing certificate could be extracted automatically. Keep the original app " +
                "installed, set 'Path to original APK', or paste the Base64 signature in the " +
                "Spoof signature verification patch options.",
        )
    }
}
