package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.item.*;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.GuiConstants;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public enum ItemRegistry implements IItemRegistry {
    INSTANCE;

    private final List<Item> inventoryItemBlacklist = new ArrayList<>();
    public final List<IInventoryItem> inventoryItems = new ArrayList<>();
    private final Map<EnumUpgrade, List<IUpgradeAcceptor>> upgradeToAcceptors = new EnumMap<>(EnumUpgrade.class);
    private final List<IMagnetSuppressor> magnetSuppressors = new ArrayList<>();
    private final List<ItemVolumeModifier> volumeModifiers = new ArrayList<>();

    public static ItemRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public void registerInventoryItem(@Nonnull IInventoryItem handler) {
        Validate.notNull(handler);
        inventoryItems.add(handler);
    }

    @Override
    public void registerUpgradeAcceptor(@Nonnull IUpgradeAcceptor upgradeAcceptor) {
        Map<EnumUpgrade,Integer> applicableUpgrades = upgradeAcceptor.getApplicableUpgrades();
        if (applicableUpgrades != null) {
            for (EnumUpgrade applicableUpgrade : applicableUpgrades.keySet()) {
                List<IUpgradeAcceptor> acceptors = upgradeToAcceptors.computeIfAbsent(applicableUpgrade, k -> new ArrayList<>());
                acceptors.add(upgradeAcceptor);
            }
        }
    }

    @Override
    public void addTooltip(EnumUpgrade upgrade, List<ITextComponent> tooltip) {
        List<IUpgradeAcceptor> acceptors = upgradeToAcceptors.get(upgrade);
        if (acceptors != null) {
            List<String> tempList = new ArrayList<>(acceptors.size());
            for (IUpgradeAcceptor acceptor : acceptors) {
                tempList.add(GuiConstants.BULLET + " " + I18n.get(acceptor.getUpgradeAcceptorTranslationKey()));
            }
            Collections.sort(tempList);
            tooltip.addAll(tempList.stream().map(StringTextComponent::new).collect(Collectors.toList()));
        }
    }

    @Override
    public void registerMagnetSuppressor(IMagnetSuppressor suppressor) {
        magnetSuppressors.add(suppressor);
    }

    @Override
    public boolean doesItemMatchFilter(@Nonnull ItemStack filterStack, @Nonnull ItemStack stack, boolean checkDurability, boolean checkNBT, boolean checkModSimilarity) {
        return PneumaticCraftUtils.doesItemMatchFilter(filterStack, stack, checkDurability, checkNBT, checkModSimilarity);
    }

    @Override
    public void registerPneumaticVolumeModifier(ItemVolumeModifier modifierFunc) {
        volumeModifiers.add(modifierFunc);
    }

    @Override
    public ISpawnerCoreStats getSpawnerCoreStats(ItemStack stack) {
        Validate.isTrue(stack.getItem() instanceof ItemSpawnerCore, "item is not a Spawner Core!");
        return ItemSpawnerCore.SpawnerCoreStats.forItemStack(stack);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean shouldSuppressMagnet(Entity e) {
        return magnetSuppressors.stream().anyMatch(s -> s.shouldSuppressMagnet(e));
    }

    /**
     * Get a list of the items contained in the given item.  This uses the {@link IInventoryItem} interface.
     *
     * @param item the item to check
     * @return a list of the items contained within the given item
     */
    public List<ItemStack> getStacksInItem(@Nonnull ItemStack item) {
        List<ItemStack> items = new ArrayList<>();
        if (item.getItem() instanceof IInventoryItem && !inventoryItemBlacklist.contains(item.getItem())) {
            try {
                ((IInventoryItem) item.getItem()).getStacksInItem(item, items);
            } catch (Throwable e) {
                Log.error("An InventoryItem crashed:");
                e.printStackTrace();
                inventoryItemBlacklist.add(item.getItem());
            }
        } else {
            Iterator<IInventoryItem> iterator = getInstance().inventoryItems.iterator();
            while (iterator.hasNext()) {
                try {
                    iterator.next().getStacksInItem(item, items);
                } catch (Throwable e) {
                    Log.error("An InventoryItem crashed:");
                    e.printStackTrace();
                    iterator.remove();
                }
            }
        }
        return items;
    }

    public int getUpgradedVolume(ItemStack stack, int baseVolume) {
        for (ItemVolumeModifier modifier : volumeModifiers) {
            baseVolume = modifier.getNewVolume(stack, baseVolume);
        }
        return baseVolume;
    }
}
