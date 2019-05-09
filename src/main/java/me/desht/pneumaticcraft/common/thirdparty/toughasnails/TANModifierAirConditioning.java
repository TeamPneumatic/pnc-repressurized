package me.desht.pneumaticcraft.common.thirdparty.toughasnails;

import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import toughasnails.api.temperature.*;

import javax.annotation.Nonnull;
import java.util.*;

public class TANModifierAirConditioning implements ITemperatureModifier {
    private static Map<UUID, Integer> lastDelta = new HashMap<>();

    @Override
    public Temperature applyEnvironmentModifiers(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull Temperature initialTemperature, @Nonnull IModifierMonitor monitor) {
        return initialTemperature;
    }

    @Override
    public Temperature applyPlayerModifiers(@Nonnull EntityPlayer player, @Nonnull Temperature initialTemperature, @Nonnull IModifierMonitor monitor) {
        CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);

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
        int deltaTemp = (TemperatureScale.getScaleMidpoint() - playerTemp);
        if (Math.abs(deltaTemp) < 2)
            deltaTemp = 0;
        else if (Math.abs(deltaTemp) == 2)
            deltaTemp /= 2;

        deltaTemp *= upgrades;
        targetTemp += deltaTemp;
        if (deltaTemp != lastDelta.getOrDefault(player.getUniqueID(), 0)) {
            NetworkHandler.sendTo(new PacketPlayerTemperatureDelta(deltaTemp), (EntityPlayerMP) player);
            lastDelta.put(player.getUniqueID(), deltaTemp);
        }

        int airUsage = (int) (deltaTemp * ConfigHandler.integration.tanAirConAirUsageMultiplier);
        handler.addAir(EntityEquipmentSlot.CHEST, -Math.abs(airUsage));

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
