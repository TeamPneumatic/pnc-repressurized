package me.desht.pneumaticcraft.common.recipes.amadron;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.desht.pneumaticcraft.api.crafting.AmadronTradeResource;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.config.subconfig.AmadronPlayerOffers;
import me.desht.pneumaticcraft.common.entity.living.EntityAmadrone;
import me.desht.pneumaticcraft.common.inventory.ContainerAmadron;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSyncAmadronOffers;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.*;
import java.util.stream.IntStream;

public enum AmadronOfferManager {
    INSTANCE;

    // static trades, always available: loaded from recipes datapack
    private final List<AmadronOffer> staticOffers = new ArrayList<>();
    // periodic trades, randomly appear: loaded from recipes datapack
    private final Map<Integer,List<AmadronOffer>> periodicOffers = new HashMap<>();  // a list due to random access needs
    // maps villager profession/level to list of trades
    private final Map<String,List<AmadronOffer>> villagerTrades = new HashMap<>();
    // villager professions which actually have some trades
    private final List<VillagerProfession> validProfessions = new ArrayList<>();
    // A complete collection of all known offers
    private final Map<ResourceLocation, AmadronOffer> allOffers = new HashMap<>();
    // And these are the offers which are actually available via the Amadron Tablet (and shown in JEI) at this time
    private final Map<ResourceLocation, AmadronOffer> activeOffers = new LinkedHashMap<>();

    public static AmadronOfferManager getInstance() {
        return INSTANCE;
    }

    public AmadronOffer getOffer(ResourceLocation offerId) {
        return allOffers.get(offerId);
    }

    public Collection<AmadronOffer> getActiveOffers() {
        return activeOffers.values();
    }

    /**
     * Try to add a player->player offer.
     *
     * @param offer the offer to add
     * @return true if the offer was added, false if an equivalent offer already exists
     */
    public boolean addPlayerOffer(AmadronPlayerOffer offer) {
        if (hasSimilarPlayerOffer(offer)) return false;

        getPlayerOffers().put(offer.getId(), offer);
        addOffer(activeOffers, offer);
        addOffer(allOffers, offer);
        addOffer(allOffers, offer.getReversedOffer());
        NetworkHandler.sendNonLocal(new PacketSyncAmadronOffers());
        saveAll();
        return true;
    }

    public boolean removePlayerOffer(AmadronPlayerOffer offer) {
        if (getPlayerOffers().remove(offer.getId()) != null) {
            activeOffers.remove(offer.getId());
            allOffers.remove(offer.getId());
            allOffers.remove(AmadronPlayerOffer.getReversedId(offer.getId()));
            NetworkHandler.sendNonLocal(new PacketSyncAmadronOffers());
            saveAll();
            return true;
        } else {
            return false;
        }
    }

    public boolean hasSimilarPlayerOffer(AmadronPlayerOffer offer) {
        for (AmadronPlayerOffer existing : getPlayerOffers().values()) {
            if (existing.equivalentTo(offer)) {
                return true;
            }
        }
        return false;
    }

    private Map<ResourceLocation, AmadronPlayerOffer> getPlayerOffers() {
        return AmadronPlayerOffers.INSTANCE.getPlayerOffers();
    }

    /**
     * Called client-side (from PacketSyncAmadronOffers) to sync up the active offer list.
     */
    public void syncOffers(Collection<AmadronOffer> newStaticOffers) {
        activeOffers.clear();
        newStaticOffers.forEach(offer -> addOffer(activeOffers, offer));
        Log.info("Received " + activeOffers.size() + " active Amadron offers from server");
    }

    public int countOffers(String playerId) {
        int count = 0;
        for (AmadronOffer offer : activeOffers.values()) {
            if (offer instanceof AmadronPlayerOffer && ((AmadronPlayerOffer) offer).getPlayerId().equals(playerId))
                count++;
        }
        return count;
    }

    /**
     * Called every 30 seconds: Amadron will send drones to restock custom player offers, and also to pay out
     * any pending payments for custom offers.
     */
    public void tryRestockPlayerOffers() {
        boolean needSave = false;
        for (AmadronPlayerOffer offer : getPlayerOffers().values()) {
            AmadronPlayerOffer reversed = offer.getReversedOffer();
            TileEntity provider = offer.getProvidingTileEntity();
            int possiblePickups = 0;
            switch (offer.getOutput().getType()) {
                case ITEM:
                    possiblePickups = offer.getOutput().countTradesInInventory(IOHelper.getInventoryForTE(provider));
                    break;
                case FLUID:
                    possiblePickups = offer.getOutput().countTradesInTank(IOHelper.getFluidHandlerForTE(provider));
                    break;
            }
            if (possiblePickups > 0) {
                EntityAmadrone drone = ContainerAmadron.retrieveOrderItems(offer.getReversedOffer(), possiblePickups,
                        offer.getProvidingPos(), offer.getProvidingPos());
                if (drone != null) {
                    drone.setHandlingOffer(reversed.getId(), possiblePickups, ItemStack.EMPTY,
                            "Restock", EntityAmadrone.AmadronAction.RESTOCKING);
                }
            }
            if (offer.payout()) needSave = true;
        }
        if (needSave) saveAll();
    }

    /**
     * Called on server start or reload
     *
     * @param resourceList a collection of all the static offers loaded as recipes
     */
    public void initOffers(Map<ResourceLocation, JsonObject> resourceList) {
        loadRecipeOffers(resourceList);

        setupVillagerTrades();

        compileActiveOffersList();
    }

    /**
     * Called when the server is stopping to ensure everything is serialized
     */
    public void saveAll() {
        AmadronPlayerOffers.save();
    }

    private <T extends AmadronOffer> void addOffer(Map<ResourceLocation, T> map, T offer) {
        map.put(offer.getId(), offer);
    }

    /**
     * Called by initOffers(), and periodically to shuffle new periodic offers in
     */
    public void compileActiveOffersList() {
        activeOffers.clear();
        allOffers.clear();

        // static offers first
        staticOffers.forEach(offer -> {
            addOffer(activeOffers, offer);
            addOffer(allOffers, offer);
        });

        // then some randomly-picked periodic & villager trades
        periodicOffers.values().forEach(offers -> offers.forEach(offer -> addOffer(allOffers, offer)));
        villagerTrades.values().forEach(offers -> offers.forEach(offer -> addOffer(allOffers, offer)));

        int nProfessions = validProfessions.size() + (periodicOffers.isEmpty() ? 0 : 1);
        if (nProfessions > 0) {  // should always be > 0, but sanity checking...
            Random rand = new Random();
            for (int i = 0; i < PNCConfig.Common.Amadron.numPeriodicOffers; i++) {
                int p = rand.nextInt(nProfessions);
                AmadronOffer offer;
                if (!periodicOffers.isEmpty() && p == validProfessions.size()) {
                    offer = pickRandomPeriodicTrade(rand);
                } else {
                    if (p == validProfessions.size()) {
                        p = rand.nextInt(validProfessions.size());
                    }
                    offer = pickRandomVillagerTrade(validProfessions.get(p), rand);
                }
                if (offer != null) {
                    addOffer(activeOffers, offer);
                }
            }
        }

        // finally, player->player trades
        addPlayerOffers();

        // send active list to all clients (but not the local player for an integrated server)
        NetworkHandler.sendNonLocal(new PacketSyncAmadronOffers());
    }

    public void addPlayerOffers() {
        getPlayerOffers().forEach((id, playerOffer) -> {
            addOffer(activeOffers, playerOffer);
            addOffer(allOffers, playerOffer);
            addOffer(allOffers, playerOffer.getReversedOffer());
        });
    }

    private AmadronOffer pickRandomPeriodicTrade(Random rand) {
        int level = getWeightedTradeLevel();
        do {
            List<AmadronOffer> offers = periodicOffers.get(level);
            if (offers != null && !offers.isEmpty()) {
                int idx = rand.nextInt(offers.size());
                return offers.get(idx);
            } else {
                level--;
            }
        } while (level > 0);
        Log.debug("Amadron: no periodic offers of level %d or lower", level);
        return null;
    }

    private AmadronOffer pickRandomVillagerTrade(VillagerProfession profession, Random rand) {
        int level = getWeightedTradeLevel();
        do {
            String key = profession.toString() + "_" + level;
            List<AmadronOffer> offers = villagerTrades.get(key);
            if (offers != null && !offers.isEmpty()) {
                int idx = rand.nextInt(offers.size());
                return offers.get(idx);
            } else {
                level--;
            }
        } while (level > 0);
        Log.warning("Amadron: failed to find any trades for profession %s ?", profession.toString());
        return null;
    }

    private int getWeightedTradeLevel() {
        // weighted villager trade level makes higher level trades much rarer
        int n = new Random().nextInt(100);
        if (n < 50) {
            return 1;
        } else if (n < 75) {
            return 2;
        } else if (n < 90) {
            return 3;
        } else if (n < 97) {
            return 4;
        } else {
            return 5;
        }
    }

    private void loadRecipeOffers(Map<ResourceLocation, JsonObject> resourceList) {
        staticOffers.clear();
        periodicOffers.clear();
        resourceList.forEach((id, json) -> {
            if (JSONUtils.getString(json, "type", "").equals("pneumaticcraft:amadron")) {
                try {
                    AmadronOffer offer = AmadronOffer.fromJson(id, json);
                    if (offer.isStaticOffer()) {
                        staticOffers.add(offer);
                    } else {
                        periodicOffers.computeIfAbsent(offer.getTradeLevel(), l -> new ArrayList<>()).add(offer);
                    }
                } catch (CommandSyntaxException | JsonSyntaxException e) {
                    Log.error("Syntax error for offer id " + id + ": " + e.getMessage());
                    Log.error(ExceptionUtils.getStackTrace(e));
                }
            }
        });
    }

    private void setupVillagerTrades() {
        // this only needs to be done once, on first-time data load
        if (villagerTrades.isEmpty()) {
            Set<VillagerProfession> validSet = new HashSet<>();
            Random rand = new Random();
            VillagerTrades.VILLAGER_DEFAULT_TRADES.forEach((profession, tradeMap) -> tradeMap.forEach((level, trades) -> {
                IntStream.range(0, trades.length).forEach(i -> {
                    try {
                        String key = profession.toString() + "_" + level;
                        MerchantOffer offer = trades[i].getOffer(null, rand);
                        ResourceLocation offerId = new ResourceLocation(profession.toString() + "_" + level + "_" + i);
                        villagerTrades.computeIfAbsent(key, k -> new ArrayList<>()).add(new AmadronOffer(offerId,
                                AmadronTradeResource.of(offer.getBuyingStackFirst()),
                                AmadronTradeResource.of(offer.getSellingStack()),
                                false,
                                level
                        ));
                        validSet.add(profession);
                    } catch (NullPointerException ignored) {
                        // some offers need a non-null entity; all we can do is ignore those
                    }
                });
            }));
            validProfessions.addAll(validSet);
        }
    }

    public static class ReloadListener extends JsonReloadListener {
        private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

        public ReloadListener() {
            super(GSON, "recipes");
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonObject> resourceList, IResourceManager resourceManagerIn, IProfiler profilerIn) {
            AmadronOfferManager.getInstance().initOffers(resourceList);
        }
    }

    @Mod.EventBusSubscriber(modid = Names.MOD_ID)
    public static class EventListener {
        @SubscribeEvent
        public static void serverLogin(PlayerEvent.PlayerLoggedInEvent evt) {
            NetworkHandler.sendNonLocal((ServerPlayerEntity) evt.getPlayer(), new PacketSyncAmadronOffers());
        }
    }
}
