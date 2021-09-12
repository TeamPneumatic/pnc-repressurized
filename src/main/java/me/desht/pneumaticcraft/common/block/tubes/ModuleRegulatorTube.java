package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.common.item.ItemTubeModule;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.LazyOptional;

public class ModuleRegulatorTube extends TubeModuleRedstoneReceiving implements IInfluenceDispersing {
    public static boolean hasTicked;
    public static boolean inLine;
    public static boolean inverted;

    private LazyOptional<IAirHandlerMachine> neighbourCap = null;

    public ModuleRegulatorTube(ItemTubeModule itemTubeModule) {
        super(itemTubeModule);
    }

    @Override
    public int getMaxDispersion() {
        return getCachedNeighbourAirHandler().map(h -> {
            int maxDispersion = (int) ((getThreshold() - h.getPressure()) * h.getVolume());
            return Math.max(0, maxDispersion);
        }).orElse(0);
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

    @Override
    public float getThreshold() {
        // non-upgraded regulator has a simple redstone gradient: 4.9 bar (redstone 0) down to 0 bar (redstone 15)
        return upgraded ? super.getThreshold() : (PneumaticValues.DANGER_PRESSURE_TIER_ONE - 0.1f) * (15 - getReceivingRedstoneLevel()) / 15f;
    }

    private LazyOptional<IAirHandlerMachine> getCachedNeighbourAirHandler() {
        if (neighbourCap == null) {
            TileEntity neighborTE = pressureTube.getLevel().getBlockEntity(pressureTube.getBlockPos().relative(dir));
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
