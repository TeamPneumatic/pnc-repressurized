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

package me.desht.pneumaticcraft.common.drone.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

// import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
// import java.util.concurrent.ThreadLocalRandom;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetJumpSub extends ProgWidget implements IJumpBackWidget, IJump {
    private boolean jump_back;

    public ProgWidgetJumpSub() {
        super(ModProgWidgets.JUMP_SUB.get());
    }

    @Override
    public void addErrors(List<Component> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (getConnectedParameters()[0] == null) curInfo.add(xlate("pneumaticcraft.gui.progWidget.label.error.noJumpLocation"));
    }

    @Override
    public boolean hasStepInput() {
        return true;
    }

    @Override
    public boolean hasStepOutput() {
        return true;
    }

    @Override
    public IProgWidget getOutputWidget(IDroneBase drone, List<IProgWidget> allWidgets) {
        List<String> locations = getPossibleJumpLocations();
        if (locations.size() > 0 && !jump_back) {
            jump_back = true;
            return ProgWidgetJump.jumpToLabel(drone, allWidgets, locations.get(0));
        }
        jump_back = false;
        return super.getOutputWidget(drone, allWidgets);
    }

    @Override
    public ProgWidgetType<?> returnType() {
        return null;
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgets.TEXT.get());
    }

    @Override
    protected boolean hasBlacklist() {
        return false;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_JUMP_SUB;
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.MEDIUM;
    }

    @Override
    public List<String> getPossibleJumpLocations() {
        ProgWidgetText jumpLabel = (ProgWidgetText) getConnectedParameters()[0];
        if (jumpLabel != null) {
            return Collections.singletonList(jumpLabel.string);
        }
        return Collections.emptyList();
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.WHITE;
    }
}
