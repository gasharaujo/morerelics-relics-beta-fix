package dev.codex.morerelicscompat;

import dev.codex.morerelicscompat.legacy.data.cast.misc.CastStage;
import dev.codex.morerelicscompat.legacy.data.cast.misc.CastType;
import dev.codex.morerelicscompat.mixin.MoreRelicBaseCompatMixin;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.abilities.activation.AbilityActivationContext;
import it.hurts.sskirillss.relics.api.relics.abilities.activation.AbilityActivationStage;
import it.hurts.sskirillss.relics.api.relics.data.AbilityData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Exercises the actual compatibility entry point against the installed Relics API. */
public final class AbilityActivationVerifier {
    public static void main(String[] args) {
        RecordingRelic relic = new RecordingRelic();
        IRelicItem currentApi = relic;
        int verified = 0;
        for (String ability : new String[] {"epitaph", "bind", "soar", "unravel"}) {
            for (CastType type : CastType.values()) {
                for (AbilityActivationStage stage : AbilityActivationStage.values()) {
                    // This test only needs the ID; no world or registered item is needed.
                    AbilityActivationContext context = new AbilityActivationContext(
                        null, null, new AbilityData(null, ability), type.toCurrent(), stage
                    );
                    int before = relic.calls;
                    currentApi.activateAbility(context);
                    if (relic.calls != before + 1 || !ability.equals(relic.ability)
                        || relic.type != type || !stage.name().equals(relic.stage.name())
                        || relic.stack != context.stack() || relic.player != context.player()) {
                        throw new AssertionError("Activation was not forwarded exactly once: "
                            + ability + " / " + type + " / " + stage);
                    }
                    verified++;
                }
            }
        }
        // Passive items inherit the legacy no-op rather than throwing a missing-method error.
        new PassiveRelic().activateAbility(new AbilityActivationContext(
            null, null, new AbilityData(null, "passive"), CastType.INSTANTANEOUS.toCurrent(),
            AbilityActivationStage.START
        ));
        System.out.println("Verified " + verified + " activation dispatches and passive fallback.");
    }

    private static class PassiveRelic extends MoreRelicBaseCompatMixin implements IRelicItem {
        @Override
        @SuppressWarnings("removal")
        public String getConfigRoute() {
            return "test";
        }
    }

    private static final class RecordingRelic extends PassiveRelic {
        private int calls;
        private ItemStack stack;
        private Player player;
        private String ability;
        private CastType type;
        private CastStage stage;

        public void castActiveAbility(ItemStack stack, Player player, String ability,
                                      CastType type, CastStage stage) {
            calls++;
            this.stack = stack;
            this.player = player;
            this.ability = ability;
            this.type = type;
            this.stage = stage;
        }
    }
}
