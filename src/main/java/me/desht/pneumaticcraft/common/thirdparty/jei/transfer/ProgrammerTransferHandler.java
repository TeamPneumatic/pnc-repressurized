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
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.client.gui.ProgrammerScreen;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.block.entity.drone.ProgrammerBlockEntity;
import me.desht.pneumaticcraft.common.drone.ProgWidgetUtils;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetCrafting;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetItemFilter;
import me.desht.pneumaticcraft.common.inventory.ProgrammerMenu;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketProgrammerSync;
import me.desht.pneumaticcraft.common.registry.ModMenuTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IUniversalRecipeTransferHandler;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ProgrammerTransferHandler implements IUniversalRecipeTransferHandler<ProgrammerMenu>/*IRecipeTransferHandler<ProgrammerMenu, RecipeHolder<CraftingRecipe>>*/ {
    private static ProgrammerScreen programmerScreen = null;

    private final IRecipeTransferHandlerHelper transferHelper;

    public ProgrammerTransferHandler(IRecipeTransferHandlerHelper transferHelper) {
        this.transferHelper = transferHelper;
    }

    @Override
    public Class<ProgrammerMenu> getContainerClass() {
        return ProgrammerMenu.class;
    }

    @Override
    public Optional<MenuType<ProgrammerMenu>> getMenuType() {
        return Optional.of(ModMenuTypes.PROGRAMMER.get());
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(ProgrammerMenu container, Object recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
        if (programmerScreen != null) {
            IProgWidget craftingWidget = findSuitableCraftingWidget(programmerScreen);

            List<ProgWidgetItemFilter> params = makeFilterWidgets(craftingWidget, recipe, recipeSlots, craftingWidget == null && !maxTransfer);
            if (params.isEmpty()) return transferHelper.createInternalError();
            if (doTransfer) {
                ProgrammerBlockEntity programmer = programmerScreen.te;
                programmer.progWidgets.addAll(params);
                NetworkHandler.sendToServer(PacketProgrammerSync.forBlockEntity(programmer));
                ProgWidgetUtils.updatePuzzleConnections(programmer.progWidgets);
            }
            return null;
        }
        return transferHelper.createInternalError();
    }

    private List<ProgWidgetItemFilter> makeFilterWidgets(IProgWidget craftingWidget, Object recipe, IRecipeSlotsView recipeSlotsView, boolean uniqueStacks) {
        List<ItemStack> l = new ArrayList<>();
        Set<Item> itemsSeen = new HashSet<>();

        recipeSlotsView.getSlotViews(RecipeIngredientRole.INPUT)
                .forEach(view -> view.getDisplayedIngredient(VanillaTypes.ITEM_STACK).ifPresentOrElse(stack -> {
                    l.add(uniqueStacks && itemsSeen.contains(stack.getItem()) ? ItemStack.EMPTY : stack);
                    itemsSeen.add(stack.getItem());
                }, () -> l.add(ItemStack.EMPTY)));

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
                        } else {
                            l.set(i, l.get(i + 1));
                        }
                    }
                }
            }
        }

        PointXY base;
        ProgWidgetItemFilter filterWidget = new ProgWidgetItemFilter();
        if (craftingWidget == null) {
            Rect2i bounds = programmerScreen.getProgrammerBounds();
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
                    w.setPosition(base.x() + c++ * filterWidget.getWidth() / 2, base.y());
                } else {
                    w.setPosition(base.x() + (n % 3) * filterWidget.getWidth() / 2, base.y() + (n / 3) * filterWidget.getHeight() / 2);
                }
                builder.add(w);
            }
        }

        return builder.build();
    }

    private IProgWidget findSuitableCraftingWidget(ProgrammerScreen guiProgrammer) {
        // find a visible crafting widget which doesn't already have any item filters attached
        return programmerScreen.te.progWidgets.stream()
                .filter(w -> w instanceof ProgWidgetCrafting && guiProgrammer.isVisible(w))
                .filter(w -> w.getConnectedParameters()[0] == null && w.getConnectedParameters()[1] == null && w.getConnectedParameters()[2] == null)
                .findFirst()
                .orElse(null);
    }

    public static class Listener {
        @SubscribeEvent
        public static void onGuiOpen(ScreenEvent.Opening event) {
            if (event.getScreen() instanceof IRecipesGui && Minecraft.getInstance().screen instanceof ProgrammerScreen p) {
                programmerScreen = p;
            } else if (!(event.getScreen() instanceof IRecipesGui)) {
                programmerScreen = null;
            }
        }
    }
}
