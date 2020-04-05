package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.advancements.AdvancementTriggers;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberValve;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class BlockPressureChamberValve extends BlockPneumaticCraft implements IBlockPressureChamber {
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");

    public BlockPressureChamberValve() {
        super(ModBlocks.defaultProps());
        setDefaultState(getStateContainer().getBaseState().with(FORMED, false));
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityPressureChamberValve.class;
    }

    @Override
    public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return ALMOST_FULL_SHAPE;
    }

    @Override
    public void onBlockPlacedBy(World par1World, BlockPos pos, BlockState state, LivingEntity par5EntityLiving, ItemStack iStack) {
        super.onBlockPlacedBy(par1World, pos, state, par5EntityLiving, iStack);
        if (!par1World.isRemote && TileEntityPressureChamberValve.checkIfProperlyFormed(par1World, pos)) {
            AdvancementTriggers.PRESSURE_CHAMBER.trigger((ServerPlayerEntity) par5EntityLiving);
        }
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    protected boolean canRotateToTopOrBottom() {
        return true;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(FORMED);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult brtr) {
        if (player.isSteppingCarefully()) {
            return ActionResultType.PASS;
        }
        TileEntity te = world.getTileEntity(pos);
        if (!world.isRemote && te instanceof TileEntityPressureChamberValve) {
            if (((TileEntityPressureChamberValve) te).multiBlockSize > 0) {
                NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) te, pos);
            } else if (((TileEntityPressureChamberValve) te).accessoryValves.size() > 0) {
                // when this isn't the core valve, track down the core valve
                //  System.out.println("size: " + ((TileEntityPressureChamberValve)te).accessoryValves.size());
                for (TileEntityPressureChamberValve valve : ((TileEntityPressureChamberValve) te).accessoryValves) {
                    if (valve.multiBlockSize > 0) {
                        NetworkHooks.openGui((ServerPlayerEntity) player, valve, valve.getPos());
                        break;
                    }
                }
            } else {
                return ActionResultType.PASS;
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            invalidateMultiBlock(world, pos);
        }
        super.onReplaced(state, world, pos, newState, isMoving);
    }

    private void invalidateMultiBlock(World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityPressureChamberValve && !world.isRemote) {
            if (((TileEntityPressureChamberValve) te).multiBlockSize > 0) {
                ((TileEntityPressureChamberValve) te).onMultiBlockBreak();
            } else if (((TileEntityPressureChamberValve) te).accessoryValves.size() > 0) {
                for (TileEntityPressureChamberValve valve : ((TileEntityPressureChamberValve) te).accessoryValves) {
                    if (valve.multiBlockSize > 0) {
                        valve.onMultiBlockBreak();
                        break;
                    }
                }
            }
        }
    }
}
