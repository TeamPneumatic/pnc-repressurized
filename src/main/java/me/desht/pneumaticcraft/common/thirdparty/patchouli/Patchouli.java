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
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import vazkii.patchouli.api.IMultiblock;
import vazkii.patchouli.api.IStateMatcher;
import vazkii.patchouli.api.PatchouliAPI;
import vazkii.patchouli.client.book.BookEntry;
import vazkii.patchouli.client.book.gui.GuiBook;
import vazkii.patchouli.client.book.gui.GuiBookEntry;
import vazkii.patchouli.common.book.Book;
import vazkii.patchouli.common.book.BookRegistry;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;
import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class Patchouli implements IThirdParty, IDocsProvider {
    private static Screen prevGui;

    @Override
    public void clientPreInit() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void clientInit() {
        PatchouliAPI.IPatchouliAPI papi = PatchouliAPI.instance;

        setConfigFlags();

        IStateMatcher edge = papi.predicateMatcher(ModBlocks.PRESSURE_CHAMBER_WALL, this::validEdge);
        IStateMatcher wall = papi.predicateMatcher(ModBlocks.PRESSURE_CHAMBER_WALL, this::validFace);
        IStateMatcher glass = papi.predicateMatcher(ModBlocks.PRESSURE_CHAMBER_GLASS, this::validFace);
        IStateMatcher valve = papi.predicateMatcher(ModBlocks.PRESSURE_CHAMBER_VALVE.getDefaultState().with(FACING, Direction.NORTH), this::validFace);
        IStateMatcher valveUp = papi.predicateMatcher(ModBlocks.PRESSURE_CHAMBER_VALVE.getDefaultState().with(FACING, Direction.UP), this::validFace);
        IStateMatcher intI = papi.predicateMatcher(ModBlocks.PRESSURE_CHAMBER_INTERFACE.getDefaultState().with(FACING, Direction.EAST), this::validFace);
        IStateMatcher intO = papi.predicateMatcher(ModBlocks.PRESSURE_CHAMBER_INTERFACE.getDefaultState().with(FACING, Direction.WEST), this::validFace);

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
        return state.getBlock() == ModBlocks.PRESSURE_CHAMBER_WALL || state.getBlock() == ModBlocks.PRESSURE_CHAMBER_GLASS;
    }

    private boolean validFace(BlockState state) {
        return state.getBlock() == ModBlocks.PRESSURE_CHAMBER_WALL || state.getBlock() == ModBlocks.PRESSURE_CHAMBER_GLASS
                || state.getBlock() == ModBlocks.PRESSURE_CHAMBER_INTERFACE || state.getBlock() == ModBlocks.PRESSURE_CHAMBER_VALVE;
    }

    private void setConfigFlags() {
        PatchouliAPI.instance.setConfigFlag(Names.MOD_ID + ":" + "liquidHopperDispenser", PNCConfig.Common.Machines.liquidHopperDispenser);
        PatchouliAPI.instance.setConfigFlag(Names.MOD_ID + ":" + "omniHopperDispenser", PNCConfig.Common.Machines.omniHopperDispenser);
        PatchouliAPI.instance.setConfigFlag(Names.MOD_ID + ":" + "electricCompressorEnabled",  false); // PNCConfig.Common.Recipes.enableElectricCompressorRecipe && Loader.isModLoaded(ModIds.INDUSTRIALCRAFT));
        PatchouliAPI.instance.setConfigFlag(Names.MOD_ID + ":" + "pneumaticGeneratorEnabled", false); // PNCConfig.Common.Recipes.enablePneumaticGeneratorRecipe && Loader.isModLoaded(ModIds.INDUSTRIALCRAFT));
    }

    @SubscribeEvent
    public void onConfigChange(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(Names.MOD_ID)) {
            setConfigFlags();
        }
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (prevGui != null) {
            event.setGui(prevGui);
            prevGui = null;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void openBookGui(ResourceLocation bookRes, String entryId) {
        // This is bad and I feel bad. It will go if & when a Patchouli API method is added to do it properly.

        Screen prev = Minecraft.getInstance().currentScreen;

        Book book = BookRegistry.INSTANCE.books.get(bookRes);
        if (book != null) {
            BookEntry entry = book.contents.entries.get(new ResourceLocation(bookRes.getNamespace(), entryId));
            if (entry != null) {
                GuiBook curr = book.contents.getCurrentGui();
                book.contents.currentGui = new GuiBookEntry(book, entry, 0);
                book.contents.guiStack.push(curr);
                book.contents.openLexiconGui(book.contents.getCurrentGui(), true);
                prevGui = prev;
            }
        }
    }

    @Override
    public void showWidgetDocs(String path) {
        openBookGui(RL("book"), "programming/" + path);
    }

    @Override
    public boolean docsProviderInstalled() {
        return true;
    }
}
