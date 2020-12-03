package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.inventory.ContainerSentryTurret;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateTextfield;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySentryTurret;
import me.desht.pneumaticcraft.common.util.EntityFilter;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

import java.util.Collections;
import java.util.List;

public class GuiSentryTurret extends GuiPneumaticContainerBase<ContainerSentryTurret,TileEntitySentryTurret> {
    private WidgetTextField entityFilter;
    private WidgetButtonExtended errorButton;
    private String prevFilterText = "";

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
        entityFilter.setFocused2(true);
        setListener(entityFilter);

        addButton(errorButton = new WidgetButtonExtended(guiLeft + 155, guiTop + 52, 16, 16, StringTextComponent.EMPTY));
        errorButton.setRenderedIcon(Textures.GUI_PROBLEMS_TEXTURE).setVisible(false);
    }

    @Override
    public void tick() {
        if (firstUpdate) {
            // setting the filter value in the textfield on init() isn't reliable; might not be sync'd in time
            prevFilterText = te.getText(0);
            entityFilter.setText(te.getText(0));
            entityFilter.setResponder(this::onEntityFilterChanged);
        }

        super.tick();

        errorButton.visible = errorButton.hasTooltip();
    }

    private void onEntityFilterChanged(String newText) {
        try {
            if (!newText.equals(prevFilterText)) {
                new EntityFilter(newText);
                errorButton.setTooltipText(Collections.emptyList());
                sendDelayed(5);
                prevFilterText = newText;
            }
        } catch (IllegalArgumentException e) {
            errorButton.setTooltipText(new StringTextComponent(e.getMessage()));
        }
    }

    @Override
    protected void doDelayedAction() {
        te.setText(0, entityFilter.getText());
        NetworkHandler.sendToServer(new PacketUpdateTextfield(te, 0));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {
        super.drawGuiContainerForegroundLayer(matrixStack, x, y);

        font.drawString(matrixStack, I18n.format("pneumaticcraft.gui.sentryTurret.ammo"), 80, 19, 0x404040);
        font.drawString(matrixStack, I18n.format("pneumaticcraft.gui.sentryTurret.targetFilter"), 80, 53, 0x404040);
        if (ClientUtils.isKeyDown(GLFW.GLFW_KEY_F1)) {
            GuiUtils.showPopupHelpScreen(matrixStack, this, font,
                    GuiUtils.xlateAndSplit("pneumaticcraft.gui.entityFilter.helpText"));
        } else if (x >= guiLeft + 76 && y >= guiTop + 51 && x <= guiLeft + 153 && y <= guiTop + 74) {
            // cursor inside the entity filter area
            String str = I18n.format("pneumaticcraft.gui.entityFilter.holdF1");
            font.drawString(matrixStack, str, (xSize - font.getStringWidth(str)) / 2f, ySize + 5, 0x808080);
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
    protected void addProblems(List<ITextComponent> curInfo) {
        super.addProblems(curInfo);

        boolean hasAmmo = false;
        for (int i = 0; i < te.getPrimaryInventory().getSlots(); i++) {
            if (!te.getPrimaryInventory().getStackInSlot(i).isEmpty()) {
                hasAmmo = true;
                break;
            }
        }
        if (!hasAmmo) curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.sentryTurret.noAmmo"));
    }
}
