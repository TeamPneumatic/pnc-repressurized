package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;

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
    public void addInfo(List<ITextComponent> curInfo) {
        super.addInfo(curInfo);

        curInfo.add(PneumaticCraftUtils.xlate("waila.redstoneModule.receiving", redstoneLevel));
        curInfo.add(PneumaticCraftUtils.xlate("waila.tubeModule.threshold", PneumaticCraftUtils.roundNumberTo(getThreshold(), 1)));
    }

    @Override
    public void onNeighborBlockUpdate() {
        redstoneLevel = pressureTube.getWorld().getRedstonePowerFromNeighbors(pressureTube.getPos());
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
            if (!getTube().getWorld().isRemote) sendDescriptionPacket();
        }
    }
}
