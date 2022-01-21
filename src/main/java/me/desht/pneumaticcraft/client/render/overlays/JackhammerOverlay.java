package me.desht.pneumaticcraft.client.render.overlays;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.item.ItemJackHammer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
import org.lwjgl.opengl.GL11;

public class JackhammerOverlay implements IIngameOverlay {
    @Override
    public void render(ForgeIngameGui gui, PoseStack mStack, float partialTicks, int width, int height) {
        Player player = Minecraft.getInstance().player;
        if (player == null || !(player.getMainHandItem().getItem() instanceof ItemJackHammer)
                || !Minecraft.getInstance().options.getCameraType().isFirstPerson())
            return;
        long timeDelta = player.level.getGameTime() - ItemJackHammer.getLastModeSwitchTime();
        ItemJackHammer.DigMode digMode = ItemJackHammer.getDigMode(player.getMainHandItem());
        if (digMode != null && (digMode.atLeast(ItemJackHammer.DigMode.MODE_1X2) || timeDelta < 30 || player.isCrouching())) {
            GuiUtils.bindTexture(digMode.getGuiIcon());
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShaderColor(1f, 1f, 1f, 0.25f);
            float scaleFactor = Mth.clamp((float) Minecraft.getInstance().getWindow().getGuiScale(), 2, 3);
            mStack.pushPose();
            mStack.translate(width / 2.0, height / 2.0, 0);
            mStack.scale(scaleFactor, scaleFactor, scaleFactor);
            mStack.translate(8, -8, 0);
            GuiComponent.blit(mStack, 0, 0, 0, 0, 16, 16, 16, 16);
            mStack.popPose();
        }
    }
}
