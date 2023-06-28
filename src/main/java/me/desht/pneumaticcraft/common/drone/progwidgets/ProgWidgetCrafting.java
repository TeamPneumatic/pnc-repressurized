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
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.drone.ai.DroneAICrafting;
import me.desht.pneumaticcraft.common.recipes.RecipeCache;
import me.desht.pneumaticcraft.common.util.DummyContainer;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetCrafting extends ProgWidget implements ICraftingWidget, ICountWidget, ISidedWidget {
    private static final boolean[] NO_SIDES = new boolean[6];

    private boolean useCount;
    private int count;
    private boolean usingVariables;

    public ProgWidgetCrafting() {
        super(ModProgWidgets.CRAFTING.get());
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
        return ImmutableList.of(ModProgWidgets.ITEM_FILTER.get(), ModProgWidgets.ITEM_FILTER.get(), ModProgWidgets.ITEM_FILTER.get());
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
    public CraftingContainer getCraftingGrid() {
        usingVariables = false;
        CraftingContainer invCrafting = new TransientCraftingContainer(new DummyContainer(), 3, 3);
        for (int y = 0; y < 3; y++) {
            ProgWidgetItemFilter itemFilter = (ProgWidgetItemFilter) getConnectedParameters()[y];
            for (int x = 0; x < 3 && itemFilter != null; x++) {
                if (!itemFilter.getVariable().isEmpty()) usingVariables = true;
                invCrafting.setItem(y * 3 + x, itemFilter.getFilter());
                itemFilter = (ProgWidgetItemFilter) itemFilter.getConnectedParameters()[0];
            }
        }
        return invCrafting;
    }

    public ItemStack getRecipeResult(Level world) {
        CraftingContainer grid = getCraftingGrid();
        return getRecipe(world, grid).map(r -> r.assemble(grid, world.registryAccess())).orElse(ItemStack.EMPTY);
    }

    @Override
    public Optional<CraftingRecipe> getRecipe(Level world, CraftingContainer grid) {
        // no caching if using variables, because the item can change at any item
        return usingVariables ?
                world.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, world) :
                RecipeCache.CRAFTING.getCachedRecipe(world, grid);
    }

    public static Recipe<CraftingContainer> getRecipe(Level world, ICraftingWidget widget) {
        return widget.getRecipe(world, widget.getCraftingGrid()).orElse(null);
    }

    @Override
    public Goal getWidgetAI(IDroneBase drone, IProgWidget widget) {
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

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        if (useCount) tag.putBoolean("useCount", true);
        tag.putInt("count", count);
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        useCount = tag.getBoolean("useCount");
        count = tag.getInt("count");
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buf) {
        super.writeToPacket(buf);
        buf.writeBoolean(useCount);
        buf.writeVarInt(count);
    }

    @Override
    public void readFromPacket(FriendlyByteBuf buf) {
        super.readFromPacket(buf);
        useCount = buf.readBoolean();
        count = buf.readVarInt();
    }
}
