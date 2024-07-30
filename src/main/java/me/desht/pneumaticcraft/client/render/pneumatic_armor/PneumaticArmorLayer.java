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
import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.neoforged.neoforge.client.ClientHooks;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static net.minecraft.client.renderer.LightTexture.FULL_BRIGHT;

public class PneumaticArmorLayer<E extends LivingEntity, M extends HumanoidModel<E>> extends RenderLayer<E, M> {
    private final HumanoidModel<E> modelLeggings;
    private final HumanoidModel<E> modelArmor;
    private final TextureAtlas armorTrimAtlas;

    public PneumaticArmorLayer(RenderLayerParent<E, M> entityRendererIn, EntityModelSet models, ModelManager modelManager) {
        super(entityRendererIn);

        this.modelLeggings = new HumanoidModel<>(models.bakeLayer(PNCModelLayers.PNEUMATIC_LEGS));
        this.modelArmor = new HumanoidModel<>(models.bakeLayer(PNCModelLayers.PNEUMATIC_ARMOR));
        this.armorTrimAtlas = modelManager.getAtlas(Sheets.ARMOR_TRIMS_SHEET);
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

    private void renderSlot(PoseStack poseStack, MultiBufferSource buffer, E entity, EquipmentSlot slot, int light, HumanoidModel<E> model, float partialTicks, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack stack = entity.getItemBySlot(slot);
        if (stack.getItem() instanceof PneumaticArmorItem armor && armor.getType().getSlot() == slot) {
            this.getParentModel().copyPropertiesTo(model);
            model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
            model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            this.setModelSlotVisible(model, slot);
            Model model1 = ClientHooks.getArmorModel(entity, stack, slot, model);

            // secondary texture layer in all slots
            this.doRender(poseStack, buffer, light, model1, armor.getSecondaryColor(stack), slot, ExtraLayer.SECONDARY_COLOR);

            if (slot == EquipmentSlot.CHEST) {
                // currently just the chestpiece "core" - untinted
                this.doRender(poseStack, buffer, FULL_BRIGHT, model1, 0xFFFFFFFF, slot, ExtraLayer.TRANSLUCENT);
            }

            if (slot == EquipmentSlot.HEAD) {
                // eyepiece in head slot only
                this.doRender(poseStack, buffer, FULL_BRIGHT, model1, armor.getEyepieceColor(stack), slot, ExtraLayer.EYEPIECE);
            }

            ArmorTrim armortrim = stack.get(DataComponents.TRIM);
            if (armortrim != null) {
                this.renderTrim(armor.getMaterial(), poseStack, buffer, light, armortrim, model, slot == EquipmentSlot.LEGS);
            }

            if (stack.hasFoil()) {
                this.renderGlint(poseStack, buffer, light, model);
            }
        }
    }

    private void doRender(PoseStack matrixStack, MultiBufferSource buffer, int light, Model model, int color, EquipmentSlot slot, ExtraLayer extraLayer) {
        ResourceLocation armorResource = extraLayer.getArmorResource(slot);
        VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(buffer, extraLayer.getRenderType(armorResource), false);
        model.renderToBuffer(matrixStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, color);
    }

    private void renderTrim(Holder<ArmorMaterial> material, PoseStack poseStack, MultiBufferSource bufferSource, int light, ArmorTrim trim, HumanoidModel<E> model, boolean innerModel) {
        TextureAtlasSprite sprite = armorTrimAtlas.getSprite(innerModel ? trim.innerTexture(material) : trim.outerTexture(material));
        VertexConsumer vertexconsumer = sprite.wrap(bufferSource.getBuffer(Sheets.armorTrimsSheet(trim.pattern().value().decal())));
        model.renderToBuffer(poseStack, vertexconsumer, light, OverlayTexture.NO_OVERLAY);
    }

    private void renderGlint(PoseStack poseStack, MultiBufferSource buffer, int light, net.minecraft.client.model.Model model) {
        model.renderToBuffer(poseStack, buffer.getBuffer(RenderType.armorEntityGlint()), light, OverlayTexture.NO_OVERLAY);
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
