package pneumaticCraft.common.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import pneumaticCraft.common.sensor.SensorHandler;
import pneumaticCraft.lib.Names;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemMachineUpgrade extends ItemPneumatic{
    public static final int UPGRADES_AMOUNT = 10;

    public static final int UPGRADE_VOLUME_DAMAGE = 0;
    public static final int UPGRADE_DISPENSER_DAMAGE = 1;
    public static final int UPGRADE_ITEM_LIFE = 2;
    public static final int UPGRADE_ENTITY_TRACKER = 3;
    public static final int UPGRADE_BLOCK_TRACKER = 4;
    public static final int UPGRADE_SPEED_DAMAGE = 5;
    public static final int UPGRADE_SEARCH_DAMAGE = 6;
    public static final int UPGRADE_COORDINATE_TRACKER_DAMAGE = 7;
    public static final int UPGRADE_RANGE = 8;
    public static final int UPGRADE_SECURITY = 9;

    private IIcon[] texture;

    public ItemMachineUpgrade(){
        setHasSubtypes(true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister par1IconRegister){
        texture = new IIcon[UPGRADES_AMOUNT];
        texture[0] = par1IconRegister.registerIcon(Textures.ITEM_UPGRADE_VOLUME);
        texture[1] = par1IconRegister.registerIcon(Textures.ITEM_UPGRADE_DISPENSER);
        texture[2] = par1IconRegister.registerIcon(Textures.ITEM_UPGRADE_ITEM_LIFE);
        texture[3] = par1IconRegister.registerIcon(Textures.ITEM_UPGRADE_ENTITY_TRACKER);
        texture[4] = par1IconRegister.registerIcon(Textures.ITEM_UPGRADE_BLOCK_TRACKER);
        texture[5] = par1IconRegister.registerIcon(Textures.ITEM_UPGRADE_SPEED);
        texture[6] = par1IconRegister.registerIcon(Textures.ITEM_UPGRADE_SEARCH);
        texture[7] = par1IconRegister.registerIcon(Textures.ITEM_UPGRADE_COORDINATE_TRACKER);
        texture[8] = par1IconRegister.registerIcon(Textures.ITEM_UPGRADE_RANGE);
        texture[9] = par1IconRegister.registerIcon(Textures.ITEM_UPGRADE_SECURITY);
    }

    @Override
    public IIcon getIconFromDamage(int meta){
        return texture[meta];
    }

    @Override
    public String getUnlocalizedName(ItemStack stack){
        return super.getUnlocalizedName(stack) + stack.getItemDamage();
    }

    @Override
    public int getMetadata(int meta){
        return meta;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item par1, CreativeTabs tab, List subItems){
        for(int i = 0; i < UPGRADES_AMOUNT; i++) {
            subItems.add(new ItemStack(this, 1, i));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List infoList, boolean par4){
        infoList.add("Used in:");
        SensorHandler.instance().addMachineUpgradeInfo(infoList, stack.getItemDamage());
        switch(stack.getItemDamage()){
            case 0:
                infoList.add("Any machine driven by");
                infoList.add("pneumatic power");
                return;
            case 1:
                infoList.add("-" + Names.AIR_CANNON);
                infoList.add("-" + Names.CHARGING_STATION);
                infoList.add("-" + Names.UNIVERSAL_SENSOR);
                infoList.add("-" + Names.AERIAL_INTERFACE);
                infoList.add("-Drone");
                return;
            case 2:
                infoList.add("-" + Names.AIR_CANNON);
                infoList.add("-" + Names.PRESSURE_CHAMBER);
                infoList.add("-Drone");
                return;
            case 3:
                infoList.add("-" + Names.SECURITY_STATION);
            case 4:
                infoList.add("-" + Names.UNIVERSAL_SENSOR);
            case 6:
            case 7:
                infoList.add("-" + Names.PNEUMATIC_HELMET);
                return;
            case 5:
                infoList.add("Most machines");
                /* infoList.add("-" + Names.AIR_CANNON);
                 infoList.add("-" + Names.AIR_COMPRESSOR);
                 infoList.add("-" + Names.CHARGING_STATION);
                 infoList.add("-" + Names.ELEVATOR);
                 infoList.add("-" + Names.PNEUMATIC_HELMET);
                 infoList.add("-" + Names.PRESSURE_CHAMBER_INTERFACE);
                 infoList.add("-" + Names.VACUUM_PUMP);
                 infoList.add("-" + Names.ASSEMBLY_CONTROLLER);
                 infoList.add("-" + Names.UV_LIGHT_BOX);*/
                return;
            case 8:
                infoList.add("-" + Names.SECURITY_STATION);
                infoList.add("-" + Names.AIR_CANNON);
                infoList.add("-" + Names.PNEUMATIC_HELMET);
                infoList.add("-" + Names.UNIVERSAL_SENSOR);
                return;
            case 9:
                infoList.add("All pneumatic machines");
                /*infoList.add("-" + Names.SECURITY_STATION);
                infoList.add("-" + Names.PNEUMATIC_HELMET);
                infoList.add("-" + Names.AIR_CANNON);
                infoList.add("-" + Names.AIR_COMPRESSOR);
                infoList.add("-" + Names.CHARGING_STATION);
                infoList.add("-" + Names.ELEVATOR);
                infoList.add("-" + Names.PRESSURE_CHAMBER);
                infoList.add("-" + Names.VACUUM_PUMP);
                infoList.add("-" + Names.ASSEMBLY_CONTROLLER);
                infoList.add("-" + Names.UV_LIGHT_BOX);*/
        }
    }

}
