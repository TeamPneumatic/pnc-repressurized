/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.model.ModelMinigun;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ItemMinigun;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3f;

public class RenderItemMinigun extends ItemStackTileEntityRenderer {
    private final ModelMinigun model = new ModelMinigun();

    @Override
    public void renderByItem(ItemStack stack, TransformType transformType, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLightIn, int combinedOverlayIn) {
        if (stack.getItem() == ModItems.MINIGUN.get() && stack.hasTag()) {
            Minecraft mc = Minecraft.getInstance();
            int id = stack.getTag().getInt(ItemMinigun.OWNING_PLAYER_ID);
            Entity owningPlayer = Minecraft.getInstance().level.getEntity(id);
            if (owningPlayer instanceof PlayerEntity) {
                Minigun minigun = ((ItemMinigun) stack.getItem()).getMinigun(stack, (PlayerEntity) owningPlayer);
                matrixStack.pushPose();
                boolean thirdPerson = transformType == TransformType.THIRD_PERSON_RIGHT_HAND || transformType == TransformType.THIRD_PERSON_LEFT_HAND;
                if (thirdPerson) {
                    if (mc.screen instanceof InventoryScreen) {
                        // our own gun in the rendered player model in inventory screen
                        matrixStack.mulPose(Vector3f.XP.rotationDegrees(90f));
                        matrixStack.translate(0.5, -1, -0.5);
                    } else {
                        // rendering our own gun in 3rd person, or rendering someone else's gun
                        matrixStack.scale(1f, -1f, -1f);
                        matrixStack.mulPose(Vector3f.XP.rotationDegrees(-90f));
                        matrixStack.translate(0.5, -1, -0.3);
                    }
                } else {
                    // our own gun in 1st person
                    matrixStack.scale(1.5f, 1.5f, 1.5f);
                    matrixStack.mulPose(Vector3f.ZP.rotationDegrees(-90f));
                    if (transformType == TransformType.FIRST_PERSON_RIGHT_HAND) {
                        matrixStack.translate(0, -0.6, -0.1);
                    } else if (transformType == TransformType.FIRST_PERSON_LEFT_HAND) {
                        matrixStack.translate(0, -1.9, -0.05);
                    }
                }
                model.renderMinigun(matrixStack, buffer, combinedLightIn, combinedOverlayIn, minigun, mc.getFrameTime(), false);
                matrixStack.popPose();
            }
        }
    }
}
