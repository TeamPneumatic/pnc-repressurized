package me.desht.pneumaticcraft.common.thirdparty.jei;

import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PositionedStack {
    private final List<ItemStack> stacks;
    private final int x, y;
    private String tooltipKey;

    private PositionedStack(ItemStack stack, int x, int y) {
        this(Collections.singletonList(stack), x, y);
    }

    private PositionedStack(List<ItemStack> stacks, int x, int y) {
        this.stacks = stacks;
        this.x = x;
        this.y = y;
    }

    public static PositionedStack of(ItemStack stack, int x, int y) {
        return new PositionedStack(stack, x, y);
    }

    public static PositionedStack of(List<ItemStack> stacks, int x, int y) {
        return new PositionedStack(stacks, x, y);
    }

    public static PositionedStack of(ItemStack[] stacks, int x, int y) {
        return new PositionedStack(Arrays.asList(stacks), x, y);
    }

    public PositionedStack setTooltipKey(String tooltipKey) {
        this.tooltipKey = tooltipKey;
        return this;
    }

    public String getTooltipKey() {
        return tooltipKey;
    }

    public List<ItemStack> getStacks() {
        return stacks;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}

