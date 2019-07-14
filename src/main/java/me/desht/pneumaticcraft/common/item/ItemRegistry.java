package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.item.IInventoryItem;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.api.item.IMagnetSuppressor;
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class ItemRegistry implements IItemRegistry {

    private static final ItemRegistry INSTANCE = new ItemRegistry();
    public final List<IInventoryItem> inventoryItems = new ArrayList<>();
    private final Map<Item, List<IUpgradeAcceptor>> upgradeToAcceptors = new HashMap<>();
    private final List<IMagnetSuppressor> magnetSuppressors = new ArrayList<>();

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
        Set<Item> applicableUpgrades = upgradeAcceptor.getApplicableUpgrades();
        if (applicableUpgrades != null) {
            for (Item applicableUpgrade : applicableUpgrades) {
                List<IUpgradeAcceptor> acceptors = upgradeToAcceptors.computeIfAbsent(applicableUpgrade, k -> new ArrayList<>());
                acceptors.add(upgradeAcceptor);
            }
        }
    }

    @Override
    public void addTooltip(Item upgrade, List<ITextComponent> tooltip) {
        List<IUpgradeAcceptor> acceptors = upgradeToAcceptors.get(upgrade);
        if (acceptors != null) {
            List<String> tempList = new ArrayList<>(acceptors.size());
            for (IUpgradeAcceptor acceptor : acceptors) {
                tempList.add("\u2022 " + I18n.format(acceptor.getUpgradeAcceptorTranslationKey()));
            }
            Collections.sort(tempList);
            tooltip.addAll(tempList.stream().map(StringTextComponent::new).collect(Collectors.toList()));
        }
    }

    @Override
    public void registerMagnetSuppressor(IMagnetSuppressor suppressor) {
        magnetSuppressors.add(suppressor);
    }

    public boolean shouldSuppressMagnet(Entity e) {
        return magnetSuppressors.stream().anyMatch(s -> s.shouldSuppressMagnet(e));
    }
}
