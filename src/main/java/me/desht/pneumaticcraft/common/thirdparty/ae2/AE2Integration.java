package me.desht.pneumaticcraft.common.thirdparty.ae2;

import appeng.api.util.AEColor;
import net.minecraft.item.Item;

public class AE2Integration {
    public static boolean isAvailable;

    public static boolean isAvailable() {
        return isAvailable;
    }

    public static void setAvailable() {
        AE2Integration.isAvailable = true;
    }

    public static Item glassCable() {
        return AE2PNCAddon.api.definitions().parts().cableGlass().item(AEColor.TRANSPARENT);
    }
}
