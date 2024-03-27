package me.desht.pneumaticcraft.client.render.overlays;

import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.item.JackHammerItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.gui.overlay.ExtendedGui;
import net.neoforged.neoforge.client.gui.overlay.IGuiOverlay;
import org.lwjgl.opengl.GL11;

public class JackhammerOverlay implements IGuiOverlay {
    @Override
    public void render(ExtendedGui gui, GuiGraphics graphics, float partialTicks, int width, int height) {
        Player player = Minecraft.getInstance().player;
        if (player == null || !(player.getMainHandItem().getItem() instanceof JackHammerItem)
                || !Minecraft.getInstance().options.getCameraType().isFirstPerson())
            return;
        long timeDelta = player.level().getGameTime() - JackHammerItem.getLastModeSwitchTime();
        JackHammerItem.DigMode digMode = JackHammerItem.getDigMode(player.getMainHandItem());
        boolean showHud = ConfigHelper.client().general.jackHammerHud.get();
        if (digMode != null && (digMode.atLeast(JackHammerItem.DigMode.MODE_1X2) && showHud || timeDelta < 30 || player.isCrouching())) {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShaderColor(1f, 1f, 1f, 0.25f);
            float scaleFactor = Mth.clamp((float) Minecraft.getInstance().getWindow().getGuiScale(), 2, 3);
            graphics.pose().pushPose();
            graphics.pose().translate(width / 2.0, height / 2.0, 0);
            graphics.pose().scale(scaleFactor, scaleFactor, scaleFactor);
            graphics.pose().translate(8, -8, 0);
            graphics.blit(digMode.getGuiIcon(), 0, 0, 0, 0, 16, 16, 16, 16);
            graphics.pose().popPose();
        }
    }
}
