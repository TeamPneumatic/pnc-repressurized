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

package me.desht.pneumaticcraft.common.dispenser;

import me.desht.pneumaticcraft.common.item.DroneItem;
import me.desht.pneumaticcraft.common.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;

public class DroneDispenseBehavior extends DefaultDispenseItemBehavior {
    private static final DroneDispenseBehavior DRONE_DISPENSE = new DroneDispenseBehavior();

    public static void registerDrones() {
        DispenserBlock.registerBehavior(ModItems.DRONE.get(), DRONE_DISPENSE);
        DispenserBlock.registerBehavior(ModItems.LOGISTICS_DRONE.get(), DRONE_DISPENSE);
        DispenserBlock.registerBehavior(ModItems.HARVESTING_DRONE.get(), DRONE_DISPENSE);
        DispenserBlock.registerBehavior(ModItems.GUARD_DRONE.get(), DRONE_DISPENSE);
        DispenserBlock.registerBehavior(ModItems.COLLECTOR_DRONE.get(), DRONE_DISPENSE);
    }

    @Override
    protected ItemStack execute(BlockSource source, ItemStack stack) {
        Direction facing = source.state().getValue(DispenserBlock.FACING);
        BlockPos placePos = source.pos().relative(facing);
        // set the "click" pos to a possible block 2 blocks in front of the dispenser
        // allows drones to pull items from chest where needed (e.g. guard drone get ammo, harvest drone get hoe...)
        ((DroneItem)stack.getItem()).spawnDrone(null, source.level(), source.pos().relative(facing, 2), facing.getOpposite(), placePos, stack);
        
        stack.shrink(1);
        return stack;
    }
}
