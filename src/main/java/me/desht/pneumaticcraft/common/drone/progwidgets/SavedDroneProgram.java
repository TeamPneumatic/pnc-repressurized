package me.desht.pneumaticcraft.common.drone.progwidgets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * An immutable collection of progwidgets, suitable for storing as an item data component.
 */
public class SavedDroneProgram {
    public static final Codec<SavedDroneProgram> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ProgWidget.CODEC.listOf().fieldOf("widget_nbt").forGetter(prog -> prog.widgets)
    ).apply(builder, SavedDroneProgram::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SavedDroneProgram> STREAM_CODEC = StreamCodec.composite(
            ProgWidget.STREAM_CODEC.apply(ByteBufCodecs.list()), prog -> prog.widgets,
            SavedDroneProgram::new
    );

    public static final SavedDroneProgram EMPTY = new SavedDroneProgram(List.of());

    private final List<IProgWidget> widgets;
    private final int hashCode;

    private SavedDroneProgram(List<IProgWidget> widgets) {
        this.widgets = widgets;

        hashCode = Objects.hash(widgets);
    }

    public static SavedDroneProgram fromWidgets(List<IProgWidget> widgets) {
        return new SavedDroneProgram(deepCopy(widgets));
    }

    public static SavedDroneProgram fromItemStack(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.SAVED_DRONE_PROGRAM, EMPTY);
    }

    public static void writeToItem(ItemStack stack, List<IProgWidget> widgets) {
        stack.set(ModDataComponents.SAVED_DRONE_PROGRAM, fromWidgets(widgets));
    }

    public static List<IProgWidget> loadProgWidgets(ItemStack stack) {
        return deepCopy(fromItemStack(stack).widgets);
    }

    public Map<ProgWidgetType<?>,Integer> summarize() {
        Map<ProgWidgetType<?>,Integer> res = new HashMap<>();
        widgets.forEach(w -> res.put(w.getType(), res.getOrDefault(w.getType(), 0) + 1));
        return res;
    }

    public int getRequiredPuzzlePieces() {
        return (int) widgets.stream().filter(w -> !w.freeToUse()).count();
    }

    public boolean isValidForDrone(IDroneBase drone) {
        return widgets.stream().allMatch(widget -> drone.isProgramApplicable(widget.getType()));
    }

    public boolean isEmpty() {
        return widgets.isEmpty();
    }

    private static List<IProgWidget> deepCopy(List<IProgWidget> list) {
        return list.stream().map(IProgWidget::copyWidget).toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SavedDroneProgram that = (SavedDroneProgram) o;
        return Objects.equals(widgets, that.widgets);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
