package eyeliss.particle.mod.item.specialitems;

import eyeliss.particle.mod.screen.EngravingGuideScreenHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class EngravingGuideBookItem extends Item {
    public EngravingGuideBookItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient()) {
            user.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                    (syncId, playerInv, player) -> new EngravingGuideScreenHandler(syncId, playerInv),
                    Text.translatable("item.eyelisspartmod.engraving_guide_book")
            ));
        }
        return TypedActionResult.success(user.getStackInHand(hand));
    }
}
