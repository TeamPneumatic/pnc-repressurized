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
        EQ("="), GE(">="), LE("<=");

        private final String symbol;

        Operator(String symbol) {
            this.symbol = symbol;
        }

        public boolean evaluate(int count1, int count2) {
            switch (this) {
                case EQ: return count1 == count2;
                case GE: return count1 >= count2;
                case LE: return count1 <= count2;
            }
            return false;
        }

        public boolean evaluate(float count1, float count2) {
            switch (this) {
                case EQ: return count1 == count2;
                case GE: return count1 >= count2;
                case LE: return count1 <= count2;
            }
            return false;
        }

        @Override
        public String toString() {
            return symbol;
        }
    }
}
