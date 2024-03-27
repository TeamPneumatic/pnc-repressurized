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

package me.desht.pneumaticcraft.common.block.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.math.IntMath;
import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.common.block.entity.RedstoneController.ReceivingRedstoneMode;
import me.desht.pneumaticcraft.common.block.entity.RedstoneController.RedstoneMode;
import me.desht.pneumaticcraft.common.inventory.PneumaticDoorBaseMenu;
import me.desht.pneumaticcraft.common.network.*;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModSounds;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.BlockEntityConstants;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;
import static me.desht.pneumaticcraft.lib.BlockEntityConstants.PNEUMATIC_DOOR_EXTENSION;
import static me.desht.pneumaticcraft.lib.BlockEntityConstants.PNEUMATIC_DOOR_SPEED_FAST;

public class PneumaticDoorBaseBlockEntity extends AbstractAirHandlingBlockEntity implements
        IRedstoneControl<PneumaticDoorBaseBlockEntity>, IMinWorkingPressure, CamouflageableBlockEntity, MenuProvider {
    private static final List<RedstoneMode<PneumaticDoorBaseBlockEntity>> REDSTONE_MODES = ImmutableList.of(
            new ReceivingRedstoneMode<>("pneumaticDoor.playerNearby", new ItemStack(Items.OBSERVER),
                    te -> true),
            new ReceivingRedstoneMode<>("pneumaticDoor.playerNearbyAndLooking", new ItemStack(Items.ENDER_EYE),
                    te -> true),
            new ReceivingRedstoneMode<>("pneumaticDoor.woodenDoor", new ItemStack(Items.OAK_DOOR),
                    te -> true),
            new ReceivingRedstoneMode<>("pneumaticDoor.ironDoor", new ItemStack(Items.IRON_DOOR),
                    te -> true)
    );

    public static final int INVENTORY_SIZE = 1;

    private static final int RS_MODE_NEAR = 0;
    private static final int RS_MODE_NEAR_LOOKING = 1;
    public static final int RS_MODE_WOODEN_DOOR = 2;
    public static final int RS_MODE_IRON_DOOR = 3;

    private PneumaticDoorBlockEntity door;
    private PneumaticDoorBaseBlockEntity doubleDoor;
    @DescSynced
    public boolean rightGoing;
    public float oldProgress;
    @DescSynced
    @LazySynced
    public float progress;
    @DescSynced
    private boolean opening;
    public boolean wasPowered;
    private BlockState camoState;
    @GuiSynced
    public final RedstoneController<PneumaticDoorBaseBlockEntity> rsController = new RedstoneController<>(this, REDSTONE_MODES);
    @DescSynced
    private float speedMultiplier;
    @GuiSynced
    private boolean passSignal;

    private int rangeSq;

    public PneumaticDoorBaseBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.PNEUMATIC_DOOR_BASE.get(), pos, state, PressureTier.TIER_ONE, PneumaticValues.VOLUME_PNEUMATIC_DOOR, 4);
    }

    @Override
    public boolean hasItemCapability() {
        return false;
    }

    @Override
    public void tickCommonPre() {
        super.tickCommonPre();

        oldProgress = progress;
        float targetProgress = opening ? 1F : 0F;
        if (progress < targetProgress) {
            if (progress > 0.05 && progress < targetProgress - PNEUMATIC_DOOR_EXTENSION) {
                progress += PNEUMATIC_DOOR_SPEED_FAST * speedMultiplier;
            } else {
                progress += Math.min(0.02, BlockEntityConstants.PNEUMATIC_DOOR_SPEED_SLOW * speedMultiplier);
            }
            if (progress > targetProgress) progress = targetProgress;
        }
        if (progress > targetProgress) {
            if (progress < 0.95 && progress > targetProgress + PNEUMATIC_DOOR_EXTENSION) {
                progress -= PNEUMATIC_DOOR_SPEED_FAST * speedMultiplier;
            } else {
                progress -= Math.min(0.02, BlockEntityConstants.PNEUMATIC_DOOR_SPEED_SLOW * speedMultiplier);
            }
            if (progress < targetProgress) progress = targetProgress;
        }

        door = getDoor();
        if (door != null) {
            door.setRotationAngle(progress * 90);
        }
    }

    @Override
    public void tickServer() {
        super.tickServer();

        if (getPressure() >= PneumaticValues.MIN_PRESSURE_PNEUMATIC_DOOR) {
            if ((nonNullLevel().getGameTime() & 0x3f) == 0) {
                BlockEntity te = nonNullLevel().getBlockEntity(getBlockPos().relative(getRotation(), 3));
                if (te instanceof PneumaticDoorBaseBlockEntity) {
                    doubleDoor = (PneumaticDoorBaseBlockEntity) te;
                } else {
                    doubleDoor = null;
                }
            }
            setOpening(shouldOpen() || isNeighborOpening());
            setNeighborOpening(isOpening());
        } else {
            setOpening(true);
        }
        speedMultiplier = getSpeedMultiplierFromUpgrades();

        if (!PneumaticCraftUtils.epsilonEquals(oldProgress, progress)) {
            addAir((int) (-Math.abs(oldProgress - progress) * PneumaticValues.USAGE_PNEUMATIC_DOOR * (getSpeedUsageMultiplierFromUpgrades() / speedMultiplier)));
        }

        if (door != null) {
            rightGoing = door.rightGoing;
        }
    }

    @Override
    public void onUpgradesChanged() {
        super.onUpgradesChanged();

        rangeSq = IntMath.pow(BlockEntityConstants.RANGE_PNEUMATIC_DOOR_BASE + this.getUpgrades(ModUpgrades.RANGE.get()), 2);
    }

    private boolean shouldOpen() {
        if (door == null) return false;
        return switch (rsController.getCurrentMode()) {
            case RS_MODE_NEAR -> hasAnyValidPlayer(player -> true);
            case RS_MODE_NEAR_LOOKING -> hasAnyValidPlayer(this::isPlayerLookingAtDoor);
            case RS_MODE_WOODEN_DOOR, RS_MODE_IRON_DOOR -> rsController.getCurrentRedstonePower() > 0 || opening;
            default -> false;
        };
    }

    private boolean hasAnyValidPlayer(Predicate<ServerPlayer> pred) {
        if (level != null && level.getServer() != null) {
            Vec3 vec = Vec3.atCenterOf(getBlockPos().relative(getRotation()));
            return level.getServer().getPlayerList().getPlayers().stream()
                    .filter(player -> player.distanceToSqr(vec) <= rangeSq)
                    .filter(player -> !SecurityStationBlockEntity.isProtectedFromPlayer(player, getBlockPos(), false))
                    .anyMatch(pred);
        }
        return false;
    }

    private boolean isPlayerLookingAtDoor(ServerPlayer player) {
        Vec3 eyePos = player.getEyePosition(0f);
        // rangeSq is longer than we need, but we've already done a proximity check, so...
        Vec3 endPos = eyePos.add(player.getLookAngle().scale(rangeSq));
        return door.getRenderBoundingBox().clip(eyePos, endPos).isPresent();
    }

    public void setOpening(boolean opening) {
        boolean wasOpening = this.opening;
        this.opening = opening;
        if (this.opening != wasOpening) {
            NetworkHandler.sendToAllTracking(new PacketPlaySound(ModSounds.PNEUMATIC_DOOR.get(), SoundSource.BLOCKS, getBlockPos(), 1.0F, 1.0F, false),this);
            sendDescriptionPacket();
        }
    }

    public boolean isOpening() {
        return opening;
    }

    private boolean isNeighborOpening() {
        return doubleDoor != null && doubleDoor.shouldOpen();
    }

    public void setNeighborOpening(boolean opening) {
        if (doubleDoor != null && doubleDoor.getPressure() >= PneumaticValues.MIN_PRESSURE_PNEUMATIC_DOOR) {
            doubleDoor.setOpening(opening);
        }
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        return side != Direction.UP;
    }

    private PneumaticDoorBlockEntity getDoor() {
        return nonNullLevel().getBlockEntity(getBlockPos().relative(getRotation()).below(), ModBlockEntityTypes.PNEUMATIC_DOOR.get()).map(teDoor -> {
            if (getRotation().getClockWise() == teDoor.getRotation() && !teDoor.rightGoing) {
                return teDoor;
            } else if (getRotation().getCounterClockWise() == teDoor.getRotation() && teDoor.rightGoing) {
                return teDoor;
            } else {
                return null;
            }
        }).orElse(null);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        progress = tag.getFloat("extension");
        opening = tag.getBoolean("opening");
        rightGoing = tag.getBoolean("rightGoing");
        passSignal = tag.getBoolean("passSignal");
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putFloat("extension", progress);
        tag.putBoolean("opening", opening);
        tag.putBoolean("rightGoing", rightGoing);
        tag.putBoolean("passSignal", passSignal);
    }

    @Override
    public void writeToPacket(CompoundTag tag) {
        super.writeToPacket(tag);

        CamouflageableBlockEntity.writeCamo(tag, camoState);
    }

    @Override
    public void readFromPacket(CompoundTag tag) {
        super.readFromPacket(tag);

        camoState = CamouflageableBlockEntity.readCamo(tag);
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer player) {
        if (rsController.parseRedstoneMode(tag)) return;

        if (tag.equals("pass_signal")) {
            passSignal = !passSignal;
            updateNeighbours();
            setChanged();
        }
    }

    @Override
    public IItemHandler getItemHandler(@org.jetbrains.annotations.Nullable Direction dir) {
        return null;
    }

    @Override
    public float getMinWorkingPressure() {
        return PneumaticValues.MIN_PRESSURE_PNEUMATIC_DOOR;
    }

    @Override
    public RedstoneController<PneumaticDoorBaseBlockEntity> getRedstoneController() {
        return rsController;
    }

    @Override
    public BlockState getCamouflage() {
        return camoState;
    }

    @Override
    public void setCamouflage(BlockState state) {
        camoState = state;
        CamouflageableBlockEntity.syncToClient(this);
    }

    @Override
    public MutableComponent getRedstoneTabTitle() {
        return xlate("pneumaticcraft.gui.tab.redstoneBehaviour.pneumaticDoor.openWhen");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        return new PneumaticDoorBaseMenu(i, playerInventory, getBlockPos());
    }

    public boolean shouldPassSignalToDoor() {
        return passSignal;
    }
}
