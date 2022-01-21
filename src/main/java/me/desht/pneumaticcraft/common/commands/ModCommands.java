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

package me.desht.pneumaticcraft.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.common.util.GlobalPosHelper;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.arguments.EntityArgument.getPlayer;
import static net.minecraft.commands.arguments.coordinates.BlockPosArgument.getLoadedBlockPos;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("dumpNBT")
                .requires(cs -> cs.hasPermission(2))
                .executes(ModCommands::dumpNBT)
        );

        dispatcher.register(Commands.literal("amadrone_deliver")
                .requires(cs -> cs.hasPermission(2))
                .then(argument("toPos", BlockPosArgument.blockPos())
                        .then(argument("fromPos", BlockPosArgument.blockPos())
                                .executes(ctx -> amadroneDeliver(ctx.getSource(), getLoadedBlockPos(ctx, "toPos"), getLoadedBlockPos(ctx, "fromPos")))
                        )
                )
                .then(argument("player", EntityArgument.player())
                        .then(argument("fromPos", BlockPosArgument.blockPos())
                                .executes(ctx -> amadroneDeliver(ctx.getSource(), getPlayer(ctx, "player").blockPosition(), getLoadedBlockPos(ctx, "fromPos")))
                        )
                )
        );

        dispatcher.register(Commands.literal("get_global_var")
                .then(argument("varname", StringArgumentType.string())
                        .executes(c -> getGlobalVar(c, StringArgumentType.getString(c,"varname")))
                )
        );

        dispatcher.register(Commands.literal("set_global_var")
                .then(argument("varname", StringArgumentType.string())
                        .then(argument("pos", BlockPosArgument.blockPos())
                                .executes(c -> setGlobalVar(c, StringArgumentType.getString(c,"varname"), getLoadedBlockPos(c, "pos")))
                        )
                )
        );
    }

    private static int dumpNBT(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        if (source.getEntity() instanceof Player) {
            ItemStack held = ((Player) source.getEntity()).getMainHandItem();
            if (held.getTag() == null) {
                source.sendFailure(new TextComponent("No NBT"));
                return 0;
            } else if (held.getTag().isEmpty()) {
                source.sendFailure(new TextComponent("Empty NBT"));
                return 0;
            }
            source.sendSuccess(new TextComponent(held.getTag().toString()), false);
            return 1;
        }
        return 0;
    }

    private static int amadroneDeliver(CommandSourceStack source, BlockPos toPos, BlockPos fromPos) {
        BlockEntity te = source.getLevel().getBlockEntity(fromPos);

        int status = IOHelper.getInventoryForTE(te).map(inv -> {
            List<ItemStack> deliveredStacks = new ArrayList<>();
            for (int i = 0; i < inv.getSlots() && deliveredStacks.size() < 36; i++) {
                if (!inv.getStackInSlot(i).isEmpty()) deliveredStacks.add(inv.getStackInSlot(i));
            }
            if (deliveredStacks.size() > 0) {
                GlobalPos gPos = GlobalPosHelper.makeGlobalPos(source.getLevel(), toPos);
                PneumaticRegistry.getInstance().getDroneRegistry().deliverItemsAmazonStyle(gPos, deliveredStacks.toArray(new ItemStack[0]));
                source.sendSuccess(xlate("pneumaticcraft.command.deliverAmazon.success", PneumaticCraftUtils.posToString(fromPos), PneumaticCraftUtils.posToString(toPos)), false);
                return 1;
            } else {
                source.sendFailure(xlate("pneumaticcraft.command.deliverAmazon.noItems", PneumaticCraftUtils.posToString(fromPos)));
                return 0;
            }
        }).orElse(-1);

        if (status == -1) source.sendFailure(xlate("pneumaticcraft.command.deliverAmazon.noInventory", PneumaticCraftUtils.posToString(fromPos)));
        return status;
    }

    private static int getGlobalVar(CommandContext<CommandSourceStack> ctx, String varName) {
        CommandSourceStack source = ctx.getSource();
        if (varName.startsWith("#")) varName = varName.substring(1);
        BlockPos pos = GlobalVariableManager.getInstance().getPos(varName);
        ItemStack stack = GlobalVariableManager.getInstance().getItem(varName);
        source.sendSuccess(xlate("pneumaticcraft.command.getGlobalVariable.output", varName, PneumaticCraftUtils.posToString(pos), stack.getHoverName().getString()), false);
        return 1;
    }

    private static int setGlobalVar(CommandContext<CommandSourceStack> ctx, String varName, BlockPos pos) {
        CommandSourceStack source = ctx.getSource();
        if (varName.startsWith("#")) varName = varName.substring(1);
        GlobalVariableManager.getInstance().set(varName, pos);
        source.sendSuccess(xlate("pneumaticcraft.command.setGlobalVariable.output", varName, PneumaticCraftUtils.posToString(pos)), false);
        return 1;
    }
}
