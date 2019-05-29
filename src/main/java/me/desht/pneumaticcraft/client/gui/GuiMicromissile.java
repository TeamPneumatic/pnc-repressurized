package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.IGuiWidget;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTooltipArea;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.item.ItemMicromissiles;
import me.desht.pneumaticcraft.common.item.ItemMicromissiles.FireMode;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateMicromissileSettings;
import me.desht.pneumaticcraft.common.util.EntityFilter;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;

public class GuiMicromissile extends GuiPneumaticScreenBase {
    private static final Rectangle SELECTOR_BOUNDS = new Rectangle(12, 21, 92, 81);
    private static final int MAX_DIST = SELECTOR_BOUNDS.width;

    // these points are relative to the SELECTOR_BOUNDS box defined above (note positive Y is downwards)
    private static final Point TOP_SPEED_PT = new Point(46, 1);
    private static final Point TURN_SPEED_PT = new Point(1, 80);
    private static final Point DMG_PT = new Point(92, 80);

    private float turnSpeed;
    private float topSpeed;
    private float damage;
    private Point point;
    private FireMode fireMode;
    private boolean dragging = false;
    private String entityFilter;
    private int sendTimer = 0;

    private WidgetTextField textField;
    private WidgetLabel filterLabel;
    private GuiButtonSpecial modeButton;
    private GuiButtonSpecial warningButton;

    public GuiMicromissile() {
        xSize = 183;
        ySize = 191;

        ItemStack stack = ItemMicromissiles.getHeldMicroMissile(Minecraft.getMinecraft().player);
        if (stack.getItem() == Itemss.MICROMISSILES) {
            if (stack.hasTagCompound()) {
                topSpeed = NBTUtil.getFloat(stack, ItemMicromissiles.NBT_TOP_SPEED);
                turnSpeed = NBTUtil.getFloat(stack, ItemMicromissiles.NBT_TURN_SPEED);
                damage = NBTUtil.getFloat(stack, ItemMicromissiles.NBT_DAMAGE);
                entityFilter = NBTUtil.getString(stack, ItemMicromissiles.NBT_FILTER);
                point = new Point(NBTUtil.getInteger(stack,ItemMicromissiles.NBT_PX), NBTUtil.getInteger(stack, ItemMicromissiles.NBT_PY));
                fireMode = FireMode.fromString(NBTUtil.getString(stack, ItemMicromissiles.NBT_FIRE_MODE));
            } else {
                topSpeed = turnSpeed = damage = 1/3f;
                point = new Point(MAX_DIST / 2, MAX_DIST / 4);
                entityFilter = "";
                fireMode = FireMode.SMART;
            }
        }
    }

    @Override
    public void initGui() {
        super.initGui();

        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;

        String labelStr = I18n.format("gui.sentryTurret.targetFilter");
        filterLabel = new WidgetLabel(guiLeft + 12, guiTop + 130, labelStr);
        addWidget(filterLabel);
        int textBoxX = guiLeft + 12 + fr.getStringWidth(labelStr) + 5;
        int textBoxWidth = xSize - (textBoxX - guiLeft) - 20;
        textField = new WidgetTextField(Minecraft.getMinecraft().fontRenderer, textBoxX, guiTop + 128, textBoxWidth, 10);
        textField.setText(entityFilter);
        addWidget(textField);
        textField.setFocused(true);

        addWidget(new WidgetTooltipArea(guiLeft + 42, guiTop + 9, 35, 9, "gui.micromissile.topSpeed"));
        addWidget(new WidgetTooltipArea(guiLeft + 6, guiTop + 103, 25, 12, "gui.micromissile.turnSpeed"));
        addWidget(new WidgetTooltipArea(guiLeft + 96, guiTop + 103, 15, 15, "gui.micromissile.damage"));

        String saveLabel = I18n.format("gui.micromissile.saveDefault");
        int buttonWidth = fr.getStringWidth(saveLabel) + 10;
        int buttonX = guiLeft + (xSize - buttonWidth) / 2;
        buttonList.add(new GuiButtonSpecial(1, buttonX, guiTop + 160, buttonWidth, 20, saveLabel));

        modeButton = new GuiButtonSpecial(2, guiLeft + 123, guiTop + 20, 52, 20, "");
        modeButton.setTooltipText("gui.micromissile.modeTooltip");
        buttonList.add(modeButton);

        warningButton = new GuiButtonSpecial(3, guiLeft + 162, guiTop + 123, 20, 20, "");
        warningButton.setVisible(false);
        warningButton.setRenderedIcon(Textures.GUI_PROBLEMS_TEXTURE);
        buttonList.add(warningButton);

        validateEntityFilter(entityFilter);

        setupWidgets();
    }

    private void setupWidgets() {
        textField.setEnabled(fireMode == FireMode.SMART);
        filterLabel.setColor(fireMode == FireMode.SMART ? 0xFF404040 : 0xFFAAAAAA);
        modeButton.displayString = I18n.format("gui.micromissile.mode." + fireMode.toString());
    }

    @Override
    public void drawScreen(int x, int y, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(x, y, partialTicks);

        if (Keyboard.isKeyDown(Keyboard.KEY_F1)) {
            GuiUtils.showPopupHelpScreen(this, fontRenderer,
                    PneumaticCraftUtils.convertStringIntoList(I18n.format("gui.entityFilter.helpText"), 60));
        } else if (textField.getBounds().contains(x, y)) {
            String str = I18n.format("gui.entityFilter");
            fontRenderer.drawString(str, guiLeft + (xSize - fontRenderer.getStringWidth(str)) / 2, guiTop + ySize + 5, 0x808080);
        }

        if (fireMode == FireMode.DUMB) {
            return;
        }

        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        if (point != null) {
            double px = point.getX();
            double py = point.getY();
            RenderUtils.glColorHex(0x2020A0, 255);
            GlStateManager.pushMatrix();
            GlStateManager.translate(guiLeft + SELECTOR_BOUNDS.x, guiTop + SELECTOR_BOUNDS.y, 0);
            BufferBuilder wr = Tessellator.getInstance().getBuffer();

            // crosshairs
            int size = dragging ? 5 : 3;
            GlStateManager.glLineWidth(2);
            GlStateManager.glBegin(GL11.GL_LINES);
            GL11.glVertex2d(px - size, py);
            GL11.glVertex2d(px + size, py);
            GlStateManager.glEnd();
            GlStateManager.glBegin(GL11.GL_LINES);
            GL11.glVertex2d(px, py - size);
            GL11.glVertex2d(px, py + size);
            GlStateManager.glEnd();

            GL11.glEnable(GL11.GL_LINE_STIPPLE);
            GL11.glLineStipple(1, (short)0xAAAA);
            // speed line
            GlStateManager.glBegin(GL11.GL_LINES);
            GL11.glVertex2d(px, py);
            GL11.glVertex2d(SELECTOR_BOUNDS.width / 2.0, 0);
            GlStateManager.glEnd();
            // turn speed line
            GlStateManager.glBegin(GL11.GL_LINES);
            GL11.glVertex2d(px, py);
            GL11.glVertex2d(0, SELECTOR_BOUNDS.height);
            GlStateManager.glEnd();
            // damage line
            GlStateManager.glBegin(GL11.GL_LINES);
            GL11.glVertex2d(px, py);
            GL11.glVertex2d(SELECTOR_BOUNDS.width, SELECTOR_BOUNDS.height);
            GlStateManager.glEnd();

            GL11.glDisable(GL11.GL_LINE_STIPPLE);
            GlStateManager.popMatrix();
            RenderUtils.glColorHex(0xffffff, 255);
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(guiLeft, guiTop, 0);
        GlStateManager.glLineWidth(10);
        GL11.glEnable(GL11.GL_LINE_STIPPLE);
        GL11.glLineStipple(1, (short)0xFEFE);
        RenderUtils.glColorHex(0x00C000, 255);
        GlStateManager.glBegin(GL11.GL_LINES);
        GL11.glVertex2i(125, 51);
        GL11.glVertex2i(125 + (int) (49 * topSpeed), 51);
        GlStateManager.glEnd();
        GlStateManager.glBegin(GL11.GL_LINES);
        GL11.glVertex2i(125, 71);
        GL11.glVertex2i(125 + (int) (49 * turnSpeed), 71);
        GlStateManager.glEnd();
        GlStateManager.glBegin(GL11.GL_LINES);
        GL11.glVertex2i(125, 91);
        GL11.glVertex2i(125 + (int) (49 * damage), 91);
        GlStateManager.glEnd();
        GlStateManager.popMatrix();

        GL11.glDisable(GL11.GL_LINE_STIPPLE);
        GlStateManager.glLineWidth(1);
        GlStateManager.enableLighting();

        GlStateManager.enableTexture2D();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        if (sendTimer > 0 && --sendTimer == 0) {
            sendSettingsToServer(false);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.GUI_MICROMISSILE;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (trySetPoint(mouseX, mouseY)) {
            dragging = true;
        } else {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (dragging) {
            trySetPoint(mouseX, mouseY);
        } else {
            super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (dragging) {
            // send updated values to server
            sendSettingsToServer(false);
            dragging = false;
        } else {
            super.mouseReleased(mouseX, mouseY, state);
        }
    }

    @Override
    public void onKeyTyped(IGuiWidget widget) {
        if (widget instanceof WidgetTextField) {
            // entity filter updated
            entityFilter = ((WidgetTextField) widget).getText();
            if (validateEntityFilter(entityFilter)) {
                sendTimer = 5;  // delayed send to reduce packet spam while typing
            }
        }
    }

    private boolean validateEntityFilter(String filter) {
        try {
            warningButton.visible = false;
            warningButton.setTooltipText("");
            EntityFilter f = new EntityFilter(filter);  // syntax check
            return true;
        } catch (Exception e) {
            warningButton.visible = true;
            warningButton.setTooltipText(TextFormatting.GOLD + e.getMessage());
            return false;
        }
    }

    private boolean trySetPoint(int mouseX, int mouseY) {
        Point p = getPoint(mouseX, mouseY);
        if (p != null) {
            double dSpeed = MAX_DIST - p.distance(TOP_SPEED_PT);
            double dTurnSpd = MAX_DIST - p.distance(TURN_SPEED_PT);
            double dDamage = MAX_DIST - p.distance(DMG_PT);
            double total = dSpeed + dTurnSpd + dDamage;
            topSpeed = (float) (dSpeed / total);
            turnSpeed = (float) (dTurnSpd / total);
            damage = (float) (dDamage / total);
            point = p;
            return true;
        }
        return false;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 1) {
            sendSettingsToServer(true);
        } else if (button.id == 2) {
            int n = fireMode.ordinal() + 1;
            if (n >= FireMode.values().length) n = 0;
            fireMode = FireMode.values()[n];
            setupWidgets();
            sendSettingsToServer(false);
        } else {
            super.actionPerformed(button);
        }
    }

    private void sendSettingsToServer(boolean saveDefault) {
        NetworkHandler.sendToServer(new PacketUpdateMicromissileSettings(topSpeed, turnSpeed, damage, point, entityFilter, fireMode, saveDefault));
    }

    private Point getPoint(int mouseX, int mouseY) {
        Rectangle r = new Rectangle(SELECTOR_BOUNDS.x + guiLeft, SELECTOR_BOUNDS.y + guiTop, SELECTOR_BOUNDS.width, SELECTOR_BOUNDS.height);

        if (!r.contains(mouseX, mouseY)) {
            return null;
        }

        Point p = new Point(mouseX - r.x, mouseY - r.y);
        return isPointInTriangle(p, TOP_SPEED_PT, TURN_SPEED_PT, DMG_PT) ? p : null;
    }

    private boolean isPointInTriangle(Point s, Point a, Point b, Point c) {
        int as_x = s.x - a.x;
        int as_y = s.y - a.y;

        boolean s_ab = (b.x - a.x) * as_y - (b.y - a.y) * as_x > 0;

        if ((c.x - a.x) * as_y - (c.y - a.y) * as_x > 0 == s_ab) return false;

        return (c.x - b.x) * (s.y - b.y) - (c.y - b.y) * (s.x - b.x) > 0 == s_ab;
    }
}
