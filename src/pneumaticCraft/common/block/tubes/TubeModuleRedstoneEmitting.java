package pneumaticCraft.common.block.tubes;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;

public abstract class TubeModuleRedstoneEmitting extends TubeModule{
    protected int redstone;

    /**
     * 
     * @param level
     * @return true if the redstone has changed compared to last time.
     */
    protected boolean setRedstone(int level){
        level = Math.max(level, 0);
        level = Math.min(level, 15);
        if(redstone != level) {
            redstone = level;
            updateNeighbors();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int getRedstoneLevel(){
        return redstone;
    }

    @Override
    public void addInfo(List<String> curInfo){
        curInfo.add("Emitting redstone: " + EnumChatFormatting.WHITE + redstone);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        tag.setInteger("redstone", redstone);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        redstone = tag.getInteger("redstone");
    }

    @Override
    public void update(){
        if(upgraded && !advancedConfig) {
            if(higherBound < lowerBound) {
                if(higherBound != lowerBound - 0.1F) {
                    higherBound = lowerBound - 0.1F;
                    sendDescriptionPacket();
                }
            } else {
                if(higherBound != lowerBound + 0.1F) {
                    higherBound = lowerBound + 0.1F;
                    sendDescriptionPacket();
                }
            }
        }
    }
}
