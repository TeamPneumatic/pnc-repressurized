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

package me.desht.pneumaticcraft.client.gui.remote.config;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import me.desht.pneumaticcraft.api.remote.BaseSettings;
import me.desht.pneumaticcraft.api.remote.IRemoteWidget;
import me.desht.pneumaticcraft.api.remote.WidgetSettings;
import me.desht.pneumaticcraft.client.gui.AbstractPneumaticCraftScreen;
import me.desht.pneumaticcraft.client.gui.remote.RemoteEditorScreen;
import me.desht.pneumaticcraft.client.gui.widget.*;
import me.desht.pneumaticcraft.common.variables.GlobalVariableHelper;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public abstract class AbstractRemoteConfigScreen<R extends IRemoteWidget> extends AbstractPneumaticCraftScreen {
    protected final R remoteWidget;
    final RemoteEditorScreen guiRemote;
    protected WidgetTextField labelField, tooltipField;
    protected WidgetComboBox enableField;
    protected WidgetTextFieldNumber xValueField, yValueField, zValueField;
    protected WidgetButtonExtended enableVarTypeButton;
    private boolean playerGlobalEnableVar;

    public AbstractRemoteConfigScreen(R remoteWidget, RemoteEditorScreen guiRemote) {
        super(Component.translatable(remoteWidget.getTranslationKey()));

        this.remoteWidget = remoteWidget;
        this.guiRemote = guiRemote;
        xSize = 183;
        ySize = 202;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(graphics, mouseX, mouseY, partialTicks);
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.GUI_WIDGET_OPTIONS;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void init() {
        super.init();

        GlobalVariableHelper variableHelper = GlobalVariableHelper.getInstance();

        BaseSettings baseSettings = remoteWidget.baseSettings();
        playerGlobalEnableVar = baseSettings.enableVariable().isEmpty() || baseSettings.enableVariable().startsWith("#");

        addLabel(xlate("pneumaticcraft.gui.remote.enable"), guiLeft + 10, guiTop + 150);
        addLabel(title, width / 2, guiTop + 5, WidgetLabel.Alignment.CENTRE);
        enableVarTypeButton = new WidgetButtonExtended(guiLeft + 10, guiTop + 158, 12, 14, variableHelper.getVarPrefix(playerGlobalEnableVar),
                b -> togglePlayerGlobalEnable()).setTooltipKey("pneumaticcraft.gui.remote.varType.tooltip");
        addRenderableWidget(enableVarTypeButton);

        if (remoteWidget.hasConfigurableText()) {
            addLabel(xlate("pneumaticcraft.gui.remote.text"), guiLeft + 10, guiTop + 20);
            addLabel(xlate("pneumaticcraft.gui.remote.tooltip"), guiLeft + 10, guiTop + 46);
        }

        addLabel(xlate("pneumaticcraft.gui.remote.enableValue"), guiLeft + 10, guiTop + 175);
        addLabel(Component.literal("X:"), guiLeft + 10, guiTop + 186);
        addLabel(Component.literal("Y:"), guiLeft + 67, guiTop + 186);
        addLabel(Component.literal("Z:"), guiLeft + 124, guiTop + 186);

        enableField = new WidgetComboBox(font, guiLeft + 23, guiTop + 159, 147);
        enableField.setElements(extractVarnames(playerGlobalEnableVar));
        enableField.setValue(variableHelper.stripVarPrefix(baseSettings.enableVariable()));
        enableField.setTooltip(Tooltip.create(xlate("pneumaticcraft.gui.remote.enable.tooltip")));
        addRenderableWidget(enableField);

        Component valueTooltip = xlate("pneumaticcraft.gui.remote.enableValue.tooltip");

        xValueField = new WidgetTextFieldNumber(font, guiLeft + 20, guiTop + 184, 38);
        xValueField.setValue(baseSettings.enablingValue().getX());
        xValueField.setTooltip(Tooltip.create(valueTooltip));
        addRenderableWidget(xValueField);

        yValueField = new WidgetTextFieldNumber(font, guiLeft + 78, guiTop + 184, 38);
        yValueField.setValue(baseSettings.enablingValue().getY());
        yValueField.setTooltip(Tooltip.create(valueTooltip));
        addRenderableWidget(yValueField);

        zValueField = new WidgetTextFieldNumber(font, guiLeft + 136, guiTop + 184, 38);
        zValueField.setValue(baseSettings.enablingValue().getZ());
        zValueField.setTooltip(Tooltip.create(valueTooltip));
        addRenderableWidget(zValueField);

        if (remoteWidget.hasConfigurableText()) {
            labelField = new WidgetTextField(font, guiLeft + 10, guiTop + 29, 160);
            labelField.setMaxLength(2048);
            labelField.setValue(toJsonString(remoteWidget.widgetSettings().title()));
            labelField.setTooltip(Tooltip.create(xlate("pneumaticcraft.gui.remote.label.tooltip")));
            addRenderableWidget(labelField);

            tooltipField = new WidgetTextField(font, guiLeft + 10, guiTop + 55, 160);
            tooltipField.setMaxLength(2048);
            tooltipField.setValue(toJsonString(remoteWidget.widgetSettings().tooltip()));
            addRenderableWidget(tooltipField);
        }
    }

    private void togglePlayerGlobalEnable() {
        playerGlobalEnableVar = !playerGlobalEnableVar;
        enableVarTypeButton.setMessage(Component.literal(GlobalVariableHelper.getInstance().getVarPrefix(playerGlobalEnableVar)));
        enableField.setElements(extractVarnames(playerGlobalEnableVar));
    }

    @Override
    public void onClose() {
        IRemoteWidget newWidget = makeUpdatedRemoteWidget();
        if (newWidget != null) {
            guiRemote.updateWidgetFromConfigScreen(newWidget);
        }

        minecraft.setScreen(guiRemote);
    }

    protected R makeUpdatedRemoteWidget() {
        return null;
    }

    protected BaseSettings makeBaseSettings() {
        return new BaseSettings(
                GlobalVariableHelper.getInstance().getPrefixedVar(enableField.getValue(), playerGlobalEnableVar),
                new BlockPos(xValueField.getIntValue(), yValueField.getIntValue(), zValueField.getIntValue())
        );
    }

    protected WidgetSettings makeWidgetSettings() {
        WidgetSettings res = remoteWidget.widgetSettings();
        if (remoteWidget.hasConfigurableText()) {
            res = res.withText(fromJson(labelField.getValue()), fromJson(tooltipField.getValue()));
        }
        return res;
    }

    private Component fromJson(String text) {
        try {
            if (!text.startsWith("\"") && !text.startsWith("{") && !text.startsWith("[")) {
                text = "\"" + text + "\"";
            }
            JsonElement json = JsonParser.parseString(text);
            return ComponentSerialization.CODEC
                    .parse(JsonOps.INSTANCE, json)
                    .getOrThrow(JsonSyntaxException::new);
        } catch (JsonParseException e) {
            return Component.literal("<parse error>: " + e.getMessage());
        }
    }

    private String toJsonString(Component component) {
        JsonElement element = ComponentSerialization.CODEC
                .encodeStart(JsonOps.INSTANCE, component)
                .result()
                .orElse(new JsonPrimitive("<encode error>"));
        String str = element.toString();
        if (str.startsWith("\"") && str.endsWith("\"")) {
            return str.substring(1, str.length() - 1);
        } else {
            return str;
        }
    }

    /**
     * Given an array of prefixed var names, return a corresponding list of var names with a matching prefix character
     * @param playerGlobal true to extract player-global, false for server-global
     * @return a list of unprefixed var names
     */
    protected List<String> extractVarnames(boolean playerGlobal) {
        return guiRemote.getMenu().allKnownGlobalVars().stream()
                .filter(v -> playerGlobal && v.startsWith("#") || !playerGlobal && v.startsWith("%"))
                .map(v -> v.substring(1))
                .toList();
    }
}
