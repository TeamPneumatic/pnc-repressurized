package pneumaticCraft.common.item;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemPlastic extends ItemPneumatic{

    public ItemPlastic(){
        setTextureName("paper");//load up the paper texture;
        setHasSubtypes(true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item par1, CreativeTabs tab, List subItems){
        for(int i = 0; i < 16; i++) {
            if(i == ItemPlasticPlants.ADRENALINE_PLANT_DAMAGE) continue;
            if(i == ItemPlasticPlants.MUSIC_PLANT_DAMAGE) continue;
            subItems.add(new ItemStack(this, 1, i));
        }
    }

    @Override
    public int getMetadata(int meta){
        return meta;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack itemStack, int renderPass){
        int plasticColour = getColour(itemStack);
        if(plasticColour < 0) {
            plasticColour = Integer.parseInt("ffffff", 16);
        }
        return plasticColour;
    }

    private int getColour(ItemStack iStack){
        switch(iStack.getItemDamage()){
            case ItemPlasticPlants.ADRENALINE_PLANT_DAMAGE:
                return Integer.parseInt("b1b1b1", 16);
            case ItemPlasticPlants.BURST_PLANT_DAMAGE:
                return Integer.parseInt("848484", 16);
            case ItemPlasticPlants.CHOPPER_PLANT_DAMAGE:
                return Integer.parseInt("82ace7", 16);
            case ItemPlasticPlants.CREEPER_PLANT_DAMAGE:
                return Integer.parseInt("4a6b18", 16);
            case ItemPlasticPlants.ENDER_PLANT_DAMAGE:
                return Integer.parseInt("8230b2", 16);
            case ItemPlasticPlants.FIRE_FLOWER_DAMAGE:
                return Integer.parseInt("a72222", 16);
            case ItemPlasticPlants.FLYING_FLOWER_DAMAGE:
                return Integer.parseInt("ffffff", 16);
            case ItemPlasticPlants.HELIUM_PLANT_DAMAGE:
                return Integer.parseInt("e5e62a", 16);
            case ItemPlasticPlants.LIGHTNING_PLANT_DAMAGE:
                return Integer.parseInt("1a6482", 16);
            case ItemPlasticPlants.MUSIC_PLANT_DAMAGE:
                return Integer.parseInt("be5cb8", 16);
            case ItemPlasticPlants.POTION_PLANT_DAMAGE:
                return Integer.parseInt("f7b4d6", 16);
            case ItemPlasticPlants.PROPULSION_PLANT_DAMAGE:
                return Integer.parseInt("e69e34", 16);
            case ItemPlasticPlants.RAIN_PLANT_DAMAGE:
                return Integer.parseInt("0a2b7a", 16);
            case ItemPlasticPlants.REPULSION_PLANT_DAMAGE:
                return Integer.parseInt("83d41c", 16);
            case ItemPlasticPlants.SLIME_PLANT_DAMAGE:
                return Integer.parseInt("795400", 16);
            case ItemPlasticPlants.SQUID_PLANT_DAMAGE:
                return Integer.parseInt("000000", 16);
        }
        return -1;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List infoList, boolean par4){
        infoList.add(I18n.format("gui.tooltip.plasticPlant", I18n.format(Itemss.plasticPlant.getUnlocalizedName(stack) + ".name")));
    }

    @Override
    public String getUnlocalizedName(ItemStack stack){
        return super.getUnlocalizedName(stack) + "." + ItemDye.field_150923_a[MathHelper.clamp_int(stack.getItemDamage(), 0, 15)];
    }
}
