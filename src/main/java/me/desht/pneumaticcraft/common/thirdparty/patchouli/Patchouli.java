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

package me.desht.pneumaticcraft.common.thirdparty.patchouli;

import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.thirdparty.IDocsProvider;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.config.ModConfig;
import vazkii.patchouli.api.*;

import java.util.Arrays;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class Patchouli implements IThirdParty, IDocsProvider {
    private static final ResourceLocation PNC_BOOK = RL("book");

    private static Screen prevGui;

    @Override
    public void clientInit() {
        MinecraftForge.EVENT_BUS.register(this);

        PatchouliAPI.IPatchouliAPI papi = PatchouliAPI.get();

        setConfigFlags(papi);

        IStateMatcher edge = papi.predicateMatcher(ModBlocks.PRESSURE_CHAMBER_WALL.get(), this::validEdge);
        IStateMatcher wall = papi.predicateMatcher(ModBlocks.PRESSURE_CHAMBER_WALL.get(), this::validFace);
        IStateMatcher glass = papi.predicateMatcher(ModBlocks.PRESSURE_CHAMBER_GLASS.get(), this::validFace);
        IStateMatcher valve = papi.predicateMatcher(ModBlocks.PRESSURE_CHAMBER_VALVE.get().defaultBlockState().setValue(FACING, Direction.NORTH), this::validFace);
        IStateMatcher valveUp = papi.predicateMatcher(ModBlocks.PRESSURE_CHAMBER_VALVE.get().defaultBlockState().setValue(FACING, Direction.UP), this::validFace);
        IStateMatcher intI = papi.predicateMatcher(ModBlocks.PRESSURE_CHAMBER_INTERFACE.get().defaultBlockState().setValue(FACING, Direction.EAST), this::validFace);
        IStateMatcher intO = papi.predicateMatcher(ModBlocks.PRESSURE_CHAMBER_INTERFACE.get().defaultBlockState().setValue(FACING, Direction.WEST), this::validFace);

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

    private boolean validEdge(BlockState state) {
        return state.getBlock() == ModBlocks.PRESSURE_CHAMBER_WALL.get() || state.getBlock() == ModBlocks.PRESSURE_CHAMBER_GLASS.get();
    }

    private boolean validFace(BlockState state) {
        return state.getBlock() == ModBlocks.PRESSURE_CHAMBER_WALL.get() || state.getBlock() == ModBlocks.PRESSURE_CHAMBER_GLASS.get()
                || state.getBlock() == ModBlocks.PRESSURE_CHAMBER_INTERFACE.get() || state.getBlock() == ModBlocks.PRESSURE_CHAMBER_VALVE.get();
    }

    private void setConfigFlags(PatchouliAPI.IPatchouliAPI papi) {
        papi.setConfigFlag(Names.MOD_ID + ":" + "inWorldPlasticSolidification", PNCConfig.Common.Recipes.inWorldPlasticSolidification);
        papi.setConfigFlag(Names.MOD_ID + ":" + "inWorldYeastCrafting", PNCConfig.Common.Recipes.inWorldYeastCrafting);
        papi.setConfigFlag(Names.MOD_ID + ":" + "liquidHopperDispenser", PNCConfig.Common.Machines.liquidHopperDispenser);
        papi.setConfigFlag(Names.MOD_ID + ":" + "omniHopperDispenser", PNCConfig.Common.Machines.omniHopperDispenser);
        papi.setConfigFlag(Names.MOD_ID + ":" + "electricCompressorEnabled",  false); // PNCConfig.Common.Recipes.enableElectricCompressorRecipe && Loader.isModLoaded(ModIds.INDUSTRIALCRAFT));
        papi.setConfigFlag(Names.MOD_ID + ":" + "pneumaticGeneratorEnabled", false); // PNCConfig.Common.Recipes.enablePneumaticGeneratorRecipe && Loader.isModLoaded(ModIds.INDUSTRIALCRAFT));
    }

    @SubscribeEvent
    public void onConfigChange(ModConfig.Reloading event) {
        if (event.getConfig().getModId().equals(Names.MOD_ID)) {
            setConfigFlags(PatchouliAPI.get());
        }
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (prevGui != null) {
            // reopen the programmer GUI if that's where we came from
            event.setGui(prevGui);
            prevGui = null;
        }
    }

    @Override
    public void showWidgetDocs(String path) {
        Screen prev = Minecraft.getInstance().screen;  // should be the programmer GUI

        PatchouliAPI.get().openBookEntry(PNC_BOOK, RL("programming/" + path), 1);
        if (PNC_BOOK.equals(PatchouliAPI.get().getOpenBookGui())) {
            prevGui = prev;
        }
    }

    @Override
    public boolean isInstalled() {
        return true;
    }

    @Override
    public ThirdPartyManager.ModType modType() {
        return ThirdPartyManager.ModType.DOCUMENTATION;
    }

    static class Util {
        static IVariable getStacks(Ingredient ingr) {
            return IVariable.wrapList(Arrays.stream(ingr.getItems()).map(IVariable::from).collect(Collectors.toList()));
        }

        public static IVariable getFluidStacks(FluidIngredient ingr) {
            return IVariable.wrapList(ingr.getFluidStacks().stream().map(IVariable::from).collect(Collectors.toList()));
        }
    }
}
