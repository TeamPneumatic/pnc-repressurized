package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.item.*;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.GuiConstants;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class ItemRegistry implements IItemRegistry {

    private static final ItemRegistry INSTANCE = new ItemRegistry();
    public final List<IInventoryItem> inventoryItems = new ArrayList<>();
    private final Map<EnumUpgrade, List<IUpgradeAcceptor>> upgradeToAcceptors = new HashMap<>();
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
                tempList.add(GuiConstants.BULLET + " " + I18n.format(acceptor.getUpgradeAcceptorTranslationKey()));
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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean shouldSuppressMagnet(Entity e) {
        return magnetSuppressors.stream().anyMatch(s -> s.shouldSuppressMagnet(e));
    }
}
