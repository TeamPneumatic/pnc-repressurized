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

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.pneumatic_armor.BaseArmorUpgradeHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorExtensionData;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.item.ItemRegistry;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class MagnetHandler extends BaseArmorUpgradeHandler<IArmorExtensionData> {
    private static final ResourceLocation ID = RL("magnet");

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public PNCUpgrade[] getRequiredUpgrades() {
        return new PNCUpgrade[] { ModUpgrades.MAGNET.get() };
    }

    @Override
    public int getMaxInstallableUpgrades(PNCUpgrade upgrade) {
        return 6;
    }

    @Override
    public float getIdleAirUsage(ICommonArmorHandler armorHandler) {
        return 0;
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.CHEST;
    }

    @Override
    public void tick(ICommonArmorHandler commonArmorHandler, boolean enabled) {
        Player player = commonArmorHandler.getPlayer();

        if (player.level().isClientSide || !enabled
                || (player.level().getGameTime() & 0x3) != 0
                || !commonArmorHandler.hasMinPressure(EquipmentSlot.CHEST))
            return;

        int magnetRadius = PneumaticValues.MAGNET_BASE_RANGE
                + Math.min(commonArmorHandler.getUpgradeCount(EquipmentSlot.CHEST, ModUpgrades.MAGNET.get()), PneumaticValues.MAGNET_MAX_UPGRADES);
        int magnetRadiusSq = magnetRadius * magnetRadius;

        AABB box = new AABB(player.blockPosition()).inflate(magnetRadius);
        List<Entity> itemList = player.getCommandSenderWorld().getEntitiesOfClass(Entity.class, box,
                e -> (e instanceof ExperienceOrb || e instanceof ItemEntity) && e.isAlive());

        Vec3 playerVec = player.position();
        for (Entity item : itemList) {
            if (item instanceof ItemEntity && ((ItemEntity) item).hasPickUpDelay()) continue;

            if (item.position().distanceToSqr(playerVec) <= magnetRadiusSq
                    && !ItemRegistry.getInstance().shouldSuppressMagnet(item)
                    && !item.getPersistentData().getBoolean(Names.PREVENT_REMOTE_MOVEMENT)) {
                if (!commonArmorHandler.hasMinPressure(EquipmentSlot.CHEST)) break;
                item.setPos(player.getX(), player.getY(), player.getZ());
                if (item instanceof ItemEntity) ((ItemEntity) item).setPickUpDelay(0);
                commonArmorHandler.addAir(EquipmentSlot.CHEST, -ConfigHelper.common().armor.magnetAirUsage.get());
            }
        }
    }
}
