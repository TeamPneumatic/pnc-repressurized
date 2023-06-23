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

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.client.model.entity.drone.ModelDrone;
import me.desht.pneumaticcraft.common.entity.drone.AbstractDroneEntity;
import me.desht.pneumaticcraft.common.item.minigun.AbstractGunAmmoItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class DroneHeldItemLayer extends RenderLayer<AbstractDroneEntity, ModelDrone> {
    DroneHeldItemLayer(RenderDrone renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, AbstractDroneEntity entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack held = entityIn.getDroneHeldItem();
        if (!held.isEmpty() && !(held.getItem() instanceof AbstractGunAmmoItem && entityIn.hasMinigun())) {
            renderHeldItem(held, matrixStackIn, bufferIn, packedLightIn, LivingEntityRenderer.getOverlayCoords(entityIn, 0.0F), entityIn.getLevel());
        }
    }

    private void renderHeldItem(@Nonnull ItemStack stack, PoseStack matrixStack, MultiBufferSource buffer, int packedLight, int packedOverlay, Level level) {
        matrixStack.pushPose();

        // note: transform is currently set up so items render upside down
        matrixStack.translate(0.0D, 1.5D, 0.0D);
        if (!(stack.getItem() instanceof DiggerItem || stack.getItem() instanceof SwordItem || stack.getItem() instanceof HoeItem)) {
            // since items are rendered suspended under the drone,
            // holding tools upside down looks more natural - especially if the drone is digging with them
            matrixStack.mulPose(Axis.XP.rotationDegrees(180));
        }
        float scaleFactor = stack.getItem() instanceof BlockItem ? 0.7F : 0.5F;
        matrixStack.scale(scaleFactor, scaleFactor, scaleFactor);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, packedLight, packedOverlay, matrixStack, buffer, level, 0);

        matrixStack.popPose();
    }

}
