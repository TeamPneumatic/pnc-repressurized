package me.desht.pneumaticcraft.common.registry;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.pressure.IPressurizableItem;
import me.desht.pneumaticcraft.common.capabilities.AirHandlerItemStack;
import me.desht.pneumaticcraft.common.item.*;
import me.desht.pneumaticcraft.common.item.minigun.AbstractGunAmmoItem;
import me.desht.pneumaticcraft.common.semiblock.SemiblockItem;
import me.desht.pneumaticcraft.common.thirdparty.patchouli.PatchouliBookCrafting;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ModCreativeModeTab {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PneumaticRegistry.MOD_ID);

    public static final Supplier<CreativeModeTab> DEFAULT = TABS.register("default", ModCreativeModeTab::buildDefaultTab);

    private static CreativeModeTab buildDefaultTab() {
        return CreativeModeTab.builder()
                .title(xlate("itemGroup.pneumaticcraft"))
                .icon(() -> new ItemStack(ModItems.PRESSURE_GAUGE.get()))
                .displayItems(ModCreativeModeTab::genDisplayItems)
                .build();
    }

    private static void genDisplayItems(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output output) {
        List<ItemStack> items = ModItems.ITEMS.getEntries().stream()
                .flatMap(ro -> stacksForItem(ro.get()))
                .sorted(new ItemSorter())
                .collect(Collectors.toCollection(ArrayList::new));
        if (ModList.get().isLoaded(ModIds.PATCHOULI)) {
            items.add(PatchouliBookCrafting.makeGuideBook());
        }
        output.acceptAll(items);
    }

    private static Stream<ItemStack> stacksForItem(Item item) {
        ItemStack stack = new ItemStack(item);
        switch (item) {
            case CreativeTabStackProvider provider -> {
                return provider.getStacksForItem();
            }
            case BlockItem bi when bi.getBlock() instanceof CreativeTabStackProvider provider -> {
                return provider.getStacksForItem();
            }
            case IPressurizableItem p -> {
                ItemStack stack2 = stack.copy();
                new AirHandlerItemStack(stack2).addAir((int) (p.getBaseVolume() * p.getMaxPressure()));
                return Stream.of(new ItemStack(item), stack2);
            }
            default -> {
                return Stream.of(stack);
            }
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
