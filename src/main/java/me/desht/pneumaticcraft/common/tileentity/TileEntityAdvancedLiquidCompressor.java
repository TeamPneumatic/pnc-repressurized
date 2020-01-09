package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.inventory.ContainerAdvancedLiquidCompressor;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.Direction;

import javax.annotation.Nullable;

public class TileEntityAdvancedLiquidCompressor extends TileEntityLiquidCompressor implements IHeatExchanger {

    @GuiSynced
    private final IHeatExchangerLogic heatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().getHeatExchangerLogic();

    public TileEntityAdvancedLiquidCompressor() {
        super(ModTileEntities.ADVANCED_LIQUID_COMPRESSOR, 20, 25, 10000);
        heatExchanger.setThermalCapacity(100);
    }

    @Override
    public IHeatExchangerLogic getHeatExchangerLogic(Direction side) {
        return heatExchanger;
    }

    @Override
    protected void onFuelBurn(int burnedFuel) {
        heatExchanger.addHeat(burnedFuel / 20D);
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
        return new ContainerAdvancedLiquidCompressor(i, playerInventory, getPos());
    }
}
