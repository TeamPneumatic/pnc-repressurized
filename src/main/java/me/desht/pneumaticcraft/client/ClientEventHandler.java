package me.desht.pneumaticcraft.client;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.item.IProgrammable;
import me.desht.pneumaticcraft.client.gui.IGuiDrone;
import me.desht.pneumaticcraft.client.model.pressureglass.PressureGlassBakedModel;
import me.desht.pneumaticcraft.client.render.RenderProgressingLine;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.DateEventHandler;
import me.desht.pneumaticcraft.common.block.BlockPneumaticCraftCamo;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.block.tubes.ModuleRegulatorTube;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.fluid.Fluids;
import me.desht.pneumaticcraft.common.item.ItemMinigun;
import me.desht.pneumaticcraft.common.item.ItemPneumaticSubtyped;
import me.desht.pneumaticcraft.common.item.ItemProgrammingPuzzle;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Names;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.util.*;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class ClientEventHandler {
    public static float playerRenderPartialTick;
    private final RenderProgressingLine minigunFire = new RenderProgressingLine().setProgress(1);
    private final Map<IModel, Class<? extends TubeModule>> awaitingBaking = new HashMap<IModel, Class<? extends TubeModule>>();

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().getItem() instanceof IProgrammable) {
            handleProgrammableTooltip(event);
        } else if (event.getItemStack().getItem() instanceof ItemBucket || event.getItemStack().getItem() instanceof UniversalBucket) {
            handleFluidContainerTooltip(event);
        }
    }

    private void handleProgrammableTooltip(ItemTooltipEvent event) {
        IProgrammable programmable = (IProgrammable) event.getItemStack().getItem();
        if (programmable.canProgram(event.getItemStack()) && programmable.showProgramTooltip()) {
            boolean hasInvalidPrograms = false;
            List<String> addedEntries = new ArrayList<>();
            List<IProgWidget> widgets = TileEntityProgrammer.getProgWidgets(event.getItemStack());
            Map<String, Integer> widgetMap = getPuzzleSummary(widgets);
            for (Map.Entry<String, Integer> entry : widgetMap.entrySet()) {
                IProgWidget widget = ItemProgrammingPuzzle.getWidgetForName(entry.getKey());
                String prefix = "";
                GuiScreen curScreen = Minecraft.getMinecraft().currentScreen;
                if (curScreen instanceof IGuiDrone) {
                    if (!((IGuiDrone) curScreen).getDrone().isProgramApplicable(widget)) {
                        prefix = TextFormatting.RED + "";
                        hasInvalidPrograms = true;
                    }
                }
                addedEntries.add(prefix + "- " + entry.getValue() + "x " + I18n.format("programmingPuzzle." + entry.getKey() + ".name"));
            }
            if (hasInvalidPrograms) {
                event.getToolTip().add(TextFormatting.RED + I18n.format("gui.tooltip.programmable.invalidPieces"));
            }
            Collections.sort(addedEntries);
            event.getToolTip().addAll(addedEntries);
            if (PneumaticCraftRepressurized.proxy.isSneakingInGui() && !widgets.isEmpty()) {
                Map<Integer,Integer> widgetColorMap = TileEntityProgrammer.getPuzzleSummary(widgets);
                event.getToolTip().add(TextFormatting.WHITE + I18n.format("gui.tooltip.programmable.requiredPieces"));
                for (int color : widgetColorMap.keySet()) {
                    ItemStack stack = ItemProgrammingPuzzle.getStackForColor(color);
                    stack.setCount(widgetColorMap.get(color));
                    event.getToolTip().add("- " + widgetColorMap.get(color) + " x " + stack.getDisplayName());
                }
            }
        }
    }

    private void handleFluidContainerTooltip(ItemTooltipEvent event) {
        FluidStack fluidStack = FluidUtil.getFluidContained(event.getItemStack());
        if (fluidStack != null && fluidStack.amount > 0) {
            String key = "gui.tooltip.item." + fluidStack.getFluid().getName() + "_bucket";
            if (I18n.hasKey(key)) {
                if (event.getToolTip().get(event.getToolTip().size() - 1).contains("Minecraft Forge")) {
                    // bit of a kludge!  otherwise the blue "Minecraft Forge" string gets shown twice
                    event.getToolTip().remove(event.getToolTip().size() - 1);
                }
                String prefix = "";
                if (!FluidRegistry.getDefaultFluidName(fluidStack.getFluid()).startsWith(Names.MOD_ID)) {
                    // fluid is owned by another mod; let's make it clear that this tooltip applies to PneumaticCraft
                    prefix = TextFormatting.DARK_AQUA + "" + TextFormatting.ITALIC + "[" + Names.MOD_NAME + "] ";
                }
                if (PneumaticCraftRepressurized.proxy.isSneakingInGui()) {
                    String translatedInfo = TextFormatting.AQUA + I18n.format(key);
                    event.getToolTip().addAll(PneumaticCraftUtils.convertStringIntoList(prefix + translatedInfo, 40));
                } else {
                    event.getToolTip().add(TextFormatting.AQUA + I18n.format("gui.tooltip.sneakForInfo"));
                }
            }
        }
    }

    private static Map<String, Integer> getPuzzleSummary(List<IProgWidget> widgets) {
        Map<String, Integer> map = new HashMap<String, Integer>();
        for (IProgWidget widget : widgets) {
            if (!map.containsKey(widget.getWidgetString())) {
                map.put(widget.getWidgetString(), 1);
            } else {
                map.put(widget.getWidgetString(), map.get(widget.getWidgetString()) + 1);
            }
        }
        return map;
    }

    @SubscribeEvent
    public void onLivingRender(RenderLivingEvent.Pre event) {
        setRenderHead(event.getEntity(), false);
    }

    @SubscribeEvent
    public void onLivingRender(RenderLivingEvent.Post event) {
        setRenderHead(event.getEntity(), true);
    }

    private void setRenderHead(EntityLivingBase entity, boolean setRender) {
        if (entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() == Itemss.PNEUMATIC_HELMET
                && (ConfigHandler.client.useHelmetModel || DateEventHandler.isIronManEvent())) {
            Render renderer = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(entity);
            if (renderer instanceof RenderBiped) {
                ModelBiped modelBiped = (ModelBiped) ((RenderBiped) renderer).getMainModel();
                modelBiped.bipedHead.showModel = setRender;
            }
        }
    }

    /* TODO 1.8 @SubscribeEvent
      public void onPlayerRender(RenderPlayerEvent.Pre event){
          playerRenderPartialTick = event.partialRenderTick;
          if(!Config.useHelmetModel && !DateEventHandler.isIronManEvent() || event.entityPlayer.getItemStackFromSlot(EntityEquipmentSlot.HEAD) == null || event.entityPlayer.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() != Itemss.pneumaticHelmet) return;
          event.renderer.modelBipedMain.bipedHead.showModel = false;
      }

      @SubscribeEvent
      public void onPlayerRender(RenderPlayerEvent.Post event){
          event.renderer.modelBipedMain.bipedHead.showModel = true;
      }*/

    @SubscribeEvent
    public void tickEnd(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.END && FMLClientHandler.instance().getClient().inGameHasFocus && PneumaticCraftRepressurized.proxy.getPlayer().world != null && (ModuleRegulatorTube.inverted || !ModuleRegulatorTube.inLine)) {
            Minecraft mc = FMLClientHandler.instance().getClient();
            ScaledResolution sr = new ScaledResolution(mc);
            FontRenderer fontRenderer = FMLClientHandler.instance().getClient().fontRenderer;
            String warning = TextFormatting.RED + I18n.format("gui.regulatorTube.hudMessage." + (ModuleRegulatorTube.inverted ? "inverted" : "notInLine"));
            fontRenderer.drawStringWithShadow(warning, sr.getScaledWidth() / 2 - fontRenderer.getStringWidth(warning) / 2, sr.getScaledHeight() / 2 + 30, 0xFFFFFFFF);
        }
    }

    private static final double GUN_RADIUS = 1.1D;

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        // render our own minigun bullet traces
        if (Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
            renderMinigunFirstPerson();
        }

        // render everyone else's minigun bullet traces
        EntityPlayer thisPlayer = Minecraft.getMinecraft().player;
        double playerX = thisPlayer.prevPosX + (thisPlayer.posX - thisPlayer.prevPosX) * event.getPartialTicks();
        double playerY = thisPlayer.prevPosY + (thisPlayer.posY - thisPlayer.prevPosY) * event.getPartialTicks();
        double playerZ = thisPlayer.prevPosZ + (thisPlayer.posZ - thisPlayer.prevPosZ) * event.getPartialTicks();
        GlStateManager.pushMatrix();
        GlStateManager.translate(-playerX, -playerY, -playerZ);

        for (EntityPlayer player : Minecraft.getMinecraft().world.playerEntities) {
            if (thisPlayer == player && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) continue;
            ItemStack curItem = player.getHeldItemMainhand();
            if (curItem.getItem() == Itemss.MINIGUN) {
                Minigun minigun = ((ItemMinigun) Itemss.MINIGUN).getMinigun(curItem, player);
                if (minigun.isMinigunActivated() && minigun.getMinigunSpeed() == Minigun.MAX_GUN_SPEED) {
                    GlStateManager.pushMatrix();
                    playerX = player.prevPosX + (player.posX - player.prevPosX) * event.getPartialTicks();
                    playerY = player.prevPosY + (player.posY - player.prevPosY) * event.getPartialTicks();
                    playerZ = player.prevPosZ + (player.posZ - player.prevPosZ) * event.getPartialTicks();
                    GlStateManager.translate(playerX, playerY + 0.5, playerZ);
                    GlStateManager.disableTexture2D();
                    GlStateManager.disableLighting();
                    GlStateManager.enableBlend();
                    GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    RenderUtils.glColorHex(0x40000000 | minigun.getAmmoColor());
                    Vec3d directionVec = player.getLookVec().normalize();
                    Vec3d vec = new Vec3d(directionVec.x, 0, directionVec.z).normalize();
                    vec.rotateYaw((float) Math.toRadians(-15 + (player.rotationYawHead - player.renderYawOffset)));
                    minigunFire.startX = vec.x * GUN_RADIUS;
                    minigunFire.startY = vec.y * GUN_RADIUS - player.getYOffset();
                    minigunFire.startZ = vec.z * GUN_RADIUS;
                    for (int i = 0; i < 5; i++) {
                        minigunFire.endX = directionVec.x * 20 + player.getRNG().nextDouble() - 0.5;
                        minigunFire.endY = directionVec.y * 20 + player.getEyeHeight() + player.getRNG().nextDouble() - 0.5;
                        minigunFire.endZ = directionVec.z * 20 + player.getRNG().nextDouble() - 0.5;
                        minigunFire.render();
                    }
                    GlStateManager.color(1, 1, 1, 1);
                    GlStateManager.enableLighting();
                    GlStateManager.enableTexture2D();
                    GlStateManager.popMatrix();
                }
            }
        }
        GlStateManager.popMatrix();
    }

    private void renderMinigunFirstPerson() {
        EntityPlayer player = Minecraft.getMinecraft().player;
        ItemStack stack = player.getHeldItemMainhand();
        if (stack.getItem() == Itemss.MINIGUN) {
            Minigun minigun = ((ItemMinigun) Itemss.MINIGUN).getMinigun(stack, player);
            if (minigun.isMinigunActivated() && minigun.getMinigunSpeed() == Minigun.MAX_GUN_SPEED) {
                GlStateManager.disableTexture2D();
                GlStateManager.disableLighting();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                RenderUtils.glColorHex(0x40000000 | minigun.getAmmoColor());
                float yawAngle = player.getPrimaryHand() == EnumHandSide.RIGHT ? -(float)Math.PI / 2f : (float)Math.PI / 2f;
                Vec3d directionVec = player.getLookVec().normalize();
                Vec3d vec2 = new Vec3d(directionVec.x, directionVec.y, directionVec.z);
                vec2 = vec2.rotateYaw(yawAngle);
                minigunFire.startX = vec2.x ;
                minigunFire.startY = 1.0;
                minigunFire.startZ = vec2.z;
                for (int i = 0; i < 5; i++) {
                    minigunFire.endX = directionVec.x * 20 + player.getRNG().nextDouble() - 0.5;
                    minigunFire.endY = directionVec.y * 20 + player.getEyeHeight() + player.getRNG().nextDouble() - 0.5;
                    minigunFire.endZ = directionVec.z * 20 + player.getRNG().nextDouble() - 0.5;
                    minigunFire.render();
                }
                GlStateManager.color(1, 1, 1, 1);
                GlStateManager.enableLighting();
                GlStateManager.enableTexture2D();
                GlStateManager.disableBlend();
            }
        }
    }

    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        // pressure glass connected textures
        for (int i = 0; i < PressureGlassBakedModel.TEXTURE_COUNT; i++) {
            ResourceLocation loc = new ResourceLocation(Textures.PRESSURE_GLASS_LOCATION + "window_" + (i + 1));
            PressureGlassBakedModel.SPRITES[i] = event.getMap().registerSprite(loc);
        }
    }

    @SubscribeEvent
    public void onModelBaking(ModelBakeEvent event) {
        // set up camo models for camouflagable blocks
        for (Block block : Blockss.blocks) {
            if (block instanceof BlockPneumaticCraftCamo) {
                Map<IBlockState,ModelResourceLocation> map
                        = event.getModelManager().getBlockModelShapes().getBlockStateMapper().getVariants(block);
                for (Map.Entry<IBlockState,ModelResourceLocation> entry : map.entrySet()) {
                    Object object = event.getModelRegistry().getObject(entry.getValue());
                    if (object != null) {
                        IBakedModel existing = (IBakedModel) object;
                        CamoModel customModel = new CamoModel(existing);
                        event.getModelRegistry().putObject(entry.getValue(), customModel);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void registerModels(ModelRegistryEvent event) {
        registerFluidModels();

        for (Block block : Blockss.blocks) {
            Item item = Item.getItemFromBlock(block);
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }

        Item assemblyIO = Item.getItemFromBlock(Blockss.ASSEMBLY_IO_UNIT);
        ModelLoader.setCustomModelResourceLocation(assemblyIO, 1, new ModelResourceLocation(RL("assembly_io_unit_import"), "inventory"));

        for (Item item: Itemss.items) {
            if (item instanceof ItemPneumaticSubtyped) {
                ModelBakery.registerItemVariants(item);
                ItemPneumaticSubtyped subtyped = (ItemPneumaticSubtyped) item;
                NonNullList<ItemStack> stacks = NonNullList.create();
                item.getSubItems(PneumaticCraftRepressurized.tabPneumaticCraft, stacks);
                for (ItemStack stack : stacks) {
                    ModelLoader.setCustomModelResourceLocation(item, stack.getMetadata(),
                            new ModelResourceLocation(RL(subtyped.getSubtypeModelName(stack.getMetadata())), "inventory"));
                }
            } else {
                ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
            }
        }

        ModelLoader.setCustomStateMapper(Blockss.DRONE_REDSTONE_EMITTER, blockIn -> Collections.emptyMap());
        ModelLoader.setCustomStateMapper(Blockss.KEROSENE_LAMP_LIGHT, blockIn -> Collections.emptyMap());
        ModelLoader.setCustomStateMapper(Blockss.PRESSURE_CHAMBER_GLASS, new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState iBlockState) {
                return PressureGlassBakedModel.BAKED_MODEL;
            }
        });
    }

    private void registerFluidModels() {
        for (IFluidBlock fluidBlock : Fluids.MOD_FLUID_BLOCKS) {
            final Item item = Item.getItemFromBlock((Block) fluidBlock);
            assert item != null;

            ModelBakery.registerItemVariants(item);

            FluidStateMapper stateMapper = new FluidStateMapper(fluidBlock.getFluid());
            ModelLoader.setCustomMeshDefinition(item, stateMapper);
            ModelLoader.setCustomStateMapper((Block) fluidBlock, stateMapper);
        }
    }
}
