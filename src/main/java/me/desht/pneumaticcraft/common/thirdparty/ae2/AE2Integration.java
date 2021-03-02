package me.desht.pneumaticcraft.common.thirdparty.ae2;

public class AE2Integration {
    private static boolean isAvailable;

    public static boolean isAvailable() {
        return isAvailable;
    }

    static void setAvailable() {
        AE2Integration.isAvailable = true;
    }
}
