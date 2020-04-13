package me.desht.pneumaticcraft.client.render.entity.drone;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.model.entity.drone.ModelDrone;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.entity.living.EntityDroneBase;
import me.desht.pneumaticcraft.common.item.ItemGunAmmo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.item.*;

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
                renderHeldItem(held, matrixStackIn, bufferIn, packedLightIn, LivingRenderer.getPackedOverlay(entityIn, 0.0F));
            }
        }
    }

    private void renderHeldItem(@Nonnull ItemStack stack, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, int packedOverlay) {
        matrixStack.push();

        // note: transform is currently set up so items render upside down
        matrixStack.translate(0.0D, 1.5D, 0.0D);
        if (!(stack.getItem() instanceof ToolItem || stack.getItem() instanceof SwordItem || stack.getItem() instanceof HoeItem)) {
            // since items are rendered suspended under the drone,
            // holding tools upside down looks more natural - especially if the drone is digging with them
            matrixStack.rotate(Vector3f.XP.rotationDegrees(180));
        }
        float scaleFactor = stack.getItem() instanceof BlockItem ? 0.7F : 0.5F;
        matrixStack.scale(scaleFactor, scaleFactor, scaleFactor);
        Minecraft.getInstance().getItemRenderer().renderItem(stack, ItemCameraTransforms.TransformType.FIXED, packedLight, packedOverlay, matrixStack, buffer);

        matrixStack.pop();
    }

}
