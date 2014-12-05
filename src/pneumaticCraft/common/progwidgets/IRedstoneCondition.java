package pneumaticCraft.common.progwidgets;

public interface IRedstoneCondition{
    public int getRequiredRedstone();

    public Operator getOperator();
    
    public enum Operator{
        EQUALS, HIGHER_THAN;
    }
}
