package me.desht.pneumaticcraft.common.item;

import com.google.common.collect.Sets;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public class ItemSeismicSensor extends Item {
    private static final int MAX_SEARCH = 500;

    private static final Set<ResourceLocation> fluidsOfInterest = new HashSet<>();
    private static boolean needRecache = true;  // recache on first startup & when tags are reloaded

    public ItemSeismicSensor() {
        super(ModItems.defaultProps().stacksTo(1));
    }

    @Override
    public ActionResultType useOn(ItemUseContext ctx) {
        World world = ctx.getLevel();
        PlayerEntity player = ctx.getPlayer();
        if (!world.isClientSide && player != null) {
            BlockPos.Mutable searchPos = ctx.getClickedPos().mutable();
            while (searchPos.getY() > PneumaticCraftUtils.getMinHeight(world)) {
                searchPos.move(Direction.DOWN);
                Fluid fluid = findFluid(world, searchPos);
                if (fluid != null) {
                    Set<BlockPos> fluidPositions = findLake(world, searchPos.immutable(), fluid);
                    int count = Math.max(1, fluidPositions.size() / 10 * 10);
                    player.displayClientMessage(new TranslationTextComponent(
                            "pneumaticcraft.message.seismicSensor.foundOilDetails",
                            new TranslationTextComponent(fluid.getAttributes().getTranslationKey()),
                            TextFormatting.GREEN.toString() + (ctx.getClickedPos().getY() - searchPos.getY()),
                            TextFormatting.GREEN.toString() + count),
                            false);
                    world.playSound(null, ctx.getClickedPos(), SoundEvents.NOTE_BLOCK_CHIME, SoundCategory.PLAYERS, 1f, 1f);
                    return ActionResultType.SUCCESS;
                }
            }
            player.displayClientMessage(new TranslationTextComponent("pneumaticcraft.message.seismicSensor.noOilFound"), false);
        }
        return ActionResultType.SUCCESS; // we don't want to use the item.
    }

    private Fluid findFluid(World world, BlockPos pos) {
        if (needRecache) {
            fluidsOfInterest.clear();
            for (Fluid f : ForgeRegistries.FLUIDS.getValues()) {
                if (!Sets.intersection(f.getTags(), PNCConfig.Common.Machines.seismicSensorFluidTags).isEmpty()) {
                    fluidsOfInterest.add(f.getRegistryName());
                } else if (PNCConfig.Common.Machines.seismicSensorFluids.contains(f.getRegistryName())) {
                    fluidsOfInterest.add(f.getRegistryName());
                }
            }
            needRecache = false;
        }

        FluidState state = world.getFluidState(pos);
        return fluidsOfInterest.contains(state.getType().getRegistryName()) ? state.getType() : null;
    }

    private Set<BlockPos> findLake(World world, BlockPos searchPos, Fluid fluid) {
        Set<BlockPos> fluidPositions = new HashSet<>();
        Deque<BlockPos> pendingPositions = new ArrayDeque<>();
        pendingPositions.add(searchPos);
        while (!pendingPositions.isEmpty() && fluidPositions.size() < MAX_SEARCH) {
            BlockPos checkingPos = pendingPositions.pop();
            for (Direction d : Direction.values()) {
                if (d != Direction.UP) {
                    BlockPos newPos = checkingPos.relative(d);
                    FluidState state = world.getFluidState(newPos);
                    if (state.getType() == fluid && state.isSource() && fluidPositions.add(newPos)) {
                        pendingPositions.add(newPos);
                    }
                }
            }
        }
        return fluidPositions;
    }

    public static void clearCachedFluids() {
        needRecache = true;
    }
}
