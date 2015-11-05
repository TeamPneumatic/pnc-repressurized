package pneumaticCraft.client.render.pneumaticArmor;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.ChunkPosition;
import net.minecraftforge.common.config.Configuration;
import pneumaticCraft.api.client.IGuiAnimatedStat;
import pneumaticCraft.api.client.pneumaticHelmet.IOptionPage;
import pneumaticCraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import pneumaticCraft.client.gui.pneumaticHelmet.GuiDroneDebuggerOptions;
import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.item.ItemPneumaticArmor;
import pneumaticCraft.common.item.Itemss;

public class DroneDebugUpgradeHandler implements IUpgradeRenderHandler{
    private final Set<ChunkPosition> shownPositions = new HashSet<ChunkPosition>();

    public Set<ChunkPosition> getShowingPositions(){
        return shownPositions;
    }

    @Override
    public String getUpgradeName(){
        return "droneDebugger";
    }

    @Override
    public void initConfig(Configuration config){

    }

    @Override
    public void saveToConfig(){

    }

    @Override
    public void update(EntityPlayer player, int rangeUpgrades){

    }

    @Override
    public void render3D(float partialTicks){

    }

    @Override
    public void render2D(float partialTicks, boolean helmetEnabled){

    }

    @Override
    public IGuiAnimatedStat getAnimatedStat(){
        return null;
    }

    @Override
    public boolean isEnabled(ItemStack[] upgradeStacks){
        if(enabledForStacks(upgradeStacks)) {
            return true;
        } else {
            shownPositions.clear();
            return false;
        }
    }

    private static boolean enabledForStacks(ItemStack[] upgradeStacks){
        for(ItemStack stack : upgradeStacks) {
            if(stack != null && stack.getItem() == Itemss.machineUpgrade && stack.getItemDamage() == ItemMachineUpgrade.UPGRADE_DISPENSER_DAMAGE) return true;
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
        return 0;
    }

    @Override
    public void reset(){

    }

    @Override
    public IOptionPage getGuiOptionsPage(){
        return new GuiDroneDebuggerOptions(this);
    }

}
