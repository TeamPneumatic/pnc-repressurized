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

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetColorSelector;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTooltipArea;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSyncThermostatModuleToServer;
import me.desht.pneumaticcraft.common.network.PacketUpdatePressureModule;
import me.desht.pneumaticcraft.common.tubemodules.AbstractTubeModule;
import me.desht.pneumaticcraft.common.tubemodules.ThermostatModule;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;

import org.joml.Matrix4f;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ThermostatModuleScreen extends AbstractTubeModuleScreen<ThermostatModule> {
    private int color;
    WidgetColorSelector colorSelector;
    private EditBox lowerBoundField;
    private EditBox higherBoundField;
    private int graphLowY;
    private int graphHighY;
    private int graphLeft;
    private int graphRight;
    private Rect2i lowerBoundArea, higherBoundArea;
    private boolean grabLower, grabHigher;

    public static AbstractTubeModuleScreen<?> createGUI(AbstractTubeModule module) {
        return module.isUpgraded() ? new ThermostatModuleScreen((ThermostatModule)module)
            : new SimpleThermostatModuleScreen((ThermostatModule)module);
    }

    public ThermostatModuleScreen(ThermostatModule module) {
        super(module);
        ySize = 191;
    }

    @Override
    public void init() {
        super.init();

        color = module.getColorChannel();

        addLabel(getTitle(), guiLeft + xSize / 2, guiTop + 5, WidgetLabel.Alignment.CENTRE);

        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        int x = guiLeft + 10;
        int y = guiTop + 22;

        WidgetLabel colorLabel;
        addRenderableWidget(colorLabel = new WidgetLabel(x, y, xlate("pneumaticcraft.gui.tubeModule.channel")));

        x = guiLeft + 10 + colorLabel.getWidth() + 7;
        colorSelector = new WidgetColorSelector(x, y-2, w -> {
                color = w.getColor().getId();
                module.setColorChannel(color);
                NetworkHandler.sendToServer(PacketSyncThermostatModuleToServer.create(module));
        })
            .withInitialColor(DyeColor.byId(color));
        addRenderableWidget(colorSelector);

        x = guiLeft + 10 + colorLabel.getWidth() + 7 + colorSelector.getWidth() + 10;
        WidgetCheckBox advancedMode = new WidgetCheckBox(x, y, 0xFF404040, Component.literal("Advanced"), b -> {
                module.advancedConfig = b.checked;
                NetworkHandler.sendToServer(PacketUpdatePressureModule.forModule(module));
        }).setChecked(true);
        advancedMode.setTooltip(Tooltip.create(xlate("pneumaticcraft.gui.tubeModule.advancedConfig.tooltip")));
        addRenderableWidget(advancedMode);

        addLabel(Component.literal("lower"), guiLeft + 15, guiTop + 33);
        addLabel(Component.literal("°C"), guiLeft + 60, guiTop + 44);
        addLabel(Component.literal("higher"), guiLeft + 140, guiTop + 33);

        addLabel(title, width / 2 - font.width(title) / 2, guiTop + 5);

        lowerBoundField = new EditBox(font, xStart + 15, yStart + 43, 40, 10,
                Component.literal(PneumaticCraftUtils.roundNumberTo(module.lowerBound, 0)));
        lowerBoundField.setResponder(s -> updateBoundFromTextfield(0));
        addRenderableWidget(lowerBoundField);

        higherBoundField = new EditBox(font, xStart + 130, yStart + 43, 40, 10,
                Component.literal(PneumaticCraftUtils.roundNumberTo(module.higherBound, 0)));
        higherBoundField.setResponder(s -> updateBoundFromTextfield(1));
        addRenderableWidget(higherBoundField);

        graphLowY = guiTop + 158;
        graphHighY = guiTop + 98;
        graphLeft = guiLeft + 22;
        graphRight = guiLeft + 172;

        addRenderableWidget(new WidgetTooltipArea(graphLeft - 20, graphHighY, 25, graphLowY - graphHighY, xlate("pneumaticcraft.gui.redstone")));
        addRenderableWidget(new WidgetTooltipArea(graphLeft, graphLowY - 5, graphRight - graphLeft, 25, xlate("pneumaticcraft.gui.temperature")));

        WidgetAnimatedStat stat = new WidgetAnimatedStat(this, xlate("pneumaticcraft.gui.tab.info"),
            WidgetAnimatedStat.StatIcon.of(Textures.GUI_INFO_LOCATION), xStart, yStart + 5, 0xFF8888FF, null, true);
        stat.setText(xlate("pneumaticcraft.gui.tab.info.tubeModule"));
        stat.setBeveled(true);
        addRenderableWidget(stat);

        higherBoundArea = new Rect2i(guiLeft + 11, guiTop + 59, 158, 15);
        lowerBoundArea = new Rect2i(guiLeft + 11, guiTop + 73, 158, 15);
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.GUI_TUBE_MODULE;
    }

    @Override
    public void drawForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int scrollbarLowerBoundX = temperatureToX((int)module.lowerBound);
        int scrollbarHigherBoundX = temperatureToX((int)module.higherBound);

        graphics.blit(getTexture(), scrollbarLowerBoundX, guiTop + 73, 183, 0, 15, 12);
        graphics.blit(getTexture(), scrollbarHigherBoundX, guiTop + 59, 183, 0, 15, 12);

        renderGraph(graphics);

        // Update bounds and advancedConfig state
        NetworkHandler.sendToServer(PacketUpdatePressureModule.forModule(module));
        // Update channel
        NetworkHandler.sendToServer(PacketSyncThermostatModuleToServer.create(module));

        graphics.hLine(graphLeft + 4, graphRight, graphHighY + (graphLowY - graphHighY) * (15 - module.getInputLevel()) / 15, 0xFFFF0000);
        String status = I18n.get("pneumaticcraft.gui.tubeModule.simpleConfig.temperature")
            + " " + PneumaticCraftUtils.roundNumberTo(module.getTemperature(), 0) + " °C";
        graphics.drawString(font, status, guiLeft + xSize / 2f - font.width(status) / 2f, guiTop + 175, 0xFF404040, false);

        // the actual graph data
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        Matrix4f posMat = graphics.pose().last().pose();
        float temperatureRange = ThermostatModule.MAX_VALUE - ThermostatModule.MIN_VALUE;
        for (int i = 0; i < 16; i++) {
            float y = graphHighY + (graphLowY - graphHighY) * (15 - i) / 15f;
            float x = graphLeft + (graphRight - graphLeft) * (module.getTemperatureForLevel(i) - ThermostatModule.MIN_VALUE) / temperatureRange;
            bufferBuilder.vertex(posMat, x, y, 90f).color(0.25f + i * 0.05f, 0f, 0f, 1.0f).endVertex();
        }
        Tesselator.getInstance().end();
        RenderSystem.disableBlend();

    }

    private void renderGraph(GuiGraphics graphics) {
        graphics.vLine(graphLeft, graphHighY, graphLowY, 0xFF303030);
        for (int i = 0; i < 16; i++) {
            boolean longer = i % 5 == 0;
            if (longer) {
                String txt = String.valueOf(i);
                graphics.drawString(font, txt, graphLeft - 5 - font.width(txt), graphHighY + (graphLowY - graphHighY) * (15 - i) / 15f - 3, 0xFF303030, false);
                graphics.hLine(graphLeft + 4, graphRight, graphHighY + (graphLowY - graphHighY) * (15 - i) / 15, i == 0 ? 0xFF303030 : 0x33000000);

            }
            graphics.hLine(graphLeft - (longer ? 5 : 3), graphLeft + 3, graphHighY + (graphLowY - graphHighY) * (15 - i) / 15, 0xFF303030);
        }

        int[] temps = { ThermostatModule.MIN_VALUE, 0, 1000, ThermostatModule.MAX_VALUE };
        int[] adjusts = { -3, 1, 1, -5 };
        for (int i = 0; i < 4; i++) {
            int offset = (int)((temps[i] - ThermostatModule.MIN_VALUE) * (100f / (ThermostatModule.MAX_VALUE - ThermostatModule.MIN_VALUE)));
            String txt = String.valueOf(temps[i]);
            graphics.drawString(font, txt, graphLeft + (graphRight - graphLeft) * offset / 100f - font.width(txt) / 2f + adjusts[i],
                graphLowY + 6, 0xFF303030, false);
            graphics.vLine(graphLeft + (graphRight - graphLeft) * offset / 100, graphHighY, graphLowY - 2, 0x33000000);
            graphics.vLine(graphLeft + (graphRight - graphLeft) * offset / 100, graphLowY - 5, graphLowY + 3, 0xFF303030);
        }
    }

    private void updateBoundFromTextfield(int fieldId) {
        try {
            float prev;
            switch (fieldId) {
                case 0 -> {
                    prev = module.lowerBound;
                    module.lowerBound = Mth.clamp(Integer.parseInt(lowerBoundField.getValue()),
                                                  ThermostatModule.MIN_VALUE, ThermostatModule.MAX_VALUE);
                    if (!Mth.equal(module.lowerBound, prev)) {
                        NetworkHandler.sendToServer(PacketUpdatePressureModule.forModule(module));
                    }
                }
                case 1 -> {
                    prev = module.higherBound;
                    module.higherBound = Mth.clamp(Integer.parseInt(higherBoundField.getValue()),
                                                   ThermostatModule.MIN_VALUE, ThermostatModule.MAX_VALUE);
                    if (!Mth.equal(module.higherBound, prev)) {
                        NetworkHandler.sendToServer(PacketUpdatePressureModule.forModule(module));
                    }
                }
                default -> throw new IllegalArgumentException("unknown field id " + fieldId);
            }
        } catch (NumberFormatException ignored) {
        }
    }

    private int xToTemperature(double mouseX) {
        float sliderWidth = 158 - 12;
        int sliderLeft = guiLeft + 11;
        float temperatureRange = ThermostatModule.MAX_VALUE - ThermostatModule.MIN_VALUE;
        float xNormalized = Math.max(0f, Math.min(1f, ((float)mouseX - sliderLeft) / sliderWidth));
        int temperature = (int)(xNormalized * temperatureRange) + ThermostatModule.MIN_VALUE;
        return temperature;
    }

    private int temperatureToX(int temperature) {
        float sliderWidth = 158 - 12;
        int sliderLeft = guiLeft + 11;
        float temperatureRange = ThermostatModule.MAX_VALUE - ThermostatModule.MIN_VALUE;
        float tempNormalized = Math.max(ThermostatModule.MIN_VALUE,
            Math.min(ThermostatModule.MAX_VALUE, ((float)temperature - ThermostatModule.MIN_VALUE) / temperatureRange));
        int x = (int)(tempNormalized * sliderWidth) + sliderLeft;
        return x;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (!colorSelector.isExpanded() && lowerBoundArea.contains((int)mouseX, (int)mouseY)) {
            module.lowerBound = xToTemperature(mouseX - 7);
            grabLower = true;
            return true;
        } else if (!colorSelector.isExpanded() && higherBoundArea.contains((int)mouseX, (int)mouseY)) {
            module.higherBound = xToTemperature(mouseX - 7);
            grabHigher = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double dx, double dy) {
        if (grabLower) {
            module.lowerBound = xToTemperature(mouseX - 7);
            return true;
        } else if (grabHigher) {
            module.higherBound = xToTemperature(mouseX - 7);
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, clickedMouseButton, dx, dy);
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        if (grabLower) {
            NetworkHandler.sendToServer(PacketUpdatePressureModule.forModule(module));
            grabLower = false;
            return true;
        } else if (grabHigher) {
            NetworkHandler.sendToServer(PacketUpdatePressureModule.forModule(module));
            grabHigher = false;
            return true;
        } else {
            return super.mouseReleased(mouseX, mouseY, state);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!module.advancedConfig) minecraft.setScreen(new SimpleThermostatModuleScreen(module));

        if (!lowerBoundField.isFocused())
            lowerBoundField.setValue(PneumaticCraftUtils.roundNumberTo(module.lowerBound, 0));
        if (!higherBoundField.isFocused())
            higherBoundField.setValue(PneumaticCraftUtils.roundNumberTo(module.higherBound, 0));
    }
}
