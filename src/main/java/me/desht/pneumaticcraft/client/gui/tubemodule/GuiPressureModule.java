package me.desht.pneumaticcraft.client.gui.tubemodule;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTooltipArea;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.block.tubes.TubeModuleRedstoneReceiving;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdatePressureModule;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.opengl.GL11;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiPressureModule extends GuiTubeModule<TubeModule> {

    private TextFieldWidget lowerBoundField;
    private TextFieldWidget higherBoundField;
    private int graphLowY;
    private int graphHighY;
    private int graphLeft;
    private int graphRight;
    private Rectangle2d lowerBoundArea, higherBoundArea;
    private boolean grabLower, grabHigher;

    public GuiPressureModule(BlockPos pos) {
        super(pos);
        ySize = 191;
    }

    GuiPressureModule(TubeModule module) {
        super(module);
        ySize = 191;
    }

    @Override
    public void init() {
        super.init();
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        addLabel(new StringTextComponent("lower"), guiLeft + 15, guiTop + 33);
        addLabel(new StringTextComponent("bar"), guiLeft + 50, guiTop + 44);
        addLabel(new StringTextComponent("higher"), guiLeft + 140, guiTop + 33);

//        String titleText = title.getFormattedText();
        addLabel(title, width / 2 - font.func_238414_a_(title) / 2, guiTop + 5);

        lowerBoundField = new TextFieldWidget(font, xStart + 15, yStart + 43, 30, 10,
                new StringTextComponent(PneumaticCraftUtils.roundNumberTo(module.lowerBound, 1)));
        lowerBoundField.setResponder(s -> updateBoundFromTextfield(0));
        addButton(lowerBoundField);

        higherBoundField = new TextFieldWidget(font, xStart + 140, yStart + 43, 30, 10,
                new StringTextComponent(PneumaticCraftUtils.roundNumberTo(module.higherBound, 1)));
        higherBoundField.setResponder(s -> updateBoundFromTextfield(1));
        addButton(higherBoundField);

        graphLowY = guiTop + 158;
        graphHighY = guiTop + 98;
        graphLeft = guiLeft + 22;
        graphRight = guiLeft + 172;

        addButton(new WidgetTooltipArea(graphLeft - 20, graphHighY, 25, graphLowY - graphHighY,
                xlate("pneumaticcraft.gui.redstone")));
        addButton(new WidgetTooltipArea(graphLeft, graphLowY - 5, graphRight - graphLeft, 25,
                xlate("pneumaticcraft.gui.threshold")));

        WidgetAnimatedStat stat = new WidgetAnimatedStat(this, xlate("pneumaticcraft.gui.tab.info"), WidgetAnimatedStat.StatIcon.of(Textures.GUI_INFO_LOCATION), xStart, yStart + 5, 0xFF8888FF, null, true);
        stat.setText("pneumaticcraft.gui.tab.info.tubeModule");
        stat.setBeveled(true);
        addButton(stat);

        WidgetCheckBox advancedMode = new WidgetCheckBox(guiLeft + 6, guiTop + 20, 0xFF404040, xlate("pneumaticcraft.gui.tubeModule.advancedConfig"), b -> {
            module.advancedConfig = b.checked;
            NetworkHandler.sendToServer(new PacketUpdatePressureModule(module));
        }).setTooltip(xlate("pneumaticcraft.gui.tubeModule.advancedConfig.tooltip"));
        advancedMode.checked = true;
        addButton(advancedMode);

        higherBoundArea = new Rectangle2d(guiLeft + 11, guiTop + 59, 158, 15);
        lowerBoundArea = new Rectangle2d(guiLeft + 11, guiTop + 73, 158, 15);
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.GUI_TUBE_MODULE;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        RenderSystem.disableLighting();

        minecraft.getTextureManager().bindTexture(getTexture());
        int scrollbarLowerBoundX = (int) (guiLeft + 16 + (158 - 11) * (module.lowerBound / (TubeModule.MAX_VALUE + 1)));
        int scrollbarHigherBoundX = (int) (guiLeft + 16 + (158 - 11) * (module.higherBound / (TubeModule.MAX_VALUE + 1)));

        blit(matrixStack, scrollbarLowerBoundX, guiTop + 73, 183, 0, 15, 12);
        blit(matrixStack, scrollbarHigherBoundX, guiTop + 59, 183, 0, 15, 12);

        renderGraph(matrixStack);

        /*
         * Draw the current redstone strength
         */
        if (module instanceof TubeModuleRedstoneReceiving) {
            module.onNeighborBlockUpdate();
            hLine(matrixStack, graphLeft + 4, graphRight, graphHighY + (graphLowY - graphHighY) * (15 - ((TubeModuleRedstoneReceiving) module).getReceivingRedstoneLevel()) / 15, 0xFFFF0000);
            String status = I18n.format("pneumaticcraft.gui.tubeModule.simpleConfig.threshold")
                    + " " + PneumaticCraftUtils.roundNumberTo(((TubeModuleRedstoneReceiving) module).getThreshold(), 1) + " bar";
            font.drawString(matrixStack, status, guiLeft + xSize / 2f - font.getStringWidth(status) / 2f, guiTop + 175, 0xFF404040);
        }

        /*
         * Draw the data in the graph 
         */
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.disableTexture();
        RenderSystem.color4f(0, 0, 0, 1.0f);
        Matrix4f posMat = matrixStack.getLast().getMatrix();
        for (int i = 0; i < 16; i++) {
            float y = graphHighY + (graphLowY - graphHighY) * (15 - i) / 15f;
            float x = graphLeft + (graphRight - graphLeft) * module.getThreshold(i) / 30f;
            bufferBuilder.pos(posMat, x, y, 90f).color(0.25f + i * 0.05f, 0f, 0f, 1.0f).endVertex();
        }
        Tessellator.getInstance().draw();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();

    }

    private void renderGraph(MatrixStack matrixStack) {
        vLine(matrixStack, graphLeft, graphHighY, graphLowY, 0xFF000000);
        for (int i = 0; i < 16; i++) {
            boolean longer = i % 5 == 0;
            if (longer) {
                font.drawString(matrixStack, i + "", graphLeft - 5 - font.getStringWidth(i + ""), graphHighY + (graphLowY - graphHighY) * (15 - i) / 15f - 3, 0xFF000000);
                hLine(matrixStack, graphLeft + 4, graphRight, graphHighY + (graphLowY - graphHighY) * (15 - i) / 15, i == 0 ? 0xFF000000 : 0x33000000);

            }
            hLine(matrixStack, graphLeft - (longer ? 5 : 3), graphLeft + 3, graphHighY + (graphLowY - graphHighY) * (15 - i) / 15, 0xFF000000);
        }
        for (int i = 0; i < 31; i++) {
            boolean longer = i % 5 == 0;
            if (longer) {
                font.drawString(matrixStack, i + "", graphLeft + (graphRight - graphLeft) * i / 30f - font.getStringWidth(i + "") / 2f + 1, graphLowY + 6, 0xFF000000);
                vLine(matrixStack, graphLeft + (graphRight - graphLeft) * i / 30, graphHighY, graphLowY - 2, 0x33000000);
            }
            vLine(matrixStack, graphLeft + (graphRight - graphLeft) * i / 30, graphLowY - 3, graphLowY + (longer ? 5 : 3), 0xFF000000);
        }
    }

    private void updateBoundFromTextfield(int fieldId) {
        try {
            float prev;
            switch (fieldId) {
                case 0:
                    prev = module.lowerBound;
                    module.lowerBound = MathHelper.clamp(Float.parseFloat(lowerBoundField.getText()), -1, TubeModule.MAX_VALUE);
                    if (!MathHelper.epsilonEquals(module.lowerBound, prev)) {
                        NetworkHandler.sendToServer(new PacketUpdatePressureModule(module));
                    }
                    break;
                case 1:
                    prev = module.higherBound;
                    module.higherBound = MathHelper.clamp(Float.parseFloat(higherBoundField.getText()), -1, TubeModule.MAX_VALUE);
                    if (!MathHelper.epsilonEquals(module.higherBound, prev)) {
                        NetworkHandler.sendToServer(new PacketUpdatePressureModule(module));
                    }
                    break;
                default:
                    throw new IllegalArgumentException("unknown field id " + fieldId);
            }
        } catch (NumberFormatException ignored) {
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (lowerBoundArea.contains((int)mouseX, (int)mouseY)) {
            module.lowerBound = (float) (mouseX - 6 - (guiLeft + 11)) / (158 - 11) * TubeModule.MAX_VALUE;
            module.lowerBound = Math.min(Math.max(-1, module.lowerBound), TubeModule.MAX_VALUE);
            grabLower = true;
            return true;
        } else if (higherBoundArea.contains((int)mouseX, (int)mouseY)) {
            module.higherBound = (float) (mouseX - 6 - (guiLeft + 11)) / (158 - 11) * TubeModule.MAX_VALUE;
            module.higherBound = Math.min(Math.max(-1, module.higherBound), TubeModule.MAX_VALUE);
            grabHigher = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double dx, double dy) {
        if (grabLower) {
            module.lowerBound = (float) (mouseX - 6 - (guiLeft + 11)) / (158 - 11) * TubeModule.MAX_VALUE;
            module.lowerBound = Math.min(Math.max(-1, module.lowerBound), TubeModule.MAX_VALUE);
            return true;
        } else if (grabHigher) {
            module.higherBound = (float) (mouseX - 6 - (guiLeft + 11)) / (158 - 11) * TubeModule.MAX_VALUE;
            module.higherBound = Math.min(Math.max(-1, module.higherBound), TubeModule.MAX_VALUE);
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, clickedMouseButton, dx, dy);
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        if (grabLower) {
            NetworkHandler.sendToServer(new PacketUpdatePressureModule(module));
            grabLower = false;
            return true;
        } else if (grabHigher) {
            NetworkHandler.sendToServer(new PacketUpdatePressureModule(module));
            grabHigher = false;
            return true;
        } else {
            return super.mouseReleased(mouseX, mouseY, state);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!module.advancedConfig) minecraft.displayGuiScreen(new GuiPressureModuleSimple(module));

        if (!lowerBoundField.isFocused())
            lowerBoundField.setText(PneumaticCraftUtils.roundNumberTo(module.lowerBound, 1));
        if (!higherBoundField.isFocused())
            higherBoundField.setText(PneumaticCraftUtils.roundNumberTo(module.higherBound, 1));
    }
}
