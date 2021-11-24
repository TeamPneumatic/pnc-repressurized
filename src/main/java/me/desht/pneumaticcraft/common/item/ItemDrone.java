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

package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IProgrammable;
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.common.advancements.AdvancementTriggers;
import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;
import me.desht.pneumaticcraft.common.util.upgrade.ApplicableUpgradesDB;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class ItemDrone extends ItemPressurizable implements IChargeableContainerProvider, IProgrammable, IUpgradeAcceptor {
    private final BiFunction<World, PlayerEntity, EntityDrone> droneCreator;
    private final boolean programmable;

    public ItemDrone(BiFunction<World, PlayerEntity, EntityDrone> droneCreator, boolean programmable) {
        super((int)(PneumaticValues.DRONE_MAX_PRESSURE * PneumaticValues.DRONE_VOLUME), PneumaticValues.DRONE_VOLUME);
        this.droneCreator = droneCreator;
        this.programmable = programmable;
    }

    @Override
    public ActionResultType useOn(ItemUseContext ctx) {
        World world = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        if (world instanceof IServerWorld) {
            ItemStack iStack = ctx.getPlayer().getItemInHand(ctx.getHand());
            if (iStack.getItem() == ModItems.LOGISTICS_DRONE.get()) {
                AdvancementTriggers.LOGISTICS_DRONE_DEPLOYED.trigger((ServerPlayerEntity) ctx.getPlayer());
            }
            BlockState state = world.getBlockState(pos);
            BlockPos placePos = state.getCollisionShape(world, pos).isEmpty() ? pos : pos.relative(ctx.getClickedFace());
            spawnDrone(ctx.getPlayer(), world, pos, ctx.getClickedFace(), placePos, iStack);
            iStack.shrink(1);
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (!entity.getCommandSenderWorld().isClientSide && stack.hasTag() && stack.getTag().contains(IProgrammable.NBT_WIDGETS)) entity.setExtendedLifetime();
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        if (stack.hasTag() && stack.getTag().contains("Tank")) {
            FluidTank fluidTank = new FluidTank(16000);
            fluidTank.readFromNBT(stack.getTag().getCompound("Tank"));
            FluidStack fluidStack = fluidTank.getFluid();
            if (!fluidStack.isEmpty()) {
                tooltip.add(new TranslationTextComponent("pneumaticcraft.gui.tooltip.fluid")
                        .append(fluidStack.getAmount() + "mB ")
                        .append(fluidStack.getDisplayName()).withStyle(TextFormatting.GRAY)
                );
            }
        }
    }

    public void spawnDrone(PlayerEntity player, World world, BlockPos clickPos, Direction facing, BlockPos placePos, ItemStack iStack){
        EntityDrone drone = droneCreator.apply(world, player);

        drone.setPos(placePos.getX() + 0.5, placePos.getY() + 0.5, placePos.getZ() + 0.5);
        drone.readFromItemStack(iStack);
        world.addFreshEntity(drone);
        drone.setDeployPos(placePos);

        if (drone.addProgram(clickPos, facing, placePos, iStack, drone.progWidgets)) {
            TileEntityProgrammer.updatePuzzleConnections(drone.progWidgets);
        }

        if (world instanceof IServerWorld) {
            drone.finalizeSpawn((IServerWorld) world, world.getCurrentDifficultyAt(placePos), SpawnReason.TRIGGERED, new ILivingEntityData() {}, null);
        }
    }

    @Override
    public boolean canProgram(ItemStack stack) {
        return programmable;
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
        return getDescriptionId();
    }

    @Override
    public INamedContainerProvider getContainerProvider(TileEntityChargingStation te) {
        return new IChargeableContainerProvider.Provider(te, ModContainers.CHARGING_DRONE.get());
    }
}
