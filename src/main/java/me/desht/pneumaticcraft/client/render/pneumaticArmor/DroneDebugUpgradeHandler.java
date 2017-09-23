package me.desht.pneumaticcraft.client.render.pneumaticArmor;

import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IOptionPage;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.client.gui.pneumaticHelmet.GuiDroneDebuggerOptions;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.item.Itemss;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Set;

public class DroneDebugUpgradeHandler implements IUpgradeRenderHandler {
    private final Set<BlockPos> shownPositions = new HashSet<BlockPos>();

    public Set<BlockPos> getShowingPositions() {
        return shownPositions;
    }

    @Override
    public String getUpgradeName() {
        return "droneDebugger";
    }

    @Override
    public void initConfig() {

    }

    @Override
    public void saveToConfig() {

    }

    @Override
    public void update(EntityPlayer player, int rangeUpgrades) {

    }

    @Override
    public void render3D(float partialTicks) {

    }

    @Override
    public void render2D(float partialTicks, boolean helmetEnabled) {

    }

    @Override
    public IGuiAnimatedStat getAnimatedStat() {
        return null;
    }

    /* @Override
     public boolean isEnabled(ItemStack[] upgradeStacks){
         if(enabledForStacks(upgradeStacks)) {
             return true;
         } else {
             shownPositions.clear(); //TODO 1.8 test
             return false;
         }
     }*/
    @Override
    public Item[] getRequiredUpgrades() {
        return new Item[]{Itemss.upgrades.get(EnumUpgrade.DISPENSER)};
    }

    private static boolean enabledForStacks(ItemStack[] upgradeStacks) {
        for (ItemStack stack : upgradeStacks) {
            if (stack.getItem() == Itemss.upgrades.get(EnumUpgrade.DISPENSER)) return true;
        }
        return false;
    }

    public static boolean enabledForPlayer(EntityPlayer player) {
        ItemStack helmet = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        if (!helmet.isEmpty()) {
            return enabledForStacks(ItemPneumaticArmor.getUpgradeStacks(helmet));
        } else {
            return false;
        }
    }

    @Override
    public float getEnergyUsage(int rangeUpgrades, EntityPlayer player) {
        return 0;
    }

    @Override
    public void reset() {

    }

    @Override
    public IOptionPage getGuiOptionsPage() {
        return new GuiDroneDebuggerOptions(this);
    }

}
