package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.advancements.AdvancementTriggers;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberValve;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberWall;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

public class BlockPressureChamberWallBase extends BlockPneumaticCraft implements IBlockPressureChamber {
    BlockPressureChamberWallBase(Block.Properties props, String registryName) {
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
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult brtr) {
        if (world.isRemote) return false;
        // forward activation to the pressure chamber valve, which will open the GUI
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityPressureChamberWall) {
            TileEntityPressureChamberValve valve = ((TileEntityPressureChamberWall) te).getCore();
            if (valve != null) {
                return valve.getBlockState().onBlockActivated(world, player, hand, brtr);
            }
        }
        return false;
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityPressureChamberWall && !world.isRemote) {
            ((TileEntityPressureChamberWall) te).onBlockBreak();
        }
        super.onReplaced(state, world, pos, newState, isMoving);
    }
}
