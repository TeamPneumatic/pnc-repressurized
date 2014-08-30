package pneumaticCraft.common.block.tubes;

import java.util.List;

import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.tuple.Pair;

import pneumaticCraft.api.tileentity.IAirHandler;
import pneumaticCraft.api.tileentity.IPneumaticMachine;
import pneumaticCraft.client.model.IBaseModel;
import pneumaticCraft.client.model.ModelSafetyValve;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.Names;

public class ModuleRegulatorTube extends TubeModuleRedstoneReceiving implements IInfluenceDispersing{
    private final IBaseModel model = new ModelSafetyValve();

    @Override
    public String getType(){
        return Names.MODULE_REGULATOR;
    }

    @Override
    public IBaseModel getModel(){
        return model;
    }

    @Override
    public int canDisperse(int amount){
        IAirHandler connectedHandler = null;
        for(Pair<ForgeDirection, IPneumaticMachine> entry : pressureTube.getAirHandler().getConnectedPneumatics()) {
            if(entry.getKey().equals(dir)) {
                connectedHandler = entry.getValue().getAirHandler();
                break;
            }
        }
        if(connectedHandler == null) return 0;
        int maxDispersion = (int)((getThreshold() - connectedHandler.getPressure(ForgeDirection.UNKNOWN)) * pressureTube.getAirHandler().getVolume());
        if(maxDispersion < 0) return 0;
        if(Math.abs(maxDispersion) < Math.abs(amount)) {
            return maxDispersion;
        } else {
            return amount;
        }
    }

    @Override
    public void addInfo(List<String> curInfo){
        super.addInfo(curInfo);
        curInfo.add("Threshold: " + EnumChatFormatting.WHITE + PneumaticCraftUtils.roundNumberTo(getThreshold(), 1) + " bar");
    }

    @Override
    public boolean isInline(){
        return true;
    }

    @Override
    public void addItemDescription(List<String> curInfo){
        curInfo.add(EnumChatFormatting.BLUE + "Formula: Threshold(bar) = 7.5 - Redstone x 0.5");
        curInfo.add("This module will stop pressurized air from");
        curInfo.add("travelling through this tube when a certain");
        curInfo.add("pressure threshold's reached.");
    }

}
