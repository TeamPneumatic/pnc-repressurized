package me.desht.pneumaticcraft.client.render.fluid;

import net.minecraft.item.ItemStack;

import java.util.List;

public abstract class FluidItemRenderInfoProvider {
    public abstract List<TankRenderInfo> getTanksToRender(ItemStack stack);
}
