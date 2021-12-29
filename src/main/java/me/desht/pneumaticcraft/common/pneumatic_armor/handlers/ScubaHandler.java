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

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.pneumatic_armor.BaseArmorUpgradeHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorExtensionData;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.api.pressure.PressureHelper;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.network.PacketSpawnParticle;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.vector.Vector3d;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class ScubaHandler extends BaseArmorUpgradeHandler<IArmorExtensionData> {
    @Override
    public ResourceLocation getID() {
        return RL("scuba");
    }

    @Override
    public EnumUpgrade[] getRequiredUpgrades() {
        return new EnumUpgrade[] { EnumUpgrade.SCUBA };
    }

    @Override
    public float getIdleAirUsage(ICommonArmorHandler armorHandler) {
        return 0;
    }

    @Override
    public EquipmentSlotType getEquipmentSlot() {
        return EquipmentSlotType.HEAD;
    }

    @Override
    public void tick(ICommonArmorHandler commonArmorHandler, boolean enabled) {
        PlayerEntity player = commonArmorHandler.getPlayer();
        if (!player.level.isClientSide && enabled
                && commonArmorHandler.hasMinPressure(EquipmentSlotType.HEAD)
                && player.getAirSupply() < 150) {

            ItemStack helmetStack = player.getItemBySlot(EquipmentSlotType.HEAD);

            int baseVol = ((ItemPneumaticArmor) helmetStack.getItem()).getBaseVolume();
            int vol = PressureHelper.getUpgradedVolume(baseVol, commonArmorHandler.getUpgradeCount(EquipmentSlotType.HEAD, EnumUpgrade.VOLUME));
            float airInHelmet = commonArmorHandler.getArmorPressure(EquipmentSlotType.HEAD) * vol;
            int playerAir = (int) Math.min(300 - player.getAirSupply(), airInHelmet / ConfigHelper.common().armor.scubaMultiplier.get());
            player.setAirSupply(player.getAirSupply() + playerAir);

            int airUsed = playerAir * ConfigHelper.common().armor.scubaMultiplier.get();
            commonArmorHandler.addAir(EquipmentSlotType.HEAD, -airUsed);

            NetworkHandler.sendToPlayer(new PacketPlaySound(ModSounds.SCUBA.get(), SoundCategory.PLAYERS, player.blockPosition(), 1f, 1.0f, false), (ServerPlayerEntity) player);
            Vector3d eyes = player.getEyePosition(1.0f).add(player.getLookAngle().scale(0.5));
            NetworkHandler.sendToAllTracking(new PacketSpawnParticle(ParticleTypes.BUBBLE, eyes.x - 0.5, eyes.y, eyes.z -0.5, 0.0, 0.2, 0.0, 10, 1.0, 1.0, 1.0), player.level, player.blockPosition());
        }
    }
}
