package pneumaticCraft.common.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.tuple.Pair;

import pneumaticCraft.common.block.tubes.ModuleRegistrator;
import pneumaticCraft.common.block.tubes.TubeModule;
import pneumaticCraft.common.block.tubes.TubeModuleRedstoneEmitting;
import pneumaticCraft.common.item.ItemTubeModule;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.thirdparty.ModInteractionUtils;
import pneumaticCraft.common.tileentity.TileEntityPressureTube;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.BBConstants;
import pneumaticCraft.lib.PneumaticValues;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockPressureTube extends BlockPneumaticCraftModeled{

    public AxisAlignedBB[] boundingBoxes = new AxisAlignedBB[6];
    private final float dangerPressure, criticalPressure;
    private final int volume;

    public BlockPressureTube(Material par2Material, float dangerPressure, float criticalPressure, int volume){
        super(par2Material);

        double width = (BBConstants.PRESSURE_PIPE_MAX_POS - BBConstants.PRESSURE_PIPE_MIN_POS) / 2;
        double height = BBConstants.PRESSURE_PIPE_MIN_POS;

        boundingBoxes[0] = AxisAlignedBB.getBoundingBox(0.5 - width, BBConstants.PRESSURE_PIPE_MIN_POS - height, 0.5 - width, 0.5 + width, BBConstants.PRESSURE_PIPE_MIN_POS, 0.5 + width);
        boundingBoxes[1] = AxisAlignedBB.getBoundingBox(0.5 - width, BBConstants.PRESSURE_PIPE_MAX_POS, 0.5 - width, 0.5 + width, BBConstants.PRESSURE_PIPE_MAX_POS + height, 0.5 + width);
        boundingBoxes[2] = AxisAlignedBB.getBoundingBox(0.5 - width, 0.5 - width, BBConstants.PRESSURE_PIPE_MIN_POS - height, 0.5 + width, 0.5 + width, BBConstants.PRESSURE_PIPE_MIN_POS);
        boundingBoxes[3] = AxisAlignedBB.getBoundingBox(0.5 - width, 0.5 - width, BBConstants.PRESSURE_PIPE_MAX_POS, 0.5 + width, 0.5 + width, BBConstants.PRESSURE_PIPE_MAX_POS + height);
        boundingBoxes[4] = AxisAlignedBB.getBoundingBox(BBConstants.PRESSURE_PIPE_MIN_POS - height, 0.5 - width, 0.5 - width, BBConstants.PRESSURE_PIPE_MIN_POS, 0.5 + width, 0.5 + width);
        boundingBoxes[5] = AxisAlignedBB.getBoundingBox(BBConstants.PRESSURE_PIPE_MAX_POS, 0.5 - width, 0.5 - width, BBConstants.PRESSURE_PIPE_MAX_POS + height, 0.5 + width, 0.5 + width);

        this.dangerPressure = dangerPressure;
        this.criticalPressure = criticalPressure;
        this.volume = volume;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityPressureTube.class;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata){
        return new TileEntityPressureTube(dangerPressure, criticalPressure, volume);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9){
        if(!world.isRemote) {
            if(tryPlaceModule(player, world, x, y, z, par6, false)) return true;
        }
        if(!player.isSneaking()) {
            TubeModule module = getLookedModule(world, x, y, z, player);
            if(module != null) {
                return module.onActivated(player);
            }
        }
        return false;
    }

    public boolean tryPlaceModule(EntityPlayer player, World world, int x, int y, int z, int par6, boolean simulate){
        if(player.getCurrentEquippedItem() != null) {
            if(player.getCurrentEquippedItem().getItem() instanceof ItemTubeModule) {
                TileEntityPressureTube pressureTube = ModInteractionUtils.getInstance().getTube(world.getTileEntity(x, y, z));
                if(pressureTube.modules[par6] == null && ModInteractionUtils.getInstance().occlusionTest(boundingBoxes[par6], world.getTileEntity(x, y, z))) {
                    TubeModule module = ModuleRegistrator.getModule(((ItemTubeModule)player.getCurrentEquippedItem().getItem()).moduleName);
                    if(simulate) module.markFake();
                    pressureTube.setModule(module, ForgeDirection.getOrientation(par6));
                    if(!simulate) {
                        onNeighborBlockChange(world, x, y, z, this);
                        world.notifyBlocksOfNeighborChange(x, y, z, this, ForgeDirection.getOrientation(par6).getOpposite().ordinal());
                        if(!player.capabilities.isCreativeMode) player.getCurrentEquippedItem().stackSize--;
                        world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, Block.soundTypeGlass.getStepResourcePath(), Block.soundTypeGlass.getVolume() * 5.0F, Block.soundTypeGlass.getPitch() * .9F);
                    }
                    return true;
                }
            } else if(player.getCurrentEquippedItem().getItem() == Itemss.advancedPCB && !simulate) {
                TubeModule module = BlockPressureTube.getLookedModule(world, x, y, z, player);
                if(module != null && !module.isUpgraded() && module.canUpgrade()) {
                    if(!world.isRemote) {
                        module.upgrade();
                        if(!player.capabilities.isCreativeMode) player.getCurrentEquippedItem().stackSize--;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public static TubeModule getLookedModule(World world, int x, int y, int z, EntityPlayer player){
        Pair<Vec3, Vec3> vecs = PneumaticCraftUtils.getStartAndEndLookVec(player);
        MovingObjectPosition mop = Blockss.pressureTube.collisionRayTrace(world, x, y, z, vecs.getLeft(), vecs.getRight());
        if(mop != null && mop.hitInfo instanceof ForgeDirection && (ForgeDirection)mop.hitInfo != ForgeDirection.UNKNOWN) {
            TileEntityPressureTube tube = ModInteractionUtils.getInstance().getTube(world.getTileEntity(x, y, z));
            return tube.modules[((ForgeDirection)mop.hitInfo).ordinal()];
        }
        return null;
    }

    @Override
    public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 origin, Vec3 direction){
        MovingObjectPosition bestMOP = null;
        AxisAlignedBB bestAABB = null;

        setBlockBounds(BBConstants.PRESSURE_PIPE_MIN_POS, BBConstants.PRESSURE_PIPE_MIN_POS, BBConstants.PRESSURE_PIPE_MIN_POS, BBConstants.PRESSURE_PIPE_MAX_POS, BBConstants.PRESSURE_PIPE_MAX_POS, BBConstants.PRESSURE_PIPE_MAX_POS);
        MovingObjectPosition mop = super.collisionRayTrace(world, x, y, z, origin, direction);
        if(isCloserMOP(origin, bestMOP, mop)) {
            bestMOP = mop;
            bestAABB = AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
        }

        TileEntityPressureTube tube = ModInteractionUtils.getInstance().getTube(world.getTileEntity(x, y, z));
        for(int i = 0; i < 6; i++) {
            if(tube.sidesConnected[i]) {
                setBlockBounds(boundingBoxes[i]);
                mop = super.collisionRayTrace(world, x, y, z, origin, direction);
                if(isCloserMOP(origin, bestMOP, mop)) {
                    bestMOP = mop;
                    bestAABB = AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
                }
            }
        }

        if(bestMOP != null) bestMOP.hitInfo = ForgeDirection.UNKNOWN;//unknown indicates we hit the tube.

        TubeModule[] modules = tube.modules;
        for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            if(modules[dir.ordinal()] != null) {
                setBlockBounds(modules[dir.ordinal()].boundingBoxes[dir.ordinal()]);
                mop = super.collisionRayTrace(world, x, y, z, origin, direction);
                if(isCloserMOP(origin, bestMOP, mop)) {
                    mop.hitInfo = dir;
                    bestMOP = mop;
                    bestAABB = AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
                }
            }
        }
        if(bestAABB != null) setBlockBounds(bestAABB);
        return bestMOP;
    }

    private boolean isCloserMOP(Vec3 origin, MovingObjectPosition originalMOP, MovingObjectPosition newMOP){
        if(newMOP == null) return false;
        if(originalMOP == null) return true;
        return PneumaticCraftUtils.distBetween(origin, newMOP.hitVec) < PneumaticCraftUtils.distBetween(origin, originalMOP.hitVec);
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z){
        if(target.hitInfo == ForgeDirection.UNKNOWN) {
            return super.getPickBlock(target, world, x, y, z);
        } else {
            TileEntityPressureTube tube = (TileEntityPressureTube)world.getTileEntity(x, y, z);
            return new ItemStack(ModuleRegistrator.getModuleItem(tube.modules[((ForgeDirection)target.hitInfo).ordinal()].getType()));
        }
    }

    private void setBlockBounds(AxisAlignedBB aabb){
        this.setBlockBounds((float)aabb.minX, (float)aabb.minY, (float)aabb.minZ, (float)aabb.maxX, (float)aabb.maxY, (float)aabb.maxZ);
    }

    @Override
    public boolean rotateBlock(World world, EntityPlayer player, int x, int y, int z, ForgeDirection side){
        TileEntityPressureTube tube = ModInteractionUtils.getInstance().getTube(world.getTileEntity(x, y, z));
        if(player.isSneaking()) {
            TubeModule module = getLookedModule(world, x, y, z, player);
            if(module != null) {
                if(!player.capabilities.isCreativeMode) {
                    List<ItemStack> drops = module.getDrops();
                    for(ItemStack drop : drops) {
                        EntityItem entity = new EntityItem(world, x + 0.5, y + 0.5, z + 0.5);
                        entity.setEntityItemStack(drop);
                        world.spawnEntityInWorld(entity);
                        entity.onCollideWithPlayer(player);
                    }
                }
                tube.setModule(null, module.getDirection());
                onNeighborBlockChange(world, x, y, z, this);
                world.notifyBlocksOfNeighborChange(x, y, z, this, module.getDirection().getOpposite().ordinal());
                return true;
            }
            if(!player.capabilities.isCreativeMode) {
                EntityItem entity = new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, new ItemStack(tube.maxPressure <= PneumaticValues.MAX_PRESSURE_PRESSURE_TUBE ? Blockss.pressureTube : Blockss.advancedPressureTube));
                world.spawnEntityInWorld(entity);
                entity.onCollideWithPlayer(player);
            }
            ModInteractionUtils.getInstance().removeTube(world.getTileEntity(x, y, z));
            return true;
        } else {
            return super.rotateBlock(world, player, x, y, z, side);
        }

    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta){
        List<ItemStack> drops = getModuleDrops((TileEntityPressureTube)world.getTileEntity(x, y, z));
        for(ItemStack drop : drops) {
            EntityItem entity = new EntityItem(world, x + 0.5, y + 0.5, z + 0.5);
            entity.setEntityItemStack(drop);
            world.spawnEntityInWorld(entity);
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    public static List<ItemStack> getModuleDrops(TileEntityPressureTube tube){
        List<ItemStack> drops = new ArrayList<ItemStack>();
        for(TubeModule module : tube.modules) {
            if(module != null) {
                drops.addAll(module.getDrops());
            }
        }
        return drops;
    }

    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB axisalignedbb, List arraylist, Entity par7Entity){
        setBlockBounds(BBConstants.PRESSURE_PIPE_MIN_POS, BBConstants.PRESSURE_PIPE_MIN_POS, BBConstants.PRESSURE_PIPE_MIN_POS, BBConstants.PRESSURE_PIPE_MAX_POS, BBConstants.PRESSURE_PIPE_MAX_POS, BBConstants.PRESSURE_PIPE_MAX_POS);
        super.addCollisionBoxesToList(world, x, y, z, axisalignedbb, arraylist, par7Entity);

        TileEntity te = world.getTileEntity(x, y, z);
        TileEntityPressureTube tePt = (TileEntityPressureTube)te;

        for(int i = 0; i < 6; i++) {
            if(tePt.sidesConnected[i]) {
                setBlockBounds(boundingBoxes[i]);
                super.addCollisionBoxesToList(world, x, y, z, axisalignedbb, arraylist, par7Entity);
            } else if(tePt.modules[i] != null) {
                setBlockBounds(tePt.modules[i].boundingBoxes[i]);
                super.addCollisionBoxesToList(world, x, y, z, axisalignedbb, arraylist, par7Entity);
            }
        }
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z){
        super.onBlockAdded(world, x, y, z);
        TileEntity te = world.getTileEntity(x, y, z);
        if(te != null && te instanceof TileEntityPressureTube) {
            TileEntityPressureTube tePt = (TileEntityPressureTube)te;
            tePt.updateConnections(world, x, y, z);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    /**
     * A randomly called display update to be able to add particles or other items for display
     */
    public void randomDisplayTick(World par1World, int par2, int par3, int par4, Random par5Random){
        TileEntity te = par1World.getTileEntity(par2, par3, par4);
        if(te instanceof TileEntityPressureTube) {
            TileEntityPressureTube tePt = (TileEntityPressureTube)te;
            int l = 0;
            for(TubeModule module : tePt.modules)
                if(module != null) l = Math.max(l, module.getRedstoneLevel());
            if(l > 0) {
                // for(int i = 0; i < 4; i++){
                double d0 = par2 + 0.5D + (par5Random.nextFloat() - 0.5D) * 0.5D;
                double d1 = par3 + 0.5D + (par5Random.nextFloat() - 0.5D) * 0.5D;
                double d2 = par4 + 0.5D + (par5Random.nextFloat() - 0.5D) * 0.5D;
                float f = l / 15.0F;
                float f1 = f * 0.6F + 0.4F;
                float f2 = f * f * 0.7F - 0.5F;
                float f3 = f * f * 0.6F - 0.7F;
                if(f2 < 0.0F) {
                    f2 = 0.0F;
                }

                if(f3 < 0.0F) {
                    f3 = 0.0F;
                }
                // PacketDispatcher.sendPacketToAllPlayers(PacketHandlerPneumaticCraft.spawnParticle("reddust",
                // d0, d1, d2, (double)f1, (double)f2, (double)f3));
                par1World.spawnParticle("reddust", d0, d1, d2, f1, f2, f3);
                // }
            }
        }

    }

    /**
     * Returns true if the block is emitting direct/strong redstone power on the
     * specified side. Args: World, X, Y, Z, side. Note that the side is
     * reversed - eg it is 1 (up) when checking the bottom of the block.
     */
    @Override
    public int isProvidingStrongPower(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5){
        return 0;
    }

    /**
     * Returns true if the block is emitting indirect/weak redstone power on the
     * specified side. If isBlockNormalCube returns true, standard redstone
     * propagation rules will apply instead and this will not be called. Args:
     * World, X, Y, Z, side. Note that the side is reversed - eg it is 1 (up)
     * when checking the bottom of the block.
     */
    @Override
    public int isProvidingWeakPower(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int side){

        TileEntity te = par1IBlockAccess.getTileEntity(par2, par3, par4);
        if(te instanceof TileEntityPressureTube) {
            TileEntityPressureTube tePt = (TileEntityPressureTube)te;
            int redstoneLevel = 0;
            for(int i = 0; i < 6; i++) {
                if(tePt.modules[i] != null) {
                    if((side ^ 1) == i || i != side && tePt.modules[i].isInline()) {//if we are on the same side, or when we have an 'in line' module that is not on the opposite side.
                        redstoneLevel = Math.max(redstoneLevel, tePt.modules[i].getRedstoneLevel());
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
    @Override
    public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side){
        if(side < 0 || side > 3) return false;
        TileEntityPressureTube tube = (TileEntityPressureTube)world.getTileEntity(x, y, z);
        ForgeDirection d = ForgeDirection.NORTH;
        for(int i = 0; i < side; i++) {
            d = d.getRotation(ForgeDirection.UP);
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
    }

}
