package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.block.tubes.ModuleRegistrator;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.config.ThirdPartyConfig;
import me.desht.pneumaticcraft.common.item.ItemTubeModule;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.thirdparty.ModInteractionUtils;
import me.desht.pneumaticcraft.common.thirdparty.mcmultipart.PneumaticMultiPart;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.BBConstants;
import me.desht.pneumaticcraft.lib.ModIds;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BlockPressureTube extends BlockPneumaticCraftModeled {
    public static final AxisAlignedBB BASE_BOUNDS = new AxisAlignedBB(
            BBConstants.PRESSURE_PIPE_MIN_POS, BBConstants.PRESSURE_PIPE_MIN_POS, BBConstants.PRESSURE_PIPE_MIN_POS,
            BBConstants.PRESSURE_PIPE_MAX_POS, BBConstants.PRESSURE_PIPE_MAX_POS, BBConstants.PRESSURE_PIPE_MAX_POS
    );

    public static final PropertyBool UP = PropertyBool.create("up");
    public static final PropertyBool DOWN = PropertyBool.create("down");
    public static final PropertyBool NORTH = PropertyBool.create("north");
    public static final PropertyBool EAST = PropertyBool.create("east");
    public static final PropertyBool SOUTH = PropertyBool.create("south");
    public static final PropertyBool WEST = PropertyBool.create("west");
    public static final PropertyBool[] CONNECTION_PROPERTIES = new PropertyBool[]{DOWN, UP, NORTH, SOUTH, WEST, EAST};

    public AxisAlignedBB[] boundingBoxes = new AxisAlignedBB[6];
    private final float dangerPressure, criticalPressure;
    private final int volume;
    private static final Object CENTER_TUBE_HIT_MARKER = new Object(); //Object that is assigned to a MOP's hitInfo when a player hovers over the center part of a tube.

    public BlockPressureTube(String registryName, float dangerPressure, float criticalPressure, int volume) {
        super(Material.IRON, registryName);

        double width = (BBConstants.PRESSURE_PIPE_MAX_POS - BBConstants.PRESSURE_PIPE_MIN_POS) / 2;
        double height = BBConstants.PRESSURE_PIPE_MIN_POS;

        boundingBoxes[0] = new AxisAlignedBB(0.5 - width, BBConstants.PRESSURE_PIPE_MIN_POS - height, 0.5 - width, 0.5 + width, BBConstants.PRESSURE_PIPE_MIN_POS, 0.5 + width);
        boundingBoxes[1] = new AxisAlignedBB(0.5 - width, BBConstants.PRESSURE_PIPE_MAX_POS, 0.5 - width, 0.5 + width, BBConstants.PRESSURE_PIPE_MAX_POS + height, 0.5 + width);
        boundingBoxes[2] = new AxisAlignedBB(0.5 - width, 0.5 - width, BBConstants.PRESSURE_PIPE_MIN_POS - height, 0.5 + width, 0.5 + width, BBConstants.PRESSURE_PIPE_MIN_POS);
        boundingBoxes[3] = new AxisAlignedBB(0.5 - width, 0.5 - width, BBConstants.PRESSURE_PIPE_MAX_POS, 0.5 + width, 0.5 + width, BBConstants.PRESSURE_PIPE_MAX_POS + height);
        boundingBoxes[4] = new AxisAlignedBB(BBConstants.PRESSURE_PIPE_MIN_POS - height, 0.5 - width, 0.5 - width, BBConstants.PRESSURE_PIPE_MIN_POS, 0.5 + width, 0.5 + width);
        boundingBoxes[5] = new AxisAlignedBB(BBConstants.PRESSURE_PIPE_MAX_POS, 0.5 - width, 0.5 - width, BBConstants.PRESSURE_PIPE_MAX_POS + height, 0.5 + width, 0.5 + width);

        this.dangerPressure = dangerPressure;
        this.criticalPressure = criticalPressure;
        this.volume = volume;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityPressureTube.class;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityPressureTube(dangerPressure, criticalPressure, volume);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, Arrays.copyOf(BlockPressureTube.CONNECTION_PROPERTIES, BlockPressureTube.CONNECTION_PROPERTIES.length));
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        state = super.getActualState(state, worldIn, pos);
        TileEntity te = getTE(worldIn, pos);
        if (te instanceof TileEntityPressureTube) {
            TileEntityPressureTube tube = (TileEntityPressureTube) te;
            for (int i = 0; i < 6; i++) {
                state = state.withProperty(CONNECTION_PROPERTIES[i], tube.sidesConnected[i]);
            }
        }
        return state;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = super.getStateFromMeta(meta);
        for (PropertyBool property : CONNECTION_PROPERTIES) {
            state = state.withProperty(property, (meta & 1) == 1);
            meta >>= 1;
        }
        return state;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
        /*int meta = 0;
        for(PropertyBool property : CONNECTION_PROPERTIES) {
            if(state.getValue(property)) {
                meta++;
            }
            meta <<= 1;
        }
        return meta;*/
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float par7, float par8, float par9) {
        if (!world.isRemote) {
            if (tryPlaceModule(player, world, pos, side, false)) return true;
        }
        if (!player.isSneaking()) {
            TubeModule module = getLookedModule(world, pos, player);
            if (module != null) {
                return module.onActivated(player);
            }
        }
        return false;
    }

    private static TileEntity getTE(IBlockAccess world, BlockPos pos) {
        return ThirdPartyConfig.isEnabled(ModIds.MCMP) ? PneumaticMultiPart.unwrapTile(world, pos) : PneumaticCraftUtils.getTileEntitySafely(world, pos);
    }

    public boolean tryPlaceModule(EntityPlayer player, World world, BlockPos pos, EnumFacing side, boolean simulate) {
        if (player.getHeldItemMainhand().getItem() instanceof ItemTubeModule) {
            TileEntity te = getTE(world, pos);
            TileEntityPressureTube pressureTube = ModInteractionUtils.getInstance().getTube(te);
            if (pressureTube.modules[side.ordinal()] == null && ModInteractionUtils.getInstance().occlusionTest(boundingBoxes[side.ordinal()], te)) {
                TubeModule module = ModuleRegistrator.getModule(((ItemTubeModule) player.getHeldItemMainhand().getItem()).moduleName);
                if (simulate) module.markFake();
                pressureTube.setModule(module, side);
                if (!simulate) {
                    neighborChanged(world.getBlockState(pos), world, pos, this, pos.offset(side));
                    world.notifyNeighborsOfStateChange(pos, this, true);
                    if (!player.capabilities.isCreativeMode) player.getHeldItemMainhand().shrink(1);
                    world.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundType.GLASS.getStepSound(), SoundCategory.BLOCKS, SoundType.GLASS.getVolume() * 5.0f, SoundType.GLASS.getPitch() * .9F, false);
                }
                return true;
            }
        } else if (player.getHeldItemMainhand().getItem() == Itemss.ADVANCED_PCB && !simulate) {
            TubeModule module = BlockPressureTube.getLookedModule(world, pos, player);
            if (module != null && !module.isUpgraded() && module.canUpgrade()) {
                if (!world.isRemote) {
                    module.upgrade();
                    if (!player.capabilities.isCreativeMode) player.getHeldItemMainhand().shrink(1);
                }
                return true;
            }
        }
        return false;
    }

    public static TubeModule getLookedModule(World world, BlockPos pos, EntityPlayer player) {
        Pair<Vec3d, Vec3d> vecs = PneumaticCraftUtils.getStartAndEndLookVec(player);
        IBlockState state = world.getBlockState(pos);
        RayTraceResult mop = state.collisionRayTrace(world, pos, vecs.getLeft(), vecs.getRight());
        if (mop != null && mop.hitInfo instanceof EnumFacing) {
            TileEntityPressureTube tube = ModInteractionUtils.getInstance().getTube(getTE(world, pos));
            return tube.modules[((EnumFacing) mop.hitInfo).ordinal()];
        }
        return null;
    }

    @Override
    public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d origin, Vec3d direction) {
        RayTraceResult bestMOP = null;
        AxisAlignedBB bestAABB = null;

        setBlockBounds(BASE_BOUNDS);
        RayTraceResult mop = super.collisionRayTrace(state, world, pos, origin, direction);

        if (isCloserMOP(origin, bestMOP, mop)) {
            bestMOP = mop;
            bestAABB = getBoundingBox(state, world, pos);
        }

        TileEntityPressureTube tube = ModInteractionUtils.getInstance().getTube(getTE(world, pos));
        for (int i = 0; i < 6; i++) {
            if (tube.sidesConnected[i]) {
                setBlockBounds(boundingBoxes[i]);
                mop = super.collisionRayTrace(state, world, pos, origin, direction);
                if (isCloserMOP(origin, bestMOP, mop)) {
                    bestMOP = mop;
                    bestAABB = getBoundingBox(state, world, pos);
                }
            }
        }
        if (bestMOP != null) bestMOP.hitInfo = CENTER_TUBE_HIT_MARKER;

        TubeModule[] modules = tube.modules;
        for (EnumFacing dir : EnumFacing.VALUES) {
            if (modules[dir.ordinal()] != null) {
                setBlockBounds(modules[dir.ordinal()].boundingBoxes[dir.ordinal()]);
                mop = super.collisionRayTrace(state, world, pos, origin, direction);
                if (isCloserMOP(origin, bestMOP, mop)) {
                    mop.hitInfo = dir;
                    bestMOP = mop;
                    bestAABB = getBoundingBox(state, world, pos);
                }
            }
        }
        if (bestAABB != null) setBlockBounds(bestAABB);
        return bestMOP;
    }

    private boolean isCloserMOP(Vec3d origin, RayTraceResult originalMOP, RayTraceResult newMOP) {
        if (newMOP == null) return false;
        if (originalMOP == null) return true;
        return PneumaticCraftUtils.distBetween(origin, newMOP.hitVec) < PneumaticCraftUtils.distBetween(origin, originalMOP.hitVec);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        if (target.hitInfo == CENTER_TUBE_HIT_MARKER) {
            return super.getPickBlock(state, target, world, pos, player);
        } else {
            TileEntityPressureTube tube = (TileEntityPressureTube) getTE(world, pos);
            return new ItemStack(ModuleRegistrator.getModuleItem(tube.modules[((EnumFacing) target.hitInfo).ordinal()].getType()));
        }
    }

//    private void setBlockBounds(AxisAlignedBB aabb) {
//        this.setBlockBounds((float) aabb.minX, (float) aabb.minY, (float) aabb.minZ, (float) aabb.maxX, (float) aabb.maxY, (float) aabb.maxZ);
//    }

    @Override
    public boolean rotateBlock(World world, EntityPlayer player, BlockPos pos, EnumFacing side) {
        TileEntityPressureTube tube = ModInteractionUtils.getInstance().getTube(getTE(world, pos));
        if (player.isSneaking()) {
            TubeModule module = getLookedModule(world, pos, player);
            if (module != null) {
                if (!player.capabilities.isCreativeMode) {
                    List<ItemStack> drops = module.getDrops();
                    for (ItemStack drop : drops) {
                        EntityItem entity = new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                        entity.setItem(drop);
                        world.spawnEntity(entity);
                        entity.onCollideWithPlayer(player);
                    }
                }
                tube.setModule(null, module.getDirection());
                neighborChanged(world.getBlockState(pos), world, pos, this, pos.offset(side));
                world.notifyNeighborsOfStateChange(pos, this, true);
                return true;
            }
            if (!player.capabilities.isCreativeMode) {
                EntityItem entity = new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(tube.criticalPressure <= PneumaticValues.MAX_PRESSURE_PRESSURE_TUBE ? Blockss.PRESSURE_TUBE : Blockss.ADVANCED_PRESSURE_TUBE));
                world.spawnEntity(entity);
                entity.onCollideWithPlayer(player);
            }
            ModInteractionUtils.getInstance().removeTube(getTE(world, pos));
            return true;
        } else {
            return super.rotateBlock(world, player, pos, side);
        }

    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        List<ItemStack> drops = getModuleDrops((TileEntityPressureTube) getTE(world, pos));
        for (ItemStack drop : drops) {
            EntityItem entity = new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            entity.setItem(drop);
            world.spawnEntity(entity);
        }
        super.breakBlock(world, pos, state);
    }

    public static List<ItemStack> getModuleDrops(TileEntityPressureTube tube) {
        List<ItemStack> drops = new ArrayList<>();
        for (TubeModule module : tube.modules) {
            if (module != null) {
                drops.addAll(module.getDrops());
            }
        }
        return drops;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        TileEntity te = getTE(worldIn, pos);

        if (te instanceof TileEntityPressureTube) {
            TileEntityPressureTube tePt = (TileEntityPressureTube) te;
            for (int i = 0; i < 6; i++) {
                if (tePt.sidesConnected[i]) {
                    return boundingBoxes[i];
                } else if (tePt.modules[i] != null) {
                    return tePt.modules[i].boundingBoxes[i];
                }
            }
        }
        return super.getCollisionBoundingBox(blockState, worldIn, pos);
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        TileEntity te = getTE(worldIn, pos);
        if (te instanceof TileEntityPressureTube) {
            TileEntityPressureTube tePt = (TileEntityPressureTube) te;
            addCollisionBoxToList(pos, entityBox, collidingBoxes, BASE_BOUNDS);
            for (int i = 0; i < 6; i++) {
                if (tePt.sidesConnected[i]) {
                    addCollisionBoxToList(pos, entityBox, collidingBoxes, boundingBoxes[i]);
                }
                if (tePt.modules[i] != null) {
                    addCollisionBoxToList(pos, entityBox, collidingBoxes, tePt.modules[i].boundingBoxes[i]);
                }
            }
        }
    }

//    @Override
//    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean p_185477_7_) {
////        setBlockBounds(BBConstants.PRESSURE_PIPE_MIN_POS, BBConstants.PRESSURE_PIPE_MIN_POS, BBConstants.PRESSURE_PIPE_MIN_POS, BBConstants.PRESSURE_PIPE_MAX_POS, BBConstants.PRESSURE_PIPE_MAX_POS, BBConstants.PRESSURE_PIPE_MAX_POS);
////        addCollisionBoxToList(pos, entityBox, collidingBoxes, entityBox);
//
//        super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, p_185477_7_);
//
//        TileEntity te = worldIn.getTileEntity(pos);
//
//        if (te instanceof TileEntityPressureTube) {
//            TileEntityPressureTube tePt = (TileEntityPressureTube) te;
//            for (int i = 0; i < 6; i++) {
//                if (tePt.sidesConnected[i]) {
//                    addCollisionBoxToList(pos, entityBox, collidingBoxes, boundingBoxes[i]);
//                } else if (tePt.modules[i] != null) {
//                    addCollisionBoxToList(pos, entityBox, collidingBoxes, tePt.modules[i].boundingBoxes[i]);
//                }
//            }
//        }
////        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
//    }


    @Override
    @SideOnly(Side.CLIENT)
    /**
     * A randomly called display update to be able to add particles or other items for display
     */
    public void randomDisplayTick(IBlockState state, World par1World, BlockPos pos, Random par5Random) {
        TileEntity te = getTE(par1World, pos);
        if (te instanceof TileEntityPressureTube) {
            TileEntityPressureTube tePt = (TileEntityPressureTube) te;
            int l = 0;
            for (TubeModule module : tePt.modules)
                if (module != null) l = Math.max(l, module.getRedstoneLevel());
            if (l > 0) {
                // for(int i = 0; i < 4; i++){
                double d0 = pos.getX() + 0.5D + (par5Random.nextFloat() - 0.5D) * 0.5D;
                double d1 = pos.getY() + 0.5D + (par5Random.nextFloat() - 0.5D) * 0.5D;
                double d2 = pos.getZ() + 0.5D + (par5Random.nextFloat() - 0.5D) * 0.5D;
                float f = l / 15.0F;
                float f1 = f * 0.6F + 0.4F;
                float f2 = f * f * 0.7F - 0.5F;
                float f3 = f * f * 0.6F - 0.7F;
                if (f2 < 0.0F) {
                    f2 = 0.0F;
                }

                if (f3 < 0.0F) {
                    f3 = 0.0F;
                }
                // PacketDispatcher.sendPacketToAllPlayers(PacketHandlerPneumaticCraft.spawnParticle("reddust",
                // d0, d1, d2, (double)f1, (double)f2, (double)f3));
                par1World.spawnParticle(EnumParticleTypes.REDSTONE, d0, d1, d2, f1, f2, f3);
                // }
            }
        }

    }

//    /**
//     * Returns true if the block is emitting direct/strong redstone power on the
//     * specified side. Args: World, X, Y, Z, side. Note that the side is
//     * reversed - eg it is 1 (up) when checking the bottom of the block.
//     */
//    @Override
//    public int getStrongPower(IBlockState state, IBlockAccess par1IBlockAccess, BlockPos pos, EnumFacing side) {
//        return 0;
//    }

    /**
     * Returns true if the block is emitting indirect/weak redstone power on the
     * specified side. If isBlockNormalCube returns true, standard redstone
     * propagation rules will apply instead and this will not be called. Args:
     * World, X, Y, Z, side. Note that the side is reversed - eg it is 1 (up)
     * when checking the bottom of the block.
     */
    @Override
    public int getWeakPower(IBlockState state, IBlockAccess par1IBlockAccess, BlockPos pos, EnumFacing side) {
        TileEntity te = getTE(par1IBlockAccess, pos);
        if (te instanceof TileEntityPressureTube) {
            TileEntityPressureTube tePt = (TileEntityPressureTube) te;
            int redstoneLevel = 0;
            for (EnumFacing face : EnumFacing.VALUES) {
                if (tePt.modules[face.ordinal()] != null) {
                    if (side.getOpposite() == face || face != side && tePt.modules[face.ordinal()].isInline()) {//if we are on the same side, or when we have an 'in line' module that is not on the opposite side.
                        redstoneLevel = Math.max(redstoneLevel, tePt.modules[face.ordinal()].getRedstoneLevel());
                    }
                }
            }
            return redstoneLevel;
        }
        return 0;
    }

    /**
     * Determine if this block can make a redstone connection on the side provided,
     * Useful to control which sides are inputs and outputs for redstone wires.
     *
     * Side:
     *  -1: UP
     *   0: NORTH
     *   1: EAST
     *   2: SOUTH
     *   3: WEST
     *
     * @param world The current world
     * @param x X Position
     * @param y Y Position
     * @param z Z Position
     * @param side The side that is trying to make the connection
     * @return True to make the connection
     */
    /* @Override
     * TODO 1.8
     public boolean canConnectRedstone(IBlockAccess world, BlockPos pos, EnumFacing side){
         if(side < 0 || side > 3) return false;
         TileEntityPressureTube tube = (TileEntityPressureTube)world.getTileEntity(x, y, z);
         EnumFacing d = EnumFacing.NORTH;
         for(int i = 0; i < side; i++) {
             d = d.getRotation(EnumFacing.UP);
         }
         side = d.ordinal();
         for(int i = 0; i < 6; i++) {
             if(tube.modules[i] != null) {
                 if((side ^ 1) == i || i != side && tube.modules[i].isInline()) {//if we are on the same side, or when we have an 'in line' module that is not on the opposite side.
                     if(tube.modules[i] instanceof TubeModuleRedstoneEmitting) return true;
                 }
             }
         }
         return false;
     }*/
}
