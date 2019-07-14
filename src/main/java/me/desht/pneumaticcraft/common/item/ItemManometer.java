package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.api.tileentity.IManoMeasurable;
import me.desht.pneumaticcraft.api.tileentity.IPneumaticMachine;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ItemManometer extends ItemPressurizable {

    public ItemManometer() {
        super("manometer", PneumaticValues.AIR_CANISTER_MAX_AIR, PneumaticValues.AIR_CANISTER_VOLUME);
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        Direction side = context.getFace();

        if (world.isRemote) return ActionResultType.PASS;

        if (((IPressurizable) stack.getItem()).getPressure(stack) > 0F) {
            TileEntity te = world.getTileEntity(context.getPos());
            IPneumaticMachine machine = IPneumaticMachine.getMachine(te);
            List<ITextComponent> curInfo = new ArrayList<>();
            if (machine != null && machine.getAirHandler(side) != null) {
                machine.getAirHandler(side).printManometerMessage(player, curInfo);
            }
            if (te instanceof IManoMeasurable) {
                ((IManoMeasurable) te).printManometerMessage(player, curInfo);
            }
            if (te instanceof IHeatExchanger) {
                IHeatExchangerLogic exchanger = ((IHeatExchanger) te).getHeatExchangerLogic(side);
                if (exchanger != null) {
                    curInfo.add(xlate("waila.temperature", (int) exchanger.getTemperature() - 273));
                } else {
                    for (Direction d : Direction.VALUES) {
                        exchanger = ((IHeatExchanger) te).getHeatExchangerLogic(d);
                        if (exchanger != null) {
                            curInfo.add(xlate("waila.temperature." + d.toString().toLowerCase(), (int) exchanger.getTemperature() - 273));
                        }
                    }
                }
            }
            if (curInfo.size() > 0) {
                ((IPressurizable) stack.getItem()).addAir(stack, -30);
                for (ITextComponent s : curInfo) {
                    player.sendStatusMessage(s, false);
                }
                return ActionResultType.SUCCESS;
            }
        } else {
            player.sendStatusMessage(xlate("message.misc.outOfAir", stack.getDisplayName().getFormattedText()).applyTextStyles(TextFormatting.RED), true);
            return ActionResultType.FAIL;
        }
        return ActionResultType.PASS;
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack iStack, PlayerEntity player, LivingEntity entity, Hand hand) {
        if (!player.world.isRemote) {
            if (entity instanceof IManoMeasurable) {
                if (((IPressurizable) iStack.getItem()).getPressure(iStack) > 0F) {
                    List<ITextComponent> curInfo = new ArrayList<>();
                    ((IManoMeasurable) entity).printManometerMessage(player, curInfo);
                    if (curInfo.size() > 0) {
                        ((IPressurizable) iStack.getItem()).addAir(iStack, -30);
                        curInfo.forEach(s -> player.sendStatusMessage(s, false));
                        return true;
                    }
                } else {
                    player.sendStatusMessage(xlate("message.misc.outOfAir", iStack.getDisplayName().getFormattedText()).applyTextStyles(TextFormatting.RED), true);
                }
            }
        }
        return false;
    }
}
