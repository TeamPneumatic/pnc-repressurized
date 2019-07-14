package me.desht.pneumaticcraft.common.block.tubes;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public abstract class TubeModuleRedstoneReceiving extends TubeModule {
    private int redstoneLevel;

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        redstoneLevel = tag.getInt("redstone");
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        tag.putInt("redstone", redstoneLevel);
    }

    @Override
    public void addInfo(List<String> curInfo) {
        super.addInfo(curInfo);
        curInfo.add("Applied redstone: " + TextFormatting.WHITE + redstoneLevel);
    }

    @Override
    public void onNeighborBlockUpdate() {
        redstoneLevel = pressureTube.world().getRedstonePowerFromNeighbors(pressureTube.pos());
    }

    public int getReceivingRedstoneLevel() {
        return redstoneLevel;
    }

    public float getThreshold() {
        return getThreshold(redstoneLevel);
    }

    @Override
    public boolean hasGui() {
        return true;
    }

    @Override
    public void update() {
        if (upgraded && !advancedConfig && higherBound != lowerBound) {
            higherBound = lowerBound;
            if (!getTube().world().isRemote) sendDescriptionPacket();
        }
    }
}
