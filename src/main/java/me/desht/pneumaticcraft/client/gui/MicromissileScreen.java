/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTooltipArea;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.MicromissilesItem;
import me.desht.pneumaticcraft.common.item.MicromissilesItem.FireMode;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateMicromissileSettings;
import me.desht.pneumaticcraft.common.util.EntityFilter;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class MicromissileScreen extends AbstractPneumaticCraftScreen {
    private static final Rect2i SELECTOR_BOUNDS = new Rect2i(12, 21, 92, 81);
    private static final int MAX_DIST = SELECTOR_BOUNDS.getWidth();

    // these points are relative to the SELECTOR_BOUNDS box defined above (note positive Y is downwards)
    private static final PointXY TOP_SPEED_PT = new PointXY(46, 1);
    private static final PointXY TURN_SPEED_PT = new PointXY(1, 80);
    private static final PointXY DMG_PT = new PointXY(92, 80);

    private float turnSpeed;
    private float topSpeed;
    private float damage;
    private PointXY point;
    private FireMode fireMode;
    private boolean dragging = false;
    private String entityFilter;
    private int sendTimer = 0;
    private final InteractionHand hand;

    private WidgetTextField textField;
    private WidgetLabel filterLabel;
    private WidgetButtonExtended modeButton;
    private WidgetButtonExtended warningButton;

    private MicromissileScreen(Component title, InteractionHand hand) {
        super(title);
        xSize = 183;
        ySize = 191;

        ItemStack stack = ClientUtils.getClientPlayer().getItemInHand(hand);
        if (stack.getItem() == ModItems.MICROMISSILES.get() && stack.hasTag()) {
            CompoundTag tag = Objects.requireNonNull(stack.getTag());
            topSpeed = tag.getFloat(MicromissilesItem.NBT_TOP_SPEED);
            turnSpeed = tag.getFloat(MicromissilesItem.NBT_TURN_SPEED);
            damage = tag.getFloat(MicromissilesItem.NBT_DAMAGE);
            entityFilter = tag.getString(MicromissilesItem.NBT_FILTER);
            point = new PointXY(tag.getInt(MicromissilesItem.NBT_PX), tag.getInt(MicromissilesItem.NBT_PY));
            fireMode = FireMode.fromString(tag.getString(MicromissilesItem.NBT_FIRE_MODE));
            this.hand = hand;
        } else {
            topSpeed = turnSpeed = damage = 1/3f;
            point = new PointXY(MAX_DIST / 2, MAX_DIST / 4);
            entityFilter = "";
            fireMode = FireMode.SMART;
            this.hand = InteractionHand.MAIN_HAND;
        }
    }

    public static void openGui(Component title, InteractionHand handIn) {
        Minecraft.getInstance().setScreen(new MicromissileScreen(title, handIn));
    }

    @Override
    public void init() {
        super.init();

        Component labelStr = xlate("pneumaticcraft.gui.sentryTurret.targetFilter");
        filterLabel = new WidgetLabel(guiLeft + 12, guiTop + 130, labelStr);
        addRenderableWidget(filterLabel);
        int textBoxX = guiLeft + 12 + font.width(labelStr) + 5;
        int textBoxWidth = xSize - (textBoxX - guiLeft) - 20;
        textField = new WidgetTextField(font, textBoxX, guiTop + 128, textBoxWidth, 10);
        textField.setValue(entityFilter);
        setFocused(textField);
        textField.setResponder(s -> {
            entityFilter = s;
            if (validateEntityFilter(entityFilter)) {
                sendTimer = 5;  // delayed send to reduce packet spam while typing
            }
        });
        addRenderableWidget(textField);

        addRenderableWidget(new WidgetTooltipArea(guiLeft + 42, guiTop + 9, 35, 9, xlate("pneumaticcraft.gui.micromissile.topSpeed")));
        addRenderableWidget(new WidgetTooltipArea(guiLeft + 6, guiTop + 103, 25, 12, xlate("pneumaticcraft.gui.micromissile.turnSpeed")));
        addRenderableWidget(new WidgetTooltipArea(guiLeft + 96, guiTop + 103, 15, 15, xlate("pneumaticcraft.gui.micromissile.damage")));

        Component saveLabel = xlate("pneumaticcraft.gui.micromissile.saveDefault");
        int buttonWidth = font.width(saveLabel) + 10;
        int buttonX = guiLeft + (xSize - buttonWidth) / 2;
        addRenderableWidget(new WidgetButtonExtended(buttonX, guiTop + 160, buttonWidth, 20, saveLabel, b -> sendSettingsToServer(true)));

        modeButton = new WidgetButtonExtended(guiLeft + 123, guiTop + 20, 52, 20, Component.empty(), b -> modeSwitch())
                .setTooltipKey("pneumaticcraft.gui.micromissile.modeTooltip");
        addRenderableWidget(modeButton);

        warningButton = new WidgetButtonExtended(guiLeft + 162, guiTop + 123, 20, 20);
        warningButton.setVisible(false);
        warningButton.setRenderedIcon(Textures.GUI_PROBLEMS_TEXTURE);
        addRenderableWidget(warningButton);

        validateEntityFilter(entityFilter);

        setupWidgets();
    }

    private void modeSwitch() {
        int n = fireMode.ordinal() + 1;
        if (n >= FireMode.values().length) n = 0;
        fireMode = FireMode.values()[n];
        setupWidgets();
        sendSettingsToServer(false);
    }

    private void setupWidgets() {
        textField.setEditable(fireMode == FireMode.SMART);
        filterLabel.setColor(fireMode == FireMode.SMART ? 0xFF404040 : 0xFFAAAAAA);
        modeButton.setMessage(xlate(fireMode.getTranslationKey()));
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float partialTicks) {
        renderBackground(graphics);

        super.render(graphics, x, y, partialTicks);

        if (ClientUtils.isKeyDown(GLFW.GLFW_KEY_F1)) {
            GuiUtils.showPopupHelpScreen(graphics, this, font,
                    GuiUtils.xlateAndSplit("pneumaticcraft.gui.entityFilter.helpText"));
        } else if (textField.isHoveredOrFocused()) {
            String str = I18n.get("pneumaticcraft.gui.entityFilter.holdF1");
            graphics.drawString(font, str, guiLeft + (xSize - font.width(str)) / 2, guiTop + ySize + 5, 0x808080, false);
        }
    }

    @Override
    protected void drawForeground(GuiGraphics graphics, int x, int y, float partialTicks) {
        if (fireMode == FireMode.DUMB) {
            return;
        }

        if (point != null) {
            float px = point.x();
            float py = point.y();
            graphics.pose().pushPose();
            graphics.pose().translate(guiLeft + SELECTOR_BOUNDS.getX(), guiTop + SELECTOR_BOUNDS.getY(), 0);

            // crosshairs
            int size = dragging ? 5 : 3;
            RenderSystem.lineWidth(2);

            BufferBuilder wr = Tesselator.getInstance().getBuilder();
            Matrix4f posMat = graphics.pose().last().pose();
            wr.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
            wr.vertex(posMat, px - size, py, 0).color(32, 32, 32, 255).endVertex();
            wr.vertex(posMat, px + size, py, 0).color(32, 32, 32, 255).endVertex();
            wr.vertex(posMat, px, py - size, 0).color(32, 32, 32, 255).endVertex();
            wr.vertex(posMat, px, py + size, 0).color(32, 32, 32, 255).endVertex();
            Tesselator.getInstance().end();

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.lineWidth(1);
            wr.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
            // speed line
            wr.vertex(posMat, px, py, 0).color(32, 32, 32, 128).endVertex();
            wr.vertex(posMat, SELECTOR_BOUNDS.getWidth() / 2f, 0, 0).color(32, 32, 32, 128).endVertex();
            // turn speed line
            wr.vertex(posMat, px, py, 0).color(32, 32, 32, 128).endVertex();
            wr.vertex(posMat, 0, SELECTOR_BOUNDS.getHeight(), 0).color(32, 32, 32, 128).endVertex();
            // damage line
            wr.vertex(posMat, px, py, 0).color(32, 32, 32, 128).endVertex();
            wr.vertex(posMat, SELECTOR_BOUNDS.getWidth(), SELECTOR_BOUNDS.getHeight(), 0).color(32, 32, 32, 128).endVertex();
            Tesselator.getInstance().end();
            RenderSystem.disableBlend();

            graphics.pose().popPose();
        }

        graphics.pose().pushPose();
        graphics.pose().translate(guiLeft, guiTop, 0);
        graphics.fill(125, 48, 125 + (int) (49 * topSpeed), 54, 0xFF00C000);
        graphics.fill(125, 68, 125 + (int) (49 * turnSpeed), 74, 0xFF00C000);
        graphics.fill(125, 88, 125 + (int) (49 * damage), 94, 0xFF00C000);
        graphics.pose().popPose();
    }

    @Override
    public void tick() {
        super.tick();

        if (sendTimer > 0 && --sendTimer == 0) {
            sendSettingsToServer(false);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.GUI_MICROMISSILE;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (trySetPoint((int) mouseX, (int) mouseY)) {
            dragging = true;
            return true;
        } else {
            return super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        if (dragging) {
            // send updated values to server
            sendSettingsToServer(false);
            dragging = false;
            return true;
        } else {
            return super.mouseReleased(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dx, double dy) {
        if (dragging) {
            trySetPoint((int) mouseX, (int) mouseY);
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, mouseButton, dx, dy);
        }
    }

    private boolean validateEntityFilter(String filter) {
        try {
            warningButton.visible = false;
            warningButton.setTooltipText(Component.empty());
            new EntityFilter(filter);  // syntax check
            return true;
        } catch (Exception e) {
            warningButton.visible = true;
            warningButton.setTooltipText(Component.literal(e.getMessage()).withStyle(ChatFormatting.GOLD));
            return false;
        }
    }

    private boolean trySetPoint(int mouseX, int mouseY) {
        PointXY p = getPoint(mouseX, mouseY);
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

    private void sendSettingsToServer(boolean saveDefault) {
        NetworkHandler.sendToServer(new PacketUpdateMicromissileSettings(topSpeed, turnSpeed, damage, point, entityFilter, fireMode, saveDefault, hand));
    }

    private PointXY getPoint(int mouseX, int mouseY) {
        Rect2i r = new Rect2i(SELECTOR_BOUNDS.getX() + guiLeft, SELECTOR_BOUNDS.getY() + guiTop, SELECTOR_BOUNDS.getWidth(), SELECTOR_BOUNDS.getHeight());

        if (!r.contains(mouseX, mouseY)) {
            return null;
        }

        PointXY p = new PointXY(mouseX - r.getX(), mouseY - r.getY());
        return isPointInTriangle(p, TOP_SPEED_PT, TURN_SPEED_PT, DMG_PT) ? p : null;
    }

    @SuppressWarnings("SameParameterValue")
    private boolean isPointInTriangle(PointXY s, PointXY a, PointXY b, PointXY c) {
        int as_x = s.x() - a.x();
        int as_y = s.y() - a.y();

        boolean s_ab = (b.x() - a.x()) * as_y - (b.y() - a.y()) * as_x > 0;

        if ((c.x() - a.x()) * as_y - (c.y() - a.y()) * as_x > 0 == s_ab) return false;

        return (c.x() - b.x()) * (s.y() - b.y()) - (c.y() - b.y()) * (s.x() - b.x()) > 0 == s_ab;
    }
}
