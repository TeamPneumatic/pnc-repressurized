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
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.drone.ai.DroneAICrafting;
import me.desht.pneumaticcraft.common.recipes.VanillaRecipeCache;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetCrafting extends ProgWidget implements ICraftingWidget, ICountWidget, ISidedWidget {
    public static final MapCodec<ProgWidgetCrafting> CODEC = RecordCodecBuilder.mapCodec(builder ->
            baseParts(builder).and(builder.group(
                            Codec.BOOL.optionalFieldOf("use_count", false).forGetter(ProgWidgetCrafting::useCount),
                            Codec.INT.optionalFieldOf("count", 1).forGetter(ProgWidgetCrafting::getCount)
                    )
            ).apply(builder, ProgWidgetCrafting::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ProgWidgetCrafting> STREAM_CODEC = StreamCodec.composite(
            PositionFields.STREAM_CODEC, ProgWidget::getPosition,
            ByteBufCodecs.BOOL, ProgWidgetCrafting::useCount,
            ByteBufCodecs.VAR_INT, ProgWidgetCrafting::getCount,
            ProgWidgetCrafting::new
    );

    private static final boolean[] NO_SIDES = new boolean[6];

    private boolean useCount;
    private int count;
    private boolean usingVariables;

    public ProgWidgetCrafting(PositionFields pos, boolean useCount, int count) {
        super(pos);

        this.useCount = useCount;
        this.count = count;
    }

    public ProgWidgetCrafting() {
        this(PositionFields.DEFAULT, false, 1);
    }

    @Override
    public IProgWidget copyWidget() {
        return new ProgWidgetCrafting(getPosition(), useCount, count);
    }

    @Override
    public void addErrors(List<Component> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);

        getCraftingGrid(); // to set up usingVariables

        if (!usingVariables && getRecipeResult(ClientUtils.getClientLevel()).isEmpty()) {
            curInfo.add(xlate("pneumaticcraft.gui.progWidget.crafting.error.noCraftingRecipe"));
        }
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.CRAFTING.get();
    }

    @Override
    public void getTooltip(List<Component> curTooltip) {
        super.getTooltip(curTooltip);

        ItemStack stack = getRecipeResult(ClientUtils.getClientLevel());
        if (!stack.isEmpty()) {
            curTooltip.add(stack.getHoverName().copy().withStyle(ChatFormatting.YELLOW));
        }
        if (useCount()) {
            curTooltip.add(xlate("pneumaticcraft.gui.progWidget.inventory.usingCount", getCount()));
        }
    }

    @Override
    public boolean hasStepInput() {
        return true;
    }

    @Override
    public ProgWidgetType<?> returnType() {
        return null;
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgetTypes.ITEM_FILTER.get(), ModProgWidgetTypes.ITEM_FILTER.get(), ModProgWidgetTypes.ITEM_FILTER.get());
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.PURPLE;
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.EASY;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_CRAFTING;
    }

    @Override
    protected boolean hasBlacklist() {
        return false;
    }

    @Override
    public CraftingInput getCraftingGrid() {
        usingVariables = false;

        List<ItemStack> stacks = new ArrayList<>(Collections.nCopies(9, ItemStack.EMPTY));
        for (int y = 0; y < 3; y++) {
            ProgWidgetItemFilter itemFilter = (ProgWidgetItemFilter) getConnectedParameters()[y];
            for (int x = 0; x < 3 && itemFilter != null; x++) {
                if (!itemFilter.getVariable().isEmpty()) {
                    usingVariables = true;
                }
                stacks.set(y * 3 + x, itemFilter.getFilter());
                itemFilter = (ProgWidgetItemFilter) itemFilter.getConnectedParameters()[0];
            }
        }
        return CraftingInput.of(3, 3, stacks);
    }

    public ItemStack getRecipeResult(Level world) {
        CraftingInput grid = getCraftingGrid();
        return getRecipe(world, grid).map(r -> r.assemble(grid, world.registryAccess())).orElse(ItemStack.EMPTY);
    }

    @Override
    public Optional<CraftingRecipe> getRecipe(Level world, CraftingInput grid) {
        // no caching if using variables, because the item can change at any item
        return usingVariables ?
                world.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, world).flatMap(holder -> Optional.of(holder.value())) :
                VanillaRecipeCache.CRAFTING.getCachedRecipe(world, grid);
    }

//    public static Recipe<CraftingInput> getRecipe(Level world, ICraftingWidget widget) {
//        return widget.getRecipe(world, widget.getCraftingGrid()).orElse(null);
//    }

    @Override
    public Goal getWidgetAI(IDrone drone, IProgWidget widget) {
        return new DroneAICrafting(drone, (ICraftingWidget) widget);
    }

    @Override
    public void setSides(boolean[] sides) {
    }

    @Override
    public boolean[] getSides() {
        return NO_SIDES;
    }

    @Override
    public boolean useCount() {
        return useCount;
    }

    @Override
    public void setUseCount(boolean useCount) {
        this.useCount = useCount;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public void setCount(int count) {
        this.count = count;
    }

}
