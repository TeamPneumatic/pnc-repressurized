package me.desht.pneumaticcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import me.desht.pneumaticcraft.client.model.ModelMinigun;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.item.ItemMinigun;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.common.util.NonNullLazy;

import java.util.Objects;

public class MinigunItemRenderer extends BlockEntityWithoutLevelRenderer {
    private final ModelMinigun model;

    public MinigunItemRenderer(BlockEntityRenderDispatcher pBlockEntityRenderDispatcher, EntityModelSet pEntityModelSet) {
        super(pBlockEntityRenderDispatcher, pEntityModelSet);

        model = new ModelMinigun(pEntityModelSet.bakeLayer(PNCModelLayers.MINIGUN));
    }

    @Override
    public void renderByItem(ItemStack stack, TransformType transformType, PoseStack matrixStack, MultiBufferSource buffer, int combinedLightIn, int combinedOverlayIn) {
        if (stack.getItem() instanceof ItemMinigun itemMinigun && stack.hasTag()) {
            Minecraft mc = Minecraft.getInstance();
            int id = Objects.requireNonNull(stack.getTag()).getInt(ItemMinigun.OWNING_PLAYER_ID);
            if (ClientUtils.getClientLevel().getEntity(id) instanceof Player player) {
                Minigun minigun = itemMinigun.getMinigun(stack, player);
                matrixStack.pushPose();
                boolean thirdPerson = transformType == TransformType.THIRD_PERSON_RIGHT_HAND || transformType == TransformType.THIRD_PERSON_LEFT_HAND;
                if (thirdPerson) {
                    if (mc.screen instanceof InventoryScreen) {
                        // our own gun in the rendered player model in inventory screen
                        matrixStack.mulPose(Vector3f.XP.rotationDegrees(-180f));
                        matrixStack.translate(0.5, -1, -0.5);
                    } else {
                        // rendering our own gun in 3rd person, or rendering someone else's gun
                        matrixStack.scale(1f, -1f, -1f);
                        matrixStack.mulPose(Vector3f.XP.rotationDegrees(75f));
                        matrixStack.mulPose(Vector3f.YP.rotationDegrees(180));
                        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(0f));
                        matrixStack.translate(-0.5, -2, -0.3);
                    }
                } else {
                    // our own gun in 1st person
                    matrixStack.scale(1.5f, 1.5f, 1.5f);
                    matrixStack.mulPose(Vector3f.XP.rotationDegrees(0));
                    matrixStack.mulPose(Vector3f.YP.rotationDegrees(0));
                    matrixStack.mulPose(Vector3f.ZP.rotationDegrees(180));
                    if (mc.options.mainHand == HumanoidArm.RIGHT) {
                        matrixStack.translate(-1, -1.7, 0.1);
                    } else {
                        matrixStack.translate(0, 0, 0);
                    }
                }
                model.renderMinigun(matrixStack, buffer, combinedLightIn, combinedOverlayIn, minigun, mc.getFrameTime(), false);
                matrixStack.popPose();
            }
        }
    }

    public static class RenderProperties implements IItemRenderProperties {
        static final NonNullLazy<BlockEntityWithoutLevelRenderer> renderer = NonNullLazy.of(() ->
                new MinigunItemRenderer(
                        Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                        Minecraft.getInstance().getEntityModels()
                )
        );

        @Override
        public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
            return renderer.get();
        }
    }
}
