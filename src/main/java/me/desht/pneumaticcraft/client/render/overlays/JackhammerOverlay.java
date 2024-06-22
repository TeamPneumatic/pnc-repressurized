package me.desht.pneumaticcraft.client.render.overlays;

import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.item.JackHammerItem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.opengl.GL11;

public class JackhammerOverlay implements LayeredDraw.Layer {
    @Override
    public void render(GuiGraphics graphics, DeltaTracker partialTicks) {
        Player player = Minecraft.getInstance().player;
        if (player == null || !(player.getMainHandItem().getItem() instanceof JackHammerItem)
                || !Minecraft.getInstance().options.getCameraType().isFirstPerson())
            return;
        long timeDelta = player.level().getGameTime() - JackHammerItem.getLastModeSwitchTime();
        JackHammerItem.DigMode digMode = JackHammerItem.getDigMode(player.getMainHandItem());
        boolean showHud = ConfigHelper.client().general.jackHammerHud.get();
        if (digMode.atLeast(JackHammerItem.DigMode.MODE_1X2) && showHud || timeDelta < 30 || player.isCrouching()) {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShaderColor(1f, 1f, 1f, 0.25f);
            float scaleFactor = Mth.clamp((float) Minecraft.getInstance().getWindow().getGuiScale(), 2, 3);
            graphics.pose().pushPose();
            graphics.pose().translate(graphics.guiWidth() / 2.0, graphics.guiHeight() / 2.0, 0);
            graphics.pose().scale(scaleFactor, scaleFactor, scaleFactor);
            graphics.pose().translate(8, -8, 0);
            graphics.blit(digMode.getGuiIcon(), 0, 0, 0, 0, 16, 16, 16, 16);
            graphics.pose().popPose();
        }
    }
}
