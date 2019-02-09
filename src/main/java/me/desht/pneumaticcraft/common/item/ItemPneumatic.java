package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
        super.addInformation(stack, worldIn, curInfo, extraInfo);
        addTooltip(stack, worldIn, curInfo);
    }

    public static void addTooltip(ItemStack stack, World world, List<String> curInfo) {
        String info = "gui.tooltip." + stack.getTranslationKey();
        String translatedInfo = I18n.format(info);
        if (!translatedInfo.equals(info)) {
            if (PneumaticCraftRepressurized.proxy.isSneakingInGui()) {
                translatedInfo = TextFormatting.AQUA + translatedInfo;
                if (!ThirdPartyManager.instance().docsProvider.docsProviderInstalled()) {
                    translatedInfo += " \\n \\n" + I18n.format("gui.tab.info.assistIGW");
                }
                curInfo.addAll(PneumaticCraftUtils.convertStringIntoList(translatedInfo, 40));
            } else {
                curInfo.add(TextFormatting.AQUA + I18n.format("gui.tooltip.sneakForInfo"));
            }
        }
    }
}
