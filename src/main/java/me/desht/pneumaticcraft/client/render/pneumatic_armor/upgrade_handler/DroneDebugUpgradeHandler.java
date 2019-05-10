package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IOptionPage;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiDroneDebuggerOptions;
import me.desht.pneumaticcraft.common.item.ItemPneumatic;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.item.Itemss;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Set;

public class DroneDebugUpgradeHandler implements IUpgradeRenderHandler {
    private final Set<BlockPos> shownPositions = new HashSet<>();
    private final Set<BlockPos> shownArea = new HashSet<>();

    public Set<BlockPos> getShowingPositions() {
        return shownPositions;
    }

    public Set<BlockPos> getShownArea() {
        return shownArea;
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

    @Override
    public Item[] getRequiredUpgrades() {
        return new Item[]{Itemss.upgrades.get(EnumUpgrade.DISPENSER)};
    }

    public static boolean enabledForPlayer(EntityPlayer player) {
        if (ItemPneumaticArmor.isPneumaticArmorPiece(player, EntityEquipmentSlot.HEAD)) {
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
            return handler.getUpgradeCount(EntityEquipmentSlot.HEAD, EnumUpgrade.DISPENSER) > 0;
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

    @Override
    public EntityEquipmentSlot getEquipmentSlot() {
        return EntityEquipmentSlot.HEAD;
    }
}
