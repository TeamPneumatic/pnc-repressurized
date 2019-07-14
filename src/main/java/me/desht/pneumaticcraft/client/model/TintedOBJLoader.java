package me.desht.pneumaticcraft.client.model;

import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static me.desht.pneumaticcraft.PneumaticCraftRepressurized.LOGGER;

public enum TintedOBJLoader implements ICustomModelLoader {
    INSTANCE;

    private final Set<String> enabledDomains = new HashSet<>();
    private IResourceManager manager;
    private final Map<ResourceLocation, TintedOBJModel> cache = new HashMap<>();
    private final Map<ResourceLocation, Exception> errors = new HashMap<>();

    public void addDomain(String domain)
    {
        enabledDomains.add(domain.toLowerCase());
        LOGGER.info("OBJLoader: Domain {} has been added.", domain.toLowerCase());
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager)
    {
        this.manager = resourceManager;
        cache.clear();
        errors.clear();
    }

    @Override
    public boolean accepts(ResourceLocation modelLocation)
    {
        return enabledDomains.contains(modelLocation.getNamespace()) && modelLocation.getPath().endsWith(".obj");
    }

    @Override
    public IUnbakedModel loadModel(ResourceLocation modelLocation) throws Exception
    {
        ResourceLocation file = new ResourceLocation(modelLocation.getNamespace(), modelLocation.getPath());
        if (!cache.containsKey(file))
        {
            IResource resource;
            try
            {
                resource = manager.getResource(file);
            }
            catch (FileNotFoundException e)
            {
                if (modelLocation.getPath().startsWith("models/block/"))
                    resource = manager.getResource(new ResourceLocation(file.getNamespace(), "models/item/" + file.getPath().substring("models/block/".length())));
                else if (modelLocation.getPath().startsWith("models/item/"))
                    resource = manager.getResource(new ResourceLocation(file.getNamespace(), "models/block/" + file.getPath().substring("models/item/".length())));
                else throw e;
            }
            TintedOBJModel.Parser parser = new TintedOBJModel.Parser(resource, manager);
            TintedOBJModel model = null;
            try
            {
                model = parser.parse();
            }
            catch (Exception e)
            {
                errors.put(modelLocation, e);
            }
            finally
            {
                cache.put(modelLocation, model);
            }
        }
        TintedOBJModel model = cache.get(file);
        if (model == null) throw new ModelLoaderRegistry.LoaderException("Error loading model previously: " + file, errors.get(modelLocation));
        return model;
    }
}
