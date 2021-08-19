package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.tileentity.IManoMeasurable;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.heat.HeatExchangerLogicAmbient;
import me.desht.pneumaticcraft.common.heat.HeatExchangerManager;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.heat.TemperatureData;
import me.desht.pneumaticcraft.common.heat.behaviour.HeatBehaviourTransition;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.lib.GuiConstants;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ItemManometer extends ItemPressurizable {

    public ItemManometer() {
        super(ModItems.toolProps(), PneumaticValues.AIR_CANISTER_MAX_AIR, PneumaticValues.AIR_CANISTER_VOLUME);
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null) return ActionResultType.PASS;
        World world = context.getWorld();

        if (world.isRemote) return ActionResultType.SUCCESS;

        return stack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).map(h -> {
            if (h.getAir() < PneumaticValues.USAGE_ITEM_MANOMETER) {
                player.sendStatusMessage(xlate("pneumaticcraft.message.misc.outOfAir", stack.getDisplayName()).mergeStyle(TextFormatting.RED), true);
                return ActionResultType.FAIL;
            }

            List<ITextComponent> curInfo = new ArrayList<>();
            Direction side = context.getFace();
            BlockPos pos = context.getPos();

            BlockState state = world.getBlockState(pos.offset(side));
            if (!player.isSneaking() && state.getBlock() instanceof FlowingFluidBlock) {
                pos = pos.offset(side);
            }
            TileEntity te = world.getTileEntity(pos);
            if (te != null) {
                if (te instanceof INameable) {
                    curInfo.add(((INameable) te).getDisplayName().deepCopy().mergeStyle(TextFormatting.AQUA));
                } else {
                    ItemStack stack1 = new ItemStack(te.getBlockState().getBlock());
                    curInfo.add(stack1.getDisplayName().deepCopy().mergeStyle(TextFormatting.AQUA));
                }

                if (te.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY).isPresent()) {
                    te.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY)
                            .ifPresent(teAirHandler -> teAirHandler.printManometerMessage(player, curInfo));
                } else {
                    te.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, side)
                            .ifPresent(teAirHandler -> teAirHandler.printManometerMessage(player, curInfo));
                }

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
                if (state1.getBlock() instanceof FlowingFluidBlock) {
                    Fluid f = ((FlowingFluidBlock) state1.getBlock()).getFluid();
                    FluidStack fs = new FluidStack(f, 1000);
                    curInfo.add(fs.getDisplayName().deepCopy().mergeStyle(TextFormatting.AQUA));
                } else {
                    ItemStack stack1 = new ItemStack(world.getBlockState(pos).getBlock());
                    curInfo.add(stack1.getDisplayName().deepCopy().mergeStyle(TextFormatting.AQUA));
                }
                HeatExchangerManager.INSTANCE.getLogic(world, pos, side)
                        .ifPresent(logic -> curInfo.add(HeatUtil.formatHeatString(logic.getTemperatureAsInt())));
                checkForHeatExtraction(world, pos, curInfo);
            }

            if (curInfo.size() > 0) {
                for (int i = 1; i < curInfo.size(); i++) {
                    curInfo.set(i, GuiConstants.bullet().append(curInfo.get(i)));
                }
                h.addAir(-PneumaticValues.USAGE_ITEM_MANOMETER);
                curInfo.forEach(s -> player.sendStatusMessage(s, false));
            }
            return ActionResultType.SUCCESS;
        }).orElse(ActionResultType.PASS);
    }

    private void checkForHeatExtraction(World world, BlockPos pos, List<ITextComponent> curInfo) {
        // look for a heat handling TE adjacent to our pos which has a heat transition behaviour for our pos
        for (Direction d : DirectionUtil.VALUES) {
            TileEntity te1 = world.getTileEntity(pos.offset(d));
            if (te1 != null) {
                te1.getCapability(PNCCapabilities.HEAT_EXCHANGER_CAPABILITY, d.getOpposite())
                        .ifPresent(handler -> handler.getHeatBehaviour(pos, HeatBehaviourTransition.class)
                                .ifPresent(behaviour -> {
                                            double progress = behaviour.getExtractionProgress();
                                            if (progress != 0) {
                                                String key = "pneumaticcraft.waila.temperature" + (progress < 0 ? "Gain" : "Loss");
                                                int pct = progress < 0 ? (int) (progress * -100) : (int) (progress * 100);
                                                curInfo.add(new TranslationTextComponent(key, pct));
                                            }
                                        }
                                )
                        );
            }
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        if (worldIn.isRemote) return ActionResult.resultSuccess(stack);
        return stack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).map(h -> {
            if (h.getPressure() >= 0.1f) {
                double temp = HeatExchangerLogicAmbient.getAmbientTemperature(worldIn, playerIn.getPosition());
                playerIn.sendStatusMessage(ItemStack.EMPTY.getDisplayName().deepCopy().mergeStyle(TextFormatting.AQUA), false);
                playerIn.sendStatusMessage(GuiConstants.bullet().append(HeatUtil.formatHeatString((int) temp)), false);
                return ActionResult.resultConsume(stack);
            }
            return ActionResult.resultFail(stack);
        }).orElse(ActionResult.resultPass(stack));
    }

    @Override
    public ActionResultType itemInteractionForEntity(ItemStack iStack, PlayerEntity player, LivingEntity entity, Hand hand) {
        if (!player.world.isRemote) {
            return iStack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).map(h -> {
                if (h.getAir() < PneumaticValues.USAGE_ITEM_MANOMETER) {
                    player.sendStatusMessage(xlate("pneumaticcraft.message.misc.outOfAir", iStack.getDisplayName()).mergeStyle(TextFormatting.RED), true);
                    return ActionResultType.FAIL;
                }
                List<ITextComponent> curInfo = new ArrayList<>();
                if (entity instanceof IManoMeasurable) {
                    ((IManoMeasurable) entity).printManometerMessage(player, curInfo);
                } else {
                    curInfo.add(entity.getDisplayName().deepCopy().mergeStyle(TextFormatting.AQUA));
                }
                if (curInfo.size() > 0) {
                    h.addAir(-PneumaticValues.USAGE_ITEM_MANOMETER);
                    for (int i = 1; i < curInfo.size(); i++) {
                        curInfo.set(i, GuiConstants.bullet().append(curInfo.get(i)));
                    }
                    curInfo.forEach(s -> player.sendStatusMessage(s, false));
                }
                return ActionResultType.SUCCESS;
            }).orElse(ActionResultType.PASS);
        }
        return ActionResultType.SUCCESS;
    }
}
