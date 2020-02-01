package me.desht.pneumaticcraft.common.thirdparty.jei;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.client.gui.GuiPneumaticScreenBase;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.handlers.IGuiProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.util.LazyOptional;

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
        return new WidgetTemperature(x, y, 273, 673,
                LazyOptional.of(() -> PneumaticRegistry.getInstance().getHeatRegistry().makeHeatExchangerLogic()), temperature);
    }

    static IDrawable makeTankOverlay(int height) {
        return JEIPlugin.jeiHelpers.getGuiHelper()
                .drawableBuilder(Textures.WIDGET_TANK, 0, 64 - height, 16, height)
                .setTextureSize(16, 64).build();
    }

    static IGuiProperties getGuiProperties(GuiPneumaticScreenBase gui) {
        return gui.width == 0 || gui.height == 0 ? null : new GuiProps(gui);
    }

    private static class GuiProps implements IGuiProperties {
        private final GuiPneumaticScreenBase gui;

        GuiProps(GuiPneumaticScreenBase gui) {
            this.gui = gui;
        }

        @Override
        public Class<? extends Screen> getScreenClass() {
            return gui.getClass();
        }

        @Override
        public int getGuiLeft() {
            return gui.guiLeft;
        }

        @Override
        public int getGuiTop() {
            return gui.guiTop;
        }

        @Override
        public int getGuiXSize() {
            return gui.xSize;
        }

        @Override
        public int getGuiYSize() {
            return gui.ySize;
        }

        @Override
        public int getScreenWidth() {
            return gui.width;
        }

        @Override
        public int getScreenHeight() {
            return gui.height;
        }
    }
}
