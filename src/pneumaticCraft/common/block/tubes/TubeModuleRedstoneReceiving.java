package pneumaticCraft.common.block.tubes;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;

public abstract class TubeModuleRedstoneReceiving extends TubeModule{
    protected int redstoneLevel;

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        redstoneLevel = tag.getInteger("redstone");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        tag.setInteger("redstone", redstoneLevel);
    }

    @Override
    public void addInfo(List<String> curInfo){
        curInfo.add("Applied redstone: " + EnumChatFormatting.WHITE + redstoneLevel);
    }

    @Override
    public void onNeighborBlockUpdate(){
        redstoneLevel = 0;
        for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
            if(dir == side || isInline() && side != dir.getOpposite()) redstoneLevel = Math.max(redstoneLevel, pressureTube.world().getIndirectPowerLevelTo(pressureTube.x() + side.offsetX, pressureTube.y() + side.offsetY, pressureTube.z() + side.offsetZ, side.ordinal()));
        }
    }

    public int getReceivingRedstoneLevel(){
        return redstoneLevel;
    }

    public float getThreshold(){
        return getThreshold(redstoneLevel);
    }

    @Override
    protected EnumGuiId getGuiId(){
        return EnumGuiId.PRESSURE_MODULE;
    }

    @Override
    public void update(){
        if(upgraded && !advancedConfig && higherBound != lowerBound) {
            higherBound = lowerBound;
            if(!getTube().world().isRemote) sendDescriptionPacket();
        }
    }
}
