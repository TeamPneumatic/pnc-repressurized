package me.desht.pneumaticcraft.common.block;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import me.desht.pneumaticcraft.common.thirdparty.theoneprobe.TOPCallback;
import me.desht.pneumaticcraft.common.tileentity.ICamouflageableTE;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.PropertyObject;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.common.Optional;
import team.chisel.ctm.api.IFacade;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Base class for blocks which may be camouflaged, storing the camouflaged block state in the
 * CAMO_STATE unlisted property
 */
@Optional.Interface (iface = "team.chisel.ctm.api.IFacade", modid = "ctm-api")
public abstract class BlockPneumaticCraftCamo extends BlockPneumaticCraftModeled implements IFacade {
    public static final PropertyObject<IBlockState> CAMO_STATE = new PropertyObject<>("camo_state", IBlockState.class);
    public static final PropertyObject<IBlockAccess> BLOCK_ACCESS = new PropertyObject<>("block_access", IBlockAccess.class);
    public static final PropertyObject<BlockPos> BLOCK_POS = new PropertyObject<>("pos", BlockPos.class);
    static final IUnlistedProperty[] UNLISTED_CAMO_PROPERTIES = new IUnlistedProperty[] { CAMO_STATE, BLOCK_ACCESS, BLOCK_POS };

    protected BlockPneumaticCraftCamo(Material par2Material, String registryName) {
        super(par2Material, registryName);
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

    @Override
    protected BlockStateContainer createBlockState() {
        IProperty[] props = isRotatable() ? new IProperty[] { ROTATION } : new IProperty[] { };
        return new ExtendedBlockState(this, props, UNLISTED_CAMO_PROPERTIES);
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity te = PneumaticCraftUtils.getTileEntitySafely(world, pos);
        if (te instanceof ICamouflageableTE) {
            IBlockState camoState = ((ICamouflageableTE) te).getCamouflage();
            if (camoState != null) {
                return ((IExtendedBlockState) state)
                        .withProperty(BLOCK_ACCESS, world)
                        .withProperty(BLOCK_POS, pos)
                        .withProperty(CAMO_STATE, camoState);
            }
        }
        return state;
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack) {
        if (te instanceof ICamouflageableTE && !player.isCreative()) {
            // if the block is camo'd, break off the camo, but don't break the block itself
            IBlockState camoState = ((ICamouflageableTE) te).getCamouflage();
            if (camoState != null) {
                ItemStack camoStack = ICamouflageableTE.getStackForState(camoState);
                EntityItem entity = new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, camoStack);
                world.spawnEntity(entity);
                ((ICamouflageableTE) te).setCamouflage(null);
                return;
            }
        }
        super.harvestBlock(world, player, pos, state, te, stack);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        IBlockState camo = getCamoState(source, pos);
        return camo != null && doesCamoOverrideBounds() ? camo.getBoundingBox(source, pos) : super.getBoundingBox(state, source, pos);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        IBlockState camo = getCamoState(worldIn, pos);
        return camo != null ? camo.getCollisionBoundingBox(worldIn, pos) : super.getCollisionBoundingBox(blockState, worldIn, pos);
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean p_185477_7_) {
        IBlockState camo = getCamoState(worldIn, pos);
        if (camo != null) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, camo.getBoundingBox(worldIn, pos));
        } else {
            super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, p_185477_7_);
        }
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
        IBlockState camo = getCamoState(world, pos);
        return camo == null ? super.doesSideBlockRendering(state, world, pos, face) : camo.doesSideBlockRendering(world, pos, face);
    }

    @Override
    public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        // ensure levers etc. can be attached to the block even though it can possibly emit redstone
        IBlockState camo = getCamoState(world, pos);
        return camo == null ? super.isSideSolid(base_state, world, pos, side) : camo.isSideSolid(world, pos, side);
    }

    protected IBlockState getCamoState(IBlockAccess blockAccess, BlockPos pos) {
        TileEntity te = PneumaticCraftUtils.getTileEntitySafely(blockAccess, pos);
        if (!(te instanceof ICamouflageableTE))
            return null;

        // must not use a camouflageable block as camouflage!
        IBlockState camoState = ((ICamouflageableTE) te).getCamouflage();
        return camoState == null || camoState.getBlock() instanceof BlockPneumaticCraftCamo ? null : camoState;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        IBlockState camoState = getCamoState(worldIn, pos);
        return camoState != null ? camoState.getBlockFaceShape(worldIn, pos, face) : BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return true;
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);

        IBlockState camo = getCamoState(world, data.getPos());
        if (camo != null) {
            TOPCallback.handleCamo(mode, probeInfo, camo);
        }
    }

    @Nonnull
    @Override
    @Optional.Method(modid = "ctm-api")
    public IBlockState getFacade(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable EnumFacing side) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof ICamouflageableTE && ((ICamouflageableTE) te).getCamouflage() != null) {
            return ((ICamouflageableTE) te).getCamouflage();
        }
        return world.getBlockState(pos);
    }
}
