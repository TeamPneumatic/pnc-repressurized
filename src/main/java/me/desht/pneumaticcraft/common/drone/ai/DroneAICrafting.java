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

package me.desht.pneumaticcraft.common.drone.ai;

import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.common.drone.progwidgets.ICraftingWidget;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.ItemTagMatcher;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.player.PlayerDestroyItemEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DroneAICrafting extends Goal {
    private final ICraftingWidget widget;
    private final IDrone drone;
    private final int maxActions;
    private int actionCount;

    public DroneAICrafting(IDrone drone, ICraftingWidget widget) {
        this.drone = drone;
        this.widget = widget;
        this.maxActions = widget.useCount() ? widget.getCount() : 0;
        this.actionCount = 0;
    }

    @Override
    public boolean canUse() {
        if (maxActions > 0 && actionCount >= maxActions) {
            return false;
        }

        CraftingInput craftingGrid = widget.getCraftingGrid();
        return widget.getRecipe(drone.getDroneLevel(), craftingGrid).map(recipe -> {
            List<List<ItemStack>> equivalentsList = buildEquivalentsList(craftingGrid);
            if (equivalentsList.isEmpty()) return false;
            int[] equivIndices = new int[9];
            do {
                NonNullList<ItemStack> stacks = NonNullList.withSize(equivalentsList.size(), ItemStack.EMPTY);
                for (int i = 0; i < equivalentsList.size(); i++) {
                    ItemStack stack = equivalentsList.get(i).isEmpty() ? ItemStack.EMPTY : equivalentsList.get(i).get(equivIndices[i]);
                    stacks.set(i, stack);
                }
                CraftingInput input = CraftingInput.of(craftingGrid.width(), craftingGrid.height(), stacks);
                if (recipe.matches(input, drone.getDroneLevel()) && doCrafting(recipe.assemble(input, drone.getDroneLevel().registryAccess()), input)) {
                    actionCount++;
                    return true;
                }
            } while (count(equivIndices, equivalentsList));
            return false;
        }).orElse(false);
    }

    /**
     * Get a list of 9 lists of item from the drone's inventory.  Each element of the list corresponds to a slot in the
     * crafting inventory that is passed.  Each sub-list contains the itemstacks from the drone's inventory
     * which match the crafting grid (either direct item match or via item tag).  The elements of each sub-list are direct
     * references to itemstacks in the drone's inventory; shrinking those stacks (as is done in
     * {@link #doCrafting(ItemStack, CraftingInput)}  will remove items from the drone.
     *
     * @param craftingGrid the crafting grid, set up from the item filter widgets attached to the crafting widget
     * @return a list of 9 lists of itemstack
     */
    private List<List<ItemStack>> buildEquivalentsList(CraftingInput craftingGrid) {
        List<List<ItemStack>> equivalentsList = new ArrayList<>();
        for (int i = 0; i < craftingGrid.size(); i++) {
            equivalentsList.add(new ArrayList<>());
            ItemStack recipeStack = craftingGrid.getItem(i);
            if (!recipeStack.isEmpty()) {
                List<ItemStack> equivalents = new ArrayList<>();
                for (int j = 0; j < drone.getInv().getSlots(); j++) {
                    ItemStack droneStack = drone.getInv().getStackInSlot(j);
                    if (!droneStack.isEmpty() && (droneStack.getItem() == recipeStack.getItem() || ItemTagMatcher.matchTags(droneStack, recipeStack))) {
                        equivalents.add(droneStack);
                    }
                }
                if (equivalents.isEmpty()) return Collections.emptyList();
                equivalentsList.get(i).addAll(equivalents);
            }
        }
        return equivalentsList;
    }

    private boolean count(int[] curIndexes, List<List<ItemStack>> equivalentsList) {
        for (int i = 0; i < equivalentsList.size(); i++) {
            List<ItemStack> list = equivalentsList.get(i);
            curIndexes[i]++;
            if (list.isEmpty() || curIndexes[i] >= list.size()) {
                curIndexes[i] = 0;
            } else {
                return true;
            }
        }
        return false;
    }

    public boolean doCrafting(ItemStack craftedStack, CraftingInput craftMatrix) {
        for (int i = 0; i < craftMatrix.size(); i++) {
            int requiredCount = 0;
            ItemStack stack = craftMatrix.getItem(i);
            if (!stack.isEmpty()) {
                for (int j = 0; j < craftMatrix.size(); j++) {
                    if (stack == craftMatrix.getItem(j)) {
                        requiredCount++;
                    }
                }
                if (requiredCount > stack.getCount()) return false;
            }
        }

        EventHooks.firePlayerCraftingEvent(drone.getFakePlayer(), craftedStack, new SimpleContainer(craftMatrix.items().toArray(new ItemStack[0])));

        for (int i = 0; i < craftMatrix.size(); ++i) {
            ItemStack stack = craftMatrix.getItem(i);

            if (!stack.isEmpty()) {
                if (stack.getItem().hasCraftingRemainingItem(stack)) {
                    ItemStack containerItem = stack.getItem().getCraftingRemainingItem(stack);
                    if (!containerItem.isEmpty() && containerItem.isDamageableItem() && containerItem.getDamageValue() > containerItem.getMaxDamage()) {
                        NeoForge.EVENT_BUS.post(new PlayerDestroyItemEvent(drone.getFakePlayer(), containerItem, InteractionHand.MAIN_HAND));
                        continue;
                    }
                    IOHelper.insertOrDrop(drone.getDroneLevel(), containerItem, drone.getInv(), drone.getDronePos(), false);
                }
                stack.shrink(1); // As this stack references to the Drones stacks in its inventory, we can do this.
            }
        }

        for (int i = 0; i < drone.getInv().getSlots(); i++) {
            ItemStack stack = drone.getInv().getStackInSlot(i);
            if (stack.getCount() <= 0) {
                drone.getInv().setStackInSlot(i, ItemStack.EMPTY);
            }
        }

        IOHelper.insertOrDrop(drone.getDroneLevel(), craftedStack, drone.getInv(), drone.getDronePos(), false);

        return true;
    }
}
