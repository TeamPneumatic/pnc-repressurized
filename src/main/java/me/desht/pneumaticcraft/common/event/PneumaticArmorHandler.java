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

package me.desht.pneumaticcraft.common.event;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import me.desht.pneumaticcraft.api.data.PneumaticCraftTags;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.core.ModUpgrades;
import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketJetBootsStateSync;
import me.desht.pneumaticcraft.common.network.PacketSendArmorHUDMessage;
import me.desht.pneumaticcraft.common.network.PacketSpawnParticle;
import me.desht.pneumaticcraft.common.particle.AirParticleData;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import me.desht.pneumaticcraft.common.pneumatic_armor.JetBootsStateTracker;
import me.desht.pneumaticcraft.common.pneumatic_armor.JetBootsStateTracker.JetBootsState;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static me.desht.pneumaticcraft.common.item.PneumaticArmorItem.isPneumaticArmorPiece;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

/**
 * Events related to Pneumatic Armor.  Note any player-tick events are handled in CommonHUDHandler#tickArmorPiece()
 */
public class PneumaticArmorHandler {
    private static final Int2IntMap targetingTracker = new Int2IntOpenHashMap();
    private static final Map<UUID, Map<String, Integer>> targetWarnings = new HashMap<>();

    private static final int ARMOR_REPAIR_AMOUNT = 16;  // durability repaired per compressed iron ingot

    @SubscribeEvent
    public void onMobTargetSet(LivingChangeTargetEvent event) {
        // Helmet with entity tracker upgrade warns player if a mob targets them.
        // LivingSetAttackTargetEvent gets continuously fired even if the mob was already targeting the same target,
        // so we need to track locally what is targeting whom, and only warn the player if the mob is newly
        // targeting them - otherwise, massive spam.
        int mobId = event.getEntity().getId();
        if (event.getNewTarget() instanceof ServerPlayer player) {
            if (isPneumaticArmorPiece(player, EquipmentSlot.HEAD)) {
                if (!targetingTracker.containsKey(mobId) || targetingTracker.get(mobId) != event.getNewTarget().getId()) {
                    CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
                    if (handler.upgradeUsable(CommonUpgradeHandlers.entityTrackerHandler, true)) {
                        Map<String, Integer> map = targetWarnings.computeIfAbsent(player.getUUID(), k -> new HashMap<>());
                        map.merge(event.getEntity().getName().getString(), 1, Integer::sum);
                    }
                }
            }
            targetingTracker.put(mobId, event.getNewTarget().getId());
        } else {
            targetingTracker.remove(mobId);
        }
    }

    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
        targetingTracker.remove(event.getEntity().getId());
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (isPneumaticArmorPiece(player, EquipmentSlot.CHEST) && event.getSource().isFire() && !(player.isCreative() || player.isSpectator())) {
                // security upgrade in chestplate protects from fire
                CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
                if (handler.isArmorEnabled() && handler.hasMinPressure(EquipmentSlot.CHEST) && handler.getUpgradeCount(EquipmentSlot.CHEST, ModUpgrades.SECURITY.get()) > 0) {
                    event.setCanceled(true);
                    player.clearFire();
                    if (!player.level.isClientSide) {
                        handler.addAir(EquipmentSlot.CHEST, -PneumaticValues.PNEUMATIC_ARMOR_FIRE_USAGE);
                        for (int i = 0; i < 2; i++) {
                            float sx = player.getRandom().nextFloat() * 1.5F - 0.75F;
                            float sz = player.getRandom().nextFloat() * 1.5F - 0.75F;
                            NetworkHandler.sendToAllTracking(new PacketSpawnParticle(AirParticleData.DENSE, player.getX() + sx, player.getY() + 1, player.getZ() + sz, sx / 4, -0.2, sz / 4), player.level, player.blockPosition());
                        }
                        if ((player.tickCount & 0xf) == 0) {
                            player.level.playSound(null, player.blockPosition(), ModSounds.LEAKING_GAS.get(), SoundSource.PLAYERS, 1f, 0.7f);
                            tryExtinguish(player);
                        }
                    }
                }
            }
        }
    }

    private void tryExtinguish(Player player) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                BlockPos pos = player.blockPosition().offset(i, 0, j);
                BlockState state = player.level.getBlockState(pos);
                if (state.getBlock() == Blocks.FIRE && player.getRandom().nextInt(3) == 0) {
                    player.level.removeBlock(pos, false);
                } else if ((state.getBlock() == Blocks.LAVA) && player.getRandom().nextInt(5) == 0) {
                    int level = state.getValue(LiquidBlock.LEVEL);
                    player.level.setBlockAndUpdate(pos, level == 0 ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState());
                }
            }
        }
    }

    /**
     * Jump boost due to jump upgrades in leggings
     */
    @SubscribeEvent
    public void onPlayerJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack stack = player.getItemBySlot(EquipmentSlot.LEGS);
            if (!(stack.getItem() instanceof PneumaticArmorItem)) {
                return;
            }
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
            if (handler.upgradeUsable(CommonUpgradeHandlers.jetBootsHandler, true)
                    && !handler.getExtensionData(CommonUpgradeHandlers.jetBootsHandler).isSmartHover()) {
                // enabled jet boots = no jumping
                return;
            }
            if (handler.upgradeUsable(CommonUpgradeHandlers.jumpBoostHandler, true)) {
                float power = PneumaticArmorItem.getIntData(stack, PneumaticArmorItem.NBT_JUMP_BOOST, 100, 0, 100) / 100.0f;
                int rangeUpgrades = handler.getUpgradeCount(EquipmentSlot.LEGS, ModUpgrades.JUMPING.get(),
                        player.isShiftKeyDown() ? 1 : PneumaticValues.PNEUMATIC_LEGS_MAX_JUMP);
                float actualBoost = Math.max(1.0f, rangeUpgrades * power);
                Vec3 m = player.getDeltaMovement();
                double scale = player.isSprinting() ? actualBoost : actualBoost * 0.6;
                player.setDeltaMovement(m.x * scale, m.y + actualBoost * 0.15f, m.z * scale);
                int airUsed = (int) Math.ceil(PneumaticValues.PNEUMATIC_ARMOR_JUMP_USAGE * actualBoost * (player.isSprinting() ? 2 : 1));
                handler.addAir(EquipmentSlot.LEGS, -airUsed);
            }
        }
    }

    /**
     * Allow the player to dig at improved speed if flying with builder mode active
     * (need tier 5 upgrade for normal dig speed)
     */
    @SubscribeEvent
    public void breakSpeedCheck(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        int max = PneumaticValues.PNEUMATIC_JET_BOOTS_MAX_UPGRADES;
        if (isPneumaticArmorPiece(player, EquipmentSlot.FEET) && !player.isOnGround()) {
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(event.getEntity());
            JetBootsState jbState = JetBootsStateTracker.getTracker(player).getJetBootsState(player);
            if (jbState.isEnabled() && jbState.isBuilderMode()) {
                int n = (max + 1) - handler.getUpgradeCount(EquipmentSlot.FEET, ModUpgrades.JET_BOOTS.get(), max);
                if (n < 4) {
                    float mult = 5.0f / n;   // default dig speed when not on ground is 1/5 of normal
                    float oldSpeed = event.getOriginalSpeed();
                    float newSpeed = event.getNewSpeed();
                    if (oldSpeed < newSpeed * mult) {
                        event.setNewSpeed(newSpeed * mult);
                    }
                }
            }
        }
    }

    /**
     * Prevent farmland trampling with pneumatic boots
     */
    @SubscribeEvent
    public void onFarmlandTrample(BlockEvent.FarmlandTrampleEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (isPneumaticArmorPiece(player, EquipmentSlot.FEET)) {
                CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
                if (handler.hasMinPressure(EquipmentSlot.FEET) && handler.isArmorReady(EquipmentSlot.FEET)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    /**
     * Client-side: play particles for all (close enough) player entities with enabled jet boots, including the actual player.
     */
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) return;

        if (event.player.level.isClientSide) {
            handleJetbootsPose(event.player);
        } else if (event.player instanceof ServerPlayer
                && event.player.level.getGameTime() % 20 == 0
                && PneumaticArmorItem.isPneumaticArmorPiece(event.player, EquipmentSlot.HEAD)) {
            handleTargetWarnings((ServerPlayer) event.player);
        }
    }

    private void handleJetbootsPose(Player thisPlayer) {
        JetBootsStateTracker tracker = JetBootsStateTracker.getClientTracker();
        int distThresholdSq = ClientUtils.getRenderDistanceThresholdSq();
        for (Player otherPlayer : thisPlayer.level.players()) {
            if (!otherPlayer.isOnGround() && isPneumaticArmorPiece(otherPlayer, EquipmentSlot.FEET)) {
                JetBootsState state = tracker.getJetBootsState(otherPlayer);
                if (state != null && state.isEnabled() && (!otherPlayer.isFallFlying() || state.isActive()) && otherPlayer.distanceToSqr(thisPlayer) < distThresholdSq) {
                    // note: particles now played in MovingSoundJetboots
                    if (otherPlayer.getId() != thisPlayer.getId() && state.shouldRotatePlayer()) {
                        otherPlayer.setPose(Pose.FALL_FLYING);
                    }
                }
            }
        }
    }

    private void handleTargetWarnings(ServerPlayer player) {
        Map<String, Integer> map = targetWarnings.get(player.getUUID());
        if (map != null) {
            map.forEach((name, count) -> {
                MutableComponent msg = xlate("pneumaticcraft.armor.message.targetWarning", name);
                if (count > 1) msg.append(" (x" + count + ")");
                NetworkHandler.sendToPlayer(new PacketSendArmorHUDMessage(msg, 60, 0x70FF4000), player);
            });
            map.clear();
        }
    }

    @SubscribeEvent
    public void onArmorRepair(AnvilUpdateEvent event) {
        if (event.getLeft().getItem() instanceof PneumaticArmorItem
                && new ItemStack(event.getLeft().getItem()).is(PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON))
        {
            ItemStack repairedItem = event.getLeft().copy();
            int damageRepaired = Math.min(repairedItem.getDamageValue(), event.getRight().getCount() * ARMOR_REPAIR_AMOUNT);
            int ingotsToUse = ((damageRepaired - 1)/ ARMOR_REPAIR_AMOUNT) + 1;
            repairedItem.setDamageValue(repairedItem.getDamageValue() - damageRepaired);

            event.setOutput(repairedItem);
            event.setCost(Math.max(1, ingotsToUse / 2));
            event.setMaterialCost(ingotsToUse);
        }
    }

    @SubscribeEvent
    public void onPlayerTrack(PlayerEvent.StartTracking event) {
        // keep other players up to date with the state of each player's jetboots activity
        if (event.getEntity() instanceof ServerPlayer && event.getTarget() instanceof ServerPlayer trackedPlayer) {
            if (trackedPlayer.getItemBySlot(EquipmentSlot.FEET).getItem() == ModItems.PNEUMATIC_BOOTS.get()) {
                CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(trackedPlayer);
                if (handler.getUpgradeCount(EquipmentSlot.FEET, ModUpgrades.JET_BOOTS.get()) > 0) {
                    JetBootsState state = JetBootsStateTracker.getServerTracker().getJetBootsState(trackedPlayer);
                    NetworkHandler.sendToPlayer(new PacketJetBootsStateSync(trackedPlayer, state), (ServerPlayer) event.getEntity());
                }
            }
        }
    }
}
