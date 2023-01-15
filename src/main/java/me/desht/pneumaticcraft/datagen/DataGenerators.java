package me.desht.pneumaticcraft.datagen;

import me.desht.pneumaticcraft.api.lib.Names;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

@Mod.EventBusSubscriber(modid = Names.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeServer(), new ModRecipeProvider(generator));
        generator.addProvider(event.includeServer(), new ModLootTablesProvider(generator));
        BlockTagsProvider blockTagsProvider = new ModBlockTagsProvider(generator, lookupProvider, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTagsProvider);
        generator.addProvider(event.includeServer(), new ModItemTagsProvider(generator, lookupProvider, blockTagsProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new ModFluidTagsProvider(generator, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new ModBiomeTagsProvider(generator, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new ModEntityTypeTagsProvider(generator, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new ModAdvancementProvider(generator, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new ModGLMProvider(generator));
        generator.addProvider(event.includeServer(), new ModPoiTypeTagsProvider(generator, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new ModStructureTagsProvider(generator, lookupProvider, event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), ModWorldGenProvider.makeProvider(generator.getPackOutput(), lookupProvider));
    }

    private static <T extends DataProvider> DataProvider.Factory<T> bindRegistries(
            BiFunction<PackOutput, CompletableFuture<HolderLookup.Provider>, T> datagenFactory,
            CompletableFuture<HolderLookup.Provider> lookupProvider)
    {
        return packOutput -> datagenFactory.apply(packOutput, lookupProvider);
    }
}
