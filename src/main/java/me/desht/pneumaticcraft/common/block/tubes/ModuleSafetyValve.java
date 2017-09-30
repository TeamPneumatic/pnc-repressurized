package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.client.model.module.ModelSafetyValve;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ModuleSafetyValve extends TubeModuleRedstoneReceiving {
    @SideOnly(Side.CLIENT)
    private final ModelSafetyValve model = new ModelSafetyValve();

    @Override
    public void update() {
        super.update();
        if (!pressureTube.world().isRemote) {
            if (pressureTube.getAirHandler(null).getPressure() > getThreshold()) {
                pressureTube.getAirHandler(null).airLeak(dir);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render(float partialTicks) {
        model.renderModel(0.0625f, dir, partialTicks);
    }

    @Override
    public void addInfo(List<String> curInfo) {
        super.addInfo(curInfo);
        curInfo.add("Threshold: " + TextFormatting.WHITE + PneumaticCraftUtils.roundNumberTo(getThreshold(), 1) + " bar");
    }

    @Override
    public String getType() {
        return Names.MODULE_SAFETY_VALVE;
    }

    @Override
    public String getModelName() {
        return "safety_valve";
    }

    @Override
    public void addItemDescription(List<String> curInfo) {
        curInfo.add(TextFormatting.BLUE + "Formula: Threshold(bar) = 7.5 - Redstone x 0.5");
        curInfo.add("This module will release high pressure gases");
        curInfo.add("when a certain threshold's reached. Though");
        curInfo.add("it prevents overpressure it can be counted");
        curInfo.add("as energy loss.");
    }
}
