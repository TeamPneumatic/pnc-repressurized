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

import me.desht.pneumaticcraft.api.crafting.recipe.AmadronRecipe;
import me.desht.pneumaticcraft.api.drone.AmadronRetrievalEvent;
import me.desht.pneumaticcraft.api.drone.DroneSuicideEvent;
import me.desht.pneumaticcraft.common.DroneRegistry;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.config.subconfig.AmadronPlayerOffers;
import me.desht.pneumaticcraft.common.entity.drone.AmadroneEntity;
import me.desht.pneumaticcraft.common.entity.drone.AmadroneEntity.AmadronAction;
import me.desht.pneumaticcraft.common.inventory.AmadronMenu;
import me.desht.pneumaticcraft.common.item.AmadronTabletItem;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketAmadronStockUpdate;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronPlayerOffer;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.List;

public class AmadronEventListener {
    private static long reshuffleCounter = 0;

    @SubscribeEvent
    public void onDroneSuicide(DroneSuicideEvent event) {
        if (event.drone instanceof AmadroneEntity) {
            AmadroneEntity drone = (AmadroneEntity) event.drone;
            AmadronRecipe offer = AmadronOfferManager.getInstance().getOffer(drone.getHandlingOffer());
            if (offer != null) {
                offer.getInput().accept(
                        itemStack -> {
                            int requiredCount = offer.getInput().getAmount() * drone.getOfferTimes();
                            for (int i = 0; i < drone.getInv().getSlots(); i++) {
                                requiredCount -= drone.getInv().getStackInSlot(i).getCount();
                            }
                            if (requiredCount <= 0) {
                                for (int i = 0; i < drone.getInv().getSlots(); i++) {
                                    drone.getInv().setStackInSlot(i, ItemStack.EMPTY);
                                }
                                MinecraftForge.EVENT_BUS.post(new AmadronRetrievalEvent(event.drone));
                            } else {
                                onAmadronFailure(drone, offer);
                            }
                        },
                        fluidStack -> {
                            int requiredCount = offer.getInput().getAmount() * drone.getOfferTimes();
                            if (drone.getFluidTank().getFluidAmount() >= requiredCount) {
                                MinecraftForge.EVENT_BUS.post(new AmadronRetrievalEvent(event.drone));
                            } else {
                                onAmadronFailure(drone, offer);
                            }
                        }
                );
            }
        }
    }

    private void onAmadronFailure(AmadroneEntity drone, AmadronRecipe offer) {
        // order failed - Amadrone didn't get enough of the purchase price (maybe player removed items after placing order?)
        if (offer instanceof AmadronPlayerOffer || offer.getMaxStock() >= 0) {
            // restore stock to previous level (we reduced stock in AmadronMenu#retrieveOrderItems())
            offer.setStock(offer.getStock() + drone.getOfferTimes());
            if (offer instanceof AmadronPlayerOffer) AmadronPlayerOffers.save();
            NetworkHandler.sendNonLocal(new PacketAmadronStockUpdate(offer.getId(), offer.getStock()));
        }
    }

    @SubscribeEvent
    public void onAmadronSuccess(AmadronRetrievalEvent event) {
        AmadroneEntity drone = (AmadroneEntity) event.drone;

        AmadronRecipe offer = AmadronOfferManager.getInstance().getOffer(drone.getHandlingOffer());

        AmadronPlayerOffer playerOffer = getPlayerOffer(offer);
        if (playerOffer == null) {
            // A normal (non-player) trade; just deliver the result
            doDelivery(drone, offer);
        } else {
            if (drone.getAmadronAction() == AmadronAction.TAKING_PAYMENT) {
                // Drone has just taken payment for player offer
                // Add a pending payment, and remove Amadron stock
                if (offer instanceof AmadronPlayerOffer) ((AmadronPlayerOffer) offer).addPayment(drone.getOfferTimes()); // don't use playerOffer here!
                playerOffer.onTrade(drone.getOfferTimes(), drone.getBuyingPlayer());
                doDelivery(drone, offer);
            } else if (drone.getAmadronAction() == AmadronAction.RESTOCKING) {
                // Drone is restocking Amadron - add stock
                playerOffer.setStock(playerOffer.getStock() + drone.getOfferTimes());
                NetworkHandler.sendNonLocal(new PacketAmadronStockUpdate(playerOffer.getId(), playerOffer.getStock()));
                playerOffer.notifyRestock();
            }
            AmadronPlayerOffers.save();
        }
    }

    private void doDelivery(AmadroneEntity drone, AmadronRecipe offer) {
        ItemStack usedTablet = drone.getUsedTablet();
        offer.getOutput().accept(
                itemStack -> {
                    GlobalPos itemPos = AmadronTabletItem.getItemProvidingLocation(usedTablet);
                    if (itemPos != null) {
                        int toDeliver = itemStack.getCount() * drone.getOfferTimes();
                        List<ItemStack> stacks = new ArrayList<>();
                        while (toDeliver > 0) {
                            ItemStack stack = ItemHandlerHelper.copyStackWithSize(itemStack, Math.min(toDeliver, itemStack.getMaxStackSize()));
                            stacks.add(stack);
                            toDeliver -= stack.getCount();
                        }
                        DroneRegistry.getInstance().deliverItemsAmazonStyle(itemPos, stacks.toArray(new ItemStack[0]));
                    }
                },
                fluidStack -> {
                    GlobalPos fluidPos = AmadronTabletItem.getFluidProvidingLocation(usedTablet);
                    if (fluidPos != null) {
                        FluidStack offeringFluid = fluidStack.copy();
                        offeringFluid.setAmount(offeringFluid.getAmount() * drone.getOfferTimes());
                        DroneRegistry.getInstance().deliverFluidAmazonStyle(fluidPos, offeringFluid);
                    }
                }
        );
    }

    @SubscribeEvent
    public void amadronHousekeeping(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

            if (reshuffleCounter++ >= ConfigHelper.common().amadron.reshuffleInterval.get() && !anyPlayerUsingAmadron(server)) {
                // don't reshuffle if any player has a tablet open, to avoid confusion
                AmadronOfferManager.getInstance().compileActiveOffersList();
                reshuffleCounter = 0;
            }
            if (server.getTickCount() % 600 == 0) {
                AmadronOfferManager.getInstance().tryRestockPlayerOffers();
            }
            Level overWorld = server.getLevel(Level.OVERWORLD);
            if (overWorld != null) {
                AmadronOfferManager.getInstance().checkForFullRebuild(overWorld);
            }
        }
    }

    private boolean anyPlayerUsingAmadron(MinecraftServer server) {
        return server.getPlayerList().getPlayers().stream()
                .anyMatch(player -> player.containerMenu instanceof AmadronMenu);
    }

    private AmadronPlayerOffer getPlayerOffer(AmadronRecipe offer) {
        AmadronRecipe o = AmadronOfferManager.getInstance().getOffer(AmadronPlayerOffer.getReversedId(offer.getId()));
        return o instanceof AmadronPlayerOffer ? (AmadronPlayerOffer) o : null;
    }
}
