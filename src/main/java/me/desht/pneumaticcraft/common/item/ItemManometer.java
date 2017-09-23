package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.api.tileentity.IManoMeasurable;
import me.desht.pneumaticcraft.api.tileentity.IPneumaticMachine;
import me.desht.pneumaticcraft.common.thirdparty.ModInteractionUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class ItemManometer extends ItemPressurizable {

    public ItemManometer() {
        super("manometer", PneumaticValues.AIR_CANISTER_MAX_AIR, PneumaticValues.AIR_CANISTER_VOLUME);
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if (world.isRemote) return EnumActionResult.PASS;

        ItemStack iStack = player.getHeldItem(hand);
        if (((IPressurizable) iStack.getItem()).getPressure(iStack) > 0F) {
            TileEntity te = world.getTileEntity(pos);
            IPneumaticMachine machine = ModInteractionUtils.getInstance().getMachine(te);
            List<ITextComponent> curInfo = new ArrayList<>();
            List<String> info = new ArrayList<>();
            if (machine != null) {
                machine.getAirHandler(side).printManometerMessage(player, info);
            }
            if (te instanceof IManoMeasurable) {
                ((IManoMeasurable) te).printManometerMessage(player, info);
            }
            for (String s : info)
                curInfo.add(new TextComponentTranslation(s));
            if (te instanceof IHeatExchanger) {
                IHeatExchangerLogic exchanger = ((IHeatExchanger) te).getHeatExchangerLogic(side);
                if (exchanger != null) {
                    curInfo.add(new TextComponentTranslation("waila.temperature", (int) exchanger.getTemperature() - 273));
                } else {
                    for (EnumFacing d : EnumFacing.VALUES) {
                        exchanger = ((IHeatExchanger) te).getHeatExchangerLogic(d);
                        if (exchanger != null) {
                            curInfo.add(new TextComponentTranslation("waila.temperature." + d.toString().toLowerCase(), (int) exchanger.getTemperature() - 273));
                        }
                    }
                }
            }
            if (curInfo.size() > 0) {
                ((IPressurizable) iStack.getItem()).addAir(iStack, -30);
                for (ITextComponent s : curInfo) {
                    player.sendStatusMessage(s, false);
                }
                return EnumActionResult.SUCCESS;
            }
        } else {
            player.sendStatusMessage(new TextComponentString(TextFormatting.RED + "The Manometer doesn't have any charge!"), false);
            return EnumActionResult.FAIL;
        }
        return EnumActionResult.PASS;
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack iStack, EntityPlayer player, EntityLivingBase entity, EnumHand hand) {
        if (!player.world.isRemote) {
            if (entity instanceof IManoMeasurable) {
                if (((IPressurizable) iStack.getItem()).getPressure(iStack) > 0F) {
                    List<String> curInfo = new ArrayList<String>();
                    ((IManoMeasurable) entity).printManometerMessage(player, curInfo);
                    if (curInfo.size() > 0) {
                        ((IPressurizable) iStack.getItem()).addAir(iStack, -30);
                        for (String s : curInfo) {
                            player.sendStatusMessage(new TextComponentTranslation(s), false);
                        }
                        return true;
                    }
                } else {
                    player.sendStatusMessage(new TextComponentTranslation(TextFormatting.RED + "The Manometer doesn't have any charge!"), false);
                }
            }
        }
        return false;
    }
}
