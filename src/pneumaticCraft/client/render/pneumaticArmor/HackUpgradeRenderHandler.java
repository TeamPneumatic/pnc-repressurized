package pneumaticCraft.client.render.pneumaticArmor;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import pneumaticCraft.api.client.IGuiAnimatedStat;
import pneumaticCraft.api.client.pneumaticHelmet.IOptionPage;
import pneumaticCraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.item.ItemPneumaticArmor;
import pneumaticCraft.common.item.Itemss;

public class HackUpgradeRenderHandler implements IUpgradeRenderHandler{

    @Override
    public String getUpgradeName(){
        return "hackingUpgrade";
    }

    @Override
    public void initConfig(Configuration config){
        // TODO Auto-generated method stub

    }

    @Override
    public void saveToConfig(){
        // TODO Auto-generated method stub

    }

    @Override
    public void update(EntityPlayer player, int rangeUpgrades){
        // TODO Auto-generated method stub

    }

    @Override
    public void render3D(float partialTicks){
        // TODO Auto-generated method stub

    }

    @Override
    public void render2D(float partialTicks, boolean helmetEnabled){
        // TODO Auto-generated method stub

    }

    @Override
    public IGuiAnimatedStat getAnimatedStat(){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isEnabled(ItemStack[] upgradeStacks){
        return enabledForStacks(upgradeStacks);
    }

    private static boolean enabledForStacks(ItemStack[] upgradeStacks){
        for(ItemStack stack : upgradeStacks) {
            if(stack != null && stack.getItem() == Itemss.machineUpgrade && stack.getItemDamage() == ItemMachineUpgrade.UPGRADE_SECURITY) return true;
        }
        return false;
    }

    public static boolean enabledForPlayer(EntityPlayer player){
        ItemStack helmet = player.getCurrentArmor(3);
        if(helmet != null) {
            return enabledForStacks(ItemPneumaticArmor.getUpgradeStacks(helmet));
        } else {
            return false;
        }
    }

    @Override
    public float getEnergyUsage(int rangeUpgrades, EntityPlayer player){
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void reset(){
        // TODO Auto-generated method stub

    }

    @Override
    public IOptionPage getGuiOptionsPage(){
        // TODO Auto-generated method stub
        return null;
    }

}
