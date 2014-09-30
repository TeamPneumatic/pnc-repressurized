package pneumaticCraft.common.thirdparty.forestry;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.common.item.ItemPneumatic;
import pneumaticCraft.common.item.Itemss;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemPlasticElectronTube extends ItemPneumatic{

    private IIcon overlayTexture;

    public ItemPlasticElectronTube(String name){
        super(name);
        setHasSubtypes(true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister){

        super.registerIcons(iconRegister);
        overlayTexture = iconRegister.registerIcon(getIconString() + "Overlay");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item par1, CreativeTabs tab, List subItems){
        subItems.addAll(getSubItems());
    }

    public List<ItemStack> getSubItems(){
        List<ItemStack> subItems = new ArrayList<ItemStack>();
        List<ItemStack> items = new ArrayList<ItemStack>();
        ((ItemPlasticPlants)Itemss.plasticPlant).addSubItems(items);
        for(ItemStack item : items) {
            subItems.add(new ItemStack(this, 1, item.getItemDamage()));
        }
        return subItems;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack itemStack, int renderPass){

        return renderPass == 0 || itemStack.getItemDamage() >= 16 ? super.getColorFromItemStack(itemStack, renderPass) : ItemDye.field_150922_c[itemStack.getItemDamage()];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean requiresMultipleRenderPasses(){

        return true;
    }

    /**
     * Gets an icon index based on an item's damage value and the given render pass
     */
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamageForRenderPass(int meta, int renderPass){

        return renderPass == 0 || meta >= 16 ? itemIcon : overlayTexture;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack){
        return super.getUnlocalizedName() + stack.getItemDamage();
    }
}
