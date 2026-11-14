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

package me.desht.pneumaticcraft.client.gui.tubemodule;

import com.mojang.blaze3d.vertex.*;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTooltipArea;
import me.desht.pneumaticcraft.client.util.GuiVertexRenderState;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdatePressureModule;
import me.desht.pneumaticcraft.common.tubemodules.AbstractRedstoneReceivingModule;
import me.desht.pneumaticcraft.common.tubemodules.AbstractTubeModule;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class PressureGaugeModuleScreen extends AbstractTubeModuleScreen<AbstractTubeModule> {
    private EditBox lowerBoundField;
    private EditBox higherBoundField;
    private int graphLowY;
    private int graphHighY;
    private int graphLeft;
    private int graphRight;
    private Rect2i lowerBoundArea, higherBoundArea;
    private boolean grabLower, grabHigher;

    public static AbstractTubeModuleScreen<?> createGUI(AbstractTubeModule module) {
        return module.advancedConfig ? new PressureGaugeModuleScreen(module) : new SimplePressureGaugeModuleScreen(module);
    }

    public PressureGaugeModuleScreen(AbstractTubeModule module) {
        super(module);
        ySize = 191;
    }

    @Override
    public void init() {
        super.init();
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        addLabel(Component.literal("lower"), guiLeft + 15, guiTop + 33);
        addLabel(Component.literal("bar"), guiLeft + 50, guiTop + 44);
        addLabel(Component.literal("higher"), guiLeft + 140, guiTop + 33);

        addLabel(title, width / 2 - font.width(title) / 2, guiTop + 5);

        lowerBoundField = new EditBox(font, xStart + 15, yStart + 43, 30, 10,
                Component.literal(PneumaticCraftUtils.roundNumberTo(module.lowerBound, 1)));
        lowerBoundField.setResponder(s -> updateBoundFromTextfield(0));
        addRenderableWidget(lowerBoundField);

        higherBoundField = new EditBox(font, xStart + 140, yStart + 43, 30, 10,
                Component.literal(PneumaticCraftUtils.roundNumberTo(module.higherBound, 1)));
        higherBoundField.setResponder(s -> updateBoundFromTextfield(1));
        addRenderableWidget(higherBoundField);

        graphLowY = guiTop + 158;
        graphHighY = guiTop + 98;
        graphLeft = guiLeft + 22;
        graphRight = guiLeft + 172;

        addRenderableWidget(new WidgetTooltipArea(graphLeft - 20, graphHighY, 25, graphLowY - graphHighY, xlate("pneumaticcraft.gui.redstone")));
        addRenderableWidget(new WidgetTooltipArea(graphLeft, graphLowY - 5, graphRight - graphLeft, 25, xlate("pneumaticcraft.gui.threshold")));

        WidgetAnimatedStat stat = new WidgetAnimatedStat(this, xlate("pneumaticcraft.gui.tab.info"), WidgetAnimatedStat.StatIcon.of(Textures.GUI_INFO_LOCATION), xStart, yStart + 5, 0xFF8888FF, null, true);
        stat.setText(xlate("pneumaticcraft.gui.tab.info.tubeModule"));
        stat.setBeveled(true);
        addRenderableWidget(stat);

        WidgetCheckBox advancedMode = new WidgetCheckBox(guiLeft + 6, guiTop + 20, 0xFF404040, xlate("pneumaticcraft.gui.tubeModule.advancedConfig"), b -> {
            module.advancedConfig = b.checked;
            NetworkHandler.sendToServer(PacketUpdatePressureModule.forModule(module));
        }).setChecked(true);
        advancedMode.setTooltip(Tooltip.create(xlate("pneumaticcraft.gui.tubeModule.advancedConfig.tooltip")));
        addRenderableWidget(advancedMode);

        higherBoundArea = new Rect2i(guiLeft + 11, guiTop + 59, 158, 15);
        lowerBoundArea = new Rect2i(guiLeft + 11, guiTop + 73, 158, 15);
    }

    @Override
    protected Identifier getGuiTexture() {
        return Textures.GUI_TUBE_MODULE;
    }

    @Override
    public void drawForeground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        int scrollbarLowerBoundX = (int) (guiLeft + 16 + (157 - 11) * (module.lowerBound / (AbstractTubeModule.MAX_VALUE + 1)));
        int scrollbarHigherBoundX = (int) (guiLeft + 16 + (157 - 11) * (module.higherBound / (AbstractTubeModule.MAX_VALUE + 1)));

        graphics.blit(RenderPipelines.GUI_TEXTURED, getGuiTexture(), scrollbarLowerBoundX, guiTop + 73, 183, 0, 15, 12, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, getGuiTexture(), scrollbarHigherBoundX, guiTop + 59, 183, 0, 15, 12, 256, 256);

        renderGraph(graphics);

        // current redstone input, if applicable
        if (module instanceof AbstractRedstoneReceivingModule) {
            module.onNeighborBlockUpdate();
            graphics.horizontalLine(graphLeft + 4, graphRight, graphHighY + (graphLowY - graphHighY) * (15 - ((AbstractRedstoneReceivingModule) module).getReceivingRedstoneLevel()) / 15, 0xFFFF0000);
            String status = I18n.get("pneumaticcraft.gui.tubeModule.simpleConfig.threshold")
                    + " " + PneumaticCraftUtils.roundNumberTo(((AbstractRedstoneReceivingModule) module).getThreshold(), 1) + " bar";
            graphics.text(font, status, (int) (guiLeft + xSize / 2f - font.width(status) / 2f), guiTop + 175, 0xFF404040, false);
        }

        // the actual graph data
        float lineWidth = GuiVertexRenderState.pixels(1f);
        GuiVertexRenderState.submit(graphics, wr -> {
            for (int i = 0; i < 15; i++) {
                float y1 = graphHighY + (graphLowY - graphHighY) * (15 - i) / 15f;
                float x1 = graphLeft + (graphRight - graphLeft) * module.getThreshold(i) / 30f;
                float y2 = graphHighY + (graphLowY - graphHighY) * (15 - (i + 1)) / 15f;
                float x2 = graphLeft + (graphRight - graphLeft) * module.getThreshold(i + 1) / 30f;
                wr.line(x1, y1, x2, y2, lineWidth, ARGB.colorFromFloat(1f, 0.25f + i * 0.05f, 0f, 0f), ARGB.colorFromFloat(1f, 0.25f + (i + 1) * 0.05f, 0f, 0f));
            }
        });

    }

    private void renderGraph(GuiGraphicsExtractor graphics) {
        graphics.verticalLine(graphLeft, graphHighY, graphLowY, 0xFF303030);
        for (int i = 0; i < 16; i++) {
            boolean longer = i % 5 == 0;
            if (longer) {
                String txt = String.valueOf(i);
                graphics.text(font, txt, graphLeft - 5 - font.width(txt), (int) (graphHighY + (graphLowY - graphHighY) * (15 - i) / 15f - 3), 0xFF303030, false);
                graphics.horizontalLine(graphLeft + 4, graphRight, graphHighY + (graphLowY - graphHighY) * (15 - i) / 15, i == 0 ? 0xFF303030 : 0x33000000);

            }
            graphics.horizontalLine(graphLeft - (longer ? 5 : 3), graphLeft + 3, graphHighY + (graphLowY - graphHighY) * (15 - i) / 15, 0xFF303030);
        }
        for (int i = 0; i < 31; i++) {
            boolean longer = i % 5 == 0;
            if (longer) {
                String txt = String.valueOf(i);
                graphics.text(font, txt, (int) (graphLeft + (graphRight - graphLeft) * i / 30f - font.width(txt) / 2f + 1), graphLowY + 6, 0xFF303030, false);
                graphics.verticalLine(graphLeft + (graphRight - graphLeft) * i / 30, graphHighY, graphLowY - 2, 0x33000000);
            }
            graphics.verticalLine(graphLeft + (graphRight - graphLeft) * i / 30, graphLowY - 3, graphLowY + (longer ? 5 : 3), 0xFF303030);
        }
    }

    private void updateBoundFromTextfield(int fieldId) {
        try {
            float prev;
            switch (fieldId) {
                case 0 -> {
                    prev = module.lowerBound;
                    module.lowerBound = Mth.clamp(Float.parseFloat(lowerBoundField.getValue()), -1, AbstractTubeModule.MAX_VALUE);
                    if (!Mth.equal(module.lowerBound, prev)) {
                        NetworkHandler.sendToServer(PacketUpdatePressureModule.forModule(module));
                    }
                }
                case 1 -> {
                    prev = module.higherBound;
                    module.higherBound = Mth.clamp(Float.parseFloat(higherBoundField.getValue()), -1, AbstractTubeModule.MAX_VALUE);
                    if (!Mth.equal(module.higherBound, prev)) {
                        NetworkHandler.sendToServer(PacketUpdatePressureModule.forModule(module));
                    }
                }
                default -> throw new IllegalArgumentException("unknown field id " + fieldId);
            }
        } catch (NumberFormatException ignored) {
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        if (lowerBoundArea.contains((int)mouseX, (int)mouseY)) {
            module.lowerBound = (float) (mouseX - 6 - (guiLeft + 11)) / (158 - 11) * AbstractTubeModule.MAX_VALUE;
            module.lowerBound = Math.min(Math.max(-1, module.lowerBound), AbstractTubeModule.MAX_VALUE);
            grabLower = true;
            return true;
        } else if (higherBoundArea.contains((int)mouseX, (int)mouseY)) {
            module.higherBound = (float) (mouseX - 6 - (guiLeft + 11)) / (158 - 11) * AbstractTubeModule.MAX_VALUE;
            module.higherBound = Math.min(Math.max(-1, module.higherBound), AbstractTubeModule.MAX_VALUE);
            grabHigher = true;
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        double mouseX = event.x();
        if (grabLower) {
            module.lowerBound = (float) (mouseX - 6 - (guiLeft + 11)) / (158 - 11) * AbstractTubeModule.MAX_VALUE;
            module.lowerBound = Math.min(Math.max(-1, module.lowerBound), AbstractTubeModule.MAX_VALUE);
            return true;
        } else if (grabHigher) {
            module.higherBound = (float) (mouseX - 6 - (guiLeft + 11)) / (158 - 11) * AbstractTubeModule.MAX_VALUE;
            module.higherBound = Math.min(Math.max(-1, module.higherBound), AbstractTubeModule.MAX_VALUE);
            return true;
        } else {
            return super.mouseDragged(event, dx, dy);
        }
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (grabLower) {
            NetworkHandler.sendToServer(PacketUpdatePressureModule.forModule(module));
            grabLower = false;
            return true;
        } else if (grabHigher) {
            NetworkHandler.sendToServer(PacketUpdatePressureModule.forModule(module));
            grabHigher = false;
            return true;
        } else {
            return super.mouseReleased(event);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!module.advancedConfig) minecraft.setScreen(new SimplePressureGaugeModuleScreen(module));

        if (!lowerBoundField.isFocused())
            lowerBoundField.setValue(PneumaticCraftUtils.roundNumberTo(module.lowerBound, 1));
        if (!higherBoundField.isFocused())
            higherBoundField.setValue(PneumaticCraftUtils.roundNumberTo(module.higherBound, 1));
    }
}
