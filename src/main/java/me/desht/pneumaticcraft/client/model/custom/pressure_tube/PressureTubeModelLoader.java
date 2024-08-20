package me.desht.pneumaticcraft.client.model.custom.pressure_tube;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public enum PressureTubeModelLoader implements IGeometryLoader<PressureTubeGeometry> {
    INSTANCE;

    @Override
    public PressureTubeGeometry read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
        String pfx = jsonObject.has("prefix") ? jsonObject.get("prefix").getAsString() + "_" : "";

        BlockModel core = loadModel(RL("block/" + pfx + "pressure_tube_disconnected"));
        BlockModel connected = loadModel(RL("block/" + pfx + "pressure_tube_connected"));
        BlockModel closed = loadModel(RL("block/" + pfx + "pressure_tube_closed"));

        return new PressureTubeGeometry(pfx, core, connected, closed);
    }

    private static BlockModel loadModel(ResourceLocation location) {
        ResourceManager manager = Minecraft.getInstance().getResourceManager();
        ResourceLocation file = ModelBakery.MODEL_LISTER.idToFile(location);
        try (InputStream stream = manager.getResourceOrThrow(file).open()) {
            return BlockModel.fromStream(new InputStreamReader(stream));
        } catch (IOException e) {
            throw new JsonParseException("Failed to load part model '" + file + "'", e);
        }
    }
}
