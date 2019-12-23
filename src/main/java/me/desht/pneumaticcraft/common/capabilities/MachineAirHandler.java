package me.desht.pneumaticcraft.common.capabilities;

import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IAirListener;
import me.desht.pneumaticcraft.client.particle.AirParticleData;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.network.PacketSpawnParticle;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A ticking air handler owned by a tile entity, which disperses air to those neighbouring air handlers
 * which have lower pressure than it does.
 */
public class MachineAirHandler extends BasicAirHandler {
    private final float dangerPressure;
    private final float criticalPressure;
    private final float maxPressure;

    private int volumeUpgrades = 0;
    private boolean hasSecurityUpgrade = false;
    private int soundCounter;

    private final List<LazyOptional<IAirHandler>> neighbourAirHandlers = new ArrayList<>();

    public MachineAirHandler(float dangerPressure, float criticalPressure, int volume) {
        super(volume);
        this.dangerPressure = dangerPressure;
        this.criticalPressure = criticalPressure;
        this.maxPressure = dangerPressure + (criticalPressure - dangerPressure) * (float) Math.random();

        for (Direction ignored : Direction.VALUES) {
            this.neighbourAirHandlers.add(null); // null indicates unknown state, needs querying
        }
    }

    @Override
    public int getVolume() {
        return getBaseVolume() + volumeUpgrades * PneumaticValues.VOLUME_VOLUME_UPGRADE;
    }

    public float getDangerPressure() {
        return dangerPressure;
    }

    public float getCriticalPressure() {
        return criticalPressure;
    }

    public float getMaxPressure() {
        return maxPressure;
    }

    /**
     * Should be called by the owning tile entity when its volume upgrades change.
     *
     * @param newVolumeUpgrades new number of volume upgrades
     */
    public void setVolumeUpgrades(int newVolumeUpgrades) {
        int newVolume = getBaseVolume() + newVolumeUpgrades * PneumaticValues.VOLUME_VOLUME_UPGRADE;
        if (newVolume < getVolume()) {
            // a decrease in volume causes a proportionate decrease in air amount to keep the pressure constant
            airAmount = (int) (airAmount * (float) newVolume / getVolume());
        }
        this.volumeUpgrades = newVolumeUpgrades;
    }

    /**
     * Should be called by the owning tile entity when its security upgrades change.
     *
     * @param hasSecurityUpgrade true if the holder has one or more security upgrades
     */
    public void setHasSecurityUpgrade(boolean hasSecurityUpgrade) {
        this.hasSecurityUpgrade = hasSecurityUpgrade;
    }

    /**
     * Should be called by the owning tile entity when a neighbour block changes state, to force a recache of the
     * neighbouring air handler.
     *
     * @param side side of the owning TE that has noticed a neighbour block change
     */
    public void onNeighborChange(Direction side) {
        neighbourAirHandlers.set(side.getIndex(), null);
    }

    /**
     * Should be called every tick by the owner.
     *
     * @param ownerTE the owning tile entity
     */
    public void tick(TileEntity ownerTE) {
        World world = ownerTE.getWorld();
        if (!world.isRemote) {
            BlockPos pos = ownerTE.getPos();
            if (hasSecurityUpgrade) {
                doSecurityAirChecks(ownerTE);
            }

            if (getPressure() > maxPressure) {
                world.createExplosion(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 1.0F, Explosion.Mode.BREAK);
                world.removeBlock(pos, false);
            } else {
                disperseAir(ownerTE);
            }
        }
        if (soundCounter > 0) soundCounter--;
    }

    private void doSecurityAirChecks(TileEntity ownerTE) {
        if (getPressure() >= dangerPressure - 0.1) {
            airLeak(ownerTE, Direction.UP);
        }

        // Remove any remaining air
        int excessAir = getAir() - (int) (getVolume() * (dangerPressure - 0.1));
        if (excessAir > 0) {
            addAir(-excessAir);
            onAirDispersion(ownerTE,null, -excessAir);
        }
    }

    public void airLeak(TileEntity ownerTE, Direction dir) {
        World world = ownerTE.getWorld();
        BlockPos pos = ownerTE.getPos();

        if (world.isRemote || Math.abs(getPressure()) < 0.01F) return;
        double motionX = dir.getXOffset();
        double motionY = dir.getYOffset();
        double motionZ = dir.getZOffset();
        if (soundCounter <= 0) {
            float pitch = MathHelper.clamp(1.0f + ((getPressure() - 3) / 10), 0.8f, 1.2f);
            soundCounter = (int) (20 / pitch);
            NetworkHandler.sendToAllAround(new PacketPlaySound(ModSounds.LEAKING_GAS, SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), 0.1F, pitch, true), world);
        }

        if (getPressure() < 0) {
            double speed = getPressure() * 0.1F - 0.1F;
            NetworkHandler.sendToAllAround(new PacketSpawnParticle(AirParticleData.DENSE, pos.getX() + 0.5D + motionX / 2D, pos.getY() + 0.5D + motionY / 2D, pos.getZ() + 0.5D + motionZ / 2D, motionX * speed, motionY * speed, motionZ * speed), world);
            int dispersedAmount = -(int) (getPressure() * PneumaticValues.AIR_LEAK_FACTOR) + 20;
            if (getAir() > dispersedAmount) dispersedAmount = -getAir();
            onAirDispersion(ownerTE, dir, dispersedAmount);
            addAir(dispersedAmount);
        } else {
            double speed = getPressure() * 0.1F + 0.1F;
            NetworkHandler.sendToAllAround(new PacketSpawnParticle(AirParticleData.DENSE, pos.getX() + 0.5D + motionX / 2D, pos.getY() + 0.5D + motionY / 2D, pos.getZ() + 0.5D + motionZ / 2D, motionX * speed, motionY * speed, motionZ * speed), world);
            int dispersedAmount = (int) (getPressure() * PneumaticValues.AIR_LEAK_FACTOR) + 20;
            if (dispersedAmount > getAir()) dispersedAmount = getAir();
            onAirDispersion(ownerTE, dir, -dispersedAmount);
            addAir(-dispersedAmount);
        }
    }

    private LazyOptional<IAirHandler> getNeighbourAirHandler(TileEntity ownerTE, Direction dir) {
        final int idx = dir.getIndex();
        if (neighbourAirHandlers.get(idx) == null) {
            TileEntity te1 = ownerTE.getWorld().getTileEntity(ownerTE.getPos().offset(dir));
            if (te1 != null) {
                neighbourAirHandlers.set(idx, te1.getCapability(CapabilityAirHandler.AIR_HANDLER_CAPABILITY));
                neighbourAirHandlers.get(idx).addListener(l -> neighbourAirHandlers.set(idx, null));
            } else {
                neighbourAirHandlers.set(idx, LazyOptional.empty());
            }
        }
        return neighbourAirHandlers.get(idx);
    }

    private void disperseAir(TileEntity ownerTE) {
        // 1. build a list of all neighbouring and otherwise connected air handlers with a lower pressure than us
        List<ConnectedAirHandler> neighbours = new ArrayList<>();
        for (Direction d : Direction.VALUES) {
            getNeighbourAirHandler(ownerTE, d).ifPresent(h -> {
                if (h.getPressure() < this.getPressure()) neighbours.add(new ConnectedAirHandler(d, h));
            });
        }
        neighbours.addAll(addExtraConnectedHandlers(ownerTE).stream()
                .filter(airHandler -> getPressure() < airHandler.getPressure())
                .map(ConnectedAirHandler::new)
                .collect(Collectors.toList()));


        // 2. get the total volume and air amount in this and all connected handlers
        int totalVolume = this.getVolume();
        int totalAir = this.getAir();
        for (ConnectedAirHandler neighbour : neighbours) {
            totalVolume += neighbour.airHandler.getVolume();
            totalAir += neighbour.airHandler.getAir();
        }

        // 3. figure out how much air will be dispersed to each neighbour
        for (ConnectedAirHandler neighbour: neighbours) {
            int totalMachineAir = (int) ((long) totalAir * neighbour.airHandler.getVolume() / totalVolume);
            neighbour.maxDispersion = getMaxDispersion(ownerTE, neighbour.direction);
            neighbour.toDisperse = totalMachineAir - neighbour.airHandler.getAir();
        }

        // 4. Any air that wants to go to a neighbour, but can't (because of regulator module etc.) has to return the
        //    excess, which will be divided amongst all the other neighbours
        adjustForDispersionLimits(neighbours);

        // 5. finally, actually disperse the air
        for (ConnectedAirHandler neighbour : neighbours) {
            onAirDispersion(ownerTE, neighbour.direction, neighbour.toDisperse);
            neighbour.airHandler.addAir(neighbour.toDisperse);
            addAir(-neighbour.toDisperse);
        }
    }

    private void adjustForDispersionLimits(List<ConnectedAirHandler> neighbours) {
        int excessAir = 0;
        int receivers = neighbours.size();
        for (ConnectedAirHandler c : neighbours) {
            if (c.toDisperse > c.maxDispersion) {
                excessAir += c.toDisperse - c.maxDispersion;
                c.toDisperse = c.maxDispersion;
                receivers--;
            }
        }

        while (excessAir >= receivers && receivers > 0) {
            // try to give every receiver an equal part of the to-be-divided air.
            int dividedValue = excessAir / receivers;
            for (ConnectedAirHandler c : neighbours) {
                int maxTransfer = c.maxDispersion - c.toDisperse;
                if (maxTransfer > 0) {
                    if (maxTransfer <= dividedValue) {
                        receivers--; //next step this receiver won't be able to receive any air.
                    }
                    int transferred = Math.min(dividedValue, maxTransfer); //cap it at the max it can have.
                    c.toDisperse += transferred;
                    excessAir -= transferred;
                } else {
                    receivers--;
                }
            }
        }
    }

    private List<IAirHandler> addExtraConnectedHandlers(TileEntity ownerTE) {
//        if (ownerTE instanceof IAirListener) {
//            return ((IAirListener) ownerTE).addConnectedPneumatics(new ArrayList<IAirHandler>());
//        }
        return Collections.emptyList();
    }

    private void onAirDispersion(TileEntity ownerTE, Direction dir, int airDispersed) {
//        if (ownerTE instanceof IAirListener) {
//            ((IAirListener) ownerTE).onAirDispersion(this, dir, airDispersed);
//        }
    }

    private int getMaxDispersion(TileEntity ownerTE, Direction dir) {
        if (ownerTE instanceof IAirListener) {
//            return ((IAirListener) ownerTE).getMaxDispersion(this, dir);
            return Integer.MAX_VALUE;
        } else {
            return Integer.MAX_VALUE;
        }
    }

    private class ConnectedAirHandler {
        final Direction direction; // may be null
        final IAirHandler airHandler;
        int maxDispersion;
        int toDisperse;

        ConnectedAirHandler(Direction direction, IAirHandler airHandler) {
            this.direction = direction;
            this.airHandler = airHandler;
        }

        ConnectedAirHandler(IAirHandler airHandler) {
            this(null, airHandler);
        }
    }
}
