package pneumaticCraft.common.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import pneumaticCraft.api.item.IProgrammable;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemNetworkComponents extends ItemPneumatic implements IProgrammable{
    public static final int COMPONENT_AMOUNT = 6;

    public static final int DIAGNOSTIC_SUBROUTINE = 0;
    public static final int NETWORK_API = 1;
    public static final int NETWORK_DATA_STORAGE = 2;
    public static final int NETWORK_IO_PORT = 3;
    public static final int NETWORK_REGISTRY = 4;
    public static final int NETWORK_NODE = 5;

    private IIcon[] texture;

    public ItemNetworkComponents(){
        setHasSubtypes(true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister par1IconRegister){
        texture = new IIcon[COMPONENT_AMOUNT];
        texture[0] = par1IconRegister.registerIcon(Textures.ITEM_DIAGNOSTIC_SUBROUTINE);
        texture[1] = par1IconRegister.registerIcon(Textures.ITEM_NETWORK_API);
        texture[2] = par1IconRegister.registerIcon(Textures.ITEM_NETWORK_DATA_STORAGE);
        texture[3] = par1IconRegister.registerIcon(Textures.ITEM_NETWORK_IO_PORT);
        texture[4] = par1IconRegister.registerIcon(Textures.ITEM_NETWORK_REGISTRY);
        texture[5] = par1IconRegister.registerIcon(Textures.ITEM_NETWORK_NODE);
    }

    @Override
    public IIcon getIconFromDamage(int meta){
        return texture[meta < texture.length ? meta : 0];
    }

    @Override
    public String getUnlocalizedName(ItemStack is){
        return super.getUnlocalizedName(is) + is.getItemDamage();
    }

    @Override
    public int getMetadata(int meta){
        return meta;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item par1, CreativeTabs tab, List subItems){
        for(int i = 0; i < COMPONENT_AMOUNT; i++) {
            subItems.add(new ItemStack(this, 1, i));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List infoList, boolean par4){}

    @Override
    public boolean canProgram(ItemStack stack){
        return stack.getItemDamage() == NETWORK_API || stack.getItemDamage() == NETWORK_DATA_STORAGE;
    }

    @Override
    public boolean usesPieces(ItemStack stack){
        return stack.getItemDamage() == NETWORK_API;
    }

    @Override
    public boolean showProgramTooltip(){
        return true;
    }

}
