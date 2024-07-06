/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.thirdparty.immersiveengineering;

import blusunrize.immersiveengineering.api.Lib;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSpawnParticle;
import me.desht.pneumaticcraft.common.particle.AirParticleData;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.registry.ModSounds;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ElectricAttackHandler {
    private static final Map<UUID, Long> sounds = new HashMap<>();

    public static void onElectricalAttack(LivingIncomingDamageEvent event) {
        if (!event.getSource().is(Lib.DamageTypes.WIRE_SHOCK)) return;

        if (event.getEntity() instanceof DroneEntity drone) {
            if (drone.getUpgrades(ModUpgrades.SECURITY.get()) > 0) {
                float dmg = event.getAmount();
                drone.getCapability(PNCCapabilities.AIR_HANDLER_ENTITY).addAir((int)(-50 * dmg));
                float dy = Math.min(dmg / 4, 0.5F);
                NetworkHandler.sendToAllTracking(new PacketSpawnParticle(AirParticleData.DENSE,
                        drone.position().toVector3f(),
                        new Vector3f(0, -dy, 0),
                        (int) (dmg),
                        Optional.empty()
                ), drone);
                event.setAmount(0f);
                playLeakSound(drone);
            }
        } else if (event.getEntity() instanceof Player player) {
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
            if (handler.getUpgradeCount(EquipmentSlot.CHEST, ModUpgrades.SECURITY.get()) > 0
                    && handler.getArmorPressure(EquipmentSlot.CHEST) > 0.1
                    && handler.isArmorReady(EquipmentSlot.CHEST)) {
                handler.addAir(EquipmentSlot.CHEST, (int)(-150 * event.getAmount()));
                float sx = player.getRandom().nextFloat() * 1.5F - 0.75F;
                float sz = player.getRandom().nextFloat() * 1.5F - 0.75F;
                float dy = Math.min(event.getAmount() / 4, 0.5F);
                NetworkHandler.sendToAllTracking(PacketSpawnParticle.oneParticle(AirParticleData.DENSE,
                        player.position().toVector3f().add(sx, 1, sz),
                        new Vector3f(sx / 4, -dy, sz / 4)
                ), player.level(), player.blockPosition());
                event.setAmount(0f);
                playLeakSound(player);
            }
        }
    }

    private static void playLeakSound(Entity e) {
        if (e.level().getGameTime() - sounds.getOrDefault(e.getUUID(), 0L) > 16) {
            e.level().playSound(null, e.blockPosition(), ModSounds.LEAKING_GAS.get(), SoundSource.PLAYERS, 0.5f, 0.7f);
            sounds.put(e.getUUID(), e.level().getGameTime());
        }
    }
}
