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

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.api.tileentity.IManoMeasurable;
import me.desht.pneumaticcraft.common.heat.HeatExchangerLogicAmbient;
import me.desht.pneumaticcraft.common.heat.HeatExchangerManager;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.heat.TemperatureData;
import me.desht.pneumaticcraft.common.heat.behaviour.HeatBehaviourTransition;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ManometerItem extends PressurizableItem {

    public ManometerItem() {
        super(ModItems.toolProps(), PneumaticValues.AIR_CANISTER_MAX_AIR, PneumaticValues.AIR_CANISTER_VOLUME);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        Level world = context.getLevel();

        if (world.isClientSide) return InteractionResult.SUCCESS;

        return PNCCapabilities.getAirHandler(stack).map(h -> {
            if (h.getAir() < PneumaticValues.USAGE_ITEM_MANOMETER) {
                player.displayClientMessage(xlate("pneumaticcraft.message.misc.outOfAir", stack.getHoverName()).withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }

            List<Component> curInfo = new ArrayList<>();
            Direction side = context.getClickedFace();
            BlockPos pos = context.getClickedPos();

            BlockState state = world.getBlockState(pos.relative(side));
            if (!player.isShiftKeyDown() && state.getBlock() instanceof LiquidBlock) {
                pos = pos.relative(side);
            }
            BlockEntity te = world.getBlockEntity(pos);
            if (te != null) {
                if (te instanceof Nameable) {
                    curInfo.add(((Nameable) te).getDisplayName().copy().withStyle(ChatFormatting.AQUA));
                } else {
                    curInfo.add(xlate(te.getBlockState().getBlock().getDescriptionId()).withStyle(ChatFormatting.AQUA));
                }

                PNCCapabilities.getAirHandler(te).ifPresentOrElse(
                        handler -> handler.printManometerMessage(player, curInfo),
                        () -> PNCCapabilities.getAirHandler(te, side)
                                .ifPresent(handler -> handler.printManometerMessage(player, curInfo)));

                if (te instanceof IManoMeasurable) {
                    ((IManoMeasurable) te).printManometerMessage(player, curInfo);
                }

                TemperatureData tempData = new TemperatureData(te);
                if (tempData.isMultisided()) {
                    for (Direction face : DirectionUtil.VALUES) {
                        if (tempData.hasData(face)) {
                            curInfo.add(HeatUtil.formatHeatString(face, (int) tempData.getTemperature(face)));
                        }
                    }
                } else if (tempData.hasData(null)) {
                    curInfo.add(HeatUtil.formatHeatString((int) tempData.getTemperature(null)));
                } else {
                    HeatExchangerManager.getInstance().getLogic(world, pos, side)
                            .ifPresent(logic -> curInfo.add(HeatUtil.formatHeatString((int) logic.getTemperature())));
                }
            } else {
                BlockState state1 = world.getBlockState(pos);
                if (state1.getBlock() instanceof LiquidBlock) {
                    Fluid f = ((LiquidBlock) state1.getBlock()).getFluid();
                    FluidStack fs = new FluidStack(f, 1000);
                    curInfo.add(fs.getDisplayName().copy().withStyle(ChatFormatting.AQUA));
                } else {
                    curInfo.add(xlate(world.getBlockState(pos).getBlock().getDescriptionId()).withStyle(ChatFormatting.AQUA));
                }
                HeatExchangerManager.INSTANCE.getLogic(world, pos, side)
                        .ifPresent(logic -> curInfo.add(HeatUtil.formatHeatString(logic.getTemperatureAsInt())));
            }
            checkForHeatExtraction(world, pos, curInfo);

            if (curInfo.size() > 0) {
                for (int i = 1; i < curInfo.size(); i++) {
                    curInfo.set(i, Symbols.bullet().append(curInfo.get(i)));
                }
                curInfo.forEach(s -> player.displayClientMessage(s, false));
                if (!player.isCreative()) {
                    h.addAir(-PneumaticValues.USAGE_ITEM_MANOMETER);
                }
            }
            return InteractionResult.SUCCESS;
        }).orElse(InteractionResult.PASS);
    }

    private void checkForHeatExtraction(Level world, BlockPos pos, List<Component> curInfo) {
        // look for a heat handling BE adjacent to our pos which has a heat transition behaviour for our pos
        for (Direction d : DirectionUtil.VALUES) {
            BlockEntity te1 = world.getBlockEntity(pos.relative(d));
            if (te1 != null) {
                PNCCapabilities.getHeatLogic(te1, d.getOpposite())
                        .flatMap(handler -> handler.getHeatBehaviour(pos, HeatBehaviourTransition.class))
                        .ifPresent(behaviour -> {
                            double progress = behaviour.getExtractionProgress();
                            if (progress != 0) {
                                String key = "pneumaticcraft.waila.temperature" + (progress < 0 ? "Gain" : "Loss");
                                int pct = progress < 0 ? (int) (progress * -100) : (int) (progress * 100);
                                curInfo.add(Component.translatable(key, pct));
                            }
                        });
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        if (worldIn.isClientSide) return InteractionResultHolder.success(stack);
        return PNCCapabilities.getAirHandler(stack).map(h -> {
            if (h.getPressure() >= 0.1f) {
                double temp = HeatExchangerLogicAmbient.getAmbientTemperature(worldIn, playerIn.blockPosition());
                playerIn.displayClientMessage(ItemStack.EMPTY.getHoverName().copy().withStyle(ChatFormatting.AQUA), false);
                playerIn.displayClientMessage(Symbols.bullet().append(HeatUtil.formatHeatString((int) temp)), false);
                return InteractionResultHolder.consume(stack);
            }
            return InteractionResultHolder.fail(stack);
        }).orElse(InteractionResultHolder.pass(stack));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack iStack, Player player, LivingEntity entity, InteractionHand hand) {
        if (!player.level().isClientSide) {
            return PNCCapabilities.getAirHandler(iStack).map(h -> {
                if (h.getAir() < PneumaticValues.USAGE_ITEM_MANOMETER) {
                    player.displayClientMessage(xlate("pneumaticcraft.message.misc.outOfAir", iStack.getHoverName()).withStyle(ChatFormatting.RED), true);
                    return InteractionResult.FAIL;
                }
                List<Component> curInfo = new ArrayList<>();
                if (entity instanceof IManoMeasurable) {
                    ((IManoMeasurable) entity).printManometerMessage(player, curInfo);
                } else {
                    curInfo.add(entity.getDisplayName().copy().withStyle(ChatFormatting.AQUA));
                }
                if (!curInfo.isEmpty()) {
                    h.addAir(-PneumaticValues.USAGE_ITEM_MANOMETER);
                    for (int i = 1; i < curInfo.size(); i++) {
                        curInfo.set(i, Symbols.bullet().append(curInfo.get(i)));
                    }
                    curInfo.forEach(s -> player.displayClientMessage(s, false));
                }
                return InteractionResult.SUCCESS;
            }).orElse(InteractionResult.PASS);
        }
        return InteractionResult.SUCCESS;
    }
}
