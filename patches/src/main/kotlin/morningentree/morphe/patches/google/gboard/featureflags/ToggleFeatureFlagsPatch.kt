package morningentree.morphe.patches.google.gboard.featureflags

import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.stringsOption
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import morningentree.morphe.patches.google.gboard.detection.signature.bypassSignaturePatch
import morningentree.morphe.patches.google.gboard.shared.COMPATIBILITY_GBOARD
import java.util.logging.Logger

private val CURATED_FLAGS: Map<String, List<String>> = mapOf(
    "Email suggestions (from device accounts)" to listOf("enable_email_provider_completion"),
    "Android Autofill in keyboard" to listOf("enable_autofill_ime_integration"),
    "Number row" to listOf("enable_number_row"),
    "Fast access bar (symbols row)" to listOf("enable_fast_access_bar"),
    "Grammar checker" to listOf("enable_grammar_checker"),
    "Multilingual typing" to listOf("enable_multilingual_typing"),
    "Settings search" to listOf("enable_settings_search"),
    "AI writing tools" to listOf("enable_writing_tools_v2"),
    "Emojify (text to emoji)" to listOf("enable_emojify"),
    "Semantic emoji search" to listOf("enable_semantic_emoji"),
    "Proactive Emoji Kitchen" to listOf("enable_proactive_emoji_kitchen"),
    "Expression moment stickers" to listOf("enable_expression_moment"),
    "Sticker predictions while typing" to listOf("enable_sticker_predictions_while_typing"),
    "Dynamic art stickers" to listOf("enable_dynamic_art"),
    "Trending GIFs" to listOf("enable_tenor_trending_gifs"),
    "Text conversion (CJK)" to listOf("enable_text_conversion"),
    "Split keyboard (large tablet)" to listOf("enable_split_keyboard_on_tablet_large"),
)

@Suppress("unused")
val toggleFeatureFlagsPatch = bytecodePatch(
    name = "Toggle feature flags",
    description = "Turn Gboard feature flags on or off. Tap features from the curated lists, or " +
        "enter any flag name yourself. Unknown or already-default flags are skipped safely.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(bypassSignaturePatch)

    val logger = Logger.getLogger(this::class.java.name)

    val enableFeatures by stringsOption(
        key = "gboardEnableFeatures",
        default = emptyList(),
        values = CURATED_FLAGS,
        title = "Enable features",
        description = "Tap the features you want turned ON.",
        required = false,
    )

    val disableFeatures by stringsOption(
        key = "gboardDisableFeatures",
        default = emptyList(),
        values = CURATED_FLAGS,
        title = "Disable features",
        description = "Tap the features you want turned OFF.",
        required = false,
    )

    val customFlags by stringsOption(
        key = "gboardCustomFlags",
        default = emptyList(),
        title = "Custom flags (advanced)",
        description = "Enter any Gboard flag name(s) not in the lists above. Use the toggle below " +
            "to choose whether these are turned on or off.",
        required = false,
    ) { flags ->
        val flagsRegex = """^[A-Za-z0-9_-]+$""".toRegex()
        flags.isNullOrEmpty() || flags.all { it.matches(flagsRegex) }
    }

    val enableCustomFlags by booleanOption(
        key = "gboardEnableCustomFlags",
        default = true,
        title = "Turn custom flags ON",
        description = "When enabled, the custom flags above are turned on; when disabled, off.",
    )

    execute {
        fun toggle(flag: String, enable: Boolean) {
            val trimmed = flag.trim()
            if (trimmed.isEmpty()) return
            val fingerprint = featureFlagFingerprint(trimmed)
            runCatching {
                fingerprint.method.apply {
                    val index = fingerprint.instructionMatches.last().index
                    val register = getInstruction<OneRegisterInstruction>(index).registerA
                    replaceInstruction(
                        index = index,
                        smaliInstruction = "const/4 v$register, ${if (enable) "0x1" else "0x0"}",
                    )
                }
            }.onSuccess {
                logger.info("[Found] \"$trimmed\" toggled ${if (enable) "on" else "off"}.")
            }.onFailure {
                logger.info("[Skipped] \"$trimmed\" not found. No changes applied.")
            }
        }

        enableFeatures.orEmpty().forEach { toggle(it, enable = true) }
        disableFeatures.orEmpty().forEach { toggle(it, enable = false) }
        customFlags.orEmpty().forEach { toggle(it, enable = enableCustomFlags != false) }
    }
}
