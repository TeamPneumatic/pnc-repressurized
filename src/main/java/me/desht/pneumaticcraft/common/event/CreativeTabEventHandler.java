package me.desht.pneumaticcraft.common.event;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.pressure.IPressurizableItem;
import me.desht.pneumaticcraft.common.capabilities.AirHandlerItemStack;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.*;
import me.desht.pneumaticcraft.common.item.minigun.AbstractGunAmmoItem;
import me.desht.pneumaticcraft.common.recipes.special.PatchouliBookCrafting;
import me.desht.pneumaticcraft.common.semiblock.SemiblockItem;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

@Mod.EventBusSubscriber(modid = Names.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CreativeTabEventHandler {
    @SubscribeEvent
    public static void onCreativeTabRegister(CreativeModeTabEvent.Register event) {
        List<ItemStack> items = ModItems.ITEMS.getEntries().stream()
                .flatMap(ro -> stacksForItem(ro.get()))
                .sorted(new ItemSorter())
                .collect(Collectors.toCollection(ArrayList::new));

        if (ModList.get().isLoaded(ModIds.PATCHOULI)) {
            items.add(PatchouliBookCrafting.makeGuideBook());
        }

        event.registerCreativeModeTab(RL("default"), builder ->
            builder.title(xlate("itemGroup.pneumaticcraft"))
                    .icon(() -> new ItemStack(ModItems.PRESSURE_GAUGE.get()))
                    .displayItems((flags, output, b) -> output.acceptAll(items))
                    .build()
        );
    }

    private static Stream<ItemStack> stacksForItem(Item item) {
        ItemStack stack = new ItemStack(item);
        if (item instanceof CreativeTabStackProvider provider) {
            return provider.getStacksForItem();
        } else if (item instanceof BlockItem bi && bi.getBlock() instanceof CreativeTabStackProvider provider) {
            return provider.getStacksForItem();
        } else if (item instanceof IPressurizableItem p) {
            ItemStack stack2 = stack.copy();
            new AirHandlerItemStack(stack2).addAir((int) (p.getBaseVolume() * p.getMaxPressure()));
            return Stream.of(new ItemStack(item), stack2);
        } else {
            return Stream.of(stack);
        }
    }

    private static class ItemSorter implements Comparator<ItemStack> {
        @Override
        public int compare(ItemStack s1, ItemStack s2) {
            for (Class<?> cls : List.of(BlockItem.class, PressurizableItem.class, CompressedIronArmorItem.class, PneumaticArmorItem.class, SemiblockItem.class, AbstractGunAmmoItem.class, UpgradeItem.class, TubeModuleItem.class, PneumaticCraftBucketItem.class)) {
                if (cls.isAssignableFrom(s1.getItem().getClass()) && !cls.isAssignableFrom(s2.getItem().getClass())) {
                    return -1;
                } else if (cls.isAssignableFrom(s2.getItem().getClass()) && !cls.isAssignableFrom(s1.getItem().getClass())) {
                    return 1;
                }
            }
            return s1.getDisplayName().getString().compareTo(s2.getDisplayName().getString());
        }
    }
}
