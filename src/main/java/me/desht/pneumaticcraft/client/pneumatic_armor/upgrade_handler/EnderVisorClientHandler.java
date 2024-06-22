package me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.EnderVisorHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class EnderVisorClientHandler extends IArmorUpgradeClientHandler.SimpleToggleableHandler<EnderVisorHandler> {
    public EnderVisorClientHandler() {
        super(CommonUpgradeHandlers.enderVisorHandler);
    }

    public static class PumpkinOverlay implements net.neoforged.neoforge.client.extensions.common.IClientItemExtensions {
        private static final ResourceLocation PUMPKIN_OVERLAY = ResourceLocation.parse("textures/misc/pumpkinblur.png");

        @Override
        public void renderHelmetOverlay(ItemStack stack, Player player, int width, int height, float partialTicks) {
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer();
            if (handler.upgradeUsable(CommonUpgradeHandlers.enderVisorHandler, true)) {
                renderTextureOverlay();
            }
        }

        // largely lifted from ForgeInGameGui#renderTextureOverlay
        private void renderTextureOverlay() {
            int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.5F);
            RenderSystem.setShaderTexture(0, PUMPKIN_OVERLAY);
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bufferbuilder.addVertex(0.0F, screenHeight, -90.0F).setUv(0.0F, 1.0F);
            bufferbuilder.addVertex(screenWidth, screenHeight, -90.0F).setUv(1.0F, 1.0F);
            bufferbuilder.addVertex(screenWidth, 0.0F, -90.0F).setUv(1.0F, 0.0F);
            bufferbuilder.addVertex(0.0F, 0.0F, -90.0F).setUv(0.0F, 0.0F);
            BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
