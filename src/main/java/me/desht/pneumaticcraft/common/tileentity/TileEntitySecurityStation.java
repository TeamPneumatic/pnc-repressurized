package me.desht.pneumaticcraft.common.tileentity;

import com.mojang.authlib.GameProfile;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.client.render.RenderRangeLines;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.item.ItemNetworkComponents;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketRenderRangeLines;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import me.desht.pneumaticcraft.proxy.CommonProxy.EnumGuiId;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TileEntitySecurityStation extends TileEntityBase implements IGUITextFieldSensitive,
        IRangeLineShower, IRedstoneControl {
    private SecurityStationHandler inventory;
    private static final int INVENTORY_SIZE = 35;

    public final List<GameProfile> hackedUsers = new ArrayList<>(); // Stores all the users that have hacked this Security Station.
    public final List<GameProfile> sharedUsers = new ArrayList<>(); // Stores all the users that have been allowed by the stationOwner.
    @GuiSynced
    private int rebootTimer; // When the player decides to reset the station, this variable will hold the remaining reboot time.
    @GuiSynced
    public String textFieldText = "";
    private int securityRange;
    private int oldSecurityRange; //range used by the range line renderer, to figure out if the range has been changed.
    private final RenderRangeLines rangeLineRenderer = new RenderRangeLines(0x33FF0000);

    @GuiSynced
    public int redstoneMode;
    public boolean oldRedstoneStatus;

    private boolean validNetwork;

    public TileEntitySecurityStation() {
        super(4);
        inventory = new SecurityStationHandler();
        addApplicableUpgrade(EnumUpgrade.ENTITY_TRACKER, EnumUpgrade.SECURITY, EnumUpgrade.RANGE);
    }

    @Override
    public void update() {
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
                rangeLineRenderer.resetRendering(getSecurityRange());
                oldSecurityRange = getSecurityRange();
            }
            rangeLineRenderer.update();
        }
        if (/* !getWorld().isRemote && */oldRedstoneStatus != shouldEmitRedstone()) {
            oldRedstoneStatus = shouldEmitRedstone();
            updateNeighbours();
        }

        securityRange = Math.min(2 + getUpgrades(EnumUpgrade.RANGE), TileEntityConstants.SECURITY_STATION_MAX_RANGE);

        super.update();

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
            rangeLineRenderer.resetRendering(getSecurityRange());
        } else {
            NetworkHandler.sendToAllAround(new PacketRenderRangeLines(this), getWorld(), TileEntityConstants.PACKET_UPDATE_DISTANCE + getSecurityRange());
        }
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return inventory;
    }

    @SideOnly(Side.CLIENT)
    public void renderRangeLines() {
        rangeLineRenderer.render();
    }

    @Override
    public void handleGUIButtonPress(int buttonID, EntityPlayer player) {
        if (buttonID == 0) {
            redstoneMode++;
            if (redstoneMode > 2) redstoneMode = 0;
            updateNeighbours();
        } else if (buttonID == 2) {
            rebootStation();
        } else if (buttonID == 3) {
            if (!hasValidNetwork()) {
                player.sendStatusMessage(new TextComponentTranslation(TextFormatting.GREEN + "This Security Station is out of order: Its network hasn't been properly configured."), false);
            } else {
                player.openGui(PneumaticCraftRepressurized.instance, EnumGuiId.HACKING.ordinal(), getWorld(), getPos().getX(), getPos().getY(), getPos().getZ());
            }
        } else if (buttonID > 3 && buttonID - 4 < sharedUsers.size()) {
            sharedUsers.remove(buttonID - 4);
        }
        sendDescriptionPacket();
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
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        if (!rangeLineRenderer.isCurrentlyRendering()) return super.getRenderBoundingBox();
        int range = getSecurityRange();
        return new AxisAlignedBB(getPos().getX() - range, getPos().getY() - range, getPos().getZ() - range, getPos().getX() + 1 + range, getPos().getY() + 1 + range, getPos().getZ() + 1 + range);
    }

    public int getSecurityRange() {
        return securityRange;
    }

    @Override
    public String getName() {
        return Blockss.SECURITY_STATION.getUnlocalizedName();
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        redstoneMode = tag.getInteger("redstoneMode");
        rebootTimer = tag.getInteger("startupTimer");
        inventory = new SecurityStationHandler();
        inventory.deserializeNBT(tag.getCompoundTag("Items"));
        checkForNetworkValidity();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("redstoneMode", redstoneMode);
        tag.setInteger("startupTimer", rebootTimer);
        tag.setTag("Items", inventory.serializeNBT());
        return tag;
    }

    @Override
    public void writeToPacket(NBTTagCompound tag) {
        super.writeToPacket(tag);
        NBTTagList sharedList = new NBTTagList();
        for (GameProfile sharedUser : sharedUsers) {
            NBTTagCompound tagCompound = new NBTTagCompound();
            tagCompound.setString("name", sharedUser.getName());
            if (sharedUser.getId() != null)
                tagCompound.setString("uuid", sharedUser.getId().toString());
            sharedList.appendTag(tagCompound);
        }
        tag.setTag("SharedUsers", sharedList);

        NBTTagList hackedList = new NBTTagList();
        for (GameProfile hackedUser : hackedUsers) {
            NBTTagCompound tagCompound = new NBTTagCompound();
            tagCompound.setString("name", hackedUser.getName());
            if (hackedUser.getId() != null)
                tagCompound.setString("uuid", hackedUser.getId().toString());
            hackedList.appendTag(tagCompound);
        }
        tag.setTag("HackedUsers", hackedList);
    }

    @Override
    public void readFromPacket(NBTTagCompound tag) {
        super.readFromPacket(tag);
        sharedUsers.clear();
        NBTTagList sharedList = tag.getTagList("SharedUsers", 10);
        for (int i = 0; i < sharedList.tagCount(); ++i) {
            NBTTagCompound tagCompound = sharedList.getCompoundTagAt(i);
            sharedUsers.add(new GameProfile(tagCompound.hasKey("uuid") ? UUID.fromString(tagCompound.getString("uuid")) : null, tagCompound.getString("name")));
        }

        hackedUsers.clear();
        NBTTagList hackedList = tag.getTagList("HackedUsers", 10);
        for (int i = 0; i < hackedList.tagCount(); ++i) {
            NBTTagCompound tagCompound = hackedList.getCompoundTagAt(i);
            hackedUsers.add(new GameProfile(tagCompound.hasKey("uuid") ? UUID.fromString(tagCompound.getString("uuid")) : null, tagCompound.getString("name")));
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
     * Returns true if the given player is allowed to interact with the covered area of this Security Station.
     *
     * @param player
     * @return
     */
    public boolean doesAllowPlayer(EntityPlayer player) {
        return rebootTimer > 0 || isPlayerOnWhiteList(player) || hasPlayerHacked(player);
    }

    public boolean isPlayerOnWhiteList(EntityPlayer player) {
        for (int i = 0; i < sharedUsers.size(); i++) {
            GameProfile user = sharedUsers.get(i);
            if (gameProfileEquals(user, player.getGameProfile())) {
                if (user.getId() == null && player.getGameProfile().getId() != null) {
                    sharedUsers.set(i, player.getGameProfile());
                    Log.info("Legacy conversion: Security Station shared username '" + player.getName() + "' is now using UUID '" + player.getGameProfile().getId() + "'.");
                }
                return true;
            }
        }
        return false;
    }

    public boolean hasPlayerHacked(EntityPlayer player) {
        for (int i = 0; i < hackedUsers.size(); i++) {
            GameProfile user = hackedUsers.get(i);
            if (gameProfileEquals(user, player.getGameProfile())) {
                if (user.getId() == null && player.getGameProfile().getId() != null) {
                    hackedUsers.set(i, player.getGameProfile());
                    Log.info("Legacy conversion: Security Station hacked username '" + player.getName() + "' is now using UUID '" + player.getGameProfile().getId() + "'.");
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the given slots are connected in the network. For this to be true both slots need to have a network component stored as well.
     *
     * @param firstSlot
     * @param secondSlot
     * @return
     */
    public boolean connects(int firstSlot, int secondSlot) {
        if (firstSlot < 0 || secondSlot < 0 || firstSlot >= 35 || secondSlot >= 35 || firstSlot == secondSlot
                || inventory.getStackInSlot(firstSlot).isEmpty() || inventory.getStackInSlot(secondSlot).isEmpty())
            return false;

        for (int column = -1; column <= 1; column++) {
            for (int row = -1; row <= 1; row++) {
                if (firstSlot + row * 5 + column == secondSlot) {
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

    public enum EnumNetworkValidityProblem {
        NONE, NO_SUBROUTINE, NO_IO_PORT, NO_REGISTRY, TOO_MANY_SUBROUTINES, TOO_MANY_IO_PORTS, TOO_MANY_REGISTRIES, NO_CONNECTION_SUB_AND_IO_PORT, NO_CONNECTION_IO_PORT_AND_REGISTRY
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
                switch (inventory.getStackInSlot(i).getItemDamage()) {
                    case ItemNetworkComponents.DIAGNOSTIC_SUBROUTINE:
                        if (subroutineSlot != -1)
                            return EnumNetworkValidityProblem.TOO_MANY_SUBROUTINES; //only one subroutine per network
                        subroutineSlot = i;
                        break;
                    case ItemNetworkComponents.NETWORK_IO_PORT:
                        if (ioPortSlot != -1)
                            return EnumNetworkValidityProblem.TOO_MANY_IO_PORTS; //only one IO port per network
                        ioPortSlot = i;
                        break;
                    case ItemNetworkComponents.NETWORK_REGISTRY:
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
    public boolean isGuiUseableByPlayer(EntityPlayer par1EntityPlayer) {
        return getWorld().getTileEntity(getPos()) == this;
    }

    @Override
    public int getRedstoneMode() {
        return redstoneMode;
    }

    private class SecurityStationHandler extends ItemStackHandler {
        private SecurityStationHandler() {
            super(INVENTORY_SIZE);
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            checkForNetworkValidity();
        }
    }
}
