package me.desht.pneumaticcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.client.model.ModelMinigun;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.item.minigun.MinigunItem;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.util.Lazy;

import java.util.Objects;

public class MinigunItemRenderer extends BlockEntityWithoutLevelRenderer {
    private final ModelMinigun model;

    public MinigunItemRenderer(BlockEntityRenderDispatcher pBlockEntityRenderDispatcher, EntityModelSet pEntityModelSet) {
        super(pBlockEntityRenderDispatcher, pEntityModelSet);

        model = new ModelMinigun(pEntityModelSet.bakeLayer(PNCModelLayers.MINIGUN));
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack matrixStack, MultiBufferSource buffer, int combinedLightIn, int combinedOverlayIn) {
        if (stack.getItem() instanceof MinigunItem itemMinigun && stack.hasTag()) {
            Minecraft mc = Minecraft.getInstance();
            int id = Objects.requireNonNull(stack.getTag()).getInt(MinigunItem.OWNING_PLAYER_ID);
            if (ClientUtils.getClientLevel().getEntity(id) instanceof Player player) {
                Minigun minigun = itemMinigun.getMinigun(stack, player);
                matrixStack.pushPose();
                boolean thirdPerson = displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND || displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
                if (thirdPerson) {
                    if (mc.screen instanceof InventoryScreen) {
                        // our own gun in the rendered player model in inventory screen
                        matrixStack.mulPose(Axis.XP.rotationDegrees(-180f));
                        matrixStack.translate(0.5, -1, -0.5);
                    } else {
                        // rendering our own gun in 3rd person, or rendering someone else's gun
                        matrixStack.scale(1f, -1f, -1f);
                        matrixStack.mulPose(Axis.XP.rotationDegrees(75f));
                        matrixStack.mulPose(Axis.YP.rotationDegrees(180));
                        matrixStack.mulPose(Axis.ZP.rotationDegrees(0f));
                        matrixStack.translate(-0.5, -2, -0.3);
                    }
                } else {
                    // Hides minigun in first person if in offhand because it's not usable in offhand anyway
                    if(player.getOffhandItem() == stack) {
                        matrixStack.scale(0f, 0f, 0f);
                    }

                    // Shows minigun in main hand appropriate to which side is set as the main hand
                    else {
                        // our own gun in 1st person
                        matrixStack.scale(1.5f, 1.5f, 1.5f);
                        matrixStack.mulPose(Axis.XP.rotationDegrees(0));
                        matrixStack.mulPose(Axis.YP.rotationDegrees(0));
                        matrixStack.mulPose(Axis.ZP.rotationDegrees(180));
                        if (mc.options.mainHand().get() == HumanoidArm.RIGHT) {
                            matrixStack.translate(-1, -1.7, 0.1);
                        } else {
                            matrixStack.translate(0.4, -1.7, 0.1);
                        }
                    }
                }
                model.renderMinigun(matrixStack, buffer, combinedLightIn, combinedOverlayIn, minigun, mc.getFrameTime(), false);
                matrixStack.popPose();
            }
        }
    }

    public static class RenderProperties implements IClientItemExtensions {
        static final Lazy<BlockEntityWithoutLevelRenderer> renderer = Lazy.of(() ->
                new MinigunItemRenderer(
                        Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                        Minecraft.getInstance().getEntityModels()
                )
        );

        @Override
        public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            return renderer.get();
        }
    }
}
