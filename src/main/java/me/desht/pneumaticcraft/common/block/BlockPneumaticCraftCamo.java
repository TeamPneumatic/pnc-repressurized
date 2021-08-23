package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.ICamouflageableTE;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.util.Constants;

/**
 * Base class for blocks which may be camouflaged, storing the camouflaged block state in the
 * CAMO_STATE model property.
 */
//@Optional.Interface (iface = "team.chisel.ctm.api.IFacade", modid = "ctm-api")
public abstract class BlockPneumaticCraftCamo extends BlockPneumaticCraft /*implements IFacade*/ {
    public static final ModelProperty<BlockState> CAMO_STATE = new ModelProperty<>();
    public static final ModelProperty<IBlockReader> BLOCK_ACCESS = new ModelProperty<>();
    public static final ModelProperty<BlockPos> BLOCK_POS = new ModelProperty<>();

    protected BlockPneumaticCraftCamo(Properties props) {
        super(props);
    }

    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof ICamouflageableTE && !player.isCreative()) {
            BlockState camoState = ((ICamouflageableTE) te).getCamouflage();
            if (camoState != null) {
                ItemStack camoStack = ICamouflageableTE.getStackForState(camoState);
                ((ICamouflageableTE) te).setCamouflage(null);
                world.levelEvent(player, Constants.WorldEvents.BREAK_BLOCK_EFFECTS, pos, getId(camoState));
                ItemEntity entity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, camoStack);
                world.addFreshEntity(entity);
                return false;
            }
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext ctx) {
        ICamouflageableTE camo = getCamoState(reader, pos);
        return camo == null ? getUncamouflagedShape(state, reader, pos, ctx) : camo.getCamouflage().getShape(reader, pos, ctx);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext ctx) {
        ICamouflageableTE camo = getCamoState(reader, pos);
        return camo == null ? getUncamouflagedCollisionShape(state, reader, pos, ctx) : camo.getCamouflage().getCollisionShape(reader, pos, ctx);
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        ICamouflageableTE camo = getCamoState(worldIn, pos);
        return camo == null ? getUncamouflagedRaytraceShape(state, worldIn, pos) : camo.getCamouflage().getVisualShape(worldIn, pos, ISelectionContext.empty());
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        ICamouflageableTE camo = getCamoState(worldIn, pos);
        return camo == null ? getUncamouflagedRenderShape(state, worldIn, pos) : camo.getCamouflage().getBlockSupportShape(worldIn, pos);
    }

    @Override
    public int getLightBlock(BlockState state, IBlockReader world, BlockPos pos) {
        ICamouflageableTE camo = getCamoState(world, pos);
        return camo == null ? super.getLightBlock(state, world, pos) : camo.getCamouflage().getLightBlock(world, pos);
    }

    @Override
    public boolean hasDynamicShape() {
        return true;  // prevent blockstate caching side solidity
    }

    private ICamouflageableTE getCamoState(IBlockReader blockAccess, BlockPos pos) {
        if (blockAccess == null || pos == null) return null;
        TileEntity te = blockAccess.getBlockEntity(pos);
        return te instanceof ICamouflageableTE && ((ICamouflageableTE) te).getCamouflage() != null ? (ICamouflageableTE) te : null;
    }

    /**
     * The equivalent of {@link net.minecraft.block.Block#getShape(BlockState, IBlockReader, BlockPos, ISelectionContext)}, but for
     * uncamouflaged camo blocks.
     * @param state the blockstate
     * @param reader the world
     * @param pos the block pos
     * @param ctx the selection context
     * @return the block's actual shape, when it isn't camouflaged
     */
    public abstract VoxelShape getUncamouflagedShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext ctx);

    protected VoxelShape getUncamouflagedCollisionShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext ctx) {
        return getUncamouflagedShape(state, reader, pos, ctx);
    }

    protected VoxelShape getUncamouflagedRenderShape(BlockState state, IBlockReader reader, BlockPos pos) {
        return getUncamouflagedShape(state, reader, pos, ISelectionContext.empty());
    }

    protected VoxelShape getUncamouflagedRaytraceShape(BlockState state, IBlockReader reader, BlockPos pos) {
        return VoxelShapes.empty();
    }

//
//    @Nonnull
//    @Override
//    @Optional.Method(modid = "ctm-api")
//    public BlockState getFacade(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable Direction side) {
//        TileEntity te = world.getTileEntity(pos);
//        if (te instanceof ICamouflageableTE && ((ICamouflageableTE) te).getCamouflage() != null) {
//            return ((ICamouflageableTE) te).getCamouflage();
//        }
//        return world.getBlockState(pos);
//    }
}
