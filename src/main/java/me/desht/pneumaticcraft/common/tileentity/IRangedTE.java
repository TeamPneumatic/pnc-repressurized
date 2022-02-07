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

package me.desht.pneumaticcraft.common.tileentity;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

/**
 * Represents a tile entity which has a range manager and the concept of a range area of effect.
 */
@FunctionalInterface
public interface IRangedTE {
    RangeManager getRangeManager();

    /**
     * Text to be displayed on the range toggle GUI button
     * @return a text component
     */
    default Component rangeText() {
        return new TextComponent("R").withStyle(getRangeManager().shouldShowRange() ? ChatFormatting.AQUA : ChatFormatting.GRAY);
    }

    default int getRange() {
        return getRangeManager().getRange();
    }
}
