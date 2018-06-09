package me.desht.pneumaticcraft.client.gui.tubemodule;

import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.GuiCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.IGuiWidget;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTooltipArea;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.block.tubes.TubeModuleRedstoneReceiving;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdatePressureModule;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;

public class GuiPressureModule extends GuiTubeModule {

    private GuiTextField lowerBoundField;
    private GuiTextField higherBoundField;
    private int graphLowY;
    private int graphHighY;
    private int graphLeft;
    private int graphRight;
    private Rectangle lowerBoundArea, higherBoundArea;
    private boolean grabLower, grabHigher;

    public GuiPressureModule(EntityPlayer player, int x, int y, int z) {
        super(player, x, y, z);
        ySize = 191;
    }

    public GuiPressureModule(TubeModule module) {
        super(module);
        ySize = 191;
    }

    @Override
    public void initGui() {
        super.initGui();
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        addLabel("lower", guiLeft + 10, guiTop + 30);
        addLabel("bar", guiLeft + 45, guiTop + 42);
        addLabel("higher", guiLeft + 140, guiTop + 30);

        String title = I18n.format("item." + module.getType() + ".name");
        addLabel(title, width / 2 - fontRenderer.getStringWidth(title) / 2, guiTop + 5);

        lowerBoundField = new GuiTextField(-1, fontRenderer, xStart + 10, yStart + 41, 30, 10);
        lowerBoundField.setText(PneumaticCraftUtils.roundNumberTo(module.lowerBound, 1));
        higherBoundField = new GuiTextField(-1, fontRenderer, xStart + 140, yStart + 41, 30, 10);
        higherBoundField.setText(PneumaticCraftUtils.roundNumberTo(module.higherBound, 1));

        graphLowY = guiTop + 153;
        graphHighY = guiTop + 93;
        graphLeft = guiLeft + 22;
        graphRight = guiLeft + 172;
        addWidget(new WidgetTooltipArea(graphLeft - 20, graphHighY, 25, graphLowY - graphHighY, "gui.redstone"));
        addWidget(new WidgetTooltipArea(graphLeft, graphLowY - 5, graphRight - graphLeft, 25, "gui.threshold"));
        addWidget((IGuiWidget) new GuiAnimatedStat(this, "gui.tab.info", Textures.GUI_INFO_LOCATION, xStart, yStart + 5, 0xFF8888FF, null, true).setText("gui.tab.info.tubeModule"));
        GuiCheckBox advancedMode = new GuiCheckBox(0, guiLeft + 6, guiTop + 15, 0xFF404040, "gui.tubeModule.advancedConfig").setTooltip(I18n.format("gui.tubeModule.advancedConfig.tooltip"));
        advancedMode.checked = true;
        addWidget(advancedMode);

        higherBoundArea = new Rectangle(guiLeft + 11, guiTop + 59, 158, 15);
        lowerBoundArea = new Rectangle(guiLeft + 11, guiTop + 73, 158, 15);
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.GUI_TUBE_MODULE;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        GL11.glDisable(GL11.GL_LIGHTING);

        FMLClientHandler.instance().getClient().getTextureManager().bindTexture(getTexture());
        int scrollbarLowerBoundX = (int) (guiLeft + 16 + (158 - 11) * (module.lowerBound / (module.maxValue + 1)));
        int scrollbarHigherBoundX = (int) (guiLeft + 16 + (158 - 11) * (module.higherBound / (module.maxValue + 1)));

        drawTexturedModalRect(scrollbarLowerBoundX, guiTop + 73, 183, 0, 15, 12);
        drawTexturedModalRect(scrollbarHigherBoundX, guiTop + 59, 183, 0, 15, 12);

        lowerBoundField.drawTextBox();
        higherBoundField.drawTextBox();

        /*
         * Draw graph
         */
        drawVerticalLine(graphLeft, graphHighY, graphLowY, 0xFF000000);
        for (int i = 0; i < 16; i++) {
            boolean longer = i % 5 == 0;
            if (longer) {
                fontRenderer.drawString(i + "", graphLeft - 5 - fontRenderer.getStringWidth(i + ""), graphHighY + (graphLowY - graphHighY) * (15 - i) / 15 - 3, 0xFF000000);
                drawHorizontalLine(graphLeft + 4, graphRight, graphHighY + (graphLowY - graphHighY) * (15 - i) / 15, i == 0 ? 0xFF000000 : 0x33000000);

            }
            drawHorizontalLine(graphLeft - (longer ? 5 : 3), graphLeft + 3, graphHighY + (graphLowY - graphHighY) * (15 - i) / 15, 0xFF000000);
        }
        for (int i = 0; i < 31; i++) {
            boolean longer = i % 5 == 0;
            if (longer) {
                fontRenderer.drawString(i + "", graphLeft + (graphRight - graphLeft) * i / 30 - fontRenderer.getStringWidth(i + "") / 2 + 1, graphLowY + 6, 0xFF000000);
                drawVerticalLine(graphLeft + (graphRight - graphLeft) * i / 30, graphHighY, graphLowY - 2, 0x33000000);
            }
            drawVerticalLine(graphLeft + (graphRight - graphLeft) * i / 30, graphLowY - 3, graphLowY + (longer ? 5 : 3), 0xFF000000);
        }

        /*
         * Draw the current redstone strength
         */
        if (module instanceof TubeModuleRedstoneReceiving) {
            module.onNeighborBlockUpdate();
            drawHorizontalLine(graphLeft + 4, graphRight, graphHighY + (graphLowY - graphHighY) * (15 - ((TubeModuleRedstoneReceiving) module).getReceivingRedstoneLevel()) / 15, 0xFFFF0000);
            String status = "Current threshold: " + PneumaticCraftUtils.roundNumberTo(((TubeModuleRedstoneReceiving) module).getThreshold(), 1) + " bar";
            fontRenderer.drawString(status, guiLeft + xSize / 2 - fontRenderer.getStringWidth(status) / 2, guiTop + 173, 0xFF000000);
        }

        /*
         * Draw the data in the graph 
         */
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableTexture2D();
        GlStateManager.color(0, 0, 0, 1.0f);
        for (int i = 0; i < 16; i++) {
            double y = graphHighY + (graphLowY - graphHighY) * (15 - i) / 15;
            double x = graphLeft + (graphRight - graphLeft) * module.getThreshold(i) / 30;
            bufferBuilder.pos(x, y, 90.0d).color(0.25f + i * 0.05f, 0f, 0f, 1.0f).endVertex();
        }
        Tessellator.getInstance().draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();

    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        boolean wasFocused = lowerBoundField.isFocused();
        lowerBoundField.mouseClicked(mouseX, mouseY, mouseButton);
        if (wasFocused && !lowerBoundField.isFocused()) {
            updateBoundFromTextfield(0);
        }

        wasFocused = higherBoundField.isFocused();
        higherBoundField.mouseClicked(mouseX, mouseY, mouseButton);
        if (wasFocused && !higherBoundField.isFocused()) {
            updateBoundFromTextfield(1);
        }

        if (lowerBoundArea.contains(mouseX, mouseY)) {
            module.lowerBound = (float) (mouseX - 6 - (guiLeft + 11)) / (158 - 11) * module.maxValue;
            module.lowerBound = Math.min(Math.max(-1, module.lowerBound), module.maxValue);
            grabLower = true;
        } else if (higherBoundArea.contains(mouseX, mouseY)) {
            module.higherBound = (float) (mouseX - 6 - (guiLeft + 11)) / (158 - 11) * module.maxValue;
            module.higherBound = Math.min(Math.max(-1, module.higherBound), module.maxValue);
            grabHigher = true;
        }
    }

    private void updateBoundFromTextfield(int fieldId) {
        try {
            switch (fieldId) {
                case 0:
                    module.lowerBound = Float.parseFloat(lowerBoundField.getText());
                    module.lowerBound = Math.max(-1, Math.min(module.lowerBound, module.maxValue));
                    NetworkHandler.sendToServer(new PacketUpdatePressureModule(module, 0, module.lowerBound));
                    break;
                case 1:
                    module.higherBound = Float.parseFloat(higherBoundField.getText());
                    module.higherBound = Math.max(-1, Math.min(module.higherBound, module.maxValue));
                    NetworkHandler.sendToServer(new PacketUpdatePressureModule(module, 1, module.higherBound));
                    break;
                default:
                    throw new IllegalArgumentException("unknown field id " + fieldId);
            }
        } catch (NumberFormatException e) {
            // ignore
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (grabLower) {
            module.lowerBound = (float) (mouseX - 6 - (guiLeft + 11)) / (158 - 11) * module.maxValue;
            module.lowerBound = Math.min(Math.max(-1, module.lowerBound), module.maxValue);
        } else if (grabHigher) {
            module.higherBound = (float) (mouseX - 6 - (guiLeft + 11)) / (158 - 11) * module.maxValue;
            module.higherBound = Math.min(Math.max(-1, module.higherBound), module.maxValue);
        } else {
            super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (grabLower) {
            NetworkHandler.sendToServer(new PacketUpdatePressureModule(module, 0, module.lowerBound));
            grabLower = false;
        } else if (grabHigher) {
            NetworkHandler.sendToServer(new PacketUpdatePressureModule(module, 1, module.higherBound));
            grabHigher = false;
        } else {
            super.mouseReleased(mouseX, mouseY, state);
        }
    }

    @Override
    protected void keyTyped(char key, int keyCode) throws IOException {
        if (lowerBoundField.isFocused() && keyCode != Keyboard.KEY_ESCAPE) {
            lowerBoundField.textboxKeyTyped(key, keyCode);
            if (keyCode == Keyboard.KEY_RETURN) updateBoundFromTextfield(0);
        } else if (higherBoundField.isFocused() && keyCode != Keyboard.KEY_ESCAPE) {
            higherBoundField.textboxKeyTyped(key, keyCode);
            if (keyCode == Keyboard.KEY_RETURN) updateBoundFromTextfield(1);
        } else {
            super.keyTyped(key, keyCode);
        }
    }

    @Override
    public void actionPerformed(IGuiWidget widget) {
        super.actionPerformed(widget);
        if (widget.getID() == 0) {
            module.advancedConfig = ((GuiCheckBox) widget).checked;
            NetworkHandler.sendToServer(new PacketUpdatePressureModule(module, 2, module.advancedConfig ? 1 : 0));
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (!module.advancedConfig) mc.displayGuiScreen(new GuiPressureModuleSimple(module));

        if (!lowerBoundField.isFocused())
            lowerBoundField.setText(PneumaticCraftUtils.roundNumberTo(module.lowerBound, 1));
        if (!higherBoundField.isFocused())
            higherBoundField.setText(PneumaticCraftUtils.roundNumberTo(module.higherBound, 1));
    }
}
