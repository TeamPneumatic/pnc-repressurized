package me.desht.pneumaticcraft.common.fluid;

import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.item.ItemDye;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

public class FluidPlastic extends FluidPneumaticCraft {

    public FluidPlastic(String name) {
        super(name);
        setTemperature(PneumaticValues.PLASTIC_MIXER_MELTING_TEMP);
    }

    @Override
    public int getColor(FluidStack plastic) {
        return getColorS(plastic);
    }

    public static int getColorS(FluidStack plastic) {
        return plastic.tag != null ? plastic.tag.getInteger("color") : 0xFFFFFF;
    }

    private static int[] getColor3(FluidStack plastic) {
        int color = getColorS(plastic);
        return new int[]{color >> 16, color >> 8 & 255, color & 255};
    }

    @Override
    public int getTemperature(FluidStack plastic) {
        return getTemperatureS(plastic);
    }

    public static int getTemperatureS(FluidStack plastic) {
        return PneumaticValues.PLASTIC_MIXER_MELTING_TEMP + 1;//plastic.tag != null ? plastic.tag.getInteger("temperature") : 295;
    }

    public static void addDye(FluidStack plastic, int dyeMetadata) {
        if (!Fluids.areFluidsEqual(plastic.getFluid(), Fluids.PLASTIC))
            throw new IllegalArgumentException("Given fluid stack isn't mixable! " + plastic);
        int dyeColor = ItemDye.DYE_COLORS[dyeMetadata];
        int[] dyeColors = new int[]{dyeColor >> 16, dyeColor >> 8 & 255, dyeColor & 255};
        int[] plasticColor = getColor3(plastic);
        double ratio = PneumaticValues.PLASTIC_MIX_RATIO / (PneumaticValues.PLASTIC_MIX_RATIO * (plastic.amount / 1000D));
        for (int i = 0; i < 3; i++) {
            plasticColor[i] = (int) (ratio * dyeColors[i] + (1 - ratio) * plasticColor[i]);
        }
        if (plastic.tag == null) plastic.tag = new NBTTagCompound();
        plastic.tag.setInteger("color", (plasticColor[0] << 16) + (plasticColor[1] << 8) + plasticColor[2]);
    }

    public static FluidStack mixFluid(FluidStack plastic, FluidStack otherPlastic) {
        if (plastic == null) return otherPlastic;
        if (otherPlastic == null) return plastic;
        int[] otherColor = getColor3(otherPlastic);
        int[] color = getColor3(plastic);
        double ratio = (double) plastic.amount / (plastic.amount + otherPlastic.amount);
        int[] newColor = new int[3];
        for (int i = 0; i < 3; i++) {
            newColor[i] = (int) (ratio * color[i] + (1 - ratio) * otherColor[i]);
        }
        NBTTagCompound newTag = new NBTTagCompound();
        newTag.setInteger("color", (newColor[0] << 16) + (newColor[1] << 8) + newColor[2]);
        // newTag.setInteger("temperature", (int)(ratio * getTemperatureS(plastic) + (1 - ratio) * getTemperatureS(otherPlastic)));
        return new FluidStack(Fluids.PLASTIC, plastic.amount + otherPlastic.amount, newTag);
    }
}
