package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAerialInterface;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockAerialInterface extends BlockPneumaticCraft {
    public BlockAerialInterface() {
        super(ModBlocks.defaultProps());
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityAerialInterface.class;
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    protected boolean reversePlacementRotation() {
        return false;
    }

    @Override
    public void onBlockPlacedBy(World par1World, BlockPos pos, BlockState state, LivingEntity entity, ItemStack par6ItemStack) {
        TileEntity te = par1World.getTileEntity(pos);
        if (te instanceof TileEntityAerialInterface && entity instanceof PlayerEntity) {
            ((TileEntityAerialInterface) te).setPlayer(((PlayerEntity) entity));
        }
        super.onBlockPlacedBy(par1World, pos, state, entity, par6ItemStack);
    }

    @Override
    public boolean canProvidePower(BlockState state) {
        return true;
    }

    @Override
    public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        TileEntity te = blockAccess.getTileEntity(pos);
        if (te instanceof TileEntityAerialInterface) {
            return ((TileEntityAerialInterface)te ).shouldEmitRedstone() ? 15 : 0;
        }
        return 0;
    }
}
