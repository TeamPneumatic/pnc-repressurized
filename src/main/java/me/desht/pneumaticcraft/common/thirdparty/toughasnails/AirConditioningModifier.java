package me.desht.pneumaticcraft.common.thirdparty.toughasnails;

import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.common.CommonHUDHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import toughasnails.api.temperature.*;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class AirConditioningModifier implements ITemperatureModifier {
    @Override
    public Temperature applyEnvironmentModifiers(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull Temperature initialTemperature, @Nonnull IModifierMonitor monitor) {
        return initialTemperature;
    }

    @Override
    public Temperature applyPlayerModifiers(@Nonnull EntityPlayer player, @Nonnull Temperature initialTemperature, @Nonnull IModifierMonitor monitor) {
        CommonHUDHandler handler = CommonHUDHandler.getHandlerForPlayer(player);

        if (!handler.isAirConEnabled()
                || !handler.isArmorReady(EntityEquipmentSlot.CHEST)
                || handler.getArmorPressure(EntityEquipmentSlot.CHEST) < 0.1) {
            return initialTemperature;
        }
        int upgrades = handler.getUpgradeCount(EntityEquipmentSlot.CHEST, IItemRegistry.EnumUpgrade.AIR_CONDITIONING, 4);
        if (upgrades == 0) {
            return initialTemperature;
        }

        int targetTemp = initialTemperature.getRawValue();
        int playerTemp = TemperatureHelper.getTemperatureData(player).getTemperature().getRawValue();
        int deltaTemp = Math.abs(TemperatureScale.getScaleMidpoint() - playerTemp);

        if (playerTemp > TemperatureScale.getScaleMidpoint() + 1) {
            targetTemp -= deltaTemp * upgrades;
            handler.addAir(player.getItemStackFromSlot(EntityEquipmentSlot.CHEST), EntityEquipmentSlot.CHEST, -deltaTemp * upgrades);
        } else if (playerTemp < TemperatureScale.getScaleMidpoint() - 1) {
            targetTemp += deltaTemp * upgrades;
            handler.addAir(player.getItemStackFromSlot(EntityEquipmentSlot.CHEST), EntityEquipmentSlot.CHEST, -deltaTemp * upgrades);
        }
        Temperature res = new Temperature(targetTemp);
        monitor.addEntry(new IModifierMonitor.Context(this.getId(), "Pneumatic Armor A/C", initialTemperature, res));
        return res;
    }

    @Override
    public boolean isPlayerSpecific() {
        return true;
    }

    @Nonnull
    @Override
    public String getId() {
        return "pneumaticcraft:air_conditioning";
    }
}
