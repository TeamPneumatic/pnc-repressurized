package me.desht.pneumaticcraft.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.model.ModelMinigun;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ItemMinigun;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Vector3f;

public class RenderItemMinigun extends ItemStackTileEntityRenderer {
    private final ModelMinigun model = new ModelMinigun();

    @Override
    public void func_239207_a_(ItemStack stack, ItemCameraTransforms.TransformType transformType, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLightIn, int combinedOverlayIn) {
        if (stack.getItem() == ModItems.MINIGUN.get() && stack.hasTag()) {
            Minecraft mc = Minecraft.getInstance();
            PlayerEntity player = mc.player;
            int id = stack.getTag().getInt("owningPlayerId");
            Entity owningPlayer = Minecraft.getInstance().world.getEntityByID(id);
            if (owningPlayer instanceof PlayerEntity) {
                Minigun minigun = ((ItemMinigun) stack.getItem()).getMinigun(stack, (PlayerEntity) owningPlayer);
                matrixStack.push();
                if (mc.gameSettings.thirdPersonView != 0 || player.getEntityId() != owningPlayer.getEntityId()) {
                    // rendering our own gun in 3rd person, or rendering someone else's gun
                    matrixStack.scale(1f, -1f, -1f);
                    matrixStack.rotate(Vector3f.XP.rotationDegrees(-90f));
                    matrixStack.translate(0.5, -1, -0.3);
                } else if (mc.currentScreen instanceof InventoryScreen) {
                    // our own gun in the rendered player model in inventory screen
                    matrixStack.rotate(Vector3f.XP.rotationDegrees(90f));
                    matrixStack.translate(0.5, -1, -0.5);
                } else {
                    // our own gun in 1st person
                    matrixStack.scale(1.5f, 1.5f, 1.5f);
                    matrixStack.rotate(Vector3f.ZP.rotationDegrees(-90f));
                    if (mc.gameSettings.mainHand == HandSide.RIGHT) {
                        matrixStack.translate(0, -0.6, -0.1);
                    } else {
                        matrixStack.translate(0, -1.9, -0.05);
                    }
                }
                model.renderMinigun(matrixStack, buffer, combinedLightIn, combinedOverlayIn, minigun, mc.getRenderPartialTicks(), false);
                matrixStack.pop();
            }
        }
    }
}
