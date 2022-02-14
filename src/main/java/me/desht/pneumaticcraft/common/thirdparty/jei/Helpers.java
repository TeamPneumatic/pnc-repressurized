/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.client.gui.AbstractPneumaticCraftScreen;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.handlers.IGuiProperties;
import net.minecraft.client.gui.screens.Screen;

class Helpers {
    static IDrawable makeTankOverlay(int height) {
        return JEIPlugin.jeiHelpers.getGuiHelper()
                .drawableBuilder(Textures.WIDGET_TANK, 0, 64 - height, 16, height)
                .setTextureSize(16, 64).build();
    }

    static IGuiProperties getGuiProperties(AbstractPneumaticCraftScreen gui) {
        return gui.width == 0 || gui.height == 0 ? null : new GuiProps(gui);
    }

    private record GuiProps(AbstractPneumaticCraftScreen gui) implements IGuiProperties {
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
