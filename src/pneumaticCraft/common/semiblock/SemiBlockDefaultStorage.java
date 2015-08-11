package pneumaticCraft.common.semiblock;

public class SemiBlockDefaultStorage extends SemiBlockStorage{
    public static final String ID = "logisticFrameDefaultStorage";

    @Override
    public int getColor(){
        return 0xFF008800;
    }

    @Override
    public int getPriority(){
        return 1;
    }

}
