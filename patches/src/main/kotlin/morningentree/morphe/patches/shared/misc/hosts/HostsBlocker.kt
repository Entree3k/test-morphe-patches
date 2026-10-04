package morningentree.morphe.patches.shared.misc.hosts

import java.io.File
import java.net.IDN
import java.net.URI

/**
 * Parses a hosts/blocklist and answers whether a given host is blocked. Ported from the adobo
 * patches. Supports "hosts file" lines (`0.0.0.0 example.com`), bare domains, and URLs.
 *
 * Allowlist support: a line prefixed with `@@` is an allow rule (AdBlock-style). Allow rules win
 * over block rules, so you can block a broad wildcard (`googleapis.com`) while keeping specific
 * subdomains reachable (`@@tenor.googleapis.com`). No `@@` lines means identical behaviour to the
 * original block-only parser.
 */
class HostsBlocker private constructor(
    private val blocklist: HashSet<String>,
    private val allowlist: HashSet<String>,
) {
    fun isBlocked(
        host: String,
        wildcard: Boolean = true,
    ): Boolean {
        if (host.isBlank()) return false

        val normalizedHost = normalizeDomain(host)
            .let(::extractHost)
            ?.takeIf(::isHostValid)
            ?: return false

        // Allow rules win over block rules.
        if (matches(allowlist, normalizedHost, wildcard)) return false
        return matches(blocklist, normalizedHost, wildcard)
    }

    private fun matches(set: Set<String>, host: String, wildcard: Boolean): Boolean {
        if (set.isEmpty()) return false
        if (set.contains(host)) return true
        if (!wildcard) return false

        var current = host
        while (current.contains(DOT_CHAR)) {
            current = current.substringAfter(DOT_CHAR)
            if (set.contains(current)) return true
        }
        return false
    }

    fun close() {
        blocklist.clear()
        allowlist.clear()
    }

    private fun normalizeDomain(domain: String): String {
        val trimmedDomain = domain.trim().trimEnd('.')
        return runCatching { IDN.toASCII(trimmedDomain) }
            .getOrDefault(domain)
            .lowercase()
    }

    companion object {
        private const val COMMENT_CHAR = '#'
        private const val SPACE_CHAR = ' '
        private const val DOT_CHAR = '.'

        private const val MAX_DOMAIN_LENGTH = 253
        private const val MAX_PARTS = 127
        private const val MIN_PARTS = 2
        private const val MAX_PARTS_LENGTH = 63

        private val RESERVED_HOSTNAMES = setOf(
            "localhost",
            "localhost6",
            "localhost.localdomain",
            "localhost6.localdomain6",
            "local",
            "broadcasthost",
            "127.0.0.1",
            "0.0.0.0",
            "::1",
            "ip6-localhost",
            "ip6-loopback",
            "ip6-localnet",
            "ip6-mcastprefix",
            "ip6-allnodes",
            "ip6-allrouters",
            "ip6-allhosts",
        )

        private const val ALLOW_PREFIX = "@@"

        fun fromString(input: String): HostsBlocker {
            val blocklist = hashSetOf<String>()
            val allowlist = hashSetOf<String>()
            parseLines(input.lineSequence(), blocklist, allowlist)
            return HostsBlocker(blocklist, allowlist)
        }

        fun fromFile(file: File): HostsBlocker {
            val blocklist = hashSetOf<String>()
            val allowlist = hashSetOf<String>()
            file.useLines { lines ->
                parseLines(lines, blocklist, allowlist)
            }
            return HostsBlocker(blocklist, allowlist)
        }

        fun extractHost(input: String): String? {
            val urlWithScheme = if (input.contains("://")) input else "http://$input"
            return runCatching { URI.create(urlWithScheme).host }.getOrNull()
        }

        private fun parseLines(
            lines: Sequence<String>,
            blockOut: MutableSet<String>,
            allowOut: MutableSet<String>,
        ) {
            for (line in lines) {
                var trimmed = line.substringBefore(COMMENT_CHAR).trim()
                if (trimmed.isBlank()) continue

                val out = if (trimmed.startsWith(ALLOW_PREFIX)) {
                    trimmed = trimmed.removePrefix(ALLOW_PREFIX).trim()
                    allowOut
                } else {
                    blockOut
                }

                val host =
                    extractHost(
                        trimmed
                            .substringAfter(SPACE_CHAR)
                            .trim(),
                    ) ?: continue

                if (host in RESERVED_HOSTNAMES) continue

                if (isHostValid(host)) {
                    out.add(host.lowercase())
                }
            }
        }

        private fun isHostValid(input: String): Boolean {
            if (input.isBlank() || input.length > MAX_DOMAIN_LENGTH) return false
            if (input.startsWith(DOT_CHAR) || input.endsWith(DOT_CHAR)) return false

            val parts = input.split(DOT_CHAR)
            if (parts.size !in MIN_PARTS..MAX_PARTS) return false

            for (part in parts) {
                if (part.length > MAX_PARTS_LENGTH) return false
            }
            return true
        }
    }
}
