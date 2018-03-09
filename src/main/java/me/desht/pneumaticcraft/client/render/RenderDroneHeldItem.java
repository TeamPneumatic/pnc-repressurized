package me.desht.pneumaticcraft.client.render;

import me.desht.pneumaticcraft.client.render.tileentity.AbstractModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.*;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class RenderDroneHeldItem {
    private AbstractModelRenderer.NoBobItemRenderer customRenderItem;
    private final World world;

    public RenderDroneHeldItem(World world) {
        this.world = world;
        if (customRenderItem == null) {
            customRenderItem = new AbstractModelRenderer.NoBobItemRenderer();
        }
    }

    public void render(@Nonnull ItemStack droneHeldItem) {
        EntityItem carriedItem = new EntityItem(world);
        carriedItem.hoverStart = 0.0F;
        carriedItem.setItem(droneHeldItem);

        double scaleFactor = carriedItem.getItem().getItem() instanceof ItemBlock ? 0.7F : 0.5F;

        double yOffset = -0.2F;
        if (carriedItem.getItem().getItem() instanceof ItemTool
                || carriedItem.getItem().getItem() instanceof ItemSword || carriedItem.getItem().getItem() instanceof ItemHoe) {
            // since items are rendered suspended under the drone,
            // holding tools upside down looks more natural - especially if the drone is digging with them
            GlStateManager.rotate(180, 1, 0, 0);
        }
        GlStateManager.translate(0, yOffset, 0);
        GlStateManager.scale(scaleFactor, scaleFactor, scaleFactor);
        customRenderItem.doRender(carriedItem, 0, 0, 0, 0, 0);
    }
}
