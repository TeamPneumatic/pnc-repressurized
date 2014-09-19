package pneumaticCraft.common.block.tubes;

import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import pneumaticCraft.api.tileentity.IAirHandler;
import pneumaticCraft.api.tileentity.IPneumaticMachine;
import pneumaticCraft.client.model.BaseModel;
import pneumaticCraft.client.model.IBaseModel;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.Names;

public class ModuleRegulatorTube extends TubeModuleRedstoneReceiving implements IInfluenceDispersing{
    private boolean renderItem;

    private IBaseModel model;

    @Override
    public void renderDynamic(double x, double y, double z, float partialTicks, int renderPass, boolean itemRender){
        renderItem = itemRender;
        super.renderDynamic(x, y, z, partialTicks, renderPass, itemRender);
    }

    @Override
    public String getType(){
        return Names.MODULE_REGULATOR;
    }

    @Override
    public IBaseModel getModel(){
        if(model == null) {
            model = new BaseModel("regulatorTubeModule.obj"){
                @Override
                public void renderStatic(float size, TileEntity te){
                    GL11.glPushMatrix();
                    GL11.glRotated(90, 0, -1, 0);
                    GL11.glTranslated(10 / 16D, 24 / 16D, 0);
                    if(renderItem) {
                        GL11.glTranslated(1 / 16D, -1 / 16D, 3 / 16D);
                    }
                    float scale = 1 / 16F;
                    GL11.glScalef(scale, scale, scale);
                    GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                    super.renderStatic(size, te);
                    GL11.glPopMatrix();
                }
            };
        }
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
