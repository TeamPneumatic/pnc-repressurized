package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.proxy.CommonProxy.EnumGuiId;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public abstract class TubeModuleRedstoneReceiving extends TubeModule {
    protected int redstoneLevel;

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        redstoneLevel = tag.getInteger("redstone");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("redstone", redstoneLevel);
    }

    @Override
    public void addInfo(List<String> curInfo) {
        curInfo.add("Applied redstone: " + TextFormatting.WHITE + redstoneLevel);
    }

    @Override
    public void onNeighborBlockUpdate() {
        redstoneLevel = 0;
        for (EnumFacing side : EnumFacing.VALUES) {
            if (dir == side || isInline() && side != dir.getOpposite())
                redstoneLevel = Math.max(redstoneLevel, PneumaticCraftUtils.getRedstoneLevel(pressureTube.world(), pressureTube.pos().offset(side), side));
        }
    }

    public int getReceivingRedstoneLevel() {
        return redstoneLevel;
    }

    public float getThreshold() {
        return getThreshold(redstoneLevel);
    }

    @Override
    protected EnumGuiId getGuiId() {
        return EnumGuiId.PRESSURE_MODULE;
    }

    @Override
    public void update() {
        if (upgraded && !advancedConfig && higherBound != lowerBound) {
            higherBound = lowerBound;
            if (!getTube().world().isRemote) sendDescriptionPacket();
        }
    }
}
