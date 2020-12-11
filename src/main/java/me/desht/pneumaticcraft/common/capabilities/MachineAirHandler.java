package me.desht.pneumaticcraft.common.capabilities;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.api.tileentity.IAirListener;
import me.desht.pneumaticcraft.api.tileentity.IManoMeasurable;
import me.desht.pneumaticcraft.client.sound.MovingSounds;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdatePressureBlock;
import me.desht.pneumaticcraft.common.particle.AirParticleData;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.upgrade.ApplicableUpgradesDB;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A ticking air handler owned by a tile entity, which disperses air to those neighbouring air handlers
 * which have lower pressure than it does.
 */
public class MachineAirHandler extends BasicAirHandler implements IAirHandlerMachine, IManoMeasurable {
    private final float dangerPressure;
    private final float criticalPressure;
    private int volumeUpgrades = 0;
    private boolean hasSecurityUpgrade = false;
    private final BitSet connectedFaces = new BitSet(6);
    private Direction leakDir = null;
    private Direction prevLeakDir = null;
    private boolean safetyLeak;
    private int prevAir;
    private final List<LazyOptional<IAirHandlerMachine>> neighbourAirHandlers = new ArrayList<>();

    public MachineAirHandler(float dangerPressure, float criticalPressure, int volume) {
        super(volume);

        this.dangerPressure = dangerPressure;
        this.criticalPressure = criticalPressure;
        for (Direction ignored : Direction.VALUES) {
            this.neighbourAirHandlers.add(LazyOptional.empty());
        }
    }

    @Override
    public int getVolume() {
        return ApplicableUpgradesDB.getInstance().getUpgradedVolume(getBaseVolume(), volumeUpgrades);
    }

    @Override
    public float getDangerPressure() {
        return dangerPressure;
    }

    @Override
    public float getCriticalPressure() {
        return criticalPressure;
    }

    @Override
    public void setPressure(float pressure) {
        addAir(((int) (pressure * getVolume())) - getAir());
    }

    @Override
    public void setVolumeUpgrades(int newVolumeUpgrades) {
        int newVolume = ApplicableUpgradesDB.getInstance().getUpgradedVolume(getBaseVolume(), newVolumeUpgrades);
        if (newVolume < getVolume()) {
            // a decrease in volume causes a proportionate decrease in air amount to keep the pressure constant
            int newAir = (int) (getAir() * (float) newVolume / getVolume());
            addAir(newAir - getAir());
        }
        this.volumeUpgrades = newVolumeUpgrades;
    }

    @Override
    public void setHasSecurityUpgrade(boolean hasSecurityUpgrade) {
        this.hasSecurityUpgrade = hasSecurityUpgrade;
    }

    @Override
    public void setConnectedFaces(List<Direction> sides) {
        connectedFaces.clear();
        sides.forEach(side -> connectedFaces.set(side.getIndex()));

        // invalidate cached neighbour data
        for (int i = 0; i < neighbourAirHandlers.size(); i++) {
            neighbourAirHandlers.set(i, LazyOptional.empty());
        }
    }

    @Override
    public void tick(TileEntity ownerTE) {
        World world = ownerTE.getWorld();
        Direction actualLeakDir = leakDir;
        if (!world.isRemote) {
            // server
            disperseAir(ownerTE);

            BlockPos pos = ownerTE.getPos();
            if (hasSecurityUpgrade) {
                if (!safetyLeak && getPressure() >= dangerPressure) {
                    safetyLeak = true;
                } else if (safetyLeak && getPressure() < dangerPressure - 0.2) {
                    safetyLeak = false;
                }
            } else if (world.getServer().getTickCounter() > 20) {
                // little kludge: no overpressure checks right after server starts up (just to be safe)
                doOverpressureChecks(ownerTE, world, pos);
            }

            actualLeakDir = safetyLeak ? anyClearDirection(world, pos) : leakDir;
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

    private Direction anyClearDirection(World w, BlockPos pos) {
        for (Direction d : Direction.VALUES) {
            BlockPos pos2 = pos.offset(d);
            if (!PneumaticCraftUtils.blockHasSolidSide(w.getBlockState(pos2), w, pos2, d.getOpposite())) return d;
        }
        return Direction.UP; // arbitrary
    }

    private Direction anyConnectedFace() {
        for (Direction d : Direction.VALUES) {
            if (connectedFaces.get(d.getIndex())) return d;
        }
        return null;
    }

    private void doOverpressureChecks(TileEntity ownerTE, World world, BlockPos pos) {
        float p = getPressure();
        if (getAir() > prevAir && p > dangerPressure) {
            float range = criticalPressure - dangerPressure;
            float delta = p - dangerPressure;
            float rnd = world.rand.nextFloat() * range;
            if (rnd < delta / 125f || p > criticalPressure) {
                world.createExplosion(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 1.0F, Explosion.Mode.BREAK);
                world.destroyBlock(pos, false);
            } else if (rnd < delta / 25f) {
                world.playSound(null, ownerTE.getPos(), ModSounds.CREAK.get(), SoundCategory.BLOCKS, 0.7f, 0.6f + world.rand.nextFloat() * 0.8f);
            }
        }
    }

    private void handleAirLeak(TileEntity ownerTE, Direction actualLeakDir) {
        World world = ownerTE.getWorld();
        BlockPos pos = ownerTE.getPos();

        float pressure = getPressure();

        if (!world.isRemote) {
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
            double mx = actualLeakDir.getXOffset();
            double my = actualLeakDir.getYOffset();
            double mz = actualLeakDir.getZOffset();
            double speed = getPressure() * 0.1F;
            if (getAir() > 0) {
                if (pressure > 1f || pressure > 0.5f && world.rand.nextBoolean() || world.rand.nextInt(3) == 0) {
                    world.addParticle(AirParticleData.DENSE, pos.getX() + 0.5D + mx * 0.6, pos.getY() + 0.5D + my * 0.6, pos.getZ() + 0.5D + mz * 0.6,
                            mx * speed, my * speed, mz * speed);
                }
            } else if (getAir() < 0 && world.rand.nextBoolean()) {
                world.addParticle(AirParticleData.NORMAL, pos.getX() + 0.5D + mx, pos.getY() + 0.5D + my, pos.getZ() + 0.5D + mz, mx * speed, my * speed, mz * speed);
            }
            MovingSounds.playMovingSound(MovingSounds.Sound.AIR_LEAK, ownerTE.getPos(), anyConnectedFace());
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

    private LazyOptional<IAirHandlerMachine> getNeighbourAirHandler(TileEntity ownerTE, Direction dir) {
        final int idx = dir.getIndex();

        if (!connectedFaces.get(idx)) return LazyOptional.empty();

        if (!neighbourAirHandlers.get(idx).isPresent()) {
            TileEntity te1 = ownerTE.getWorld().getTileEntity(ownerTE.getPos().offset(dir));
            if (te1 != null) {
                LazyOptional<IAirHandlerMachine> cap = te1.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, dir.getOpposite());
                if (cap.isPresent()) {
                    neighbourAirHandlers.set(idx, cap);
                    neighbourAirHandlers.get(idx).addListener(l -> neighbourAirHandlers.set(idx, LazyOptional.empty()));
                }
            } else {
                neighbourAirHandlers.set(idx, LazyOptional.empty());
            }
        }
        return neighbourAirHandlers.get(idx);
    }

    private void disperseAir(TileEntity ownerTE) {
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

    private List<Connection> getConnectedAirHandlers(TileEntity ownerTE, boolean onlyLowerPressure) {
        List<IAirHandlerMachine.Connection> neighbours = new ArrayList<>();
        for (Direction dir : Direction.VALUES) {
            if (connectedFaces.get(dir.getIndex())) {
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
                .collect(Collectors.toList()));
        return neighbours;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = super.serializeNBT();
        if (leakDir != null) nbt.putByte("Leaking", (byte) leakDir.getIndex());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        leakDir = nbt.contains("Leaking") ? Direction.byIndex(nbt.getByte("Leaking")) : null;
    }

    @Override
    public List<IAirHandlerMachine.Connection> getConnectedAirHandlers(TileEntity ownerTE) {
        return getConnectedAirHandlers(ownerTE, false);
    }

    private List<IAirHandlerMachine> addExtraConnectedHandlers(TileEntity ownerTE) {
        if (ownerTE instanceof IAirListener) {
            return ((IAirListener) ownerTE).addConnectedPneumatics(new ArrayList<>());
        }
        return Collections.emptyList();
    }

    private void onAirDispersion(TileEntity ownerTE, Direction dir, int airDispersed) {
        if (ownerTE instanceof IAirListener) {
            ((IAirListener) ownerTE).onAirDispersion(this, dir, airDispersed);
        }
    }

    private int getMaxDispersion(TileEntity ownerTE, Direction dir) {
        if (ownerTE instanceof IAirListener) {
            return ((IAirListener) ownerTE).getMaxDispersion(this, dir);
        } else {
            return Integer.MAX_VALUE;
        }
    }

    @Override
    public void printManometerMessage(PlayerEntity player, List<ITextComponent> curInfo) {
        curInfo.add(new TranslationTextComponent("pneumaticcraft.gui.tooltip.pressure",
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

        public void setMaxDispersion(int maxDispersion) {
            this.maxDispersion = maxDispersion;
        }

        @Override
        public int getDispersedAir() {
            return toDisperse;
        }

        public void setAirToDisperse(int toDisperse) {
            this.toDisperse = toDisperse;
        }

        @Override
        public IAirHandlerMachine getAirHandler() {
            return airHandler;
        }
    }
}
