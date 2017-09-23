package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemPneumatic extends Item {
    public ItemPneumatic(String registryName) {
        super();
        setCreativeTab(PneumaticCraftRepressurized.tabPneumaticCraft);
        setRegistryName(registryName);
        setUnlocalizedName(registryName);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> curInfo, ITooltipFlag extraInfo) {
        super.addInformation(stack, worldIn, curInfo, extraInfo);
        addTooltip(stack, worldIn, curInfo);
    }

//    public void registerItemVariants() {
//        List<ItemStack> stacks = new ArrayList<ItemStack>();
//        getSubItems(this, null, stacks);
//        for (ItemStack stack : stacks) {
//            ResourceLocation resLoc = new ResourceLocation(Names.MOD_ID, getModelLocation(stack));
//            ModelBakery.registerItemVariants(this, resLoc);
//            Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(this, stack.getItemDamage(), new ModelResourceLocation(resLoc, "inventory"));
//        }
//    }
//
//    protected String getModelLocation(ItemStack stack) {
//        return stack.getUnlocalizedName().substring(5);
//    }

    public static void addTooltip(ItemStack stack, World world, List<String> curInfo) {
        String info = "gui.tooltip." + stack.getItem().getUnlocalizedName();
        String translatedInfo = I18n.format(info);
        if (!translatedInfo.equals(info)) {
            if (PneumaticCraftRepressurized.proxy.isSneakingInGui()) {
                translatedInfo = TextFormatting.AQUA + translatedInfo;
                if (!Loader.isModLoaded(ModIds.IGWMOD))
                    translatedInfo += " \\n \\n" + I18n.format("gui.tab.info.assistIGW");
                curInfo.addAll(PneumaticCraftUtils.convertStringIntoList(translatedInfo, 40));
            } else {
                curInfo.add(TextFormatting.AQUA + I18n.format("gui.tooltip.sneakForInfo"));
            }
        }
    }
}
