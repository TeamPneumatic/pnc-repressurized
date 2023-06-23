package me.desht.pneumaticcraft.common.pneumatic_armor.handlers;

import me.desht.pneumaticcraft.api.item.PNCUpgrade;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.pneumatic_armor.BaseArmorUpgradeHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorExtensionData;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.core.ModUpgrades;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSpawnParticle;
import me.desht.pneumaticcraft.common.particle.AirParticleData;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import me.desht.pneumaticcraft.common.pneumatic_armor.JetBootsStateTracker;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class FallProtectionHandler extends BaseArmorUpgradeHandler<IArmorExtensionData> {
    private static final ResourceLocation ID = RL("fall_protection");

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public PNCUpgrade[] getRequiredUpgrades() {
        return new PNCUpgrade[0]; // no upgrades needed, it's a built-in
    }

    @Override
    public float getIdleAirUsage(ICommonArmorHandler armorHandler) {
        return 0;
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.FEET;
    }

    @Mod.EventBusSubscriber(modid = Names.MOD_ID)
    public static class Listener {
        @SubscribeEvent
        public static void onPlayerFall(LivingFallEvent event) {
            if (event.getDistance() > 3.0F && event.getEntity() instanceof Player player) {
                float origDistance = event.getDistance();

                CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);

                JetBootsStateTracker.JetBootsState jbState = JetBootsStateTracker.getTracker(player).getJetBootsState(player);
                if (event.getEntity().level.getDifficulty() == Difficulty.HARD && jbState.isActive()) {
                    // thrusting into the ground hurts at hard difficulty!
                    event.setDamageMultiplier(0.2F);
                    return;
                }
                if (handler.upgradeUsable(CommonUpgradeHandlers.jumpBoostHandler, true)) {
                    // straight fall distance reduction if jump upgrade operational in legs
                    event.setDistance(Math.max(0, event.getDistance() - 1.5f * handler.getUpgradeCount(EquipmentSlot.LEGS, ModUpgrades.JUMPING.get())));
                    if (event.getDistance() < 2) {
                        event.setCanceled(true);
                        return;
                    }
                }
                if (!handler.upgradeUsable(CommonUpgradeHandlers.fallProtectionHandler, true)) {
                    return;
                }

                if (!player.level.isClientSide) {
                    float airNeeded = event.getDistance() * PneumaticValues.PNEUMATIC_ARMOR_FALL_USAGE;
                    float extraAirNeeded = 0f;

                    int airAvailable = handler.getAir(EquipmentSlot.FEET);
                    List<Entity> stomped = new ArrayList<>();
                    if (handler.upgradeUsable(CommonUpgradeHandlers.stompHandler, true)) {
                        for (Entity e: player.level.getEntities(player, new AABB(player.blockPosition()).inflate(7.0), e -> e instanceof Mob && e.isAlive())) {
                            if (airAvailable > airNeeded + extraAirNeeded) {
                                stomped.add(e);
                                extraAirNeeded += airNeeded;
                            } else {
                                break;
                            }
                        }
                    }
                    if (airAvailable < 1) {
                        return;
                    } else if (airAvailable >= airNeeded + extraAirNeeded) {
                        event.setCanceled(true);
                    } else {
                        event.setDamageMultiplier(1.0F - (airAvailable / airNeeded));
                    }
                    for (int i = 0; i < event.getDistance() / 2; i++) {
                        float sx = player.getRandom().nextFloat() * 0.6F - 0.3F;
                        float sz = player.getRandom().nextFloat() * 0.6F - 0.3F;
                        NetworkHandler.sendToAllTracking(new PacketSpawnParticle(AirParticleData.DENSE, player.getX(), player.getY(), player.getZ(), sx, 0.1, sz), player.level, player.blockPosition());
                    }
                    for (Entity e : stomped) {
                        NetworkHandler.sendToAllTracking(new PacketSpawnParticle(ParticleTypes.EXPLOSION, e.getX(), e.getY(), e.getZ(), 0, 0, 0), player.level, player.blockPosition());
                        e.hurt(player.damageSources().explosion(player, null), Mth.clamp(origDistance / 3f, 1f, 20f));
                    }
                    if (!stomped.isEmpty()) {
                        player.level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1f, 0.5f);
                    }
                    player.level.playSound(null, player.blockPosition(), ModSounds.SHORT_HISS.get(), SoundSource.PLAYERS, 0.3f, 0.8f);
                    handler.addAir(EquipmentSlot.FEET, (int) -(airNeeded + extraAirNeeded));
                }
            }
        }
    }
}
