package me.desht.pneumaticcraft.client.util;

import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.settings.KeyModifier;

public class KeyModifierUtil {
    // modifier bitmask -> KeyModifier object, intentionally not supporting chorded modifiers
    private static final KeyModifier[] MODIFIER_LOOKUP = new KeyModifier[] {
            KeyModifier.NONE,
            KeyModifier.SHIFT, KeyModifier.CONTROL, KeyModifier.NONE, KeyModifier.ALT,
            KeyModifier.NONE
    };

    public static KeyModifier oneModifierAtMost() {
        var mods = KeyModifier.getActiveModifiers();
        return mods.size() == 1 ? mods.getFirst() : KeyModifier.NONE;
    }

    public static KeyModifier oneModifierAtMost(int modifiers) {
        return MODIFIER_LOOKUP[Mth.clamp(modifiers, 0, 5)];
    }
}
