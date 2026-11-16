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

package me.desht.pneumaticcraft.datagen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.client.model.custom.FluidItemModel;
import me.desht.pneumaticcraft.client.model.item.JackhammerDrillBitProperty;
import me.desht.pneumaticcraft.client.render.MinigunItemRenderer;
import me.desht.pneumaticcraft.common.item.PneumaticCraftBucketItem;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModItems;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.properties.select.DisplayContext;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.item.DynamicFluidContainerModel;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Generates the client item definitions ({@code assets/pneumaticcraft/items/*.json}) for all PneumaticCraft items.
 * <p>
 * The models themselves are still hand-written in {@code src/main/resources}; the default definition is a plain
 * {@code pneumaticcraft:item/<name>} model. Items which are tinted at runtime get one
 * {@code pneumaticcraft:tintable} tint source per tint index used by their model, worked out by reading the
 * existing model JSON (following parents).
 */
public class ModModelProvider extends ModelProvider {
    private static final Set<Identifier> GENERATED_PARENTS = Set.of(
            Identifier.withDefaultNamespace("item/generated"),
            Identifier.withDefaultNamespace("builtin/generated")
    );
    private static final int DRILL_BIT_MODELS = 6;

    private final ResourceManager resourceManager;
    private final Map<Identifier, Integer> tintCounts = new HashMap<>();

    public ModModelProvider(PackOutput output, ResourceManager resourceManager) {
        super(output, Names.MOD_ID);
        this.resourceManager = resourceManager;
    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        // TODO 26.1 datagen: blockstates and block models are still hand-written in src/main/resources
        return Stream.empty();
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        ItemModelOutput output = itemModels.itemModelOutput;

        fluidTank(output, ModBlocks.TANK_SMALL, "fluid_tank_inv_iron");
        fluidTank(output, ModBlocks.TANK_MEDIUM, "fluid_tank_inv_gold");
        fluidTank(output, ModBlocks.TANK_LARGE, "fluid_tank_inv_diamond");
        fluidTank(output, ModBlocks.TANK_HUGE, "fluid_tank_inv_netherite");
        fluidTank(output, ModBlocks.LIQUID_HOPPER, "liquid_hopper_inv");
        minigun(output);
        jackhammer(output);

        for (var holder : ModItems.ITEMS.getEntries()) {
            Item item = holder.get();
            if (item == ModBlocks.TANK_SMALL.asItem() || item == ModBlocks.TANK_MEDIUM.asItem()
                    || item == ModBlocks.TANK_LARGE.asItem() || item == ModBlocks.TANK_HUGE.asItem()
                    || item == ModBlocks.LIQUID_HOPPER.asItem() || item == ModItems.MINIGUN.get()
                    || item == ModItems.JACKHAMMER.get()) {
                continue;
            }
            if (item instanceof PneumaticCraftBucketItem bucket) {
                output.accept(item, new DynamicFluidContainerModel.Unbaked(
                        new DynamicFluidContainerModel.Textures(Optional.empty(),
                                Optional.of(new Material(Identifier.withDefaultNamespace("item/bucket"))),
                                Optional.of(new Material(Identifier.fromNamespaceAndPath("neoforge", "item/mask/bucket_fluid"))),
                                Optional.empty()),
                        bucket.content, false, true, true));
            } else {
                output.accept(item, model(item, RL("item/" + holder.getId().getPath())));
            }
        }
    }

    private void fluidTank(ItemModelOutput output, ItemLike item, String modelName) {
        Identifier model = RL("block/" + modelName);
        output.accept(item.asItem(), ItemModelUtils.composite(
                ItemModelUtils.plainModel(model),
                ItemModelUtils.specialModel(model, FluidItemModel.Unbaked.INSTANCE)
        ));
    }

    private void minigun(ItemModelOutput output) {
        Identifier special = RL("item/minigun_special");
        output.accept(ModItems.MINIGUN.get(), ItemModelUtils.select(new DisplayContext(),
                ItemModelUtils.plainModel(RL("block/minigun")),
                ItemModelUtils.when(List.of(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, ItemDisplayContext.FIRST_PERSON_LEFT_HAND),
                        ItemModelUtils.specialModel(special, new MinigunItemRenderer.Unbaked(false))),
                ItemModelUtils.when(List.of(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, ItemDisplayContext.THIRD_PERSON_LEFT_HAND),
                        ItemModelUtils.specialModel(special, new MinigunItemRenderer.Unbaked(true)))
        ));
    }

    private void jackhammer(ItemModelOutput output) {
        Item jackhammer = ModItems.JACKHAMMER.get();
        // drill bit models 0 (no bit) to 5; the property is 0 with no bit, and animates up to 0.99 while drilling
        List<ItemModel.Unbaked> bits = IntStream.range(0, DRILL_BIT_MODELS)
                .mapToObj(i -> model(jackhammer, RL("item/jackhammer_bit_" + i)))
                .toList();
        output.accept(jackhammer, ItemModelUtils.rangeSelect(new JackhammerDrillBitProperty(), bits.getFirst(),
                IntStream.range(1, DRILL_BIT_MODELS)
                        .mapToObj(i -> ItemModelUtils.override(bits.get(i), i / (float) DRILL_BIT_MODELS))
                        .toList()
        ));
    }

    private ItemModel.Unbaked model(Item item, Identifier model) {
        if (!isTinted(item)) {
            return ItemModelUtils.plainModel(model);
        }
        ItemTintSource[] tints = IntStream.range(0, tintCount(model))
                .mapToObj(ColorHandlers.TintableItemTint::new)
                .toArray(ItemTintSource[]::new);
        return tints.length == 0 ? ItemModelUtils.plainModel(model) : ItemModelUtils.tintedModel(model, tints);
    }

    private static boolean isTinted(Item item) {
        return item instanceof ColorHandlers.ITintableItem
                || item instanceof BlockItem bi && bi.getBlock() instanceof ColorHandlers.ITintableBlock;
    }

    /**
     * {@return the number of tint indices used by the given model} This is the highest {@code tintindex} of the
     * model's elements plus one or, for {@code item/generated} models, the number of {@code layerN} textures.
     */
    private int tintCount(Identifier model) {
        Integer count = tintCounts.get(model);
        if (count == null) {
            count = computeTintCount(model, new ArrayList<>());
            tintCounts.put(model, count);
        }
        return count;
    }

    private int computeTintCount(Identifier model, List<String> layers) {
        return computeTintCount(readModel(model), layers);
    }

    private int computeTintCount(JsonObject json, List<String> layers) {
        if (json.has("textures")) {
            json.getAsJsonObject("textures").keySet().stream()
                    .filter(key -> key.startsWith("layer") && !layers.contains(key))
                    .forEach(layers::add);
        }
        int count = 0;
        if (json.has("children") && json.get("children").isJsonObject()) {
            // neoforge:composite model; each child is an inline model
            JsonObject children = json.getAsJsonObject("children");
            for (String name : children.keySet()) {
                count = Math.max(count, computeTintCount(children.getAsJsonObject(name), new ArrayList<>()));
            }
        }
        if (json.has("elements")) {
            int max = -1;
            for (JsonElement element : json.getAsJsonArray("elements")) {
                JsonObject faces = element.getAsJsonObject().getAsJsonObject("faces");
                if (faces == null) continue;
                for (String dir : faces.keySet()) {
                    JsonObject face = faces.getAsJsonObject(dir);
                    if (face.has("tintindex")) {
                        max = Math.max(max, face.get("tintindex").getAsInt());
                    }
                }
            }
            return Math.max(count, max + 1);
        }
        if (json.has("parent")) {
            Identifier parent = Identifier.parse(json.get("parent").getAsString());
            return Math.max(count, GENERATED_PARENTS.contains(parent) ? layers.size() : computeTintCount(parent, layers));
        }
        return count;
    }

    private JsonObject readModel(Identifier model) {
        Identifier file = model.withPath(path -> "models/" + path + ".json");
        Resource resource = resourceManager.getResource(file)
                .orElseThrow(() -> new IllegalStateException("Missing model " + model + " (" + file + ")"));
        try (Reader reader = resource.openAsReader()) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
