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
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
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
import net.minecraftforge.client.ForgeHooksClient;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class PneumaticArmorLayer<E extends LivingEntity, M extends EntityModel<E>> extends RenderLayer<E, M> {
    private final HumanoidModel<E> modelLeggings;
    private final HumanoidModel<E> modelArmor;

    public PneumaticArmorLayer(RenderLayerParent<E, M> entityRendererIn, EntityModelSet models) {
        super(entityRendererIn);
        this.modelLeggings = new HumanoidModel<>(models.bakeLayer(PNCModelLayers.PNEUMATIC_LEGS));
        this.modelArmor = new HumanoidModel<>(models.bakeLayer(PNCModelLayers.PNEUMATIC_ARMOR));;
    }

    @Override
    public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, E entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (ItemPneumaticArmor.isPlayerWearingAnyPneumaticArmor(ClientUtils.getClientPlayer())) {
            modelArmor.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            modelLeggings.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            renderSlot(matrixStackIn, bufferIn, entity, EquipmentSlot.CHEST, packedLightIn, modelArmor);
            renderSlot(matrixStackIn, bufferIn, entity, EquipmentSlot.LEGS, packedLightIn, modelLeggings);
            renderSlot(matrixStackIn, bufferIn, entity, EquipmentSlot.FEET, packedLightIn, modelArmor);
            renderSlot(matrixStackIn, bufferIn, entity, EquipmentSlot.HEAD, packedLightIn, modelArmor);
        }
    }

    private void renderSlot(PoseStack matrixStack, MultiBufferSource buffer, E entity, EquipmentSlot slot, int light, HumanoidModel<E> model) {
        ItemStack stack = entity.getItemBySlot(slot);
        if (stack.getItem() instanceof ItemPneumaticArmor) {
            if (((ItemPneumaticArmor) stack.getItem()).getSlot() == slot) {
                model = ForgeHooksClient.getArmorModel(entity, stack, slot, model);
                this.getParentModel().copyPropertiesTo(model);
                this.setModelSlotVisible(model, slot);
                boolean glint = stack.hasFoil();

                // secondary texture layer in all slots
                float[] secondary = RenderUtils.decomposeColorF(((ItemPneumaticArmor) stack.getItem()).getSecondaryColor(stack));
                this.doRender(matrixStack, buffer, light, glint, model, secondary[1], secondary[2], secondary[3], slot, ExtraLayer.SECONDARY_COLOR);

                if (slot == EquipmentSlot.CHEST) {
                    // currently just the chestpiece "core" - untinted
                    this.doRender(matrixStack, buffer, RenderUtils.FULL_BRIGHT, glint, model, 1f, 1f, 1f, slot, ExtraLayer.TRANSLUCENT);
                }

                if (slot == EquipmentSlot.HEAD) {
                    // eyepiece in head slot only
                    float[] eyepiece = RenderUtils.decomposeColorF(((ItemPneumaticArmor) stack.getItem()).getEyepieceColor(stack));
                    this.doRender(matrixStack, buffer, RenderUtils.FULL_BRIGHT, false, model, eyepiece[1], eyepiece[2], eyepiece[3], slot, ExtraLayer.EYEPIECE);
                }
            }
        }
    }

    private void doRender(PoseStack matrixStack, MultiBufferSource buffer, int light, boolean glint, HumanoidModel<E> model, float r, float g, float b, EquipmentSlot slot, ExtraLayer extraLayer) {
        ResourceLocation armorResource = extraLayer.getArmorResource(slot);
        VertexConsumer ivertexbuilder = ItemRenderer.getArmorFoilBuffer(buffer, extraLayer.getRenderType(armorResource), false, glint);
        model.renderToBuffer(matrixStack, ivertexbuilder, light, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
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
