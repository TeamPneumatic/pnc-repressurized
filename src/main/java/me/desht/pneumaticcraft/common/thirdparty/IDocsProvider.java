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

package me.desht.pneumaticcraft.common.thirdparty;

import net.minecraft.network.chat.Component;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public interface IDocsProvider {
    default void showWidgetDocs(String path) {
        showDocsPage("programming/" + path);
    }

    void showDocsPage(String path);

    default void addTooltip(List<Component> tooltip, boolean showingAll) {
        tooltip.add(xlate(showingAll ? "pneumaticcraft.gui.programmer.pressIForInfoTrayOpen" : "pneumaticcraft.gui.programmer.pressIForInfo"));
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    default boolean isInstalled() {
        return false;
    }

    class NoDocsProvider implements IDocsProvider {
        @Override
        public void showDocsPage(String path) {
        }

        @Override
        public void addTooltip(List<Component> tooltip, boolean showingAll) {
        }
    }
}
