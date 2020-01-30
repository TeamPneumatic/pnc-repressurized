package me.desht.pneumaticcraft.common.heat;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TemperatureData implements INBTSerializable<CompoundNBT> {
    private final Double[] temp = new Double[7];

    private boolean isMultisided = true;

    public static TemperatureData fromNBT(CompoundNBT nbt) {
        TemperatureData data = new TemperatureData();
        data.deserializeNBT(nbt);
        return data;
    }

    private TemperatureData() {}

    public TemperatureData(ICapabilityProvider provider) {
        Arrays.fill(temp, null);

        Set<IHeatExchangerLogic> heatExchangers = new HashSet<>();
        for (Direction face : Direction.VALUES) {
            boolean done = provider.getCapability(PNCCapabilities.HEAT_EXCHANGER_CAPABILITY, face).map(h -> {
                if (heatExchangers.contains(h)) {
                    isMultisided = false;
                    return true;
                } else {
                    heatExchangers.add(h);
                    return false;
                }
            }).orElse(false);
            if (done) break;
        }

        if (isMultisided) {
            for (Direction face : Direction.VALUES) {
                provider.getCapability(PNCCapabilities.HEAT_EXCHANGER_CAPABILITY, face)
                        .ifPresent(h -> temp[face.ordinal()] = h.getTemperature());
            }
        } else {
            provider.getCapability(PNCCapabilities.HEAT_EXCHANGER_CAPABILITY)
                    .ifPresent(h -> temp[6] = h.getTemperature());
        }
    }

    public boolean isMultisided() {
        return isMultisided;
    }

    public double getTemperature(Direction face) {
        return face == null ? temp[6] : temp[face.ordinal()];
    }

    public boolean hasData(Direction face) {
        return face == null ? temp[6] != null : temp[face.ordinal()] != null;
    }

    public CompoundNBT toNBT() {
        CompoundNBT nbt = new CompoundNBT();
        if (isMultisided()) {
            ListNBT tagList = new ListNBT();
            for (Direction face : Direction.VALUES) {
                if (temp[face.ordinal()] != null) {
                    CompoundNBT heatTag = new CompoundNBT();
                    heatTag.putByte("side", (byte) face.ordinal());
                    heatTag.putInt("temp", (int) getTemperature(face));
                    tagList.add(heatTag);
                }
            }
            nbt.put("heat", tagList);
        } else {
            nbt.putInt("temp", (int) getTemperature(null));
        }
        return nbt;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        if (isMultisided()) {
            ListNBT tagList = new ListNBT();
            for (Direction face : Direction.VALUES) {
                CompoundNBT heatTag = new CompoundNBT();
                heatTag.putByte("side", (byte) face.ordinal());
                heatTag.putInt("temp", (int) getTemperature(face));
                tagList.add(heatTag);
            }
            nbt.put("heat", tagList);
        } else {
            nbt.putInt("temp", (int) getTemperature(null));
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (nbt.contains("heat")) {
            isMultisided = true;
            ListNBT tagList = nbt.getList("heat", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++) {
                CompoundNBT heatTag = tagList.getCompound(i);
                temp[heatTag.getByte("side")] = (double) heatTag.getInt("temp");
            }
        } else {
            isMultisided = false;
            temp[6] = (double) nbt.getInt("temp");
        }
    }
}
