package me.desht.pneumaticcraft.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.common.util.GlobalPosHelper;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.variables.GlobalVariableHelper;
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
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;
import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;
import static net.minecraft.command.arguments.BlockPosArgument.getLoadedBlockPos;
import static net.minecraft.command.arguments.EntityArgument.getPlayer;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("pncr")
                .then(literal("dump_nbt")
                        .requires(cs -> cs.hasPermissionLevel(2))
                        .executes(ModCommands::dumpNBT)
                )
                .then(literal("get_global_var")
                        .then(argument("varname", new VarnameType())
                                .executes(c -> getGlobalVar(c, StringArgumentType.getString(c,"varname")))
                        )
                )
                .then(literal("set_global_var")
                        .then(argument("varname", new VarnameType())
                                .then(argument("pos", BlockPosArgument.blockPos())
                                        .executes(c -> setGlobalVar(c, StringArgumentType.getString(c,"varname"), BlockPosArgument.getLoadedBlockPos(c, "pos")))
                                )
                        )
                )
                .then(literal("import_global_vars")  // TODO remove in 1.17
                        .executes(ModCommands::importGlobals)
                )
                .then(literal("amadrone_deliver")
                        .requires(cs -> cs.hasPermissionLevel(2))
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
                )
        );
    }

    private static int importGlobals(CommandContext<CommandSource> ctx) {
        // TODO remove in 1.17
        CommandSource source = ctx.getSource();
        if (source.getEntity() instanceof PlayerEntity) {
            if (GlobalVariableManager.getInstance().importGlobals(source.getEntity().getUniqueID())) {
                source.sendFeedback(xlate("pneumaticcraft.command.importGlobals.success"), false);
            } else {
                source.sendErrorMessage(xlate("pneumaticcraft.command.importGlobals.failure"));
            }
        }
        return 1;
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
            for (int i = 0; i < inv.getSlots() && deliveredStacks.size() < 36; i++) {
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
        if (!varName.startsWith("#") && !varName.startsWith("%")) {
            source.sendFeedback(xlate("pneumaticcraft.command.getGlobalVariable.prefixReminder").mergeStyle(TextFormatting.GOLD), false);
            varName = "#" + varName;
        }
        try {
            UUID id = ctx.getSource().asPlayer().getUniqueID();
            BlockPos pos = GlobalVariableHelper.getPos(id, varName);
            ItemStack stack = GlobalVariableHelper.getStack(id, varName);
            if (pos != null) {
                source.sendFeedback(xlate("pneumaticcraft.command.getGlobalVariable.outputPos", varName, PneumaticCraftUtils.posToString(pos)), false);
            }
            if (!stack.isEmpty()) {
                source.sendFeedback(xlate("pneumaticcraft.command.getGlobalVariable.outputItem", varName, stack.getDisplayName().getString()), false);
            }
            if (pos == null && stack.isEmpty()) {
                source.sendErrorMessage(xlate("pneumaticcraft.command.getGlobalVariable.missing", varName));
            }
        } catch (CommandSyntaxException e) {
            source.sendErrorMessage(new StringTextComponent("Player-globals require player context!"));
        }

        return 1;
    }

    private static int setGlobalVar(CommandContext<CommandSource> ctx, String varName, BlockPos pos) {
        CommandSource source = ctx.getSource();

        if (!varName.startsWith("#") && !varName.startsWith("%")) {
            source.sendFeedback(xlate("pneumaticcraft.command.getGlobalVariable.prefixReminder").mergeStyle(TextFormatting.GOLD), false);
        }

        try {
            UUID id = varName.startsWith("%") ? null : ctx.getSource().asPlayer().getUniqueID();
            GlobalVariableHelper.setPos(id, varName, pos);
            source.sendFeedback(xlate("pneumaticcraft.command.setGlobalVariable.output", varName, PneumaticCraftUtils.posToString(pos)), false);
        } catch (CommandSyntaxException e) {
            source.sendFeedback(new StringTextComponent("Player-globals require player context!"), false);
        }

        return 1;
    }

    static class VarnameType implements ArgumentType<String> {
        @Override
        public String parse(StringReader reader) throws CommandSyntaxException {
            int start = reader.getCursor();
            if (reader.peek() == '#' || reader.peek() == '%') reader.skip();
            while (reader.canRead() && StringUtils.isAlphanumeric(String.valueOf(reader.peek()))) {
                reader.skip();
            }
            return reader.getString().substring(start, reader.getCursor());
        }
    }
}
