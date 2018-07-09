package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IPneumaticMachine;
import me.desht.pneumaticcraft.client.ClientTickHandler;
import me.desht.pneumaticcraft.client.model.module.ModelModuleBase;
import me.desht.pneumaticcraft.client.model.module.ModelPressureRegulator;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketDescriptionPacketRequest;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticBase;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class ModuleRegulatorTube extends TubeModuleRedstoneReceiving implements IInfluenceDispersing {
    public static boolean hasTicked;
    public static boolean inLine;
    public static boolean inverted;

    @SideOnly(Side.CLIENT)
    private void renderPreview() {
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

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        if (inLine && !inverted) {
            GlStateManager.color(0, 1, 0, 0.3F);
        } else {
            GlStateManager.color(1, 0, 0, 0.3F);
        }
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 1, 0.2 + ClientTickHandler.TICKS % 20 * 0.015);
        GlStateManager.rotate(90, 1, 0, 0);

        RenderUtils.render3DArrow();
        GlStateManager.color(1, 1, 1, 0.5F);  // 0.5 because we're rendering a preview
        GlStateManager.popMatrix();
        GlStateManager.disableBlend();
    }

    @Override
    public String getType() {
        return Names.MODULE_REGULATOR;
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

    @Override
    public Class<? extends ModelModuleBase> getModelClass() {
        return ModelPressureRegulator.class;
    }

}
