package pneumaticCraft.common.tileentity;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.tuple.Pair;

import pneumaticCraft.api.tileentity.IPneumaticMachine;
import pneumaticCraft.common.PneumaticCraftUtils;
import pneumaticCraft.common.block.tubes.IInfluenceDispersing;
import pneumaticCraft.common.block.tubes.ModuleAirGrate;
import pneumaticCraft.common.block.tubes.ModuleFlowDetector;
import pneumaticCraft.common.block.tubes.ModulePressureGauge;
import pneumaticCraft.common.block.tubes.ModuleRegistrator;
import pneumaticCraft.common.block.tubes.ModuleRegulatorTube;
import pneumaticCraft.common.block.tubes.ModuleSafetyValve;
import pneumaticCraft.common.block.tubes.TubeModule;
import pneumaticCraft.common.thirdparty.ModInteractionUtils;
import pneumaticCraft.lib.Log;
import pneumaticCraft.lib.PneumaticValues;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityPressureTube extends TileEntityPneumaticBase{
    public boolean[] sidesConnected;

    public TubeModule[] modules = new TubeModule[6];
    private boolean firstRun = true;

    public TileEntityPressureTube(){
        super(PneumaticValues.DANGER_PRESSURE_PRESSURE_TUBE, PneumaticValues.MAX_PRESSURE_PRESSURE_TUBE, PneumaticValues.VOLUME_PRESSURE_TUBE);
        sidesConnected = new boolean[6];
    }

    public TileEntityPressureTube(float dangerPressurePressureTube, float maxPressurePressureTube,
            int volumePressureTube){
        super(dangerPressurePressureTube, maxPressurePressureTube, volumePressureTube);
        sidesConnected = new boolean[6];
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt){
        super.readFromNBT(nbt);
        for(int i = 0; i < 6; i++) {
            sidesConnected[i] = nbt.getBoolean("sideConnected" + i);
        }

        modules = new TubeModule[6];
        NBTTagList moduleList = nbt.getTagList("modules", 10);
        for(int i = 0; i < moduleList.tagCount(); i++) {
            NBTTagCompound moduleTag = moduleList.getCompoundTagAt(i);
            TubeModule module = ModuleRegistrator.getModule(moduleTag.getString("type"));
            module.readFromNBT(moduleTag);
            setModule(module, ForgeDirection.getOrientation(moduleTag.getInteger("side")));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt){
        super.writeToNBT(nbt);
        for(int i = 0; i < 6; i++) {
            nbt.setBoolean("sideConnected" + i, sidesConnected[i]);
        }

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
        nbt.setTag("modules", moduleList);
    }

    @Override
    public void updateEntity(){
        super.updateEntity();

        if(!worldObj.isRemote && firstRun) {
            firstRun = false;
            updateConnections(worldObj, xCoord, yCoord, zCoord);
            legacyHelper();
        }

        for(TubeModule module : modules) {
            if(module != null) module.update();
        }

        List<Pair<ForgeDirection, IPneumaticMachine>> teList = getConnectedPneumatics();

        if(teList.size() == 1 && !worldObj.isRemote) {
            for(Pair<ForgeDirection, IPneumaticMachine> entry : teList) {
                if(modules[entry.getKey().getOpposite().ordinal()] == null) airLeak(entry.getKey().getOpposite());
            }
        }
    }

    @Override
    protected int onAirDispersion(int amount, ForgeDirection side){
        if(side != ForgeDirection.UNKNOWN) {
            int intSide = side/*.getOpposite()*/.ordinal();
            if(modules[intSide] instanceof IInfluenceDispersing) {
                amount = ((IInfluenceDispersing)modules[intSide]).canDisperse(amount);
            }
        }
        return amount;
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
        if(worldObj != null && !worldObj.isRemote) sendDescriptionPacket();
    }

    @Override
    public boolean isConnectedTo(ForgeDirection side){
        return modules[side.ordinal()] == null || modules[side.ordinal()].isInline();
    }

    public void onNeighborBlockChange(){
        updateConnections(worldObj, xCoord, yCoord, zCoord);
        for(TubeModule module : modules) {
            if(module != null) module.onNeighborBlockUpdate();
        }
    }

    public void updateConnections(World world, int x, int y, int z){
        sidesConnected = new boolean[6];
        for(ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
            IPneumaticMachine machine = ModInteractionUtils.getInstance().getMachine(getTileCache()[direction.ordinal()].getTileEntity());
            if(machine != null) {
                sidesConnected[direction.ordinal()] = isConnectedTo(direction) && machine.isConnectedTo(direction.getOpposite());
            }
        }
        int sidesCount = 0;
        for(boolean bool : sidesConnected) {
            if(bool) sidesCount++;
        }
        if(sidesCount == 1) {
            for(int i = 0; i < 6; i++) {
                if(sidesConnected[i]) {
                    if(modules[i ^ 1] == null) sidesConnected[i ^ 1] = true;
                    break;
                }
            }
        }
        for(int i = 0; i < 6; i++) {
            if(modules[i] != null && modules[i].isInline()) sidesConnected[i] = false;
        }

        sendDescriptionPacket();
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
}
