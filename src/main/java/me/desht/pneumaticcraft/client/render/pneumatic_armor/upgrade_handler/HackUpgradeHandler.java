package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class HackUpgradeHandler implements IUpgradeRenderHandler {

    @Override
    public String getUpgradeID() {
        return "hackingUpgrade";
    }

    @Override
    public void update(PlayerEntity player, int rangeUpgrades) {

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
    public EnumUpgrade[] getRequiredUpgrades() {
        return new EnumUpgrade[] { EnumUpgrade.SECURITY };
    }

    public static boolean enabledForPlayer(PlayerEntity player) {
        if (ItemPneumaticArmor.isPneumaticArmorPiece(player, EquipmentSlotType.HEAD)) {
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
            return handler.getUpgradeCount(EquipmentSlotType.HEAD, EnumUpgrade.SECURITY) > 0;
        }
        return false;
    }

    @Override
    public float getEnergyUsage(int rangeUpgrades, PlayerEntity player) {
        return 0;
    }

    @Override
    public void reset() {

    }

    @Override
    public IOptionPage getGuiOptionsPage(IGuiScreen screen) {
        return null;
    }

    @Override
    public EquipmentSlotType getEquipmentSlot() {
        return EquipmentSlotType.HEAD;
    }

    public static void addKeybindTooltip(List<String> curInfo) {
        if (KeyHandler.getInstance().keybindHack.getKey().getKeyCode() != 0) {
            curInfo.add(TextFormatting.GOLD + "Press [" + KeyHandler.getInstance().keybindHack.getLocalizedName() + "] to hack");
        }
    }
}
