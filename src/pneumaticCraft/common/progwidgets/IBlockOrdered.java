package pneumaticCraft.common.progwidgets;

import net.minecraft.client.resources.I18n;

public interface IBlockOrdered{
    public enum EnumOrder{
        CLOSEST("closest"), LOW_TO_HIGH("lowToHigh"), HIGH_TO_LOW("highToLow");
        public String name;

        EnumOrder(String name){
            this.name = name;
        }

        public String getLocalizedName(){
            return I18n.format("gui.progWidget.blockOrder." + this);
        }

        @Override
        public String toString(){
            return name;
        }
    }

    public EnumOrder getOrder();

    public void setOrder(EnumOrder order);
}
