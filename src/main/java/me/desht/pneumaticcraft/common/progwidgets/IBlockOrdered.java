package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.common.util.ITranslatableEnum;

public interface IBlockOrdered {
    enum Ordering implements ITranslatableEnum {
        CLOSEST("closest"), LOW_TO_HIGH("lowToHigh"), HIGH_TO_LOW("highToLow");
        public final String name;

        Ordering(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public String getTranslationKey() {
            return "pneumaticcraft.gui.progWidget.blockOrder." + this;
        }
    }

    Ordering getOrder();

    void setOrder(Ordering order);
}
