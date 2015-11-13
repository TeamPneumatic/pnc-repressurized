package pneumaticCraft.common.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.ModIds;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemPneumatic extends Item{
    private boolean hasTexture;

    public ItemPneumatic(){
        setCreativeTab(PneumaticCraft.tabPneumaticCraft);
    }

    public ItemPneumatic(String textureLocation){
        this();
        if(textureLocation != null) {
            setTextureName(Textures.ICON_LOCATION + textureLocation);
            setUnlocalizedName(textureLocation);
        }
    }

    @Override
    public Item setTextureName(String p_111206_1_){
        super.setTextureName(p_111206_1_);
        hasTexture = true;
        return this;
    }

    @Override
    public void registerIcons(IIconRegister par1IconRegister){
        if(hasTexture) super.registerIcons(par1IconRegister);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List curInfo, boolean extraInfo){
        super.addInformation(stack, player, curInfo, extraInfo);
        addTooltip(stack, player, curInfo);
    }

    public static void addTooltip(ItemStack stack, EntityPlayer player, List curInfo){
        String info = "gui.tooltip." + stack.getItem().getUnlocalizedName();
        String translatedInfo = I18n.format(info);
        if(!translatedInfo.equals(info)) {
            if(PneumaticCraft.proxy.isSneakingInGui()) {
                translatedInfo = EnumChatFormatting.AQUA + translatedInfo;
                if(!Loader.isModLoaded(ModIds.IGWMOD)) translatedInfo += " \\n \\n" + I18n.format("gui.tab.info.assistIGW");
                curInfo.addAll(PneumaticCraftUtils.convertStringIntoList(translatedInfo, 60));
            } else {
                curInfo.add(EnumChatFormatting.AQUA + I18n.format("gui.tooltip.sneakForInfo"));
            }
        }
    }
}
