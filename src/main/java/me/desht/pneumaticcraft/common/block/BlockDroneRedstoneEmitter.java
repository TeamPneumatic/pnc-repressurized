package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.tileentity.TileEntityDroneRedstoneEmitter;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class BlockDroneRedstoneEmitter extends BlockAir {
    BlockDroneRedstoneEmitter() {
        super();
        setRegistryName("drone_redstone_emitter");
        setUnlocalizedName("drone_redstone_emitter");
        setCreativeTab(PneumaticCraftRepressurized.tabPneumaticCraft);
    }

    @Override
    public boolean canProvidePower(IBlockState state) {
        return true;
    }

    @Override
    public int getStrongPower(IBlockState state, IBlockAccess par1IBlockAccess, BlockPos pos, EnumFacing side) {
        return 0;
    }

    @Override
    public int getWeakPower(IBlockState state, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        if (blockAccess instanceof World) {
            World world = (World) blockAccess;
            List<EntityDrone> drones = world.getEntitiesWithinAABB(EntityDrone.class, new AxisAlignedBB(pos, pos.add(1, 1, 1)));
            int signal = 0;
            for (EntityDrone drone : drones) {
                signal = Math.max(signal, drone.getEmittingRedstone(side.getOpposite()));
            }
            return signal;
        } else {
            return 0;
        }
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityDroneRedstoneEmitter();
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        super.breakBlock(world, pos, state);
        world.removeTileEntity(pos);
    }
}