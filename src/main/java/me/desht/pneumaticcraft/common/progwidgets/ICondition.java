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
        EQUALS("="), HIGHER_THAN_EQUALS(">="), LESS_THAN_EQUALS("<=");

        private final String symbol;

        Operator(String symbol) {
            this.symbol = symbol;
        }

        public boolean evaluate(int count1, int count2) {
            switch (this) {
                case EQUALS: return count1 == count2;
                case HIGHER_THAN_EQUALS: return count1 >= count2;
                case LESS_THAN_EQUALS: return count1 <= count2;
            }
            return false;
        }

        public boolean evaluate(float count1, float count2) {
            switch (this) {
                case EQUALS: return count1 == count2;
                case HIGHER_THAN_EQUALS: return count1 >= count2;
                case LESS_THAN_EQUALS: return count1 <= count2;
            }
            return false;
        }

        @Override
        public String toString() {
            return symbol;
        }
    }
}
