package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class PneumaticArmorLayer<T extends LivingEntity, M extends BipedModel<T>, A extends BipedModel<T>> extends LayerRenderer<T, M> {
    private final A modelLeggings;
    private final A modelArmor;

    public PneumaticArmorLayer(IEntityRenderer<T, M> entityRendererIn, A modelLeggings, A modelArmor) {
        super(entityRendererIn);
        this.modelLeggings = modelLeggings;
        this.modelArmor = modelArmor;
    }

    @Override
    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        renderSlot(matrixStackIn, bufferIn, entity, EquipmentSlotType.CHEST, packedLightIn, modelArmor);
        renderSlot(matrixStackIn, bufferIn, entity, EquipmentSlotType.LEGS, packedLightIn, modelLeggings);
        renderSlot(matrixStackIn, bufferIn, entity, EquipmentSlotType.FEET, packedLightIn, modelArmor);
        renderSlot(matrixStackIn, bufferIn, entity, EquipmentSlotType.HEAD, packedLightIn, modelArmor);
    }

    private void renderSlot(MatrixStack matrixStack, IRenderTypeBuffer buffer, T entity, EquipmentSlotType slot, int light, A model) {
        ItemStack stack = entity.getItemStackFromSlot(slot);
        if (stack.getItem() instanceof ItemPneumaticArmor) {
            if (((ItemPneumaticArmor) stack.getItem()).getEquipmentSlot() == slot) {
                model = ForgeHooksClient.getArmorModel(entity, stack, slot, model);
                this.getEntityModel().setModelAttributes(model);
                this.setModelSlotVisible(model, slot);
                boolean glint = stack.hasEffect();

                // secondary texture layer in all slots
                float[] secondary = RenderUtils.decomposeColorF(((ItemPneumaticArmor) stack.getItem()).getSecondaryColor(stack));
                this.doRender(matrixStack, buffer, light, glint, model, secondary[1], secondary[2], secondary[3], slot, ExtraLayer.SECONDARY_COLOR);

                if (slot == EquipmentSlotType.CHEST) {
                    // currently just the chestpiece "core" - untinted
                    this.doRender(matrixStack, buffer, RenderUtils.FULL_BRIGHT, glint, model, 1f, 1f, 1f, slot, ExtraLayer.TRANSLUCENT);
                }

                if (slot == EquipmentSlotType.HEAD) {
                    // eyepiece in head slot only
                    float[] eyepiece = RenderUtils.decomposeColorF(((ItemPneumaticArmor) stack.getItem()).getEyepieceColor(stack));
                    this.doRender(matrixStack, buffer, RenderUtils.FULL_BRIGHT, false, model, eyepiece[1], eyepiece[2], eyepiece[3], slot, ExtraLayer.EYEPIECE);
                }
            }
        }
    }

    private void doRender(MatrixStack matrixStack, IRenderTypeBuffer buffer, int light, boolean glint, A model, float r, float g, float b, EquipmentSlotType slot, ExtraLayer extraLayer) {
        ResourceLocation armorResource = extraLayer.getArmorResource(slot);
        IVertexBuilder ivertexbuilder = ItemRenderer.getArmorVertexBuilder(buffer, extraLayer.getRenderType(armorResource), false, glint);
        model.render(matrixStack, ivertexbuilder, light, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
    }

    protected void setModelSlotVisible(A model, EquipmentSlotType slotIn) {
        model.setVisible(false);
        switch (slotIn) {
            case HEAD:
                model.bipedHead.showModel = true;
                model.bipedHeadwear.showModel = true;
                break;
            case CHEST:
                model.bipedBody.showModel = true;
                model.bipedRightArm.showModel = true;
                model.bipedLeftArm.showModel = true;
                break;
            case LEGS:
                model.bipedBody.showModel = true;
                model.bipedRightLeg.showModel = true;
                model.bipedLeftLeg.showModel = true;
                break;
            case FEET:
                model.bipedRightLeg.showModel = true;
                model.bipedLeftLeg.showModel = true;
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
            return translucent ? ModRenderTypes.getArmorTranslucentNoCull(rl) :  RenderType.getArmorCutoutNoCull(rl);
        }

        ResourceLocation getArmorResource(EquipmentSlotType slot) {
            return slot == EquipmentSlotType.LEGS ? rl2 : rl1;
        }
    }
}
