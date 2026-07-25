package morningentree.morphe.patches.lifesum.premium

import app.morphe.patcher.Fingerprint

// Lifesum (com.sillens.shapeupclub) is a native app and — unlike the obfuscated
// l/* helper classes — its network model and DB-extension facade keep real
// (unobfuscated) names. Premium ("Gold") is a single Boolean that originates from
// the backend user profile and fans out to ~30 read sites via the
// ProfileModel.premium wrapper (obfuscated class Ll/yrc;, field a). We anchor on
// the two stable, unobfuscated names rather than those drifting l/* mappers.

// Source of truth: the deserialized backend "premium" flag on the network model.
// It is called exactly once (by the network -> ProfileModel mapper), so forcing it
// to true makes every downstream reader see premium once the profile syncs.
internal object ApiUserProfileGetPremiumFingerprint : Fingerprint(
    custom = { method, classDef ->
        classDef.type == "Lcom/lifesum/profile/network/models/ApiUserProfile;" &&
                method.name == "getPremium"
    }
)

// Semantic gate used directly by ~10 feature checks (getPremium().a.booleanValue()).
// returnType Z, so it can be forced with returnEarly(true) for immediate effect
// (covers cold start before the first profile sync completes).
internal object HasPremiumFingerprint : Fingerprint(
    custom = { method, classDef ->
        classDef.type == "Lcom/sillens/shapeupclub/db/models/ProfileModelExtensionsKt;" &&
                method.name == "hasPremium"
    }
)
