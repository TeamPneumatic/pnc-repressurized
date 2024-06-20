package me.desht.pneumaticcraft.common.amadron;

import me.desht.pneumaticcraft.api.crafting.AmadronTradeResource;
import me.desht.pneumaticcraft.api.crafting.recipe.AmadronRecipe;
import me.desht.pneumaticcraft.common.inventory.AmadronMenu;
import me.desht.pneumaticcraft.common.item.AmadronTabletItem;
import me.desht.pneumaticcraft.common.util.CountedItemStacks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MutableBasket extends ShoppingBasket {
    MutableBasket(Map<ResourceLocation, Integer> basket) {
        super(new HashMap<>(basket));
    }

    MutableBasket() {
        super(new HashMap<>());
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
    public ImmutableBasket toImmutable() {
        return new ImmutableBasket(basket);
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
    public AmadronMenu.EnumProblemState validate(ItemStack tablet, boolean allOffers) {
        if (basket.isEmpty()) return AmadronMenu.EnumProblemState.NO_PROBLEMS;  // simple case

        Optional<IItemHandler> itemCap = AmadronTabletItem.getItemCapability(tablet);
        Optional<IFluidHandler> fluidCap = AmadronTabletItem.getFluidCapability(tablet);

        // make sure the inventory and/or tank are actually present for each available offer
        if (basket.keySet().removeIf(offerId -> {
            AmadronRecipe offer = AmadronOfferManager.getInstance().getOffer(offerId);
            boolean inputOk = offer.getInput().apply(itemStack -> itemCap.isPresent(), fluidStack -> fluidCap.isPresent());
            boolean outputOk = offer.getOutput().apply(itemStack -> itemCap.isPresent(), fluidStack -> fluidCap.isPresent());
            return !inputOk || !outputOk;
        })) return AmadronMenu.EnumProblemState.NO_INVENTORY;


        CountedItemStacks itemAmounts = itemCap.map(CountedItemStacks::new).orElse(new CountedItemStacks());
        Map<Fluid, Integer> fluidAmounts = fluidCap.map(MutableBasket::countFluids).orElse(Map.of());

        AmadronMenu.EnumProblemState problem = AmadronMenu.EnumProblemState.NO_PROBLEMS;
        for (ResourceLocation offerId : basket.keySet()) {
            AmadronRecipe offer = AmadronOfferManager.getInstance().getOffer(offerId);

            // check there's enough in stock, if the order has limited stock
            int units0 = getUnits(offerId);
            int units;
            if (offer.getMaxStock() >= 0 && units0 > offer.getStock()) {
                units = offer.getStock();
                setUnits(offerId, units);
                problem = offer.getStock() == 0 ? AmadronMenu.EnumProblemState.OUT_OF_STOCK : AmadronMenu.EnumProblemState.NOT_ENOUGH_STOCK;
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
                            return AmadronMenu.EnumProblemState.NOT_ENOUGH_ITEMS;
                        }
                        return AmadronMenu.EnumProblemState.NO_PROBLEMS;
                    },
                    fluidStack -> {
                        int available = fluidAmounts.getOrDefault(fluidStack.getFluid(), 0);
                        int needed = fluidStack.getAmount() * units;
                        if (allOffers) fluidAmounts.put(fluidStack.getFluid(), available / fluidStack.getAmount());
                        if (available < needed) {
                            setUnits(offerId, available / fluidStack.getAmount());
                            return AmadronMenu.EnumProblemState.NOT_ENOUGH_FLUID;
                        }
                        return AmadronMenu.EnumProblemState.NO_PROBLEMS;
                    }
            ));

            // check there's enough space in the Amadrone for the order
            problem = verifyDroneSpace(problem, offerId, offer.getInput());
            if (problem == AmadronMenu.EnumProblemState.NO_PROBLEMS) {
                problem = verifyDroneSpace(problem, offerId, offer.getOutput());
            }

            // check there's enough space for the returned item/fluid in the output inventory/tank
            problem = problem.addProblem(offer.getOutput().apply(
                    itemStack -> {
                        int availableSpace = itemCap.map(h -> offer.getOutput().findSpaceInItemOutput(h, units)).orElse(0);
                        if (availableSpace < units) {
                            setUnits(offerId, availableSpace);
                            return AmadronMenu.EnumProblemState.NOT_ENOUGH_ITEM_SPACE;
                        }
                        return AmadronMenu.EnumProblemState.NO_PROBLEMS;
                    },
                    fluidStack -> {
                        int space = fluidCap.map(h -> offer.getOutput().findSpaceInFluidOutput(h, units)).orElse(0);
                        int availableTrades = Math.min(AmadronMenu.HARD_MAX_MB / fluidStack.getAmount(), space);
                        if (availableTrades < units) {
                            setUnits(offerId, availableTrades);
                            return AmadronMenu.EnumProblemState.NOT_ENOUGH_FLUID_SPACE;
                        }
                        return AmadronMenu.EnumProblemState.NO_PROBLEMS;
                    }
            ));
        }

        basket.keySet().removeIf(offerId -> basket.get(offerId) == 0);

        return problem;
    }


    private static Map<Fluid, Integer> countFluids(IFluidHandler handler) {
        Map<Fluid, Integer> result = new HashMap<>();
        for (int i = 0; i < handler.getTanks(); i++) {
            FluidStack stack = handler.getFluidInTank(i);
            result.merge(stack.getFluid(), stack.getAmount(), Integer::sum);
        }
        return result;
    }

    private AmadronMenu.EnumProblemState verifyDroneSpace(AmadronMenu.EnumProblemState curProb, ResourceLocation offerId, AmadronTradeResource resource) {
        return curProb.addProblem(resource.apply(
                itemStack -> {
                    int stacks = resource.totalSpaceRequired(getUnits(offerId));
                    if (stacks > AmadronMenu.HARD_MAX_STACKS) {
                        int maxItems = AmadronMenu.HARD_MAX_STACKS * itemStack.getMaxStackSize();
                        setUnits(offerId, maxItems / itemStack.getCount());
                        return AmadronMenu.EnumProblemState.TOO_MANY_ITEMS;
                    }
                    return AmadronMenu.EnumProblemState.NO_PROBLEMS;
                },
                fluidStack -> {
                    int space = resource.totalSpaceRequired(getUnits(offerId));
                    if (space > AmadronMenu.HARD_MAX_MB) {
                        setUnits(offerId, AmadronMenu.HARD_MAX_MB / fluidStack.getAmount());
                        return AmadronMenu.EnumProblemState.TOO_MUCH_FLUID;
                    }
                    return AmadronMenu.EnumProblemState.NO_PROBLEMS;
                }
        ));
    }
}
