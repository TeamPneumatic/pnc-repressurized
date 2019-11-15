package me.desht.pneumaticcraft.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.render.tileentity.AbstractTileModelRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.*;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class RenderDroneHeldItem {
    private AbstractTileModelRenderer.NoBobItemRenderer customRenderItem;
    private final World world;

    public RenderDroneHeldItem(World world) {
        this.world = world;
        if (customRenderItem == null) {
            customRenderItem = new AbstractTileModelRenderer.NoBobItemRenderer();
        }
    }

    public void render(@Nonnull ItemStack droneHeldItem) {
        ItemEntity carriedItem = new ItemEntity(EntityType.ITEM, world);
//        carriedItem.hoverStart = 0.0F;
        carriedItem.setItem(droneHeldItem);

        double scaleFactor = carriedItem.getItem().getItem() instanceof BlockItem ? 0.7F : 0.5F;

        double yOffset = -0.2F;
        if (carriedItem.getItem().getItem() instanceof ToolItem
                || carriedItem.getItem().getItem() instanceof SwordItem || carriedItem.getItem().getItem() instanceof HoeItem) {
            // since items are rendered suspended under the drone,
            // holding tools upside down looks more natural - especially if the drone is digging with them
            GlStateManager.rotated(180, 1, 0, 0);
        }
        GlStateManager.translated(0, yOffset, 0);
        GlStateManager.scaled(scaleFactor, scaleFactor, scaleFactor);
        customRenderItem.doRender(carriedItem, 0, 0, 0, 0, 0);
    }
}
