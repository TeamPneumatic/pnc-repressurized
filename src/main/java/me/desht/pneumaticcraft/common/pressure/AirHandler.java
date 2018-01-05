package me.desht.pneumaticcraft.common.pressure;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IAirListener;
import me.desht.pneumaticcraft.api.tileentity.IPneumaticMachine;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.network.PacketSpawnParticle;
import me.desht.pneumaticcraft.common.thirdparty.ModInteractionUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import me.desht.pneumaticcraft.common.tileentity.TileEntityTickableBase;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.TileEntityCache;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Sounds;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;

public class AirHandler implements IAirHandler {
    private float maxPressure;
    @GuiSynced
    private int volume;
    private int defaultVolume;
    private final float dangerPressure;
    private final float criticalPressure;
    @GuiSynced
    private int air; //Pressure = air / volume
    private int soundCounter;
    private final Set<IAirHandler> specialConnectedHandlers = new HashSet<>();
    private TileEntityCache[] tileCache;

    private TileEntityTickableBase.UpgradeCache upgradeCache;
    private IAirListener airListener;
    private IPneumaticMachine parentPneumatic;
    private World world;
    private BlockPos pos;

    AirHandler(float dangerPressure, float criticalPressure, int volume) {
        Validate.isTrue(volume > 0, "Volume can't be lower than or equal to 0!");
        this.dangerPressure = dangerPressure;
        this.criticalPressure = criticalPressure;
        maxPressure = dangerPressure + (criticalPressure - dangerPressure) * (float) Math.random();
        this.volume = volume;
        defaultVolume = volume;
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
    public void printManometerMessage(EntityPlayer player, List<String> curInfo) {
        curInfo.add(TextFormatting.GREEN + "Current pressure: " + PneumaticCraftUtils.roundNumberTo(getPressure(), 1) + " bar.");
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {
            updateVolume();

            if (getUpgrades(EnumUpgrade.SECURITY) > 0) {
                doSecurityAirChecks();
            }

            if (getPressure() > maxPressure) {
                getWorld().createExplosion(null, getPos().getX() + 0.5D, getPos().getY() + 0.5D, getPos().getZ() + 0.5D, 1.0F, true);
                getWorld().setBlockToAir(getPos());
            } else {
                disperseAir();
            }
        }

        if (soundCounter > 0) soundCounter--;
    }

    private void updateVolume() {
        int upgradeVolume = getVolumeFromUpgrades();
        setVolume(defaultVolume + upgradeVolume);
    }

    private void doSecurityAirChecks() {
        if (getPressure() >= dangerPressure - 0.1) {
            airLeak(EnumFacing.DOWN);
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

        if (newVolume < volume) air = (int) (air * (float) newVolume / volume); // lose air when we decrease in volume.
        volume = newVolume;
    }

    private void onAirDispersion(EnumFacing dir, int airAdded) {
        if (airListener != null) airListener.onAirDispersion(this, dir, airAdded);
    }

    private int getMaxDispersion(EnumFacing dir) {
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

    private void disperseAir(List<Pair<EnumFacing, IAirHandler>> teList) {

        boolean shouldRepeat;
        List<Pair<Integer, Integer>> dispersion = new ArrayList<>();
        do {
            shouldRepeat = false;
            //Add up every volume and air.
            int totalVolume = getVolume();
            int totalAir = air;
            for (Pair<EnumFacing, IAirHandler> entry : teList) {
                IAirHandler airHandler = entry.getValue();
                totalVolume += airHandler.getVolume();
                totalAir += airHandler.getAir();
            }
            //Only go push based, ignore any machines that have a higher pressure than this block.
            Iterator<Pair<EnumFacing, IAirHandler>> iterator = teList.iterator();
            while (iterator.hasNext()) {
                Pair<EnumFacing, IAirHandler> entry = iterator.next();
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
        air += amount;
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
    public void readFromNBT(NBTTagCompound tag) {
        NBTTagCompound pneumaticTag = tag.getCompoundTag("pneumatic");
        air = pneumaticTag.getInteger("air");
        maxPressure = pneumaticTag.getFloat("maxPressure");
        volume = pneumaticTag.getInteger("volume");
        if (volume == 0 && PneumaticCraftRepressurized.proxy.getClientWorld() == null) {
            // only warn about a zero volume on the server side
            Log.error("Volume was 0! Assigning default");
            volume = defaultVolume;
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        NBTTagCompound pneumaticTag = new NBTTagCompound();
        pneumaticTag.setInteger("air", air);
        pneumaticTag.setInteger("volume", volume);
        pneumaticTag.setFloat("maxPressure", maxPressure);
        tag.setTag("pneumatic", pneumaticTag);
    }

    @Override
    public void validate(TileEntity parent) {
        upgradeCache = parent instanceof TileEntityTickableBase ? ((TileEntityBase) parent).getUpgradeCache() : null;
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
    public void airLeak(EnumFacing side) {
        if (getWorld().isRemote || Math.abs(getPressure()) < 0.01F) return;
        double motionX = side.getFrontOffsetX();
        double motionY = side.getFrontOffsetY();
        double motionZ = side.getFrontOffsetZ();
        if (soundCounter <= 0) {
            soundCounter = 20;
            NetworkHandler.sendToAllAround(new PacketPlaySound(Sounds.LEAKING_GAS_SOUND, SoundCategory.BLOCKS, getPos().getX(), getPos().getY(), getPos().getZ(), 0.1F, 1.0F, true), getWorld());
        }

        if (getPressure() < 0) {
            double speed = getPressure() * 0.1F - 0.1F;
            NetworkHandler.sendToAllAround(new PacketSpawnParticle(EnumParticleTypes.SMOKE_NORMAL, getPos().getX() + 0.5D + motionX / 2D, getPos().getY() + 0.5D + motionY / 2D, getPos().getZ() + 0.5D + motionZ / 2D, motionX * speed, motionY * speed, motionZ * speed), getWorld());

            int dispersedAmount = -(int) (getPressure() * PneumaticValues.AIR_LEAK_FACTOR) + 20;
            if (getAir() > dispersedAmount) dispersedAmount = -getAir();
            onAirDispersion(side, dispersedAmount);
            addAir(dispersedAmount);
        } else {
            double speed = getPressure() * 0.1F + 0.1F;
            // if(DateEventHandler.isEvent()) {
            //DateEventHandler.spawnFirework(getWorld(), getPos().getX() + 0.5D + motionX / 2D, getPos().getY() + 0.5D + motionY / 2D, getPos().getZ() + 0.5D + motionZ / 2D);
            // } else {
            NetworkHandler.sendToAllAround(new PacketSpawnParticle(EnumParticleTypes.SMOKE_NORMAL, getPos().getX() + 0.5D + motionX / 2D, getPos().getY() + 0.5D + motionY / 2D, getPos().getZ() + 0.5D + motionZ / 2D, motionX * speed, motionY * speed, motionZ * speed), getWorld());
            // }

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
    public List<Pair<EnumFacing, IAirHandler>> getConnectedPneumatics() {
        List<Pair<EnumFacing, IAirHandler>> teList = new ArrayList<>();
        for (IAirHandler specialConnection : specialConnectedHandlers) {
            teList.add(new ImmutablePair<>(null, specialConnection));
        }
        for (EnumFacing direction : EnumFacing.VALUES) {
            TileEntity te = getTileCache()[direction.ordinal()].getTileEntity();
            IPneumaticMachine machine = ModInteractionUtils.getInstance().getMachine(te);
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
    public void setUpgradeSlots(int... upgradeSlots) {
        // does nothing - deprecated method
    }

    @Override
    public int[] getUpgradeSlots() {
        return new int[0];
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

    @Override
    public Set<Item> getApplicableUpgrades() {
        Set<Item> upgrades = new HashSet<>(2);
        upgrades.add(Itemss.upgrades.get(EnumUpgrade.VOLUME));
        upgrades.add(Itemss.upgrades.get(EnumUpgrade.SECURITY));
        return upgrades;
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }
}
