package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IProgrammable;
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.common.advancements.AdvancementTriggers;
import me.desht.pneumaticcraft.common.capabilities.BasicAirHandler;
import me.desht.pneumaticcraft.common.core.ModContainerTypes;
import me.desht.pneumaticcraft.common.core.ModEntityTypes;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.common.util.upgrade.ApplicableUpgradesDB;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.Map;

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

        int air = iStack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).map(IAirHandler::getAir).orElse(0);
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
    public Map<EnumUpgrade,Integer> getApplicableUpgrades() {
        return ApplicableUpgradesDB.getInstance().getApplicableUpgrades(this);
    }

    @Override
    public String getUpgradeAcceptorTranslationKey() {
        return getTranslationKey();
    }

    @Override
    public INamedContainerProvider getContainerProvider(TileEntityChargingStation te) {
        return new IChargeableContainerProvider.Provider(te, ModContainerTypes.CHARGING_DRONE);
    }
}
