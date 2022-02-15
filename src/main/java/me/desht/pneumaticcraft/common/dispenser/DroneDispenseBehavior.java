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
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;

public class DroneDispenseBehavior extends DefaultDispenseItemBehavior {
    @Override
    protected ItemStack execute(BlockSource source, ItemStack stack){
        Direction facing = source.getBlockState().getValue(DispenserBlock.FACING);
        BlockPos placePos = source.getPos().relative(facing);
        ((DroneItem)stack.getItem()).spawnDrone(null, source.getLevel(), null, null, placePos, stack);
        
        stack.shrink(1);
        return stack;
    }
}
