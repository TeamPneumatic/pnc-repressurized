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

package me.desht.pneumaticcraft.common.drone.progwidgets;

import me.desht.pneumaticcraft.api.misc.ITranslatableEnum;
import net.minecraft.util.StringRepresentable;

public interface IBlockOrdered {
    enum Ordering implements ITranslatableEnum, StringRepresentable {
        CLOSEST("closest"), LOW_TO_HIGH("lowToHigh"), HIGH_TO_LOW("highToLow");
        public final String name;

        Ordering(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public String getTranslationKey() {
            return "pneumaticcraft.gui.progWidget.blockOrder." + this;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    Ordering getOrder();

    void setOrder(Ordering order);
}
