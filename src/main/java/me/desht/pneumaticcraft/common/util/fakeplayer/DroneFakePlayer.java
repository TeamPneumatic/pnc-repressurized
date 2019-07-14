package me.desht.pneumaticcraft.common.util.fakeplayer;

import com.mojang.authlib.GameProfile;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;

import javax.annotation.Nonnull;

public class DroneFakePlayer extends FakePlayer {
    private final IDroneBase drone;
    private boolean sneaking;

    public DroneFakePlayer(ServerWorld world, GameProfile name, IDroneBase drone) {
        super(world, name);
        this.drone = drone;
    }

    @Override
    public void giveExperiencePoints(int amount) {
        Vec3d pos = drone.getDronePos();
        ExperienceOrbEntity orb = new ExperienceOrbEntity(drone.world(), pos.x, pos.y, pos.z, amount);
        drone.world().addEntity(orb);
    }

    @Nonnull
    @Override
    public ItemStack getItemStackFromSlot(@Nonnull EquipmentSlotType slotIn) {
        return slotIn == EquipmentSlotType.MAINHAND ? drone.getInv().getStackInSlot(0) : ItemStack.EMPTY;
    }

    @Override
    public void setItemStackToSlot(EquipmentSlotType slotIn, ItemStack stack) {
        if (slotIn == EquipmentSlotType.MAINHAND) {
            drone.getInv().setStackInSlot(0, stack);
        }
    }

    @Override
    public boolean isSneaking() {
        return sneaking;
    }

    @Override
    public void setSneaking(boolean sneaking) {
        this.sneaking = sneaking;
    }

    @Override
    public void tick() {
        ticksSinceLastSwing++;  // without this, drone's melee will be hopeless
    }
}
