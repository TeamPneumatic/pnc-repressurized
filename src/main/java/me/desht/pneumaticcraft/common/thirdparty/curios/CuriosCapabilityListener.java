package me.desht.pneumaticcraft.common.thirdparty.curios;

import me.desht.pneumaticcraft.common.item.ItemMemoryStick;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Names.MOD_ID)
public class CuriosCapabilityListener {
    @SubscribeEvent
    public static void attachCurioInvTicker(AttachCapabilitiesEvent<ItemStack> event) {
        if (Curios.available && event.getObject().getItem() instanceof ItemMemoryStick) {
            CuriosTickerCapability.addCuriosCap(event);
        }
    }
}
