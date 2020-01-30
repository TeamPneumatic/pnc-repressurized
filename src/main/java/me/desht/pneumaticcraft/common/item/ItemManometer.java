package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.tileentity.IManoMeasurable;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
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
        super(PneumaticValues.AIR_CANISTER_MAX_AIR, PneumaticValues.AIR_CANISTER_VOLUME);
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        Direction side = context.getFace();

        if (world.isRemote) return ActionResultType.PASS;

        return stack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).map(h -> {
            if (h.getPressure() > 0.1F) {
                TileEntity te = world.getTileEntity(context.getPos());

                List<ITextComponent> curInfo = new ArrayList<>();
                te.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, side)
                        .ifPresent(teAirHandler -> teAirHandler.printManometerMessage(player, curInfo));

                if (te instanceof IManoMeasurable) {
                    ((IManoMeasurable) te).printManometerMessage(player, curInfo);
                }

                te.getCapability(PNCCapabilities.HEAT_EXCHANGER_CAPABILITY)
                        .ifPresent(exchanger -> curInfo.add(HeatUtil.formatHeatString(exchanger.getTemperatureAsInt())));
                for (Direction d : Direction.VALUES) {
                    te.getCapability(PNCCapabilities.HEAT_EXCHANGER_CAPABILITY, d)
                            .ifPresent(exchanger -> curInfo.add(HeatUtil.formatHeatString(exchanger.getTemperatureAsInt())));
                }

                if (curInfo.size() > 0) {
                    h.addAir(-30);
                    curInfo.forEach(s -> player.sendStatusMessage(s, false));
                }
                return ActionResultType.SUCCESS;
            } else {
                player.sendStatusMessage(xlate("message.misc.outOfAir", stack.getDisplayName().getFormattedText()).applyTextStyles(TextFormatting.RED), true);
                return ActionResultType.FAIL;
            }
        }).orElse(ActionResultType.PASS);
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack iStack, PlayerEntity player, LivingEntity entity, Hand hand) {
        if (!player.world.isRemote) {
            if (entity instanceof IManoMeasurable) {
                return iStack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).map(h -> {
                    if (h.getPressure() > 0.1F) {
                        List<ITextComponent> curInfo = new ArrayList<>();
                        ((IManoMeasurable) entity).printManometerMessage(player, curInfo);
                        if (curInfo.size() > 0) {
                            h.addAir(-30);
                            curInfo.forEach(s -> player.sendStatusMessage(s, false));
                        }
                    } else {
                        player.sendStatusMessage(xlate("message.misc.outOfAir", iStack.getDisplayName().getFormattedText()).applyTextStyles(TextFormatting.RED), true);
                    }
                    return true;
                }).orElse(false);
            }
        }
        return false;
    }
}
