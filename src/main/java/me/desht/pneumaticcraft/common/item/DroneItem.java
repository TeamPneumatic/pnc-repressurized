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

import me.desht.pneumaticcraft.api.item.IProgrammable;
import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.common.block.entity.drone.ProgrammerBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.utility.ChargingStationBlockEntity;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import me.desht.pneumaticcraft.common.registry.ModCriterionTriggers;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.registry.ModMenuTypes;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

import static me.desht.pneumaticcraft.common.entity.drone.DroneEntity.NBT_DRONE_COLOR;

public class DroneItem extends PressurizableItem
        implements IChargeableContainerProvider, IProgrammable, ColorHandlers.ITintableItem {
    private final BiFunction<Level, Player, DroneEntity> droneCreator;
    private final boolean programmable;
    private final DyeColor defaultColor;

    public DroneItem(BiFunction<Level, Player, DroneEntity> droneCreator, boolean programmable, DyeColor defaultColor) {
        super(ModItems.defaultProps(), (int)(PneumaticValues.DRONE_MAX_PRESSURE * PneumaticValues.DRONE_VOLUME), PneumaticValues.DRONE_VOLUME);
        this.droneCreator = droneCreator;
        this.programmable = programmable;
        this.defaultColor = defaultColor;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level world = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        if (ctx.getPlayer() instanceof ServerPlayer sp) {
            ItemStack iStack = sp.getItemInHand(ctx.getHand());
            if (iStack.getItem() == ModItems.LOGISTICS_DRONE.get()) {
                ModCriterionTriggers.LOGISTICS_DRONE_DEPLOYED.get().trigger(sp);
            }
            BlockState state = world.getBlockState(pos);
            BlockPos placePos = state.getCollisionShape(world, pos).isEmpty() ? pos : pos.relative(ctx.getClickedFace());
            spawnDrone(ctx.getPlayer(), world, pos, ctx.getClickedFace(), placePos, iStack);
            iStack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (!entity.getCommandSenderWorld().isClientSide && stack.hasTag() && stack.getTag().contains(IProgrammable.NBT_WIDGETS)) entity.setExtendedLifetime();
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        if (stack.hasTag() && stack.getTag().contains("Tank")) {
            FluidTank fluidTank = new FluidTank(16000);
            fluidTank.readFromNBT(stack.getTag().getCompound("Tank"));
            FluidStack fluidStack = fluidTank.getFluid();
            if (!fluidStack.isEmpty()) {
                tooltip.add(Component.translatable("pneumaticcraft.gui.tooltip.fluid")
                        .append(fluidStack.getAmount() + "mB ")
                        .append(fluidStack.getDisplayName()).withStyle(ChatFormatting.GRAY)
                );
            }
        }
    }

    public DyeColor getDroneColor(ItemStack stack) {
        return stack.hasTag() && Objects.requireNonNull(stack.getTag()).contains(NBT_DRONE_COLOR) ?
                DyeColor.byId(stack.getTag().getInt(NBT_DRONE_COLOR)) :
                defaultColor;
    }

    public void spawnDrone(Player player, Level world, BlockPos clickPos, Direction facing, BlockPos placePos, ItemStack iStack){
        DroneEntity drone = droneCreator.apply(world, player);

        drone.setPos(placePos.getX() + 0.5, placePos.getY() + 0.5, placePos.getZ() + 0.5);
        drone.readFromItemStack(iStack);
        world.addFreshEntity(drone);
        drone.setDeployPos(placePos);

        if (drone.addProgram(clickPos, facing, placePos, iStack, drone.progWidgets)) {
            ProgrammerBlockEntity.updatePuzzleConnections(drone.progWidgets);
        }

        if (world instanceof ServerLevelAccessor) {
            drone.finalizeSpawn((ServerLevelAccessor) world, world.getCurrentDifficultyAt(placePos), MobSpawnType.TRIGGERED, new SpawnGroupData() {}, null);
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
    public MenuProvider getContainerProvider(ChargingStationBlockEntity te) {
        return new IChargeableContainerProvider.Provider(te, ModMenuTypes.CHARGING_DRONE.get());
    }

    @Override
    public int getTintColor(ItemStack stack, int tintIndex) {
        return tintIndex == 1 ? PneumaticCraftUtils.getDyeColorAsRGB(getDroneColor(stack)) : -1;
    }
}
