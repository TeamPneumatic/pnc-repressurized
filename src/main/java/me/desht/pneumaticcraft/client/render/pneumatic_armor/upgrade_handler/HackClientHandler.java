package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.option_screens.HackOptions;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class HackClientHandler extends IArmorUpgradeClientHandler.AbstractHandler {
    public HackClientHandler() {
        super(ArmorUpgradeRegistry.getInstance().hackHandler);
    }

    @Override
    public void tickClient(ICommonArmorHandler armorHandler) {
    }

    @Override
    public void render3D(MatrixStack matrixStack, IRenderTypeBuffer buffer, float partialTicks) {
    }

    @Override
    public void render2D(MatrixStack matrixStack, float partialTicks, boolean helmetEnabled) {
    }

    @Override
    public void reset() {
    }

    @Override
    public IOptionPage getGuiOptionsPage(IGuiScreen screen) {
        return new HackOptions(screen, this);
    }

    public static boolean enabledForPlayer(PlayerEntity player) {
        return ItemPneumaticArmor.isPneumaticArmorPiece(player, EquipmentSlotType.HEAD)
                && CommonArmorHandler.getHandlerForPlayer(player).getUpgradeCount(EquipmentSlotType.HEAD, EnumUpgrade.SECURITY) > 0;
    }

    public static void addKeybindTooltip(List<String> curInfo) {
        if (KeyHandler.getInstance().keybindHack.getKey().getKeyCode() != 0) {
            String s = ClientUtils.translateKeyBind(KeyHandler.getInstance().keybindHack);
            curInfo.add(TextFormatting.GOLD + "Press [" + s + "] to hack");
        }
    }
}
