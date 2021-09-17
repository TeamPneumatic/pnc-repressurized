package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.api.crafting.recipe.RefineryRecipe;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntityRefineryController;
import me.desht.pneumaticcraft.common.tileentity.TileEntityRefineryOutput;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.stream.Stream;

public class BlockRefineryOutput extends BlockPneumaticCraft {
    private static final VoxelShape SHAPE_N = Stream.of(
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
    ).reduce((v1, v2) -> {return VoxelShapes.join(v1, v2, IBooleanFunction.OR);}).get();

    private static final VoxelShape SHAPE_E = VoxelShapeUtils.rotateY(SHAPE_N, 90);
    private static final VoxelShape SHAPE_S = VoxelShapeUtils.rotateY(SHAPE_E, 90);
    private static final VoxelShape SHAPE_W = VoxelShapeUtils.rotateY(SHAPE_S, 90);
    private static final VoxelShape[] SHAPES = new VoxelShape[] { SHAPE_S, SHAPE_W, SHAPE_N, SHAPE_E };

    public BlockRefineryOutput() {
        super(ModBlocks.defaultProps());
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityRefineryOutput.class;
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult brtr) {
        return PneumaticCraftUtils.getTileEntityAt(world, pos, TileEntityRefineryOutput.class).map(te -> {
            // normally, activating any refinery block would open the controller TE's gui, but if we
            // activate with a fluid tank in hand (which can actually transfer fluid out),
            // then we must activate the actual refinery output that was clicked
            boolean canTransferFluid = FluidUtil.getFluidHandler(ItemHandlerHelper.copyStackWithSize(player.getItemInHand(hand), 1))
                    .map(heldHandler -> FluidUtil.getFluidHandler(world, pos, brtr.getDirection())
                            .map(refineryHandler -> couldTransferFluidOut(heldHandler, refineryHandler))
                            .orElse(false))
                    .orElse(false);
            if (canTransferFluid) {
                return super.use(state, world, pos, player, hand, brtr);
            } else if (!world.isClientSide) {
                TileEntityRefineryController master = te.getRefineryController();
                if (master != null) {
                    NetworkHooks.openGui((ServerPlayerEntity) player, master, master.getBlockPos());
                }
            }
            return ActionResultType.SUCCESS;
        }).orElse(ActionResultType.PASS);
    }

    private boolean couldTransferFluidOut(IFluidHandler h1, IFluidHandler h2) {
        FluidStack f = FluidUtil.tryFluidTransfer(h1, h2, 1000, false);
        return !f.isEmpty();
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        Direction d = state.getValue(directionProperty());
        return SHAPES[d.get2DDataValue()];
    }

    @Override
    public boolean canSurvive(BlockState state, IWorldReader worldIn, BlockPos pos) {
        int nOutputs = 0;
        int up = 1, down = 1;
        while (worldIn.getBlockState(pos.above(up++)).getBlock() instanceof BlockRefineryOutput) {
            nOutputs++;
        }
        while (worldIn.getBlockState(pos.below(down++)).getBlock() instanceof BlockRefineryOutput) {
            nOutputs++;
        }
        return nOutputs < RefineryRecipe.MAX_OUTPUTS  && super.canSurvive(state, worldIn, pos);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (!worldIn.isClientSide() && facingState.getBlock() == ModBlocks.REFINERY_OUTPUT.get()) {
            recache(worldIn, currentPos);
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    private void recache(IWorld world, BlockPos pos) {
        PneumaticCraftUtils.getTileEntityAt(world, pos, TileEntityRefineryOutput.class).ifPresent(te -> {
            TileEntityRefineryController teC = te.getRefineryController();
            if (teC != null) teC.cacheRefineryOutputs();
        });
    }
}
