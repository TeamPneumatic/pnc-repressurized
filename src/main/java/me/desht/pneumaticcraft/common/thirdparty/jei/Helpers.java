package me.desht.pneumaticcraft.common.thirdparty.jei;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import java.util.List;

class Helpers {
    static void drawIconAt(IDrawable icon, int x, int y) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableDepthTest();
        GlStateManager.enableAlphaTest();
        icon.draw(x, y);
        GlStateManager.enableDepthTest();
        GlStateManager.disableAlphaTest();
    }

    static void drawTextAt(String translationKey, int x, int y) {
        List<String> text = PneumaticCraftUtils.splitString(I18n.format(translationKey), 30);
        int h = Minecraft.getInstance().fontRenderer.FONT_HEIGHT;
        for (int i = 0; i < text.size(); i++) {
            Minecraft.getInstance().fontRenderer.drawString(text.get(i), x, y + i * h, 0xFF404040);
        }
    }

    static WidgetTemperature makeTemperatureWidget(int x, int y, int temperature) {
        return new WidgetTemperature(x, y, 273, 673, PneumaticRegistry.getInstance().getHeatRegistry().getHeatExchangerLogic(), temperature);
    }

    static IDrawable makeTankOverlay(int height) {
        return JEIPlugin.jeiHelpers.getGuiHelper()
                .drawableBuilder(Textures.WIDGET_TANK, 0, 64 - height, 16, height)
                .setTextureSize(16, 64).build();
    }
}
