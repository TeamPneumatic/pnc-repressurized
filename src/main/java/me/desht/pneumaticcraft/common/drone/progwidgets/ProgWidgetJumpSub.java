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
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetJumpSub extends ProgWidget implements IJumpBackWidget, IJump {

    public static final MapCodec<ProgWidgetJumpSub> CODEC = RecordCodecBuilder.mapCodec(builder ->
            baseParts(builder).apply(builder, ProgWidgetJumpSub::new));

    private boolean jumpBack;

    public ProgWidgetJumpSub(PositionFields pos) {
        super(pos);
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.JUMP_SUB.get();
    }

    public ProgWidgetJumpSub() {
        super(PositionFields.DEFAULT);
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
    public IProgWidget getOutputWidget(IDrone drone, List<IProgWidget> allWidgets) {
        List<String> locations = getPossibleJumpLocations();
        if (!locations.isEmpty() && !jumpBack) {
            jumpBack = true;
            return ProgWidgetJump.jumpToLabel(drone, allWidgets, locations.getFirst());
        }
        jumpBack = false;
        return super.getOutputWidget(drone, allWidgets);
    }

    @Override
    public ProgWidgetType<?> returnType() {
        return null;
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgetTypes.TEXT.get());
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
