package me.desht.pneumaticcraft.common.block.tubes;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IPneumaticMachine;
import me.desht.pneumaticcraft.client.ClientTickHandler;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketDescriptionPacketRequest;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticBase;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

public class ModuleRegulatorTube extends TubeModuleRedstoneReceiving implements IInfluenceDispersing {
    public static boolean hasTicked;
    public static boolean inLine;
    public static boolean inverted;

    @OnlyIn(Dist.CLIENT)
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
            GlStateManager.color4f(0, 1, 0, 0.3F);
        } else {
            GlStateManager.color4f(1, 0, 0, 0.3F);
        }
        GlStateManager.pushMatrix();
        GlStateManager.translated(0, 1, 0.2 + ClientTickHandler.TICKS % 20 * 0.015);
        GlStateManager.rotated(90, 1, 0, 0);

        RenderUtils.render3DArrow();
        GlStateManager.color4f(1, 1, 1, 0.5F);  // 0.5 because we're rendering a preview
        GlStateManager.popMatrix();
        GlStateManager.disableBlend();
    }

    @Override
    public ResourceLocation getType() {
        return Names.MODULE_REGULATOR;
    }

    @Override
    public int getMaxDispersion() {
        IAirHandler connectedHandler = null;
        for (Pair<Direction, IAirHandler> entry : pressureTube.getAirHandler(null).getConnectedPneumatics()) {
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
    public boolean isInline() {
        return true;
    }
}
