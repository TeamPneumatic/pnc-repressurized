package pneumaticExample;

import pneumaticCraft.api.client.assemblymachine.AssemblyRenderOverriding;
import pneumaticCraft.api.item.ItemSupplier;

public class ClientProxy extends CommonProxy{
    @Override
    public void doClientOnlyStuff(){

        //register a Assembly render override
        AssemblyRenderOverriding.renderOverrides.put(ItemSupplier.getItem("plasticPlant").itemID, new AssemblyRenderOverrideExample());
    }
}
