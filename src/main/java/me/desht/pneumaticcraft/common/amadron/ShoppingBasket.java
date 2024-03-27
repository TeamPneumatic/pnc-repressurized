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

package me.desht.pneumaticcraft.common.amadron;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import me.desht.pneumaticcraft.api.crafting.AmadronTradeResource;
import me.desht.pneumaticcraft.api.crafting.recipe.AmadronRecipe;
import me.desht.pneumaticcraft.common.inventory.AmadronMenu;
import me.desht.pneumaticcraft.common.inventory.AmadronMenu.EnumProblemState;
import me.desht.pneumaticcraft.common.item.AmadronTabletItem;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketAmadronOrderResponse;
import me.desht.pneumaticcraft.common.util.CountedItemStacks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class ShoppingBasket implements Iterable<ResourceLocation> {
    private final Map<ResourceLocation, Integer> basket;

    public ShoppingBasket() {
        basket = new Object2IntLinkedOpenHashMap<>();
    }

    public static ShoppingBasket fromNBT(CompoundTag subTag) {
        ShoppingBasket res = new ShoppingBasket();
        if (subTag != null) {
            for (String key : subTag.getAllKeys()) {
                int count = subTag.getInt(key);
                if (count > 0) res.setUnits(new ResourceLocation(key), count);
            }
        }
        return res;
    }

    public CompoundTag toNBT() {
        CompoundTag subTag = new CompoundTag();
        basket.forEach((key, value) -> {
            if (value > 0) subTag.putInt(key.toString(), value);
        });
        return subTag;
    }

    public int getUnits(ResourceLocation offerId) {
        return basket.getOrDefault(offerId, 0);
    }

    public void setUnits(ResourceLocation offerId, int units) {
        basket.put(offerId, units);
    }

    public void addUnitsToOffer(ResourceLocation offerId, int toAdd) {
        basket.put(offerId, Math.max(0, getUnits(offerId) + toAdd));
        removeIfEmpty(offerId);
    }

    public void remove(ResourceLocation offerId) {
        basket.remove(offerId);
    }

    public void halve(ResourceLocation offerId) {
        basket.put(offerId, getUnits(offerId) / 2);
        removeIfEmpty(offerId);
    }

    private void removeIfEmpty(ResourceLocation offerId) {
        if (getUnits(offerId) == 0) basket.remove(offerId);
    }

    public void clear() {
        basket.clear();
    }

    @Override
    public Iterator<ResourceLocation> iterator() {
        return basket.keySet().iterator();
    }

    /**
     * Go through all items and fluids in the basket and ensure that:
     * <ol>
     * <li>the providing inventory/tank exists</li>
     * <li>there's enough of the offer in stock, where applicable</li>
     * <li>the providing inventory/tank contains enough resources to fund all of the offers</li>
     * <li>the Amadrone has space to carry all resources (36 itemstacks and/or 576000mB fluid)</li>
     * <li>there's enough space in the output</li>
     * </ol>
     * <p>
     * The basket may be modified to make the order(s) valid
     *
     * @param tablet    the Amadron tablet, to get the inventory/tank locations
     * @param allOffers true to check all offers as one, false to check each individually
     */
    public EnumProblemState validate(ItemStack tablet, boolean allOffers) {
        if (basket.isEmpty()) return EnumProblemState.NO_PROBLEMS;  // simple case

        Optional<IItemHandler> itemCap = AmadronTabletItem.getItemCapability(tablet);
        Optional<IFluidHandler> fluidCap = AmadronTabletItem.getFluidCapability(tablet);

        // make sure the inventory and/or tank are actually present for each available offer
        if (basket.keySet().removeIf(offerId -> {
            AmadronRecipe offer = AmadronOfferManager.getInstance().getOffer(offerId);
            boolean inputOk = offer.getInput().apply(itemStack -> itemCap.isPresent(), fluidStack -> fluidCap.isPresent());
            boolean outputOk = offer.getOutput().apply(itemStack -> itemCap.isPresent(), fluidStack -> fluidCap.isPresent());
            return !inputOk || !outputOk;
        })) return EnumProblemState.NO_INVENTORY;


        CountedItemStacks itemAmounts = itemCap.map(CountedItemStacks::new).orElse(new CountedItemStacks());
        Map<Fluid, Integer> fluidAmounts = fluidCap.map(handler -> countFluids(handler)).orElse(Map.of());

        EnumProblemState problem = EnumProblemState.NO_PROBLEMS;
        for (ResourceLocation offerId : basket.keySet()) {
            AmadronRecipe offer = AmadronOfferManager.getInstance().getOffer(offerId);

            // check there's enough in stock, if the order has limited stock
            int units0 = getUnits(offerId);
            int units;
            if (offer.getMaxStock() >= 0 && units0 > offer.getStock()) {
                units = offer.getStock();
                setUnits(offerId, units);
                problem = offer.getStock() == 0 ? EnumProblemState.OUT_OF_STOCK : EnumProblemState.NOT_ENOUGH_STOCK;
            } else {
                units = units0;
            }

            // check there's enough items or fluid in the input
            problem = problem.addProblem(offer.getInput().apply(
                    itemStack -> {
                        int available = itemAmounts.getOrDefault(itemStack, 0);
                        int needed = itemStack.getCount() * units;
                        if (allOffers) itemAmounts.put(itemStack, available - needed);
                        if (available < needed) {
                            setUnits(offerId, available / itemStack.getCount());
                            return EnumProblemState.NOT_ENOUGH_ITEMS;
                        }
                        return EnumProblemState.NO_PROBLEMS;
                    },
                    fluidStack -> {
                        int available = fluidAmounts.getOrDefault(fluidStack.getFluid(), 0);
                        int needed = fluidStack.getAmount() * units;
                        if (allOffers) fluidAmounts.put(fluidStack.getFluid(), available / fluidStack.getAmount());
                        if (available < needed) {
                            setUnits(offerId, available / fluidStack.getAmount());
                            return EnumProblemState.NOT_ENOUGH_FLUID;
                        }
                        return EnumProblemState.NO_PROBLEMS;
                    }
            ));

            // check there's enough space in the Amadrone for the order
            problem = verifyDroneSpace(problem, offerId, offer.getInput());
            if (problem == EnumProblemState.NO_PROBLEMS) {
                problem = verifyDroneSpace(problem, offerId, offer.getOutput());
            }

            // check there's enough space for the returned item/fluid in the output inventory/tank
            problem = problem.addProblem(offer.getOutput().apply(
                    itemStack -> {
                        int availableSpace = itemCap.map(h -> offer.getOutput().findSpaceInItemOutput(h, units)).orElse(0);
                        if (availableSpace < units) {
                            setUnits(offerId, availableSpace);
                            return EnumProblemState.NOT_ENOUGH_ITEM_SPACE;
                        }
                        return EnumProblemState.NO_PROBLEMS;
                    },
                    fluidStack -> {
                        int space = fluidCap.map(h -> offer.getOutput().findSpaceInFluidOutput(h, units)).orElse(0);
                        int availableTrades = Math.min(AmadronMenu.HARD_MAX_MB / fluidStack.getAmount(), space);
                        if (availableTrades < units) {
                            setUnits(offerId, availableTrades);
                            return EnumProblemState.NOT_ENOUGH_FLUID_SPACE;
                        }
                        return EnumProblemState.NO_PROBLEMS;
                    }
            ));
        }

        basket.keySet().removeIf(offerId -> basket.get(offerId) == 0);

        return problem;
    }

    private EnumProblemState verifyDroneSpace(EnumProblemState curProb, ResourceLocation offerId, AmadronTradeResource resource) {
        return curProb.addProblem(resource.apply(
                itemStack -> {
                    int stacks = resource.totalSpaceRequired(getUnits(offerId));
                    if (stacks > AmadronMenu.HARD_MAX_STACKS) {
                        int maxItems = AmadronMenu.HARD_MAX_STACKS * itemStack.getMaxStackSize();
                        setUnits(offerId, maxItems / itemStack.getCount());
                        return EnumProblemState.TOO_MANY_ITEMS;
                    }
                    return EnumProblemState.NO_PROBLEMS;
                },
                fluidStack -> {
                    int space = resource.totalSpaceRequired(getUnits(offerId));
                    if (space > AmadronMenu.HARD_MAX_MB) {
                        setUnits(offerId, AmadronMenu.HARD_MAX_MB / fluidStack.getAmount());
                        return EnumProblemState.TOO_MUCH_FLUID;
                    }
                    return EnumProblemState.NO_PROBLEMS;
                }
        ));
    }

    public void syncToPlayer(ServerPlayer player) {
        basket.forEach((offerId, units) -> NetworkHandler.sendToPlayer(new PacketAmadronOrderResponse(offerId, units), player));
    }

    public boolean isEmpty() {
        return basket.values().stream().noneMatch(amount -> amount > 0);
    }

    private static Map<Fluid, Integer> countFluids(IFluidHandler handler) {
        Map<Fluid,Integer> result = new HashMap<>();
        for (int i = 0; i < handler.getTanks(); i++) {
            FluidStack stack = handler.getFluidInTank(i);
            result.merge(stack.getFluid(), stack.getAmount(), Integer::sum);
        }
        return result;
    }
}
