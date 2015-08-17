package pneumaticCraft.common.block.tubes;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.model.IBaseModel;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketOpenTubeModuleGui;
import pneumaticCraft.common.thirdparty.ModInteractionUtils;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.BBConstants;
import pneumaticCraft.lib.ModIds;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.Optional;

public abstract class TubeModule implements ISidedPart{
    protected IPneumaticPosProvider pressureTube;
    protected ForgeDirection dir = ForgeDirection.UP;
    public AxisAlignedBB[] boundingBoxes = new AxisAlignedBB[6];
    protected boolean upgraded;
    public float lowerBound = 7.5F, higherBound = 0, maxValue = 30;
    private boolean fake;
    public boolean advancedConfig;
    public boolean shouldDrop;

    public TubeModule(){
        double width = getWidth() / 2;
        double height = getHeight();

        boundingBoxes[0] = AxisAlignedBB.getBoundingBox(0.5 - width, BBConstants.PRESSURE_PIPE_MIN_POS - height, 0.5 - width, 0.5 + width, BBConstants.PRESSURE_PIPE_MIN_POS, 0.5 + width);
        boundingBoxes[1] = AxisAlignedBB.getBoundingBox(0.5 - width, BBConstants.PRESSURE_PIPE_MAX_POS, 0.5 - width, 0.5 + width, BBConstants.PRESSURE_PIPE_MAX_POS + height, 0.5 + width);
        boundingBoxes[2] = AxisAlignedBB.getBoundingBox(0.5 - width, 0.5 - width, BBConstants.PRESSURE_PIPE_MIN_POS - height, 0.5 + width, 0.5 + width, BBConstants.PRESSURE_PIPE_MIN_POS);
        boundingBoxes[3] = AxisAlignedBB.getBoundingBox(0.5 - width, 0.5 - width, BBConstants.PRESSURE_PIPE_MAX_POS, 0.5 + width, 0.5 + width, BBConstants.PRESSURE_PIPE_MAX_POS + height);
        boundingBoxes[4] = AxisAlignedBB.getBoundingBox(BBConstants.PRESSURE_PIPE_MIN_POS - height, 0.5 - width, 0.5 - width, BBConstants.PRESSURE_PIPE_MIN_POS, 0.5 + width, 0.5 + width);
        boundingBoxes[5] = AxisAlignedBB.getBoundingBox(BBConstants.PRESSURE_PIPE_MAX_POS, 0.5 - width, 0.5 - width, BBConstants.PRESSURE_PIPE_MAX_POS + height, 0.5 + width, 0.5 + width);
    }

    public void markFake(){
        fake = true;
    }

    public boolean isFake(){
        return fake;
    }

    public void setTube(IPneumaticPosProvider pressureTube){
        this.pressureTube = pressureTube;
    }

    public IPneumaticPosProvider getTube(){
        return pressureTube;
    }

    public double getWidth(){
        return BBConstants.PRESSURE_PIPE_MAX_POS - BBConstants.PRESSURE_PIPE_MIN_POS;
    }

    protected double getHeight(){
        return BBConstants.PRESSURE_PIPE_MIN_POS;
    }

    public float getThreshold(int redstone){
        double slope = (higherBound - lowerBound) / 15;
        double threshold = lowerBound + slope * redstone;
        return (float)threshold;
    }

    /**
     * Returns the item that this part drops.
     * @return
     */
    public List<ItemStack> getDrops(){
        List<ItemStack> drops = new ArrayList<ItemStack>();
        if(shouldDrop) {
            drops.add(new ItemStack(ModuleRegistrator.getModuleItem(getType())));
            if(upgraded) drops.add(new ItemStack(Itemss.advancedPCB));
        }
        return drops;
    }

    @Override
    public void setDirection(ForgeDirection dir){
        this.dir = dir;
    }

    public ForgeDirection getDirection(){
        return dir;
    }

    public void readFromNBT(NBTTagCompound nbt){
        dir = ForgeDirection.getOrientation(nbt.getInteger("dir"));
        upgraded = nbt.getBoolean("upgraded");
        lowerBound = nbt.getFloat("lowerBound");
        higherBound = nbt.getFloat("higherBound");
        advancedConfig = nbt.hasKey("advancedConfig") ? nbt.getBoolean("advancedConfig") : true;
    }

    public void writeToNBT(NBTTagCompound nbt){
        nbt.setInteger("dir", dir.ordinal());
        nbt.setBoolean("upgraded", upgraded);
        nbt.setFloat("lowerBound", lowerBound);
        nbt.setFloat("higherBound", higherBound);
        nbt.setBoolean("advancedConfig", advancedConfig);
    }

    @Optional.Method(modid = ModIds.FMP)
    public void writeDesc(MCDataOutput data){
        data.writeInt(dir.ordinal());
    }

    @Optional.Method(modid = ModIds.FMP)
    public void readDesc(MCDataInput data){
        dir = ForgeDirection.getOrientation(data.readInt());
    }

    public void update(){}

    public void onNeighborTileUpdate(){}

    public void onNeighborBlockUpdate(){}

    /**
     * Used by multiparts and/or by NBT saving.
     * @return
     */
    public abstract String getType();

    public void renderDynamic(double x, double y, double z, float partialTicks, int renderPass, boolean itemRender){
        if(renderPass == 0) {
            GL11.glPushMatrix(); // start
            // GL11.glDisable(GL11.GL_TEXTURE_2D);
            // GL11.glEnable(GL11.GL_BLEND);
            // GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            // GL11.glColor4f(0.82F, 0.56F, 0.09F, 1.0F);
            GL11.glTranslatef((float)x + 0.5F, (float)y + 1.5F, (float)z + 0.5F); // size
            GL11.glRotatef(0, 0.0F, 1.0F, 0.0F);

            GL11.glScalef(1.0F, -1F, -1F); // to make your block have a normal
                                           // positioning. comment out to see what
                                           // happens

            FMLClientHandler.instance().getClient().getTextureManager().bindTexture(getModel().getModelTexture(null));

            PneumaticCraftUtils.rotateMatrixByMetadata(dir.ordinal());
            renderModule();
            getModel().renderStatic(0.0625F, null);
            getModel().renderDynamic(0.0625F, null, partialTicks);
            GL11.glPopMatrix();
        }
    }

    protected void renderModule(){}

    public abstract IBaseModel getModel();

    public int getRedstoneLevel(){
        return 0;
    }

    protected void updateNeighbors(){
        pressureTube.world().notifyBlocksOfNeighborChange(pressureTube.x(), pressureTube.y(), pressureTube.z(), pressureTube.world().getBlock(pressureTube.x(), pressureTube.y(), pressureTube.z()));
    }

    public boolean isInline(){
        return false;
    }

    public void sendDescriptionPacket(){
        ModInteractionUtils.getInstance().sendDescriptionPacket(pressureTube);
    }

    public void addInfo(List<String> curInfo){}

    public void addItemDescription(List<String> curInfo){}

    public boolean canUpgrade(){
        return true;
    }

    public void upgrade(){
        upgraded = true;
    }

    public boolean isUpgraded(){
        return upgraded;
    }

    public boolean onActivated(EntityPlayer player){
        if(!player.worldObj.isRemote && upgraded && getGuiId() != null) {
            NetworkHandler.sendTo(new PacketOpenTubeModuleGui(getGuiId().ordinal(), pressureTube.x(), pressureTube.y(), pressureTube.z()), (EntityPlayerMP)player);
            return true;
        }
        return false;
    }

    protected abstract EnumGuiId getGuiId();

}
