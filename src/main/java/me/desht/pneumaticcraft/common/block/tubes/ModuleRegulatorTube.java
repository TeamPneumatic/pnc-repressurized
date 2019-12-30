package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.LazyOptional;

public class ModuleRegulatorTube extends TubeModuleRedstoneReceiving implements IInfluenceDispersing {
    public static boolean hasTicked;
    public static boolean inLine;
    public static boolean inverted;

    private LazyOptional<IAirHandlerMachine> neighbourCap = null;

//    @OnlyIn(Dist.CLIENT)
//    private void renderPreview() {
//        if (!hasTicked) {
//            TileEntityPneumaticBase tile = getTube();
//            NetworkHandler.sendToServer(new PacketDescriptionPacketRequest(tile.getPos()));
//            TileEntity neighbor = tile.getWorld().getTileEntity(tile.getPos().offset(dir));
//            inLine = neighbor instanceof IPneumaticMachine;
//            if (inLine) {
//                IAirHandlerMachine neighborHandler = ((IPneumaticMachine) neighbor).getAirHandler(dir);
//                inverted = neighborHandler != null && neighborHandler.getPressure() > tile.getAirHandler(null).getPressure();
//
//
//                NetworkHandler.sendToServer(new PacketDescriptionPacketRequest(neighbor.getPos()));
//            }
//            hasTicked = true;
//        }
//
//        GlStateManager.enableBlend();
//        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//        if (inLine && !inverted) {
//            GlStateManager.color4f(0, 1, 0, 0.3F);
//        } else {
//            GlStateManager.color4f(1, 0, 0, 0.3F);
//        }
//        GlStateManager.pushMatrix();
//        GlStateManager.translated(0, 1, 0.2 + ClientTickHandler.TICKS % 20 * 0.015);
//        GlStateManager.rotated(90, 1, 0, 0);
//
//        RenderUtils.render3DArrow();
//        GlStateManager.color4f(1, 1, 1, 0.5F);  // 0.5 because we're rendering a preview
//        GlStateManager.popMatrix();
//        GlStateManager.disableBlend();
//    }

    @Override
    public ResourceLocation getType() {
        return Names.MODULE_REGULATOR;
    }

    @Override
    public int getMaxDispersion() {
        return getCachedNeighbourAirHandler().map(h -> {
            int maxDispersion = (int) ((getThreshold() - h.getPressure()) * h.getVolume());
            return Math.max(0, maxDispersion);
        }).orElse(0);
//        // TODO should be able to cache the neighbouring air handler
//        TileEntity neighborTE = pressureTube.getWorld().getTileEntity(pressureTube.getPos().offset(dir));
//        if (neighborTE != null) {
//            return neighborTE.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, dir.getOpposite()).map(h -> {
//                int maxDispersion = (int) ((getThreshold() - h.getPressure()) * h.getVolume());
//                return Math.max(0, maxDispersion);
//            }).orElse(0);
//        }
//        return 0;
    }

    @Override
    public void onAirDispersion(int amount) {
    }

    @Override
    public boolean isInline() {
        return true;
    }

    @Override
    public void onNeighborBlockUpdate() {
        super.onNeighborBlockUpdate();
        neighbourCap = null;
    }

    private LazyOptional<IAirHandlerMachine> getCachedNeighbourAirHandler() {
        if (neighbourCap == null) {
            TileEntity neighborTE = pressureTube.getWorld().getTileEntity(pressureTube.getPos().offset(dir));
            if (neighborTE != null) {
                neighbourCap = neighborTE.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, dir.getOpposite());
                if (neighbourCap.isPresent()) neighbourCap.addListener(l -> neighbourCap = null);
            } else {
                neighbourCap = LazyOptional.empty();
            }
        }
        return neighbourCap;
    }
}
