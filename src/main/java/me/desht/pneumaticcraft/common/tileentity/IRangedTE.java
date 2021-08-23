package me.desht.pneumaticcraft.common.tileentity;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

/**
 * Represents a tile entity which has a range manager and the concept of a range area of effect.
 */
@FunctionalInterface
public interface IRangedTE {
    RangeManager getRangeManager();

    /**
     * Text to be displayed on the range toggle GUI button
     * @return a text component
     */
    default ITextComponent rangeText() {
        return new StringTextComponent("R").withStyle(getRangeManager().shouldShowRange() ? TextFormatting.AQUA : TextFormatting.GRAY);
    }

    default int getRange() {
        return getRangeManager().getRange();
    }
}
