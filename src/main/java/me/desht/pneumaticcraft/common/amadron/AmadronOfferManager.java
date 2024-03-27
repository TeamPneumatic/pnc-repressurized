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

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.desht.pneumaticcraft.api.crafting.recipe.AmadronRecipe;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.config.subconfig.AmadronPlayerOffers;
import me.desht.pneumaticcraft.common.entity.drone.AmadroneEntity;
import me.desht.pneumaticcraft.common.inventory.AmadronMenu;
import me.desht.pneumaticcraft.common.item.AmadronTabletItem;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSyncAmadronOffers;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronPlayerOffer;
import me.desht.pneumaticcraft.common.registry.ModRecipeTypes;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper;
import net.neoforged.neoforge.items.wrapper.PlayerOffhandInvWrapper;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public enum AmadronOfferManager {
    INSTANCE;

    // static trades, always available: loaded from recipes datapack
    private final List<AmadronOffer> staticOffers = new ArrayList<>();
    // periodic trades, randomly appear: loaded from recipes datapack
    private final Int2ObjectMap<List<AmadronOffer>> periodicOffers = new Int2ObjectOpenHashMap<>();  // a list due to random access needs
    // maps villager profession/level to list of trades
    private final Map<String,List<AmadronOffer>> villagerTrades = new HashMap<>();
    // villager professions which actually have some trades
    private final List<VillagerProfession> validProfessions = new ArrayList<>();
    // A complete collection of all known offers
    private final Map<ResourceLocation, AmadronOffer> allOffers = new HashMap<>();
    // And these are the offers which are actually available via the Amadron Tablet (and shown in JEI) at this time
    private final Map<ResourceLocation, AmadronOffer> activeOffers = new LinkedHashMap<>();
    // rebuild offers?  true initially and after a /reload
    private boolean rebuildRequired = true;

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
     * @return true if the offer was added, false if an equivalent offer already exists or the offer is invalid
     */
    public boolean addPlayerOffer(AmadronPlayerOffer offer) {
        if (hasSimilarPlayerOffer(offer)) return false;
        if (offer.getInput().isEmpty() || offer.getOutput().isEmpty()) return false;

        getPlayerOffers().put(offer.getOfferId(), offer);
        addOffer(activeOffers, offer);
        addOffer(allOffers, offer);
        addOffer(allOffers, offer.getReversedOffer());
        NetworkHandler.sendNonLocal(PacketSyncAmadronOffers.create(true));
        saveAll();
        return true;
    }

    public boolean removePlayerOffer(AmadronPlayerOffer offer) {
        if (getPlayerOffers().remove(offer.getOfferId()) != null) {
            activeOffers.remove(offer.getOfferId());
            allOffers.remove(offer.getOfferId());
            allOffers.remove(AmadronPlayerOffer.getReversedId(offer.getOfferId()));
            NetworkHandler.sendNonLocal(PacketSyncAmadronOffers.create(true));
            saveAll();
            return true;
        } else {
            return false;
        }
    }

    public boolean hasSimilarPlayerOffer(AmadronPlayerOffer offer) {
        return getPlayerOffers().values().stream().anyMatch(existing -> existing.equivalentTo(offer));
    }

    private Map<ResourceLocation, AmadronPlayerOffer> getPlayerOffers() {
        return AmadronPlayerOffers.INSTANCE.getPlayerOffers();
    }

    /**
     * Called client-side (from PacketSyncAmadronOffers) to sync up the active offer list.
     * @param newOffers the new offers
     * @param notifyPlayer true to notify players of new offers
     */
    public void syncOffers(Collection<AmadronOffer> newOffers, boolean notifyPlayer) {
        activeOffers.clear();
        newOffers.forEach(offer -> addOffer(activeOffers, offer));

        Log.debug("Received " + activeOffers.size() + " active Amadron offers from server");
        if (notifyPlayer && ConfigHelper.client().general.notifyAmadronOfferUpdates.get()) {
            maybeNotifyPlayerOfUpdates(ClientUtils.getClientPlayer());
        }
    }

    private void maybeNotifyPlayerOfUpdates(Player player) {
        CombinedInvWrapper inv = new CombinedInvWrapper(new PlayerMainInvWrapper(player.getInventory()), new PlayerOffhandInvWrapper(player.getInventory()));
        for (int i = 0; i < inv.getSlots(); i++) {
            if (inv.getStackInSlot(i).getItem() instanceof AmadronTabletItem) {
                player.displayClientMessage(xlate("pneumaticcraft.message.amadron.offersUpdated"), false);
                break;
            }
        }
    }

    void maybeNotifyLocalPlayerOfUpdates() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null && !server.isDedicatedServer()) {
            for (Player player : server.getPlayerList().getPlayers()) {
                if (server.isSingleplayerOwner(player.getGameProfile())) {
                    maybeNotifyPlayerOfUpdates(player);
                    break;
                }
            }
        }
    }

    /**
     * Called client-side (from PacketAmadronUpdateStock) to update stock levels of an offer that someone just purchased.
     * @param id offer ID, must be in the active offers list
     * @param stock new stock level
     */
    public void updateStock(ResourceLocation id, int stock) {
        AmadronRecipe offer = activeOffers.get(id);
        if (offer != null) {
            offer.setStock(stock);
        }
    }

    public int countPlayerOffers(UUID playerId) {
        return (int) activeOffers.values().stream()
                .filter(offer -> offer instanceof AmadronPlayerOffer po && po.getPlayerId().equals(playerId))
                .count();
    }

    /**
     * Called every 30 seconds: Amadron will send drones to restock custom player offers, and also to pay out
     * any pending payments for custom offers.
     */
    public void tryRestockPlayerOffers() {
        boolean needSave = false;
        for (AmadronPlayerOffer offer : getPlayerOffers().values()) {
            AmadronPlayerOffer reversed = offer.getReversedOffer();
            BlockEntity provider = offer.getProvidingTileEntity();
            int possiblePickups = offer.getOutput().apply(
                    itemStack -> IOHelper.getInventoryForBlock(provider).map(h -> offer.getOutput().countTradesInInventory(h)).orElse(0),
                    fluidStack -> IOHelper.getFluidHandlerForBlock(provider).map(h -> offer.getOutput().countTradesInTank(h)).orElse(0)
            );
            if (possiblePickups > 0) {
                AmadroneEntity drone = AmadronMenu.retrieveOrder(null, offer.getReversedOffer(), possiblePickups,
                        offer.getProvidingPos(), offer.getProvidingPos());
                if (drone != null) {
                    drone.setHandlingOffer(reversed.getOfferId(), possiblePickups, ItemStack.EMPTY,
                            "Restock", AmadroneEntity.AmadronAction.RESTOCKING);
                }
            }
            if (offer.payout()) needSave = true;
        }
        if (needSave) saveAll();
    }

    /**
     * Called when the server is stopping to ensure everything is serialized
     */
    public void saveAll() {
        AmadronPlayerOffers.save();
    }

    private <T extends AmadronOffer> void addOffer(Map<ResourceLocation, T> map, T offer) {
        map.put(offer.getOfferId(), offer);
    }

    /**
     * Called on a resource reload (including startup) and periodically to shuffle new periodic offers in
     */
    public void compileActiveOffersList() {
        activeOffers.clear();
        allOffers.clear();

        // static offers first
        staticOffers.forEach(offer -> {
            addOffer(activeOffers, offer);
            addOffer(allOffers, offer);
        });

        Random rand = ThreadLocalRandom.current();

        // random periodic trades
        int s1 = allOffers.size();
        periodicOffers.values().forEach(offers -> offers.forEach(offer -> addOffer(allOffers, offer)));
        int nPeriodics = allOffers.size() - s1;
        for (int i = 0; i < Math.min(nPeriodics, ConfigHelper.common().amadron.numPeriodicOffers.get()); i++) {
            AmadronOffer offer = pickRandomPeriodicTrade(rand);
            if (offer != null) addOffer(activeOffers, offer);
        }

        // random villager trades
        int s2 = allOffers.size();
        villagerTrades.values().forEach(offers -> offers.forEach(offer -> addOffer(allOffers, offer)));
        int nVillager = allOffers.size() - s2;
        if (!validProfessions.isEmpty()) {
            for (int i = 0; i < Math.min(nVillager, ConfigHelper.common().amadron.numVillagerOffers.get()); i++) {
                pickRandomVillagerTrade(validProfessions.get(rand.nextInt(validProfessions.size())), rand)
                        .ifPresent(offer -> addOffer(activeOffers, offer));
            }
        }

        // finally, player->player trades
        addPlayerOffers();

        // reset offer stock levels to initial
        for (AmadronRecipe r : activeOffers.values()) {
            if (r.getMaxStock() > 0) r.setStock(r.getMaxStock());
        }

        // send active list to all clients (but not the local player for an integrated server)
        NetworkHandler.sendNonLocal(PacketSyncAmadronOffers.create(true));
        maybeNotifyLocalPlayerOfUpdates();
        Log.debug(activeOffers.size() + " active Amadron offers to sync to clients");
    }

    public void addPlayerOffers() {
        getPlayerOffers().forEach((id, playerOffer) -> {
            addOffer(activeOffers, playerOffer);
            addOffer(allOffers, playerOffer);
            addOffer(allOffers, playerOffer.getReversedOffer());
        });
    }

    private AmadronOffer pickRandomPeriodicTrade(Random rand) {
        int level = getWeightedTradeLevel(rand);
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

    private Optional<AmadronOffer> pickRandomVillagerTrade(VillagerProfession profession, Random rand) {
        int level = getWeightedTradeLevel(rand);
        do {
            String key = profession.toString() + "_" + level;
            List<AmadronOffer> offers = villagerTrades.get(key);
            if (offers != null && !offers.isEmpty()) {
                int idx = rand.nextInt(offers.size());
                return Optional.ofNullable(offers.get(idx));
            } else {
                level--;
            }
        } while (level > 0);
        return Optional.empty();
    }

    private int getWeightedTradeLevel(Random rand) {
        // weighted trade level makes higher level trades much rarer
        int n = rand.nextInt(100);
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

    private void setupVillagerTrades() {
        // this only needs to be done once, on first load
        if (villagerTrades.isEmpty()) {
            Set<VillagerProfession> validSet = new HashSet<>();
            RandomSource rand = RandomSource.createNewThreadLocalInstance();
            VillagerTrades.TRADES.forEach((profession, tradeMap) -> tradeMap.forEach((level, trades) -> {
                IntStream.range(0, trades.length).forEach(i -> {
                    MerchantOffer offer = getOfferForNullVillager(trades[i], rand);
                    if (offer != null && !offer.getBaseCostA().isEmpty() && !offer.getResult().isEmpty()) {
                        ResourceLocation offerId = new ResourceLocation(profession + "_" + level + "_" + i);
                        String key = profession.toString() + "_" + level;
                        villagerTrades.computeIfAbsent(key, k ->
                                new ArrayList<>()).add(AmadronOffer.villagerTrade(offerId, offer, level));
                        validSet.add(profession);
                    }
                });
            }));
            validProfessions.addAll(validSet);
        }
    }

    private MerchantOffer getOfferForNullVillager(VillagerTrades.ItemListing trade, RandomSource rand) {
        try {
            // shouldn't really pass null here, but creating a fake villager can cause worldgen-related server lockups
            // https://github.com/TeamPneumatic/pnc-repressurized/issues/899
            //noinspection ConstantConditions
            return trade.getOffer(null, rand);
        }catch (NullPointerException ignored) {
            return null;
        }
    }

    public void rebuildRequired() {
        rebuildRequired = true;
    }

    public void checkForFullRebuild(Level level) {
        if (rebuildRequired) {
            Log.debug("Rebuilding Amadron offer list");

            staticOffers.clear();
            periodicOffers.clear();

            ModRecipeTypes.AMADRON.get().allRecipeHolders(level).forEach(recipe -> {
                if (recipe.value() instanceof AmadronOffer offer) {
                    if (offer.isStaticOffer()) {
                        staticOffers.add(offer);
                    } else {
                        periodicOffers.computeIfAbsent(offer.getTradeLevel(), l -> new ArrayList<>()).add(offer);
                    }
                }
            });

            setupVillagerTrades();
            compileActiveOffersList();

            rebuildRequired = false;
        }
    }

    public boolean isActive(ResourceLocation offerId) {
        return activeOffers.containsKey(offerId);
    }

    @Mod.EventBusSubscriber(modid = Names.MOD_ID)
    public static class EventListener {
        @SubscribeEvent
        public static void serverLogin(PlayerEvent.PlayerLoggedInEvent evt) {
            NetworkHandler.sendNonLocal((ServerPlayer) evt.getEntity(), PacketSyncAmadronOffers.create(false));
        }
    }
}
