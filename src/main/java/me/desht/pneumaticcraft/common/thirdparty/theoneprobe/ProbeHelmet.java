package me.desht.pneumaticcraft.common.thirdparty.theoneprobe;

import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import me.desht.pneumaticcraft.common.registry.ModItems;
import net.minecraft.world.item.ArmorItem;
import net.neoforged.neoforge.registries.DeferredItem;

public class ProbeHelmet {
    public static final DeferredItem<PneumaticArmorItem> PNEUMATIC_HELMET_PROBE
            = ModItems.register("pneumatic_helmet_probe", () -> new PneumaticArmorItem(ArmorItem.Type.HELMET));

    public static void init() {
    }
}
