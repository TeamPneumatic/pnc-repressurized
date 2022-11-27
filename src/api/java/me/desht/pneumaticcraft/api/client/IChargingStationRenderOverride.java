package me.desht.pneumaticcraft.api.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * You can register an implementation of this via {@link IClientRegistry#registerRenderOverride(ItemLike, IChargingStationRenderOverride)}
 * to provide custom rendering of items in the Charging Station.
 */
@FunctionalInterface
public interface IChargingStationRenderOverride {
    /**
     * Called when the item is about to be rendered. You can modify the pose stack here (no need to push or pop it -
     * that is handled by the caller) and return true to proceed with the default rendering (which renders the item
     * using a FIXED transform type, like an item frame does), or you can do your own rendering and cancel the default
     * rendering by returning false.
     *
     * @param poseStack the pose stack
     * @param renderedStack the item to be rendered
     * @param partialTicks partial ticks
     * @param bufferIn the buffer source
     * @param combinedLightIn light level
     * @param combinedOverlayIn overlay
     * @return true to use the standard item renderer for charging stations, false to cancel default rendering
     */
    boolean onRender(PoseStack poseStack, ItemStack renderedStack, float partialTicks, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn);
}
