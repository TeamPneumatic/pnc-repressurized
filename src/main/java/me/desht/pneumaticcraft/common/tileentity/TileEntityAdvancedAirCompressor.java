package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.common.core.ModTileEntityTypes;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.inventory.ContainerAdvancedAirCompressor;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.Direction;

import javax.annotation.Nullable;

public class TileEntityAdvancedAirCompressor extends TileEntityAirCompressor implements IHeatExchanger {

    @GuiSynced
    private final IHeatExchangerLogic heatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().getHeatExchangerLogic();

    public TileEntityAdvancedAirCompressor() {
        super(ModTileEntityTypes.ADVANCED_AIR_COMPRESSOR, PneumaticValues.DANGER_PRESSURE_TIER_TWO, PneumaticValues.MAX_PRESSURE_TIER_TWO, PneumaticValues.VOLUME_ADVANCED_AIR_COMPRESSOR);
        heatExchanger.setThermalCapacity(100);
    }

    @Override
    public IHeatExchangerLogic getHeatExchangerLogic(Direction side) {
        return heatExchanger;
    }

    @Override
    protected void onFuelBurn(int burnedFuel) {
        heatExchanger.addHeat(burnedFuel / 2D);
    }

    @Override
    public int getBaseProduction() {
        return PneumaticValues.PRODUCTION_ADVANCED_COMPRESSOR;
    }

    @Override
    public int getEfficiency() {
        return HeatUtil.getEfficiency(heatExchanger.getTemperatureAsInt());
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerAdvancedAirCompressor(i, playerInventory, getPos());
    }

}
