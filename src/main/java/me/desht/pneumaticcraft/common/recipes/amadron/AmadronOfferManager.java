package me.desht.pneumaticcraft.common.recipes.amadron;

import me.desht.pneumaticcraft.common.config.aux.AmadronOfferPeriodicConfig;
import me.desht.pneumaticcraft.common.config.aux.AmadronOfferStaticConfig;
import me.desht.pneumaticcraft.common.core.ModFluids;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.inventory.ContainerAmadron;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer.TradeResource;
import me.desht.pneumaticcraft.common.util.IOHelper;
import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MerchantOffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.items.IItemHandler;

import java.io.IOException;
import java.util.*;

public class AmadronOfferManager {
    private static final AmadronOfferManager CLIENT_INSTANCE = new AmadronOfferManager();
    private static final AmadronOfferManager SERVER_INSTANCE = new AmadronOfferManager();

    private final LinkedHashSet<AmadronOffer> staticOffers = new LinkedHashSet<>();
    private final List<AmadronOffer> periodicOffers = new ArrayList<>();  // a list due to random access needs
    private final LinkedHashSet<AmadronOffer> selectedPeriodicOffers = new LinkedHashSet<>();
    private final LinkedHashSet<AmadronOffer> allOffers = new LinkedHashSet<>();

    public static AmadronOfferManager getInstance() {
        return EffectiveSide.get() == LogicalSide.SERVER ? SERVER_INSTANCE : CLIENT_INSTANCE;
    }

    public Collection<AmadronOffer> getStaticOffers() {
        return staticOffers;
    }

    public Collection<AmadronOffer> getPeriodicOffers() {
        return periodicOffers;
    }

    public LinkedHashSet<AmadronOffer> getSelectedPeriodicOffers() {
        return selectedPeriodicOffers;
    }

    public Collection<AmadronOffer> getAllOffers() {
        return allOffers;
    }

    public boolean addStaticOffer(AmadronOffer offer) {
        allOffers.add(offer);
        return staticOffers.add(offer);
    }

    public boolean removeStaticOffer(AmadronOffer offer) {
        allOffers.remove(offer);
        return staticOffers.remove(offer);
    }

    public boolean addPeriodicOffer(AmadronOffer offer) {
        if (periodicOffers.contains(offer)) {
            return false;
        } else {
            periodicOffers.add(offer);
            return true;
        }
    }

    public void removePeriodicOffer(AmadronOffer offer) {
        periodicOffers.remove(offer);
    }

    public boolean hasOffer(AmadronOffer offer) {
        return allOffers.contains(offer);
    }

    public void recompileOffers() {
        allOffers.clear();
        allOffers.addAll(staticOffers);
        allOffers.addAll(selectedPeriodicOffers);
    }

    /**
     * Call client-side to sync up the offer list.  It is important that the offer references in allOffers point to
     * the same objects in staticOffers after syncing, otherwise custom offer stock levels etc. will not be properly
     * serialized in single-player instance.  While custom offers may seem pointless in a single-player world, this
     * also applies to 'open to lan' worlds.
     */
    public void syncOffers(Collection<AmadronOffer> newStaticOffers, Collection<AmadronOffer> newSelectedPeriodicOffers) {
        staticOffers.clear();
        staticOffers.addAll(newStaticOffers);
        selectedPeriodicOffers.clear();
        selectedPeriodicOffers.addAll(newSelectedPeriodicOffers);
        recompileOffers();
    }

    /**
     * Gets the offer that equals() a copy.
     *
     * @param offer the wanted offer
     * @return the actual offer that is in the offer manager
     */
    public AmadronOffer get(AmadronOffer offer) {
        for (AmadronOffer o : allOffers) {
            if (o.equals(offer)) return o;
        }
        return null;
    }

    public int countOffers(String playerId) {
        int count = 0;
        for (AmadronOffer offer : allOffers) {
            if (offer instanceof AmadronOfferCustom && ((AmadronOfferCustom) offer).getPlayerId().equals(playerId))
                count++;
        }
        return count;
    }

    public void tryRestockCustomOffers() {
        for (AmadronOffer offer : allOffers) {
            if (offer instanceof AmadronOfferCustom) {
                AmadronOfferCustom custom = (AmadronOfferCustom) offer;
                TileEntity input = custom.getProvidingTileEntity();
                TileEntity output = custom.getReturningTileEntity();
                int possiblePickups = ContainerAmadron.capShoppingAmount(custom.invert(), 50,
                        getItemHandler(input), getItemHandler(output),
                        getFluidHandler(input), getFluidHandler(output),
                        null);
                if (possiblePickups > 0) {
                    EntityDrone drone = ContainerAmadron.retrieveOrderItems(custom, possiblePickups, custom.getProvidingPos(), custom.getProvidingPos());
                    if (drone != null) {
                        drone.setHandlingOffer(custom.copy(), possiblePickups, ItemStack.EMPTY, "Restock");
                    }
                }
                custom.invert();
                custom.payout();
            }
        }
    }

    static IItemHandler getItemHandler(TileEntity te) {
        return IOHelper.getInventoryForTE(te).map(handler -> handler).orElse(null);
    }

    static IFluidHandler getFluidHandler(TileEntity te) {
        return te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).map(handler -> handler).orElse(null);
    }

    public void shufflePeriodicOffers() {
        Random rand = new Random();
        selectedPeriodicOffers.clear();
        int toBeSelected = Math.min(AmadronOfferPeriodicConfig.offersPer, periodicOffers.size());
        while (selectedPeriodicOffers.size() < toBeSelected) {
            selectedPeriodicOffers.add(periodicOffers.get(rand.nextInt(periodicOffers.size())));
        }

        recompileOffers();
    }

    /**
     * Called when the server is stopping to ensure everything is serialized
     */
    public void saveAll() {
        try {
            AmadronOfferStaticConfig.INSTANCE.writeToFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            AmadronOfferPeriodicConfig.INSTANCE.writeToFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void init() {
        addStaticOffer(new AmadronOffer(
                TradeResource.of(new ItemStack(Items.EMERALD, 8)),
                TradeResource.of(new ItemStack(ModItems.PCB_BLUEPRINT))
        ));
        addStaticOffer(new AmadronOffer(
                TradeResource.of(new ItemStack(Items.EMERALD, 8)),
                TradeResource.of(new ItemStack(ModItems.ASSEMBLY_PROGRAM_DRILL))
        ));
        addStaticOffer(new AmadronOffer(
                TradeResource.of(new ItemStack(Items.EMERALD, 8)),
                TradeResource.of(new ItemStack(ModItems.ASSEMBLY_PROGRAM_LASER))
        ));
        addStaticOffer(new AmadronOffer(
                TradeResource.of(new ItemStack(Items.EMERALD, 14)),
                TradeResource.of(new ItemStack(ModItems.ASSEMBLY_PROGRAM_DRILL_LASER))
        ));
        addStaticOffer(new AmadronOffer(
                TradeResource.of(new FluidStack(ModFluids.OIL_SOURCE, 5000)),
                TradeResource.of(new ItemStack(Items.EMERALD, 1))
        ));
        addStaticOffer(new AmadronOffer(
                TradeResource.of(new FluidStack(ModFluids.OIL_SOURCE, 5000)),
                TradeResource.of(new ItemStack(Items.EMERALD))
        ));
        addStaticOffer(new AmadronOffer(
                TradeResource.of(new FluidStack(ModFluids.DIESEL_SOURCE, 4000)),
                TradeResource.of(new ItemStack(Items.EMERALD))
        ));
        addStaticOffer(new AmadronOffer(
                TradeResource.of(new FluidStack(ModFluids.KEROSENE_SOURCE, 3000)),
                TradeResource.of(new ItemStack(Items.EMERALD))
        ));
        addStaticOffer(new AmadronOffer(
                TradeResource.of(new FluidStack(ModFluids.GASOLINE_SOURCE, 2000)),
                TradeResource.of(new ItemStack(Items.EMERALD))
        ));
        addStaticOffer(new AmadronOffer(
                TradeResource.of(new FluidStack(ModFluids.LPG_SOURCE, 1000)),
                TradeResource.of(new ItemStack(Items.EMERALD))
        ));
        addStaticOffer(new AmadronOffer(
                TradeResource.of(new ItemStack(Items.EMERALD)),
                TradeResource.of(new FluidStack(ModFluids.OIL_SOURCE, 1000))
        ));
        addStaticOffer(new AmadronOffer(
                TradeResource.of(new ItemStack(Items.EMERALD, 5)),
                TradeResource.of(new FluidStack(ModFluids.LUBRICANT_SOURCE, 1000))
        ));

        addVillagerTrades();
    }

    private void addVillagerTrades() {
        Random rand = new Random();
        VillagerTrades.field_221239_a.forEach((profession, tradeMap) -> tradeMap.forEach((level, trades) -> {
            for (VillagerTrades.ITrade trade : trades) {
                try {
                    MerchantOffer offer = trade.getOffer(null, rand);
                    addPeriodicOffer(new AmadronOffer(
                            TradeResource.of(offer.func_222218_a()),  // buyingStackFirst
                            TradeResource.of(offer.func_222200_d())   // sellingStack
                    ));
                } catch (NullPointerException ignored) {
                    // some offers need a non-null entity; all we can do is ignore those
                }
            }
        }));
    }
}
