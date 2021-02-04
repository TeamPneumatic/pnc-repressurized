package me.desht.pneumaticcraft.common.event;

import me.desht.pneumaticcraft.api.drone.AmadronRetrievalEvent;
import me.desht.pneumaticcraft.api.drone.DroneSuicideEvent;
import me.desht.pneumaticcraft.common.DroneRegistry;
import me.desht.pneumaticcraft.common.config.subconfig.AmadronPlayerOffers;
import me.desht.pneumaticcraft.common.entity.living.EntityAmadrone;
import me.desht.pneumaticcraft.common.entity.living.EntityAmadrone.AmadronAction;
import me.desht.pneumaticcraft.common.item.ItemAmadronTablet;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketAmadronStockUpdate;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOfferManager;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronPlayerOffer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class EventHandlerAmadron {
    @SubscribeEvent
    public void onDroneSuicide(DroneSuicideEvent event) {
        if (event.drone instanceof EntityAmadrone) {
            EntityAmadrone drone = (EntityAmadrone) event.drone;
            AmadronOffer offer = AmadronOfferManager.getInstance().getOffer(drone.getHandlingOffer());
            if (offer != null) {
                int requiredCount = offer.getInput().getAmount() * drone.getOfferTimes();
                switch (offer.getInput().getType()) {
                    case ITEM:
                        for (int i = 0; i < drone.getInv().getSlots(); i++) {
                            requiredCount -= drone.getInv().getStackInSlot(i).getCount();
                        }
                        if (requiredCount <= 0) {
                            for (int i = 0; i < drone.getInv().getSlots(); i++) {
                                drone.getInv().setStackInSlot(i, ItemStack.EMPTY);
                            }
                            MinecraftForge.EVENT_BUS.post(new AmadronRetrievalEvent(event.drone));
                        }
                        break;
                    case FLUID:
                        if (drone.getFluidTank().getFluidAmount() >= requiredCount) {
                            MinecraftForge.EVENT_BUS.post(new AmadronRetrievalEvent(event.drone));
                        }
                        break;
                }
            }
        }
    }

    @SubscribeEvent
    public void onAmadronSuccess(AmadronRetrievalEvent event) {
        EntityAmadrone drone = (EntityAmadrone) event.drone;

        AmadronOffer offer = AmadronOfferManager.getInstance().getOffer(drone.getHandlingOffer());

        AmadronPlayerOffer playerOffer = getPlayerOffer(offer);
        if (playerOffer == null) {
            // A normal (non-player) trade; just deliver the result
            doDelivery(drone, offer);
            if (offer.getStock() > 0) {
                offer.addStock(-drone.getOfferTimes());
            }
        } else {
            if (drone.getAmadronAction() == AmadronAction.TAKING_PAYMENT) {
                // Drone has just taken payment for player offer
                // Add a pending payment, and remove Amadron stock
                playerOffer.addPayment(drone.getOfferTimes());
                playerOffer.addStock(-drone.getOfferTimes());
                playerOffer.onTrade(drone.getOfferTimes(), drone.getBuyingPlayer());
                AmadronPlayerOffers.save();
                doDelivery(drone, offer);
            } else if (drone.getAmadronAction() == AmadronAction.RESTOCKING) {
                // Drone is restocking Amadron - add stock
                playerOffer.addStock(drone.getOfferTimes());
                notifyRestock(event.drone.world(), playerOffer);
                AmadronPlayerOffers.save();
            }
        }
        if (offer.getMaxStock() > 0) {
            NetworkHandler.sendNonLocal(new PacketAmadronStockUpdate(offer.getId(), offer.getStock()));
        }
    }

    private void notifyRestock(World world, AmadronPlayerOffer playerOffer) {
        MinecraftServer server = world.getServer();
        if (server != null) {
            ServerPlayerEntity player = server.getPlayerList().getPlayerByUUID(playerOffer.getPlayerId());
            if (player != null) {
                player.sendStatusMessage(xlate("pneumaticcraft.message.amadron.amadronRestocked",
                        playerOffer.getDescription(), playerOffer.getStock()), false);
            }
        }
    }

    private void doDelivery(EntityAmadrone drone, AmadronOffer offer) {
        ItemStack usedTablet = drone.getUsedTablet();
        switch (offer.getOutput().getType()) {
            case ITEM:
                ItemStack offeringItems = offer.getOutput().getItem();
                int producedItems = offeringItems.getCount() * drone.getOfferTimes();
                List<ItemStack> stacks = new ArrayList<>();
                while (producedItems > 0) {
                    ItemStack stack = offeringItems.copy();
                    stack.setCount(Math.min(producedItems, stack.getMaxStackSize()));
                    stacks.add(stack);
                    producedItems -= stack.getCount();
                }
                GlobalPos pos = ItemAmadronTablet.getItemProvidingLocation(usedTablet);
                if (pos != null) {
                    DroneRegistry.getInstance().deliverItemsAmazonStyle(pos, stacks.toArray(new ItemStack[0]));
                }
                break;
            case FLUID:
                FluidStack offeringFluid = offer.getOutput().getFluid().copy();
                offeringFluid.setAmount(offeringFluid.getAmount() * drone.getOfferTimes());
                GlobalPos fpos = ItemAmadronTablet.getFluidProvidingLocation(usedTablet);
                if (fpos != null) {
                    DroneRegistry.getInstance().deliverFluidAmazonStyle(fpos, offeringFluid);
                }
                break;
        }
    }

    private AmadronPlayerOffer getPlayerOffer(AmadronOffer offer) {
        AmadronOffer o = AmadronOfferManager.getInstance().getOffer(AmadronPlayerOffer.getReversedId(offer.getId()));
        return o instanceof AmadronPlayerOffer ? (AmadronPlayerOffer) o : null;
    }
}
