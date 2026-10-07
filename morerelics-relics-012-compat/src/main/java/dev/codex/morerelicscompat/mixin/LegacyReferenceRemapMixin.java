package dev.codex.morerelicscompat.mixin;

import org.spongepowered.asm.mixin.Mixin;

/**
 * Empty marker mixin. LegacyReferenceRemapPlugin rewrites the obsolete Relics
 * type references on each target immediately before this mixin is applied.
 */
@Mixin(
    targets = {
        "com.blorb.morerelics.MoreRelicsUtil",
        "com.blorb.morerelics.relics.AxolotlCream",
        "com.blorb.morerelics.relics.BioJoint",
        "com.blorb.morerelics.relics.BionicEye",
        "com.blorb.morerelics.relics.ConvergingOrb",
        "com.blorb.morerelics.relics.CrownOfTheLegend",
        "com.blorb.morerelics.relics.CyberwareBase",
        "com.blorb.morerelics.relics.DepletedSpool",
        "com.blorb.morerelics.relics.EjectButton",
        "com.blorb.morerelics.relics.EpochApple",
        "com.blorb.morerelics.relics.GravitumGlove",
        "com.blorb.morerelics.relics.GravitumStrider",
        "com.blorb.morerelics.relics.GutsOrb",
        "com.blorb.morerelics.relics.KingCrimson",
        "com.blorb.morerelics.relics.MadeInHeaven",
        "com.blorb.morerelics.relics.MassGauntlet",
        "com.blorb.morerelics.relics.MoodWorm",
        "com.blorb.morerelics.relics.OpalNecklace",
        "com.blorb.morerelics.relics.RunicPlate",
        "com.blorb.morerelics.relics.SentientRust",
        "com.blorb.morerelics.relics.ShieldWeaveCape",
        "com.blorb.morerelics.relics.SlumberingAmulet",
        "com.blorb.morerelics.relics.Swiftedge",
        "com.blorb.morerelics.relics.ThermoseismicHeart",
        "com.blorb.morerelics.relics.TwinFangs",
        "com.blorb.morerelics.relics.TyrantMask",
        "com.blorb.morerelics.relics.VertebraX",
        "com.blorb.morerelics.relics.WeaversSpool",
        "com.blorb.morerelics.relics.WhimsOfFate",
        "com.blorb.morerelics.relics.WhisperingAmulet",
        "com.blorb.morerelics.relics.WonderOfU"
    },
    remap = false
)
public abstract class LegacyReferenceRemapMixin {
}

