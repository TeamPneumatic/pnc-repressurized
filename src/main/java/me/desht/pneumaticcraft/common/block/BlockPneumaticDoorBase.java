package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDoor;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDoorBase;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockPneumaticDoorBase extends BlockPneumaticCraftCamo {

    public BlockPneumaticDoorBase() {
        super(ModBlocks.defaultProps());
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
        PneumaticCraftUtils.getTileEntityAt(world, pos, TileEntityPneumaticDoorBase.class).ifPresent(this::updateDoorSide);
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        PneumaticCraftUtils.getTileEntityAt(world, pos, TileEntityPneumaticDoorBase.class).ifPresent(teDoorBase -> {
            updateDoorSide(teDoorBase);
            teDoorBase.onNeighborBlockUpdate(fromPos);
            BlockPos doorPos = pos.offset(teDoorBase.getRotation());
            BlockState doorState = world.getBlockState(doorPos);
            if (doorState.getBlock() instanceof BlockPneumaticDoor) {
                doorState.neighborChanged(world, doorPos, doorState.getBlock(), pos, false);
            }
        });
    }

    private void updateDoorSide(TileEntityPneumaticDoorBase doorBase) {
        PneumaticCraftUtils.getTileEntityAt(doorBase.getWorld(), doorBase.getPos().offset(doorBase.getRotation()), TileEntityPneumaticDoor.class)
                .ifPresent(teDoor -> {
                    if (doorBase.getRotation().rotateY() == teDoor.getRotation() && teDoor.rightGoing
                            || doorBase.getRotation().rotateYCCW() == teDoor.getRotation() && !teDoor.rightGoing) {
                        teDoor.rightGoing = !teDoor.rightGoing;
                        teDoor.setRotationAngle(0);
                        teDoor.markDirty();
                    }
                });
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

    @Override
    public VoxelShape getUncamouflagedShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext ctx) {
        return VoxelShapes.fullCube();
    }

    @Override
    public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        return PneumaticCraftUtils.getTileEntityAt(blockAccess, pos, TileEntityPneumaticDoorBase.class)
                .map(te -> te.shouldPassSignalToDoor() && side == te.getRotation().getOpposite() ? te.getCurrentRedstonePower() : 0)
                .orElse(0);
    }
}
