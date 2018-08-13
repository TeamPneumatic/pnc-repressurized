package me.desht.pneumaticcraft.client.render;

import me.desht.pneumaticcraft.client.model.entity.ModelDroneMinigun;
import me.desht.pneumaticcraft.common.item.ItemMinigun;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class RenderItemMinigun extends TileEntityItemStackRenderer {
    private final ModelDroneMinigun model = new ModelDroneMinigun();

    @Override
    public void renderByItem(ItemStack stack, float partialTicks) {
        if (stack.getItem() == Itemss.MINIGUN && stack.hasTagCompound()) {
            EntityPlayer player = Minecraft.getMinecraft().player;
            int id = stack.getTagCompound().getInteger("owningPlayerId");
            Entity owningPlayer = Minecraft.getMinecraft().world.getEntityByID(id);
            if (owningPlayer instanceof EntityPlayer) {
                Minigun minigun = ((ItemMinigun) stack.getItem()).getMinigun(stack, (EntityPlayer) owningPlayer);
                GlStateManager.pushMatrix();
                if (Minecraft.getMinecraft().gameSettings.thirdPersonView != 0 || player.getEntityId() != owningPlayer.getEntityId()) {
                    // rendering our own gun in 3rd person, or rendering someone else's gun
                    GlStateManager.scale(1, -1, 1);
                    GlStateManager.rotate(-90, 1, 0, 0);
                    GlStateManager.translate(0.5, -2, -0.3);
                } else if (Minecraft.getMinecraft().currentScreen instanceof GuiInventory) {
                    // our own gun in the rendered player model in inventory screen
                    GlStateManager.rotate(90, 1, 0, 0);
                    GlStateManager.translate(0.5, -1, -0.5);
                } else {
                    // our own gun in 1st person
                    GlStateManager.scale(1.5, 1.5, 1.5);
                    GlStateManager.rotate(-90, 0, 0, 1);
                    GlStateManager.translate(0, -0.6, -0.1);
                }
                model.renderMinigun(minigun, 1 / 16F, partialTicks, false);
                GlStateManager.popMatrix();
            }
        }
    }
}
