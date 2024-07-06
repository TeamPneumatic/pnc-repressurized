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

package me.desht.pneumaticcraft.client.gui.remote.actionwidget;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.client.gui.RemoteEditorScreen;
import me.desht.pneumaticcraft.client.gui.remote.RemoteVariableOptionScreen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

public abstract class ActionWidgetVariable<W extends AbstractWidget> extends ActionWidget<W> {
    protected static <P extends ActionWidgetVariable<?>> Products.P3<RecordCodecBuilder.Mu<P>, BaseSettings, WidgetSettings, String> varParts(RecordCodecBuilder.Instance<P> pInstance) {
        return baseParts(pInstance).and(Codec.STRING.fieldOf("variableName").forGetter(p -> p.variableName));
    }

    protected String variableName = "";

    public ActionWidgetVariable(BaseSettings baseSettings, WidgetSettings widgetSettings, String variableName) {
        super(baseSettings, widgetSettings);

        this.variableName = variableName;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    @Override
    public Screen createConfigurationGui(RemoteEditorScreen guiRemote) {
        return new RemoteVariableOptionScreen<>(this, guiRemote);
    }

    public abstract void onActionPerformed();

    public void onKeyTyped() {
    }

    public abstract void onVariableChange();
}
