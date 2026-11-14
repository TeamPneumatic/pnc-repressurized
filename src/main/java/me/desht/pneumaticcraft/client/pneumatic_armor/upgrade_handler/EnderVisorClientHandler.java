package me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler;

import com.mojang.blaze3d.vertex.*;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.EnderVisorHandler;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.neoforged.neoforge.client.gui.GuiLayer;

public class EnderVisorClientHandler extends IArmorUpgradeClientHandler.SimpleToggleableHandler<EnderVisorHandler> {
    public EnderVisorClientHandler() {
        super(CommonUpgradeHandlers.enderVisorHandler);
    }

    public static class PumpkinLayer implements GuiLayer {
        private static final Identifier PUMPKIN_OVERLAY = Identifier.parse("textures/misc/pumpkinblur.png");

        @Override
        public void render(GuiGraphicsExtractor graphics, DeltaTracker pDeltaTracker) {
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer();
            if (!handler.upgradeUsable(CommonUpgradeHandlers.enderVisorHandler, true)) {
               return;
            }

            int w = graphics.guiWidth(), h = graphics.guiHeight();
            graphics.blit(RenderPipelines.GUI_TEXTURED, PUMPKIN_OVERLAY, 0, 0, 0, 0, w, h, w, h, ARGB.white(0.5f));
        }
    }
}
