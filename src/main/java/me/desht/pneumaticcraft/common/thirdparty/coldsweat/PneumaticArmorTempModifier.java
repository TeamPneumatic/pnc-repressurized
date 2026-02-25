package me.desht.pneumaticcraft.common.thirdparty.coldsweat;

import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.api.util.placement.Matcher;
import com.momosoftworks.coldsweat.api.util.placement.Placement;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlayerTemperatureDelta;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class PneumaticArmorTempModifier extends TempModifier {
    public static final ResourceLocation ID = PneumaticRegistry.RL("air_conditioning");

    private static final Map<UUID,Double> lastTemp = new HashMap<>();
    private static final int TICK_INTERVAL = 20;

    static void addModifier(Player player) {
        if (player.tickCount % TICK_INTERVAL == 0) {
            Temperature.addModifier(player,
                    new PneumaticArmorTempModifier().expires(TICK_INTERVAL).tickRate(TICK_INTERVAL),
                    Temperature.Trait.CORE,
                    Placement.LAST.noDuplicates(Matcher.SAME_CLASS)
            );
        }
    }

    @Override
    protected Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait) {
        if (entity instanceof ServerPlayer player) {
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);

            if (handler.upgradeUsable(CommonUpgradeHandlers.airConHandler, true)) {
                var temp = Temperature.get(entity, Temperature.Trait.CORE);
                if (!PneumaticCraftUtils.epsilonEquals(temp, lastTemp.getOrDefault(player.getUUID(), 0.0))) {
                    NetworkHandler.sendToPlayer(new PacketPlayerTemperatureDelta((int) (-temp / 2.5)), player);
                    lastTemp.put(player.getUUID(), temp);
                }

                if (Math.abs(temp) > 0.5) {
                    double coolingFactor;
                    if (Math.abs(temp) > 50) {
                        coolingFactor = 0.004;
                    } else if (Math.abs(temp) > 25) {
                        coolingFactor = 0.003;
                    } else {
                        coolingFactor = 0.002;
                    }
                    int airCostFactor = (int) (coolingFactor * 1000);
                    int upgradeCount = handler.getUpgradeCount(EquipmentSlot.CHEST, ModUpgrades.AIR_CONDITIONING.get());
                    handler.addAir(EquipmentSlot.CHEST, -(upgradeCount * airCostFactor * TICK_INTERVAL));
                    double efficiency = ConfigHelper.common().integration.coldSweatAirConEfficiency.getAsDouble();
                    double mul = 1.0 - coolingFactor * efficiency * upgradeCount;
                    return t -> t * mul;
                }
            }
        }

        return Function.identity();
    }
}
