package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IPneumaticMachine;
import me.desht.pneumaticcraft.client.ClientTickHandler;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketDescriptionPacketRequest;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticBase;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class ModuleRegulatorTube extends TubeModuleRedstoneReceiving implements IInfluenceDispersing {
    private boolean renderItem;
    public static boolean hasTicked;
    public static boolean inLine;
    public static boolean inverted;

    @Override
    public void renderDynamic(double x, double y, double z, float partialTicks, int renderPass, boolean itemRender) {
        renderItem = itemRender;
        super.renderDynamic(x, y, z, partialTicks, renderPass, itemRender);
    }

    @Override
    protected void renderModule() {
        super.renderModule();
        if (isFake()) {
            if (!hasTicked) {
                TileEntityPneumaticBase tile = (TileEntityPneumaticBase) getTube();
                NetworkHandler.sendToServer(new PacketDescriptionPacketRequest(tile.getPos()));
                TileEntity neighbor = tile.getWorld().getTileEntity(tile.getPos().offset(dir));
                inLine = neighbor instanceof IPneumaticMachine;
                if (inLine) {
                    IAirHandler neighborHandler = ((IPneumaticMachine) neighbor).getAirHandler(dir);
                    inverted = neighborHandler != null && neighborHandler.getPressure() > tile.getAirHandler(null).getPressure();
                    NetworkHandler.sendToServer(new PacketDescriptionPacketRequest(neighbor.getPos()));
                }
                hasTicked = true;
            }

            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            if (inLine && !inverted) {
                GL11.glColor4d(0, 1, 0, 0.3);
            } else {
                GL11.glColor4d(1, 0, 0, 0.3);
            }
            GL11.glPushMatrix();
            GL11.glTranslated(0, 1, 0.2 + ClientTickHandler.TICKS % 20 * 0.015);
            GL11.glRotated(90, 1, 0, 0);

            RenderUtils.render3DArrow();
            GL11.glColor4d(1, 1, 1, 1);
            GL11.glPopMatrix();
            GL11.glDisable(GL11.GL_BLEND);
        }
    }

    @Override
    public String getType() {
        return Names.MODULE_REGULATOR;
    }

    @Override
    public String getModelName() {
        return "regulator";
        //if(model == null) {
        /*TODO 1.8 model = new BaseModel("regulatorTubeModule.obj"){
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
         };*/
        //  }
        ////  return model;
    }

    @Override
    public int getMaxDispersion() {
        IAirHandler connectedHandler = null;
        for (Pair<EnumFacing, IAirHandler> entry : pressureTube.getAirHandler(null).getConnectedPneumatics()) {
            if (entry.getKey().equals(dir)) {
                connectedHandler = entry.getValue();
                break;
            }
        }
        if (connectedHandler == null) return 0;
        int maxDispersion = (int) ((getThreshold() - connectedHandler.getPressure()) * connectedHandler.getVolume());
        if (maxDispersion < 0) return 0;
        return maxDispersion;
    }

    @Override
    public void onAirDispersion(int amount) {
    }

    @Override
    public void addInfo(List<String> curInfo) {
        super.addInfo(curInfo);
        curInfo.add("Threshold: " + TextFormatting.WHITE + PneumaticCraftUtils.roundNumberTo(getThreshold(), 1) + " bar");
    }

    @Override
    public boolean isInline() {
        return true;
    }

    @Override
    public void addItemDescription(List<String> curInfo) {
        curInfo.add(TextFormatting.BLUE + "Formula: Threshold(bar) = 7.5 - Redstone x 0.5");
        curInfo.add("This module will stop pressurized air from");
        curInfo.add("travelling through this tube when a certain");
        curInfo.add("pressure threshold's reached.");
    }

}
