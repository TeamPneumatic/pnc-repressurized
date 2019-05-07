package me.desht.pneumaticcraft.client;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.client.IFOVModifierItem;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.api.item.IProgrammable;
import me.desht.pneumaticcraft.client.gui.GuiUtils;
import me.desht.pneumaticcraft.client.gui.IExtraGuiHandling;
import me.desht.pneumaticcraft.client.gui.IGuiDrone;
import me.desht.pneumaticcraft.client.model.pressureglass.PressureGlassBakedModel;
import me.desht.pneumaticcraft.client.particle.AirParticle;
import me.desht.pneumaticcraft.client.render.RenderProgressingLine;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.CommonHUDHandler;
import me.desht.pneumaticcraft.common.block.BlockPneumaticCraftCamo;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.block.tubes.ModuleRegulatorTube;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.event.DateEventHandler;
import me.desht.pneumaticcraft.common.fluid.Fluids;
import me.desht.pneumaticcraft.common.item.*;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketJetBootState;
import me.desht.pneumaticcraft.common.network.PacketMarkPlayerJetbootsActive;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Names;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Quaternion;

import java.util.*;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class ClientEventHandler {
    private static final double MINIGUN_RADIUS = 1.1D;
    private static final double MINIGUN_TEXT_SIZE = 0.55D;
    private static final float MAX_SCREEN_ROLL = 25F;  // max roll in degrees when flying with jetboots

    private float currentScreenRoll = 0F;

    public static float playerRenderPartialTick;
    private final RenderProgressingLine minigunFire = new RenderProgressingLine().setProgress(1);

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
                        prefix = TextFormatting.RED + TextFormatting.ITALIC.toString() + "";
                        hasInvalidPrograms = true;
                    }
                }
                addedEntries.add(prefix + "\u2022 " + entry.getValue() + "x " + I18n.format("programmingPuzzle." + entry.getKey() + ".name"));
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
        Map<String, Integer> map = new HashMap<>();
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
        if (event.phase == TickEvent.Phase.END && FMLClientHandler.instance().getClient().inGameHasFocus && PneumaticCraftRepressurized.proxy.getClientPlayer().world != null && (ModuleRegulatorTube.inverted || !ModuleRegulatorTube.inLine)) {
            Minecraft mc = FMLClientHandler.instance().getClient();
            ScaledResolution sr = new ScaledResolution(mc);
            FontRenderer fontRenderer = FMLClientHandler.instance().getClient().fontRenderer;
            String warning = TextFormatting.RED + I18n.format("gui.regulatorTube.hudMessage." + (ModuleRegulatorTube.inverted ? "inverted" : "notInLine"));
            fontRenderer.drawStringWithShadow(warning, sr.getScaledWidth() / 2f - fontRenderer.getStringWidth(warning) / 2f, sr.getScaledHeight() / 2f + 30, 0xFFFFFFFF);
        }
    }

    @SubscribeEvent
    public void renderFirstPersonMinigun(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.CROSSHAIRS && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
            EntityPlayer player = Minecraft.getMinecraft().player;
            Minecraft mc = Minecraft.getMinecraft();
            ItemStack stack = player.getHeldItemMainhand();
            if (stack.getItem() instanceof ItemMinigun){
                Minigun minigun = ((ItemMinigun) stack.getItem()).getMinigun(stack, player);
                int w = event.getResolution().getScaledWidth();
                int h = event.getResolution().getScaledHeight();

                if (minigun.isMinigunActivated() && minigun.getMinigunSpeed() == Minigun.MAX_GUN_SPEED) {
                     drawBulletTraces2D(minigun.getAmmoColor() | 0x40000000, w, h);
                }

                ItemStack ammo = minigun.getAmmoStack();
                if (!ammo.isEmpty()) {
                    GuiUtils.drawItemStack(ammo,w / 2 + 16, h / 2 - 7);
                    int remaining = ammo.getMaxDamage() - ammo.getItemDamage();
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(w / 2f + 32, h / 2f - 1, 0);
                    GlStateManager.scale(MINIGUN_TEXT_SIZE, MINIGUN_TEXT_SIZE, 1.0);
                    String text = remaining + "/" + ammo.getMaxDamage();
                    mc.fontRenderer.drawString(text, 1, 0, 0);
                    mc.fontRenderer.drawString(text, -1, 0, 0);
                    mc.fontRenderer.drawString(text, 0, 1, 0);
                    mc.fontRenderer.drawString(text, 0, -1, 0);
                    mc.fontRenderer.drawString(text, 0, 0, minigun.getAmmoColor());
                    GlStateManager.popMatrix();
                }
                mc.getTextureManager().bindTexture(Textures.GUI_MINIGUN_CROSSHAIR);
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GlStateManager.color(0.2f, 1.0f, 0.2f, 0.6f);
                Gui.drawModalRectWithCustomSizedTexture(w / 2 - 7, h / 2 - 7, 0, 0, 16, 16, 16, 16);
                event.setCanceled(true);
            }
        }
    }

    private void drawBulletTraces2D(int color, int w, int h) {
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_STIPPLE);
        RenderUtils.glColorHex(color);

        int x = w / 2;
        int y = h / 2;

        Random rand = Minecraft.getMinecraft().world.rand;
        for (int i = 0; i < 5; i++) {
            int stipple = 0xFFFF & ~(3 << rand.nextInt(16));
            GL11.glLineStipple(4, (short) stipple);
            GlStateManager.glBegin(GL11.GL_LINES);
            GL11.glVertex2f(x + rand.nextInt(12) - 6, y + rand.nextInt(12) - 6);
            float f = Minecraft.getMinecraft().gameSettings.mainHand == EnumHandSide.RIGHT ? 0.665F : 0.335F;
            GL11.glVertex2f(w * f, h * 0.685F);
            GlStateManager.glEnd();
        }
        GlStateManager.color(1, 1, 1, 1);
        GL11.glDisable(GL11.GL_LINE_STIPPLE);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        // render everyone else's (and ours in 3rd person camera) minigun bullet traces
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
                    GlStateManager.enableBlend();
                    GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    RenderUtils.glColorHex(0x40000000 | minigun.getAmmoColor());
                    Vec3d directionVec = player.getLookVec().normalize();
                    Vec3d vec = new Vec3d(directionVec.x, 0, directionVec.z).normalize();
                    vec.rotateYaw((float) Math.toRadians(-15 + (player.rotationYawHead - player.renderYawOffset)));
                    minigunFire.startX = vec.x * MINIGUN_RADIUS;
                    minigunFire.startY = vec.y * MINIGUN_RADIUS - player.getYOffset();
                    minigunFire.startZ = vec.z * MINIGUN_RADIUS;
                    for (int i = 0; i < 5; i++) {
                        minigunFire.endX = directionVec.x * 20 + player.getRNG().nextDouble() - 0.5;
                        minigunFire.endY = directionVec.y * 20 + player.getEyeHeight() + player.getRNG().nextDouble() - 0.5;
                        minigunFire.endZ = directionVec.z * 20 + player.getRNG().nextDouble() - 0.5;
                        minigunFire.render();
                    }
                    GlStateManager.color(1, 1, 1, 1);
                    GlStateManager.enableTexture2D();
                    GlStateManager.popMatrix();
                }
            }
        }
        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        // pressure glass connected textures
        for (int i = 0; i < PressureGlassBakedModel.TEXTURE_COUNT; i++) {
            ResourceLocation loc = new ResourceLocation(Textures.PRESSURE_GLASS_LOCATION + "window_" + (i + 1));
            PressureGlassBakedModel.SPRITES[i] = event.getMap().registerSprite(loc);
        }

        // air particles
        event.getMap().registerSprite(AirParticle.AIR_PARTICLE_TEXTURE);
        event.getMap().registerSprite(AirParticle.AIR_PARTICLE_TEXTURE2);
    }

    @SubscribeEvent
    public void onModelBaking(ModelBakeEvent event) {
        // set up camo models for camouflageable blocks
        for (Block block : Blockss.blocks) {
            if (block instanceof BlockPneumaticCraftCamo) {
                Map<IBlockState,ModelResourceLocation> map
                        = event.getModelManager().getBlockModelShapes().getBlockStateMapper().getVariants(block);
                for (Map.Entry<IBlockState,ModelResourceLocation> entry : map.entrySet()) {
                    IBakedModel model = event.getModelRegistry().getObject(entry.getValue());
                    if (model != null) {
                        CamoModel customModel = new CamoModel(model);
                        event.getModelRegistry().putObject(entry.getValue(), customModel);
                    }
                }
            }
        }

        // minigun model: using TEISR for in-hand transforms
        ModelResourceLocation mrl = new ModelResourceLocation(Itemss.MINIGUN.getRegistryName(), "inventory");
        IBakedModel object = event.getModelRegistry().getObject(mrl);
        if (object != null) {
            event.getModelRegistry().putObject(mrl, new BakedMinigunWrapper(object));
        }
    }


    @SubscribeEvent
    public void screenTilt(EntityViewRenderEvent.CameraSetup event) {
        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntity();
            if (player.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem() instanceof ItemPneumaticArmor && !player.onGround) {
                CommonHUDHandler handler = CommonHUDHandler.getHandlerForPlayer(player);
                float targetRoll;
                float div = 50F;
                if (handler.isJetBootsActive()) {
                    float roll = player.rotationYawHead - player.prevRotationYawHead;
                    if (Math.abs(roll) < 0.0001) {
                        targetRoll = 0F;
                    } else {
                        targetRoll = Math.signum(roll) * MAX_SCREEN_ROLL;
                        div = Math.abs(400F / roll);
                    }
                } else {
                    targetRoll = 0F;
                }
                currentScreenRoll += (targetRoll - currentScreenRoll) / div;
                event.setRoll(currentScreenRoll);
            } else {
                currentScreenRoll = 0F;
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

    @SubscribeEvent
    public void jetBootsEvent(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            EntityPlayer player = FMLClientHandler.instance().getClientPlayerEntity();
            if (player == null || player.world == null) return;
            CommonHUDHandler handler = CommonHUDHandler.getHandlerForPlayer(player);
            GameSettings settings = FMLClientHandler.instance().getClient().gameSettings;
            if (handler.isJetBootsActive() && (!handler.isJetBootsEnabled() || !settings.keyBindJump.isKeyDown())) {
                NetworkHandler.sendToServer(new PacketJetBootState(false));
                handler.setJetBootsActive(false, player);
            } else if (!handler.isJetBootsActive() && handler.isJetBootsEnabled() && settings.keyBindJump.isKeyDown()) {
                NetworkHandler.sendToServer(new PacketJetBootState(true));
                handler.setJetBootsActive(true, player);
            }
        }
    }

    @SubscribeEvent
    public void playerPreRotateEvent(RenderPlayerEvent.Pre event) {
        EntityPlayer player = event.getEntityPlayer();
        if (PacketMarkPlayerJetbootsActive.shouldPlayerBeRotated(player)) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(event.getX(), event.getY(), event.getZ());
            GlStateManager.rotate(makeQuaternion(player));
            GlStateManager.translate(-event.getX(), -event.getY(), -event.getZ());
            player.limbSwingAmount = player.prevLimbSwingAmount = 0F;
        }
    }

    @SubscribeEvent
    public void playerPostRotateEvent(RenderPlayerEvent.Post event) {
        if (PacketMarkPlayerJetbootsActive.shouldPlayerBeRotated(event.getEntityPlayer())) {
            GlStateManager.popMatrix();
        }
    }

    private static Quaternion makeQuaternion(EntityPlayer player) {
        Vec3d forward = player.getLookVec().normalize();

        double dot = new Vec3d(0, 1, 0).dotProduct(forward);
        if (Math.abs(dot + 1) < 0.000001) {
            return new Quaternion(0F, 1F, 0F, (float)Math.PI);
        }
        if (Math.abs (dot - 1) < 0.000001) {
            return new Quaternion(); //identity
        }

        Vec3d rotAxis = new Vec3d(0, 1, 0).crossProduct(forward).normalize();

        double a2 = Math.acos(dot) * .5f;
        float s = (float) Math.sin(a2);
        return new Quaternion((float) rotAxis.x * s, (float) rotAxis.y * s, (float) rotAxis.z * s, (float) Math.cos(a2));
    }

    @SubscribeEvent
    public void adjustFOVEvent(FOVUpdateEvent event) {
        CommonHUDHandler handler = CommonHUDHandler.getHandlerForPlayer();

        float modifier = 1.0f;
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            ItemStack stack = event.getEntity().getItemStackFromSlot(slot);
            if (stack.getItem() instanceof IFOVModifierItem) {
                modifier *= ((IFOVModifierItem) stack.getItem()).getFOVModifier(stack, event.getEntity(), slot);
            }
        }

        event.setNewfov(event.getNewfov() * modifier);
    }

    @SubscribeEvent
    public void fogDensityEvent(EntityViewRenderEvent.FogDensity event) {
        if (event.getState().getMaterial() == Material.WATER && event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntity();
            CommonHUDHandler handler = CommonHUDHandler.getHandlerForPlayer();
            if (handler.isArmorReady(EntityEquipmentSlot.HEAD) && handler.isScubaEnabled() && handler.getUpgradeCount(EntityEquipmentSlot.HEAD, IItemRegistry.EnumUpgrade.SCUBA) > 0) {
                event.setDensity(0.02f);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void guiContainerForeground(GuiContainerEvent.DrawForeground event) {
        if (Minecraft.getMinecraft().currentScreen instanceof IExtraGuiHandling) {
            ((IExtraGuiHandling) Minecraft.getMinecraft().currentScreen).drawExtras(event);
        }
    }

    @SubscribeEvent
    public void renderTooltipEvent(RenderTooltipEvent.PostText event) {
        ItemStack stack = event.getStack();
        if (stack.getItem() instanceof ItemMicromissiles && stack.hasTagCompound()) {
            int width = 0;
            FontRenderer fr = event.getFontRenderer();
            int y = event.getY() + fr.FONT_HEIGHT * 2 + 5;
            width = Math.max(width, renderString(fr, (I18n.format("gui.micromissile.topSpeed")), event.getX(), y));
            width = Math.max(width, renderString(fr, (I18n.format("gui.micromissile.turnSpeed")), event.getX(), y + fr.FONT_HEIGHT));
            width = Math.max(width, renderString(fr, (I18n.format("gui.micromissile.damage")), event.getX(), y + fr.FONT_HEIGHT * 2));
            int barX = event.getX() + width + 2;
            int barW = event.getWidth() - width - 10;
            GlStateManager.disableTexture2D();
            GlStateManager.glLineWidth(10);
            GL11.glEnable(GL11.GL_LINE_STIPPLE);
            GL11.glLineStipple(1, (short)0xFEFE);
            RenderUtils.glColorHex(0x00C000, 255);
            GlStateManager.glBegin(GL11.GL_LINES);
            GL11.glVertex2i(barX, y + 4);
            GL11.glVertex2i(barX + (int) (barW * stack.getTagCompound().getFloat(ItemMicromissiles.NBT_TOP_SPEED)), y + 4);
            GlStateManager.glEnd();
            GlStateManager.glBegin(GL11.GL_LINES);
            GL11.glVertex2i(barX, y + 4 + fr.FONT_HEIGHT);
            GL11.glVertex2i(barX + (int) (barW * stack.getTagCompound().getFloat(ItemMicromissiles.NBT_TURN_SPEED)), y + 4 + fr.FONT_HEIGHT);
            GlStateManager.glEnd();
            GlStateManager.glBegin(GL11.GL_LINES);
            GL11.glVertex2i(barX, y + 4 + fr.FONT_HEIGHT * 2);
            GL11.glVertex2i(barX + (int) (barW * stack.getTagCompound().getFloat(ItemMicromissiles.NBT_DAMAGE)), y + 4 + fr.FONT_HEIGHT * 2);
            GlStateManager.glEnd();
            GlStateManager.glLineWidth(1);
            GL11.glDisable(GL11.GL_LINE_STIPPLE);
        }
    }

    private int renderString(FontRenderer fr, String s, int x, int y) {
        fr.drawStringWithShadow(s, x, y, 0xFFAAAAAA);
        return fr.getStringWidth(s);
    }

    @SubscribeEvent
    public void drawScreen(GuiScreenEvent.DrawScreenEvent.Pre event) {
        // with thanks to V0idWa1k3r
        // https://github.com/V0idWa1k3r/ExPetrum/blob/master/src/main/java/v0id/exp/client/ExPHandlerClient.java#L235
        if (event.getGui() instanceof GuiContainer) {
            GlStateManager.disableTexture2D();
            GlStateManager.color(1F, 1F, 1F, 1F);
            BufferBuilder bb = Tessellator.getInstance().getBuffer();
            GuiContainer container = (GuiContainer) event.getGui();
            int i = container.getGuiLeft();
            int j = container.getGuiTop();
            bb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            for (Slot s : container.inventorySlots.inventorySlots) {
                if (!s.getStack().isEmpty()) {
                    float x = s.xPos;
                    float y = s.yPos;
                    if (s.getStack().getItem() instanceof ItemPneumaticArmor && ItemPressurizable.shouldShowPressureDurability(s.getStack())) {
                        // render secondary durability bar showing remaining air
                        ItemPneumaticArmor a = (ItemPneumaticArmor) s.getStack().getItem();
                        float val = a.getPressure(s.getStack()) / a.maxPressure(s.getStack());
                        int c = ItemPressurizable.getDurabilityColor(s.getStack());
                        float r = ((c & 0xFF0000) >> 16) / 256f;
                        float g = ((c & 0xFF00) >> 8) / 256f;
                        float b = ((c & 0xFF)) / 256f;
                        int yOff = s.getStack().getItemDamage() > 0 ? 0 : 1;

                        if (yOff == 1) {
                            bb.pos(i + x + 2, j + y + 15, 1).color(0.2F, 0.2F, 0.2F, 1F).endVertex();
                            bb.pos(i + x + 15, j + y + 15, 1).color(0.2F, 0.2F, 0.2F, 1F).endVertex();
                            bb.pos(i + x + 15, j + y + 14, 1).color(0.2F, 0.2F, 0.2F, 1F).endVertex();
                            bb.pos(i + x + 2, j + y + 14, 1).color(0.2F, 0.2F, 0.2F, 1F).endVertex();
                        }
                        bb.pos(i + x + 2, j + y + 13 + yOff, 300).color(r, g, b, 1F).endVertex();
                        bb.pos(i + x + 2 + 13 * val, j + y + 13 + yOff, 300).color(r, g, b, 1F).endVertex();
                        bb.pos(i + x + 2 + 13 * val, j + y + 12 + yOff, 300).color(r, g, b, 1F).endVertex();
                        bb.pos(i + x + 2, j + y + 12 + yOff, 300).color(r, g, b, 1F).endVertex();
                    }
                }
            }
            Tessellator.getInstance().draw();
            GlStateManager.enableTexture2D();
        }
    }
}

