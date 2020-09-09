package me.desht.pneumaticcraft.common.item;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.client.sound.MovingSounds;
import me.desht.pneumaticcraft.common.PneumaticCraftTags;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.inventory.ContainerJackhammerSetup;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.item.ItemDrillBit.DrillBitType;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlayMovingSound;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import me.desht.pneumaticcraft.common.util.NBTUtils;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.common.util.upgrade.ApplicableUpgradesDB;
import me.desht.pneumaticcraft.lib.Names;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static net.minecraft.util.Direction.Axis.Y;

public class ItemJackHammer extends ItemPressurizable implements IChargeableContainerProvider, IUpgradeAcceptor, ColorHandlers.ITintableItem {
    private static final float[] SPEED_MULT = new float[] {
            1f,
            2f,
            2.41421356237309f,
            2.73205080756888f,
            3f,
            3.23606797749979f,
            3.44948974278318f,
            3.64575131106459f,
            3.82842712474619f,
            4f,
            4.16227766016838f
    };
    private static final String NBT_DIG_MODE = "DigMode";

    public ItemJackHammer() {
        super(PneumaticValues.VOLUME_JACKHAMMER * 10, PneumaticValues.VOLUME_JACKHAMMER);
    }

    public static DrillBitHandler getDrillBitHandler(ItemStack stack) {
        if (stack.getItem() instanceof ItemJackHammer) {
            return new DrillBitHandler(stack);
        }
        return null;
    }

    public static EnchantmentHandler getEnchantmentHandler(ItemStack stack) {
        if (stack.getItem() instanceof ItemJackHammer) {
            return new EnchantmentHandler(stack);
        }
        return null;
    }

    public DrillBitType getDrillBit(ItemStack stack) {
        DrillBitHandler handler = new DrillBitHandler(stack);
        ItemStack bitStack = handler.getStackInSlot(0);
        if (bitStack.getItem() instanceof ItemDrillBit) {
            return ((ItemDrillBit) bitStack.getItem()).getType();
        }
        return DrillBitType.NONE;
    }

    @Override
    public boolean canHarvestBlock(ItemStack stack, BlockState blockIn) {
        DrillBitType drillBitType = getDrillBit(stack);
        return drillBitType.getHarvestLevel() >= blockIn.getHarvestLevel();
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        DrillBitType bitType = getDrillBit(stack);
        int speed = bitType == DrillBitType.NONE ? 0 : UpgradableItemUtils.getUpgrades(stack, EnumUpgrade.SPEED);
        return getAir(stack) > 0f ? bitType.getBaseEfficiency() * SPEED_MULT[speed] : 1;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        if (!playerIn.isCrouching()) return ActionResult.resultPass(stack);
        if (!worldIn.isRemote) {
            NetworkHooks.openGui((ServerPlayerEntity) playerIn, new INamedContainerProvider() {
                @Override
                public ITextComponent getDisplayName() {
                    return stack.getDisplayName();
                }

                @Nullable
                @Override
                public Container createMenu(int windowId, PlayerInventory inv, PlayerEntity player) {
                    return new ContainerJackhammerSetup(windowId, inv, handIn);
                }
            }, buf -> buf.writeBoolean(handIn == Hand.MAIN_HAND));
        }
        return ActionResult.resultSuccess(stack);
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {
        if (entityLiving instanceof PlayerEntity && ((PlayerEntity) entityLiving).isCreative()) return true;
        int speed = UpgradableItemUtils.getUpgrades(stack, EnumUpgrade.SPEED);
        stack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).orElseThrow(RuntimeException::new)
                .addAir(-PneumaticValues.USAGE_JACKHAMMER * speed);
        return true;
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        return getDrillBit(stack) != DrillBitType.NONE && getAir(stack) > 0f;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, PlayerEntity player) {
        if (!player.getEntityWorld().isRemote && !player.isCrouching()) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            World world = serverPlayer.getEntityWorld();

            RayTraceResult brtr = PneumaticCraftUtils.getEntityLookedObject(player);
            if (brtr instanceof BlockRayTraceResult) {
                itemstack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).ifPresent(airHandler -> {
                    List<Integer> upgrades = UpgradableItemUtils.getUpgradeList(itemstack, EnumUpgrade.SPEED, EnumUpgrade.RANGE, EnumUpgrade.MAGNET);
                    int speed = upgrades.get(0);
                    DigMode digMode = ItemJackHammer.getDigMode(itemstack);
                    DrillBitType bitType = getDrillBit(itemstack);
                    if (digMode.getBitType().getTier() > bitType.getTier()) {
                        // sanity check
                        digMode = DigMode.MODE_1X1;
                    }
                    Set<BlockPos> brokenPos = getBreakPositions(world, pos, ((BlockRayTraceResult) brtr).getFace(), player.getHorizontalFacing(), digMode);
                    brokenPos.remove(pos); // start pos already broken

                    float air = airHandler.getAir();
                    float air0 = air;
                    float usage = PneumaticValues.USAGE_JACKHAMMER * SPEED_MULT[speed];
                    for (BlockPos pos1 : brokenPos) {
                        BlockState state1 = world.getBlockState(pos1);
                        if (state1.getBlockHardness(world, pos1) < 0) continue;

                        int exp = ForgeHooks.onBlockBreakEvent(serverPlayer.world, serverPlayer.interactionManager.getGameType(), serverPlayer, pos1);
                        if (exp == -1) {
                            continue;
                        }
                        if (world.getTileEntity(pos1) != null) {
                            continue;
                        }
                        Block block = state1.getBlock();
                        boolean removed = state1.removedByPlayer(world, pos1, player, true, world.getFluidState(pos1));
                        if (removed) {
                            block.onPlayerDestroy(world, pos1, state1);
                            block.harvestBlock(world, player, pos1, state1, null, itemstack);
                            if (exp > 0 && world instanceof ServerWorld) {
                                block.dropXpOnBlockBreak((ServerWorld) world, pos1, exp);
                            }
                            if (!player.isCreative()) {
                                air -= usage;
                            }
                            player.addStat(Stats.ITEM_USED.get(this));
                        }
                        if (air < usage) break;
                    }
                    if (air != air0) {
                        airHandler.addAir((int)(air - air0));
                    }
                });
            }
        }
        return super.onBlockStartBreak(itemstack, pos, player);
    }

    private Set<BlockPos> getBreakPositions(World world, BlockPos pos, Direction dir, Direction playerHoriz, DigMode digMode) {
        if (digMode.isVeinMining()) {
            return new HashSet<>(getVeinPositions(world, pos, digMode));
        }

        Set<BlockPos> res = new HashSet<>();
        if (digMode.atLeast(DigMode.MODE_1X2)) {
            res.add(dir.getAxis() == Y ? pos.offset(playerHoriz) : pos.down());
        }
        if (digMode.atLeast(DigMode.MODE_1X3)) {
            res.add(dir.getAxis() == Y ? pos.offset(playerHoriz.getOpposite()) : pos.up());
        }
        if (digMode.atLeast(DigMode.MODE_3X3_CROSS)) {
            switch (dir.getAxis()) {
                case X:
                    res.add(pos.north());
                    res.add(pos.south());
                    res.add(pos.up());
                    res.add(pos.down());
                    break;
                case Y:
                    res.add(pos.north());
                    res.add(pos.south());
                    res.add(pos.west());
                    res.add(pos.east());
                    break;
                case Z:
                    res.add(pos.up());
                    res.add(pos.down());
                    res.add(pos.west());
                    res.add(pos.east());
                    break;
            }
        }
        if (digMode.atLeast(DigMode.MODE_3X3_FULL)) {
            switch (dir.getAxis()) {
                case X:
                    res.add(pos.up().north());
                    res.add(pos.up().south());
                    res.add(pos.down().north());
                    res.add(pos.down().south());
                    break;
                case Y:
                    res.add(pos.north().east());
                    res.add(pos.north().west());
                    res.add(pos.south().east());
                    res.add(pos.south().west());
                    break;
                case Z:
                    res.add(pos.up().east());
                    res.add(pos.up().west());
                    res.add(pos.down().east());
                    res.add(pos.down().west());
                    break;
            }
        }
        return res;
    }

    private List<BlockPos> getVeinPositions(World world, BlockPos startPos, DigMode mode) {
        BlockState state = world.getBlockState(startPos);

        if (!mode.okToVeinMine(state)) {
            return Collections.emptyList();
        }

        int maxRange = PNCConfig.Common.Jackhammer.maxVeinMinerRange;
        int maxRangeSq = maxRange * maxRange;

        List<BlockPos> found = new ArrayList<>();
        found.add(startPos);
        Set<BlockPos> checked = new ObjectOpenHashSet<>();

        Block startBlock = state.getBlock();
        int maxBlocks = mode.getBlocksDug();

        // thanks due to Mekanism for this useful & efficient blockfinder algorithm which I shamelessly cribbed ;)
        for (int i = 0; i < found.size(); i++) {
            BlockPos blockPos = found.get(i);
            for (BlockPos pos : BlockPos.getAllInBoxMutable(blockPos.add(-1, -1, -1), blockPos.add(1, 1, 1))) {
                if (!checked.contains(pos)) {
                    if (mode == DigMode.MODE_VEIN_PLUS && startPos.distanceSq(pos) > maxRangeSq) {
                        continue;
                    }
                    if (world.isBlockPresent(pos) && startBlock == world.getBlockState(pos).getBlock()) {
                        BlockPos pos1 = pos.toImmutable();
                        found.add(pos1);
                        checked.add(pos1);
                        if (found.size() > maxBlocks) {
                            return found;
                        }
                    }
                }
            }
        }
        return found;
    }

    @Override
    public Map<EnumUpgrade, Integer> getApplicableUpgrades() {
        return ApplicableUpgradesDB.getInstance().getApplicableUpgrades(this);
    }

    @Override
    public String getUpgradeAcceptorTranslationKey() {
        return getTranslationKey();
    }

    @Override
    public INamedContainerProvider getContainerProvider(TileEntityChargingStation te) {
        return new IChargeableContainerProvider.Provider(te, ModContainers.CHARGING_JACKHAMMER.get());
    }

    @Override
    public int getTintColor(ItemStack stack, int tintIndex) {
        if (tintIndex == 0) {
            return getDrillBit(stack).getTint();
        }
        return 0xFFFFFFFF;
    }

    public static DigMode getDigMode(ItemStack stack) {
        if (stack.getItem() instanceof ItemJackHammer && stack.hasTag()) {
            try {
                return stack.getTag().contains(NBT_DIG_MODE) ? DigMode.valueOf(stack.getTag().getString(NBT_DIG_MODE)): DigMode.MODE_1X1;
            } catch (IllegalArgumentException ignored) {
            }
        }
        return DigMode.MODE_1X1;
    }

    public static void setDigMode(ItemStack stack, DigMode mode) {
        stack.getOrCreateTag().putString(NBT_DIG_MODE, mode.toString());
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        // not enchantable by normal means; only by adding a silk touch or fortune book via GUI
        return false;
    }

    public enum DigMode {
        MODE_1X1("1x1", 1, DrillBitType.IRON),
        MODE_1X2("1x2", 2, DrillBitType.COMPRESSED_IRON),
        MODE_1X3("1x3", 3, DrillBitType.COMPRESSED_IRON),
        MODE_3X3_CROSS("3x3_cross", 5, DrillBitType.DIAMOND),
        MODE_VEIN("vein", 128, DrillBitType.DIAMOND),
        MODE_3X3_FULL("3x3_full", 9, DrillBitType.NETHERITE),
        MODE_VEIN_PLUS("vein_plus", 128, DrillBitType.NETHERITE);

        private final String name;
        private final int blocksDug;
        private final DrillBitType bitType;

        DigMode(String name, int blocksDug, DrillBitType bitType) {
            this.name = name;
            this.blocksDug = blocksDug;
            this.bitType = bitType;
        }

        public String getName() {
            return name;
        }

        public DrillBitType getBitType() {
            return bitType;
        }

        public int getBlocksDug() {
            return blocksDug;
        }

        public ResourceLocation getGuiIcon() {
            return Textures.guiIconTexture("gui_" + name + ".png");
        }

        public boolean atLeast(DigMode type) {
            return type.ordinal() <= this.ordinal();
        }

        public boolean isVeinMining() { return this == DigMode.MODE_VEIN || this == DigMode.MODE_VEIN_PLUS; }

        public boolean okToVeinMine(BlockState state) {
            switch (this) {
                case MODE_VEIN: return state.isIn(PneumaticCraftTags.Blocks.JACKHAMMER_ORES);
                case MODE_VEIN_PLUS: return true;
                default: return false;
            }
        }
    }

    public static class DrillBitHandler extends BaseItemStackHandler {
        private static final String NBT_DRILL_BIT = "DrillBit";

        private final ItemStack jackhammerStack;

        public DrillBitHandler(ItemStack jackhammerStack) {
            super(1);

            this.jackhammerStack = jackhammerStack;
            if (jackhammerStack.hasTag() && jackhammerStack.getTag().contains(NBT_DRILL_BIT, Constants.NBT.TAG_STRING)) {
                String name = jackhammerStack.getTag().getString(NBT_DRILL_BIT);
                try {
                    DrillBitType type = DrillBitType.valueOf(name);
                    setStackInSlot(0, type.asItemStack());
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        @Override
        public boolean isItemValid(int slot, ItemStack itemStack) {
            return itemStack.isEmpty() || itemStack.getItem() instanceof ItemDrillBit;
        }

        public void save() {
            ItemStack bitStack = getStackInSlot(0);
            if (bitStack.getItem() instanceof ItemDrillBit) {
                NBTUtils.setString(jackhammerStack, NBT_DRILL_BIT, ((ItemDrillBit) bitStack.getItem()).getType().toString());
            } else {
                NBTUtils.setString(jackhammerStack, NBT_DRILL_BIT, DrillBitType.NONE.toString());
            }
        }
    }

    public static class EnchantmentHandler extends BaseItemStackHandler {
        private final ItemStack jackhammerStack;

        public EnchantmentHandler(ItemStack jackhammerStack) {
            super(1);

            this.jackhammerStack = jackhammerStack;

            Map<Enchantment, Integer> ench = EnchantmentHelper.getEnchantments(jackhammerStack);
            if (ench.size() == 1 && (ench.containsKey(Enchantments.SILK_TOUCH) || ench.containsKey(Enchantments.FORTUNE))) {
                ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
                EnchantmentHelper.setEnchantments(ench, book);
                setStackInSlot(0, book);
            }
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return stack.isEmpty() || validateBook(stack);
        }

        public void save() {
            ItemStack bookStack = getStackInSlot(0);
            if (validateBook(bookStack)) {
                EnchantmentHelper.setEnchantments(EnchantmentHelper.getEnchantments(bookStack), jackhammerStack);
            } else {
                EnchantmentHelper.setEnchantments(Collections.emptyMap(), jackhammerStack);
            }
        }

        public static boolean validateBook(ItemStack bookStack) {
            // must be an enchanted book with Silk Touch or Fortune and nothing else
            if (bookStack.getItem() == Items.ENCHANTED_BOOK) {
                Map<Enchantment, Integer> ench = EnchantmentHelper.getEnchantments(bookStack);
                if (ench.size() != 1) return false;
                return ench.containsKey(Enchantments.FORTUNE) || ench.containsKey(Enchantments.SILK_TOUCH);
            }
            return false;
        }
    }

    @Mod.EventBusSubscriber(modid = Names.MOD_ID)
    public static class Listener {
        @SubscribeEvent
        public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
            PlayerEntity player = event.getPlayer();
            ItemStack stack = player.getHeldItem(event.getHand());
            if (stack.getItem() == ModItems.JACKHAMMER.get() && ItemPressurizable.getAir(stack) > 0f) {
                if (event.getWorld().isRemote) {
                    MovingSounds.playMovingSound(MovingSounds.Sound.JACKHAMMER, event.getPlayer());
                } else {
                    NetworkHandler.sendToAllTracking(new PacketPlayMovingSound(MovingSounds.Sound.JACKHAMMER, player), player);
                }
            }
        }
    }
}
