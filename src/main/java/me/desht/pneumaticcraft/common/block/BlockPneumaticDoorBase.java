package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDoor;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDoorBase;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockPneumaticDoorBase extends BlockPneumaticCraftCamo {

    public BlockPneumaticDoorBase(Properties props) {
        super(props);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityPneumaticDoorBase.class;
    }

    /**
     * Called when the block is placed in the world.
     */
    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, entity, stack);
        TileEntityPneumaticDoorBase doorBase = (TileEntityPneumaticDoorBase) world.getTileEntity(pos);
        updateDoorSide(doorBase);
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean b) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityPneumaticDoorBase) {
            updateDoorSide((TileEntityPneumaticDoorBase) te);
            Direction dir = ((TileEntityPneumaticDoorBase) te).getRotation();
            if (world.getBlockState(pos.offset(dir)).getBlock() == ModBlocks.PNEUMATIC_DOOR.get()) {
                ModBlocks.PNEUMATIC_DOOR.get().neighborChanged(world.getBlockState(pos.offset(dir)), world, pos, block, pos.offset(dir), b);
            }
        }
    }

    private void updateDoorSide(TileEntityPneumaticDoorBase doorBase) {
        TileEntity teDoor = doorBase.getWorld().getTileEntity(doorBase.getPos().offset(doorBase.getRotation()));
        if (teDoor instanceof TileEntityPneumaticDoor) {
            TileEntityPneumaticDoor door = (TileEntityPneumaticDoor) teDoor;
            if (doorBase.getRotation().rotateY() == door.getRotation() && door.rightGoing
                    || doorBase.getRotation().rotateYCCW() == door.getRotation() && !door.rightGoing) {
                door.rightGoing = !door.rightGoing;
                door.setRotationAngle(0);
                door.markDirty();
            }
        }
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    protected boolean canRotateToTopOrBottom() {
        return false;
    }

    @Override
    protected boolean reversePlacementRotation() {
        return true;
    }
}
