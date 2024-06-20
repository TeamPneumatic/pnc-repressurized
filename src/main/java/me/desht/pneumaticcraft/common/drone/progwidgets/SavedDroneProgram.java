package me.desht.pneumaticcraft.common.drone.progwidgets;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SavedDroneProgram {
    public static final Codec<SavedDroneProgram> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            CompoundTag.CODEC.listOf().fieldOf("widget_nbt").forGetter(prog -> prog.widgetNBT)
    ).apply(builder, SavedDroneProgram::new));

    public static final StreamCodec<FriendlyByteBuf, SavedDroneProgram> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG.apply(ByteBufCodecs.list()), prog -> prog.widgetNBT,
            SavedDroneProgram::new
    );

    public static final SavedDroneProgram EMPTY = new SavedDroneProgram(List.of());

    private final List<CompoundTag> widgetNBT;
    private final int hashCode;

    public static SavedDroneProgram create(List<IProgWidget> widgets) {
        ImmutableList.Builder<CompoundTag> builder = ImmutableList.builder();
        widgets.forEach(w -> ProgWidget.CODEC.encodeStart(NbtOps.INSTANCE, w).ifSuccess(tag -> {
            if (tag instanceof CompoundTag c) builder.add(c);
        }));
        return new SavedDroneProgram(builder.build());
    }

    private SavedDroneProgram(List<CompoundTag> widgetNBT) {
        this.widgetNBT = widgetNBT;

        hashCode = Objects.hash(widgetNBT);
    }

    public List<IProgWidget> buildProgram() {
        return Util.make(new ArrayList<>(), list -> widgetNBT.forEach(tag -> ProgWidget.CODEC.parse(NbtOps.INSTANCE, tag).ifSuccess(list::add)));
    }

    public static List<IProgWidget> forItemStack(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.SAVED_DRONE_PROGRAM, EMPTY).buildProgram();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SavedDroneProgram that = (SavedDroneProgram) o;
        return Objects.equals(widgetNBT, that.widgetNBT);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
