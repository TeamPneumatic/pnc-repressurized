package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.item.IInventoryItem;
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ItemPneumatic extends Item {
    public static final Item.Properties DEFAULT_PROPS = new Item.Properties().group(ModItems.PNC_CREATIVE_TAB);

    public ItemPneumatic(String registryName) {
        this(DEFAULT_PROPS, registryName);
    }

    public ItemPneumatic(Item.Properties props, String registryName) {
        super(props);
        setRegistryName(registryName);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> curInfo, ITooltipFlag extraInfo) {
        addStandardTooltip(stack, worldIn, curInfo, extraInfo);
    }

    @OnlyIn(Dist.CLIENT)
    static void addStandardTooltip(ItemStack stack, World world, List<ITextComponent> curInfo, ITooltipFlag flagIn) {
        ItemPressurizable.addPressureTooltip(stack, curInfo);

        if (stack.getItem() instanceof IUpgradeAcceptor) {
            UpgradableItemUtils.addUpgradeInformation(stack, world, curInfo, ITooltipFlag.TooltipFlags.NORMAL);
        }

        if (stack.getItem() instanceof IInventoryItem) {
            List<ItemStack> stacks = new ArrayList<>();
            ((IInventoryItem) stack.getItem()).getStacksInItem(stack, stacks);
            ITextComponent header = ((IInventoryItem) stack.getItem()).getInventoryHeader();
            if (header != null && !stacks.isEmpty()) {
                curInfo.add(header);
            }
            PneumaticCraftUtils.sortCombineItemStacksAndToString(curInfo, stacks.toArray(new ItemStack[0]));
        }

        String info = "gui.tooltip." + stack.getTranslationKey();
        if (I18n.hasKey(info)) {
            if (PneumaticCraftRepressurized.proxy.isSneakingInGui()) {
                String translatedInfo = TextFormatting.AQUA + I18n.format(info);
                curInfo.addAll(PneumaticCraftUtils.asStringComponent(PneumaticCraftUtils.convertStringIntoList(translatedInfo, 50)));
                if (!ThirdPartyManager.instance().docsProvider.docsProviderInstalled()) {
                    curInfo.add(xlate("gui.tab.info.assistIGW"));
                }
            } else {
                curInfo.add(xlate("gui.tooltip.sneakForInfo").applyTextStyle(TextFormatting.AQUA));
            }
        }
    }
}
