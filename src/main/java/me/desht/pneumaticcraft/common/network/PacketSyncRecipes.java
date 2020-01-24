package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.api.crafting.PneumaticCraftRecipes;
import me.desht.pneumaticcraft.api.crafting.recipe.*;
import me.desht.pneumaticcraft.common.recipes.ModCraftingHelper;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Received on: CLIENT
 *
 * Sent by server to sync machine recipes to (non-local) clients
 */
public class PacketSyncRecipes {
    private final Map<ResourceLocation, IPressureChamberRecipe> pressureChamberRecipes;
    private final Map<ResourceLocation, IThermopneumaticProcessingPlantRecipe> thermopneumaticProcessingPlantRecipes;
    private final Map<ResourceLocation, IHeatFrameCoolingRecipe> heatFrameCoolingRecipes;
    private final Map<ResourceLocation, IExplosionCraftingRecipe> explosionCraftingRecipes;
    private final Map<ResourceLocation, IRefineryRecipe> refineryRecipes;
    private final Map<ResourceLocation, IAssemblyRecipe> assemblyLaserRecipes;
    private final Map<ResourceLocation, IAssemblyRecipe> assemblyDrillRecipes;
    private final Map<ResourceLocation, IAssemblyRecipe> assemblyLaserDrillRecipes;

    public PacketSyncRecipes(Map<ResourceLocation, IPressureChamberRecipe> pressureChamberRecipes,
                             Map<ResourceLocation, IThermopneumaticProcessingPlantRecipe> thermopneumaticProcessingPlantRecipes,
                             Map<ResourceLocation, IHeatFrameCoolingRecipe> heatFrameCoolingRecipes,
                             Map<ResourceLocation, IExplosionCraftingRecipe> explosionCraftingRecipes,
                             Map<ResourceLocation, IRefineryRecipe> refineryRecipes,
                             Map<ResourceLocation, IAssemblyRecipe> assemblyLaserRecipes,
                             Map<ResourceLocation, IAssemblyRecipe> assemblyDrillRecipes,
                             Map<ResourceLocation, IAssemblyRecipe> assemblyLaserDrillRecipes) {

        this.pressureChamberRecipes = pressureChamberRecipes;
        this.thermopneumaticProcessingPlantRecipes = thermopneumaticProcessingPlantRecipes;
        this.heatFrameCoolingRecipes = heatFrameCoolingRecipes;
        this.explosionCraftingRecipes = explosionCraftingRecipes;
        this.refineryRecipes = refineryRecipes;
        this.assemblyLaserRecipes = assemblyLaserRecipes;
        this.assemblyDrillRecipes = assemblyDrillRecipes;
        this.assemblyLaserDrillRecipes = assemblyLaserDrillRecipes;
    }

    PacketSyncRecipes(PacketBuffer buf) {
        int pressureCount = buf.readVarInt();
        this.pressureChamberRecipes = Stream.generate(() -> (IPressureChamberRecipe) ModCraftingHelper.readRecipe(buf))
                .limit(pressureCount).collect(Collectors.toMap(IPressureChamberRecipe::getId, r -> r));

        int tppCount = buf.readVarInt();
        this.thermopneumaticProcessingPlantRecipes = Stream.generate(() -> (IThermopneumaticProcessingPlantRecipe) ModCraftingHelper.readRecipe(buf))
                .limit(tppCount).collect(Collectors.toMap(IThermopneumaticProcessingPlantRecipe::getId, r -> r));

        int hfcCount = buf.readVarInt();
        this.heatFrameCoolingRecipes = Stream.generate(() -> (IHeatFrameCoolingRecipe) ModCraftingHelper.readRecipe(buf))
                .limit(hfcCount).collect(Collectors.toMap(IHeatFrameCoolingRecipe::getId, r -> r));

        int expCount = buf.readVarInt();
        this.explosionCraftingRecipes = Stream.generate(() -> (IExplosionCraftingRecipe) ModCraftingHelper.readRecipe(buf))
                .limit(expCount).collect(Collectors.toMap(IExplosionCraftingRecipe::getId, r -> r));

        int refineryCount = buf.readVarInt();
        this.refineryRecipes = Stream.generate(() -> (IRefineryRecipe) ModCraftingHelper.readRecipe(buf))
                .limit(refineryCount).collect(Collectors.toMap(IRefineryRecipe::getId, r -> r));

        Supplier<IAssemblyRecipe> supplier = () -> (IAssemblyRecipe) ModCraftingHelper.readRecipe(buf);
        int assLaser = buf.readVarInt();
        this.assemblyLaserRecipes = Stream.generate(supplier)
                .limit(assLaser).collect(Collectors.toMap(IAssemblyRecipe::getId, r -> r));
        int assDrill = buf.readVarInt();
        this.assemblyDrillRecipes = Stream.generate(supplier)
                .limit(assDrill).collect(Collectors.toMap(IAssemblyRecipe::getId, r -> r));
        int assLaserDill = buf.readVarInt();
        this.assemblyLaserDrillRecipes = Stream.generate(supplier)
                .limit(assLaserDill).collect(Collectors.toMap(IAssemblyRecipe::getId, r -> r));

    }

    public void toBytes(PacketBuffer buf) {
        buf.writeVarInt(pressureChamberRecipes.size());
        for (IPressureChamberRecipe recipe : pressureChamberRecipes.values()) {
            ModCraftingHelper.writeRecipe(recipe, buf);
        }
        buf.writeVarInt(thermopneumaticProcessingPlantRecipes.size());
        for (IThermopneumaticProcessingPlantRecipe recipe : thermopneumaticProcessingPlantRecipes.values()) {
            ModCraftingHelper.writeRecipe(recipe, buf);
        }
        buf.writeVarInt(heatFrameCoolingRecipes.size());
        for (IHeatFrameCoolingRecipe recipe : heatFrameCoolingRecipes.values()) {
            ModCraftingHelper.writeRecipe(recipe, buf);
        }
        buf.writeVarInt(explosionCraftingRecipes.size());
        for (IExplosionCraftingRecipe recipe : explosionCraftingRecipes.values()) {
            ModCraftingHelper.writeRecipe(recipe, buf);
        }
        buf.writeVarInt(refineryRecipes.size());
        for (IRefineryRecipe recipe : refineryRecipes.values()) {
            ModCraftingHelper.writeRecipe(recipe, buf);
        }
        buf.writeVarInt(assemblyLaserRecipes.size());
        for (IAssemblyRecipe recipe : assemblyLaserRecipes.values()) {
            ModCraftingHelper.getSerializer(recipe).write(buf, recipe);
        }
        buf.writeVarInt(assemblyDrillRecipes.size());
        for (IAssemblyRecipe recipe : assemblyDrillRecipes.values()) {
            ModCraftingHelper.getSerializer(recipe).write(buf, recipe);
        }
        buf.writeVarInt(assemblyLaserDrillRecipes.size());
        for (IAssemblyRecipe recipe : assemblyLaserDrillRecipes.values()) {
            ModCraftingHelper.writeRecipe(recipe, buf);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PneumaticCraftRecipes.pressureChamberRecipes = pressureChamberRecipes;
            PneumaticCraftRecipes.thermopneumaticProcessingPlantRecipes = thermopneumaticProcessingPlantRecipes;
            PneumaticCraftRecipes.heatFrameCoolingRecipes = heatFrameCoolingRecipes;
            PneumaticCraftRecipes.explosionCraftingRecipes = explosionCraftingRecipes;
            PneumaticCraftRecipes.refineryRecipes = refineryRecipes;
            PneumaticCraftRecipes.assemblyLaserRecipes = assemblyLaserRecipes;
            PneumaticCraftRecipes.assemblyDrillRecipes = assemblyDrillRecipes;
            PneumaticCraftRecipes.assemblyLaserDrillRecipes = assemblyLaserDrillRecipes;
        });
        ctx.get().setPacketHandled(true);
    }
}
