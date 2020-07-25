package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTooltipArea;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ItemMicromissiles;
import me.desht.pneumaticcraft.common.item.ItemMicromissiles.FireMode;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateMicromissileSettings;
import me.desht.pneumaticcraft.common.util.EntityFilter;
import me.desht.pneumaticcraft.common.util.NBTUtils;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiMicromissile extends GuiPneumaticScreenBase {
    private static final Rectangle2d SELECTOR_BOUNDS = new Rectangle2d(12, 21, 92, 81);
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
    private final Hand hand;

    private WidgetTextField textField;
    private WidgetLabel filterLabel;
    private WidgetButtonExtended modeButton;
    private WidgetButtonExtended warningButton;

    private GuiMicromissile(ITextComponent title, Hand hand) {
        super(title);
        xSize = 183;
        ySize = 191;

        ItemStack stack = Minecraft.getInstance().player.getHeldItem(hand);
        if (stack.getItem() == ModItems.MICROMISSILES.get() && stack.hasTag()) {
            topSpeed = NBTUtils.getFloat(stack, ItemMicromissiles.NBT_TOP_SPEED);
            turnSpeed = NBTUtils.getFloat(stack, ItemMicromissiles.NBT_TURN_SPEED);
            damage = NBTUtils.getFloat(stack, ItemMicromissiles.NBT_DAMAGE);
            entityFilter = NBTUtils.getString(stack, ItemMicromissiles.NBT_FILTER);
            point = new PointXY(NBTUtils.getInteger(stack,ItemMicromissiles.NBT_PX), NBTUtils.getInteger(stack, ItemMicromissiles.NBT_PY));
            fireMode = FireMode.fromString(NBTUtils.getString(stack, ItemMicromissiles.NBT_FIRE_MODE));
            this.hand = hand;
        } else {
            topSpeed = turnSpeed = damage = 1/3f;
            point = new PointXY(MAX_DIST / 2, MAX_DIST / 4);
            entityFilter = "";
            fireMode = FireMode.SMART;
            this.hand = Hand.MAIN_HAND;
        }
    }

    public static void openGui(ITextComponent title, Hand handIn) {
        Minecraft.getInstance().displayGuiScreen(new GuiMicromissile(title, handIn));
    }

    @Override
    public void init() {
        super.init();

        ITextComponent labelStr = xlate("pneumaticcraft.gui.sentryTurret.targetFilter");
        filterLabel = new WidgetLabel(guiLeft + 12, guiTop + 130, labelStr);
        addButton(filterLabel);
        int textBoxX = guiLeft + 12 + font.func_238414_a_(labelStr) + 5;
        int textBoxWidth = xSize - (textBoxX - guiLeft) - 20;
        textField = new WidgetTextField(font, textBoxX, guiTop + 128, textBoxWidth, 10);
        textField.setText(entityFilter);
        setListener(textField);
        textField.setFocused2(true);
        textField.setResponder(s -> {
            entityFilter = s;
            if (validateEntityFilter(entityFilter)) {
                sendTimer = 5;  // delayed send to reduce packet spam while typing
            }
        });
        addButton(textField);

        addButton(new WidgetTooltipArea(guiLeft + 42, guiTop + 9, 35, 9, xlate("pneumaticcraft.gui.micromissile.topSpeed")));
        addButton(new WidgetTooltipArea(guiLeft + 6, guiTop + 103, 25, 12, xlate("pneumaticcraft.gui.micromissile.turnSpeed")));
        addButton(new WidgetTooltipArea(guiLeft + 96, guiTop + 103, 15, 15, xlate("pneumaticcraft.gui.micromissile.damage")));

        ITextComponent saveLabel = xlate("pneumaticcraft.gui.micromissile.saveDefault");
        int buttonWidth = font.func_238414_a_(saveLabel) + 10;
        int buttonX = guiLeft + (xSize - buttonWidth) / 2;
        addButton(new WidgetButtonExtended(buttonX, guiTop + 160, buttonWidth, 20, saveLabel, b -> sendSettingsToServer(true)));

        modeButton = new WidgetButtonExtended(guiLeft + 123, guiTop + 20, 52, 20, StringTextComponent.EMPTY, b -> modeSwitch());
        modeButton.setTooltipText(PneumaticCraftUtils.splitStringComponent(I18n.format("pneumaticcraft.gui.micromissile.modeTooltip"), 40));
        addButton(modeButton);

        warningButton = new WidgetButtonExtended(guiLeft + 162, guiTop + 123, 20, 20);
        warningButton.setVisible(false);
        warningButton.setRenderedIcon(Textures.GUI_PROBLEMS_TEXTURE);
        addButton(warningButton);

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
        textField.setEnabled(fireMode == FireMode.SMART);
        filterLabel.setColor(fireMode == FireMode.SMART ? 0xFF404040 : 0xFFAAAAAA);
        modeButton.setMessage(xlate(fireMode.getTranslationKey()));
    }

    @Override
    public void render(MatrixStack matrixStack, int x, int y, float partialTicks) {
        renderBackground(matrixStack);
        super.render(matrixStack, x, y, partialTicks);

        if (ClientUtils.isKeyDown(GLFW.GLFW_KEY_F1)) {
            GuiUtils.showPopupHelpScreen(matrixStack, this, font,
                    PneumaticCraftUtils.splitString(I18n.format("pneumaticcraft.gui.entityFilter.helpText"), 60));
        } else if (textField.isHovered()) {
            String str = I18n.format("pneumaticcraft.gui.entityFilter");
            font.drawString(matrixStack, str, guiLeft + (xSize - font.getStringWidth(str)) / 2f, guiTop + ySize + 5, 0x808080);
        }

        if (fireMode == FireMode.DUMB) {
            return;
        }

        RenderSystem.disableTexture();
        RenderSystem.disableLighting();
        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        if (point != null) {
            float px = point.x;
            float py = point.y;
            matrixStack.push();
            matrixStack.translate(guiLeft + SELECTOR_BOUNDS.getX(), guiTop + SELECTOR_BOUNDS.getY(), 0);

            // crosshairs
            int size = dragging ? 5 : 3;
            RenderSystem.lineWidth(2);

            Matrix4f posMat = matrixStack.getLast().getMatrix();
            wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
            wr.pos(posMat, px - size, py, 0).color(32, 32, 32, 255).endVertex();
            wr.pos(posMat, px + size, py, 0).color(32, 32, 32, 255).endVertex();
            wr.pos(posMat, px, py - size, 0).color(32, 32, 32, 255).endVertex();
            wr.pos(posMat, px, py + size, 0).color(32, 32, 32, 255).endVertex();
            Tessellator.getInstance().draw();

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.lineWidth(1);
            wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
            // speed line
            wr.pos(posMat, px, py, 0).color(32, 32, 32, 128).endVertex();
            wr.pos(posMat, SELECTOR_BOUNDS.getWidth() / 2f, 0, 0).color(32, 32, 32, 128).endVertex();
            // turn speed line
            wr.pos(posMat, px, py, 0).color(32, 32, 32, 128).endVertex();
            wr.pos(posMat, 0, SELECTOR_BOUNDS.getHeight(), 0).color(32, 32, 32, 128).endVertex();
            // damage line
            wr.pos(posMat, px, py, 0).color(32, 32, 32, 128).endVertex();
            wr.pos(posMat, SELECTOR_BOUNDS.getWidth(), SELECTOR_BOUNDS.getHeight(), 0).color(32, 32, 32, 128).endVertex();
            Tessellator.getInstance().draw();
            RenderSystem.disableBlend();

            matrixStack.pop();
        }

        matrixStack.push();
        matrixStack.translate(guiLeft, guiTop, 0);
        fill(matrixStack, 125, 48, 125 + (int) (49 * topSpeed), 54, 0xFF00C000);
        fill(matrixStack, 125, 68, 125 + (int) (49 * turnSpeed), 74, 0xFF00C000);
        fill(matrixStack, 125, 88, 125 + (int) (49 * damage), 94, 0xFF00C000);
        matrixStack.pop();

        RenderSystem.enableLighting();

        RenderSystem.enableTexture();
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
            warningButton.setTooltipText(StringTextComponent.EMPTY);
            new EntityFilter(filter);  // syntax check
            return true;
        } catch (Exception e) {
            warningButton.visible = true;
            warningButton.setTooltipText(new StringTextComponent(e.getMessage()).mergeStyle(TextFormatting.GOLD));
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
        Rectangle2d r = new Rectangle2d(SELECTOR_BOUNDS.getX() + guiLeft, SELECTOR_BOUNDS.getY() + guiTop, SELECTOR_BOUNDS.getWidth(), SELECTOR_BOUNDS.getHeight());

        if (!r.contains(mouseX, mouseY)) {
            return null;
        }

        PointXY p = new PointXY(mouseX - r.getX(), mouseY - r.getY());
        return isPointInTriangle(p, TOP_SPEED_PT, TURN_SPEED_PT, DMG_PT) ? p : null;
    }

    @SuppressWarnings("SameParameterValue")
    private boolean isPointInTriangle(PointXY s, PointXY a, PointXY b, PointXY c) {
        int as_x = s.x - a.x;
        int as_y = s.y - a.y;

        boolean s_ab = (b.x - a.x) * as_y - (b.y - a.y) * as_x > 0;

        if ((c.x - a.x) * as_y - (c.y - a.y) * as_x > 0 == s_ab) return false;

        return (c.x - b.x) * (s.y - b.y) - (c.y - b.y) * (s.x - b.x) > 0 == s_ab;
    }
}
