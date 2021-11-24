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

package me.desht.pneumaticcraft.client.render.entity.drone;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.model.entity.drone.ModelDrone;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.entity.living.EntityDroneBase;
import me.desht.pneumaticcraft.common.item.ItemGunAmmo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.item.*;
import net.minecraft.util.math.vector.Vector3f;

import javax.annotation.Nonnull;

public class DroneHeldItemLayer extends LayerRenderer<EntityDroneBase, ModelDrone> {
    DroneHeldItemLayer(RenderDrone renderer) {
        super(renderer);
    }

    @Override
    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, EntityDroneBase entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entityIn instanceof EntityDrone) {
            EntityDrone drone = (EntityDrone) entityIn;
            ItemStack held = drone.getDroneHeldItem();
            if (!held.isEmpty() && !(held.getItem() instanceof ItemGunAmmo && drone.hasMinigun())) {
                renderHeldItem(held, matrixStackIn, bufferIn, packedLightIn, LivingRenderer.getOverlayCoords(entityIn, 0.0F));
            }
        }
    }

    private void renderHeldItem(@Nonnull ItemStack stack, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, int packedOverlay) {
        matrixStack.pushPose();

        // note: transform is currently set up so items render upside down
        matrixStack.translate(0.0D, 1.5D, 0.0D);
        if (!(stack.getItem() instanceof ToolItem || stack.getItem() instanceof SwordItem || stack.getItem() instanceof HoeItem)) {
            // since items are rendered suspended under the drone,
            // holding tools upside down looks more natural - especially if the drone is digging with them
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(180));
        }
        float scaleFactor = stack.getItem() instanceof BlockItem ? 0.7F : 0.5F;
        matrixStack.scale(scaleFactor, scaleFactor, scaleFactor);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemCameraTransforms.TransformType.FIXED, packedLight, packedOverlay, matrixStack, buffer);

        matrixStack.popPose();
    }

}
