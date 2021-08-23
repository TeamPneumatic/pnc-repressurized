package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import me.desht.pneumaticcraft.api.DamageSourcePneumaticCraft;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.hacking.secstation.HackSimulation;
import me.desht.pneumaticcraft.common.hacking.secstation.ISimulationController;
import me.desht.pneumaticcraft.common.hacking.secstation.ISimulationController.HackingSide;
import me.desht.pneumaticcraft.common.hacking.secstation.SimulationController;
import me.desht.pneumaticcraft.common.inventory.ContainerSecurityStationHacking;
import me.desht.pneumaticcraft.common.inventory.ContainerSecurityStationMain;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.item.ItemNetworkComponent;
import me.desht.pneumaticcraft.common.item.ItemNetworkComponent.NetworkComponentType;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.tileentity.RedstoneController.EmittingRedstoneMode;
import me.desht.pneumaticcraft.common.tileentity.RedstoneController.RedstoneMode;
import me.desht.pneumaticcraft.common.util.GlobalTileEntityCacheManager;
import me.desht.pneumaticcraft.common.util.ITranslatableEnum;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.Names;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

import static me.desht.pneumaticcraft.common.hacking.secstation.HackSimulation.GRID_SIZE;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class TileEntitySecurityStation extends TileEntityTickableBase implements
        IRedstoneControl<TileEntitySecurityStation>, INamedContainerProvider, IRangedTE
{
    private static final List<RedstoneMode<TileEntitySecurityStation>> REDSTONE_MODES = ImmutableList.of(
            new EmittingRedstoneMode<>("standard.never", new ItemStack(Items.GUNPOWDER), te -> false),
            new EmittingRedstoneMode<>("securityStation.hacked", EnumUpgrade.SECURITY.getItemStack(), TileEntitySecurityStation::isHacked),
            new EmittingRedstoneMode<>("securityStation.doneRebooting", new ItemStack(ModBlocks.SECURITY_STATION.get()), te -> te.getRebootTime() <= 0)
    );

    public static final int INV_ROWS = 7;
    public static final int INV_COLS = 5;
    private static final int INVENTORY_SIZE = INV_ROWS * INV_COLS;

    private final SecurityStationHandler inventory = new SecurityStationHandler();
    private final LazyOptional<IItemHandler> invCap = LazyOptional.of(() -> inventory);
    public final List<GameProfile> hackedUsers = new ArrayList<>(); // Stores all the users that have hacked this Security Station.
    public final List<GameProfile> sharedUsers = new ArrayList<>(); // Stores all the users that have been allowed by the stationOwner.
    @GuiSynced
    private int rebootTimer; // When the player decides to reset the station, this variable will hold the remaining reboot time.
    @GuiSynced
    public final RedstoneController<TileEntitySecurityStation> rsController = new RedstoneController<>(this, REDSTONE_MODES);
    private final RangeManager rangeManager = new RangeManager(this, 0x60FF0000);
    private boolean oldRedstoneStatus;
    private boolean validNetwork;
    private ISimulationController simulationController = null;

    public TileEntitySecurityStation() {
        super(ModTileEntities.SECURITY_STATION.get(), 4);
    }

    @Override
    public void setRemoved(){
        super.setRemoved();
        GlobalTileEntityCacheManager.getInstance().securityStations.remove(this);
    }

    @Override
    public void clearRemoved(){
        super.clearRemoved();
        GlobalTileEntityCacheManager.getInstance().securityStations.add(this);
    }

    @Override
    public void tick() {
        if (rebootTimer > 0) {
            rebootTimer--;
            if (!getLevel().isClientSide) {
                if (rebootTimer == 0) {
                    hackedUsers.clear();
                    NetworkHandler.sendToAllTracking(new PacketPlaySound(ModSounds.HUD_ENTITY_LOCK.get(), SoundCategory.BLOCKS, getBlockPos(), 1f, 1f, false), getLevel(), getBlockPos());
                }
            }
        } else if (simulationController != null) {
            // hack in progress
            simulationController.tick();
            PlayerEntity hacker = simulationController.getHacker();
            if (!(hacker.containerMenu instanceof ContainerSecurityStationHacking)) {
                if (simulationController.isSimulationDone()) {
                    simulationController = null;
                } else if (simulationController.getSimulation(HackingSide.AI).isAwake() && !simulationController.isJustTesting()) {
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

        rangeManager.setRange(Math.min(2 + getUpgrades(EnumUpgrade.RANGE), TileEntityConstants.SECURITY_STATION_MAX_RANGE));

        super.tick();

    }

    public void rebootStation() {
        rebootTimer = TileEntityConstants.SECURITY_STATION_REBOOT_TIME;
        NetworkHandler.sendToAllTracking(new PacketPlaySound(ModSounds.MINIGUN_STOP.get(), SoundCategory.BLOCKS, getBlockPos(), 1f, 1f, false), getLevel(), getBlockPos());
    }

    public int getRebootTime() {
        return rebootTimer;
    }

    @Override
    protected LazyOptional<IItemHandler> getInventoryCap() {
        return invCap;
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayerEntity player) {
        if (rsController.parseRedstoneMode(tag)) return;

        if (player.containerMenu instanceof ContainerSecurityStationMain && isPlayerOnWhiteList(player)) {
            if (tag.equals("reboot")) {
                rebootStation();
            } else if (tag.equals("test")) {
                if (hasValidNetwork()) {
                    initiateHacking(player);
                } else {
                    player.displayClientMessage(new TranslationTextComponent("pneumaticcraft.message.securityStation.outOfOrder"), false);
                }
            } else if (tag.startsWith("remove:")) {
                String name = tag.split(":", 2)[1];
                removeTrustedUser(name);
            } else if (tag.startsWith("add:")) {
                String name = tag.split(":", 2)[1];
                addTrustedUser(new GameProfile(null, name));
            }
        } else if (player.containerMenu instanceof ContainerSecurityStationHacking && isPlayerHacking(player)) {
            if (tag.equals("end_test") && simulationController.isJustTesting()) {
                NetworkHooks.openGui((ServerPlayerEntity) player, this, getBlockPos());
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

    private boolean isPlayerHacking(PlayerEntity player) {
        return simulationController != null
                && simulationController.getHacker().isAlive()
                && simulationController.getHacker().equals(player);
    }

    private void tryHack(String tag, PlayerEntity player) {
        try {
            int nodePos = Integer.parseInt(tag.split(":", 2)[1]);
            HackSimulation playerSim = simulationController.getSimulation(HackingSide.PLAYER);
            HackSimulation.Node node = playerSim.getNodeAt(nodePos);
            if (node != null && !node.isHacked() && playerSim.getHackedNeighbour(nodePos) >= 0) {
                simulationController.getSimulation(HackingSide.PLAYER).startHack(nodePos);
            }
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            Log.warning("security station @ %s: ignoring bad message %s from %s",
                    PneumaticCraftUtils.posToString(worldPosition), tag, player.getGameProfile().getName());
        }
    }

    private void tryFortify(String tag, PlayerEntity player) {
        try {
            // fortify is applied to the AI's node to slow it down, but only if the corresponding player node is already hacked
            int nodePos = Integer.parseInt(tag.split(":", 2)[1]);
            HackSimulation.Node node = simulationController.getSimulation(HackingSide.PLAYER).getNodeAt(nodePos);
            if (node != null && node.isHacked()) {
                simulationController.getSimulation(HackingSide.AI).fortify(nodePos);
            }
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            Log.warning("security station @ %s: ignoring bad message %s from %s",
                    PneumaticCraftUtils.posToString(worldPosition), tag, player.getGameProfile().getName());
        }
    }

    private void tryStopWorm(PlayerEntity player) {
        if (!simulationController.getSimulation(HackingSide.AI).isStopWormed()) {
            if (PneumaticCraftUtils.consumeInventoryItem(player.inventory, ModItems.STOP_WORM.get())) {
                int r = 80 + getLevel().random.nextInt(40);
                simulationController.getSimulation(HackingSide.AI).applyStopWorm(r);
            } else {
                // client is lying about how many stop worms they have!
                player.hurt(DamageSource.OUT_OF_WORLD, 10000);
            }
        }
    }

    private void tryNukeVirus(String tag, PlayerEntity player) {
        try {
            if (PneumaticCraftUtils.consumeInventoryItem(player.inventory, ModItems.NUKE_VIRUS.get())) {
                int nodePos = Integer.parseInt(tag.split(":", 2)[1]);
                simulationController.getSimulation(HackingSide.PLAYER).initiateNukeVirus(nodePos);
            } else {
                // client is lying about how many nuke viruses they have!
                player.hurt(DamageSource.OUT_OF_WORLD, 10000);
            }
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            Log.warning("security station @ %s: ignoring bad message %s from %s",
                    PneumaticCraftUtils.posToString(worldPosition), tag, player.getGameProfile().getName());
        }
    }

    public HackingContainerProvider getHackingContainerProvider() {
        return new HackingContainerProvider();
    }

    public void addTrustedUser(GameProfile user) {
        for (GameProfile sharedUser : sharedUsers) {
            if (gameProfileEquals(sharedUser, user)) return;
        }
        sharedUsers.add(user);
        if (!level.isClientSide) sendDescriptionPacket();
    }

    private void removeTrustedUser(String name) {
        sharedUsers.removeIf(prof -> name.equals(prof.getName()));
        if (!level.isClientSide) sendDescriptionPacket();
    }

    public void addHacker(GameProfile user) {
        for (GameProfile hackedUser : hackedUsers) {
            if (gameProfileEquals(hackedUser, user)) {
                return;
            }
        }
        for (GameProfile sharedUser : sharedUsers) {
            if (gameProfileEquals(sharedUser, user)) return;
        }
        hackedUsers.add(user);
        if (!level.isClientSide) sendDescriptionPacket();
    }

    private boolean gameProfileEquals(GameProfile profile1, GameProfile profile2) {
        return profile1.getId() != null && profile2.getId() != null ? profile1.getId().equals(profile2.getId()) : profile1.getName().equals(profile2.getName());
    }

    public boolean isHacked() {
        return !hackedUsers.isEmpty();
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return rangeManager.shouldShowRange() ? getSecurityCoverage() : super.getRenderBoundingBox();
    }

    public AxisAlignedBB getSecurityCoverage() {
        return rangeManager.getExtents();
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);

        rebootTimer = tag.getInt("startupTimer");
        inventory.deserializeNBT(tag.getCompound("Items"));
        checkForNetworkValidity();
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        super.save(tag);

        tag.putInt("startupTimer", rebootTimer);
        tag.put("Items", inventory.serializeNBT());

        return tag;
    }

    @Override
    public void writeToPacket(CompoundNBT tag) {
        super.writeToPacket(tag);

        tag.put("SharedUsers", toNBTList(sharedUsers));
        tag.put("HackedUsers", toNBTList(hackedUsers));
    }

    @Override
    public void readFromPacket(CompoundNBT tag) {
        super.readFromPacket(tag);

        sharedUsers.clear();
        sharedUsers.addAll(fromNBTList(tag.getList("SharedUsers", Constants.NBT.TAG_COMPOUND)));
        hackedUsers.clear();
        hackedUsers.addAll(fromNBTList(tag.getList("HackedUsers", Constants.NBT.TAG_COMPOUND)));
    }

    private ListNBT toNBTList(Collection<GameProfile> profiles) {
        ListNBT res = new ListNBT();
        for (GameProfile profile : profiles) {
            CompoundNBT tagCompound = new CompoundNBT();
            tagCompound.putString("name", profile.getName());
            if (profile.getId() != null) tagCompound.putString("uuid", profile.getId().toString());
            res.add(tagCompound);
        }
        return res;
    }

    private Collection<GameProfile> fromNBTList(ListNBT list) {
        ImmutableList.Builder<GameProfile> builder = ImmutableList.builder();
        for (int i = 0; i < list.size(); ++i) {
            CompoundNBT tagCompound = list.getCompound(i);
            builder.add(new GameProfile(tagCompound.contains("uuid") ? UUID.fromString(tagCompound.getString("uuid")) : null, tagCompound.getString("name")));
        }
        return builder.build();
    }

    /**
     * Check if the given player is allowed to interact within the covered area of this Security Station.
     *
     * @param player the player
     * @return true if the player is allowed to interact
     */
    public boolean doesAllowPlayer(PlayerEntity player) {
        return rebootTimer > 0 || isPlayerOnWhiteList(player) || hasPlayerHacked(player);
    }

    public boolean isPlayerOnWhiteList(PlayerEntity player) {
        for (int i = 0; i < sharedUsers.size(); i++) {
            GameProfile user = sharedUsers.get(i);
            if (gameProfileEquals(user, player.getGameProfile())) {
                if (user.getId() == null && player.getGameProfile().getId() != null) {
                    sharedUsers.set(i, player.getGameProfile());
                    Log.info("Legacy conversion: Security Station shared username '" + player.getName().getString() + "' is now using UUID '" + player.getGameProfile().getId() + "'.");
                }
                return true;
            }
        }
        return false;
    }

    public boolean hasPlayerHacked(PlayerEntity player) {
        for (int i = 0; i < hackedUsers.size(); i++) {
            GameProfile user = hackedUsers.get(i);
            if (gameProfileEquals(user, player.getGameProfile())) {
                if (user.getId() == null && player.getGameProfile().getId() != null) {
                    hackedUsers.set(i, player.getGameProfile());
                    Log.info("Legacy conversion: Security Station hacked username '" + player.getName().getString() + "' is now using UUID '" + player.getGameProfile().getId() + "'.");
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the given slots are connected in the network. For this to be true both slots need to have a network component stored as well.
     *
     * @param firstSlot slot 1
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
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerSecurityStationMain(i, playerInventory, getBlockPos());
    }

    public void initiateHacking(PlayerEntity hacker) {
        // will only get here if the TE has a valid node network
        if (simulationController != null) {
            hacker.displayClientMessage(xlate("pneumaticcraft.message.securityStation.hackInProgress").withStyle(TextFormatting.GOLD), false);
        } else {
            simulationController = new SimulationController(this, hacker, isPlayerOnWhiteList(hacker));
            NetworkHooks.openGui((ServerPlayerEntity) hacker, getHackingContainerProvider(), buf -> simulationController.toBytes(buf));
        }
    }

    public int findComponent(NetworkComponentType type) {
        for (int i = 0; i < GRID_SIZE; i++) {
            if (ItemNetworkComponent.getType(inventory.getStackInSlot(i)) == type) {
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

    public void retaliate(PlayerEntity hacker) {
        hacker.hurt(DamageSourcePneumaticCraft.SECURITY_STATION, hacker.getMaxHealth() - 0.5f);
        hacker.addEffect(new EffectInstance(Effects.CONFUSION, 100));
        hacker.addEffect(new EffectInstance(Effects.BLINDNESS, 200));
        hacker.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 300, 3));
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
                NetworkComponentType type = ItemNetworkComponent.getType(inventory.getStackInSlot(i));
                assert type != null;
                switch (type) {
                    case DIAGNOSTIC_SUBROUTINE:
                        if (subroutineSlot != -1)
                            return EnumNetworkValidityProblem.TOO_MANY_SUBROUTINES; //only one subroutine per network
                        subroutineSlot = i;
                        break;
                    case NETWORK_IO_PORT:
                        if (ioPortSlot != -1)
                            return EnumNetworkValidityProblem.TOO_MANY_IO_PORTS; //only one IO port per network
                        ioPortSlot = i;
                        break;
                    case NETWORK_REGISTRY:
                        if (registrySlot != -1)
                            return EnumNetworkValidityProblem.TOO_MANY_REGISTRIES; //only one registry per network
                        registrySlot = i;
                        break;
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
        double n = 1.0 - Math.pow(0.7, getUpgrades(EnumUpgrade.ENTITY_TRACKER) + 1);
        return MathHelper.clamp((int)(n * 100), 0, 100);
    }

    public int getSecurityLevel() {
        return Math.min(64, 1 + getUpgrades(EnumUpgrade.SECURITY));
    }

    @Override
    public boolean isGuiUseableByPlayer(PlayerEntity par1EntityPlayer) {
        return getLevel().getBlockEntity(getBlockPos()) == this;
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return inventory;
    }

    @Override
    public RedstoneController<TileEntitySecurityStation> getRedstoneController() {
        return rsController;
    }

    /**
     * Get the number of security stations protecting the given blockpos from the given player.
     *
     * @param player the player who is trying to do something with the blockpos in question
     * @param pos the blockpos whose protection is being checked
     * @param isPlacingSecurityStation true when trying to place a security station, false otherwise
     * @return the number of security stations which currently prevent access by the player
     */
    public static int getProtectingSecurityStations(PlayerEntity player, BlockPos pos, boolean isPlacingSecurityStation) {
        return (int) getSecurityStations(player.getCommandSenderWorld(), pos, isPlacingSecurityStation)
                .filter(teSS -> !teSS.doesAllowPlayer(player))
                .count();
    }

    /**
     * Check if any security station is preventing the given player from interacting with the given blockpos.
     *
     * @param player the player who is trying to do something with the blockpos in question
     * @param pos the blockpos whose protection is being checked
     * @param isPlacingSecurityStation true when trying to place a security station, false otherwise
     * @return the number of security stations which currently prevent access by the player
     */
    public static boolean isProtectedFromPlayer(PlayerEntity player, BlockPos pos, final boolean isPlacingSecurityStation) {
        return getSecurityStations(player.getCommandSenderWorld(), pos, isPlacingSecurityStation)
                .anyMatch(teSS -> !teSS.doesAllowPlayer(player));
    }

    static Stream<TileEntitySecurityStation> getSecurityStations(final World world, final BlockPos pos, final boolean isPlacingSecurityStation) {
        return GlobalTileEntityCacheManager.getInstance().securityStations.stream()
                .filter(station -> isValidAndInRange(world, pos, isPlacingSecurityStation, station));
    }

    private static boolean isValidAndInRange(World world, BlockPos pos, boolean isPlacingSecurityStation, TileEntitySecurityStation teSS) {
        if (!teSS.isRemoved() && teSS.getLevel().dimension().compareTo(world.dimension()) == 0 && teSS.hasValidNetwork()) {
            AxisAlignedBB aabb = teSS.getSecurityCoverage();
            // prevent security stations of different owners from being placed too near each other
            if (isPlacingSecurityStation) aabb = aabb.inflate(16);
            // can't just use AxisAlignedBB#contains here; it will miss blocks on the positive X/Z edges of the box
            return aabb.minX <= pos.getX() && aabb.maxX >= pos.getX()
                    && aabb.minY <= pos.getY() && aabb.maxY >= pos.getY()
                    && aabb.minZ <= pos.getZ() && aabb.maxZ >= pos.getZ();
        }
        return false;
    }

    private static boolean isPlayerExempt(PlayerEntity player) {
        // can player ignore security stations entirely? server ops and creative mode players
        // note : player.getCommandSource() will throw NPE if player is a fakeplayer with a null id
        return player.isCreative() || (player.getGameProfile().getId() != null && player.createCommandSourceStack().hasPermission(2));
    }

    private class SecurityStationHandler extends BaseItemStackHandler {
        private SecurityStationHandler() {
            super(TileEntitySecurityStation.this, INVENTORY_SIZE);
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            checkForNetworkValidity();
        }
    }

    private class HackingContainerProvider implements INamedContainerProvider {
        @Override
        public ITextComponent getDisplayName() {
            return getName().plainCopy().append(" ").append(xlate("pneumaticcraft.armor.upgrade.hacking"));
        }

        @Nullable
        @Override
        public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
            return new ContainerSecurityStationHacking(windowId, playerInventory, getBlockPos());
        }
    }

    @Mod.EventBusSubscriber(modid = Names.MOD_ID)
    public static class Listener {
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onPlayerInteract(PlayerInteractEvent event) {
            if (event instanceof PlayerInteractEvent.RightClickEmpty || event.getWorld().isClientSide) return;

            PlayerEntity player = event.getPlayer();
            if (isPlayerExempt(player)) return;

            ItemStack heldItem = player.getItemInHand(event.getHand());
            BlockState interactedBlockState = event.getWorld().getBlockState(event.getPos());
            Block interactedBlock = interactedBlockState.getBlock();

            if (interactedBlock != ModBlocks.SECURITY_STATION.get() || event instanceof PlayerInteractEvent.LeftClickBlock) {
                boolean tryingToPlaceSecurityStation = heldItem.getItem() instanceof BlockItem && ((BlockItem) heldItem.getItem()).getBlock() == ModBlocks.SECURITY_STATION.get();
                if (TileEntitySecurityStation.isProtectedFromPlayer(player, event.getPos(), tryingToPlaceSecurityStation)) {
                    event.setCanceled(true);
                    player.displayClientMessage(xlate(tryingToPlaceSecurityStation ?
                            "pneumaticcraft.message.securityStation.stationPlacementPrevented" :
                            "pneumaticcraft.message.securityStation.accessPrevented"
                    ).withStyle(TextFormatting.RED), true);
                    if (player instanceof ServerPlayerEntity && heldItem.getItem() instanceof BlockItem) {
                        ((ServerPlayerEntity) player).connection.send(new SSetSlotPacket(-2, player.inventory.selected, heldItem));
                    }
                }
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
            if (event.getEntity() instanceof PlayerEntity && !event.getWorld().isClientSide()) {
                PlayerEntity player = (PlayerEntity) event.getEntity();
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
            PlayerEntity player = event.getPlayer();
            if (!isPlayerExempt(player) && isProtectedFromPlayer(player, event.getPos(), false)) {
                event.setCanceled(true);
                player.displayClientMessage(xlate("pneumaticcraft.message.securityStation.accessPrevented").withStyle(TextFormatting.RED), true);
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onBlockExplode(ExplosionEvent event) {
            final PlayerEntity player = getPlayerForExplosion(event);
            if (player != null && !isPlayerExempt(player) && event.getWorld() != null && !event.getWorld().isClientSide) {
                event.getExplosion().getToBlow().removeIf(pos -> isProtectedFromPlayer(player, pos, false));
            }
        }

        private static boolean isPlacementPrevented(BlockEvent.EntityPlaceEvent event, PlayerEntity player, BlockPos pos) {
            boolean tryingToPlaceSecurityStation = event.getPlacedBlock().getBlock() == ModBlocks.SECURITY_STATION.get();
            if (isProtectedFromPlayer(player, pos, tryingToPlaceSecurityStation)) {
                event.setCanceled(true);
                player.displayClientMessage(xlate(tryingToPlaceSecurityStation ?
                        "pneumaticcraft.message.securityStation.stationPlacementPrevented" :
                        "pneumaticcraft.message.securityStation.accessPrevented"
                ).withStyle(TextFormatting.RED), true);
                return true;
            }
            return false;
        }

        private static PlayerEntity getPlayerForExplosion(ExplosionEvent event) {
            LivingEntity entity = event.getExplosion().getSourceMob();
            return entity instanceof PlayerEntity ? (PlayerEntity) entity : null;
        }

        @SubscribeEvent
        public static void onContainerClose(PlayerContainerEvent.Close event) {
            // reopen the main secstation window if closing a test-mode hacking window
            if (event.getPlayer() instanceof ServerPlayerEntity && event.getContainer() instanceof ContainerSecurityStationHacking) {
                TileEntitySecurityStation teSS = ((ContainerSecurityStationHacking) event.getContainer()).te;
                if (teSS.getSimulationController() != null && teSS.getSimulationController().isJustTesting()) {
                    ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
                    if (player.getServer() != null) {
                        // deferring is important to avoid infinite close/open loop
                        player.getServer().tell(new TickDelayedTask(1, () -> NetworkHooks.openGui(player, teSS, teSS.getBlockPos())));
                    }
                }
            }
        }
    }
}
