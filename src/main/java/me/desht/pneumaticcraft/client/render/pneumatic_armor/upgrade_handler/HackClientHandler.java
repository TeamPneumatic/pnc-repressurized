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
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.List;
import java.util.Optional;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

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

    @Override
    public Optional<KeyBinding> getInitialKeyBinding() {
        return Optional.empty();
    }

    public static boolean enabledForPlayer(PlayerEntity player) {
        return ItemPneumaticArmor.isPneumaticArmorPiece(player, EquipmentSlotType.HEAD)
                && CommonArmorHandler.getHandlerForPlayer(player).getUpgradeCount(EquipmentSlotType.HEAD, EnumUpgrade.SECURITY) > 0;
    }

    public static void addKeybindTooltip(List<ITextComponent> curInfo) {
        KeyBinding hack = KeyHandler.getInstance().keybindHack;
        if (hack.getKey().getKeyCode() != 0) {
            IFormattableTextComponent str = xlate("pneumaticcraft.armor.hacking.pressToHack", ClientUtils.translateKeyBind(hack));
            curInfo.add(str.mergeStyle(TextFormatting.GOLD));
        }
    }
}
