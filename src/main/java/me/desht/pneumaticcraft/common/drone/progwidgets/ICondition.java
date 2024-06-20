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

import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public interface ICondition {
    boolean isAndFunction();

    void setAndFunction(boolean isAndFunction);

    int getRequiredCount();

    void setRequiredCount(int count);

    Operator getOperator();

    void setOperator(Operator operator);

    String getMeasureVar();

    void setMeasureVar(String var);

    default void maybeRecordMeasuredVal(IDrone drone, int val) {
        if (!getMeasureVar().isEmpty() && drone instanceof IDroneBase droneBase) {
            droneBase.getAIManager().setCoordinate(getMeasureVar(), new BlockPos(val, 0, 0));
        }
    }

    /**
     * Used in the CC compatibility.
     *
     * @param drone the drone
     * @return evaluation result
     */
    boolean evaluate(IDrone drone, IProgWidget widget);

    enum Operator implements StringRepresentable {
        EQ("="), GE(">="), LE("<=");

        private final String symbol;

        Operator(String symbol) {
            this.symbol = symbol;
        }

        public boolean evaluate(int count1, int count2) {
            return switch (this) {
                case EQ -> count1 == count2;
                case GE -> count1 >= count2;
                case LE -> count1 <= count2;
            };
        }

        public boolean evaluate(float count1, float count2) {
            return switch (this) {
                case EQ -> count1 == count2;
                case GE -> count1 >= count2;
                case LE -> count1 <= count2;
            };
        }

        @Override
        public String toString() {
            return symbol;
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
