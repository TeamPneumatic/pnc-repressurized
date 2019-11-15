package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.api.item.IPressurizable;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CraftingEventHandler {
    /**
     * Add any air in recipe ingredients to the final item (assuming it's a pressurizable item)
     */
    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack outputStack = event.getCrafting();
        if (outputStack.getItem() instanceof IPressurizable) {
            int totalAir = 0;
            for (int i = 0; i < event.getInventory().getSizeInventory(); i++) {
                ItemStack s = event.getInventory().getStackInSlot(i);
                if (s.getItem() instanceof IPressurizable) {
                    float p = ((IPressurizable) s.getItem()).getPressure(s);
                    float a = ((IPressurizable) s.getItem()).getVolume(s);
                    totalAir += p * a;
                }
            }
            ((IPressurizable) outputStack.getItem()).addAir(outputStack, totalAir);
        }
    }
}
