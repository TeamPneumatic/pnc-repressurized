package pneumaticCraft.common.block.tubes;

public interface IInfluenceDispersing{
    public int getMaxDispersion();

    public void onAirDispersion(int amount);
}
