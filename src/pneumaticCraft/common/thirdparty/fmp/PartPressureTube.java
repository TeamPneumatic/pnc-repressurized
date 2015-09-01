package pneumaticCraft.common.thirdparty.fmp;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.api.block.IPneumaticWrenchable;
import pneumaticCraft.api.tileentity.IAirHandler;
import pneumaticCraft.client.model.ModelPressureTube;
import pneumaticCraft.common.block.BlockPressureTube;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.block.tubes.IPneumaticPosProvider;
import pneumaticCraft.common.block.tubes.TubeModule;
import pneumaticCraft.common.block.tubes.TubeModuleRedstoneEmitting;
import pneumaticCraft.common.config.Config;
import pneumaticCraft.common.tileentity.TileEntityPressureTube;
import pneumaticCraft.lib.BBConstants;
import pneumaticCraft.lib.Log;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.ISidedHollowConnect;
import codechicken.multipart.IRedstonePart;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.NormalOcclusionTest;
import codechicken.multipart.NormallyOccludedPart;
import codechicken.multipart.PartMap;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TSlottedPart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartPressureTube extends TMultiPart implements IPneumaticPosProvider, IPneumaticWrenchable, TSlottedPart,
        JNormalOcclusion, ISidedHollowConnect, IRedstonePart{

    private TileEntityPressureTube tube = getNewTube().setPart(this);

    private static final Cuboid6[] boundingBoxes = new Cuboid6[7];
    static {
        boundingBoxes[0] = new Cuboid6(BBConstants.PRESSURE_PIPE_MIN_POS, 0.0F, BBConstants.PRESSURE_PIPE_MIN_POS, BBConstants.PRESSURE_PIPE_MAX_POS, BBConstants.PRESSURE_PIPE_MIN_POS, BBConstants.PRESSURE_PIPE_MAX_POS);
        boundingBoxes[1] = new Cuboid6(BBConstants.PRESSURE_PIPE_MIN_POS, BBConstants.PRESSURE_PIPE_MAX_POS, BBConstants.PRESSURE_PIPE_MIN_POS, BBConstants.PRESSURE_PIPE_MAX_POS, 1.0F, BBConstants.PRESSURE_PIPE_MAX_POS);
        boundingBoxes[2] = new Cuboid6(BBConstants.PRESSURE_PIPE_MIN_POS, BBConstants.PRESSURE_PIPE_MIN_POS, 0.0F, BBConstants.PRESSURE_PIPE_MAX_POS, BBConstants.PRESSURE_PIPE_MAX_POS, BBConstants.PRESSURE_PIPE_MIN_POS);
        boundingBoxes[3] = new Cuboid6(BBConstants.PRESSURE_PIPE_MIN_POS, BBConstants.PRESSURE_PIPE_MIN_POS, BBConstants.PRESSURE_PIPE_MAX_POS, BBConstants.PRESSURE_PIPE_MAX_POS, BBConstants.PRESSURE_PIPE_MAX_POS, 1.0F);
        boundingBoxes[4] = new Cuboid6(0.0F, BBConstants.PRESSURE_PIPE_MIN_POS, BBConstants.PRESSURE_PIPE_MIN_POS, BBConstants.PRESSURE_PIPE_MIN_POS, BBConstants.PRESSURE_PIPE_MAX_POS, BBConstants.PRESSURE_PIPE_MAX_POS);
        boundingBoxes[5] = new Cuboid6(BBConstants.PRESSURE_PIPE_MAX_POS, BBConstants.PRESSURE_PIPE_MIN_POS, BBConstants.PRESSURE_PIPE_MIN_POS, 1.0F, BBConstants.PRESSURE_PIPE_MAX_POS, BBConstants.PRESSURE_PIPE_MAX_POS);
        boundingBoxes[6] = new Cuboid6(BBConstants.PRESSURE_PIPE_MIN_POS, BBConstants.PRESSURE_PIPE_MIN_POS, BBConstants.PRESSURE_PIPE_MIN_POS, BBConstants.PRESSURE_PIPE_MAX_POS, BBConstants.PRESSURE_PIPE_MAX_POS, BBConstants.PRESSURE_PIPE_MAX_POS);
    }

    public PartPressureTube(){}

    public PartPressureTube(TileEntityPressureTube tube){
        this.tube = tube.setPart(this);
    }

    @Override
    public IAirHandler getAirHandler(){
        return tube.getAirHandler();
    }

    @Override
    public void load(NBTTagCompound nbt){
        if(nbt.hasKey("tube")) {//TODO remove legacy
            nbt = nbt.getCompoundTag("tube");
        }
        tube.readFromNBTI(nbt);
    }

    @Override
    public void save(NBTTagCompound nbt){
        NBTTagCompound tag = new NBTTagCompound();
        tube.writeToNBTI(tag);
        nbt.setTag("tube", tag);
    }

    protected TileEntityPressureTube getNewTube(){
        return new TileEntityPressureTube();
    }

    @Override
    public void update(){
        //Log.info("sides connected " + world().isRemote + ": " + Arrays.toString(sidesConnected));
        if(Config.convertMultipartsToBlocks && !world().isRemote) {
            Log.info("Converting Pressure Tube part to Pressure Tube block at " + x() + ", " + y() + ", " + z());
            Block pressureTube = Block.getBlockFromItem(getItem().getItem());
            world().setBlock(x(), y(), z(), pressureTube);
            TileEntityPressureTube t = (TileEntityPressureTube)world().getTileEntity(x(), y(), z());
            NBTTagCompound tag = new NBTTagCompound();
            tube.writeToNBTI(tag);
            t.readFromNBT(tag);
            world().notifyBlocksOfNeighborChange(x(), y(), z(), pressureTube);
            return;
        }
        tube.updateEntityI();
    }

    @Override
    public boolean isConnectedTo(ForgeDirection side){
        return tube.isConnectedTo(side);
    }

    public boolean passesOcclusionTest(ForgeDirection side){
        TubeModule[] modules = tube.modules;
        tube.modules = new TubeModule[6];
        boolean result = tile() != null && tile().canAddPart(new NormallyOccludedPart(boundingBoxes[side.ordinal()]));
        tube.modules = modules;
        return result;
    }

    public TileEntityPressureTube getTube(){
        return tube;
    }

    @Override
    public void onWorldJoin(){
        tube.setWorldObj(world());
        tube.xCoord = x();
        tube.yCoord = y();
        tube.zCoord = z();
    }

    @Override
    public void onPartChanged(TMultiPart part){
        onNeighborChanged();
    }

    @Override
    public void onNeighborChanged(){
        if(!world().isRemote) {
            tube.onNeighborTileUpdate();
            tube.onNeighborChange();
            tube.onNeighborBlockUpdate();
            sendDescriptionPacket();
        }
    }

    @Override
    public boolean activate(EntityPlayer player, MovingObjectPosition hit, ItemStack item){
        boolean result = Blockss.pressureTube.onBlockActivated(player.worldObj, x(), y(), z(), player, hit.sideHit, (float)hit.hitVec.xCoord, (float)hit.hitVec.yCoord, (float)hit.hitVec.zCoord);
        if(result) onNeighborChanged();
        return result ? true : super.activate(player, hit, item);
    }

    @Override
    public Cuboid6 getRenderBounds(){
        return Cuboid6.full;
    }

    @Override
    public String getType(){
        return "tile.pressureTube";
    }

    @Override
    public int getHollowSize(int side){
        if(tube.modules[side] != null) {
            return Math.min(12, (int)(tube.modules[side].getWidth() * 16));
        }
        return 4;
    }

    @Override
    public Iterable<ItemStack> getDrops(){
        List<ItemStack> drops = BlockPressureTube.getModuleDrops(getTube());
        drops.add(getItem());
        return drops;
    }

    @Override
    public ItemStack pickItem(MovingObjectPosition hit){
        return getItem();
    }

    public ItemStack getItem(){
        return new ItemStack(Blockss.pressureTube);
    }

    @Override
    public Iterable<Cuboid6> getOcclusionBoxes(){
        List<Cuboid6> boxes = new ArrayList<Cuboid6>();
        boxes.add(boundingBoxes[6]);
        for(int i = 0; i < 6; i++) {
            if(tube.modules[i] != null) boxes.add(boundingBoxes[i]);//The full bounding box of modules is too big for hollow covers.
        }
        return boxes;
    }

    @Override
    public boolean occlusionTest(TMultiPart npart){
        return NormalOcclusionTest.apply(this, npart);
    }

    @Override
    public Iterable<Cuboid6> getCollisionBoxes(){
        List<Cuboid6> boxes = new ArrayList<Cuboid6>();
        boxes.add(boundingBoxes[6]);
        for(int i = 0; i < 6; i++) {
            if(tube.sidesConnected[i]) boxes.add(boundingBoxes[i]);
            if(tube.modules[i] != null) boxes.add(new Cuboid6(tube.modules[i].boundingBoxes[i]));
        }
        return boxes;
    }

    @Override
    public Iterable<IndexedCuboid6> getSubParts(){
        Iterable<Cuboid6> boxList = getCollisionBoxes();
        LinkedList<IndexedCuboid6> partList = new LinkedList<IndexedCuboid6>();
        for(Cuboid6 c : boxList)
            partList.add(new IndexedCuboid6(0, c));
        return partList;
    }

    @Override
    public int getSlotMask(){
        return PartMap.CENTER.mask;
    }

    @SideOnly(Side.CLIENT)
    private static ModelPressureTube tubeModel;

    @SideOnly(Side.CLIENT)
    @Override
    public void renderDynamic(Vector3 pos, float partialTicks, int renderPass){
        if(renderPass == 0) {
            /*   GL11.glPushMatrix(); // start
               // GL11.glDisable(GL11.GL_TEXTURE_2D);
               // GL11.glEnable(GL11.GL_BLEND);
               // GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
               FMLClientHandler.instance().getClient().getTextureManager().bindTexture(getTexture());

               // GL11.glColor4f(0.82F, 0.56F, 0.09F, 1.0F);
               GL11.glTranslatef((float)pos.x + 0.5F, (float)pos.y + 1.5F, (float)pos.z + 0.5F); // size
               GL11.glRotatef(0, 0.0F, 1.0F, 0.0F);

               GL11.glScalef(1.0F, -1F, -1F);
               if(tubeModel == null) tubeModel = new ModelPressureTube();
               tubeModel.renderModel(0.0625F, tube.sidesConnected);
               GL11.glPopMatrix();*/
            TileEntityRendererDispatcher.instance.getSpecialRenderer(tube).renderTileEntityAt(tube, pos.x, pos.y, pos.z, partialTicks);

        }
    }

    private void sendDescriptionPacket(){
        sendDescUpdate();
    }

    @Override
    public void writeDesc(MCDataOutput packet){
        for(int i = 0; i < 6; i++) {
            packet.writeBoolean(tube.sidesConnected[i]);
        }
        NBTTagCompound tag = new NBTTagCompound();
        tube.writeToNBT(tag);
        packet.writeNBTTagCompound(tag);
    }

    @Override
    public void readDesc(MCDataInput packet){
        for(int i = 0; i < 6; i++) {
            tube.sidesConnected[i] = packet.readBoolean();
        }
        tube.readFromNBT(packet.readNBTTagCompound());
    }

    @Override
    public boolean canConnectRedstone(int side){
        side = side ^ 1;
        for(int i = 0; i < 6; i++) {
            if(tube.modules[i] != null) {
                if((side ^ 1) == i || i != side && tube.modules[i].isInline()) {//if we are on the same side, or when we have an 'in line' module that is not on the opposite side.
                    if(tube.modules[i] instanceof TubeModuleRedstoneEmitting) return true;
                }
            }
        }
        return false;
    }

    @Override
    public int strongPowerLevel(int side){
        return 0;
    }

    @Override
    public int weakPowerLevel(int side){
        side = side ^ 1;
        int redstoneLevel = 0;
        for(int i = 0; i < 6; i++) {
            if(tube.modules[i] != null) {
                if((side ^ 1) == i || i != side && tube.modules[i].isInline()) {//if we are on the same side, or when we have an 'in line' module that is not on the opposite side.
                    redstoneLevel = Math.max(redstoneLevel, tube.modules[i].getRedstoneLevel());
                }
            }
        }
        return redstoneLevel;
    }

    @Override
    public boolean rotateBlock(World world, EntityPlayer player, int x, int y, int z, ForgeDirection side){
        return ((IPneumaticWrenchable)Blockss.pressureTube).rotateBlock(world, player, x, y, z, side);
    }
}
