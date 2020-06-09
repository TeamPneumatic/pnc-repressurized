package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.client.util.RangeLines;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerSecurityStationHacking;
import me.desht.pneumaticcraft.common.inventory.ContainerSecurityStationMain;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.item.ItemNetworkComponent;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketRenderRangeLines;
import me.desht.pneumaticcraft.common.util.GlobalTileEntityCacheManager;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class TileEntitySecurityStation extends TileEntityTickableBase implements IGUITextFieldSensitive,
        IRangeLineShower, IRedstoneControl, INamedContainerProvider {

    private static final List<String> REDSTONE_LABELS = ImmutableList.of(
            "pneumaticcraft.gui.tab.redstoneBehaviour.button.never",
            "pneumaticcraft.gui.tab.redstoneBehaviour.securityStation.button.hacked",
            "pneumaticcraft.gui.tab.redstoneBehaviour.securityStation.button.doneRebooting"
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
    private String textFieldText = "";
    private int securityRange;
    private int oldSecurityRange; //range used by the range line renderer, to figure out if the range has been changed.
    public RangeLines rangeLines;

    @GuiSynced
    public int redstoneMode;
    private boolean oldRedstoneStatus;

    private boolean validNetwork;

    public TileEntitySecurityStation() {
        super(ModTileEntities.SECURITY_STATION.get(), 4);
    }
    
    @Override
    public void remove(){
        super.remove();
        GlobalTileEntityCacheManager.getInstance().securityStations.remove(this);
    }
    
    @Override
    public void validate(){
        super.validate();
        GlobalTileEntityCacheManager.getInstance().securityStations.add(this);
        rangeLines = new RangeLines(0x33FF0000);
    }

    @Override
    public void tick() {
        if (rebootTimer > 0) {
            rebootTimer--;
            if (!getWorld().isRemote) {
                if (rebootTimer == 0) {
                    hackedUsers.clear();
                }
            }
        }
        if (getWorld().isRemote && !firstRun) {
            if (oldSecurityRange != getSecurityRange() || oldSecurityRange == 0) {
                rangeLines.startRendering(getSecurityRange());
                oldSecurityRange = getSecurityRange();
            }
            rangeLines.tick(world.rand);
        }
        if (/* !getWorld().isRemote && */oldRedstoneStatus != shouldEmitRedstone()) {
            oldRedstoneStatus = shouldEmitRedstone();
            updateNeighbours();
        }

        securityRange = Math.min(2 + getUpgrades(EnumUpgrade.RANGE), TileEntityConstants.SECURITY_STATION_MAX_RANGE);

        super.tick();

    }

    public void rebootStation() {
        rebootTimer = TileEntityConstants.SECURITY_STATION_REBOOT_TIME;
    }

    public int getRebootTime() {
        return rebootTimer;
    }

    /**
     * Will initiate the wireframe rendering. When invoked on the server, it sends a packet to every client to render the box.
     */
    @Override
    public void showRangeLines() {
        if (getWorld().isRemote) {
            rangeLines.startRendering(getSecurityRange());
        } else {
            NetworkHandler.sendToAllAround(new PacketRenderRangeLines(this), getWorld(), TileEntityConstants.PACKET_UPDATE_DISTANCE + getSecurityRange());
        }
    }

    @Override
    protected LazyOptional<IItemHandler> getInventoryCap() {
        return invCap;
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, PlayerEntity player) {
        if (tag.equals(IGUIButtonSensitive.REDSTONE_TAG)) {
            redstoneMode++;
            if (redstoneMode > 2) redstoneMode = 0;
            updateNeighbours();
        } else if (tag.equals("reboot")) {
            rebootStation();
        } else if (tag.equals("test")) {
            if (!hasValidNetwork()) {
                player.sendStatusMessage(new TranslationTextComponent(TextFormatting.GREEN + "This Security Station is out of order: Its network hasn't been properly configured."), false);
            } else {
                NetworkHooks.openGui((ServerPlayerEntity) player, new HackingContainerProvider(), getPos());
            }
        } else if (tag.startsWith("remove:")) {
            try {
                int idx = Integer.parseInt(tag.split(":")[1]);
                sharedUsers.remove(idx);
            } catch (IllegalArgumentException|ArrayIndexOutOfBoundsException ignored) {
            }
        }
        sendDescriptionPacket();
    }

    public HackingContainerProvider getHackingContainerProvider() {
        return new HackingContainerProvider();
    }

    public void addSharedUser(GameProfile user) {
        for (GameProfile sharedUser : sharedUsers) {
            if (gameProfileEquals(sharedUser, user)) return;
        }
        sharedUsers.add(user);
        sendDescriptionPacket();
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
        sendDescriptionPacket();
    }

    private boolean gameProfileEquals(GameProfile profile1, GameProfile profile2) {
        return profile1.getId() != null && profile2.getId() != null ? profile1.getId().equals(profile2.getId()) : profile1.getName().equals(profile2.getName());
    }

    public boolean shouldEmitRedstone() {
        switch (redstoneMode) {
            case 0:
                return false;
            case 1:
                return isHacked();
            case 2:
                return getRebootTime() <= 0;
        }
        return false;
    }

    public boolean isHacked() {
        return hackedUsers.size() > 0;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (rangeLines == null || !rangeLines.shouldRender()) return super.getRenderBoundingBox();
        return getAffectedBoundingBox();
    }

    public AxisAlignedBB getAffectedBoundingBox(){
        return new AxisAlignedBB(getPos()).grow(getSecurityRange());
    }

    public int getSecurityRange() {
        return securityRange;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        redstoneMode = tag.getInt("redstoneMode");
        rebootTimer = tag.getInt("startupTimer");
        inventory.deserializeNBT(tag.getCompound("Items"));
        checkForNetworkValidity();
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        tag.putInt("redstoneMode", redstoneMode);
        tag.putInt("startupTimer", rebootTimer);
        tag.put("Items", inventory.serializeNBT());
        return tag;
    }

    @Override
    public void writeToPacket(CompoundNBT tag) {
        super.writeToPacket(tag);
        ListNBT sharedList = new ListNBT();
        for (GameProfile sharedUser : sharedUsers) {
            CompoundNBT tagCompound = new CompoundNBT();
            tagCompound.putString("name", sharedUser.getName());
            if (sharedUser.getId() != null)
                tagCompound.putString("uuid", sharedUser.getId().toString());
            sharedList.add(tagCompound);
        }
        tag.put("SharedUsers", sharedList);

        ListNBT hackedList = new ListNBT();
        for (GameProfile hackedUser : hackedUsers) {
            CompoundNBT tagCompound = new CompoundNBT();
            tagCompound.putString("name", hackedUser.getName());
            if (hackedUser.getId() != null)
                tagCompound.putString("uuid", hackedUser.getId().toString());
            hackedList.add(tagCompound);
        }
        tag.put("HackedUsers", hackedList);
    }

    @Override
    public void readFromPacket(CompoundNBT tag) {
        super.readFromPacket(tag);
        sharedUsers.clear();
        ListNBT sharedList = tag.getList("SharedUsers", 10);
        for (int i = 0; i < sharedList.size(); ++i) {
            CompoundNBT tagCompound = sharedList.getCompound(i);
            sharedUsers.add(new GameProfile(tagCompound.contains("uuid") ? UUID.fromString(tagCompound.getString("uuid")) : null, tagCompound.getString("name")));
        }

        hackedUsers.clear();
        ListNBT hackedList = tag.getList("HackedUsers", 10);
        for (int i = 0; i < hackedList.size(); ++i) {
            CompoundNBT tagCompound = hackedList.getCompound(i);
            hackedUsers.add(new GameProfile(tagCompound.contains("uuid") ? UUID.fromString(tagCompound.getString("uuid")) : null, tagCompound.getString("name")));
        }
    }

    @Override
    public void setText(int textFieldID, String text) {
        textFieldText = text;
    }

    @Override
    public String getText(int textFieldID) {
        return textFieldText;
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
                    Log.info("Legacy conversion: Security Station shared username '" + player.getName().getFormattedText() + "' is now using UUID '" + player.getGameProfile().getId() + "'.");
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
                    Log.info("Legacy conversion: Security Station hacked username '" + player.getName().getFormattedText() + "' is now using UUID '" + player.getGameProfile().getId() + "'.");
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
     * @return true iof the slots are connected
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

    @Override
    public ITextComponent getDisplayName() {
        return getDisplayNameInternal();
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerSecurityStationMain(i, playerInventory, getPos());
    }

    public enum EnumNetworkValidityProblem {
        NONE,
        NO_SUBROUTINE, NO_IO_PORT, NO_REGISTRY,
        TOO_MANY_SUBROUTINES, TOO_MANY_IO_PORTS, TOO_MANY_REGISTRIES,
        NO_CONNECTION_SUB_AND_IO_PORT, NO_CONNECTION_IO_PORT_AND_REGISTRY
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
                ItemNetworkComponent.NetworkComponentType type = ItemNetworkComponent.getType(inventory.getStackInSlot(i));
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
        return Math.min(100, 20 + 20 * getUpgrades(EnumUpgrade.ENTITY_TRACKER));
    }

    public int getSecurityLevel() {
        return 1 + getUpgrades(EnumUpgrade.SECURITY);
    }

    @Override
    public boolean isGuiUseableByPlayer(PlayerEntity par1EntityPlayer) {
        return getWorld().getTileEntity(getPos()) == this;
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return inventory;
    }

    @Override
    public int getRedstoneMode() {
        return redstoneMode;
    }

    @Override
    protected List<String> getRedstoneButtonLabels() {
        return REDSTONE_LABELS;
    }


    /**
     * Get a count of the number of security stations protecting the given blockpos from the given player.
     *
     * @param player the player who is trying to do something with the blockpos in question
     * @param pos the blockpos whose protection is being checked
     * @param showRangeLines whether to display the stations' range bounding boxes when access is denied
     * @param placementRange true when trying to place a block, false when trying to interact with a block
     * @return the number of security stations which currently prevent access by the player
     */
    public static int getProtectingSecurityStations(PlayerEntity player, BlockPos pos, boolean showRangeLines, boolean placementRange) {
        int blockingStations = 0;
        Iterator<TileEntitySecurityStation> iterator = getSecurityStations(player.getEntityWorld(), pos, placementRange).iterator();
        for (TileEntitySecurityStation station; iterator.hasNext();) {
            station = iterator.next();
            if (!station.doesAllowPlayer(player)) {
                blockingStations++;
                if (showRangeLines) station.showRangeLines();
            }
        }
        return blockingStations;
    }

    static Stream<TileEntitySecurityStation> getSecurityStations(final World world, final BlockPos pos, final boolean placementRange) {
        return GlobalTileEntityCacheManager.getInstance().securityStations.stream()
                .filter(station -> isValidAndInRange(world, pos, placementRange, station));
    }

    private static boolean isValidAndInRange(World world, BlockPos pos, boolean placementRange, TileEntitySecurityStation station) {
        if (!station.isRemoved() && station.getWorld().getDimension().getType() == world.getDimension().getType() && station.hasValidNetwork()) {
            AxisAlignedBB aabb = station.getAffectedBoundingBox();
            if (placementRange) aabb = aabb.grow(16);
            return aabb.contains(new Vec3d(pos));
        }
        return false;
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
            return getDisplayNameInternal().appendText(" Hacking");
        }

        @Nullable
        @Override
        public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
            return new ContainerSecurityStationHacking(i, playerInventory, getPos());
        }
    }
}
