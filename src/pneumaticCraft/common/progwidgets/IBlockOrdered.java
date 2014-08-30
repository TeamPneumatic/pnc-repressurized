package pneumaticCraft.common.progwidgets;

public interface IBlockOrdered{
    public enum EnumOrder{
        CLOSEST("Closest block", "closest"), LOW_TO_HIGH("From low to high", "lowToHigh"), HIGH_TO_LOW(
                "From high to low", "highToLow");
        public String name;
        public String ccName;

        EnumOrder(String name, String ccName){
            this.name = name;
            this.ccName = ccName;
        }

        @Override
        public String toString(){
            return name;
        }
    }

    public EnumOrder getOrder();

    public void setOrder(EnumOrder order);
}
