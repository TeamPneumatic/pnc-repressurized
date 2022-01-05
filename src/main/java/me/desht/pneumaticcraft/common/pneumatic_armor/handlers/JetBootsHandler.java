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
import me.desht.pneumaticcraft.client.sound.MovingSounds;
import me.desht.pneumaticcraft.common.advancements.AdvancementTriggers;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlayMovingSound;
import me.desht.pneumaticcraft.common.network.PacketPlayMovingSound.SoundSource;
import me.desht.pneumaticcraft.common.network.PacketSpawnParticle;
import me.desht.pneumaticcraft.common.particle.AirParticleData;
import me.desht.pneumaticcraft.common.pneumatic_armor.JetBootsStateTracker;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Difficulty;

import java.util.function.Supplier;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class JetBootsHandler extends BaseArmorUpgradeHandler<JetBootsHandler.JetBootsLocalState> {
    public static final int BUILDER_MODE_LEVEL = 3;  // tier needed for builder mode
    public static final int STABLIZERS_LEVEL = 4;  // tier needed for flight stabilizers

    @Override
    public ResourceLocation getID() {
        return RL("jet_boots");
    }

    @Override
    public EnumUpgrade[] getRequiredUpgrades() {
        return new EnumUpgrade[] { EnumUpgrade.JET_BOOTS };
    }

    @Override
    public float getIdleAirUsage(ICommonArmorHandler armorHandler) {
        return 0;
    }

    @Override
    public EquipmentSlotType getEquipmentSlot() {
        return EquipmentSlotType.FEET;
    }

    @Override
    public Supplier<JetBootsLocalState> extensionData() {
        return JetBootsLocalState::new;
    }

    @Override
    public void tick(ICommonArmorHandler commonArmorHandler, boolean enabled) {
        int jetbootsCount = commonArmorHandler.getUpgradeCount(EquipmentSlotType.FEET, EnumUpgrade.JET_BOOTS);
        if (jetbootsCount == 0) return;

        int jetbootsAirUsage = 0;

        PlayerEntity player = commonArmorHandler.getPlayer();
        JetBootsStateTracker.JetBootsState jbState = JetBootsStateTracker.getTracker(player).getJetBootsState(player);
        JetBootsLocalState jbLocal = commonArmorHandler.getExtensionData(this);

        if (commonArmorHandler.hasMinPressure(EquipmentSlotType.FEET)) {
            if (jbState.isActive()) {
                if (jbState.isBuilderMode() && jetbootsCount >= BUILDER_MODE_LEVEL) {
                    // builder mode - rise vertically (or hover if sneaking and firing)
                    setYMotion(player, player.isShiftKeyDown() ? 0 : 0.15 + 0.15 * (jetbootsCount - 3));
                    jetbootsAirUsage = (int) (PNCConfig.Common.Armor.jetBootsAirUsage * jetbootsCount / 2.5F);
                } else {
                    // jetboots firing - move in direction of looking
                    Vector3d lookVec = player.getLookAngle().scale(0.3 * jetbootsCount);
                    jbLocal.updateAccel(lookVec);
                    lookVec = jbLocal.getEffectiveMotion(lookVec);
                    player.setDeltaMovement(lookVec.x, player.isOnGround() ? 0 : lookVec.y, lookVec.z);
                    jetbootsAirUsage = jbLocal.calcAirUsage(jetbootsCount);
                }
                if (player.isInWater()) jetbootsAirUsage *= 4;
                jbLocal.tickActive();
            } else if (jbState.isEnabled() && !player.isOnGround() && !player.isFallFlying()) {
                // jetboots not firing, but enabled - slowly descend (or hover if enough upgrades)
                // and bring player to complete halt if flight stabilizers and not actively moving forward/sideways
                boolean reallyHovering = !jbLocal.isSmartHover() || jbLocal.isHovering();
                boolean stopped = jbLocal.isFlightStabilizers()
                        && jetbootsCount >= STABLIZERS_LEVEL
                        && PneumaticCraftUtils.epsilonEquals(player.zza, 0f)
                        && PneumaticCraftUtils.epsilonEquals(player.xxa, 0f);
                double xMotion = stopped ? 0 : player.getDeltaMovement().x;
                double yMotion = reallyHovering ? (player.isShiftKeyDown() ? -0.45 : -0.1 + 0.02 * jetbootsCount) : player.getDeltaMovement().y;
                double zMotion = stopped ? 0 : player.getDeltaMovement().z;
                player.setDeltaMovement(new Vector3d(xMotion, yMotion, zMotion));
                if (reallyHovering) player.fallDistance = 0;
                jetbootsAirUsage = reallyHovering ? (int) (PNCConfig.Common.Armor.jetBootsAirUsage * (player.isShiftKeyDown() ? 0.25F : 0.5F)) : 0;
                jbLocal.resetAccel();
            } else if (player.isOnGround()) {
                jbLocal.setHovering(false);
            } else {
                jbLocal.resetAccel();
            }
        } else {
            // insufficient pressure!
            if (jbState.isEnabled() && !player.isOnGround() && !player.isFallFlying() && jbLocal.isHovering()) {
                // still active and in the air: using minimal air here keeps the boots running
                // and thus avoids triggering multiple looping sounds (see "jet boots starting up" code below)
                jetbootsAirUsage = 1;
            }
            setJetBootsActive(commonArmorHandler,false);
        }

        if (jetbootsAirUsage != 0 && !player.level.isClientSide) {
            if (jbLocal.getPrevJetBootsAirUsage() == 0) {
                // jet boots starting up
                NetworkHandler.sendToAllTracking(new PacketPlayMovingSound(MovingSounds.Sound.JET_BOOTS, SoundSource.of(player)), player.level, player.blockPosition());
                AdvancementTriggers.FLIGHT.trigger((ServerPlayerEntity) player);
            }
            if (player.horizontalCollision) {
                double vel = player.getDeltaMovement().length();
                if (player.level.getDifficulty() == Difficulty.HARD) {
                    vel *= 2;
                } else if (player.level.getDifficulty() == Difficulty.NORMAL) {
                    vel *= 1.5;
                }
                if (vel > 2) {
                    player.playSound(vel > 2.5 ? SoundEvents.GENERIC_BIG_FALL : SoundEvents.GENERIC_SMALL_FALL, 1.0F, 1.0F);
                    player.hurt(DamageSource.FLY_INTO_WALL, (float) vel);
                    AdvancementTriggers.FLY_INTO_WALL.trigger((ServerPlayerEntity) player);
                }
            }
            commonArmorHandler.addAir(EquipmentSlotType.FEET, -jetbootsAirUsage);
        }
        jbLocal.setPrevJetBootsAirUsage(jetbootsAirUsage);
    }

    @Override
    public void onInit(ICommonArmorHandler commonArmorHandler) {
        PlayerEntity player = commonArmorHandler.getPlayer();
        ItemStack armorStack = player.getItemBySlot(EquipmentSlotType.FEET);
        JetBootsHandler.JetBootsLocalState jbLocal = commonArmorHandler.getExtensionData(this);
        jbLocal.flightStabilizers = ItemPneumaticArmor.getBooleanData(armorStack, ItemPneumaticArmor.NBT_FLIGHT_STABILIZERS, false);
        jbLocal.jetBootsPower = ItemPneumaticArmor.getIntData(armorStack, ItemPneumaticArmor.NBT_JET_BOOTS_POWER, 100, 0, 100) / 100f;
        jbLocal.smartHover = ItemPneumaticArmor.getBooleanData(armorStack, ItemPneumaticArmor.NBT_SMART_HOVER, false);
        boolean jetBootsBuilderMode = ItemPneumaticArmor.getBooleanData(armorStack, ItemPneumaticArmor.NBT_BUILDER_MODE, false);
        JetBootsStateTracker.JetBootsState jbState = JetBootsStateTracker.getTracker(player).getJetBootsState(player);
        JetBootsStateTracker.getTracker(player).setJetBootsState(player, jbState.isEnabled(), jbState.isActive(), jetBootsBuilderMode);
    }

    @Override
    public void onToggle(ICommonArmorHandler commonArmorHandler, boolean newState) {
        PlayerEntity player = commonArmorHandler.getPlayer();
        JetBootsStateTracker tracker = JetBootsStateTracker.getTracker(player);
        JetBootsStateTracker.JetBootsState jbs = tracker.getJetBootsState(player);
        tracker.setJetBootsState(player, newState, jbs.isActive(), jbs.isBuilderMode());
        super.onToggle(commonArmorHandler, newState);
    }

    @Override
    public void onDataFieldUpdated(ICommonArmorHandler commonArmorHandler, String tagName, INBT inbt) {
        PlayerEntity player = commonArmorHandler.getPlayer();
        JetBootsHandler.JetBootsLocalState jbLocal = commonArmorHandler.getExtensionData(this);
        switch (tagName) {
            case ItemPneumaticArmor.NBT_BUILDER_MODE:
                JetBootsStateTracker.getTracker(player).getJetBootsState(player).setBuilderMode(((ByteNBT) inbt).getAsByte() == 1);
                break;
            case ItemPneumaticArmor.NBT_JET_BOOTS_POWER:
                jbLocal.jetBootsPower = MathHelper.clamp(((IntNBT) inbt).getAsInt() / 100f, 0f, 1f);
                break;
            case ItemPneumaticArmor.NBT_FLIGHT_STABILIZERS:
                jbLocal.flightStabilizers = ((ByteNBT) inbt).getAsByte() == 1;
                break;
            case ItemPneumaticArmor.NBT_SMART_HOVER:
                jbLocal.smartHover = ((ByteNBT) inbt).getAsByte() == 1;
                break;
        }
    }

    public void setJetBootsActive(ICommonArmorHandler commonArmorHandler, boolean newActive) {
        PlayerEntity player = commonArmorHandler.getPlayer();
        JetBootsStateTracker.JetBootsState jbs = JetBootsStateTracker.getTracker(player).getJetBootsState(player);
        JetBootsLocalState jbLocal = commonArmorHandler.getExtensionData(this);

        if (!newActive) {
            jbLocal.resetActive();
            if (jbLocal.isFlightStabilizers() && jbs.isActive() && !jbs.isBuilderMode()) {
                if (!player.level.isClientSide) {
                    double l = Math.pow(player.getDeltaMovement().length(), 1.65);
                    commonArmorHandler.addAir(EquipmentSlotType.FEET, (int) (l * -50));
                    NetworkHandler.sendToAllTracking(new PacketSpawnParticle(AirParticleData.DENSE, player.getX(), player.getY(), player.getZ(), 0, 0, 0, (int) (l * 2), 0, 0, 0), player.level, player.blockPosition());
                }
                player.setDeltaMovement(Vector3d.ZERO);
            }
        }

        if (jbLocal.isSmartHover()) {
            jbLocal.setHovering(true);
        }

        JetBootsStateTracker.getTracker(player).setJetBootsState(player, jbs.isEnabled(), newActive, jbs.isBuilderMode());
        player.setForcedPose(newActive && !jbs.isBuilderMode() ? Pose.FALL_FLYING : null);
    }

    private void setYMotion(Entity entity, double y) {
        Vector3d v = entity.getDeltaMovement();
        v = v.add(0, y - v.y, 0);
        entity.setDeltaMovement(v);
    }

    public JetBootsStateTracker.JetBootsState getJetBootsSyncedState(ICommonArmorHandler commonArmorHandler) {
        return JetBootsStateTracker.getTracker(commonArmorHandler.getPlayer()).getJetBootsState(commonArmorHandler.getPlayer());
    }

    /**
     * Stuff that isn't sync'd like in JetBootsStateTracker but tracked internally on both client and server
     */
    public static class JetBootsLocalState implements IArmorExtensionData {
        public boolean smartHover;
        public boolean flightStabilizers;
        public float jetBootsPower;
        private float flightAccel = 1.0F;  // increases while diving, decreases while climbing
        private int prevJetBootsAirUsage;  // so we know when the jet boots are starting up
        private int jetBootsActiveTicks;
        private boolean hovering;

        public void tickActive() {
            jetBootsActiveTicks++;
        }

        public void resetActive() {
            jetBootsActiveTicks = 0;
        }

        public void updateAccel(Vector3d lookVec) {
            float div = lookVec.y > 0 ? -64f : -16f;
            flightAccel = MathHelper.clamp(flightAccel + (float)lookVec.y / div, 0.8F, 4.2F);
        }

        public Vector3d getEffectiveMotion(Vector3d lookVec) {
            lookVec = lookVec.scale(flightAccel * jetBootsPower);
            if (jetBootsActiveTicks < 20 && jetBootsActiveTicks > 0) {
                // simulate lower performance in first 20 ticks due to spin-up time
                lookVec = lookVec.scale(jetBootsActiveTicks * 0.05);
            }
            return lookVec;
        }

        public boolean isHovering() {
            return hovering;
        }

        public void setHovering(boolean hovering) {
            this.hovering = hovering;
        }

        public int getPrevJetBootsAirUsage() {
            return prevJetBootsAirUsage;
        }

        public void setPrevJetBootsAirUsage(int prevJetBootsAirUsage) {
            this.prevJetBootsAirUsage = prevJetBootsAirUsage;
        }

        public void resetAccel() {
            flightAccel = 1.0f;
        }

        public int calcAirUsage(int jetbootsCount) {
            return (int) (PNCConfig.Common.Armor.jetBootsAirUsage * jetbootsCount * jetBootsPower);
        }

        public boolean isSmartHover() {
            return smartHover;
        }

        public boolean isFlightStabilizers() {
            return flightStabilizers;
        }
    }
}
