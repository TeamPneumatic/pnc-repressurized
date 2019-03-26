package me.desht.pneumaticcraft.common.heat;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.Map;

public class HeatExtractionTracker extends WorldSavedData {
    private static final String DATA_NAME = "PneumaticCraftHeatExtraction";

    private final Map<BlockPos, Double> extracted = new HashMap<>();

    private HeatExtractionTracker() {
        super(DATA_NAME);
    }

    public HeatExtractionTracker(String name) {
        super(name);
    }

    public static HeatExtractionTracker getInstance(World world) {
        MapStorage storage = world.getPerWorldStorage();
        HeatExtractionTracker tracker = (HeatExtractionTracker) storage.getOrLoadData(HeatExtractionTracker.class, DATA_NAME);
        if (tracker == null) {
            tracker = new HeatExtractionTracker();
            storage.setData(DATA_NAME, tracker);
        }
        return tracker;
    }

    public double getHeatExtracted(BlockPos pos) {
        return extracted.getOrDefault(pos, 0.0);
    }

    public void extractHeat(BlockPos pos, double heat) {
        double newAmount = getHeatExtracted(pos) + heat;
        if (Math.abs(newAmount) < 0.000001) {
            extracted.remove(pos);
        } else {
            extracted.put(pos, newAmount);
        }
        markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        extracted.clear();

        NBTTagList list = nbt.getTagList("extracted", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound sub = list.getCompoundTagAt(i);
            BlockPos pos = new BlockPos(sub.getInteger("x"), sub.getInteger("y"), sub.getInteger("z"));
            extracted.put(pos, sub.getDouble("heat"));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        for (Map.Entry<BlockPos, Double> entry : extracted.entrySet()) {
            NBTTagCompound sub = new NBTTagCompound();
            sub.setInteger("x", entry.getKey().getX());
            sub.setInteger("y", entry.getKey().getY());
            sub.setInteger("z", entry.getKey().getZ());
            sub.setDouble("heat", entry.getValue());
            list.appendTag(sub);
        }
        compound.setTag("extracted", list);
        return compound;
    }
}
