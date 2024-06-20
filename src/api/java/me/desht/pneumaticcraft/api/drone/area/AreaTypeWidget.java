package me.desht.pneumaticcraft.api.drone.area;

import me.desht.pneumaticcraft.api.misc.ITranslatableEnum;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Abstraction of widgets which are displayed by the editor screens for area types. These widgets allow the player
 * to configure the area type parameters as needed.
 */
public abstract class AreaTypeWidget {
    private final String translationKey;

    public AreaTypeWidget(String translationKey) {
        this.translationKey = translationKey;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public abstract Component getDisplayName();

    /**
     * Adds a number textbox allowing an integer quantity to be entered.
     */
    public static class IntegerField extends AreaTypeWidget {
        public final IntSupplier readAction;
        public final IntConsumer writeAction;

        public IntegerField(String translationKey, IntSupplier readAction, IntConsumer writeAction) {
            super(translationKey);
            this.readAction = readAction;
            this.writeAction = writeAction;
        }

        @Override
        public Component getDisplayName() {
            return Component.literal(Integer.toString(readAction.getAsInt()));
        }
    }

    /**
     * Adds a drop-down selector allowing selection of all the values of the provided enum.
     */
    public static class EnumSelectorField<E extends ITranslatableEnum> extends AreaTypeWidget {
        public final Class<E> enumClass;
        public final Supplier<E> readAction;
        public final Consumer<E> writeAction;

        public EnumSelectorField(String translationKey, Class<E> enumClass, Supplier<E> readAction, Consumer<E> writeAction) {
            super(translationKey);
            this.enumClass = enumClass;
            this.readAction = readAction;
            this.writeAction = writeAction;
        }

        @Override
        public Component getDisplayName() {
            return Component.translatable(readAction.get().getTranslationKey());
        }
    }
}
