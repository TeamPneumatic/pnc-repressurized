package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.inventory.ContainerSentryTurret;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateTextfield;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySentryTurret;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class GuiSentryTurret extends GuiPneumaticContainerBase<ContainerSentryTurret,TileEntitySentryTurret> {
    private WidgetTextField entityFilter;

    public GuiSentryTurret(ContainerSentryTurret container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_SENTRY_TURRET;
    }

    @Override
    public void init() {
        super.init();
        addButton(entityFilter = new WidgetTextField(font, guiLeft + 80, guiTop + 63, 70, font.FONT_HEIGHT));
        entityFilter.setMaxStringLength(256);
        entityFilter.setText(te.getText(0));
        entityFilter.setResponder(s -> sendDelayed(5));
        entityFilter.setFocused2(true);
        setFocused(entityFilter);
    }

    @Override
    protected void doDelayedAction() {
        te.setText(0, entityFilter.getText());
        NetworkHandler.sendToServer(new PacketUpdateTextfield(te, 0));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);

        font.drawString(I18n.format("pneumaticcraft.gui.sentryTurret.ammo"), 80, 19, 0x404040);
        font.drawString(I18n.format("pneumaticcraft.gui.sentryTurret.targetFilter"), 80, 53, 0x404040);
        if (ClientUtils.isKeyDown(GLFW.GLFW_KEY_F1)) {
            GuiUtils.showPopupHelpScreen(this, font,
                    PneumaticCraftUtils.splitString(I18n.format("pneumaticcraft.gui.entityFilter.helpText"), 60));
        } else if (x >= guiLeft + 76 && y >= guiTop + 51 && x <= guiLeft + 153 && y <= guiTop + 74) {
            // cursor inside the entity filter area
            String str = I18n.format("pneumaticcraft.gui.entityFilter");
            font.drawString(str, (xSize - font.getStringWidth(str)) / 2f, ySize + 5, 0x808080);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            minecraft.player.closeScreen();
        }

        return entityFilter.keyPressed(keyCode, scanCode, modifiers)
                || entityFilter.canWrite()
                || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void addProblems(List<String> curInfo) {
        super.addProblems(curInfo);

        boolean hasAmmo = false;
        for (int i = 0; i < te.getPrimaryInventory().getSlots(); i++) {
            if (!te.getPrimaryInventory().getStackInSlot(i).isEmpty()) {
                hasAmmo = true;
                break;
            }
        }
        if (!hasAmmo) curInfo.add("pneumaticcraft.gui.tab.problems.sentryTurret.noAmmo");
    }
}
