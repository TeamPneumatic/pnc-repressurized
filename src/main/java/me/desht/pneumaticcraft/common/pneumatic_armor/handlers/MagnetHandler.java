package me.desht.pneumaticcraft.common.pneumatic_armor.handlers;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.pneumatic_armor.BaseArmorUpgradeHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorExtensionData;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.item.ItemRegistry;
import me.desht.pneumaticcraft.lib.Names;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class MagnetHandler extends BaseArmorUpgradeHandler<IArmorExtensionData> {
    @Override
    public ResourceLocation getID() {
        return RL("magnet");
    }

    @Override
    public EnumUpgrade[] getRequiredUpgrades() {
        return new EnumUpgrade[] { EnumUpgrade.MAGNET };
    }

    @Override
    public int getMaxInstallableUpgrades(EnumUpgrade upgrade) {
        return 6;
    }

    @Override
    public float getIdleAirUsage(ICommonArmorHandler armorHandler) {
        return 0;
    }

    @Override
    public EquipmentSlotType getEquipmentSlot() {
        return EquipmentSlotType.CHEST;
    }

    @Override
    public void tick(ICommonArmorHandler commonArmorHandler, boolean enabled) {
        PlayerEntity player = commonArmorHandler.getPlayer();

        if (player.world.isRemote || !enabled
                || (player.world.getGameTime() & 0x3) != 0
                || !commonArmorHandler.hasMinPressure(EquipmentSlotType.CHEST))
            return;

        int magnetRadius = PneumaticValues.MAGNET_BASE_RANGE
                + Math.min(commonArmorHandler.getUpgradeCount(EquipmentSlotType.CHEST, EnumUpgrade.MAGNET), PneumaticValues.MAGNET_MAX_UPGRADES);
        int magnetRadiusSq = magnetRadius * magnetRadius;

        AxisAlignedBB box = new AxisAlignedBB(player.getPosition()).grow(magnetRadius);
        List<Entity> itemList = player.getEntityWorld().getEntitiesWithinAABB(Entity.class, box,
                e -> (e instanceof ExperienceOrbEntity || e instanceof ItemEntity) && e.isAlive());

        Vector3d playerVec = player.getPositionVec();
        for (Entity item : itemList) {
            if (item instanceof ItemEntity && ((ItemEntity) item).cannotPickup()) continue;

            if (item.getPositionVec().squareDistanceTo(playerVec) <= magnetRadiusSq
                    && !ItemRegistry.getInstance().shouldSuppressMagnet(item)
                    && !item.getPersistentData().getBoolean(Names.PREVENT_REMOTE_MOVEMENT)) {
                if (!commonArmorHandler.hasMinPressure(EquipmentSlotType.CHEST)) break;
                item.setPosition(player.getPosX(), player.getPosY(), player.getPosZ());
                if (item instanceof ItemEntity) ((ItemEntity) item).setPickupDelay(0);
                commonArmorHandler.addAir(EquipmentSlotType.CHEST, -PNCConfig.Common.Armor.magnetAirUsage);
            }
        }
    }
}
