package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.api.harvesting.HoeHandler;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.item.HoeItem;
import net.minecraftforge.registries.DeferredRegister;

public class ModHoeHandlers {
    public static final DeferredRegister<HoeHandler> HOE_HANDLERS = new DeferredRegister<>(ModRegistries.HOE_HANDLERS, Names.MOD_ID);

    static {
        HOE_HANDLERS.register("default_hoe_handler",
                () -> new HoeHandler(item -> item.getItem() instanceof HoeItem,
                        (stack, player) -> stack.damageItem(1, player, p -> { })));
    }

}
