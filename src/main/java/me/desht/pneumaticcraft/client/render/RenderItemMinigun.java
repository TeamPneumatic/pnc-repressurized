package me.desht.pneumaticcraft.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.model.entity.ModelDroneMinigun;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ItemMinigun;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;

public class RenderItemMinigun extends ItemStackTileEntityRenderer {
    private final ModelDroneMinigun model = new ModelDroneMinigun();

    @Override
    public void renderByItem(ItemStack stack) {
        if (stack.getItem() == ModItems.MINIGUN && stack.hasTag()) {
            Minecraft mc = Minecraft.getInstance();
            PlayerEntity player = mc.player;
            int id = stack.getTag().getInt("owningPlayerId");
            Entity owningPlayer = Minecraft.getInstance().world.getEntityByID(id);
            if (owningPlayer instanceof PlayerEntity) {
                Minigun minigun = ((ItemMinigun) stack.getItem()).getMinigun(stack, (PlayerEntity) owningPlayer);
                GlStateManager.pushMatrix();
                if (mc.gameSettings.thirdPersonView != 0 || player.getEntityId() != owningPlayer.getEntityId()) {
                    // rendering our own gun in 3rd person, or rendering someone else's gun
                    GlStateManager.scaled(1, -1, 1);
                    GlStateManager.rotated(-90, 1, 0, 0);
                    GlStateManager.translated(0.5, -2, -0.3);
                } else if (mc.currentScreen instanceof InventoryScreen) {
                    // our own gun in the rendered player model in inventory screen
                    GlStateManager.rotated(90, 1, 0, 0);
                    GlStateManager.translated(0.5, -1, -0.5);
                } else {
                    // our own gun in 1st person
                    GlStateManager.scaled(1.5, 1.5, 1.5);
                    GlStateManager.rotated(-90, 0, 0, 1);
                    if (mc.gameSettings.mainHand == HandSide.RIGHT) {
                        GlStateManager.translated(0, -0.6, -0.1);
                    } else {
                        GlStateManager.translated(0, -1.9, -0.05);
                    }
                }
                model.renderMinigun(minigun, 1 / 16F, mc.getRenderPartialTicks(), false);
                GlStateManager.popMatrix();
            }
        }
    }
}
