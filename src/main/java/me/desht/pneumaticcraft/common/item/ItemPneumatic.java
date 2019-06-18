package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.item.IInventoryItem;
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class ItemPneumatic extends Item {
    public ItemPneumatic(String registryName) {
        super();
        setCreativeTab(PneumaticCraftRepressurized.tabPneumaticCraft);
        setRegistryName(registryName);
        setTranslationKey(registryName);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> curInfo, ITooltipFlag extraInfo) {
        addStandardTooltip(stack, worldIn, curInfo, extraInfo);
    }

    @SideOnly(Side.CLIENT)
    static void addStandardTooltip(ItemStack stack, World world, List<String> curInfo, ITooltipFlag flagIn) {
        ItemPressurizable.addPressureTooltip(stack, curInfo);

        if (stack.getItem() instanceof IUpgradeAcceptor) {
            UpgradableItemUtils.addUpgradeInformation(stack, world, curInfo, ITooltipFlag.TooltipFlags.NORMAL);
        }

        if (stack.getItem() instanceof IInventoryItem) {
            List<ItemStack> stacks = new ArrayList<>();
            ((IInventoryItem) stack.getItem()).getStacksInItem(stack, stacks);
            String header = ((IInventoryItem) stack.getItem()).getInventoryHeader();
            if (header != null && !stacks.isEmpty()) {
                curInfo.add(header);
            }
            PneumaticCraftUtils.sortCombineItemStacksAndToString(curInfo, stacks.toArray(new ItemStack[0]));
        }

        String info = "gui.tooltip." + stack.getTranslationKey();
        if (I18n.hasKey(info)) {
            if (PneumaticCraftRepressurized.proxy.isSneakingInGui()) {
                String translatedInfo = TextFormatting.AQUA + I18n.format(info);
                curInfo.addAll(PneumaticCraftUtils.convertStringIntoList(translatedInfo, 50));
                if (!ThirdPartyManager.instance().docsProvider.docsProviderInstalled()) {
                    curInfo.add(I18n.format("gui.tab.info.assistIGW"));
                }
            } else {
                curInfo.add(TextFormatting.AQUA + I18n.format("gui.tooltip.sneakForInfo"));
            }
        }
    }
}
