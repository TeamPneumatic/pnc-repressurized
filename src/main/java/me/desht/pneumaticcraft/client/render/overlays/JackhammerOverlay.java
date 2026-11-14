package me.desht.pneumaticcraft.client.render.overlays;

import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.item.JackHammerItem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.gui.GuiLayer;

public class JackhammerOverlay implements GuiLayer {
    @Override
    public void render(GuiGraphicsExtractor graphics, DeltaTracker partialTicks) {
        Player player = Minecraft.getInstance().player;
        if (player == null || !(player.getMainHandItem().getItem() instanceof JackHammerItem)
                || !Minecraft.getInstance().options.getCameraType().isFirstPerson())
            return;
        long timeDelta = player.level().getGameTime() - JackHammerItem.getLastModeSwitchTime();
        JackHammerItem.DigMode digMode = JackHammerItem.getDigMode(player.getMainHandItem());
        boolean showHud = ConfigHelper.client().general.jackHammerHud.get();
        if (digMode.atLeast(JackHammerItem.DigMode.MODE_1X2) && showHud || timeDelta < 30 || player.isShiftKeyDown()) {
            float scaleFactor = Mth.clamp((float) Minecraft.getInstance().getWindow().getGuiScale(), 2, 3);
            graphics.pose().pushMatrix();
            graphics.pose().translate(graphics.guiWidth() / 2f, graphics.guiHeight() / 2f);
            graphics.pose().scale(scaleFactor, scaleFactor);
            graphics.pose().translate(8, -8);
            graphics.blit(RenderPipelines.GUI_TEXTURED, digMode.getGuiIcon(), 0, 0, 0, 0, 16, 16, 16, 16, ARGB.white(0.25f));
            graphics.pose().popMatrix();
        }
    }
}
