package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.api.crafting.recipe.RefineryRecipe;
import me.desht.pneumaticcraft.common.block.entity.RefineryControllerBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.RefineryOutputBlockEntity;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

public class RefineryOutputBlock extends AbstractPneumaticCraftBlock
        implements PneumaticCraftEntityBlock, IBlockComparatorSupport
{
    private static final VoxelShape SHAPE_N = VoxelShapeUtils.or(
            Block.box(0, 0, 0, 16, 1, 16),
            Block.box(13, 11, 14.5, 14.5, 12, 15.5),
            Block.box(12, 1, 14, 13, 13, 16),
            Block.box(3, 1, 14, 4, 13, 16),
            Block.box(1.5, 11, 14.5, 3, 12, 15.5),
            Block.box(0, 10, 14, 2, 13, 16),
            Block.box(14, 10, 14, 16, 13, 16),
            Block.box(14.5, 1, 14.5, 15.5, 15, 15.5),
            Block.box(14.5, 1, 0.5, 15.5, 15, 1.5),
            Block.box(14, 10, 0, 16, 13, 2),
            Block.box(1.5, 11, 0.5, 14.5, 12, 1.5),
            Block.box(2, 1, 1, 14, 16, 13),
            Block.box(4, 1, 13, 12, 14, 16),
            Block.box(0, 15, 15, 16, 16, 16),
            Block.box(0, 15, 0, 16, 16, 1),
            Block.box(0, 15, 1, 1, 16, 3),
            Block.box(0, 15, 6, 1, 16, 15),
            Block.box(15, 15, 1, 16, 16, 7),
            Block.box(15, 15, 11, 16, 16, 15),
            Block.box(0.5, 11, 1.5, 1.5, 12, 14.5),
            Block.box(0.5, 1, 0.5, 1.5, 15, 1.5),
            Block.box(0.5, 1, 14.5, 1.5, 15, 15.5),
            Block.box(0, 10, 0, 2, 13, 2),
            Block.box(14, 14, 7, 16, 16, 11),
            Block.box(13.25, 7.5, 9.25, 14.25, 9.5, 11.25),
            Block.box(13.25, 7.5, 6.75, 14.25, 9.5, 8.75),
            Block.box(0, 13, 3, 2, 16, 6),
            Block.box(0, 3.5, 3, 2, 5.5, 6),
            Block.box(13.5, 8, 7.25, 15.5, 9, 8.25),
            Block.box(13.5, 8, 9.75, 15.5, 9, 10.75),
            Block.box(14.5, 1, 7.25, 15.5, 8, 8.25),
            Block.box(14.5, 1, 9.75, 15.5, 8, 10.75),
            Block.box(0.5, 1, 3.5, 2.5, 4, 5.5),
            Block.box(14.5, 11, 1.5, 15.5, 12, 14.5)
    );

    private static final VoxelShape SHAPE_E = VoxelShapeUtils.rotateY(SHAPE_N, 90);
    private static final VoxelShape SHAPE_S = VoxelShapeUtils.rotateY(SHAPE_E, 90);
    private static final VoxelShape SHAPE_W = VoxelShapeUtils.rotateY(SHAPE_S, 90);
    private static final VoxelShape[] SHAPES = new VoxelShape[] { SHAPE_S, SHAPE_W, SHAPE_N, SHAPE_E };

    public RefineryOutputBlock() {
        super(ModBlocks.defaultProps());
    }

    @Override
    protected boolean isWaterloggable() {
        return true;
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult brtr) {
        return world.getBlockEntity(pos, ModBlockEntityTypes.REFINERY_OUTPUT.get()).map(te -> {
            // normally, activating any refinery output block would open the controller BE's gui, but if we
            // activate with a fluid tank in hand (which can actually transfer fluid out),
            // then we must activate the actual refinery output that was clicked
            boolean canTransferFluid = FluidUtil.getFluidHandler(ItemHandlerHelper.copyStackWithSize(player.getItemInHand(hand), 1))
                    .map(heldHandler -> FluidUtil.getFluidHandler(world, pos, brtr.getDirection())
                            .map(refineryHandler -> couldTransferFluidOut(heldHandler, refineryHandler))
                            .orElse(false))
                    .orElse(false);
            if (canTransferFluid) {
                return super.use(state, world, pos, player, hand, brtr);
            } else if (player instanceof ServerPlayer sp) {
                RefineryControllerBlockEntity master = te.getRefineryController();
                if (master != null) {
                    sp.openMenu(master, master.getBlockPos());
                }
            }
            return InteractionResult.SUCCESS;
        }).orElse(InteractionResult.PASS);
    }

    private boolean couldTransferFluidOut(IFluidHandler h1, IFluidHandler h2) {
        FluidStack f = FluidUtil.tryFluidTransfer(h1, h2, 1000, false);
        return !f.isEmpty();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        Direction d = state.getValue(directionProperty());
        return SHAPES[d.get2DDataValue()];
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        int nOutputs = 0;
        int up = 1, down = 1;
        while (worldIn.getBlockState(pos.above(up++)).getBlock() instanceof RefineryOutputBlock) {
            nOutputs++;
        }
        while (worldIn.getBlockState(pos.below(down++)).getBlock() instanceof RefineryOutputBlock) {
            nOutputs++;
        }
        return nOutputs < RefineryRecipe.MAX_OUTPUTS  && super.canSurvive(state, worldIn, pos);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (!worldIn.isClientSide() && facingState.getBlock() == ModBlocks.REFINERY_OUTPUT.get()) {
            recache(worldIn, currentPos);
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    private void recache(LevelAccessor world, BlockPos pos) {
        PneumaticCraftUtils.getTileEntityAt(world, pos, RefineryOutputBlockEntity.class).ifPresent(te -> {
            RefineryControllerBlockEntity teC = te.getRefineryController();
            if (teC != null) teC.clearOutputCache();
        });
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new RefineryOutputBlockEntity(pPos, pState);
    }
}
