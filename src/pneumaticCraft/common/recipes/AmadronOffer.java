package pneumaticCraft.common.recipes;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import pneumaticCraft.lib.Log;

import com.google.gson.JsonObject;

import cpw.mods.fml.common.registry.GameData;

public class AmadronOffer{
    protected Object input;
    protected Object output;

    public AmadronOffer(Object input, Object output){
        if(input == null) throw new NullPointerException("Input item/fluid can't be null!");
        if(output == null) throw new NullPointerException("Output item/fluid can't be null!");
        if(input instanceof ItemStack) {
            if(((ItemStack)input).stackSize <= 0) throw new IllegalArgumentException("Input item needs to have a stacksize of > 0!");
            if(((ItemStack)input).getItem() == null) throw new IllegalArgumentException("Input item can't be null!");
        } else if(input instanceof FluidStack) {
            if(((FluidStack)input).amount <= 0) throw new IllegalArgumentException("Input fluid needs to have an amount of > 0!");
        } else {
            throw new IllegalArgumentException("Input must be of type ItemStack or FluidStack. Input: " + input);
        }
        if(output instanceof ItemStack) {
            if(((ItemStack)output).stackSize <= 0) throw new IllegalArgumentException("Output item needs to have a stacksize of > 0!");
            if(((ItemStack)output).getItem() == null) throw new IllegalArgumentException("Output item can't be null!");
        } else if(output instanceof FluidStack) {
            if(((FluidStack)output).amount <= 0) throw new IllegalArgumentException("Output fluid needs to have an amount of > 0!");
        } else {
            throw new IllegalArgumentException("Output must be of type ItemStack or FluidStack. Output: " + input);
        }
        this.input = input;
        this.output = output;
    }

    public Object getInput(){
        return input;
    }

    public Object getOutput(){
        return output;
    }

    public String getVendor(){
        return StatCollector.translateToLocal("gui.amadron");//Hardcoded for now until inter-player trading is implemented.
    }

    public int getStock(){
        return -1;
    }

    public boolean passesQuery(String query){
        String queryLow = query.toLowerCase();
        return getObjectName(getInput()).toLowerCase().contains(queryLow) || getObjectName(getOutput()).toLowerCase().contains(queryLow) || getVendor().toLowerCase().contains(queryLow);
    }

    private String getObjectName(Object object){
        return object instanceof ItemStack ? ((ItemStack)object).getDisplayName() : ((FluidStack)object).getLocalizedName();
    }

    public void onTrade(int tradingAmount, String buyingPlayer){}

    public void writeToNBT(NBTTagCompound tag){
        NBTTagCompound subTag = new NBTTagCompound();
        if(input instanceof ItemStack) {
            ((ItemStack)input).writeToNBT(subTag);
            tag.setTag("inputItem", subTag);
        } else {
            ((FluidStack)input).writeToNBT(subTag);
            tag.setTag("inputFluid", subTag);
        }
        subTag = new NBTTagCompound();
        if(output instanceof ItemStack) {
            ((ItemStack)output).writeToNBT(subTag);
            tag.setTag("outputItem", subTag);
        } else {
            ((FluidStack)output).writeToNBT(subTag);
            tag.setTag("outputFluid", subTag);
        }
    }

    public static AmadronOffer loadFromNBT(NBTTagCompound tag){
        Object input;
        if(tag.hasKey("inputItem")) {
            input = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("inputItem"));
        } else {
            input = FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("inputFluid"));
        }
        Object output;
        if(tag.hasKey("outputItem")) {
            output = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("outputItem"));
        } else {
            output = FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("outputFluid"));
        }
        return new AmadronOffer(input, output);
    }

    public JsonObject toJson(){
        JsonObject object = new JsonObject();

        JsonObject inputObject = new JsonObject();
        if(input instanceof ItemStack) {
            inputObject.addProperty("id", GameData.getItemRegistry().getNameForObject(((ItemStack)input).getItem()));
            inputObject.addProperty("damage", ((ItemStack)input).getItemDamage());
            inputObject.addProperty("amount", ((ItemStack)input).stackSize);
        } else {
            inputObject.addProperty("id", ((FluidStack)input).getFluid().getName());
            inputObject.addProperty("amount", ((FluidStack)input).amount);
        }
        object.add("input", inputObject);

        JsonObject outputObject = new JsonObject();
        if(output instanceof ItemStack) {
            outputObject.addProperty("id", GameData.getItemRegistry().getNameForObject(((ItemStack)output).getItem()));
            outputObject.addProperty("damage", ((ItemStack)output).getItemDamage());
            outputObject.addProperty("amount", ((ItemStack)output).stackSize);
        } else {
            outputObject.addProperty("id", ((FluidStack)output).getFluid().getName());
            outputObject.addProperty("amount", ((FluidStack)output).amount);
        }
        object.add("output", outputObject);

        return object;
    }

    public static AmadronOffer fromJson(JsonObject object){
        JsonObject inputObject = object.getAsJsonObject("input");
        Object input;
        if(inputObject.has("damage")) {
            Item item = GameData.getItemRegistry().getObject(inputObject.get("id").getAsString());
            if(item != null) {
                input = new ItemStack(item, inputObject.get("amount").getAsInt(), inputObject.get("damage").getAsInt());
            } else {
                Log.error("Invalid Amadron Offer input item. Invalid item name: " + inputObject.get("id").getAsString() + ". Offer will be skipped");
                return null;
            }
        } else {
            Fluid fluid = FluidRegistry.getFluid(inputObject.get("id").getAsString());
            if(fluid != null) {
                input = new FluidStack(fluid, inputObject.get("amount").getAsInt());
            } else {
                Log.error("Invalid Amadron Offer input fluid. Invalid fluid name: " + inputObject.get("id").getAsString() + ". Offer will be skipped");
                return null;
            }
        }

        JsonObject outputObject = object.getAsJsonObject("output");
        Object output;
        if(outputObject.has("damage")) {
            Item item = GameData.getItemRegistry().getObject(outputObject.get("id").getAsString());
            if(item != null) {
                output = new ItemStack(item, outputObject.get("amount").getAsInt(), outputObject.get("damage").getAsInt());
            } else {
                Log.error("Invalid Amadron Offer output item. Invalid item name: " + outputObject.get("id").getAsString() + ". Offer will be skipped");
                return null;
            }
        } else {
            Fluid fluid = FluidRegistry.getFluid(outputObject.get("id").getAsString());
            if(fluid != null) {
                output = new FluidStack(fluid, outputObject.get("amount").getAsInt());
            } else {
                Log.error("Invalid Amadron Offer output fluid. Invalid fluid name: " + outputObject.get("id").getAsString() + ". Offer will be skipped");
                return null;
            }
        }

        return new AmadronOffer(input, output);
    }

    @Override
    public int hashCode(){
        int code = getObjectHashCode(getInput());
        code = 31 * code + getObjectHashCode(getOutput());
        code = 31 * code + getVendor().hashCode();
        return code;
    }

    private int getObjectHashCode(Object o){
        if(o instanceof FluidStack) {
            return o.hashCode();
        } else {
            ItemStack stack = (ItemStack)o;
            return GameData.getItemRegistry().getNameForObject(stack.getItem()).hashCode() + stack.stackSize * 19;
        }
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof AmadronOffer) {
            AmadronOffer offer = (AmadronOffer)o;
            if(offer.getInput().getClass() == getInput().getClass() && offer.getOutput().getClass() == getOutput().getClass()) {
                if(offer.getInput() instanceof ItemStack) {
                    ItemStack s1 = (ItemStack)offer.getInput();
                    ItemStack s2 = (ItemStack)getInput();
                    if(!ItemStack.areItemStacksEqual(s1, s2)) return false;
                } else {
                    FluidStack s1 = (FluidStack)offer.getInput();
                    FluidStack s2 = (FluidStack)getInput();
                    if(!s1.isFluidEqual(s2) || s1.amount != s2.amount) return false;
                }
                if(offer.getOutput() instanceof ItemStack) {
                    ItemStack s1 = (ItemStack)offer.getOutput();
                    ItemStack s2 = (ItemStack)getOutput();
                    if(!ItemStack.areItemStacksEqual(s1, s2)) return false;
                } else {
                    FluidStack s1 = (FluidStack)offer.getOutput();
                    FluidStack s2 = (FluidStack)getOutput();
                    if(!s1.isFluidEqual(s2) || s1.amount != s2.amount) return false;
                }
                return getVendor().equals(offer.getVendor());
            }
        }
        return false;
    }
}
