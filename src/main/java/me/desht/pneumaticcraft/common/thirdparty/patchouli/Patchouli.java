package me.desht.pneumaticcraft.common.thirdparty.patchouli;

import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.thirdparty.IDocsProvider;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Direction;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;
import vazkii.patchouli.api.IMultiblock;
import vazkii.patchouli.api.IStateMatcher;
import vazkii.patchouli.api.PatchouliAPI;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;
import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class Patchouli implements IThirdParty, IDocsProvider {
    private static Screen prevGui;

    @Override
    public void clientInit() {
        MinecraftForge.EVENT_BUS.register(this);

        PatchouliAPI.IPatchouliAPI papi = PatchouliAPI.instance;

        setConfigFlags();

        IStateMatcher edge = papi.predicateMatcher(ModBlocks.PRESSURE_CHAMBER_WALL.get(), this::validEdge);
        IStateMatcher wall = papi.predicateMatcher(ModBlocks.PRESSURE_CHAMBER_WALL.get(), this::validFace);
        IStateMatcher glass = papi.predicateMatcher(ModBlocks.PRESSURE_CHAMBER_GLASS.get(), this::validFace);
        IStateMatcher valve = papi.predicateMatcher(ModBlocks.PRESSURE_CHAMBER_VALVE.get().getDefaultState().with(FACING, Direction.NORTH), this::validFace);
        IStateMatcher valveUp = papi.predicateMatcher(ModBlocks.PRESSURE_CHAMBER_VALVE.get().getDefaultState().with(FACING, Direction.UP), this::validFace);
        IStateMatcher intI = papi.predicateMatcher(ModBlocks.PRESSURE_CHAMBER_INTERFACE.get().getDefaultState().with(FACING, Direction.EAST), this::validFace);
        IStateMatcher intO = papi.predicateMatcher(ModBlocks.PRESSURE_CHAMBER_INTERFACE.get().getDefaultState().with(FACING, Direction.WEST), this::validFace);

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
    }

    private boolean validEdge(BlockState state) {
        return state.getBlock() == ModBlocks.PRESSURE_CHAMBER_WALL.get() || state.getBlock() == ModBlocks.PRESSURE_CHAMBER_GLASS.get();
    }

    private boolean validFace(BlockState state) {
        return state.getBlock() == ModBlocks.PRESSURE_CHAMBER_WALL.get() || state.getBlock() == ModBlocks.PRESSURE_CHAMBER_GLASS.get()
                || state.getBlock() == ModBlocks.PRESSURE_CHAMBER_INTERFACE.get() || state.getBlock() == ModBlocks.PRESSURE_CHAMBER_VALVE.get();
    }

    private void setConfigFlags() {
        PatchouliAPI.instance.setConfigFlag(Names.MOD_ID + ":" + "liquidHopperDispenser", PNCConfig.Common.Machines.liquidHopperDispenser);
        PatchouliAPI.instance.setConfigFlag(Names.MOD_ID + ":" + "omniHopperDispenser", PNCConfig.Common.Machines.omniHopperDispenser);
        PatchouliAPI.instance.setConfigFlag(Names.MOD_ID + ":" + "electricCompressorEnabled",  false); // PNCConfig.Common.Recipes.enableElectricCompressorRecipe && Loader.isModLoaded(ModIds.INDUSTRIALCRAFT));
        PatchouliAPI.instance.setConfigFlag(Names.MOD_ID + ":" + "pneumaticGeneratorEnabled", false); // PNCConfig.Common.Recipes.enablePneumaticGeneratorRecipe && Loader.isModLoaded(ModIds.INDUSTRIALCRAFT));
    }

    @SubscribeEvent
    public void onConfigChange(ModConfig.Reloading event) {
        if (event.getConfig().getModId().equals(Names.MOD_ID)) {
            setConfigFlags();
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
        Screen prev = Minecraft.getInstance().currentScreen;  // should be the programmer GUI

        PatchouliAPI.instance.openBookEntry(RL("book"), RL("programming/" + path), 1);
        if (PatchouliAPI.instance.getOpenBookGui().equals(RL("book"))) {
            prevGui = prev;
        }
    }

    @Override
    public boolean isInstalled() {
        return true;
    }
}
