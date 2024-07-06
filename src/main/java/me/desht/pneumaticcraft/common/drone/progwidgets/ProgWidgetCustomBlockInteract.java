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
import me.desht.pneumaticcraft.api.drone.ICustomBlockInteract;
import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.api.registry.PNCRegistries;
import me.desht.pneumaticcraft.common.drone.ai.DroneAICustomBlockInteract;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.DyeColor;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class ProgWidgetCustomBlockInteract extends ProgWidgetInventoryBase {
    public static final MapCodec<ProgWidgetCustomBlockInteract> CODEC = RecordCodecBuilder.mapCodec(builder ->
            invParts(builder).apply(builder, ProgWidgetCustomBlockInteract::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ProgWidgetCustomBlockInteract> STREAM_CODEC = StreamCodec.composite(
            PositionFields.STREAM_CODEC, ProgWidget::getPosition,
            InvBaseFields.STREAM_CODEC, ProgWidgetInventoryBase::invBaseFields,
            ProgWidgetCustomBlockInteract::new
    );

    private ICustomBlockInteract interactor;
    private ProgWidgetType<?> customType = null;

    public ProgWidgetCustomBlockInteract(PositionFields pos, InvBaseFields invBaseFields) {
        super(pos, invBaseFields);
    }

    public ProgWidgetCustomBlockInteract() {
        super(PositionFields.DEFAULT, InvBaseFields.DEFAULT);
    }

    @Override
    public IProgWidget copyWidget() {
        return Util.make(new ProgWidgetCustomBlockInteract(getPosition(), invBaseFields().copy()), w -> w.setInteractor(interactor));
    }

    public ProgWidgetCustomBlockInteract setInteractor(ICustomBlockInteract interactor) {
        this.interactor = interactor;
        return this;
    }

    @Override
    public ProgWidgetType<?> getType() {
        if (customType == null) {
            customType = PNCRegistries.PROG_WIDGETS_REGISTRY.get(RL(interactor.getID()));
            Objects.requireNonNull(customType);
        }
        return customType;
    }

    @Override
    public Optional<? extends IProgWidget> copy(HolderLookup.Provider provider) {
        return super.copy(provider)
                .filter(w -> w instanceof ProgWidgetCustomBlockInteract)
                .map(w -> ((ProgWidgetCustomBlockInteract) w).setInteractor(interactor));
    }

    @Override
    public ResourceLocation getTexture() {
        return interactor.getTexture();
    }

    @Override
    public Goal getWidgetAI(IDrone drone, IProgWidget widget) {
        return new DroneAICustomBlockInteract(drone, (ProgWidgetInventoryBase) widget, interactor);
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgetTypes.AREA.get());
    }

    @Override
    public DyeColor getColor() {
        return interactor.getColor();
    }

}
