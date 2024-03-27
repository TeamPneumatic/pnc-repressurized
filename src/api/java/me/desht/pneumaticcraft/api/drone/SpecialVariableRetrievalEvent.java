/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.api.drone;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

import javax.annotation.Nullable;

/**
 * Fired when a Drone is trying to get a special coordinate, by accessing a variable with '$' prefix.
 * These event are posted on the MinecraftForge.EVENT_BUS.
 */
public abstract class SpecialVariableRetrievalEvent extends Event {
    /**
     * The special variable name, with the '$' stripped away.
     */
    public final String specialVarName;

    SpecialVariableRetrievalEvent(String specialVarName) {
        this.specialVarName = specialVarName;
    }

    public static abstract class CoordinateVariable extends SpecialVariableRetrievalEvent {
        private BlockPos coordinate;

        CoordinateVariable(String specialVarName) {
            super(specialVarName);
        }

        public BlockPos getCoordinate() {
            return coordinate;
        }

        /**
         * Update the blockpos coordinate for the special variable that was passed.
         *
         * @param coordinate the new coordinate; passing null is equivalent to passing BlockPos.ZERO
         */
        public void setCoordinate(@Nullable BlockPos coordinate) {
            this.coordinate = coordinate;
        }

        public static class Drone extends CoordinateVariable {
            public final IDrone drone;

            public Drone(IDrone drone, String specialVarName) {
                super(specialVarName);
                this.drone = drone;
            }
        }
    }

    public static abstract class ItemVariable extends SpecialVariableRetrievalEvent {
        private final ItemStack item = ItemStack.EMPTY;

        ItemVariable(String specialVarName) {
            super(specialVarName);
        }

        public ItemStack getItem() {
            return item;
        }

        public static class Drone extends ItemVariable {
            public final IDrone drone;

            public Drone(IDrone drone, String specialVarName) {
                super(specialVarName);
                this.drone = drone;
            }
        }
    }
}
