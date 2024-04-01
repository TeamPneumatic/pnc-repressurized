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

package me.desht.pneumaticcraft.client.gui.remote;

import me.desht.pneumaticcraft.client.gui.RemoteEditorScreen;
import me.desht.pneumaticcraft.client.gui.remote.actionwidget.ActionWidgetVariable;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
import me.desht.pneumaticcraft.common.variables.GlobalVariableHelper;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class RemoteVariableOptionScreen<A extends ActionWidgetVariable<?>> extends BasicRemoteOptionScreen<A> {
    private boolean playerGlobal;
    private WidgetButtonExtended varTypeButton;
    private WidgetComboBox variableField;

    public RemoteVariableOptionScreen(A actionWidget, RemoteEditorScreen guiRemote) {
        super(actionWidget, guiRemote);
    }

    @Override
    public void init() {
        super.init();

        addLabel(xlate("pneumaticcraft.gui.progWidget.coordinate.variableName"), guiLeft + 10, guiTop + 70);

        playerGlobal = actionWidget.getVariableName().isEmpty() || actionWidget.getVariableName().startsWith("#");

        varTypeButton = new WidgetButtonExtended(guiLeft + 10, guiTop + 78, 12, 14, GlobalVariableHelper.getVarPrefix(playerGlobal),
                b -> togglePlayerGlobal())
                .setTooltipKey("pneumaticcraft.gui.remote.varType.tooltip");
        addRenderableWidget(varTypeButton);

        variableField = new WidgetComboBox(font, guiLeft + 23, guiTop + 79, 147, font.lineHeight + 3);
        variableField.setElements(GlobalVariableHelper.extractVarnames(guiRemote.getMenu().variables, playerGlobal));
        variableField.setValue(GlobalVariableHelper.stripVarPrefix(actionWidget.getVariableName()));
        variableField.setTooltip(Tooltip.create(xlate("pneumaticcraft.gui.remote.variable.tooltip")));
        addRenderableWidget(variableField);
    }

    @Override
    public void removed() {
        actionWidget.setVariableName(GlobalVariableHelper.getPrefixedVar(variableField.getValue(), playerGlobal));

        super.removed();
    }

    private void togglePlayerGlobal() {
        playerGlobal = !playerGlobal;
        variableField.setElements(GlobalVariableHelper.extractVarnames(guiRemote.getMenu().variables, playerGlobal));
        varTypeButton.setMessage(Component.literal(GlobalVariableHelper.getVarPrefix(playerGlobal)));
    }
}
