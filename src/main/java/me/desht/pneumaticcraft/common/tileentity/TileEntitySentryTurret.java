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

package me.desht.pneumaticcraft.common.tileentity;

import com.mojang.authlib.GameProfile;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.client.render.RenderMinigunTracers;
import me.desht.pneumaticcraft.common.ai.StringFilterEntitySelector;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerSentryTurret;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.item.ItemGunAmmo;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.PacketPlayMovingSound.MovingSoundFocus;
import me.desht.pneumaticcraft.common.util.EntityDistanceComparator;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

public class TileEntitySentryTurret extends TileEntityTickableBase implements
        IRedstoneControl<TileEntitySentryTurret>, IGUITextFieldSensitive, MenuProvider {
    private static final int INVENTORY_SIZE = 4;
    public static final String NBT_ENTITY_FILTER = "entityFilter";

    private final ItemStackHandler inventory = new TurretItemStackHandler(this);
    private final LazyOptional<IItemHandler> invCap = LazyOptional.of(() -> inventory);

    @GuiSynced
    private String entityFilter = "@mob";
    @GuiSynced
    private final RedstoneController<TileEntitySentryTurret> rsController = new RedstoneController<>(this);
    @DescSynced
    private double range;
    @DescSynced
    private boolean activated;
    @DescSynced
    private ItemStack minigunColorStack = ItemStack.EMPTY;
    private Minigun minigun;
    @DescSynced
    private int targetEntityId = -1;
    @DescSynced
    private boolean sweeping;
    private final SentryTurretEntitySelector entitySelector = new SentryTurretEntitySelector();
    private double rangeSq;
    private Vec3 tileVec;
    @DescSynced
    private float idleYaw;

    public TileEntitySentryTurret(BlockPos pos, BlockState state) {
        super(ModTileEntities.SENTRY_TURRET.get(), pos, state, 4);
    }

    @Override
    public void tickServer() {
        super.tickServer();

        if (getMinigun().getAttackTarget() == null && rsController.shouldRun()) {
            getMinigun().setSweeping(true);
            if ((nonNullLevel().getGameTime() & 0xF) == 0) {
                List<LivingEntity> entities = nonNullLevel().getEntitiesOfClass(LivingEntity.class, getTargetingBoundingBox(), entitySelector);
                if (entities.size() > 0) {
                    entities.sort(new EntityDistanceComparator(getBlockPos()));
                    getMinigun().setAttackTarget(entities.get(0));
                    targetEntityId = entities.get(0).getId();
                } else if (targetEntityId > 0) {
                    getMinigun().setReturning(true);
                    targetEntityId = -1;
                }
            }
        } else {
            getMinigun().setSweeping(false);
        }
        LivingEntity target = getMinigun().getAttackTarget();
        if (target != null) {
            if (rsController.shouldRun() && entitySelector.test(target)) {
                if ((nonNullLevel().getGameTime() & 0x7) == 0) {
                    // Make sure any knockback has the right direction.
                    getFakePlayer().setPos(getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5);
                    boolean usedUpAmmo = getMinigun().tryFireMinigun(target);
                    if (usedUpAmmo) {
                        clearEmptyAmmo();
                    }
                }
            } else {
                getMinigun().setAttackTarget(null);
                getMinigun().minigunYaw = idleYaw;
                targetEntityId = -1;
            }
        }
    }

    @Override
    public void tickCommonPost() {
        super.tickCommonPost();

        getMinigun().update(getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5);
    }

    private void clearEmptyAmmo() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.getItem() instanceof ItemGunAmmo && stack.getDamageValue() >= stack.getMaxDamage()) {
                inventory.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public void serializeExtraItemData(CompoundTag blockEntityTag, boolean preserveState) {
        blockEntityTag.putString(NBT_ENTITY_FILTER, getText(0));
    }

    private boolean canTurretSeeEntity(Entity entity) {
        Vec3 entityVec = new Vec3(entity.getX() + entity.getBbWidth() / 2, entity.getY() + entity.getBbHeight() / 2, entity.getZ() + entity.getBbWidth() / 2);
        ClipContext ctx = new ClipContext(entityVec, tileVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity);
        BlockHitResult trace = nonNullLevel().clip(ctx);
        return trace.getBlockPos().equals(getBlockPos());
    }

    private AABB getTargetingBoundingBox() {
        return new AABB(getBlockPos(), getBlockPos()).inflate(range);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return RenderMinigunTracers.shouldRender(getMinigun()) ?
                new AABB(getBlockPos(), getMinigun().getAttackTarget().blockPosition()) :
                super.getRenderBoundingBox();
    }

    @Override
    public void onLoad() {
        super.onLoad();

        tileVec = new Vec3(getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5);
        updateAmmo();
        onFilterChanged(entityFilter);
    }

    @Override
    public void onDescUpdate() {
        super.onDescUpdate();

        Entity entity = nonNullLevel().getEntity(targetEntityId);
        if (entity instanceof LivingEntity) {
            getMinigun().setAttackTarget((LivingEntity) entity);
        } else {
            getMinigun().setAttackTarget(null);
            getMinigun().setReturning(true);
        }
    }

    public Minigun getMinigun() {
        if (minigun == null) {
            minigun = new MinigunSentryTurret(nonNullLevel().isClientSide ? null : getFakePlayer());
            minigun.setWorld(getLevel());
            minigun.minigunYaw = idleYaw;
            minigun.setIdleYaw(idleYaw);
        }
        return minigun;
    }

    private Player getFakePlayer() {
        return FakePlayerFactory.get((ServerLevel) getLevel(), new GameProfile(null, "Sentry Turret"));
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", inventory.serializeNBT());
        tag.putString(NBT_ENTITY_FILTER, entityFilter);
        tag.putFloat("idleYaw", idleYaw);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        inventory.deserializeNBT(tag.getCompound("Items"));
        idleYaw = tag.getFloat("idleYaw");
        setText(0, tag.getString(NBT_ENTITY_FILTER));
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return inventory;
    }

    @Override
    public RedstoneController<TileEntitySentryTurret> getRedstoneController() {
        return rsController;
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer player) {
        rsController.parseRedstoneMode(tag);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        return new ContainerSentryTurret(i, playerInventory, getBlockPos());
    }

    public void setIdleYaw(float idleYaw) {
        this.idleYaw = Minigun.clampYaw(idleYaw);
    }

    private class TurretItemStackHandler extends BaseItemStackHandler {
        TurretItemStackHandler(BlockEntity te) {
            super(te, INVENTORY_SIZE);
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            updateAmmo();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack itemStack) {
            return itemStack.isEmpty() || itemStack.getItem() instanceof ItemGunAmmo;
        }
    }

    private void updateAmmo() {
        ItemStack ammo = ItemStack.EMPTY;
        for (int i = 0; i < inventory.getSlots(); i++) {
            ammo = inventory.getStackInSlot(i);
            if (!ammo.isEmpty()) {
                break;
            }
        }
        getMinigun().setAmmoStack(ammo);
        recalculateRange();
    }

    @Override
    protected LazyOptional<IItemHandler> getInventoryCap() {
        return invCap;
    }

    @Override
    public void onUpgradesChanged() {
        super.onUpgradesChanged();
        if (getLevel() != null) {
            // this can get called when reading nbt on load when world = null
            // in that case, range is recalculated in onFirstServerTick()
            recalculateRange();
        }
    }

    private void recalculateRange() {
        range = 16 + Math.min(16, getUpgrades(EnumUpgrade.RANGE));
        ItemStack ammoStack = getMinigun().getAmmoStack();
        if (ammoStack.getItem() instanceof ItemGunAmmo ammo) {
            range *= ammo.getRangeMultiplier(ammoStack);
        }
        rangeSq = range * range;
    }

    private class MinigunSentryTurret extends Minigun {

        MinigunSentryTurret(Player fakePlayer) {
            super(fakePlayer,true);
        }

        @Override
        public boolean isMinigunActivated() {
            return activated;
        }

        @Override
        public void setMinigunActivated(boolean activated) {
            if (!world.isClientSide) {
                // ony set server-side; TE sync's the activation state to client
                TileEntitySentryTurret.this.activated = activated;
            }
        }

        @Override
        public void setAmmoColorStack(@Nonnull ItemStack ammo) {
            if (!world.isClientSide) {
                // ony set server-side; TE sync's the activation state to client
                minigunColorStack = ammo;
            }
        }

        @Override
        public int getAmmoColor() {
            return getAmmoColor(minigunColorStack);
        }

        @Override
        public void playSound(SoundEvent soundName, float volume, float pitch) {
            world.playSound(null, getBlockPos(), soundName, SoundSource.BLOCKS, volume, pitch);
        }

        @Override
        public void setSweeping(boolean sweeping) {
            TileEntitySentryTurret.this.sweeping = sweeping;
        }

        @Override
        public boolean isSweeping() {
            return sweeping;
        }

        @Override
        public MovingSoundFocus getSoundSource() {
            return MovingSoundFocus.of(TileEntitySentryTurret.this);
        }

        @Override
        public boolean isValid() {
            return !TileEntitySentryTurret.this.isRemoved();
        }
    }

    private class SentryTurretEntitySelector extends StringFilterEntitySelector {
        @Override
        public boolean test(Entity entity) {
            if (entity instanceof Player player) {
                if (player.isCreative() || player.isSpectator() || isExcludedBySecurityStations(player)) return false;
            }
            return super.test(entity) && inRange(entity) && canTurretSeeEntity(entity);
        }

        private boolean inRange(Entity entity) {
            return PneumaticCraftUtils.distBetweenSq(new BlockPos(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ()), entity.getX(), entity.getY(), entity.getZ()) <= rangeSq;
        }

        private boolean isExcludedBySecurityStations(Player player) {
            Iterator<TileEntitySecurityStation> iterator = TileEntitySecurityStation.getSecurityStations(getLevel(), getBlockPos(), false).iterator();
            if (iterator.hasNext()) {
                // When there are any Security Stations, ALL stations need to be allowing the player
                while (iterator.hasNext()) {
                    if (!iterator.next().doesAllowPlayer(player)) return false;
                }
                return true;
            } else {
                // When there are no Security Stations at all, the player isn't protected
                return false;
            }
        }
    }

    @Override
    public void setText(int textFieldID, String text) {
        entityFilter = text;
        if (level != null && !level.isClientSide) {
            onFilterChanged(text);
            if (minigun != null) minigun.setAttackTarget(null);
        }
    }

    private void onFilterChanged(String text) {
        entitySelector.setFilter(text);
        setChanged();
    }

    @Override
    public String getText(int textFieldID) {
        return entityFilter;
    }
}
