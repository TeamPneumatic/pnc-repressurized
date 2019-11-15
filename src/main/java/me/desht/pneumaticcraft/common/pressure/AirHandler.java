package me.desht.pneumaticcraft.common.pressure;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IAirListener;
import me.desht.pneumaticcraft.api.tileentity.IPneumaticMachine;
import me.desht.pneumaticcraft.client.particle.AirParticleData;
import me.desht.pneumaticcraft.common.block.BlockPneumaticCraft;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.network.PacketSpawnParticle;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import me.desht.pneumaticcraft.common.tileentity.TileEntityTickableBase;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.TileEntityCache;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class AirHandler implements IAirHandler {
    private float maxPressure;
    @GuiSynced
    private int volume;
    private int defaultVolume;
    private final float dangerPressure;
    private final float criticalPressure;
    @GuiSynced
    private int air; // Pressure = air / volume
    private int soundCounter;
    private final Set<IAirHandler> specialConnectedHandlers = new HashSet<>();
    private TileEntityCache[] tileCache;

    private TileEntityTickableBase.UpgradeCache upgradeCache;
    private IAirListener airListener;
    private IPneumaticMachine parentPneumatic;
    private World world;
    private BlockPos pos;

    public AirHandler() {
        this(PneumaticValues.DANGER_PRESSURE_TIER_ONE, PneumaticValues.MAX_PRESSURE_TIER_ONE, 3000);
    }

    AirHandler(float dangerPressure, float criticalPressure, int volume) {
        Validate.isTrue(volume > 0, "Volume can't be lower than or equal to 0!");
        this.dangerPressure = dangerPressure;
        this.criticalPressure = criticalPressure;
        this.maxPressure = dangerPressure + (criticalPressure - dangerPressure) * (float) Math.random();
        this.volume = volume;
        this.defaultVolume = volume;
    }

    public TileEntityCache[] getTileCache() {
        if (tileCache == null) tileCache = TileEntityCache.getDefaultCache(getWorld(), getPos());
        return tileCache;
    }

    @Override
    public void createConnection(@Nonnull IAirHandler otherHandler) {
        if (specialConnectedHandlers.add(otherHandler)) {
            otherHandler.createConnection(this);
        }
    }

    @Override
    public void removeConnection(@Nonnull IAirHandler otherHandler) {
        if (specialConnectedHandlers.remove(otherHandler)) {
            otherHandler.removeConnection(this);
        }
    }

    @Override
    public void printManometerMessage(PlayerEntity player, List<ITextComponent> curInfo) {
        curInfo.add(xlate("gui.tooltip.pressure", PneumaticCraftUtils.roundNumberTo(getPressure(), 1)).applyTextStyle(TextFormatting.GREEN));
    }

    public void tick() {
        if (!getWorld().isRemote) {
            updateVolume();

            if (getUpgrades(EnumUpgrade.SECURITY) > 0) {
                doSecurityAirChecks();
            }

            if (getPressure() > maxPressure) {
                getWorld().createExplosion(null, getPos().getX() + 0.5D, getPos().getY() + 0.5D, getPos().getZ() + 0.5D, 1.0F, Explosion.Mode.BREAK);
                getWorld().removeBlock(getPos(), false);
            } else {
                disperseAir();
            }
        }

        if (soundCounter > 0) soundCounter--;
    }

    private void updateVolume() {
        setVolume(defaultVolume + getVolumeFromUpgrades());
    }

    private void doSecurityAirChecks() {
        if (getPressure() >= dangerPressure - 0.1) {
            airLeak(Direction.UP);
        }

        // Remove any remaining air
        int excessAir = getAir() - (int) (getVolume() * (dangerPressure - 0.1));
        if (excessAir > 0) {
            addAir(-excessAir);
            onAirDispersion(null, -excessAir);
        }
    }

    /**
     * Sets the volume of this TE's air tank. When the volume decreases the pressure will remain the same, meaning air will
     * be lost. When the volume increases, the air remains the same meaning the pressure will drop.
     * Used in the Volume Upgrade calculations.
     *
     * @param newVolume the new volume
     */
    public void setVolume(int newVolume) {
        Validate.isTrue(newVolume > 0, "Volume can't be lower or equal than 0!");

        if (newVolume < volume) {
            air = (int) (air * (float) newVolume / volume); // lose air when we decrease in volume.
        }
        volume = newVolume;
    }

    private void onAirDispersion(Direction dir, int airAdded) {
        if (airListener != null) airListener.onAirDispersion(this, dir, airAdded);
    }

    private int getMaxDispersion(Direction dir) {
        return airListener != null ? airListener.getMaxDispersion(this, dir) : Integer.MAX_VALUE;
    }

    private int getUpgrades(EnumUpgrade upgrade) {
        return upgradeCache == null ? 0 : upgradeCache.getUpgrades(upgrade);
    }

    private int getVolumeFromUpgrades() {
        return getUpgrades(EnumUpgrade.VOLUME) * PneumaticValues.VOLUME_VOLUME_UPGRADE;
    }

    /**
     * Method invoked every update tick which is used to handle air dispersion. It retrieves the pneumatics connecting
     * with this TE, and pushes air to those with a lower pressure than this one.
     */
    private void disperseAir() {
        if (getWorld().isRemote) return;
        disperseAir(getConnectedPneumatics());
    }

    private void disperseAir(List<Pair<Direction, IAirHandler>> teList) {

        boolean shouldRepeat;
        List<Pair<Integer, Integer>> dispersion = new ArrayList<>();
        do {
            shouldRepeat = false;
            //Add up every volume and air.
            int totalVolume = getVolume();
            int totalAir = air;
            for (Pair<Direction, IAirHandler> entry : teList) {
                IAirHandler airHandler = entry.getValue();
                totalVolume += airHandler.getVolume();
                totalAir += airHandler.getAir();
            }
            //Only go push based, ignore any machines that have a higher pressure than this block.
            Iterator<Pair<Direction, IAirHandler>> iterator = teList.iterator();
            while (iterator.hasNext()) {
                Pair<Direction, IAirHandler> entry = iterator.next();
                IAirHandler airHandler = entry.getValue();
                int totalMachineAir = (int) ((long) totalAir * airHandler.getVolume() / totalVolume);//Calculate the total air the machine is going to get.
                int airDispersed = totalMachineAir - airHandler.getAir();
                if (airDispersed < 0) {
                    iterator.remove();
                    shouldRepeat = true;
                    dispersion.clear();
                    break;
                } else {
                    dispersion.add(new MutablePair<>(getMaxDispersion(entry.getKey()), airDispersed));
                }
            }
        } while (shouldRepeat);

        int toBeDivided = 0;
        int receivers = dispersion.size();
        for (Pair<Integer, Integer> disp : dispersion) {
            if (disp.getValue() > disp.getKey()) {
                toBeDivided += disp.getValue() - disp.getKey();//Any air that wants to go to a neighbor, but can't (because of regulator module) gives back its air.
                disp.setValue(disp.getKey());
                receivers--;
            }
        }

        while (toBeDivided >= receivers && receivers > 0) {
            int dividedValue = toBeDivided / receivers; //try to give every receiver an equal part of the to be divided air.
            for (Pair<Integer, Integer> disp : dispersion) {
                int maxTransfer = disp.getKey() - disp.getValue();
                if (maxTransfer > 0) {
                    if (maxTransfer <= dividedValue) {
                        receivers--;//next step this receiver won't be able to receive any air.
                    }
                    int transfered = Math.min(dividedValue, maxTransfer);//cap it at the max it can have.
                    disp.setValue(disp.getValue() + transfered);
                    toBeDivided -= transfered;
                } else {
                    receivers--;
                }
            }
        }

        for (int i = 0; i < teList.size(); i++) {
            IAirHandler neighbor = teList.get(i).getValue();
            int transferedAir = dispersion.get(i).getValue();

            onAirDispersion(teList.get(i).getKey(), transferedAir);
            neighbor.addAir(transferedAir);
            addAir(-transferedAir);
        }
    }

    /**
     * Adds air to the tank of the given side of this TE.
     *
     * @param amount amount of air (in mL) to add
     */
    @Override
    public void addAir(int amount) {
        air = Math.max(air + amount, -volume);  // floor at -1 bar otherwise negative air is reported
    }

    @Override
    public void setDefaultVolume(int defaultVolume) {
        this.defaultVolume = defaultVolume;
    }

    @Override
    public float getPressure() {
        return (float) air / volume;
    }

    @Override
    public void deserializeNBT(CompoundNBT tag) {
        air = tag.getInt("air");
        maxPressure = tag.getFloat("maxPressure");
        volume = tag.getInt("volume");
        if (volume == 0 && PneumaticCraftRepressurized.proxy.getClientWorld() == null) {
            // only warn about a zero volume on the server side
            Log.error("Volume was 0! Assigning default");
            volume = defaultVolume;
        }
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT pneumaticTag = new CompoundNBT();
        pneumaticTag.putInt("air", air);
        pneumaticTag.putInt("volume", volume);
        pneumaticTag.putFloat("maxPressure", maxPressure);
        return pneumaticTag;
    }

    @Override
    public void validate(TileEntity parent) {
        upgradeCache = parent instanceof TileEntityBase ? ((TileEntityBase) parent).getUpgradeCache() : null;
        airListener = parent instanceof IAirListener ? (IAirListener) parent : null;
        parentPneumatic = (IPneumaticMachine) parent;
        setWorld(parent.getWorld());
        setPos(parent.getPos());
    }

    @Override
    public void setPneumaticMachine(IPneumaticMachine machine) {
        parentPneumatic = machine;
    }

    @Override
    public void setAirListener(IAirListener airListener) {
        this.airListener = airListener;
    }

    @Override
    public void airLeak(Direction side) {
        if (getWorld().isRemote || Math.abs(getPressure()) < 0.01F) return;
        double motionX = side.getXOffset();
        double motionY = side.getYOffset();
        double motionZ = side.getZOffset();
        if (soundCounter <= 0) {
            float pitch = MathHelper.clamp(1.0f + ((getPressure() - 3) / 10), 0.8f, 1.2f);
            soundCounter = (int) (20 / pitch);
            NetworkHandler.sendToAllAround(new PacketPlaySound(ModSounds.LEAKING_GAS, SoundCategory.BLOCKS, getPos().getX(), getPos().getY(), getPos().getZ(), 0.1F, pitch, true), getWorld());
        }

        if (getPressure() < 0) {
            double speed = getPressure() * 0.1F - 0.1F;
            NetworkHandler.sendToAllAround(new PacketSpawnParticle(AirParticleData.DENSE, getPos().getX() + 0.5D + motionX / 2D, getPos().getY() + 0.5D + motionY / 2D, getPos().getZ() + 0.5D + motionZ / 2D, motionX * speed, motionY * speed, motionZ * speed), getWorld());
            int dispersedAmount = -(int) (getPressure() * PneumaticValues.AIR_LEAK_FACTOR) + 20;
            if (getAir() > dispersedAmount) dispersedAmount = -getAir();
            onAirDispersion(side, dispersedAmount);
            addAir(dispersedAmount);
        } else {
            double speed = getPressure() * 0.1F + 0.1F;
            NetworkHandler.sendToAllAround(new PacketSpawnParticle(AirParticleData.DENSE, getPos().getX() + 0.5D + motionX / 2D, getPos().getY() + 0.5D + motionY / 2D, getPos().getZ() + 0.5D + motionZ / 2D, motionX * speed, motionY * speed, motionZ * speed), getWorld());
            int dispersedAmount = (int) (getPressure() * PneumaticValues.AIR_LEAK_FACTOR) + 20;
            if (dispersedAmount > getAir()) dispersedAmount = getAir();
            onAirDispersion(side, -dispersedAmount);
            addAir(-dispersedAmount);
        }
    }

    /**
     * Retrieves a list of all the connecting pneumatics. It takes sides in account.
     *
     * @return a list of face->air-handler pairs
     */
    @Override
    public List<Pair<Direction, IAirHandler>> getConnectedPneumatics() {
        List<Pair<Direction, IAirHandler>> teList = new ArrayList<>();
        for (IAirHandler specialConnection : specialConnectedHandlers) {
            teList.add(new ImmutablePair<>(null, specialConnection));
        }
        for (Direction direction : Direction.VALUES) {
            TileEntity te = getTileCache()[direction.ordinal()].getTileEntity();
            IPneumaticMachine machine = IPneumaticMachine.getMachine(te);
            if (machine != null && parentPneumatic.getAirHandler(direction) == this && machine.getAirHandler(direction.getOpposite()) != null) {
                teList.add(new ImmutablePair<>(direction, machine.getAirHandler(direction.getOpposite())));
            }
        }
        if (airListener != null) airListener.addConnectedPneumatics(teList);
        return teList;
    }

    @Override
    public void onNeighborChange() {
        for (TileEntityCache cache : getTileCache()) {
            cache.update();
        }
    }

    @Override
    public int getVolume() {
        return volume;
    }

    @Override
    public float getMaxPressure() {
        return maxPressure;
    }
    
    @Override
    public float getDangerPressure(){
        return dangerPressure;
    }
    
    @Override
    public float getCriticalPressure(){
        return criticalPressure;
    }

    @Override
    public int getAir() {
        return air;
    }

    /**
     * Sets the amount of air in this handler.  Currently only used for server->client sync'ing.
     *
     * @param air air amount in mL
     */
    public void setAir(int air) {
        this.air = air;
    }

    /**
     * Set the air pressure directly.  Currently only used by the Creative Compressor.
     *
     * @param pressure the pressure, in bar
     */
    public void setPressure(float pressure) {
        air = (int) (pressure * volume);
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public void setWorld(World world) {
        this.world = world;
    }

    @Override
    public BlockPos getPos() {
        return pos;
    }

    @Override
    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    public static BlockState getBlockConnectionState(BlockState state, IAirHandler handler) {
        boolean[] conn = new boolean[6];
        for (Pair<Direction, IAirHandler> entry : handler.getConnectedPneumatics()) {
            conn[entry.getKey().ordinal()] = true;
        }
        for (int i = 0; i < 6; i++) {
            state = state.with(BlockPneumaticCraft.CONNECTION_PROPERTIES[i], conn[i]);
        }
        return state;
    }
}
