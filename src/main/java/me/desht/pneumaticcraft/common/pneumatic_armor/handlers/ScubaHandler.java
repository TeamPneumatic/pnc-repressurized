package me.desht.pneumaticcraft.common.pneumatic_armor.handlers;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.pneumatic_armor.BaseArmorUpgradeHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorExtensionData;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.api.pressure.PressureHelper;
import me.desht.pneumaticcraft.common.config.PNCConfig;
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
            int playerAir = (int) Math.min(300 - player.getAirSupply(), airInHelmet / PNCConfig.Common.Armor.scubaMultiplier);
            player.setAirSupply(player.getAirSupply() + playerAir);

            int airUsed = playerAir * PNCConfig.Common.Armor.scubaMultiplier;
            commonArmorHandler.addAir(EquipmentSlotType.HEAD, -airUsed);

            NetworkHandler.sendToPlayer(new PacketPlaySound(ModSounds.SCUBA.get(), SoundCategory.PLAYERS, player.blockPosition(), 1f, 1.0f, false), (ServerPlayerEntity) player);
            Vector3d eyes = player.getEyePosition(1.0f).add(player.getLookAngle().scale(0.5));
            NetworkHandler.sendToAllTracking(new PacketSpawnParticle(ParticleTypes.BUBBLE, eyes.x - 0.5, eyes.y, eyes.z -0.5, 0.0, 0.2, 0.0, 10, 1.0, 1.0, 1.0), player.level, player.blockPosition());
        }
    }
}
