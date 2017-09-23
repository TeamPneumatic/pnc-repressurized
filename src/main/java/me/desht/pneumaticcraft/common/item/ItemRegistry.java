package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.item.IInventoryItem;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;

import java.util.*;

public class ItemRegistry implements IItemRegistry {

    private static ItemRegistry INSTANCE = new ItemRegistry();
    public final List<IInventoryItem> inventoryItems = new ArrayList<IInventoryItem>();
    private final Map<Item, List<IUpgradeAcceptor>> upgradeToAcceptors = new HashMap<Item, List<IUpgradeAcceptor>>();

    public static ItemRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public void registerInventoryItem(IInventoryItem handler) {
        if (handler == null) throw new NullPointerException("IInventoryItem is null!");
        inventoryItems.add(handler);
    }

    @Override
    public Item getUpgrade(EnumUpgrade type) {
        return Itemss.upgrades.get(type);
    }

    @Override
    public void registerUpgradeAcceptor(IUpgradeAcceptor upgradeAcceptor) {
        if (upgradeAcceptor == null) throw new NullPointerException("Upgrade acceptor is null!");
        Set<Item> applicableUpgrades = upgradeAcceptor.getApplicableUpgrades();
        if (applicableUpgrades != null) {
            for (Item applicableUpgrade : applicableUpgrades) {
                List<IUpgradeAcceptor> acceptors = upgradeToAcceptors.computeIfAbsent(applicableUpgrade, k -> new ArrayList<>());
                acceptors.add(upgradeAcceptor);
            }
        }
    }

    @Override
    public void addTooltip(Item upgrade, List<String> tooltip) {
        List<IUpgradeAcceptor> acceptors = upgradeToAcceptors.get(upgrade);
        if (acceptors != null) {
            List<String> tempList = new ArrayList<String>(acceptors.size());
            for (IUpgradeAcceptor acceptor : acceptors) {
                tempList.add("-" + I18n.format(acceptor.getName()));
            }
            Collections.sort(tempList);
            tooltip.addAll(tempList);
        }
    }

}
