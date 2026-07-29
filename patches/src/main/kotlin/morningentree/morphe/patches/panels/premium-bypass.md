# Panels — Premium Bypass

- Package: com.fossor.panels
- Version: 1.596 (versionCode 1596)
- Billing SDK: Google Play Billing (obfuscated to `Lrs;`), Google TrivialDrive-style `BillingDataSource`

## Target 1: Full-version gate (`fullVersion` SharedPreferences boolean)

- Class: `Lw92;` (R8-merged utility/settings class; instance obtained via `w92.l(context)`)
- Method: `public o()Z`
- DEX: classes (smali/)
- Purpose: THE app-wide premium gate. Reads `getBoolean("fullVersion", false)` from the
  `Lzi1;` SharedPreferences wrapper and returns it. The billing flow writes this pref
  (`putBoolean("fullVersion", ...)`) in `BillingDataSource.setSkuStateFromPurchase` once the
  `"full_version"` SKU is purchased. Called from 13 sites across the app
  (PanelsActivity, SettingsActivity, s1 observer, BillingDataSource, ed, fs4, lv0, mq0,
  qv2, tv2, tw2, fw, p60).
- Smali verified: YES
- Patch approach: force `return true` at method entry → every premium feature check passes.

### Fingerprint Strategy
```kotlin
Fingerprint(
    returnType = "Z",
    parameters = listOf(),
    filters = listOf(
        string("fullVersion"),        // stable, developer string
        methodCall(name = "getBoolean"),
    )
)
```
`"fullVersion"` appears in exactly two methods: this reader (`getBoolean`, returns Z, no
params) and `BillingDataSource.setSkuStateFromPurchase(Ly73;)V` (a `putBoolean` writer,
returns V, has a param). The `returnType = "Z"` + empty `parameters` + `getBoolean` filter
uniquely resolve to `w92.o()`.

### Smali Evidence
```smali
.method public o()Z
    .locals 2
    iget-object p0, p0, Lw92;->c:Ljava/lang/Object;
    check-cast p0, Lzi1;
    const-string v0, "fullVersion"
    const/4 v1, 0x0
    invoke-virtual {p0, v0, v1}, Lzi1;->getBoolean(Ljava/lang/String;Z)Z
    move-result p0
    return p0
.end method
```

## Notes on alternative targets (not used)

- `BillingDataSource.isPurchased(String)Lu61;` returns a per-SKU `Boolean` Flow. Forcing it
  true would also mark the consumable donation SKUs ("pizza", "coffee", ...) as purchased,
  risking spurious consume attempts. The `w92.o()` gate is the cleaner, single-point target.
- SKU for premium is `"full_version"` (observed in SettingsActivity `bc2.f("full_version")`).
