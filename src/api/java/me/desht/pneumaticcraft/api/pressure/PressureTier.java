package me.desht.pneumaticcraft.api.pressure;

public interface PressureTier {
    float getDangerPressure();
    float getCriticalPressure();

    PressureTier TIER_ONE = new PressureTier() {
        @Override
        public float getDangerPressure() {
            return 5f;
        }

        @Override
        public float getCriticalPressure() {
            return 7f;
        }
    };

    PressureTier TIER_TWO = new PressureTier() {
        @Override
        public float getDangerPressure() {
            return 20f;
        }

        @Override
        public float getCriticalPressure() {
            return 25f;
        }
    };
}
