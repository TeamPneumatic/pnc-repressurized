package me.desht.pneumaticcraft.client.render.fluid;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * The glue between FluidItemModel and TankRenderInfo.  Provides the fluids & bounding boxes for the FluidItemModel
 * to add the right quads to the item model.
 */
public interface IFluidItemRenderInfoProvider {
    @Nonnull
    List<TankRenderInfo> getTanksToRender(ItemStack stack);
}
