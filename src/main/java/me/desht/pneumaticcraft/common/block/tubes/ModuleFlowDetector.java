package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.client.model.module.ModelFlowDetector;
import me.desht.pneumaticcraft.client.model.module.ModelModuleBase;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;

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
    public int getMaxDispersion() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void onAirDispersion(int amount) {
        flow += amount;
    }

    @Override
    public void addInfo(List<ITextComponent> curInfo) {
        super.addInfo(curInfo);
        curInfo.add(PneumaticCraftUtils.xlate("waila.flowModule.level", oldFlow));
    }

    @Override
    public boolean isInline() {
        return true;
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        rotation = tag.getFloat("rotation");
        oldFlow = tag.getInt("flow");//taggin it for waila purposes.
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        tag.putFloat("rotation", rotation);
        tag.putInt("flow", oldFlow);
    }

    @Override
    public boolean canUpgrade() {
        return false;
    }

    @Override
    public Class<? extends ModelModuleBase> getModelClass() {
        return ModelFlowDetector.class;
    }
}
