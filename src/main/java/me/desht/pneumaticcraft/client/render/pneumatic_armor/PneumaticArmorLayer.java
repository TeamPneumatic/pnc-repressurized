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

package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.ClientHooks;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class PneumaticArmorLayer<E extends LivingEntity, M extends HumanoidModel<E>> extends RenderLayer<E, M> {
    private final HumanoidModel<E> modelLeggings;
    private final HumanoidModel<E> modelArmor;

    public PneumaticArmorLayer(RenderLayerParent<E, M> entityRendererIn, EntityModelSet models) {
        super(entityRendererIn);
        this.modelLeggings = new HumanoidModel<>(models.bakeLayer(PNCModelLayers.PNEUMATIC_LEGS));
        this.modelArmor = new HumanoidModel<>(models.bakeLayer(PNCModelLayers.PNEUMATIC_ARMOR));
    }

    @Override
    public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, E entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        renderSlot(matrixStackIn, bufferIn, entity, EquipmentSlot.CHEST, packedLightIn, modelArmor,
                partialTicks, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        renderSlot(matrixStackIn, bufferIn, entity, EquipmentSlot.LEGS, packedLightIn, modelLeggings,
                partialTicks, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        renderSlot(matrixStackIn, bufferIn, entity, EquipmentSlot.FEET, packedLightIn, modelArmor,
                partialTicks, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        renderSlot(matrixStackIn, bufferIn, entity, EquipmentSlot.HEAD, packedLightIn, modelArmor,
                partialTicks, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    private void renderSlot(PoseStack matrixStack, MultiBufferSource buffer, E entity, EquipmentSlot slot, int light, HumanoidModel<E> model, float partialTicks, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack stack = entity.getItemBySlot(slot);
        if (stack.getItem() instanceof PneumaticArmorItem armor && armor.getType().getSlot() == slot) {
            this.getParentModel().copyPropertiesTo(model);
            model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
            model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            this.setModelSlotVisible(model, slot);
            Model model1 = ClientHooks.getArmorModel(entity, stack, slot, model);
            boolean glint = stack.hasFoil();

            // secondary texture layer in all slots
            float[] secondary = RenderUtils.decomposeColorF(armor.getSecondaryColor(stack));
            this.doRender(matrixStack, buffer, light, glint, model1, secondary[1], secondary[2], secondary[3], slot, ExtraLayer.SECONDARY_COLOR);

            if (slot == EquipmentSlot.CHEST) {
                // currently just the chestpiece "core" - untinted
                this.doRender(matrixStack, buffer, RenderUtils.FULL_BRIGHT, glint, model1, 1f, 1f, 1f, slot, ExtraLayer.TRANSLUCENT);
            }

            if (slot == EquipmentSlot.HEAD) {
                // eyepiece in head slot only
                float[] eyepiece = RenderUtils.decomposeColorF(armor.getEyepieceColor(stack));
                this.doRender(matrixStack, buffer, RenderUtils.FULL_BRIGHT, false, model1, eyepiece[1], eyepiece[2], eyepiece[3], slot, ExtraLayer.EYEPIECE);
            }
        }
    }

    private void doRender(PoseStack matrixStack, MultiBufferSource buffer, int light, boolean glint, Model model, float r, float g, float b, EquipmentSlot slot, ExtraLayer extraLayer) {
        ResourceLocation armorResource = extraLayer.getArmorResource(slot);
        VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(buffer, extraLayer.getRenderType(armorResource), false, glint);
        model.renderToBuffer(matrixStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
    }

    protected void setModelSlotVisible(HumanoidModel<E> model, EquipmentSlot slotIn) {
        model.setAllVisible(false);
        switch (slotIn) {
            case HEAD -> {
                model.head.visible = true;
                model.hat.visible = true;
            }
            case CHEST -> {
                model.body.visible = true;
                model.rightArm.visible = true;
                model.leftArm.visible = true;
            }
            case LEGS -> {
                model.body.visible = true;
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
            }
            case FEET -> {
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
            }
        }
    }

    enum ExtraLayer {
        SECONDARY_COLOR("overlay", false),
        TRANSLUCENT("translucent", true),
        EYEPIECE("eyepiece", true);

        private final ResourceLocation rl1, rl2;
        private final boolean translucent;

        ExtraLayer(String name, boolean translucent) {
            this.translucent = translucent;
            this.rl1 = RL("textures/armor/pneumatic_1_" + name + ".png");
            this.rl2 = RL("textures/armor/pneumatic_2_" + name + ".png");
        }

        RenderType getRenderType(ResourceLocation rl) {
            return translucent ? ModRenderTypes.getArmorTranslucentNoCull(rl) :  RenderType.armorCutoutNoCull(rl);
        }

        ResourceLocation getArmorResource(EquipmentSlot slot) {
            return slot == EquipmentSlot.LEGS ? rl2 : rl1;
        }
    }
}
