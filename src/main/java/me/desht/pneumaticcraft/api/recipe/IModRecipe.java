package me.desht.pneumaticcraft.api.recipe;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public interface IModRecipe {
    ResourceLocation getId();

    void write(PacketBuffer buf);
}
