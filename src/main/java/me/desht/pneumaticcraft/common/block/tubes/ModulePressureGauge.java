package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdatePressureBlock;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticBase;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.util.ResourceLocation;

public class ModulePressureGauge extends TubeModuleRedstoneEmitting {
    public ModulePressureGauge() {
        lowerBound = 0;
        higherBound = 7.5F;
    }

    @Override
    public void update() {
        super.update();

        if (!pressureTube.world().isRemote) {
            if (pressureTube.world().getGameTime() % 20 == 0)
                NetworkHandler.sendToAllAround(new PacketUpdatePressureBlock((TileEntityPneumaticBase) getTube()), getTube().world());
            setRedstone(getRedstone(pressureTube.getAirHandler(null).getPressure()));
        }
    }

    private int getRedstone(float pressure) {
        return (int) ((pressure - lowerBound) / (higherBound - lowerBound) * 15);
    }

    @Override
    public ResourceLocation getType() {
        return Names.MODULE_GAUGE;
    }

    @Override
    public double getWidth() {
        return 8D;
    }

    @Override
    protected double getHeight() {
        return 4D;
    }

    @Override
    public boolean hasGui() {
        return true;
    }
}
