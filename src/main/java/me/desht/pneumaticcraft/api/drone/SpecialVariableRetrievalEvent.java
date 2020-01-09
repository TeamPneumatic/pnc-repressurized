package me.desht.pneumaticcraft.api.drone;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.eventbus.api.Event;

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

        public void setCoordinate(BlockPos coordinate) {
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
        private ItemStack item = ItemStack.EMPTY;

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
