package me.desht.pneumaticcraft.common.tileentity;

import com.mojang.authlib.GameProfile;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.common.ai.StringFilterEntitySelector;
import me.desht.pneumaticcraft.common.core.ModTileEntityTypes;
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
import net.minecraft.world.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class TileEntitySentryTurret extends TileEntityTickableBase implements IRedstoneControlled, IGUITextFieldSensitive, INamedContainerProvider {
    private static final int INVENTORY_SIZE = 4;

    private final ItemStackHandler inventory = new TurretItemStackHandler(this);
    private final LazyOptional<IItemHandlerModifiable> invCap = LazyOptional.of(() -> inventory);

    @GuiSynced
    private String entityFilter = "";
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

    public TileEntitySentryTurret() {
        super(ModTileEntityTypes.SENTRY_TURRET, 4);
        addApplicableUpgrade(EnumUpgrade.RANGE);
    }

    @Override
    public void tick() {
        super.tick();
        if (!getWorld().isRemote) {
            if (getMinigun().getAttackTarget() == null && redstoneAllows()) {
                getMinigun().setSweeping(true);
                if ((getWorld().getGameTime() & 0xF) == 0) {
                    List<LivingEntity> entities = getWorld().getEntitiesWithinAABB(LivingEntity.class, getTargetingBoundingBox(), entitySelector);
                    if (entities.size() > 0) {
                        entities.sort(new TargetSorter());
                        getMinigun().setAttackTarget(entities.get(0));
                        targetEntityId = entities.get(0).getEntityId();
                    }
                }
            } else {
                getMinigun().setSweeping(false);
            }
            LivingEntity target = getMinigun().getAttackTarget();
            if (target != null) {
                if (!redstoneAllows() || !entitySelector.apply(target)) {
                    getMinigun().setAttackTarget(null);
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

    private boolean canSeeEntity(Entity entity) {
        Vec3d entityVec = new Vec3d(entity.posX + entity.getWidth() / 2, entity.posY + entity.getHeight() / 2, entity.posZ + entity.getWidth() / 2);
        RayTraceContext ctx = new RayTraceContext(entityVec, tileVec, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, entity);
        BlockRayTraceResult trace = getWorld().rayTraceBlocks(ctx);
        return trace.getPos().equals(getPos());
    }

    private AxisAlignedBB getTargetingBoundingBox() {
        return new AxisAlignedBB(getPos()).grow(range);
    }

    @Override
    protected void onFirstServerUpdate() {
        super.onFirstServerUpdate();
        tileVec = new Vec3d(getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5);
        updateAmmo();
    }

    @Override
    public void onDescUpdate() {
        super.onDescUpdate();
        Entity entity = getWorld().getEntityByID(targetEntityId);
        if (entity instanceof LivingEntity) {
            getMinigun().setAttackTarget((LivingEntity) entity);
        } else {
            getMinigun().setAttackTarget(null);
        }
    }

    public Minigun getMinigun() {
        if (minigun == null) {
            minigun = new MinigunSentryTurret();
            minigun.setWorld(getWorld());
            if (!getWorld().isRemote) {
                minigun.setPlayer(getFakePlayer());
            }
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
        tag.putString("entityFilter", entityFilter);
        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        inventory.deserializeNBT(tag.getCompound("Items"));
        redstoneMode = tag.getByte("redstoneMode");
        setText(0, tag.getString("entityFilter"));
    }

    @Override
    public boolean redstoneAllows() {
        return redstoneMode == 3 || super.redstoneAllows();
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return inventory;
    }

    @Override
    public int getRedstoneMode() {
        return redstoneMode;
    }

    @Override
    public void handleGUIButtonPress(String tag, PlayerEntity player) {
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
    public LazyOptional<IItemHandlerModifiable> getInventoryCap() {
        return invCap;
    }

    @Override
    protected void onUpgradesChanged() {
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
                if (player.isCreative() || isExcludedBySecurityStations(player)) return false;
            }
            return super.apply(entity) && inRange(entity) && canSeeEntity(entity);
        }

        private boolean inRange(Entity entity) {
            return PneumaticCraftUtils.distBetweenSq(new BlockPos(getPos().getX(), getPos().getY(), getPos().getZ()), entity.posX, entity.posY, entity.posZ) <= rangeSq;
        }

        private boolean isExcludedBySecurityStations(PlayerEntity player) {
            Iterator<TileEntitySecurityStation> iterator = PneumaticCraftUtils.getSecurityStations(getWorld(), getPos(), false).iterator();
            if (iterator.hasNext()) { //When there are Security Stations, all stations need to be allowing the player.
                while (iterator.hasNext()) {
                    if (!iterator.next().doesAllowPlayer(player)) return false;
                }
                return true;
            } else {
                return false; //When there are no Security Stations at all, the player isn't automatically 'allowed to live'.
            }
        }
    }

    @Override
    public void setText(int textFieldID, String text) {
        entityFilter = text;
        entitySelector.setFilter(text);
        if (minigun != null) minigun.setAttackTarget(null);
        markDirty();
    }

    @Override
    public String getText(int textFieldID) {
        return entityFilter;
    }
}
