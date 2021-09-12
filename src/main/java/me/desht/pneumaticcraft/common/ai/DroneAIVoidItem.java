package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.progwidgets.IItemFiltering;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

public class DroneAIVoidItem extends Goal {
    private final IDroneBase drone;
    private final IItemFiltering widget;

    public DroneAIVoidItem(IDroneBase drone, IItemFiltering widget) {
        this.drone = drone;
        this.widget = widget;
    }

    @Override
    public boolean canUse() {
        for (int i = 0; i < drone.getInv().getSlots(); i++) {
            ItemStack stack = drone.getInv().getStackInSlot(i);
            if (!stack.isEmpty() && widget.isItemValidForFilters(stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void start() {
        for (int i = 0; i < drone.getInv().getSlots(); i++) {
            ItemStack stack = drone.getInv().getStackInSlot(i);
            if (!stack.isEmpty() && widget.isItemValidForFilters(stack)) {
                drone.getInv().setStackInSlot(i, ItemStack.EMPTY);
                drone.addAirToDrone(-PneumaticValues.DRONE_USAGE_VOID * stack.getCount());
                if (drone.world() instanceof ServerWorld) {
                    Vector3d vec = drone.getDronePos();
                    ((ServerWorld)drone.world()).sendParticles(ParticleTypes.LAVA, vec.x, vec.y, vec.z, 5, 0, 0, 0, 0);
                }
            }
        }
    }
}
