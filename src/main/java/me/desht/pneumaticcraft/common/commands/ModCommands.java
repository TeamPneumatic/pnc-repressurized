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
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSetGlobalVariable;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorRegistry;
import me.desht.pneumaticcraft.common.util.GlobalPosHelper;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.variables.GlobalVariableHelper;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.arguments.EntityArgument.getPlayer;
import static net.minecraft.commands.arguments.coordinates.BlockPosArgument.getLoadedBlockPos;

public class ModCommands {
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES
            = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, Names.MOD_ID);
    private static final DeferredHolder<ArgumentTypeInfo<?, ?>, SingletonArgumentInfo<VarnameType>> VARNAME_COMMAND_ARGUMENT_TYPE
            = COMMAND_ARGUMENT_TYPES.register("varname",
            () -> ArgumentTypeInfos.registerByClass(ModCommands.VarnameType.class, SingletonArgumentInfo.contextFree(VarnameType::new)));
    private static final ResourceLocation UNKNOWN_ITEM = RL("unknown");

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(literal("pncr")
                .then(literal("dump_nbt")
                        .requires(cs -> cs.hasPermission(2))
                        .executes(ModCommands::dumpNBT)
                )
                .then(literal("global_var")
                        .then(literal("get")
                                .then(argument("varname", new VarnameType())
                                        .suggests(ModCommands::suggestVarNames)
                                        .executes(c -> getGlobalVar(c, StringArgumentType.getString(c,"varname")))
                                )
                        )
                        .then(literal("set")
                                .then(argument("varname", new VarnameType())
                                        .then(argument("pos", BlockPosArgument.blockPos())
                                                .executes(c -> setGlobalVar(c, StringArgumentType.getString(c,"varname"), Either.left(BlockPosArgument.getLoadedBlockPos(c, "pos"))))
                                        )
                                        .then(argument("item", ItemArgument.item(buildContext))
                                                .executes(c -> setGlobalVar(c, StringArgumentType.getString(c,"varname"), Either.right(ItemArgument.getItem(c, "item"))))
                                        )
                                )
                        )
                        .then(literal("delete")
                                .then(argument("varname", StringArgumentType.greedyString())
                                        .suggests(ModCommands::suggestVarNames)
                                        .executes(c -> delGlobalVar(c, StringArgumentType.getString(c,"varname")))
                                )
                        )
                        .then(literal("list")
                                .executes(ModCommands::listGlobalVars)
                        )
                )
                .then(literal("amadrone_deliver")
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
                )
                .then(literal("armor_upgrade")
                        .then(argument("upgrade", ResourceLocationArgument.id())
                                .suggests((ctx, builder) -> suggestUpgradeIDs(builder))
                                .then(argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> setArmorUpgrade(ctx.getSource(), ResourceLocationArgument.getId(ctx, "upgrade"), BoolArgumentType.getBool(ctx, "enabled")))
                                )
                        )
                )
        );
    }

    private static int setArmorUpgrade(CommandSourceStack source, ResourceLocation id, boolean enabled) throws CommandSyntaxException {
        Optional<IArmorUpgradeHandler<?>> upgrade = CommonArmorRegistry.getInstance().getArmorUpgradeHandler(id);

        if (upgrade.isPresent()) {
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(source.getPlayerOrException());
            if (handler.upgradeUsable(upgrade.get(), false)) {
                handler.setUpgradeEnabled(upgrade.get(), enabled);
                source.sendSuccess(() -> Component.literal(id + " enabled = " + enabled), false);
                return 1;
            } else {
                source.sendFailure(Component.literal("Upgrade " + id + " is not inserted!").withStyle(ChatFormatting.RED));
                return 0;
            }
        } else {
            source.sendFailure(Component.literal("Unknown upgrade ID: " + id).withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static CompletableFuture<Suggestions> suggestUpgradeIDs(SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(ArmorUpgradeRegistry.getInstance().getKnownUpgradeIds(), builder);
    }

    private static CompletableFuture<Suggestions> suggestVarNames(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        Collection<String> varNames = GlobalVariableManager.getInstance().getAllActiveVariableNames(ctx.getSource().getPlayer());
        return SharedSuggestionProvider.suggest(varNames, builder);
    }

    private static int dumpNBT(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        if (source.getEntity() instanceof Player) {
            ItemStack held = ((Player) source.getEntity()).getMainHandItem();
            if (held.getTag() == null) {
                source.sendFailure(Component.literal("No NBT"));
                return 0;
            } else if (held.getTag().isEmpty()) {
                source.sendFailure(Component.literal("Empty NBT"));
                return 0;
            }
            source.sendSuccess(() -> Component.literal(held.getTag().toString()), false);
            return 1;
        }
        return 0;
    }

    private static int amadroneDeliver(CommandSourceStack source, BlockPos toPos, BlockPos fromPos) {
        BlockEntity te = source.getLevel().getBlockEntity(fromPos);

        int status = IOHelper.getInventoryForBlock(te).map(inv -> {
            List<ItemStack> deliveredStacks = new ArrayList<>();
            for (int i = 0; i < inv.getSlots() && deliveredStacks.size() < 36; i++) {
                if (!inv.getStackInSlot(i).isEmpty()) deliveredStacks.add(inv.getStackInSlot(i));
            }
            if (!deliveredStacks.isEmpty()) {
                GlobalPos gPos = GlobalPosHelper.makeGlobalPos(source.getLevel(), toPos);
                PneumaticRegistry.getInstance().getDroneRegistry().deliverItemsAmazonStyle(gPos, deliveredStacks.toArray(new ItemStack[0]));
                source.sendSuccess(() -> xlate("pneumaticcraft.command.deliverAmazon.success", PneumaticCraftUtils.posToString(fromPos), PneumaticCraftUtils.posToString(toPos)), false);
                return 1;
            } else {
                source.sendFailure(xlate("pneumaticcraft.command.deliverAmazon.noItems", PneumaticCraftUtils.posToString(fromPos)));
                return 0;
            }
        }).orElse(-1);

        if (status == -1) source.sendFailure(xlate("pneumaticcraft.command.deliverAmazon.noInventory", PneumaticCraftUtils.posToString(fromPos)));
        return status;
    }

    private static int listGlobalVars(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        Player playerEntity = source.getEntity() instanceof Player ? (Player) source.getEntity() : null;
        UUID id = playerEntity == null ? null : playerEntity.getUUID();
        Collection<String> varNames = GlobalVariableManager.getInstance().getAllActiveVariableNames(playerEntity);
        source.sendSuccess(() -> Component.literal(varNames.size() + " vars").withStyle(ChatFormatting.GREEN, ChatFormatting.UNDERLINE), false);
        varNames.stream().sorted().forEach(var -> {
            BlockPos pos = GlobalVariableHelper.getPos(id, var);
            ItemStack stack = GlobalVariableHelper.getStack(id, var);
            String val = PneumaticCraftUtils.posToString(pos) + (stack.isEmpty() ? "" : " / " + PneumaticCraftUtils.getRegistryName(stack.getItem()).orElse(UNKNOWN_ITEM));
            source.sendSuccess(() -> Component.literal(var).append(" = [").append(val).append("]"), false);
        });
        return 1;
    }

    private static int getGlobalVar(CommandContext<CommandSourceStack> ctx, String varName0) {
        CommandSourceStack source = ctx.getSource();
        String varName;
        if (!GlobalVariableHelper.hasPrefix(varName0)) {
            source.sendSuccess(() -> xlate("pneumaticcraft.command.globalVariable.prefixReminder", varName0).withStyle(ChatFormatting.GOLD), false);
            varName = "#" + varName0;
        } else {
            varName = varName0;
        }
        UUID id = varName.startsWith("%") || !(ctx.getSource().getEntity() instanceof Player player) ? null : player.getUUID();
        BlockPos pos = GlobalVariableHelper.getPos(id, varName);
        ItemStack stack = GlobalVariableHelper.getStack(id, varName);
        String val = PneumaticCraftUtils.posToString(pos) + (stack.isEmpty() ? "" : " / " + PneumaticCraftUtils.getRegistryName(stack.getItem()).orElse(UNKNOWN_ITEM));
        if (pos == null && stack.isEmpty()) {
            source.sendFailure(xlate("pneumaticcraft.command.globalVariable.missing", varName));
        } else {
            source.sendSuccess(() -> xlate("pneumaticcraft.command.globalVariable.output", varName, val), false);
        }

        return 1;
    }

    private static int setGlobalVar(CommandContext<CommandSourceStack> ctx, String varName0, Either<BlockPos, ItemInput> posOrItem) {
        CommandSourceStack source = ctx.getSource();

        String varName;
        if (!GlobalVariableHelper.hasPrefix(varName0)) {
            source.sendSuccess(() -> xlate("pneumaticcraft.command.globalVariable.prefixReminder", varName0).withStyle(ChatFormatting.GOLD), false);
            varName = "#" + varName0;
        } else {
            varName = varName0;
        }

        try {
            UUID id = varName.startsWith("%") ? null : ctx.getSource().getPlayerOrException().getUUID();
            final String v = varName;
            posOrItem.ifLeft(pos -> {
                GlobalVariableHelper.setPos(id, v, pos);
                source.sendSuccess(() -> xlate("pneumaticcraft.command.globalVariable.output", v, PneumaticCraftUtils.posToString(pos)), true);
            }).ifRight(item -> {
                ItemStack stack = new ItemStack(item.getItem());
                GlobalVariableHelper.setStack(id, v, stack);
                source.sendSuccess(() -> xlate("pneumaticcraft.command.globalVariable.output", v, PneumaticCraftUtils.getRegistryName(stack.getItem()).orElse(UNKNOWN_ITEM)), true);
            });
        } catch (CommandSyntaxException e) {
            source.sendFailure(Component.literal("Player-globals require player context!"));
        }

        return 1;
    }

    private static int delGlobalVar(CommandContext<CommandSourceStack> ctx, String varName0) {
        CommandSourceStack source = ctx.getSource();

        String varName;
        if (!GlobalVariableHelper.hasPrefix(varName0)) {
            source.sendSuccess(() -> xlate("pneumaticcraft.command.globalVariable.prefixReminder", varName0).withStyle(ChatFormatting.GOLD), false);
            varName = "#" + varName0;
        } else {
            varName = varName0;
        }

        try {
            UUID id = varName.startsWith("%") ? null : ctx.getSource().getPlayerOrException().getUUID();
            if (GlobalVariableHelper.getPos(id, varName) == null && GlobalVariableHelper.getStack(id, varName).isEmpty()) {
                source.sendFailure(xlate("pneumaticcraft.command.globalVariable.missing", varName));
            } else {
                GlobalVariableHelper.setPos(id, varName, null);
                GlobalVariableHelper.setStack(id, varName, ItemStack.EMPTY);
                // global var deletions need to get sync'd to players; syncing normally happens when remote/gps tool/etc GUI's
                // are opened, but deleted vars won't get sync'd there, so could wrongly hang around on the client
                if (id != null) {
                    PneumaticRegistry.getInstance().getMiscHelpers().syncGlobalVariable(ctx.getSource().getPlayerOrException(), varName);
                } else {
                    NetworkHandler.sendToAll(PacketSetGlobalVariable.forPos(varName, null));
                    NetworkHandler.sendToAll(PacketSetGlobalVariable.forItem(varName, ItemStack.EMPTY));
                }
                source.sendSuccess(() -> xlate("pneumaticcraft.command.globalVariable.delete", varName), true);
            }
        } catch (CommandSyntaxException e) {
            source.sendFailure(Component.literal("Player-globals require player context!"));
        }
        return 1;
    }

    private static class VarnameType implements ArgumentType<String> {
        @Override
        public String parse(StringReader reader) {
            int start = reader.getCursor();
            if (reader.peek() == '#' || reader.peek() == '%') reader.skip();
            while (reader.canRead() && (StringUtils.isAlphanumeric(String.valueOf(reader.peek())) || reader.peek() == '_')) {
                reader.skip();
            }
            return reader.getString().substring(start, reader.getCursor());
        }
    }
}
