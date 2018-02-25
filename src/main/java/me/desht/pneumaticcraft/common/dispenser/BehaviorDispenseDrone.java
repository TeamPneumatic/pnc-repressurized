package me.desht.pneumaticcraft.common.dispenser;

import me.desht.pneumaticcraft.common.item.ItemDrone;
import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class BehaviorDispenseDrone extends BehaviorDefaultDispenseItem{
    @Override
    protected ItemStack dispenseStack(IBlockSource source, ItemStack stack){
        EnumFacing facing = source.getBlockState().getValue(BlockDispenser.FACING);
        BlockPos placePos = source.getBlockPos().offset(facing);
        ((ItemDrone)stack.getItem()).spawnDrone(null, source.getWorld(), null, null, placePos, stack);
        
        stack.shrink(1);
        return stack;
    }
}
