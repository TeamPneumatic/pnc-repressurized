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
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.fakeplayer.FakeNetHandlerPlayerServer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class TileEntitySentryTurret extends TileEntityTickableBase implements IRedstoneControlled, IGUITextFieldSensitive, INamedContainerProvider {
    private static final int INVENTORY_SIZE = 4;
    public static final String NBT_ENTITY_FILTER = "entityFilter";

    private final ItemStackHandler inventory = new TurretItemStackHandler(this);
    private final LazyOptional<IItemHandler> invCap = LazyOptional.of(() -> inventory);

    @GuiSynced
    private String entityFilter = "@mob";
    @GuiSynced
    private int redstoneMode;
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
    private Vec3d tileVec;
    @DescSynced
    public float idleYaw;

    public TileEntitySentryTurret() {
        super(ModTileEntities.SENTRY_TURRET.get(), 4);
    }

    @Override
    public void tick() {
        super.tick();
        if (!getWorld().isRemote) {
            if (getMinigun().getAttackTarget() == null && redstoneAllows()) {
                if (!MathHelper.epsilonEquals(getMinigun().minigunYaw, getMinigun().getIdleYaw())) {
                    getMinigun().setReturning(true);
                }
                getMinigun().setSweeping(true);
                if ((getWorld().getGameTime() & 0xF) == 0) {
                    List<LivingEntity> entities = getWorld().getEntitiesWithinAABB(LivingEntity.class, getTargetingBoundingBox(), entitySelector);
                    if (entities.size() > 0) {
                        entities.sort(new TargetSorter());
                        getMinigun().setAttackTarget(entities.get(0));
                        targetEntityId = entities.get(0).getEntityId();
                    } else {
                        targetEntityId = -1;
                    }
                }
            } else {
                getMinigun().setSweeping(false);
            }
            LivingEntity target = getMinigun().getAttackTarget();
            if (target != null) {
                if (!redstoneAllows() || !entitySelector.apply(target)) {
                    getMinigun().setAttackTarget(null);
                    getMinigun().minigunYaw = idleYaw;
                    targetEntityId = -1;
                } else {
                    if ((getWorld().getGameTime() & 0x7) == 0) {
                        // Make sure any knockback has the right direction.
                        getFakePlayer().setPosition(getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5);
                        boolean usedAmmo = getMinigun().tryFireMinigun(target);
                        if (usedAmmo) {
                            for (int i = 0; i < inventory.getSlots(); i++) {
                                if (!inventory.getStackInSlot(i).isEmpty()) {
                                    inventory.setStackInSlot(i, ItemStack.EMPTY);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        getMinigun().update(getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5);
    }

    @Override
    public void serializeExtraItemData(CompoundNBT blockEntityTag) {
        blockEntityTag.putString(NBT_ENTITY_FILTER, getText(0));
    }

    private boolean canSeeEntity(Entity entity) {
        Vec3d entityVec = new Vec3d(entity.getPosX() + entity.getWidth() / 2, entity.getPosY() + entity.getHeight() / 2, entity.getPosZ() + entity.getWidth() / 2);
        RayTraceContext ctx = new RayTraceContext(entityVec, tileVec, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, entity);
        BlockRayTraceResult trace = getWorld().rayTraceBlocks(ctx);
        return trace.getPos().equals(getPos());
    }

    private AxisAlignedBB getTargetingBoundingBox() {
        return new AxisAlignedBB(getPos()).grow(range);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return RenderMinigunTracers.shouldRender(getMinigun()) ?
                new AxisAlignedBB(getPos(), getMinigun().getAttackTarget().getPosition()) :
                super.getRenderBoundingBox();
    }

    @Override
    protected void onFirstServerTick() {
        super.onFirstServerTick();
        tileVec = new Vec3d(getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5);
        updateAmmo();
        onFilterChanged(entityFilter);
    }

    @Override
    public void onDescUpdate() {
        super.onDescUpdate();

        Entity entity = getWorld().getEntityByID(targetEntityId);
        if (entity instanceof LivingEntity) {
            getMinigun().setAttackTarget((LivingEntity) entity);
        } else {
            getMinigun().setAttackTarget(null);
            getMinigun().setReturning(true);
        }
    }

    public Minigun getMinigun() {
        if (minigun == null) {
            minigun = new MinigunSentryTurret();
            minigun.setWorld(getWorld());
            if (!getWorld().isRemote) {
                minigun.setPlayer(getFakePlayer());
            }
            minigun.minigunYaw = idleYaw;
            minigun.setIdleYaw(idleYaw);
        }
        return minigun;
    }

    private PlayerEntity getFakePlayer() {
        FakePlayer fakePlayer = FakePlayerFactory.get((ServerWorld) getWorld(), new GameProfile(null, "Sentry Turret"));
        if (fakePlayer.connection == null) {
            fakePlayer.connection = new FakeNetHandlerPlayerServer(ServerLifecycleHooks.getCurrentServer(), fakePlayer);
        }
        return fakePlayer;
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        tag.put("Items", inventory.serializeNBT());
        tag.putByte("redstoneMode", (byte) redstoneMode);
        tag.putString(NBT_ENTITY_FILTER, entityFilter);
        tag.putFloat("idleYaw", idleYaw);
        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        inventory.deserializeNBT(tag.getCompound("Items"));
        redstoneMode = tag.getByte("redstoneMode");
        idleYaw = tag.getFloat("idleYaw");
        setText(0, tag.getString(NBT_ENTITY_FILTER));
    }

    @Override
    public boolean redstoneAllows() {
        return redstoneMode == 3 || super.redstoneAllows();
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
    public void handleGUIButtonPress(String tag, boolean shiftHeld, PlayerEntity player) {
        if (tag.equals(IGUIButtonSensitive.REDSTONE_TAG)) {
            redstoneMode++;
            if (redstoneMode > 2) redstoneMode = 0;
        }
    }

    @Override
    public ITextComponent getDisplayName() {
        return getDisplayNameInternal();
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerSentryTurret(i, playerInventory, getPos());
    }

    private class TurretItemStackHandler extends BaseItemStackHandler {
        TurretItemStackHandler(TileEntity te) {
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
        if (getWorld() != null) {
            // this can get called when reading nbt on load when world = null
            // in that case, range is recalculated in onFirstServerUpdate()
            recalculateRange();
        }
    }

    private void recalculateRange() {
        range = 16.0 + Math.min(16, getUpgrades(EnumUpgrade.RANGE));
        ItemStack ammoStack = getMinigun().getAmmoStack();
        if (ammoStack.getItem() instanceof ItemGunAmmo) {
            range *= ((ItemGunAmmo) ammoStack.getItem()).getRangeMultiplier(ammoStack);
        }
        rangeSq = range * range;
    }

    private class MinigunSentryTurret extends Minigun {

        MinigunSentryTurret() {
            super(true);
        }

        @Override
        public boolean isMinigunActivated() {
            return activated;
        }

        @Override
        public void setMinigunActivated(boolean activated) {
            TileEntitySentryTurret.this.activated = activated;
        }

        @Override
        public void setAmmoColorStack(@Nonnull ItemStack ammo) {
            minigunColorStack = ammo;
        }

        @Override
        public int getAmmoColor() {
            return getAmmoColor(minigunColorStack);
        }

        @Override
        public void playSound(SoundEvent soundName, float volume, float pitch) {
            NetworkHandler.sendToAllAround(new PacketPlaySound(soundName, SoundCategory.BLOCKS,
                    getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5,
                    volume, pitch, false), world);
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
        public Object getSoundSource() {
            return TileEntitySentryTurret.this.getPos();
        }

        @Override
        public boolean isValid() {
            return !TileEntitySentryTurret.this.isRemoved();
        }
    }

    private class TargetSorter implements Comparator<Entity> {
        private final BlockPos pos;

        TargetSorter() {
            pos = new BlockPos(getPos().getX(), getPos().getY(), getPos().getZ());
        }

        @Override
        public int compare(Entity arg0, Entity arg1) {
            double dist1 = PneumaticCraftUtils.distBetweenSq(pos, arg0.getPosition());
            double dist2 = PneumaticCraftUtils.distBetweenSq(pos, arg1.getPosition());
            return Double.compare(dist1, dist2);
        }
    }

    private class SentryTurretEntitySelector extends StringFilterEntitySelector {
        @Override
        public boolean apply(Entity entity) {
            if (entity instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) entity;
                if (player.isCreative() || player.isSpectator() || isExcludedBySecurityStations(player)) return false;
            }
            return super.apply(entity) && inRange(entity) && canSeeEntity(entity);
        }

        private boolean inRange(Entity entity) {
            return PneumaticCraftUtils.distBetweenSq(new BlockPos(getPos().getX(), getPos().getY(), getPos().getZ()), entity.getPosX(), entity.getPosY(), entity.getPosZ()) <= rangeSq;
        }

        private boolean isExcludedBySecurityStations(PlayerEntity player) {
            Iterator<TileEntitySecurityStation> iterator = TileEntitySecurityStation.getSecurityStations(getWorld(), getPos(), false).iterator();
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
        if (world != null && !world.isRemote) {
            onFilterChanged(text);
            if (minigun != null) minigun.setAttackTarget(null);
        }
    }

    private void onFilterChanged(String text) {
        entitySelector.setFilter(text);
        markDirty();
    }

    @Override
    public String getText(int textFieldID) {
        return entityFilter;
    }
}
