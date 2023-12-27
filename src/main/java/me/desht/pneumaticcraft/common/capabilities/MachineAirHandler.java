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

package me.desht.pneumaticcraft.common.capabilities;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.pressure.PressureHelper;
import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.api.tileentity.IAirListener;
import me.desht.pneumaticcraft.api.tileentity.IManoMeasurable;
import me.desht.pneumaticcraft.client.sound.MovingSounds;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdatePressureBlock;
import me.desht.pneumaticcraft.common.particle.AirParticleData;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

/**
 * A ticking air handler owned by a block entity, which disperses air to those neighbouring air handlers
 * which have lower pressure than it does.
 */
public class MachineAirHandler extends BasicAirHandler implements IAirHandlerMachine, IManoMeasurable {
    private final PressureTier tier;
    private int volumeUpgrades = 0;
    private final BitSet connectedFaces = new BitSet(6);
    private Direction leakDir = null;
    private Direction prevLeakDir = null;
    private int prevAir;
    private final Map<Direction, LazyOptional<IAirHandlerMachine>> neighbourAirHandlers = new EnumMap<>(Direction.class);
    private final Map<Direction, NonNullConsumer<LazyOptional<IAirHandlerMachine>>> neighbourAirInvalidationListeners = new EnumMap<>(Direction.class);

    // note: leaks due to security upgrade are tracked separately from leaks due to disconnection
    private boolean safetyLeaking;   // is the handler venting right now?
    private Direction safetyLeakDir; // direction handler would vent in (non-null does not mean actively venting)
    private Predicate<Float> safetyPredicate;  // for determining when safety venting is needed

    public MachineAirHandler(PressureTier tier, int volume) {
        super(volume);

        this.tier = tier;
        for (Direction dir : DirectionUtil.VALUES) {
            this.neighbourAirHandlers.put(dir, LazyOptional.empty());
            this.neighbourAirInvalidationListeners.put(dir, l -> {
                if (l != this.neighbourAirHandlers.get(dir)) {
                    return;
                }

                this.neighbourAirHandlers.put(dir, LazyOptional.empty());
            });
        }
    }

    @Override
    public int getVolume() {
        return PressureHelper.getUpgradedVolume(getBaseVolume(), volumeUpgrades);
    }

    @Override
    public float getDangerPressure() {
        return tier.getDangerPressure();
    }

    @Override
    public float getCriticalPressure() {
        return tier.getCriticalPressure();
    }

    @Override
    public void setPressure(float pressure) {
        addAir(((int) (pressure * getVolume())) - getAir());
    }

    @Override
    public void setVolumeUpgrades(int newVolumeUpgrades) {
        int newVolume = PressureHelper.getUpgradedVolume(getBaseVolume(), newVolumeUpgrades);
        if (newVolume < getVolume()) {
            // a decrease in volume causes a proportionate decrease in air amount to keep the pressure constant
            int newAir = (int) (getAir() * (float) newVolume / getVolume());
            addAir(newAir - getAir());
        }
        this.volumeUpgrades = newVolumeUpgrades;
    }

    @Override
    public void enableSafetyVenting(Predicate<Float> pressureCheck, Direction dir) {
        this.safetyLeakDir = dir;
        this.safetyPredicate = pressureCheck;
    }

    @Override
    public void disableSafetyVenting() {
        this.safetyLeakDir = null;
        this.safetyPredicate = null;
    }

    @Override
    public void setConnectedFaces(List<Direction> sides) {
        connectedFaces.clear();
        sides.forEach(side -> connectedFaces.set(side.get3DDataValue()));

        // invalidate cached neighbour data
        for (Direction dir : DirectionUtil.VALUES) {
            this.neighbourAirHandlers.put(dir, LazyOptional.empty());
        }
    }

    @Override
    public void tick(BlockEntity ownerTE) {
        Level world = Objects.requireNonNull(ownerTE.getLevel());
        Direction actualLeakDir = leakDir;
        if (!world.isClientSide) {
            // server
            disperseAir(ownerTE);

            BlockPos pos = ownerTE.getBlockPos();
            if (safetyLeakDir != null) {
                float pressure = getPressure();
                if (!safetyLeaking && safetyPredicate.test(pressure)) {
                    safetyLeaking = true;
                } else if (safetyLeaking && !safetyPredicate.test(pressure + 0.2f)) {
                    safetyLeaking = false;
                }
                // also cap pressure at critical level (it's still possible for air to added faster than it can vent)
                if (pressure >= getCriticalPressure()) {
                    int wanted = (int)(getCriticalPressure() * getVolume());
                    addAir(wanted - getAir());
                }
            } else if (Objects.requireNonNull(world.getServer()).getTickCount() > 20) {
                // little kludge: no overpressure checks right after server starts up (just to be safe)
                doOverpressureChecks(ownerTE, world, pos);
            }

            actualLeakDir = safetyLeaking ? safetyLeakDir : leakDir;
            if (prevLeakDir != actualLeakDir || actualLeakDir != null && (world.getGameTime() & 0x1f) == 0) {
                // if leak status changes, sync pressure & leak dir to the client
                // OR if already leaking, periodically sync pressure & leak dir to the client
                NetworkHandler.sendToAllTracking(new PacketUpdatePressureBlock(ownerTE, anyConnectedFace(), actualLeakDir, getAir()), ownerTE);
            }

            prevAir = getAir();
            prevLeakDir = actualLeakDir;
        }

        if (actualLeakDir != null && getAir() != 0) {
            handleAirLeak(ownerTE, actualLeakDir);
        }
    }

    private Direction anyConnectedFace() {
        for (Direction d : DirectionUtil.VALUES) {
            if (connectedFaces.get(d.get3DDataValue())) return d;
        }
        return null;
    }

    private void doOverpressureChecks(BlockEntity ownerTE, Level world, BlockPos pos) {
        float p = getPressure();
        if (getAir() > prevAir && p > getDangerPressure()) {
            float range = getCriticalPressure() - getDangerPressure();
            float delta = p - getDangerPressure();
            float rnd = world.random.nextFloat() * range;
            if (rnd < delta / 125f || p > getCriticalPressure()) {
                world.explode(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 1.0F, Explosion.BlockInteraction.BREAK);
                world.destroyBlock(pos, false);
                // notify client too so block shapes can be properly updated
                PneumaticRegistry.getInstance().getMiscHelpers().forceClientShapeRecalculation(world, pos);
            } else if (rnd < delta / 25f) {
                world.playSound(null, ownerTE.getBlockPos(), ModSounds.CREAK.get(), SoundSource.BLOCKS, 0.7f, 0.6f + world.random.nextFloat() * 0.8f);
            }
        }
    }

    private void handleAirLeak(BlockEntity ownerTE, Direction actualLeakDir) {
        Level world = Objects.requireNonNull(ownerTE.getLevel());
        BlockPos pos = ownerTE.getBlockPos();

        float pressure = getPressure();

        if (!world.isClientSide) {
            if (getAir() > 0) {
                int leakedAmount = (int) (pressure * PneumaticValues.AIR_LEAK_FACTOR) + 20;
                if (leakedAmount > getAir()) leakedAmount = getAir();
                onAirDispersion(ownerTE, leakDir, -leakedAmount);
                addAir(-leakedAmount);
            } else if (getAir() < 0) {
                int leakedAmount = -(int) (pressure * PneumaticValues.AIR_LEAK_FACTOR) + 20;
                if (getAir() > leakedAmount) leakedAmount = -getAir();
                onAirDispersion(ownerTE, leakDir, leakedAmount);
                addAir(leakedAmount);
            }
        } else {
            double mx = actualLeakDir.getStepX();
            double my = actualLeakDir.getStepY();
            double mz = actualLeakDir.getStepZ();
            double speed = getPressure() * 0.1F;
            if (getAir() > 0) {
                if (pressure > 1f || pressure > 0.5f && world.random.nextBoolean() || world.random.nextInt(3) == 0) {
                    world.addParticle(AirParticleData.DENSE, pos.getX() + 0.5D + mx * 0.6, pos.getY() + 0.5D + my * 0.6, pos.getZ() + 0.5D + mz * 0.6,
                            mx * speed, my * speed, mz * speed);
                }
            } else if (getAir() < 0 && world.random.nextBoolean()) {
                world.addParticle(AirParticleData.DENSE, pos.getX() + 0.5D + mx, pos.getY() + 0.5D + my, pos.getZ() + 0.5D + mz, mx * speed, my * speed, mz * speed);
            }
            MovingSounds.playMovingSound(MovingSounds.Sound.AIR_LEAK, ownerTE.getBlockPos(), anyConnectedFace());
        }
    }

    @Override
    public void setSideLeaking(@Nullable Direction dir) {
        this.leakDir = dir;
    }

    @Nullable
    @Override
    public Direction getSideLeaking() {
        return this.leakDir;
    }

    private LazyOptional<IAirHandlerMachine> getNeighbourAirHandler(BlockEntity ownerTE, Direction dir) {
        if (!connectedFaces.get(dir.get3DDataValue())) return LazyOptional.empty();

        if (!neighbourAirHandlers.get(dir).isPresent()) {
            BlockEntity te1 = Objects.requireNonNull(ownerTE.getLevel()).getBlockEntity(ownerTE.getBlockPos().relative(dir));
            if (te1 != null) {
                LazyOptional<IAirHandlerMachine> cap = te1.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, dir.getOpposite());
                if (cap.isPresent()) {
                    neighbourAirHandlers.put(dir, cap);
                    cap.addListener(neighbourAirInvalidationListeners.get(dir));
                }
            } else {
                neighbourAirHandlers.put(dir, LazyOptional.empty());
            }
        }
        return neighbourAirHandlers.get(dir);
    }

    private void disperseAir(BlockEntity ownerTE) {
        // 1. build a list of all neighbouring and otherwise connected air handlers with a lower pressure than us
        List<IAirHandlerMachine.Connection> neighbours = getConnectedAirHandlers(ownerTE, true);

        // 2. get the total volume and air amount in this and all connected handlers
        int totalVolume = this.getVolume();
        int totalAir = this.getAir();
        for (IAirHandlerMachine.Connection neighbour : neighbours) {
            totalVolume += neighbour.getAirHandler().getVolume();
            totalAir += neighbour.getAirHandler().getAir();
        }

        // 3. figure out how much air will be dispersed to each neighbour
        for (IAirHandlerMachine.Connection neighbour: neighbours) {
            int totalMachineAir = (int) ((long) totalAir * neighbour.getAirHandler().getVolume() / totalVolume);
            neighbour.setMaxDispersion(getMaxDispersion(ownerTE, neighbour.getDirection()));
            neighbour.setAirToDisperse(Math.max(0, totalMachineAir - neighbour.getAirHandler().getAir()));  // no backflow
        }

        // 4. finally, actually disperse the air
        for (IAirHandlerMachine.Connection neighbour : neighbours) {
            int air = Math.min(neighbour.getMaxDispersion(), neighbour.getDispersedAir());
            if (air != 0) {
                onAirDispersion(ownerTE, neighbour.getDirection(), air);
                neighbour.getAirHandler().addAir(air);
                addAir(-air);
            }
        }
    }

    private List<Connection> getConnectedAirHandlers(BlockEntity ownerTE, boolean onlyLowerPressure) {
        List<IAirHandlerMachine.Connection> neighbours = new ArrayList<>();
        for (Direction dir : DirectionUtil.VALUES) {
            if (connectedFaces.get(dir.get3DDataValue())) {
                getNeighbourAirHandler(ownerTE, dir).ifPresent(h -> {
                    if ((!onlyLowerPressure || h.getPressure() < getPressure())) {
                        neighbours.add(new ConnectedAirHandler(dir, h));
                    }
                });
            }
        }
        neighbours.addAll(addExtraConnectedHandlers(ownerTE).stream()
                .filter(h -> !onlyLowerPressure || h.getPressure() < getPressure())
                .map(ConnectedAirHandler::new)
                .toList());
        return neighbours;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = super.serializeNBT();
        if (leakDir != null) nbt.putByte("Leaking", (byte) leakDir.get3DDataValue());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);
        leakDir = nbt.contains("Leaking") ? Direction.from3DDataValue(nbt.getByte("Leaking")) : null;
    }

    @Override
    public List<IAirHandlerMachine.Connection> getConnectedAirHandlers(BlockEntity ownerTE) {
        return getConnectedAirHandlers(ownerTE, false);
    }

    private List<IAirHandlerMachine> addExtraConnectedHandlers(BlockEntity ownerTE) {
        if (ownerTE instanceof IAirListener) {
            return ((IAirListener) ownerTE).addConnectedPneumatics(new ArrayList<>());
        }
        return Collections.emptyList();
    }

    private void onAirDispersion(BlockEntity ownerTE, Direction dir, int airDispersed) {
        if (ownerTE instanceof IAirListener) {
            ((IAirListener) ownerTE).onAirDispersion(this, dir, airDispersed);
        }
    }

    private int getMaxDispersion(BlockEntity ownerTE, Direction dir) {
        if (ownerTE instanceof IAirListener) {
            return ((IAirListener) ownerTE).getMaxDispersion(this, dir);
        } else {
            return Integer.MAX_VALUE;
        }
    }

    @Override
    public void printManometerMessage(Player player, List<Component> curInfo) {
        curInfo.add(Component.translatable("pneumaticcraft.gui.tooltip.pressure",
                PneumaticCraftUtils.roundNumberTo(getPressure(), 1)));
    }

    private static class ConnectedAirHandler implements IAirHandlerMachine.Connection {
        final Direction direction; // may be null
        final IAirHandlerMachine airHandler;
        int maxDispersion;
        int toDisperse;

        ConnectedAirHandler(Direction direction, IAirHandlerMachine airHandler) {
            this.direction = direction;
            this.airHandler = airHandler;
        }

        ConnectedAirHandler(IAirHandlerMachine airHandler) {
            this(null, airHandler);
        }

        @Override
        public Direction getDirection() {
            return direction;
        }

        @Override
        public int getMaxDispersion() {
            return maxDispersion;
        }

        @Override
        public void setMaxDispersion(int maxDispersion) {
            this.maxDispersion = maxDispersion;
        }

        @Override
        public int getDispersedAir() {
            return toDisperse;
        }

        @Override
        public void setAirToDisperse(int toDisperse) {
            this.toDisperse = toDisperse;
        }

        @Override
        public IAirHandlerMachine getAirHandler() {
            return airHandler;
        }
    }
}
