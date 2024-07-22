package me.desht.pneumaticcraft.common.thirdparty.patchouli;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import vazkii.patchouli.api.*;

import java.util.Arrays;
import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

public class PatchouliAccess {
    public static void setup() {
        PatchouliAPI.IPatchouliAPI papi = PatchouliAPI.get();

        setConfigFlags();

        IStateMatcher edge = papi.predicateMatcher(ModBlocks.PRESSURE_CHAMBER_WALL.get(), PatchouliAccess::validEdge);
        IStateMatcher wall = papi.predicateMatcher(ModBlocks.PRESSURE_CHAMBER_WALL.get(), PatchouliAccess::validFace);
        IStateMatcher glass = papi.predicateMatcher(ModBlocks.PRESSURE_CHAMBER_GLASS.get(), PatchouliAccess::validFace);
        IStateMatcher valve = papi.predicateMatcher(ModBlocks.PRESSURE_CHAMBER_VALVE.get().defaultBlockState().setValue(FACING, Direction.NORTH), PatchouliAccess::validFace);
        IStateMatcher valveUp = papi.predicateMatcher(ModBlocks.PRESSURE_CHAMBER_VALVE.get().defaultBlockState().setValue(FACING, Direction.UP), PatchouliAccess::validFace);
        IStateMatcher intI = papi.predicateMatcher(ModBlocks.PRESSURE_CHAMBER_INTERFACE.get().defaultBlockState().setValue(FACING, Direction.EAST), PatchouliAccess::validFace);
        IStateMatcher intO = papi.predicateMatcher(ModBlocks.PRESSURE_CHAMBER_INTERFACE.get().defaultBlockState().setValue(FACING, Direction.WEST), PatchouliAccess::validFace);

        IMultiblock pc3 = papi.makeMultiblock(new String[][] {
                        { "WWW", "WWW", "WWW" },
                        { "WIW", "VAF", "WIW" },
                        { "WWW", "W0W", "WWW" },
                },
                'W', edge, 'F', glass, '0', wall, 'V', valve, 'I', intI, 'O', intO, 'A', papi.airMatcher()
        ).setSymmetrical(true);
        papi.registerMultiblock(RL("pressure_chamber_3"), pc3);

        IMultiblock pc4 = papi.makeMultiblock(new String[][] {
                        { "WWWW", "WWWW", "WWWW", "WWWW" },
                        { "WFFW", "VAAF", "FAAF", "WFFW" },
                        { "WFFW", "VAAF", "FAAF", "WIOW" },
                        { "WWWW", "W0WW", "WWWW", "WWWW" },
                },
                'W', edge, 'F', glass, '0', wall, 'V', valve, 'I', intI, 'O', intO, 'A', papi.airMatcher()
        ).setSymmetrical(false);
        papi.registerMultiblock(RL("pressure_chamber_4"), pc4);

        IMultiblock pc5 = papi.makeMultiblock(new String[][] {
                        { "WWWWW", "WWVWW", "WVWVW", "WWVWW", "WWWWW" },
                        { "WFFFW", "FAAAF", "FAAAF", "FAAAF", "WOFIW" },
                        { "WFFFW", "FAAAF", "FAAAF", "FAAAF", "WFFFW" },
                        { "WFFFW", "FAAAF", "FAAAF", "FAAAF", "WOFIW" },
                        { "WWWWW", "WWWWW", "WW0WW", "WWWWW", "WWWWW" },
                },
                'W', edge, 'F', glass, '0', wall, 'V', valveUp, 'I', intI, 'O', intO, 'A', papi.airMatcher()
        ).setSymmetrical(true);
        papi.registerMultiblock(RL("pressure_chamber_5"), pc5);

        VariableHelper.instance().registerSerializer(new FluidStackVariableSerializer(), FluidStack.class);
    }

    private static boolean validEdge(BlockState state) {
        return state.getBlock() == ModBlocks.PRESSURE_CHAMBER_WALL.get() || state.getBlock() == ModBlocks.PRESSURE_CHAMBER_GLASS.get();
    }

    private static boolean validFace(BlockState state) {
        return state.getBlock() == ModBlocks.PRESSURE_CHAMBER_WALL.get() || state.getBlock() == ModBlocks.PRESSURE_CHAMBER_GLASS.get()
                || state.getBlock() == ModBlocks.PRESSURE_CHAMBER_INTERFACE.get() || state.getBlock() == ModBlocks.PRESSURE_CHAMBER_VALVE.get();
    }

    static void setConfigFlags() {
        PatchouliAPI.IPatchouliAPI papi = PatchouliAPI.get();

        papi.setConfigFlag(Names.MOD_ID + ":" + "inWorldPlasticSolidification", ConfigHelper.common().recipes.inWorldPlasticSolidification.get());
        papi.setConfigFlag(Names.MOD_ID + ":" + "inWorldYeastCrafting", ConfigHelper.common().recipes.inWorldYeastCrafting.get());
        papi.setConfigFlag(Names.MOD_ID + ":" + "liquidHopperDispenser", ConfigHelper.common().machines.liquidHopperDispenser.get());
        papi.setConfigFlag(Names.MOD_ID + ":" + "omniHopperDispenser", ConfigHelper.common().machines.omniHopperDispenser.get());
        papi.setConfigFlag(Names.MOD_ID + ":" + "securityStationHacking", ConfigHelper.common().machines.securityStationAllowHacking.get());

        // stubbed since IC2 isn't around at the moment
        papi.setConfigFlag(Names.MOD_ID + ":" + "electricCompressorEnabled",  false); // ConfigHelper.common().recipes.enableElectricCompressorRecipe.get() && Loader.isModLoaded(ModIds.INDUSTRIALCRAFT));
        papi.setConfigFlag(Names.MOD_ID + ":" + "pneumaticGeneratorEnabled", false); // ConfigHelper.common().recipes.enablePneumaticGeneratorRecipe.get() && Loader.isModLoaded(ModIds.INDUSTRIALCRAFT));
    }

    static boolean openBookEntry(ResourceLocation page) {
        PatchouliAPI.get().openBookEntry(Patchouli.PNC_BOOK, page, 1);
        return Patchouli.PNC_BOOK.equals(PatchouliAPI.get().getOpenBookGui());
    }

    static IVariable getStacks(SizedIngredient ingr, HolderLookup.Provider lookup) {
        return IVariable.wrapList(Arrays.stream(ingr.getItems()).map((ItemStack object) -> IVariable.from(object, lookup)).toList(), lookup);
    }

    static IVariable getStacks(Ingredient ingr, HolderLookup.Provider lookup) {
        return IVariable.wrapList(Arrays.stream(ingr.getItems()).map((ItemStack object) -> IVariable.from(object, lookup)).toList(), lookup);
    }

    static IVariable getStacks(List<ItemStack> stacks, HolderLookup.Provider lookup) {
        return IVariable.wrapList(stacks.stream().map((ItemStack object) -> IVariable.from(object, lookup)).toList(), lookup);
    }

    public static IVariable getFluidStacks(SizedFluidIngredient ingr, HolderLookup.Provider lookup) {
        return IVariable.wrapList(Arrays.stream(ingr.getFluids()).map((FluidStack object) -> IVariable.from(object, lookup)).toList(), lookup);
    }
}
