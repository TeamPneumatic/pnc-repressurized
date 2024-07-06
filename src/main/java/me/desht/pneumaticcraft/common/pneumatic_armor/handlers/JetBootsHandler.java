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
import me.desht.pneumaticcraft.client.sound.MovingSounds;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlayMovingSound;
import me.desht.pneumaticcraft.common.network.PacketPlayMovingSound.MovingSoundFocus;
import me.desht.pneumaticcraft.common.network.PacketSpawnParticle;
import me.desht.pneumaticcraft.common.particle.AirParticleData;
import me.desht.pneumaticcraft.common.pneumatic_armor.JetBootsStateTracker;
import me.desht.pneumaticcraft.common.registry.ModCriterionTriggers;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Optional;
import java.util.function.Supplier;

public class JetBootsHandler extends BaseArmorUpgradeHandler<JetBootsHandler.JetBootsLocalState> {
    public static final int BUILDER_MODE_LEVEL = 3;  // tier needed for builder mode
    public static final int STABILIZERS_LEVEL = 4;  // tier needed for flight stabilizers

    @Override
    public ResourceLocation getID() {
        return BuiltinArmorUpgrades.JET_BOOTS;
    }

    @Override
    public PNCUpgrade[] getRequiredUpgrades() {
        return new PNCUpgrade[] { ModUpgrades.JET_BOOTS.get() };
    }

    @Override
    public float getIdleAirUsage(ICommonArmorHandler armorHandler) {
        return 0;
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.FEET;
    }

    @Override
    public Supplier<JetBootsLocalState> extensionData() {
        return JetBootsLocalState::new;
    }

    @Override
    public void tick(ICommonArmorHandler commonArmorHandler, boolean enabled) {
        if (commonArmorHandler.isOnCooldown(EquipmentSlot.FEET)) {
            // putting the boots on cooldown is a way for other mods to temporarily suppress jet boots
            return;
        }

        int jetbootsCount = commonArmorHandler.getUpgradeCount(EquipmentSlot.FEET, ModUpgrades.JET_BOOTS.get());
        if (jetbootsCount == 0) return;

        int jetbootsAirUsage = 0;

        Player player = commonArmorHandler.getPlayer();
        JetBootsStateTracker.JetBootsState jbState = JetBootsStateTracker.getTracker(player).getJetBootsState(player);
        JetBootsLocalState jbLocal = commonArmorHandler.getExtensionData(this);

        if (commonArmorHandler.hasMinPressure(EquipmentSlot.FEET)) {
            if (jbState.isActive()) {
                if (jbState.isBuilderMode() && jetbootsCount >= BUILDER_MODE_LEVEL) {
                    // builder mode - rise vertically (or hover if sneaking and firing)
                    setYMotion(player, player.isShiftKeyDown() ? 0 : 0.15 + 0.15 * (jetbootsCount - 3));
                    jetbootsAirUsage = (int) (ConfigHelper.common().armor.jetBootsAirUsage.get() * jetbootsCount / 2.5F);
                } else {
                    // jetboots firing - move in direction of looking
                    Vec3 lookVec = player.getLookAngle().scale(0.3 * jetbootsCount);
                    jbLocal.updateAccel(lookVec);
                    lookVec = jbLocal.getEffectiveMotion(lookVec, player.isFallFlying());
                    player.setDeltaMovement(lookVec.x, player.onGround() ? 0 : lookVec.y, lookVec.z);
                    jetbootsAirUsage = jbLocal.calcAirUsage(jetbootsCount);
                }
                if (player.isInWater()) jetbootsAirUsage *= 4;
                jbLocal.tickActive();
            } else if (jbState.isEnabled() && !isOnGround(player) && !player.isFallFlying()) {
                // jetboots not firing, but enabled - slowly descend (or hover if enough upgrades)
                // and bring player to complete halt if flight stabilizers and not actively moving forward/sideways
                boolean reallyHovering = jbLocal.canHover() && (!jbLocal.isSmartHover() || jbLocal.isHovering());
                boolean stopped = jbLocal.isFlightStabilizers()
                        && jetbootsCount >= STABILIZERS_LEVEL
                        && PneumaticCraftUtils.epsilonEquals(player.zza, 0f)
                        && PneumaticCraftUtils.epsilonEquals(player.xxa, 0f);
                double xMotion = stopped ? 0 : player.getDeltaMovement().x;
                double yMotion = reallyHovering ? (player.isShiftKeyDown() ? -0.45 : -0.1 + 0.02 * jetbootsCount) : player.getDeltaMovement().y;
                double zMotion = stopped ? 0 : player.getDeltaMovement().z;
                player.setDeltaMovement(new Vec3(xMotion, yMotion, zMotion));
                if (reallyHovering) player.fallDistance = 0;
                jetbootsAirUsage = reallyHovering ? (int) (ConfigHelper.common().armor.jetBootsAirUsage.get() * (player.isShiftKeyDown() ? 0.25F : 0.5F)) : 0;
                jbLocal.resetAccel();
            } else if (isOnGround(player)) {
                jbLocal.setHovering(false);
            } else {
                jbLocal.resetAccel();
            }
        } else {
            // insufficient pressure!
            if (jbState.isEnabled() && !player.onGround() && !player.isFallFlying() && jbLocal.isHovering()) {
                // still active and in the air: using minimal air here keeps the boots running
                // and thus avoids triggering multiple looping sounds (see "jet boots starting up" code below)
                jetbootsAirUsage = 1;
            }
            setJetBootsActive(commonArmorHandler,false);
        }

        if (jetbootsAirUsage != 0 && !player.level().isClientSide) {
            if (jbLocal.getPrevJetBootsAirUsage() == 0) {
                // jet boots starting up
                NetworkHandler.sendToAllTracking(new PacketPlayMovingSound(MovingSounds.Sound.JET_BOOTS, MovingSoundFocus.of(player)), player.level(), player.blockPosition());
                ModCriterionTriggers.FLIGHT.get().trigger((ServerPlayer) player);
            }
            if (player.horizontalCollision) {
                double vel = player.getDeltaMovement().length();
                if (player.level().getDifficulty() == Difficulty.HARD) {
                    vel *= 2;
                } else if (player.level().getDifficulty() == Difficulty.NORMAL) {
                    vel *= 1.5;
                }
                if (vel > 2) {
                    player.playSound(vel > 2.5 ? SoundEvents.GENERIC_BIG_FALL : SoundEvents.GENERIC_SMALL_FALL, 1.0F, 1.0F);
                    player.hurt(player.damageSources().flyIntoWall(), (float) vel);
                    ModCriterionTriggers.FLY_INTO_WALL.get().trigger((ServerPlayer) player);
                }
            }
            commonArmorHandler.addAir(EquipmentSlot.FEET, -jetbootsAirUsage);

            if (player.position().y > player.level().getMaxBuildHeight() + 64) {
                player.getCooldowns().addCooldown(ModItems.PNEUMATIC_BOOTS.get(), 20);
            }
        }
        jbLocal.setPrevJetBootsAirUsage(jetbootsAirUsage);

    }

    private boolean isOnGround(Player player) {
        if (player.onGround()) return true;
        if (!player.level().isClientSide) {
            // isOnGround() on server can be unreliable, especially if player flew into the ground
            // this little kludge makes sure jetboots properly switch off on both client and server
            BlockPos pos = player.getOnPos();
            VoxelShape shape = player.level().getBlockState(pos).getCollisionShape(player.level(), pos);
            if (!shape.isEmpty()) {
                return player.getBoundingBox().move(0, -0.01, 0).intersects(shape.bounds().move(pos));
            }
        }
        return false;
    }

    @Override
    public void onInit(ICommonArmorHandler commonArmorHandler) {
        Player player = commonArmorHandler.getPlayer();
        ItemStack armorStack = player.getItemBySlot(EquipmentSlot.FEET);
        JetBootsHandler.JetBootsLocalState jbLocal = commonArmorHandler.getExtensionData(this);
        jbLocal.flightStabilizers = PneumaticArmorItem.getBooleanData(armorStack, ModDataComponents.JET_BOOTS_STABILIZERS.get(), false);
        jbLocal.jetBootsPower = PneumaticArmorItem.getIntData(armorStack, ModDataComponents.JET_BOOTS_PCT.get(), 100, 0, 100) / 100f;
        jbLocal.hover = PneumaticArmorItem.getBooleanData(armorStack, ModDataComponents.JET_BOOTS_HOVER.get(), true);
        jbLocal.smartHover = PneumaticArmorItem.getBooleanData(armorStack, ModDataComponents.JET_BOOTS_SMART_HOVER.get(), false);
        boolean jetBootsBuilderMode = PneumaticArmorItem.getBooleanData(armorStack, ModDataComponents.JET_BOOTS_BUILDER_MODE.get(), false);
        JetBootsStateTracker.JetBootsState jbState = JetBootsStateTracker.getTracker(player).getJetBootsState(player);
        JetBootsStateTracker.getTracker(player).setJetBootsState(player, jbState.isEnabled(), jbState.isActive(), jetBootsBuilderMode);
    }

    @Override
    public void onToggle(ICommonArmorHandler commonArmorHandler, boolean newState) {
        Player player = commonArmorHandler.getPlayer();
        JetBootsStateTracker tracker = JetBootsStateTracker.getTracker(player);
        JetBootsStateTracker.JetBootsState jbs = tracker.getJetBootsState(player);
        tracker.setJetBootsState(player, newState, jbs.isActive(), jbs.isBuilderMode());
        super.onToggle(commonArmorHandler, newState);
    }

    @Override
    public void onShutdown(ICommonArmorHandler commonArmorHandler) {
        setJetBootsActive(commonArmorHandler, false);
    }

    @Override
    public void onDataFieldUpdated(ICommonArmorHandler commonArmorHandler, DataComponentType<?> componentType, Object val) {
        Player player = commonArmorHandler.getPlayer();
        JetBootsHandler.JetBootsLocalState jbLocal = commonArmorHandler.getExtensionData(this);
        if (componentType == ModDataComponents.JET_BOOTS_BUILDER_MODE.get()) {
            JetBootsStateTracker.getTracker(player).getJetBootsState(player).setBuilderMode((Boolean) val);
        } else if (componentType == ModDataComponents.JET_BOOTS_PCT.get()) {
            jbLocal.jetBootsPower = Mth.clamp((Integer) val / 100f, 0f, 1f);
        } else if (componentType == ModDataComponents.JET_BOOTS_STABILIZERS.get()) {
            jbLocal.flightStabilizers = (Boolean) val;
        } else if (componentType == ModDataComponents.JET_BOOTS_HOVER.get()) {
            jbLocal.hover = (Boolean) val;
        } else if (componentType == ModDataComponents.JET_BOOTS_SMART_HOVER.get()) {
            jbLocal.smartHover = (Boolean) val;
        }
    }

    public void setJetBootsActive(ICommonArmorHandler commonArmorHandler, boolean newActive) {
        Player player = commonArmorHandler.getPlayer();
        JetBootsStateTracker.JetBootsState jbs = JetBootsStateTracker.getTracker(player).getJetBootsState(player);
        JetBootsLocalState jbLocal = commonArmorHandler.getExtensionData(this);

        if (!newActive) {
            jbLocal.resetActive();
            if (jbLocal.isFlightStabilizers() && jbs.isActive() && !jbs.isBuilderMode()) {
                if (!player.level().isClientSide) {
                    double l = Math.pow(player.getDeltaMovement().length(), 1.65);
                    commonArmorHandler.addAir(EquipmentSlot.FEET, (int) (l * -50));
                    NetworkHandler.sendToAllTracking(new PacketSpawnParticle(AirParticleData.DENSE,
                            player.position().toVector3f(),
                            PneumaticCraftUtils.VEC3F_ZERO,
                            (int) (l * 2),
                            Optional.empty()
                    ), player.level(), player.blockPosition());
                }
                player.setDeltaMovement(Vec3.ZERO);
            }
        }

        if (jbLocal.isSmartHover()) {
            jbLocal.setHovering(true);
        }

        JetBootsStateTracker.getTracker(player).setJetBootsState(player, jbs.isEnabled(), newActive, jbs.isBuilderMode());
        player.setForcedPose(newActive && !jbs.isBuilderMode() ? Pose.FALL_FLYING : null);
    }

    private void setYMotion(Entity entity, double y) {
        Vec3 v = entity.getDeltaMovement();
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
        public boolean hover;
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

        public void updateAccel(Vec3 lookVec) {
            float div = lookVec.y > 0 ? -64f : -16f;
            flightAccel = Mth.clamp(flightAccel + (float)lookVec.y / div, 0.8F, 4.2F);
        }

        public Vec3 getEffectiveMotion(Vec3 lookVec, boolean gliding) {
            lookVec = lookVec.scale(flightAccel * jetBootsPower);
            if (!gliding && jetBootsActiveTicks < 20 && jetBootsActiveTicks > 0) {
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
            return (int) (ConfigHelper.common().armor.jetBootsAirUsage.get() * jetbootsCount * jetBootsPower);
        }

        public boolean canHover() {
            return hover;
        }

        public boolean isSmartHover() {
            return smartHover;
        }

        public boolean isFlightStabilizers() {
            return flightStabilizers;
        }
    }
}
