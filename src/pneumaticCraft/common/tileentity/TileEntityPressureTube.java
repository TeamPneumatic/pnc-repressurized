package pneumaticCraft.common.tileentity;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.tuple.Pair;

import pneumaticCraft.api.tileentity.IAirHandler;
import pneumaticCraft.api.tileentity.IPneumaticMachine;
import pneumaticCraft.api.tileentity.ISidedPneumaticMachine;
import pneumaticCraft.common.block.tubes.IInfluenceDispersing;
import pneumaticCraft.common.block.tubes.ModuleAirGrate;
import pneumaticCraft.common.block.tubes.ModuleFlowDetector;
import pneumaticCraft.common.block.tubes.ModulePressureGauge;
import pneumaticCraft.common.block.tubes.ModuleRegistrator;
import pneumaticCraft.common.block.tubes.ModuleRegulatorTube;
import pneumaticCraft.common.block.tubes.ModuleSafetyValve;
import pneumaticCraft.common.block.tubes.TubeModule;
import pneumaticCraft.common.network.DescSynced;
import pneumaticCraft.common.thirdparty.ModInteractionUtils;
import pneumaticCraft.common.thirdparty.fmp.PartPressureTube;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.Log;
import pneumaticCraft.lib.ModIds;
import pneumaticCraft.lib.PneumaticValues;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityPressureTube extends TileEntityPneumaticBase{
    @DescSynced
    public boolean[] sidesConnected = new boolean[6];
    public TubeModule[] modules = new TubeModule[6];

    private Object part;

    public TileEntityPressureTube(){
        super(PneumaticValues.DANGER_PRESSURE_PRESSURE_TUBE, PneumaticValues.MAX_PRESSURE_PRESSURE_TUBE, PneumaticValues.VOLUME_PRESSURE_TUBE);
    }

    public TileEntityPressureTube(float dangerPressurePressureTube, float maxPressurePressureTube,
            int volumePressureTube){
        super(dangerPressurePressureTube, maxPressurePressureTube, volumePressureTube);
    }

    public TileEntityPressureTube setPart(Object part){
        this.part = part;
        for(TubeModule module : modules) {
            if(module != null) module.shouldDrop = false;
        }
        return this;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt){
        super.readFromNBT(nbt);
        for(int i = 0; i < 6; i++) {
            sidesConnected[i] = nbt.getBoolean("sideConnected" + i);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt){
        super.writeToNBT(nbt);
        for(int i = 0; i < 6; i++) {
            nbt.setBoolean("sideConnected" + i, sidesConnected[i]);
        }
    }

    @Override
    public void writeToPacket(NBTTagCompound tag){
        super.writeToPacket(tag);
        writeModulesToNBT(tag);
    }

    public void writeModulesToNBT(NBTTagCompound tag){
        NBTTagList moduleList = new NBTTagList();
        for(int i = 0; i < modules.length; i++) {
            if(modules[i] != null) {
                NBTTagCompound moduleTag = new NBTTagCompound();
                moduleTag.setString("type", modules[i].getType());
                modules[i].writeToNBT(moduleTag);
                moduleTag.setInteger("side", i);
                moduleList.appendTag(moduleTag);
            }
        }
        tag.setTag("modules", moduleList);
    }

    @Override
    public void readFromPacket(NBTTagCompound tag){
        super.readFromPacket(tag);
        modules = new TubeModule[6];
        NBTTagList moduleList = tag.getTagList("modules", 10);
        for(int i = 0; i < moduleList.tagCount(); i++) {
            NBTTagCompound moduleTag = moduleList.getCompoundTagAt(i);
            TubeModule module = ModuleRegistrator.getModule(moduleTag.getString("type"));
            module.readFromNBT(moduleTag);
            setModule(module, ForgeDirection.getOrientation(moduleTag.getInteger("side")));
        }
        if(worldObj != null && worldObj.isRemote) {
            rerenderChunk();
        }
    }

    @Override
    public void updateEntity(){
        super.updateEntity();

        for(TubeModule module : modules) {
            if(module != null) {
                module.shouldDrop = true;
                module.update();
            }
        }

        List<Pair<ForgeDirection, IAirHandler>> teList = getConnectedPneumatics();

        boolean hasModules = false;
        for(TubeModule module : modules) {
            if(module != null) {
                hasModules = true;
                break;
            }
        }
        if(!hasModules && teList.size() - specialConnectedHandlers.size() == 1 && !worldObj.isRemote) {
            for(Pair<ForgeDirection, IAirHandler> entry : teList) {
                if(entry.getKey() != ForgeDirection.UNKNOWN && modules[entry.getKey().getOpposite().ordinal()] == null && isConnectedTo(entry.getKey().getOpposite())) airLeak(entry.getKey().getOpposite());
            }
        }
    }

    @Override
    protected void onAirDispersion(int amount, ForgeDirection side){
        if(side != ForgeDirection.UNKNOWN) {
            int intSide = side/*.getOpposite()*/.ordinal();
            if(modules[intSide] instanceof IInfluenceDispersing) {
                ((IInfluenceDispersing)modules[intSide]).onAirDispersion(amount);
            }
        }
    }

    @Override
    protected int getMaxDispersion(ForgeDirection side){
        if(side != ForgeDirection.UNKNOWN) {
            int intSide = side/*.getOpposite()*/.ordinal();
            if(modules[intSide] instanceof IInfluenceDispersing) {
                return ((IInfluenceDispersing)modules[intSide]).getMaxDispersion();
            }
        }
        return Integer.MAX_VALUE;
    }

    @Override
    protected void onFirstServerUpdate(){
        legacyHelper();
    }

    //TODO legacy, remove after a while
    private void legacyHelper(){
        if(getBlockMetadata() > 0) {
            switch(getBlockMetadata()){
                case 1:
                    legacyAddModule(new ModuleFlowDetector());
                    break;
                case 2:
                    legacyAddModule(new ModuleSafetyValve());
                    break;
                case 3:
                    legacyAddModule(new ModuleRegulatorTube());
                    break;
                case 4:
                    legacyAddModule(new ModuleAirGrate());
                    break;
                case 5:
                    legacyAddModule(new ModulePressureGauge());
            }
            NBTTagCompound tag = new NBTTagCompound();
            writeToNBT(tag);
            worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 3);
            ((TileEntityPressureTube)worldObj.getTileEntity(xCoord, yCoord, zCoord)).readFromNBT(tag);
        }
    }

    private void legacyAddModule(TubeModule module){
        for(int i = 0; i < 6; i++) {
            if(sidesConnected[i] == module.isInline()) {
                Log.info("Converting legacy Pressure Tube to tube with module: " + module.getType() + " at " + xCoord + ", " + yCoord + ", " + zCoord);
                setModule(module, ForgeDirection.getOrientation(i));
                return;
            }
        }
        Log.warning("Converting legacy Pressure Tube. TUBES ARE ALL CONNECTED, force connecting! Module: " + module.getType() + " at " + xCoord + ", " + yCoord + ", " + zCoord);
        setModule(module, ForgeDirection.getOrientation(0));
    }

    public void setModule(TubeModule module, ForgeDirection side){
        if(module != null) {
            module.setDirection(side);
            module.setTube(this);
        }
        modules[side.ordinal()] = module;
        if(worldObj != null && !worldObj.isRemote) {
            if(part != null) updatePart();
            sendDescriptionPacket();
        }
    }

    @Override
    public boolean isConnectedTo(ForgeDirection side){
        return (modules[side.ordinal()] == null || modules[side.ordinal()].isInline()) && (part == null || ModInteractionUtils.getInstance().isMultipartWiseConnected(part, side));
    }

    @Override
    public void onNeighborTileUpdate(){
        super.onNeighborTileUpdate();
        updateConnections(worldObj, xCoord, yCoord, zCoord);
        for(TubeModule module : modules) {
            if(module != null) module.onNeighborTileUpdate();
        }
    }

    @Override
    public void onNeighborBlockUpdate(){
        super.onNeighborBlockUpdate();
        updateConnections(worldObj, xCoord, yCoord, zCoord);
        for(TubeModule module : modules) {
            if(module != null) module.onNeighborBlockUpdate();
        }
    }

    public void updateConnections(World world, int x, int y, int z){
        sidesConnected = new boolean[6];
        boolean hasModule = false;
        for(ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
            TileEntity te = getTileCache()[direction.ordinal()].getTileEntity();
            IPneumaticMachine machine = ModInteractionUtils.getInstance().getMachine(te);
            if(machine != null) {
                sidesConnected[direction.ordinal()] = isConnectedTo(direction) && machine.isConnectedTo(direction.getOpposite());
            } else if(te instanceof ISidedPneumaticMachine) {
                sidesConnected[direction.ordinal()] = ((ISidedPneumaticMachine)te).getAirHandler(direction.getOpposite()) != null;
            }
            if(modules[direction.ordinal()] != null) {
                hasModule = true;
            }
        }
        int sidesCount = 0;
        for(boolean bool : sidesConnected) {
            if(bool) sidesCount++;
        }
        if(sidesCount == 1 && !hasModule) {
            for(int i = 0; i < 6; i++) {
                if(sidesConnected[i]) {
                    if(isConnectedTo(ForgeDirection.getOrientation(i).getOpposite())) sidesConnected[i ^ 1] = true;
                    break;
                }
            }
        }
        for(int i = 0; i < 6; i++) {
            if(modules[i] != null && modules[i].isInline()) sidesConnected[i] = false;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox(){
        return AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 1, zCoord + 1);
    }

    @Override
    public void printManometerMessage(EntityPlayer player, List<String> text){
        super.printManometerMessage(player, text);
        MovingObjectPosition mop = PneumaticCraftUtils.getEntityLookedObject(player);
        if(mop != null && mop.hitInfo instanceof ForgeDirection) {
            ForgeDirection dir = (ForgeDirection)mop.hitInfo;
            if(dir != ForgeDirection.UNKNOWN && modules[dir.ordinal()] != null) {
                modules[dir.ordinal()].addInfo(text);
            }
        }
    }

    @Override
    @Optional.Method(modid = ModIds.FMP)
    public void sendDescriptionPacket(){
        if(part != null && !worldObj.isRemote) {
            ((PartPressureTube)part).sendDescUpdate();
        }
        super.sendDescriptionPacket();
    }

    @Optional.Method(modid = ModIds.FMP)
    public void updatePart(){
        ((PartPressureTube)part).onNeighborChanged();
    }
}
