package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.common.ai.IDroneBase;

public interface ICondition {
    boolean isAndFunction();

    void setAndFunction(boolean isAndFunction);

    int getRequiredCount();

    void setRequiredCount(int count);

    Operator getOperator();

    void setOperator(Operator operator);

    /**
     * Used in the CC compatibility.
     *
     * @param drone
     * @return
     */
    boolean evaluate(IDroneBase drone, IProgWidget widget);

    enum Operator {
        EQUALS("="), HIGHER_THAN_EQUALS(">=");

        private final String symbol;

        Operator(String symbol) {
            this.symbol = symbol;
        }

        @Override
        public String toString() {
            return symbol;
        }
    }
}
