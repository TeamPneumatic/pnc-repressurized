package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IProgrammable;
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerBase;
import me.desht.pneumaticcraft.common.advancements.AdvancementTriggers;
import me.desht.pneumaticcraft.common.capabilities.BasicAirHandler;
import me.desht.pneumaticcraft.common.capabilities.CapabilityAirHandler;
import me.desht.pneumaticcraft.common.core.ModContainerTypes;
import me.desht.pneumaticcraft.common.core.ModEntityTypes;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemDrone extends ItemPressurizable implements IChargeableContainerProvider, IProgrammable, IUpgradeAcceptor {

    ItemDrone(String registryName) {
        super(registryName, (int)(PneumaticValues.DRONE_MAX_PRESSURE * PneumaticValues.DRONE_VOLUME), PneumaticValues.DRONE_VOLUME);
    }

    public ItemDrone() {
        this("drone");
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext ctx) {
        if (ctx.getHand() != Hand.MAIN_HAND) return ActionResultType.PASS;

        World world = ctx.getWorld();
        BlockPos pos = ctx.getPos();
        if (!world.isRemote) {
            ItemStack iStack = ctx.getPlayer().getHeldItemMainhand();
            if (iStack.getItem().getRegistryName().getPath().equals("logistic_drone")) {
                AdvancementTriggers.LOGISTICS_DRONE_DEPLOYED.trigger((ServerPlayerEntity) ctx.getPlayer());
            }
            BlockPos placePos = pos.offset(ctx.getFace());
            spawnDrone(ctx.getPlayer(), world, pos, ctx.getFace(), placePos, iStack);
            iStack.shrink(1);
        }
        return ActionResultType.SUCCESS;
    }

    public void spawnDrone(PlayerEntity player, World world, BlockPos clickPos, Direction facing, BlockPos placePos, ItemStack iStack){
        EntityDrone drone = new EntityDrone(ModEntityTypes.DRONE, world, player);

        drone.setPosition(placePos.getX() + 0.5, placePos.getY() + 0.5, placePos.getZ() + 0.5);
        world.addEntity(drone);

        CompoundNBT stackTag = iStack.getTag();
        CompoundNBT entityTag = new CompoundNBT();
        drone.writeAdditional(entityTag);

        int air = iStack.getCapability(CapabilityAirHandler.AIR_HANDLER_ITEM_CAPABILITY).map(IAirHandlerBase::getAir).orElse(0);
        BasicAirHandler h = new BasicAirHandler(air);
        h.addAir(air);

        if (stackTag != null) {
            entityTag.put(IProgrammable.NBT_WIDGETS, stackTag.getList(IProgrammable.NBT_WIDGETS, Constants.NBT.TAG_COMPOUND).copy());
            entityTag.put("airHandler", h.serializeNBT());
            entityTag.putInt("color", stackTag.getInt("color"));
            entityTag.put(UpgradableItemUtils.NBT_UPGRADE_TAG, stackTag.getCompound(UpgradableItemUtils.NBT_UPGRADE_TAG));
        }
        drone.readAdditional(entityTag);
        if (iStack.hasDisplayName()) drone.setCustomName(iStack.getDisplayName());

        drone.naturallySpawned = false;
        drone.onInitialSpawn(world, world.getDifficultyForLocation(placePos), SpawnReason.TRIGGERED, new ILivingEntityData() {}, null);
    }

    public static void setProgWidgets(List<IProgWidget> widgets, ItemStack iStack) {
        NBTUtil.initNBTTagCompound(iStack);
        TileEntityProgrammer.setWidgetsToNBT(widgets, iStack.getTag());
    }

    @Override
    public boolean canProgram(ItemStack stack) {
        return true;
    }

    @Override
    public boolean usesPieces(ItemStack stack) {
        return true;
    }

    @Override
    public boolean showProgramTooltip() {
        return true;
    }

    @Override
    public Set<Item> getApplicableUpgrades() {
        Set<Item> set = new HashSet<>();
        for (EnumUpgrade upgrade : EnumUpgrade.values()) {
            if (upgradeApplies(upgrade)) {
                set.add(ModItems.Registration.UPGRADES.get(upgrade));
            }
        }
        return Collections.unmodifiableSet(set);
    }

    @Override
    public String getUpgradeAcceptorTranslationKey() {
        return getTranslationKey();
    }

    public boolean upgradeApplies(EnumUpgrade upgrade) {
        switch (upgrade) {
            case VOLUME:
            case INVENTORY:
            case ITEM_LIFE:
            case SECURITY:
            case SPEED:
            case ENTITY_TRACKER:
            case MAGNET:
            case RANGE:
                return true;
        }
        return false;
    }

    @Override
    public INamedContainerProvider getContainerProvider(TileEntityChargingStation te) {
        return new IChargeableContainerProvider.Provider(te, ModContainerTypes.CHARGING_DRONE);
    }
}
