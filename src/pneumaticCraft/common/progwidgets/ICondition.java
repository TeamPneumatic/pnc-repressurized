package pneumaticCraft.common.progwidgets;

import pneumaticCraft.common.ai.IDroneBase;

public interface ICondition{
    public boolean isAndFunction();

    public void setAndFunction(boolean isAndFunction);

    public int getRequiredCount();

    public void setRequiredCount(int count);

    public Operator getOperator();

    public void setOperator(Operator operator);

    /**
     * Used in the CC compatibility.
     * @param drone TODO
     * @return
     */
    public boolean evaluate(IDroneBase drone, IProgWidget widget);

    public enum Operator{
        EQUALS("="), HIGHER_THAN_EQUALS(">=");

        private final String symbol;

        private Operator(String symbol){
            this.symbol = symbol;
        }

        @Override
        public String toString(){
            return symbol;
        }
    }
}
