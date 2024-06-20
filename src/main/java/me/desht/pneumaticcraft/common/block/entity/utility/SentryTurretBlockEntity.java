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

import com.mojang.authlib.GameProfile;
import me.desht.pneumaticcraft.common.block.entity.AbstractTickingBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.IGUITextFieldSensitive;
import me.desht.pneumaticcraft.common.block.entity.IRedstoneControl;
import me.desht.pneumaticcraft.common.block.entity.RedstoneController;
import me.desht.pneumaticcraft.common.inventory.SentryTurretMenu;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.item.minigun.AbstractGunAmmoItem;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.PacketPlayMovingSound.MovingSoundFocus;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.EntityDistanceComparator;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.StringFilterEntitySelector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentMap;
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
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

public class SentryTurretBlockEntity extends AbstractTickingBlockEntity implements
        IRedstoneControl<SentryTurretBlockEntity>, IGUITextFieldSensitive, MenuProvider {
    private static final int INVENTORY_SIZE = 4;
    public static final String NBT_ENTITY_FILTER = "entityFilter";
    private static final String FAKE_NAME = "Sentry Turret";
    private static final GameProfile FAKE_PROFILE = UUIDUtil.createOfflineProfile(FAKE_NAME);

    private final ItemStackHandler inventory = new TurretItemStackHandler(this);

    @GuiSynced
    private String entityFilter = "@mob";
    @GuiSynced
    private final RedstoneController<SentryTurretBlockEntity> rsController = new RedstoneController<>(this);
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

    public SentryTurretBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.SENTRY_TURRET.get(), pos, state, 4);
    }

    @Override
    public void tickServer() {
        super.tickServer();

        if (getMinigun().getAttackTarget() == null && rsController.shouldRun()) {
            getMinigun().setSweeping(true);
            if ((nonNullLevel().getGameTime() & 0xF) == 0) {
                List<LivingEntity> entities = nonNullLevel().getEntitiesOfClass(LivingEntity.class, getTargetingBoundingBox(), entitySelector);
                if (!entities.isEmpty()) {
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

        getMinigun().setIdleYaw(idleYaw);
        getMinigun().tick(getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5);
    }

    private void clearEmptyAmmo() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.getItem() instanceof AbstractGunAmmoItem && stack.getDamageValue() >= stack.getMaxDamage()) {
                inventory.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);

        entityFilter = componentInput.getOrDefault(ModDataComponents.ENTITY_FILTER, "");
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);

        builder.set(ModDataComponents.ENTITY_FILTER, entityFilter);
    }

    private boolean canTurretSeeEntity(Entity entity) {
        Vec3 entityVec = new Vec3(entity.getX() + entity.getBbWidth() / 2, entity.getY() + entity.getBbHeight() / 2, entity.getZ() + entity.getBbWidth() / 2);
        ClipContext ctx = new ClipContext(entityVec, tileVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity);
        BlockHitResult trace = nonNullLevel().clip(ctx);
        return trace.getBlockPos().equals(getBlockPos());
    }

    private AABB getTargetingBoundingBox() {
        return new AABB(getBlockPos()).inflate(range);
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
        }
        return minigun;
    }

    private Player getFakePlayer() {
        return FakePlayerFactory.get((ServerLevel) getLevel(), FAKE_PROFILE);
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put("Items", inventory.serializeNBT(provider));
        tag.putString(NBT_ENTITY_FILTER, entityFilter);
        tag.putFloat("idleYaw", idleYaw);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        inventory.deserializeNBT(provider, tag.getCompound("Items"));
        idleYaw = tag.getFloat("idleYaw");
        setText(0, tag.getString(NBT_ENTITY_FILTER));
    }

    @Override
    public IItemHandler getItemHandler(@Nullable Direction dir) {
        return inventory;
    }

    @Override
    public RedstoneController<SentryTurretBlockEntity> getRedstoneController() {
        return rsController;
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer player) {
        rsController.parseRedstoneMode(tag);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        return new SentryTurretMenu(i, playerInventory, getBlockPos());
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
            return itemStack.isEmpty() || itemStack.getItem() instanceof AbstractGunAmmoItem;
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
    public void onUpgradesChanged() {
        super.onUpgradesChanged();
        if (getLevel() != null) {
            // this can get called when reading nbt on load when world = null
            // in that case, range is recalculated in onFirstServerTick()
            recalculateRange();
        }
        getMinigun().setInfiniteAmmo(getUpgrades(ModUpgrades.CREATIVE.get()) > 0);
    }

    private void recalculateRange() {
        range = 16 + Math.min(16, getUpgrades(ModUpgrades.RANGE.get()));
        ItemStack ammoStack = getMinigun().getAmmoStack();
        if (ammoStack.getItem() instanceof AbstractGunAmmoItem ammo) {
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
                // ony set server-side; BE sync's the activation state to client
                SentryTurretBlockEntity.this.activated = activated;
            }
        }

        @Override
        public void setAmmoColorStack(@Nonnull ItemStack ammo) {
            if (!world.isClientSide) {
                // ony set server-side; BE sync's the activation state to client
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
        public Vec3 getMuzzlePosition() {
            Vec3 centre = Vec3.atCenterOf(getPosition());
            LivingEntity target = minigun.getAttackTarget();
            if (target == null) return null;
            Vec3 offset = target.position()
                    .add(0, target.getBbHeight() / 2, 0)
                    .subtract(centre)
                    .add(0, 0.5, 0)
                    .normalize().scale(1.5);
            return centre.add(offset);
        }

        @Override
        public Vec3 getLookAngle() {
            return Vec3.directionFromRotation(minigunPitch, minigunYaw).normalize();
        }

        @Override
        public float getParticleScale() {
            return 1f;
        }

        @Override
        public void setSweeping(boolean sweeping) {
            SentryTurretBlockEntity.this.sweeping = sweeping;
        }

        @Override
        public boolean isSweeping() {
            return sweeping;
        }

        @Override
        public MovingSoundFocus getSoundSource() {
            return MovingSoundFocus.of(SentryTurretBlockEntity.this);
        }

        @Override
        public boolean isValid() {
            return !SentryTurretBlockEntity.this.isRemoved();
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
            Iterator<SecurityStationBlockEntity> iterator = SecurityStationBlockEntity.getSecurityStations(getLevel(), getBlockPos(), false).iterator();
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
