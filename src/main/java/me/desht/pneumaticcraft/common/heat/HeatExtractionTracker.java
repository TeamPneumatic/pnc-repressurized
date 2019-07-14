package me.desht.pneumaticcraft.common.heat;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorld;
import net.minecraft.world.World;
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

    public static HeatExtractionTracker getInstance(World world) {
        return ((ServerWorld) world).getSavedData().getOrCreate(HeatExtractionTracker::new, DATA_NAME);
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
    public void read(CompoundNBT nbt) {
        extracted.clear();

        ListNBT list = nbt.getList("extracted", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT sub = list.getCompound(i);
            BlockPos pos = new BlockPos(sub.getInt("x"), sub.getInt("y"), sub.getInt("z"));
            extracted.put(pos, sub.getDouble("heat"));
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        ListNBT list = new ListNBT();
        for (Map.Entry<BlockPos, Double> entry : extracted.entrySet()) {
            CompoundNBT sub = new CompoundNBT();
            sub.putInt("x", entry.getKey().getX());
            sub.putInt("y", entry.getKey().getY());
            sub.putInt("z", entry.getKey().getZ());
            sub.putDouble("heat", entry.getValue());
            list.add(sub);
        }
        compound.put("extracted", list);
        return compound;
    }
}
