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

package me.desht.pneumaticcraft.common.pneumatic_armor.handlers;

import me.desht.pneumaticcraft.api.pneumatic_armor.BaseArmorUpgradeHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.BuiltinArmorUpgrades;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorExtensionData;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.common.config.CommonConfig;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.network.PacketSpawnParticle;
import me.desht.pneumaticcraft.common.registry.ModSounds;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Optional;

public class ScubaHandler extends BaseArmorUpgradeHandler<IArmorExtensionData> {

    public static final Vector3f BUBBLE_SPEED = new Vector3f(0.0f, 0.2f, 0.0f);
    public static final Vector3f BUBBLE_AREA = new Vector3f(1.0f, 1.0f, 1.0f);

    @Override
    public ResourceLocation getID() {
        return BuiltinArmorUpgrades.SCUBA;
    }

    @Override
    public PNCUpgrade[] getRequiredUpgrades() {
        return new PNCUpgrade[] { ModUpgrades.SCUBA.get() };
    }

    @Override
    public float getIdleAirUsage(ICommonArmorHandler armorHandler) {
        return 0;
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }

    @Override
    public void tick(ICommonArmorHandler commonArmorHandler, boolean enabled) {
        Player player = commonArmorHandler.getPlayer();
        if (!player.level().isClientSide && enabled
                && commonArmorHandler.hasMinPressure(EquipmentSlot.HEAD)
                && player.getAirSupply() < player.getMaxAirSupply() / 2) {

            CommonConfig.Armor armorConf = ConfigHelper.common().armor;

            int airMult = armorConf.scubaMultiplier.get();
            if (armorConf.scubaAirUsagePerBlockDepth.get() > 0.0) {
                int thresholdDepth = player.level().getSeaLevel() - armorConf.scubaMinAirUsageIncreaseDepth.get();
                if (player.position().y < thresholdDepth) {
                    airMult += (int) Math.round(armorConf.scubaAirUsagePerBlockDepth.get() * (thresholdDepth - player.position().y));
                }
            }

            float airInHelmet = commonArmorHandler.getAir(EquipmentSlot.HEAD);
            int playerAirAdded = (int) Math.min(player.getMaxAirSupply() - player.getAirSupply(), airInHelmet / airMult);
            player.setAirSupply(player.getAirSupply() + playerAirAdded);

            commonArmorHandler.addAir(EquipmentSlot.HEAD, -(playerAirAdded * airMult));

            NetworkHandler.sendToPlayer(new PacketPlaySound(ModSounds.SCUBA.get(), SoundSource.PLAYERS, player.blockPosition(), 1f, 1.0f, false), (ServerPlayer) player);
            Vec3 eyes = player.getEyePosition(1.0f).add(player.getLookAngle().scale(0.5));
            NetworkHandler.sendToAllTracking(new PacketSpawnParticle(ParticleTypes.BUBBLE,
                    eyes.toVector3f().add(-0.5f, 0, -0.5f),
                    BUBBLE_SPEED,
                    10,
                    Optional.of(BUBBLE_AREA)
            ), player.level(), player.blockPosition());
        }
    }
}
