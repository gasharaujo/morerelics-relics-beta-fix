# MoreRelics New Relics Fix

Local NeoForge 1.21.1 compatibility mod for:

- More Relics `1.7.7` (legacy bridge introduced for `1.7.6`)
- Relics `0.12.8`
- NeoForge `21.1.248`

More Relics was compiled against the pre-0.12 Relics API. Relics 0.12 moved and
replaced `RelicData`, its builders, activation data, leveling data, and several
helper methods. That causes `NoClassDefFoundError` while NeoForge scans automatic
event subscribers.

This mod applies an early Mixin bytecode remap to More Relics classes, supplies a
small legacy builder facade, converts legacy definitions into current
`RelicTemplate` instances, and bridges runtime ability/cooldown/experience calls.
It also generates language aliases from More Relics' old `tooltip.relics.*`
keys to the `relics.description.*` keys used by the Relics 0.12 interface.

Version `1.0.2` restores active abilities, including Crimson Mask's Epitaph
(`morerelics:king_crimson`) and Weaver's Spool's Bind, Soar and Unravel.
The previous bridge translated activation templates but did not implement
`IRelicItem.activateAbility(AbilityActivationContext)`, so Relics called its
empty default method instead of More Relics' `castActiveAbility` overrides.
The bridge now forwards the stack, player, ability ID, activation type and stage
to those original overrides. Existing unlock rules, silk costs and cooldowns
remain enforced by Relics and More Relics.

Build and verify with PowerShell 7 using the local Java 21 installation and
dependencies from CurseForge instance `21`:

```powershell
.\build.ps1
.\verify.ps1
```

The verifier exercises the current Relics API against the bridge, checks all
36 ability/type/stage combinations plus the passive fallback, validates the
remapped callbacks in the actual More Relics JAR, and checks API references and
translation aliases. It does not run Minecraft or simulate the item effects.

Build, verify and install in `C:\Users\pichau\curseforge\minecraft\Instances\21`:

```powershell
.\build.ps1 -Install
```

The JAR is saved to `build/libs` and `MeusMods/builds`. Installation backs up
previous compatibility JARs under `build/installed-backups` before replacing
them. The existing mod ID is preserved. Restart the instance to load the new JAR.

In-game validation after restarting:

1. Equip Crimson Mask, move around for at least 10 seconds, then activate Epitaph.
   Confirm the rewind and its cooldown.
2. Equip Weaver's Spool and unlock the abilities being tested. Accumulate silk
   by attacking enemies: Bind needs 6, Soar needs 3, and Unravel needs 7.
3. Activate each ability and check its effect, silk consumption and cooldown.
   Confirm activation is rejected when there is insufficient silk.
