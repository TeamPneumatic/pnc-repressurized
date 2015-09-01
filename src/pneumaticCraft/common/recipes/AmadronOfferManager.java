package pneumaticCraft.common.recipes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;
import net.minecraftforge.fluids.IFluidHandler;
import pneumaticCraft.common.config.AmadronOfferPeriodicConfig;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.inventory.ContainerAmadron;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class AmadronOfferManager{
    private static AmadronOfferManager CLIENT_INSTANCE = new AmadronOfferManager();
    private static AmadronOfferManager SERVER_INSTANCE = new AmadronOfferManager();
    private final LinkedHashSet<AmadronOffer> staticOffers = new LinkedHashSet<AmadronOffer>();
    private final List<AmadronOffer> periodicOffers = new ArrayList<AmadronOffer>();
    private final LinkedHashSet<AmadronOffer> selectedPeriodicOffers = new LinkedHashSet<AmadronOffer>();
    private final LinkedHashSet<AmadronOffer> allOffers = new LinkedHashSet<AmadronOffer>();

    public static AmadronOfferManager getInstance(){
        return FMLCommonHandler.instance().getSide() == Side.SERVER ? SERVER_INSTANCE : CLIENT_INSTANCE;
    }

    public Collection<AmadronOffer> getStaticOffers(){
        return staticOffers;
    }

    public Collection<AmadronOffer> getPeriodicOffers(){
        return periodicOffers;
    }

    public Collection<AmadronOffer> getAllOffers(){
        return allOffers;
    }

    public boolean addStaticOffer(AmadronOffer offer){
        allOffers.add(offer);
        return staticOffers.add(offer);
    }

    public boolean removeStaticOffer(AmadronOffer offer){
        allOffers.remove(offer);
        return staticOffers.remove(offer);
    }

    public boolean addPeriodicOffer(AmadronOffer offer){
        if(periodicOffers.contains(offer)) {
            return false;
        } else {
            periodicOffers.add(offer);
            return true;
        }
    }

    public void removePeriodicOffer(AmadronOffer offer){
        periodicOffers.remove(offer);
    }

    public boolean hasOffer(AmadronOffer offer){
        return allOffers.contains(offer);
    }

    public void recompileOffers(){
        allOffers.clear();
        allOffers.addAll(staticOffers);
        allOffers.addAll(selectedPeriodicOffers);
    }

    /**
     *  Called client-side to update the client about the available offers.
     */
    @SideOnly(Side.CLIENT)
    public void setOffers(Collection<AmadronOffer> newOffers){
        allOffers.clear();
        allOffers.addAll(newOffers);
    }

    /**
     * Gets the offer that equals() a copy.
     * @param offer
     * @return
     */
    public AmadronOffer get(AmadronOffer offer){
        for(AmadronOffer o : allOffers) {
            if(o.equals(offer)) return o;
        }
        return null;
    }

    public int countOffers(String playerId){
        int count = 0;
        for(AmadronOffer offer : allOffers) {
            if(offer instanceof AmadronOfferCustom && ((AmadronOfferCustom)offer).getPlayerId().equals(playerId)) count++;
        }
        return count;
    }

    public void tryRestockCustomOffers(){

        for(AmadronOffer offer : allOffers) {
            if(offer instanceof AmadronOfferCustom) {
                AmadronOfferCustom custom = (AmadronOfferCustom)offer;
                TileEntity input = custom.getProvidingTileEntity();
                TileEntity output = custom.getReturningTileEntity();
                int possiblePickups = ContainerAmadron.capShoppingAmount(custom.invert(), 50, input instanceof IInventory ? (IInventory)input : null, output instanceof IInventory ? (IInventory)output : null, input instanceof IFluidHandler ? (IFluidHandler)input : null, output instanceof IFluidHandler ? (IFluidHandler)output : null, null);
                if(possiblePickups > 0) {
                    ChunkPosition pos = new ChunkPosition(input.xCoord, input.yCoord, input.zCoord);
                    EntityDrone drone = ContainerAmadron.retrieveOrderItems(custom, possiblePickups, input.getWorldObj(), pos, input.getWorldObj(), pos);
                    if(drone != null) {
                        drone.setHandlingOffer(custom.copy(), possiblePickups, null, "Restock");
                    }
                }
                custom.invert();
                custom.payout();
            }
        }
    }

    public void shufflePeriodicOffers(){
        Random rand = new Random();
        allOffers.removeAll(selectedPeriodicOffers);
        selectedPeriodicOffers.clear();
        int toBeSelected = Math.min(AmadronOfferPeriodicConfig.offersPer, periodicOffers.size());
        while(selectedPeriodicOffers.size() < toBeSelected) {
            selectedPeriodicOffers.add(periodicOffers.get(rand.nextInt(periodicOffers.size())));
        }
        allOffers.addAll(selectedPeriodicOffers);
    }
}
