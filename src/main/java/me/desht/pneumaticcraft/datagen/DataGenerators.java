package me.desht.pneumaticcraft.datagen;

import me.desht.pneumaticcraft.api.lib.Names;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.registries.RegistryPatchGenerator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = Names.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeServer(), new ModRecipeProvider(generator, lookupProvider));
        generator.addProvider(event.includeServer(), new ModLootTablesProvider(generator, lookupProvider));
        BlockTagsProvider blockTagsProvider = new ModBlockTagsProvider(generator, lookupProvider, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTagsProvider);
        generator.addProvider(event.includeServer(), new ModItemTagsProvider(generator, lookupProvider, blockTagsProvider.contentsGetter(), existingFileHelper));
        generator.addProvider(event.includeServer(), new ModFluidTagsProvider(generator, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new ModBiomeTagsProvider(generator, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new ModEntityTypeTagsProvider(generator, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new ModAdvancementProvider(generator, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new ModGLMProvider(generator, lookupProvider));
        generator.addProvider(event.includeServer(), new ModPoiTypeTagsProvider(generator, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new ModStructureTagsProvider(generator, lookupProvider, event.getExistingFileHelper()));
//        generator.addProvider(event.includeServer(), new ModDamageTypeTagsProvider(generator.getPackOutput(), lookupProvider, event.getExistingFileHelper()));

        makeProviders(generator.getPackOutput(), lookupProvider, existingFileHelper)
                .forEach(p -> generator.addProvider(event.includeServer(), p));
    }

    public static List<DataProvider> makeProviders(PackOutput output, CompletableFuture<HolderLookup.Provider> vanillaRegistries, ExistingFileHelper efh) {
        RegistrySetBuilder builder = new RegistrySetBuilder()
                .add(Registries.CONFIGURED_FEATURE, ModWorldGenProvider.ConfiguredFeatures::bootstrap)
                .add(Registries.PLACED_FEATURE, ModWorldGenProvider.PlacedFeatures::bootstrap)
                .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModWorldGenProvider.BiomeModifiers::bootstrap)
                .add(Registries.DAMAGE_TYPE, ModDamageTypeProvider::bootstrap);
        return List.of(
                new DatapackBuiltinEntriesProvider(output, vanillaRegistries, builder, Set.of(Names.MOD_ID)),
                new ModDamageTypeTagsProvider(output, append(vanillaRegistries, builder), efh)
        );
    }

    private static CompletableFuture<HolderLookup.Provider> append(CompletableFuture<HolderLookup.Provider> original, RegistrySetBuilder builder) {
        return RegistryPatchGenerator.createLookup(original, builder).thenApply(RegistrySetBuilder.PatchedRegistries::full);
    }
}
