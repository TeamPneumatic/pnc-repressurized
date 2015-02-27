package pneumaticCraft.common.thirdparty.fmp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import pneumaticCraft.api.tileentity.IAirHandler;
import pneumaticCraft.api.tileentity.IPneumaticMachine;
import pneumaticCraft.client.model.ModelPressureTube;
import pneumaticCraft.common.Config;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.block.tubes.IPneumaticPosProvider;
import pneumaticCraft.common.block.tubes.TubeModule;
import pneumaticCraft.common.thirdparty.ModInteractionUtils;
import pneumaticCraft.common.tileentity.TileEntityPneumaticBase;
import pneumaticCraft.common.tileentity.TileEntityPressureTube;
import pneumaticCraft.lib.BBConstants;
import pneumaticCraft.lib.Log;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.lib.Textures;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.ISidedHollowConnect;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.NormalOcclusionTest;
import codechicken.multipart.NormallyOccludedPart;
import codechicken.multipart.PartMap;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TSlottedPart;
import codechicken.multipart.TileMultipart;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartPressureTube extends TMultiPart implements IPneumaticPosProvider, TSlottedPart, JNormalOcclusion,
        ISidedHollowConnect{

    private final TileEntityPneumaticBase airHandler;
    public boolean[] sidesConnected;
    protected TubeModule[] convertedModules;//only used while converting a non FMP to a FMP part.
    private int ticksExisted;

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

    public PartPressureTube(){
        this(PneumaticValues.DANGER_PRESSURE_PRESSURE_TUBE, PneumaticValues.MAX_PRESSURE_PRESSURE_TUBE, PneumaticValues.VOLUME_PRESSURE_TUBE);
    }

    public PartPressureTube(TubeModule[] tubeModules){
        this();
        convertedModules = tubeModules;
    }

    public PartPressureTube(float dangerPressurePressureTube, float maxPressurePressureTube, int volumePressureTube){
        airHandler = new TileEntityPneumaticBase(dangerPressurePressureTube, maxPressurePressureTube, volumePressureTube);
        sidesConnected = new boolean[6];
    }

    @Override
    public IAirHandler getAirHandler(){
        return airHandler;
    }

    @Override
    public void load(NBTTagCompound nbt){
        airHandler.readFromNBTI(nbt);
    }

    @Override
    public void save(NBTTagCompound nbt){
        airHandler.writeToNBTI(nbt);
    }

    @Override
    public void update(){
        //Log.info("sides connected " + world().isRemote + ": " + Arrays.toString(sidesConnected));
        if(Config.convertMultipartsToBlocks && !world().isRemote) {
            Log.info("Converting Pressure Tube part to Pressure Tube block at " + x() + ", " + y() + ", " + z());
            world().setBlock(x(), y(), z(), Block.getBlockFromItem(getItem().getItem()));
            TileEntityPressureTube tube = (TileEntityPressureTube)world().getTileEntity(x(), y(), z());
            for(PartTubeModule module : FMP.getMultiParts(tile(), PartTubeModule.class)) {
                tube.setModule(module.getModule(), module.getModule().getDirection());
                tube.updateConnections(world(), x(), y(), z());
                world().notifyBlocksOfNeighborChange(x(), y(), z(), Blockss.pressureTube, module.getModule().getDirection().getOpposite().ordinal());
            }
            return;
        }
        if(convertedModules != null && !world().isRemote) {//when we convert a tube block to a tube part, look for attached modules.
            for(TubeModule module : convertedModules) {
                if(module != null) {
                    PartTubeModule part = (PartTubeModule)MultiPartRegistry.createPart(module.getType(), false);
                    part.setModule(module);
                    part.setDirection(module.getDirection());
                    module.setTube(this);
                    TileMultipart.addPart(world(), new BlockCoord(x(), y(), z()), part);
                }
            }
            convertedModules = null;
        }
        if(ticksExisted++ == 2) {
            if(!world().isRemote) world().notifyBlocksOfNeighborChange(x(), y(), z(), Blockss.pressureTube);
        }

        airHandler.updateEntityI();

        List<Pair<ForgeDirection, IPneumaticMachine>> teList = airHandler.getConnectedPneumatics();

        if(teList.size() == 1 && !world().isRemote) {
            for(Pair<ForgeDirection, IPneumaticMachine> entry : teList) {
                if(isConnectedTo(entry.getKey().getOpposite())) airHandler.airLeak(entry.getKey().getOpposite());
            }
        }
    }

    @Override
    public boolean isConnectedTo(ForgeDirection side){
        //if(getBlockMetadata() > 0 && getBlockMetadata() != BlockPressureTube.AIR_GRATE_TUBE_META) {
        //   return sidesConnected[side.ordinal()];
        // } else {
        boolean[] tempConnections = sidesConnected;
        sidesConnected = new boolean[6];
        boolean canConnect = tile() != null && tile().canAddPart(new NormallyOccludedPart(boundingBoxes[side.ordinal()]));
        sidesConnected = tempConnections;
        return canConnect;
        // }
    }

    @Override
    public void onWorldJoin(){
        airHandler.validateI(getTile());
    }

    @Override
    public void onPartChanged(TMultiPart part){
        onNeighborChanged();
    }

    @Override
    public void onNeighborChanged(){
        if(!world().isRemote) {
            airHandler.onNeighborTileUpdate();
            airHandler.onNeighborChange();
            updateConnections();
        }
    }

    public void updateConnections(){
        sidesConnected = new boolean[6];
        for(ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
            if(isConnectedTo(direction)) {
                TileEntity te = airHandler.getTileCache()[direction.ordinal()].getTileEntity();
                IPneumaticMachine machine = ModInteractionUtils.getInstance().getMachine(te);
                if(machine != null) {
                    sidesConnected[direction.ordinal()] = machine.isConnectedTo(direction.getOpposite());
                }
            }
        }
        int sidesCount = 0;
        for(int i = 0; i < 6; i++) {
            if(sidesConnected[i]) sidesCount++;
        }
        if(sidesCount == 1) {
            for(int i = 0; i < 6; i++) {
                if(sidesConnected[i]) {
                    if(isConnectedTo(ForgeDirection.getOrientation(i).getOpposite())) sidesConnected[i ^ 1] = true;
                    break;
                }
            }
        }
        sendDescriptionPacket();
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
    public int getHollowSize(int arg0){
        return 4;
    }

    @Override
    public Iterable<ItemStack> getDrops(){
        return Arrays.asList(new ItemStack[]{getItem()});
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
        return boxes;
    }

    @Override
    public boolean occlusionTest(TMultiPart npart){
        if(convertedModules != null) {
            for(TubeModule module : convertedModules) {
                if(module != null) return false;//FIXME remove when FMP crash fixed
            }
        }
        return NormalOcclusionTest.apply(this, npart);
    }

    @Override
    public Iterable<Cuboid6> getCollisionBoxes(){
        List<Cuboid6> boxes = (List<Cuboid6>)getOcclusionBoxes();
        for(int i = 0; i < 6; i++) {
            if(sidesConnected[i]) boxes.add(boundingBoxes[i]);
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
            GL11.glPushMatrix(); // start
            // GL11.glDisable(GL11.GL_TEXTURE_2D);
            // GL11.glEnable(GL11.GL_BLEND);
            // GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            FMLClientHandler.instance().getClient().getTextureManager().bindTexture(getTexture());

            // GL11.glColor4f(0.82F, 0.56F, 0.09F, 1.0F);
            GL11.glTranslatef((float)pos.x + 0.5F, (float)pos.y + 1.5F, (float)pos.z + 0.5F); // size
            GL11.glRotatef(0, 0.0F, 1.0F, 0.0F);

            GL11.glScalef(1.0F, -1F, -1F); // to make your block have a normal
                                           // positioning. comment out to see what
                                           // happens
            if(tubeModel == null) tubeModel = new ModelPressureTube();
            tubeModel.renderModel(0.0625F, sidesConnected);
            GL11.glPopMatrix();
        }
    }

    protected ResourceLocation getTexture(){
        return Textures.MODEL_PRESSURE_TUBE;
    }

    private void sendDescriptionPacket(){
        sendDescUpdate();
    }

    @Override
    public void writeDesc(MCDataOutput packet){
        for(int i = 0; i < 6; i++) {
            packet.writeBoolean(sidesConnected[i]);
        }
    }

    @Override
    public void readDesc(MCDataInput packet){
        for(int i = 0; i < 6; i++) {
            sidesConnected[i] = packet.readBoolean();
        }
    }
}
