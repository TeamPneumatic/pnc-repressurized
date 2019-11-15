package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.ICamouflageableTE;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.ModelProperty;

/**
 * Base class for blocks which may be camouflaged, storing the camouflaged block state in the
 * CAMO_STATE unlisted property
 */
//@Optional.Interface (iface = "team.chisel.ctm.api.IFacade", modid = "ctm-api")
public abstract class BlockPneumaticCraftCamo extends BlockPneumaticCraft /*implements IFacade*/ {
    public static final ModelProperty<BlockState> CAMO_STATE = new ModelProperty<>();
    public static final ModelProperty<IEnviromentBlockReader> BLOCK_ACCESS = new ModelProperty<>();
    public static final ModelProperty<BlockPos> BLOCK_POS = new ModelProperty<>();

    protected BlockPneumaticCraftCamo(String registryName) {
        super(registryName);
    }

    /**
     * When camouflaged, should getBoundingBox() return the bounding box of the camo block?  Override this
     * to return false if the subclass needs to be able to highlight subsections, e.g. elevator caller buttons
     *
     * @return true if camouflage block's bounding box should always be used
     */
    protected boolean doesCamoOverrideBounds() {
        return true;
    }

//    @Override
//    public BlockState getExtendedState(BlockState state, IBlockAccess world, BlockPos pos) {
//        TileEntity te = PneumaticCraftUtils.getTileEntitySafely(world, pos);
//        if (te instanceof ICamouflageableTE) {
//            BlockState camoState = ((ICamouflageableTE) te).getCamouflage();
//            if (camoState != null) {
//                return ((IExtendedBlockState) state)
//                        .withProperty(BLOCK_ACCESS, world)
//                        .withProperty(BLOCK_POS, pos)
//                        .withProperty(CAMO_STATE, camoState);
//            }
//        }
//        return state;
//    }

    @Override
    public void harvestBlock(World world, PlayerEntity player, BlockPos pos, BlockState state, TileEntity te, ItemStack stack) {
        if (te instanceof ICamouflageableTE && !player.isCreative()) {
            // if the block is camo'd, break off the camo, but don't break the block itself
            BlockState camoState = ((ICamouflageableTE) te).getCamouflage();
            if (camoState != null) {
                ItemStack camoStack = ICamouflageableTE.getStackForState(camoState);
                ItemEntity entity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, camoStack);
                world.addEntity(entity);
                ((ICamouflageableTE) te).setCamouflage(null);
                return;
            }
        }
        super.harvestBlock(world, player, pos, state, te, stack);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext ctx) {
        BlockState camo = getCamoState(reader, pos);
        return camo != null ? camo.getCollisionShape(reader, pos) : super.getCollisionShape(state, reader, pos, ctx);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext ctx) {
        BlockState camo = getCamoState(reader, pos);
        return camo != null && doesCamoOverrideBounds() ? camo.getShape(reader, pos) : super.getShape(state, reader, pos, ctx);
    }

    @Override
    public boolean doesSideBlockRendering(BlockState state, IEnviromentBlockReader world, BlockPos pos, Direction face) {
        BlockState camo = getCamoState(world, pos);
        return camo == null ? super.doesSideBlockRendering(state, world, pos, face) : camo.doesSideBlockRendering(world, pos, face);
    }

    protected BlockState getCamoState(IBlockReader blockAccess, BlockPos pos) {
        TileEntity te = blockAccess.getTileEntity(pos);
        if (!(te instanceof ICamouflageableTE))
            return null;

        // must not use a camouflageable block as camouflage!
        BlockState camoState = ((ICamouflageableTE) te).getCamouflage();
        return camoState == null || camoState.getBlock() instanceof BlockPneumaticCraftCamo ? null : camoState;
    }

    @Override
    public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
        return true;
    }

//    @Override
//    @Optional.Method(modid = "theoneprobe")
//    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
//        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
//
//        BlockState camo = getCamoState(world, data.getPos());
//        if (camo != null) {
//            TOPCallback.handleCamo(mode, probeInfo, camo);
//        }
//    }
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
