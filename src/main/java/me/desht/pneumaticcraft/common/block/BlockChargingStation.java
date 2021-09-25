package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.api.lib.NBTKeys;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

import java.util.stream.Stream;

public class BlockChargingStation extends BlockPneumaticCraftCamo {
    public static final BooleanProperty CHARGE_PAD = BooleanProperty.create("charge_pad");

//    private static final VoxelShape PAD_FRAME = Block.box(3, 1, 3, 13, 16, 13);

    private static final VoxelShape SHAPE_N = Stream.of(
        Block.box(0, 0, 0, 16, 1, 16),
        Block.box(1, 1, 1, 15, 3, 15),
        Block.box(6, 6, 0, 10, 10, 1),
        Block.box(5, 3, 1, 11, 11, 3),
        Block.box(3, 3, 3, 13, 4, 13),
        Block.box(11, 7.25, 1.25, 12.5, 7.75, 1.75),
        Block.box(12, 3.25, 1.25, 12.5, 7.25, 1.75),
        Block.box(11.25, 3.25, 2, 11.75, 8.25, 2.5),
        Block.box(11.25, 3.25, 2.5, 11.75, 3.75, 3),
        Block.box(12, 3.25, 1.75, 12.5, 3.75, 3.25),
        Block.box(3.5, 7.25, 1.25, 5, 7.75, 1.75),
        Block.box(3.5, 3.25, 1.75, 4, 3.75, 3.25),
        Block.box(10.75, 8.25, 2, 11.75, 8.75, 2.5),
        Block.box(3.5, 3.25, 1.25, 4, 7.25, 1.75),
        Block.box(4.25, 8.25, 2, 5.25, 8.75, 2.5),
        Block.box(4.25, 3.25, 2, 4.75, 8.25, 2.5),
        Block.box(4.25, 3.25, 2.5, 4.75, 3.75, 3),
        Block.box(5, 1, 0, 11, 5, 1)
).reduce((v1, v2) -> VoxelShapes.join(v1, v2, IBooleanFunction.OR)).get();

    private static final VoxelShape SHAPE_E = VoxelShapeUtils.rotateY(SHAPE_N, 90);
    private static final VoxelShape SHAPE_S = VoxelShapeUtils.rotateY(SHAPE_E, 90);
    private static final VoxelShape SHAPE_W = VoxelShapeUtils.rotateY(SHAPE_S, 90);
    private static final VoxelShape[] SHAPES = new VoxelShape[] { SHAPE_S, SHAPE_W, SHAPE_N, SHAPE_E };

//    private static final VoxelShape PAD_SHAPE = VoxelShapes.join(SHAPES, PAD_FRAME, IBooleanFunction.OR);

    public BlockChargingStation() {
        super(ModBlocks.defaultProps());
        registerDefaultState(getStateDefinition().any().setValue(CHARGE_PAD, false));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CHARGE_PAD);
    }

    @Override
    public VoxelShape getUncamouflagedShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext selectionContext) {
        Direction d = state.getValue(directionProperty());
        return SHAPES[d.get2DDataValue()];
//        return state.getValue(CHARGE_PAD) ? PAD_SHAPE : SHAPE;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityChargingStation.class;
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        return PneumaticCraftUtils.getTileEntityAt(blockAccess, pos, TileEntityChargingStation.class)
                .map(teCS -> teCS.getRedstoneController().shouldEmit() ? 15 : 0).orElse(0);
    }

    public static class ItemBlockChargingStation extends BlockItem {
        public ItemBlockChargingStation(Block blockIn) {
            super(blockIn, ModItems.defaultProps());
        }

        @Override
        public String getDescriptionId(ItemStack stack) {
            CompoundNBT tag = stack.getTagElement(NBTKeys.BLOCK_ENTITY_TAG);
            if (tag != null && tag.getBoolean("UpgradeOnly")) {
                return super.getDescriptionId(stack) + ".upgrade_only";
            } else {
                return super.getDescriptionId(stack);
            }
        }

//        @Override
//        public ITextComponent getDisplayName(ItemStack stack) {
//            CompoundNBT tag = stack.getChildTag(NBTKeys.BLOCK_ENTITY_TAG);
//            if (tag != null && tag.getBoolean("UpgradeOnly")) {
//                return super.getDisplayName(stack).deepCopy().appendString(" ").append(xlate("pneumaticcraft.gui.tooltip.charging_station.upgradesOnly"));
//            } else {
//                return super.getDisplayName(stack);
//            }
//        }
    }
}
