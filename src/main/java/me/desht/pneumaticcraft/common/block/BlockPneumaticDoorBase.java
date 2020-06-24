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
        TileEntityPneumaticDoorBase doorBase = (TileEntityPneumaticDoorBase) world.getTileEntity(pos);
        updateDoorSide(doorBase);
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        PneumaticCraftUtils.getTileEntityAt(world, pos, TileEntityPneumaticDoorBase.class).ifPresent(teDoorBase -> {
            updateDoorSide(teDoorBase);
            Direction dir = teDoorBase.getRotation();
            if (world.getBlockState(pos.offset(dir)).getBlock() == ModBlocks.PNEUMATIC_DOOR.get()) {
                ModBlocks.PNEUMATIC_DOOR.get().neighborChanged(world.getBlockState(pos.offset(dir)), world, pos, block, pos.offset(dir), isMoving);
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
}
