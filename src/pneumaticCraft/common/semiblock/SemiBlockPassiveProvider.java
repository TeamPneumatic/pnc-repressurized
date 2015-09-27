package pneumaticCraft.common.semiblock;

public class SemiBlockPassiveProvider extends SemiBlockActiveProvider{
    public static String ID = "logisticFramePassiveProvider";

    @Override
    public int getColor(){
        return 0xFFFF0000;
    }

    @Override
    public boolean shouldProvideTo(int priority){
        return priority > 2;
    }
}
