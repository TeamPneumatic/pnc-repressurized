package me.desht.pneumaticcraft.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSetGlobalVariable;
import me.desht.pneumaticcraft.common.util.GlobalPosHelper;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.variables.GlobalVariableHelper;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.ItemArgument;
import net.minecraft.command.arguments.ItemInput;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
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
                .then(literal("global_var")
                        .then(literal("get")
                                .then(argument("varname", new VarnameType())
                                        .executes(c -> getGlobalVar(c, StringArgumentType.getString(c,"varname")))
                                )
                        )
                        .then(literal("set")
                                .then(argument("varname", new VarnameType())
                                        .then(argument("pos", BlockPosArgument.blockPos())
                                                .executes(c -> setGlobalVar(c, StringArgumentType.getString(c,"varname"), Either.left(BlockPosArgument.getLoadedBlockPos(c, "pos"))))
                                        )
                                        .then(argument("item", ItemArgument.item())
                                                .executes(c -> setGlobalVar(c, StringArgumentType.getString(c,"varname"), Either.right(ItemArgument.getItem(c, "item"))))
                                        )
                                )
                        )
                        .then(literal("delete")
                                .then(argument("varname", new VarnameType())
                                        .executes(c -> delGlobalVar(c, StringArgumentType.getString(c,"varname")))
                                )
                        )
                        .then(literal("list")
                                .executes(ModCommands::listGlobals)
                        )
                        .then(literal("import_server_globals") // TODO remove in 1.17
                                .executes(ModCommands::importGlobals)
                        )
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
                source.sendFeedback(xlate("pneumaticcraft.command.importGlobals.success"), true);
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
                source.sendFeedback(xlate("pneumaticcraft.command.deliverAmazon.success", PneumaticCraftUtils.posToString(fromPos), PneumaticCraftUtils.posToString(toPos)), true);
                return 1;
            } else {
                source.sendErrorMessage(xlate("pneumaticcraft.command.deliverAmazon.noItems", PneumaticCraftUtils.posToString(fromPos)));
                return 0;
            }
        }).orElse(-1);

        if (status == -1) source.sendErrorMessage(xlate("pneumaticcraft.command.deliverAmazon.noInventory", PneumaticCraftUtils.posToString(fromPos)));
        return status;
    }

    private static int listGlobals(CommandContext<CommandSource> ctx) {
        CommandSource source = ctx.getSource();
        PlayerEntity playerEntity = source.getEntity() instanceof PlayerEntity ? (PlayerEntity) source.getEntity() : null;
        UUID id = playerEntity == null ? null : playerEntity.getUniqueID();
        Collection<String> varNames = GlobalVariableManager.getInstance().getAllActiveVariableNames(playerEntity);
        source.sendFeedback(new StringTextComponent(varNames.size() + " vars").mergeStyle(TextFormatting.GREEN, TextFormatting.UNDERLINE), false);
        varNames.stream().sorted().forEach(var -> {
            BlockPos pos = GlobalVariableHelper.getPos(id, var);
            ItemStack stack = GlobalVariableHelper.getStack(id, var);
            String val = PneumaticCraftUtils.posToString(pos);
            if (!stack.isEmpty()) val += " / " + stack.getItem().getRegistryName();
            source.sendFeedback(new StringTextComponent(var).appendString(" = [").appendString(val).appendString("]"), false);
        });
        return 1;
    }

    private static int getGlobalVar(CommandContext<CommandSource> ctx, String varName) {
        CommandSource source = ctx.getSource();
        if (!GlobalVariableHelper.hasPrefix(varName)) {
            source.sendFeedback(xlate("pneumaticcraft.command.globalVariable.prefixReminder").mergeStyle(TextFormatting.GOLD), false);
            varName = "#" + varName;
        }
        try {
            UUID id = varName.startsWith("%") ? null : ctx.getSource().asPlayer().getUniqueID();
            BlockPos pos = GlobalVariableHelper.getPos(id, varName);
            ItemStack stack = GlobalVariableHelper.getStack(id, varName);
            String val = PneumaticCraftUtils.posToString(pos);
            if (!stack.isEmpty()) val += " / " + stack.getItem().getRegistryName();
            if (pos == null && stack.isEmpty()) {
                source.sendErrorMessage(xlate("pneumaticcraft.command.globalVariable.missing", varName));
            } else {
                source.sendFeedback(xlate("pneumaticcraft.command.globalVariable.output", varName, val), false);
            }
        } catch (CommandSyntaxException e) {
            source.sendErrorMessage(new StringTextComponent("Player-globals require player context!"));
        }

        return 1;
    }

    private static int setGlobalVar(CommandContext<CommandSource> ctx, String varName, Either<BlockPos, ItemInput> posOrItem) {
        CommandSource source = ctx.getSource();

        if (!GlobalVariableHelper.hasPrefix(varName)) {
            source.sendFeedback(xlate("pneumaticcraft.command.globalVariable.prefixReminder").mergeStyle(TextFormatting.GOLD), false);
            varName = "#" + varName;
        }

        try {
            UUID id = varName.startsWith("%") ? null : ctx.getSource().asPlayer().getUniqueID();
            final String v = varName;
            posOrItem.ifLeft(pos -> {
                GlobalVariableHelper.setPos(id, v, pos);
                source.sendFeedback(xlate("pneumaticcraft.command.globalVariable.output", v, PneumaticCraftUtils.posToString(pos)), true);
            }).ifRight(item -> {
                ItemStack stack = new ItemStack(item.getItem());
                GlobalVariableHelper.setStack(id, v, stack);
                source.sendFeedback(xlate("pneumaticcraft.command.globalVariable.output", v, stack.getItem().getRegistryName()), true);
            });
        } catch (CommandSyntaxException e) {
            source.sendErrorMessage(new StringTextComponent("Player-globals require player context!"));
        }

        return 1;
    }

    private static int delGlobalVar(CommandContext<CommandSource> ctx, String varName) {
        CommandSource source = ctx.getSource();
        if (!varName.startsWith("#") && !varName.startsWith("%")) {
            source.sendFeedback(xlate("pneumaticcraft.command.globalVariable.prefixReminder").mergeStyle(TextFormatting.GOLD), false);
        }

        try {
            UUID id = varName.startsWith("%") ? null : ctx.getSource().asPlayer().getUniqueID();
            if (GlobalVariableHelper.getPos(id, varName) == null && GlobalVariableHelper.getStack(id, varName).isEmpty()) {
                source.sendErrorMessage(xlate("pneumaticcraft.command.globalVariable.missing", varName));
            } else {
                GlobalVariableHelper.setPos(id, varName, null);
                GlobalVariableHelper.setStack(id, varName, ItemStack.EMPTY);
                // global var deletions need to get sync'd to players; syncing normally happens when remote/gps tool/etc GUI's
                // are opened, but deleted vars won't get sync'd there, so could wrongly hang around on the client
                if (id != null) {
                    PneumaticRegistry.getInstance().syncGlobalVariable(ctx.getSource().asPlayer(), varName);
                } else {
                    NetworkHandler.sendToAll(new PacketSetGlobalVariable(varName, (BlockPos) null));
                    NetworkHandler.sendToAll(new PacketSetGlobalVariable(varName, ItemStack.EMPTY));
                }
                source.sendFeedback(xlate("pneumaticcraft.command.globalVariable.delete", varName), true);
            }
        } catch (CommandSyntaxException e) {
            source.sendErrorMessage(new StringTextComponent("Player-globals require player context!"));
        }
        return 1;
    }

    static class VarnameType implements ArgumentType<String> {
        @Override
        public String parse(StringReader reader) throws CommandSyntaxException {
            int start = reader.getCursor();
            if (reader.peek() == '#' || reader.peek() == '%') reader.skip();
            while (reader.canRead() && (StringUtils.isAlphanumeric(String.valueOf(reader.peek())) || reader.peek() == '_')) {
                reader.skip();
            }
            return reader.getString().substring(start, reader.getCursor());
        }
    }
}
