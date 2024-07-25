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
import me.desht.pneumaticcraft.common.block.entity.utility.ChargingStationBlockEntity;
import me.desht.pneumaticcraft.common.drone.ProgWidgetUtils;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import me.desht.pneumaticcraft.common.registry.ModCriterionTriggers;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.registry.ModMenuTypes;
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
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;

import java.util.List;
import java.util.function.BiFunction;

public class DroneItem extends PressurizableItem
        implements IChargeableContainerProvider, IProgrammable, ColorHandlers.ITintableItem {
    private final BiFunction<Level, Player, DroneEntity> droneCreator;
    private final boolean programmable;
    private final DyeColor defaultColor;

    public DroneItem(BiFunction<Level, Player, DroneEntity> droneCreator, boolean programmable, DyeColor defaultColor) {
        super(ModItems.defaultProps()
                        .component(ModDataComponents.DRONE_COLOR, defaultColor.getId())
                        .component(ModDataComponents.STORED_FLUID, SimpleFluidContent.EMPTY),
                (int)(PneumaticValues.DRONE_MAX_PRESSURE * PneumaticValues.DRONE_VOLUME), PneumaticValues.DRONE_VOLUME);
        this.droneCreator = droneCreator;
        this.programmable = programmable;
        this.defaultColor = defaultColor;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (ctx.getPlayer() instanceof ServerPlayer sp) {
            Level world = ctx.getLevel();
            BlockPos pos = ctx.getClickedPos();
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
        if (!entity.getCommandSenderWorld().isClientSide && stack.has(ModDataComponents.SAVED_DRONE_PROGRAM)) {
            entity.setExtendedLifetime();
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip, flagIn);

        SimpleFluidContent storedFluid = stack.get(ModDataComponents.STORED_FLUID);
        if (storedFluid != null && !storedFluid.isEmpty()) {
            FluidStack fluidStack = storedFluid.copy();
            if (!fluidStack.isEmpty()) {
                tooltip.add(Component.translatable("pneumaticcraft.gui.tooltip.fluid")
                        .append(fluidStack.getAmount() + "mB ")
                        .append(fluidStack.getHoverName()).withStyle(ChatFormatting.GRAY)
                );
            }
        }
    }

    public DyeColor getDroneColor(ItemStack stack) {
        int color = stack.getOrDefault(ModDataComponents.DRONE_COLOR, defaultColor.getId());
        return DyeColor.byId(color);
    }

    public void spawnDrone(Player player, Level level, BlockPos clickPos, Direction facing, BlockPos placePos, ItemStack iStack) {
        DroneEntity drone = droneCreator.apply(level, player);

        drone.setPos(placePos.getX() + 0.5, placePos.getY() + 0.5, placePos.getZ() + 0.5);
        drone.readFromItemStack(iStack);
        level.addFreshEntity(drone);
        drone.setDeployPos(placePos);

        if (drone.addProgram(clickPos, facing, placePos, iStack, drone.progWidgets)) {
            ProgWidgetUtils.updatePuzzleConnections(drone.progWidgets);
        }

        if (level instanceof ServerLevelAccessor) {
            EventHooks.finalizeMobSpawn(drone, (ServerLevelAccessor) level, level.getCurrentDifficultyAt(placePos),
                    MobSpawnType.TRIGGERED, new SpawnGroupData() {});
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
        return tintIndex == 1 ? getDroneColor(stack).getTextureDiffuseColor() : 0xFFFFFFFF;
    }

    public static boolean isBasicDrone(ItemStack stack) {
        return stack.getItem() instanceof DroneItem d && !d.canProgram(stack);
    }
}
