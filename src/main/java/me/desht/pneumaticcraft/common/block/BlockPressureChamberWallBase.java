package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.advancements.AdvancementTriggers;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberValve;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberWall;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockPressureChamberWallBase extends BlockPneumaticCraft implements IBlockPressureChamber {
    BlockPressureChamberWallBase(String registryName) {
        super(Material.IRON, registryName);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityPressureChamberWall.class;
    }

    @Override
    public void onBlockPlacedBy(World par1World, BlockPos pos, BlockState state, LivingEntity par5EntityLiving, ItemStack iStack) {
        super.onBlockPlacedBy(par1World, pos, state, par5EntityLiving, iStack);
        if (TileEntityPressureChamberValve.checkIfProperlyFormed(par1World, pos) && par5EntityLiving instanceof ServerPlayerEntity) {
            AdvancementTriggers.PRESSURE_CHAMBER.trigger((ServerPlayerEntity) par5EntityLiving);
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction side, float par7, float par8, float par9) {
        if (world.isRemote) return false;
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityPressureChamberWall) {
            TileEntityPressureChamberValve valve = ((TileEntityPressureChamberWall) te).getCore();
            if (valve != null) {
                return valve.getBlockType().onBlockActivated(world, valve.getPos(), world.getBlockState(valve.getPos()), player, hand, side, par7, par8, par9);
            }
        }
        return false;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, BlockState state) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityPressureChamberWall && !world.isRemote) {
            ((TileEntityPressureChamberWall) te).onBlockBreak();
        }
        super.breakBlock(world, pos, state);

    }

}
