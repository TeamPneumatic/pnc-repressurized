package me.desht.pneumaticcraft.common.thirdparty.mekanism;

import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.block.entity.IHeatExchangingTE;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import mekanism.api.heat.ISidedHeatHandler;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;

public record PNC2MekHeatAdapter(IHeatExchangerLogic heatExchanger, Direction direction) implements ISidedHeatHandler {
    public static ISidedHeatHandler maybe(BlockEntity blockEntity, Direction direction) {
        return blockEntity instanceof IHeatExchangingTE heat ? new PNC2MekHeatAdapter(heat.getHeatExchanger(), direction) : null;
    }

    @Nullable
    @Override
    public Direction getHeatSideFor() {
        return direction;
    }

    @Override
    public int getHeatCapacitorCount(@Nullable Direction direction) {
        return 1;
    }

    @Override
    public double getTemperature(int i, @Nullable Direction direction) {
        return heatExchanger.getTemperature();
    }

    @Override
    public double getInverseConduction(int i, @Nullable Direction direction) {
        return heatExchanger.getThermalResistance() * ConfigHelper.common().integration.mekThermalResistanceFactor.get();
    }

    @Override
    public double getHeatCapacity(int i, @Nullable Direction direction) {
        return heatExchanger.getThermalCapacity();
    }

    @Override
    public void handleHeat(int i, double amount, @Nullable Direction direction) {
        heatExchanger.addHeat(amount * ConfigHelper.common().integration.mekThermalEfficiencyFactor.get());
    }
}
