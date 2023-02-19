package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.api.lib.NBTKeys;
import me.desht.pneumaticcraft.common.block.entity.ChargingStationBlockEntity;
import me.desht.pneumaticcraft.common.core.ModBlockEntities;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class ChargingStationBlock extends AbstractCamouflageBlock implements PneumaticCraftEntityBlock {
    public static final BooleanProperty CHARGE_PAD = BooleanProperty.create("charge_pad");

    private static final VoxelShape CHARGING_STATION_N = Stream.of(
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
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

    private static final VoxelShape CHARGING_STATION_E = VoxelShapeUtils.rotateY(CHARGING_STATION_N, 90);
    private static final VoxelShape CHARGING_STATION_S = VoxelShapeUtils.rotateY(CHARGING_STATION_E, 90);
    private static final VoxelShape CHARGING_STATION_W = VoxelShapeUtils.rotateY(CHARGING_STATION_S, 90);

    private static final VoxelShape CHARGING_PAD_N = Stream.of(
            Block.box(4, 15.05, 4, 12, 16.05, 12),
            Block.box(2, 2, 13, 3, 15, 14),
            Block.box(13, 2, 13, 14, 15, 14),
            Block.box(13, 2, 2, 14, 15, 3),
            Block.box(2, 2, 2, 3, 15, 3),
            Block.box(2, 15, 2, 14, 16, 14),
            Block.box(2, 13, 3, 3, 14, 13),
            Block.box(13, 13, 3, 14, 14, 13),
            Block.box(3, 13, 13, 13, 14, 14),
            Block.box(3, 13, 2, 13, 14, 3),
            Block.box(5, 12.75, 0.25, 11, 16.05, 4),
            Block.box(5.7, 11.2, 1.7, 6.3, 12.8, 2.3),
            Block.box(6.95, 11.2, 1.7, 7.55, 12.8, 2.3),
            Block.box(11.45, 9.2, 1.2, 12.05, 13.8, 1.8),
            Block.box(10.95, 9.2, 1.2, 11.55, 9.8, 1.8),
            Block.box(10.95, 13.2, 1.2, 11.55, 13.8, 1.8)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

    private static final VoxelShape CHARGING_PAD_E = VoxelShapeUtils.rotateY(CHARGING_PAD_N, 90);
    private static final VoxelShape CHARGING_PAD_S = VoxelShapeUtils.rotateY(CHARGING_PAD_E, 90);
    private static final VoxelShape CHARGING_PAD_W = VoxelShapeUtils.rotateY(CHARGING_PAD_S, 90);

    private static final VoxelShape CHARGING_STATION_WITH_PAD_N = Shapes.join(CHARGING_STATION_N, CHARGING_PAD_N, BooleanOp.OR);
    private static final VoxelShape CHARGING_STATION_WITH_PAD_E = Shapes.join(CHARGING_STATION_E, CHARGING_PAD_E, BooleanOp.OR);
    private static final VoxelShape CHARGING_STATION_WITH_PAD_S = Shapes.join(CHARGING_STATION_S, CHARGING_PAD_S, BooleanOp.OR);
    private static final VoxelShape CHARGING_STATION_WITH_PAD_W = Shapes.join(CHARGING_STATION_W, CHARGING_PAD_W, BooleanOp.OR);

    private static final VoxelShape CHARGING_STATION_WITH_PAD_N_COLL = Shapes.join(CHARGING_STATION_WITH_PAD_N, Shapes.block(), BooleanOp.AND);
    private static final VoxelShape CHARGING_STATION_WITH_PAD_E_COLL = Shapes.join(CHARGING_STATION_WITH_PAD_E, Shapes.block(), BooleanOp.AND);
    private static final VoxelShape CHARGING_STATION_WITH_PAD_S_COLL = Shapes.join(CHARGING_STATION_WITH_PAD_S, Shapes.block(), BooleanOp.AND);
    private static final VoxelShape CHARGING_STATION_WITH_PAD_W_COLL = Shapes.join(CHARGING_STATION_WITH_PAD_W, Shapes.block(), BooleanOp.AND);

    private static final VoxelShape[] CHARGING_STATION = new VoxelShape[] { CHARGING_STATION_S, CHARGING_STATION_W, CHARGING_STATION_N, CHARGING_STATION_E };
    private static final VoxelShape[] CHARGING_STATION_WITH_PAD = new VoxelShape[] { CHARGING_STATION_WITH_PAD_S, CHARGING_STATION_WITH_PAD_W, CHARGING_STATION_WITH_PAD_N, CHARGING_STATION_WITH_PAD_E };
    private static final VoxelShape[] CHARGING_STATION_WITH_PAD_COLL = new VoxelShape[] { CHARGING_STATION_WITH_PAD_S_COLL, CHARGING_STATION_WITH_PAD_W_COLL, CHARGING_STATION_WITH_PAD_N_COLL, CHARGING_STATION_WITH_PAD_E_COLL };

    public ChargingStationBlock() {
        super(ModBlocks.defaultProps());
        registerDefaultState(getStateDefinition().any().setValue(CHARGE_PAD, false));
    }

    @Override
    protected boolean isWaterloggable() {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CHARGE_PAD);
    }

    @Override
    public VoxelShape getUncamouflagedShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext selectionContext) {
        Direction d = state.getValue(directionProperty());
        return state.getValue(CHARGE_PAD) ? CHARGING_STATION_WITH_PAD[d.get2DDataValue()] : CHARGING_STATION[d.get2DDataValue()];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext ctx) {
        // avoids confusing entities, since the selection shape can extend into the block above, preventing pathfinding above the block
        Direction d = state.getValue(directionProperty());
        return state.getValue(CHARGE_PAD) ? CHARGING_STATION_WITH_PAD_COLL[d.get2DDataValue()] : CHARGING_STATION[d.get2DDataValue()];
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
    public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return blockAccess.getBlockEntity(pos, ModBlockEntities.CHARGING_STATION.get())
                .map(teCS -> teCS.getRedstoneController().shouldEmit() ? 15 : 0).orElse(0);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ChargingStationBlockEntity(pPos, pState);
    }

    public static class ItemBlockChargingStation extends BlockItem {
        public ItemBlockChargingStation(Block blockIn) {
            super(blockIn, ModItems.defaultProps());
        }

        @Override
        public String getDescriptionId(ItemStack stack) {
            CompoundTag tag = stack.getTagElement(NBTKeys.BLOCK_ENTITY_TAG);
            if (tag != null && tag.getBoolean("UpgradeOnly")) {
                return super.getDescriptionId(stack) + ".upgrade_only";
            } else {
                return super.getDescriptionId(stack);
            }
        }
    }
}
