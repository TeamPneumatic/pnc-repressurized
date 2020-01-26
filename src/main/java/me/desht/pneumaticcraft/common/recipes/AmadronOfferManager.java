package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.common.config.AmadronOfferPeriodicConfig;
import me.desht.pneumaticcraft.common.config.AmadronOfferStaticConfig;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.inventory.ContainerAmadron;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
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
        return FMLCommonHandler.instance().getSide() == Side.SERVER ? SERVER_INSTANCE : CLIENT_INSTANCE;
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
    @SideOnly(Side.CLIENT)
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
        boolean needSave = false;
        for (AmadronOffer offer : allOffers) {
            if (offer instanceof AmadronOfferCustom) {
                AmadronOfferCustom custom = (AmadronOfferCustom) offer;
                TileEntity input = custom.getProvidingTileEntity();
                TileEntity output = custom.getReturningTileEntity();
                int possiblePickups = ContainerAmadron.capShoppingAmount(custom.invert(), 50,
                        getItemHandler(input), getItemHandler(output),
                        getFluidHandler(input), getFluidHandler(output),
                        null);
                if (possiblePickups > 0 && input != null) {
                    BlockPos pos = new BlockPos(input.getPos().getX(), input.getPos().getY(), input.getPos().getZ());
                    EntityDrone drone = ContainerAmadron.retrieveOrderItems(custom, possiblePickups, input.getWorld(), pos, input.getWorld(), pos);
                    if (drone != null) {
                        drone.setHandlingOffer(custom.copy(), possiblePickups, ItemStack.EMPTY, "Restock");
                    }
                }
                custom.invert();
                if (custom.payout()) needSave = true;
            }
        }
        if (needSave) {
            try {
                AmadronOfferStaticConfig.INSTANCE.writeToFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static IItemHandler getItemHandler(TileEntity te) {
        return te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null) ?
                te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null) : null;
    }

    static IFluidHandler getFluidHandler(TileEntity te) {
        return te != null && te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null) ?
                te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null) : null;
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
}
