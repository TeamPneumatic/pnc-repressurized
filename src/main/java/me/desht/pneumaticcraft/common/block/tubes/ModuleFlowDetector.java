package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.lib.Names;
import me.desht.pneumaticcraft.proxy.CommonProxy.EnumGuiId;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class ModuleFlowDetector extends TubeModuleRedstoneEmitting implements IInfluenceDispersing {
    public float rotation, oldRotation;
    private int flow;
    private int oldFlow;

    @Override
    public void update() {
        super.update();
        oldRotation = rotation;
        rotation += getRedstoneLevel() / 100F;

        if (!pressureTube.world().isRemote) {
            if (setRedstone(flow / 5)) {
                sendDescriptionPacket();
            }
            oldFlow = flow;
            flow = 0;
        }
    }

    @Override
    public String getType() {
        return Names.MODULE_FLOW_DETECTOR;
    }

    @Override
    public String getModelName() {
        return "flowDetector";
    }

    @Override
    public int getMaxDispersion() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void onAirDispersion(int amount) {
        flow += amount;
    }

    @Override
    public void addInfo(List<String> curInfo) {
        curInfo.add("Flow: " + TextFormatting.WHITE + oldFlow + " mL/tick");
        super.addInfo(curInfo);
    }

    @Override
    public boolean isInline() {
        return true;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        rotation = tag.getFloat("rotation");
        oldFlow = tag.getInteger("flow");//taggin it for waila purposes.
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setFloat("rotation", rotation);
        tag.setInteger("flow", oldFlow);
    }

    @Override
    public void addItemDescription(List<String> curInfo) {
        curInfo.add(TextFormatting.BLUE + "Formula: Redstone = 0.2 x flow(mL/tick)");
        curInfo.add("This module emits a redstone signal of which");
        curInfo.add("the strength is dependent on how much air");
        curInfo.add("is travelling through the tube.");
    }

    @Override
    public boolean canUpgrade() {
        return false;
    }

    @Override
    protected EnumGuiId getGuiId() {
        return null;
    }
}
