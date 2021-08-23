package me.desht.pneumaticcraft.common.dispenser;

import me.desht.pneumaticcraft.common.item.ItemDrone;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class BehaviorDispenseDrone extends DefaultDispenseItemBehavior {
    @Override
    protected ItemStack execute(IBlockSource source, ItemStack stack){
        Direction facing = source.getBlockState().getValue(DispenserBlock.FACING);
        BlockPos placePos = source.getPos().relative(facing);
        ((ItemDrone)stack.getItem()).spawnDrone(null, source.getLevel(), null, null, placePos, stack);
        
        stack.shrink(1);
        return stack;
    }
}
