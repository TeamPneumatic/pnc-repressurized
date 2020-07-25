package me.desht.pneumaticcraft.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.common.util.GlobalPosHelper;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;
import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.arguments.BlockPosArgument.getLoadedBlockPos;
import static net.minecraft.command.arguments.EntityArgument.getPlayer;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("dumpNBT")
                .executes(ModCommands::dumpNBT)
        );

        dispatcher.register(Commands.literal("amadrone_deliver")
                .then(argument("toPos", BlockPosArgument.blockPos())
                        .then(argument("fromPos", BlockPosArgument.blockPos())
                                .executes(ctx -> amadroneDeliver(ctx.getSource(), getLoadedBlockPos(ctx, "toPos"), getLoadedBlockPos(ctx, "fromPos")))
                        )
                )
                .then(argument("player", EntityArgument.player())
                        .then(argument("fromPos", BlockPosArgument.blockPos())
                                .executes(ctx -> amadroneDeliver(ctx.getSource(), getPlayer(ctx, "player").getPosition(), getLoadedBlockPos(ctx, "fromPos")))
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
                                .executes(c -> setGlobalVar(c, StringArgumentType.getString(c,"varname"), BlockPosArgument.getLoadedBlockPos(c, "pos")))
                        )
                )
        );
    }

    private static int dumpNBT(CommandContext<CommandSource> ctx) {
        CommandSource source = ctx.getSource();
        if (source.getEntity() instanceof PlayerEntity) {
            ItemStack held = ((PlayerEntity) source.getEntity()).getHeldItemMainhand();
            if (held.getTag() == null) {
                source.sendErrorMessage(new StringTextComponent("No NBT"));
                return 0;
            } else if (held.getTag().isEmpty()) {
                source.sendErrorMessage(new StringTextComponent("Empty NBT"));
                return 0;
            }
            source.sendFeedback(new StringTextComponent(held.getTag().toString()), false);
            return 1;
        }
        return 0;
    }

    private static int amadroneDeliver(CommandSource source, BlockPos toPos, BlockPos fromPos) {
        TileEntity te = source.getWorld().getTileEntity(fromPos);

        int status = IOHelper.getInventoryForTE(te).map(inv -> {
            List<ItemStack> deliveredStacks = new ArrayList<>();
            for (int i = 0; i < inv.getSlots() && deliveredStacks.size() < 65; i++) {
                if (!inv.getStackInSlot(i).isEmpty()) deliveredStacks.add(inv.getStackInSlot(i));
            }
            if (deliveredStacks.size() > 0) {
                GlobalPos gPos = GlobalPosHelper.makeGlobalPos(source.getWorld(), toPos);
                PneumaticRegistry.getInstance().getDroneRegistry().deliverItemsAmazonStyle(gPos, deliveredStacks.toArray(new ItemStack[0]));
                source.sendFeedback(xlate("pneumaticcraft.command.deliverAmazon.success", PneumaticCraftUtils.posToString(fromPos), PneumaticCraftUtils.posToString(toPos)), false);
                return 1;
            } else {
                source.sendErrorMessage(xlate("pneumaticcraft.command.deliverAmazon.noItems", PneumaticCraftUtils.posToString(fromPos)));
                return 0;
            }
        }).orElse(-1);

        if (status == -1) source.sendErrorMessage(xlate("pneumaticcraft.command.deliverAmazon.noInventory", PneumaticCraftUtils.posToString(fromPos)));
        return status;
    }

    private static int getGlobalVar(CommandContext<CommandSource> ctx, String varName) {
        CommandSource source = ctx.getSource();
        if (varName.startsWith("#")) varName = varName.substring(1);
        BlockPos pos = GlobalVariableManager.getInstance().getPos(varName);
        ItemStack stack = GlobalVariableManager.getInstance().getItem(varName);
        source.sendFeedback(xlate("pneumaticcraft.command.getGlobalVariable.output", varName, pos.toString(), stack.getDisplayName().getString()), false);
        return 1;
    }

    private static int setGlobalVar(CommandContext<CommandSource> ctx, String varName, BlockPos pos) {
        CommandSource source = ctx.getSource();
        if (varName.startsWith("#")) varName = varName.substring(1);
        GlobalVariableManager.getInstance().set(varName, pos);
        source.sendFeedback(xlate("pneumaticcraft.command.setGlobalVariable.output", varName, pos.toString()), false);
        return 1;
    }
}
