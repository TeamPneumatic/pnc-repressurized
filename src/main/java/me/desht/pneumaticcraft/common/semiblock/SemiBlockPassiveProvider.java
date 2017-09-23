package me.desht.pneumaticcraft.common.semiblock;

public class SemiBlockPassiveProvider extends SemiBlockActiveProvider {
    public static final String ID = "logistic_frame_passive_provider";

    @Override
    public int getColor() {
        return 0xFFFF0000;
    }

    @Override
    public boolean shouldProvideTo(int priority) {
        return priority > 2;
    }
}
