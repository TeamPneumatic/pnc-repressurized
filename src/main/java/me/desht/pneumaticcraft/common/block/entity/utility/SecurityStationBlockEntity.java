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

package me.desht.pneumaticcraft.common.block.entity.utility;

import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.misc.ITranslatableEnum;
import me.desht.pneumaticcraft.common.PNCDamageSource;
import me.desht.pneumaticcraft.common.block.entity.*;
import me.desht.pneumaticcraft.common.block.entity.RedstoneController.EmittingRedstoneMode;
import me.desht.pneumaticcraft.common.block.entity.RedstoneController.RedstoneMode;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.hacking.secstation.HackSimulation;
import me.desht.pneumaticcraft.common.hacking.secstation.ISimulationController;
import me.desht.pneumaticcraft.common.hacking.secstation.ISimulationController.HackingSide;
import me.desht.pneumaticcraft.common.hacking.secstation.SimulationController;
import me.desht.pneumaticcraft.common.inventory.SecurityStationHackingMenu;
import me.desht.pneumaticcraft.common.inventory.SecurityStationMainMenu;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.item.NetworkComponentItem;
import me.desht.pneumaticcraft.common.item.NetworkComponentItem.NetworkComponentType;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.registry.ModSounds;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.GlobalBlockEntityCacheManager;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.BlockEntityConstants;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

import static me.desht.pneumaticcraft.common.hacking.secstation.HackSimulation.GRID_SIZE;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class SecurityStationBlockEntity extends AbstractTickingBlockEntity implements
        IRedstoneControl<SecurityStationBlockEntity>, MenuProvider, IRangedTE {
    private static final List<RedstoneMode<SecurityStationBlockEntity>> REDSTONE_MODES = ImmutableList.of(
            new EmittingRedstoneMode<>("standard.never", new ItemStack(Items.GUNPOWDER), te -> false),
            new EmittingRedstoneMode<>("securityStation.hacked", ModUpgrades.SECURITY.get().getItemStack(), SecurityStationBlockEntity::isHacked),
            new EmittingRedstoneMode<>("securityStation.doneRebooting", new ItemStack(ModBlocks.SECURITY_STATION.get()), te -> te.getRebootTime() <= 0)
    );

    public static final int INV_ROWS = 7;
    public static final int INV_COLS = 5;
    private static final int INVENTORY_SIZE = INV_ROWS * INV_COLS;

    private final SecurityStationHandler inventory = new SecurityStationHandler();
    public final List<GameProfile> hackedUsers = new ArrayList<>(); // Stores all the users that have hacked this Security Station.
    public final List<GameProfile> sharedUsers = new ArrayList<>(); // Stores all the users that have been allowed by the stationOwner.
    @GuiSynced
    private int rebootTimer; // When the player decides to reset the station, this variable will hold the remaining reboot time.
    @GuiSynced
    public final RedstoneController<SecurityStationBlockEntity> rsController = new RedstoneController<>(this, REDSTONE_MODES);
    private final RangeManager rangeManager = new RangeManager(this, 0x60FF0000);
    private boolean oldRedstoneStatus;
    private boolean validNetwork;
    private ISimulationController simulationController = null;

    public SecurityStationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.SECURITY_STATION.get(), pos, state, 4);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        GlobalBlockEntityCacheManager.getInstance(getLevel()).getSecurityStations().remove(this);
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        GlobalBlockEntityCacheManager.getInstance(getLevel()).getSecurityStations().add(this);
    }

    @Override
    public void tickCommonPre() {
        super.tickCommonPre();

        if (rebootTimer > 0) {
            rebootTimer--;
            if (!nonNullLevel().isClientSide) {
                if (rebootTimer == 0) {
                    hackedUsers.clear();
                    NetworkHandler.sendToAllTracking(new PacketPlaySound(ModSounds.HUD_ENTITY_LOCK.get(), SoundSource.BLOCKS, getBlockPos(), 1f, 1f, false), getLevel(), getBlockPos());
                }
            }
        } else if (simulationController != null) {
            // hack in progress
            simulationController.tick();
            Player hacker = simulationController.getHacker();
            if (!(hacker.containerMenu instanceof SecurityStationHackingMenu)) {
                if (!simulationController.isSimulationDone()
                        && simulationController.getSimulation(HackingSide.AI).isAwake()
                        && !simulationController.isJustTesting()) {
                    // hacker closed the window during hack...
                    retaliate(hacker);
                }
                simulationController = null;
            }
        }

        if (oldRedstoneStatus != rsController.shouldEmit()) {
            oldRedstoneStatus = rsController.shouldEmit();
            updateNeighbours();
        }

        rangeManager.setRange(Math.min(2 + getUpgrades(ModUpgrades.RANGE.get()), BlockEntityConstants.SECURITY_STATION_MAX_RANGE));
    }

    private boolean isOwner(Player player) {
        return !sharedUsers.isEmpty() && player.getGameProfile().equals(sharedUsers.getFirst());
    }

    public void rebootStation() {
        rebootTimer = BlockEntityConstants.SECURITY_STATION_REBOOT_TIME;
        NetworkHandler.sendToAllTracking(new PacketPlaySound(ModSounds.MINIGUN_STOP.get(), SoundSource.BLOCKS, getBlockPos(), 1f, 1f, false), getLevel(), getBlockPos());
    }

    public int getRebootTime() {
        return rebootTimer;
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer player) {
        if (rsController.parseRedstoneMode(tag)) return;

        if (player.containerMenu instanceof SecurityStationMainMenu && isPlayerOnWhiteList(player)) {
            if (tag.equals("reboot")) {
                rebootStation();
            } else if (tag.equals("test") && ConfigHelper.common().machines.securityStationAllowHacking.get()) {
                if (hasValidNetwork()) {
                    initiateHacking(player);
                } else {
                    player.displayClientMessage(Component.translatable("pneumaticcraft.message.securityStation.outOfOrder"), false);
                }
            } else if (tag.startsWith("remove:") && isOwner(player)) {
                String name = tag.split(":", 2)[1];
                removeTrustedUser(name);
            } else if (tag.startsWith("add:") && isOwner(player)) {
                String name = tag.split(":", 2)[1];
                PneumaticCraftUtils.getProfileForName(player.getServer(), name).ifPresent(this::addTrustedUser);
            }
        } else if (player.containerMenu instanceof SecurityStationHackingMenu && isPlayerHacking(player)) {
            if (tag.equals("end_test") && simulationController.isJustTesting()) {
                player.openMenu(this, getBlockPos());
            } else if (tag.startsWith("nuke:")) {
                tryNukeVirus(tag, player);
            } else if (tag.equals("stop_worm")) {
                tryStopWorm(player);
            } else if (tag.startsWith("fortify:")) {
                tryFortify(tag, player);
            } else if (tag.startsWith("hack:")) {
                tryHack(tag, player);
            }
        }
        sendDescriptionPacket();
    }

    private boolean isPlayerHacking(Player player) {
        return simulationController != null
                && simulationController.getHacker().isAlive()
                && simulationController.getHacker().equals(player);
    }

    private void tryHack(String tag, Player player) {
        try {
            int nodePos = Integer.parseInt(tag.split(":", 2)[1]);
            HackSimulation playerSim = simulationController.getSimulation(HackingSide.PLAYER);
            HackSimulation.Node node = playerSim.getNodeAt(nodePos);
            if (node != null && !node.isHacked() && playerSim.getHackedNeighbour(nodePos) >= 0) {
                simulationController.getSimulation(HackingSide.PLAYER).startHack(nodePos);
            }
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            Log.warning("security station @ {}: ignoring bad message {} from {}",
                    PneumaticCraftUtils.posToString(worldPosition), tag, player.getGameProfile().getName());
        }
    }

    private void tryFortify(String tag, Player player) {
        try {
            // fortify is applied to the AI's node to slow it down, but only if the corresponding player node is already hacked
            int nodePos = Integer.parseInt(tag.split(":", 2)[1]);
            HackSimulation.Node node = simulationController.getSimulation(HackingSide.PLAYER).getNodeAt(nodePos);
            if (node != null && node.isHacked()) {
                simulationController.getSimulation(HackingSide.AI).fortify(nodePos);
            }
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            Log.warning("security station @ {}: ignoring bad message {} from {}",
                    PneumaticCraftUtils.posToString(worldPosition), tag, player.getGameProfile().getName());
        }
    }

    private void tryStopWorm(Player player) {
        if (!simulationController.getSimulation(HackingSide.AI).isStopWormed()) {
            if (PneumaticCraftUtils.consumeInventoryItem(player.getInventory(), ModItems.STOP_WORM.get())) {
                int r = 80 + nonNullLevel().random.nextInt(40);
                simulationController.getSimulation(HackingSide.AI).applyStopWorm(r);
            } else {
                // client is lying about how many stop worms they have!
                player.kill();
            }
        }
    }

    private void tryNukeVirus(String tag, Player player) {
        try {
            if (PneumaticCraftUtils.consumeInventoryItem(player.getInventory(), ModItems.NUKE_VIRUS.get())) {
                int nodePos = Integer.parseInt(tag.split(":", 2)[1]);
                simulationController.getSimulation(HackingSide.PLAYER).initiateNukeVirus(nodePos);
            } else {
                // client is lying about how many nuke viruses they have!
                player.kill();
            }
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            Log.warning("security station @ {}: ignoring bad message {}} from {}}",
                    PneumaticCraftUtils.posToString(worldPosition), tag, player.getGameProfile().getName());
        }
    }

    private HackingContainerProvider getHackingContainerProvider() {
        return new HackingContainerProvider();
    }

    public void addTrustedUser(GameProfile user) {
        for (GameProfile sharedUser : sharedUsers) {
            if (gameProfileEquals(sharedUser, user)) return;
        }
        sharedUsers.add(user);
        sendDescriptionPacket();
    }

    private void removeTrustedUser(String name) {
        sharedUsers.removeIf(prof -> name.equals(prof.getName()));
        sendDescriptionPacket();
    }

    public void addHacker(GameProfile user) {
        for (GameProfile hackedUser : hackedUsers) {
            if (gameProfileEquals(hackedUser, user)) return;
        }
        for (GameProfile sharedUser : sharedUsers) {
            if (gameProfileEquals(sharedUser, user)) return;
        }
        hackedUsers.add(user);
        sendDescriptionPacket();
    }

    private boolean gameProfileEquals(GameProfile profile1, GameProfile profile2) {
        return profile1.getId() != null && profile2.getId() != null ?
                profile1.getId().equals(profile2.getId()) :
                profile1.getName().equals(profile2.getName());
    }

    public boolean isHacked() {
        return !hackedUsers.isEmpty();
    }

    public BoundingBox getSecurityCoverage() {
        return rangeManager.getExtents();
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        rebootTimer = tag.getInt("startupTimer");
        inventory.deserializeNBT(provider, tag.getCompound("Items"));
        checkForNetworkValidity();
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);

        tag.putInt("startupTimer", rebootTimer);
        tag.put("Items", inventory.serializeNBT(provider));
    }

    @Override
    public void writeToPacket(CompoundTag tag, HolderLookup.Provider provider) {
        super.writeToPacket(tag, provider);

        tag.put("SharedUsers", toNBTList(sharedUsers));
        tag.put("HackedUsers", toNBTList(hackedUsers));
    }

    @Override
    public void readFromPacket(CompoundTag tag, HolderLookup.Provider provider) {
        super.readFromPacket(tag, provider);

        sharedUsers.clear();
        sharedUsers.addAll(fromNBTList(tag.getList("SharedUsers", Tag.TAG_COMPOUND)));
        hackedUsers.clear();
        hackedUsers.addAll(fromNBTList(tag.getList("HackedUsers", Tag.TAG_COMPOUND)));
    }

    private ListTag toNBTList(Collection<GameProfile> profiles) {
        ListTag res = new ListTag();
        for (GameProfile profile : profiles) {
            res.add(Util.make(new CompoundTag(), t -> {
                t.putString("name", profile.getName());
                t.putString("uuid", profile.getId().toString());
            }));
        }
        return res;
    }

    private Collection<GameProfile> fromNBTList(ListTag list) {
        ImmutableList.Builder<GameProfile> builder = ImmutableList.builder();
        for (int i = 0; i < list.size(); ++i) {
            CompoundTag tagCompound = list.getCompound(i);
            builder.add(new GameProfile(UUID.fromString(tagCompound.getString("uuid")), tagCompound.getString("name")));
        }
        return builder.build();
    }

    /**
     * Check if the given player is allowed to interact within the covered area of this Security Station.
     *
     * @param player the player
     * @return true if the player is allowed to interact
     */
    public boolean doesAllowPlayer(Player player) {
        return rebootTimer > 0 || isPlayerOnWhiteList(player) || hasPlayerHacked(player);
    }

    public boolean doesAllowPlayer(UUID playerID) {
        return rebootTimer > 0 || isPlayerOnWhiteList(playerID) || hasPlayerHacked(playerID);
    }

    private boolean isPlayerOnWhiteList(UUID playerID) {
        return sharedUsers.stream().anyMatch(p -> p.getId().equals(playerID));
    }

    private boolean hasPlayerHacked(UUID playerID) {
        return hackedUsers.stream().anyMatch(p -> p.getId().equals(playerID));
    }

    public boolean isPlayerOnWhiteList(Player player) {
        return sharedUsers.contains(player.getGameProfile());
    }

    public boolean hasPlayerHacked(Player player) {
        return hackedUsers.contains(player.getGameProfile());
    }

    /**
     * Check if the given slots are connected in the network. For this to be true both slots need to have a network component stored as well.
     *
     * @param firstSlot  slot 1
     * @param secondSlot slot 2
     * @return true if the slots are connected
     */
    public boolean connects(int firstSlot, int secondSlot) {
        if (firstSlot < 0 || secondSlot < 0 || firstSlot >= INVENTORY_SIZE || secondSlot >= INVENTORY_SIZE || firstSlot == secondSlot
                || inventory.getStackInSlot(firstSlot).isEmpty() || inventory.getStackInSlot(secondSlot).isEmpty())
            return false;

        for (int column = -1; column <= 1; column++) {
            for (int row = -1; row <= 1; row++) {
                if (firstSlot + row * INV_COLS + column == secondSlot) {
                    if (firstSlot % 5 > 0 && firstSlot % 5 < 4 || secondSlot % 5 > 0 && secondSlot % 5 < 4 || secondSlot % 5 == firstSlot % 5)
                        return true;
                }
            }
        }
        return false;
    }

    public boolean hasValidNetwork() {
        return validNetwork;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        return new SecurityStationMainMenu(i, playerInventory, getBlockPos());
    }

    public void initiateHacking(ServerPlayer hacker) {
        // will only get here if the BE has a valid node network
        if (simulationController != null) {
            hacker.displayClientMessage(xlate("pneumaticcraft.message.securityStation.hackInProgress").withStyle(ChatFormatting.GOLD), false);
        } else {
            simulationController = new SimulationController(this, hacker, isPlayerOnWhiteList(hacker));
            hacker.openMenu(getHackingContainerProvider(), buf -> simulationController.toBytes(buf));
        }
    }

    public int findComponent(NetworkComponentType type) {
        for (int i = 0; i < GRID_SIZE; i++) {
            if (NetworkComponentItem.isType(inventory.getStackInSlot(i), type)) {
                return i;
            }
        }
        return -1;
    }

    public ISimulationController getSimulationController() {
        return simulationController;
    }

    public void setSimulationController(ISimulationController newController) {
        // used client-side during network sync
        this.simulationController = newController;
    }

    public void retaliate(Player hacker) {
        hacker.hurt(PNCDamageSource.securityStation(getLevel()), hacker.getMaxHealth() - 0.5f);
        hacker.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100));
        hacker.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200));
        hacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 300, 3));
    }

    @Override
    public RangeManager getRangeManager() {
        return rangeManager;
    }

    public enum EnumNetworkValidityProblem implements ITranslatableEnum {
        NONE,
        NO_SUBROUTINE, NO_IO_PORT, NO_REGISTRY,
        TOO_MANY_SUBROUTINES, TOO_MANY_IO_PORTS, TOO_MANY_REGISTRIES,
        NO_CONNECTION_SUB_AND_IO_PORT, NO_CONNECTION_IO_PORT_AND_REGISTRY;

        @Override
        public String getTranslationKey() {
            return "pneumaticcraft.gui.tab.problems.security_station." + toString().toLowerCase(Locale.ROOT);
        }
    }

    /**
     * Method used to update the check of the validity of the network.
     *
     * @return optional problem enum
     */
    public EnumNetworkValidityProblem checkForNetworkValidity() {
        validNetwork = false;
        int ioPortSlot = -1;
        int registrySlot = -1;
        int subroutineSlot = -1;
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) {
                Optional<NetworkComponentType> type = NetworkComponentItem.getType(inventory.getStackInSlot(i));
                if (type.isEmpty()) continue; // shouldn't happen but let's be careful
                switch (type.get()) {
                    case DIAGNOSTIC_SUBROUTINE -> {
                        if (subroutineSlot != -1)
                            return EnumNetworkValidityProblem.TOO_MANY_SUBROUTINES; //only one subroutine per network
                        subroutineSlot = i;
                    }
                    case NETWORK_IO_PORT -> {
                        if (ioPortSlot != -1)
                            return EnumNetworkValidityProblem.TOO_MANY_IO_PORTS; //only one IO port per network
                        ioPortSlot = i;
                    }
                    case NETWORK_REGISTRY -> {
                        if (registrySlot != -1)
                            return EnumNetworkValidityProblem.TOO_MANY_REGISTRIES; //only one registry per network
                        registrySlot = i;
                    }
                }
            }
        }
        if (subroutineSlot == -1) return EnumNetworkValidityProblem.NO_SUBROUTINE;
        if (ioPortSlot == -1) return EnumNetworkValidityProblem.NO_IO_PORT;
        if (registrySlot == -1) return EnumNetworkValidityProblem.NO_REGISTRY;
        if (!traceComponent(subroutineSlot, ioPortSlot, new boolean[INVENTORY_SIZE]))
            return EnumNetworkValidityProblem.NO_CONNECTION_SUB_AND_IO_PORT;//check if there's a valid route between the subroutine/ioPort
        if (!traceComponent(ioPortSlot, registrySlot, new boolean[INVENTORY_SIZE]))
            return EnumNetworkValidityProblem.NO_CONNECTION_IO_PORT_AND_REGISTRY; // and ioPort/registry.
        validNetwork = true;
        return EnumNetworkValidityProblem.NONE;
    }

    private boolean traceComponent(int startSlot, int targetSlot, boolean[] slotsDone) {
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (!slotsDone[i] && connects(startSlot, i)) {
                if (i == targetSlot) return true;
                slotsDone[i] = true;
                if (traceComponent(i, targetSlot, slotsDone)) return true;
            }
        }
        return false;
    }

    public int getDetectionChance() {
        double n = 1.0 - Math.pow(0.7, getUpgrades(ModUpgrades.ENTITY_TRACKER.get()) + 1);
        return Mth.clamp((int) (n * 100), 0, 100);
    }

    public int getSecurityLevel() {
        return Math.min(64, 1 + getUpgrades(ModUpgrades.SECURITY.get()));
    }

    @Override
    public boolean isGuiUseableByPlayer(Player par1EntityPlayer) {
        return nonNullLevel().getBlockEntity(getBlockPos()) == this;
    }

    @Override
    public IItemHandler getItemHandler(@Nullable Direction dir) {
        return inventory;
    }

    @Override
    public RedstoneController<SecurityStationBlockEntity> getRedstoneController() {
        return rsController;
    }

    /**
     * Get the number of security stations protecting the given blockpos from the given player.
     *
     * @param player                   the player who is trying to do something with the blockpos in question
     * @param pos                      the blockpos whose protection is being checked
     * @param isPlacingSecurityStation true when trying to place a security station, false otherwise
     * @return the number of security stations which currently prevent access by the player
     */
    public static int getProtectingSecurityStations(Player player, BlockPos pos, boolean isPlacingSecurityStation) {
        return (int) getSecurityStations(player.getCommandSenderWorld(), pos, isPlacingSecurityStation)
                .filter(teSS -> !teSS.doesAllowPlayer(player))
                .count();
    }

    /**
     * Check if any security station is preventing the given player from interacting with the given blockpos.
     *
     * @param player                   the player who is trying to do something with the blockpos in question
     * @param pos                      the blockpos whose protection is being checked
     * @param isPlacingSecurityStation true when trying to place a security station, false otherwise
     * @return  true if a security station would prevent access to the area
     */
    public static boolean isProtectedFromPlayer(Player player, BlockPos pos, final boolean isPlacingSecurityStation) {
        return getSecurityStations(player.getCommandSenderWorld(), pos, isPlacingSecurityStation)
                .anyMatch(teSS -> !teSS.doesAllowPlayer(player));
    }

    /**
     * Check if any security station is preventing the given player ID from interacting with the given blockpos. Works
     * for offline players too, e.g. a player's drone may be operating while they are offline.
     *
     * @param playerId                 UUID of the player who is trying to do something with the blockpos in question
     * @param pos                      the blockpos whose protection is being checked
     * @param isPlacingSecurityStation true when trying to place a security station, false otherwise
     * @return true if a security station would prevent access to the area
     */
    public static boolean isProtectedFromPlayer(UUID playerId, Level level, BlockPos pos, final boolean isPlacingSecurityStation) {
        return getSecurityStations(level, pos, isPlacingSecurityStation)
                .anyMatch(teSS -> !teSS.doesAllowPlayer(playerId));
    }

    static Stream<SecurityStationBlockEntity> getSecurityStations(final Level level, final BlockPos pos, final boolean isPlacingSecurityStation) {
        return GlobalBlockEntityCacheManager.getInstance(level).getSecurityStations().stream()
                .filter(station -> isValidAndInRange(level, pos, isPlacingSecurityStation, station));
    }

    private static boolean isValidAndInRange(Level world, BlockPos pos, boolean isPlacingSecurityStation, SecurityStationBlockEntity teSS) {
        if (!teSS.isRemoved() && teSS.nonNullLevel().dimension().compareTo(world.dimension()) == 0 && teSS.hasValidNetwork()) {
            BoundingBox boundingBox = teSS.getSecurityCoverage();
            // prevent security stations of different owners from being placed too near each other
            if (isPlacingSecurityStation) boundingBox = boundingBox.inflatedBy(16);
            // can't just use AxisAlignedBB#contains here; it will miss blocks on the positive X/Z edges of the box
            return boundingBox.minX() <= pos.getX() && boundingBox.maxX() >= pos.getX()
                    && boundingBox.minY() <= pos.getY() && boundingBox.maxY() >= pos.getY()
                    && boundingBox.minZ() <= pos.getZ() && boundingBox.maxZ() >= pos.getZ();
        }
        return false;
    }

    private static boolean isPlayerExempt(Player player) {
        // can player ignore security stations entirely? server ops and possibly creative mode players
        return player.isCreative() && ConfigHelper.common().machines.securityStationCreativePlayersExempt.get()
                || player.hasPermissions(2);
    }

    private class SecurityStationHandler extends BaseItemStackHandler {
        private SecurityStationHandler() {
            super(SecurityStationBlockEntity.this, INVENTORY_SIZE);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return NetworkComponentItem.getType(stack).map(NetworkComponentType::isSecStationComponent).orElse(false);
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);

            checkForNetworkValidity();
        }
    }

    private class HackingContainerProvider implements MenuProvider {
        @Override
        public Component getDisplayName() {
            return getName().plainCopy().append(" ").append(xlate("pneumaticcraft.armor.upgrade.hacking"));
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
            return new SecurityStationHackingMenu(windowId, playerInventory, getBlockPos());
        }
    }

    @EventBusSubscriber(modid = Names.MOD_ID)
    public static class Listener {
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
            if (!handleInteraction(event)) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onPlayerInteract(PlayerInteractEvent.LeftClickBlock event) {
            if (!handleInteraction(event)) {
                event.setCanceled(true);
            }
        }

        private static boolean handleInteraction(PlayerInteractEvent event) {
            if (!(event.getEntity() instanceof ServerPlayer player) || !event.getLevel().isLoaded(event.getPos())) {
                return true;
            }

            ItemStack heldItem = player.getItemInHand(event.getHand());
            BlockState interactedBlockState = event.getLevel().getBlockState(event.getPos());
            Block interactedBlock = interactedBlockState.getBlock();

            // FIXME this looks wrong!
            if (interactedBlock != ModBlocks.SECURITY_STATION.get() || event instanceof PlayerInteractEvent.LeftClickBlock) {
                boolean tryingToPlaceSecurityStation = heldItem.getItem() instanceof BlockItem bi && bi.getBlock() == ModBlocks.SECURITY_STATION.get();
                if (SecurityStationBlockEntity.isProtectedFromPlayer(player, event.getPos(), tryingToPlaceSecurityStation)) {
                    player.displayClientMessage(xlate(tryingToPlaceSecurityStation ?
                            "pneumaticcraft.message.securityStation.stationPlacementPrevented" :
                            "pneumaticcraft.message.securityStation.accessPrevented"
                    ).withStyle(ChatFormatting.RED), true);
                    if (heldItem.getItem() instanceof BlockItem) {
                        player.connection.send(new ClientboundContainerSetSlotPacket(-2, 0, player.getInventory().selected, heldItem));
                    }
                    return false;
                }
            }
            return true;
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
            if (event.getEntity() instanceof Player player && !event.getLevel().isClientSide()) {
                if (!isPlayerExempt(player)) {
                    if (event instanceof BlockEvent.EntityMultiPlaceEvent) {
                        for (BlockSnapshot snapshot : ((BlockEvent.EntityMultiPlaceEvent) event).getReplacedBlockSnapshots()) {
                            if (isPlacementPrevented(event, player, snapshot.getPos())) return;
                        }
                    } else {
                        isPlacementPrevented(event, player, event.getPos());
                    }
                }
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onBlockBreak(BlockEvent.BreakEvent event) {
            // Note: the PlayerInteractEvent handler will handle real player (or even drone) interaction, but direct
            // block breaking by fake players from other mods will need to go through here to be safely caught
            Player player = event.getPlayer();
            if (!isPlayerExempt(player) && isProtectedFromPlayer(player, event.getPos(), false)) {
                event.setCanceled(true);
                player.displayClientMessage(xlate("pneumaticcraft.message.securityStation.accessPrevented").withStyle(ChatFormatting.RED), true);
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onBlockExplode(ExplosionEvent.Start event) {
            final Player player = getPlayerForExplosion(event);
            if (player != null && !isPlayerExempt(player) && !event.getLevel().isClientSide) {
                event.getExplosion().getToBlow().removeIf(pos -> isProtectedFromPlayer(player, pos, false));
            }
        }

        private static boolean isPlacementPrevented(BlockEvent.EntityPlaceEvent event, Player player, BlockPos pos) {
            boolean tryingToPlaceSecurityStation = event.getPlacedBlock().getBlock() == ModBlocks.SECURITY_STATION.get();
            if (isProtectedFromPlayer(player, pos, tryingToPlaceSecurityStation)) {
                event.setCanceled(true);
                player.displayClientMessage(xlate(tryingToPlaceSecurityStation ?
                        "pneumaticcraft.message.securityStation.stationPlacementPrevented" :
                        "pneumaticcraft.message.securityStation.accessPrevented"
                ).withStyle(ChatFormatting.RED), true);
                return true;
            }
            return false;
        }

        private static Player getPlayerForExplosion(ExplosionEvent event) {
            LivingEntity entity = event.getExplosion().getIndirectSourceEntity();
            return entity instanceof Player ? (Player) entity : null;
        }

        @SubscribeEvent
        public static void onContainerClose(PlayerContainerEvent.Close event) {
            // reopen the main secstation window if closing a test-mode hacking window
            if (event.getEntity() instanceof ServerPlayer player && event.getContainer() instanceof SecurityStationHackingMenu menu) {
                SecurityStationBlockEntity teSS = menu.blockEntity;
                if (teSS.getSimulationController() != null && teSS.getSimulationController().isJustTesting()) {
                    MinecraftServer server = player.getServer();
                    if (server != null) {
                        // deferring is important to avoid infinite close/open loop
                        server.tell(new TickTask(server.getTickCount(), () -> player.openMenu(teSS, teSS.getBlockPos())));
                    }
                }
            }
        }
    }
}
