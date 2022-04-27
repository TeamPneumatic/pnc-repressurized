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

package me.desht.pneumaticcraft.common.thirdparty.jei.transfer;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.inventory.ContainerProgrammer;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketProgrammerUpdate;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetCrafting;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetItemFilter;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.*;

public class ProgrammerTransferHandler implements IRecipeTransferHandler<ContainerProgrammer> {
    private static GuiProgrammer programmerScreen = null;

    private final IRecipeTransferHandlerHelper transferHelper;

    public ProgrammerTransferHandler(IRecipeTransferHandlerHelper transferHelper) {
        this.transferHelper = transferHelper;
    }

    @Override
    public Class<ContainerProgrammer> getContainerClass() {
        return ContainerProgrammer.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(ContainerProgrammer container, Object recipe, IRecipeLayout recipeLayout, PlayerEntity player, boolean maxTransfer, boolean doTransfer) {
        if (programmerScreen != null) {
            IProgWidget craftingWidget = recipe instanceof ICraftingRecipe ? findSuitableCraftingWidget(programmerScreen) : null;

            List<ProgWidgetItemFilter> params = makeFilterWidgets(craftingWidget, recipe, recipeLayout, craftingWidget == null && !maxTransfer);
            if (params.isEmpty()) return transferHelper.createInternalError();
            if (doTransfer) {
                TileEntityProgrammer programmer = programmerScreen.te;
                programmer.progWidgets.addAll(params);
                NetworkHandler.sendToServer(new PacketProgrammerUpdate(programmer));
                TileEntityProgrammer.updatePuzzleConnections(programmer.progWidgets);
            }
            return null;
        }
        return transferHelper.createInternalError();
    }

    private List<ProgWidgetItemFilter> makeFilterWidgets(IProgWidget craftingWidget, Object recipe, IRecipeLayout recipeLayout, boolean uniqueStacks) {
        IGuiItemStackGroup stacks = recipeLayout.getItemStacks();
        List<ItemStack> l = new ArrayList<>();
        Set<Item> itemsSeen = new HashSet<>();
        for (IGuiIngredient<ItemStack> ingr : stacks.getGuiIngredients().values()) {
            ItemStack stack = ingr.getDisplayedIngredient() == null ? ItemStack.EMPTY : ingr.getDisplayedIngredient();
            if (ingr.isInput() && (recipe instanceof ShapedRecipe || !stack.isEmpty())) {
                l.add(uniqueStacks && itemsSeen.contains(stack.getItem()) ? ItemStack.EMPTY : stack);
                itemsSeen.add(stack.getItem());
            }
        }

        if (recipe instanceof ShapedRecipe) {
            if (l.size() != 9) {
                // expecting a standard 3x3 grid for shaped crafting
                return Collections.emptyList();
            }

            // if recipe has empty ingredients in left column, shift left (possibly twice)
            for (int n = 0; n < 2; n++) {
                if (l.get(0).isEmpty() && l.get(3).isEmpty() && l.get(6).isEmpty()) {
                    for (int i = 0; i < l.size(); i++) {
                        if (i % 3 == 2) {
                            l.set(i, ItemStack.EMPTY);
                        } else if (i + 1 < l.size()) {
                            l.set(i, l.get(i + 1));
                        }
                    }
                }
            }
        }

        PointXY base;
        ProgWidgetItemFilter filterWidget = new ProgWidgetItemFilter();
        if (craftingWidget == null) {
            Rectangle2d bounds = programmerScreen.getProgrammerBounds();
            base = programmerScreen.mouseToWidgetCoords(programmerScreen.getGuiLeft() + bounds.getX() + 50, programmerScreen.getGuiTop() + bounds.getY() + 50, filterWidget);
        } else {
            base = new PointXY(craftingWidget.getX() + craftingWidget.getWidth() / 2, craftingWidget.getY());
        }

        ImmutableList.Builder<ProgWidgetItemFilter> builder = ImmutableList.builder();
        int c = 0;
        for (int n = 0; n < l.size(); n++) {
            ItemStack stack = l.get(n);
            if (!stack.isEmpty() || craftingWidget != null && n % 3 < 2 && !l.get(n + 1).isEmpty()) {
                ProgWidgetItemFilter w = ProgWidgetItemFilter.withFilter(stack);
                if (craftingWidget == null) {
                    w.setX(base.x + c++ * filterWidget.getWidth() / 2);
                    w.setY(base.y);
                } else {
                    w.setX(base.x + (n % 3) * filterWidget.getWidth() / 2);
                    w.setY(base.y + (n / 3) * filterWidget.getHeight() / 2);
                }
                builder.add(w);
            }
        }

        return builder.build();
    }

    private IProgWidget findSuitableCraftingWidget(GuiProgrammer guiProgrammer) {
        // find a visible crafting widget which doesn't already have any item filters attached
        return programmerScreen.te.progWidgets.stream()
                .filter(w -> w instanceof ProgWidgetCrafting && guiProgrammer.isVisible(w))
                .filter(w -> w.getConnectedParameters()[0] == null && w.getConnectedParameters()[1] == null && w.getConnectedParameters()[2] == null)
                .findFirst()
                .orElse(null);
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Names.MOD_ID)
    public static class Listener {
        @SubscribeEvent
        public static void onGuiOpen(GuiOpenEvent event) {
            if (event.getGui() instanceof IRecipesGui && Minecraft.getInstance().screen instanceof GuiProgrammer) {
                programmerScreen = (GuiProgrammer) Minecraft.getInstance().screen;
            } else if (!(event.getGui() instanceof IRecipesGui)) {
                programmerScreen = null;
            }
        }
    }
}
