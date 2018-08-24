package me.desht.pneumaticcraft.common.util.fakeplayer;

import com.mojang.authlib.GameProfile;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;

import javax.annotation.Nonnull;

public class DroneFakePlayer extends FakePlayer {
    private final IDroneBase drone;
    private boolean sneaking;

    public DroneFakePlayer(WorldServer world, GameProfile name, IDroneBase drone) {
        super(world, name);
        this.drone = drone;
    }

    @Override
    public void addExperience(int amount) {
        Vec3d pos = drone.getDronePos();
        EntityXPOrb orb = new EntityXPOrb(drone.world(), pos.x, pos.y, pos.z, amount);
        drone.world().spawnEntity(orb);
    }

    @Nonnull
    @Override
    public ItemStack getItemStackFromSlot(@Nonnull EntityEquipmentSlot slotIn) {
        switch (slotIn) {
            case MAINHAND:
                return drone.getInv().getStackInSlot(0);
            default:
                return ItemStack.EMPTY;
        }
    }

    @Override
    public void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack) {
        if (slotIn == EntityEquipmentSlot.MAINHAND) {
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
    public void onUpdate() {
        ticksSinceLastSwing++;  // without this, drone's melee will be hopeless
    }
}
