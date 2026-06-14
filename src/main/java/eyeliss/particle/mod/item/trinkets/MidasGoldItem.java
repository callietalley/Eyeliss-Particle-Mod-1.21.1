package eyeliss.particle.mod.item.trinkets;

import dev.emi.trinkets.api.Trinket;
import dev.emi.trinkets.api.TrinketItem;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MidasGoldItem extends Item implements Trinket {

    public MidasGoldItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        AtomicBoolean pocketHasItem = new AtomicBoolean(false);
        TrinketsApi.getTrinketComponent(user).ifPresent(comp ->
                comp.forEach((slotRef, stack) -> {
                    boolean isInPocket = slotRef.inventory().getSlotType().getId().endsWith("legs/pocket");
                    if (isInPocket && !stack.isEmpty()) {
                        pocketHasItem.set(true);
                    }
                })
        );

        if (pocketHasItem.get()) {
            return TypedActionResult.pass(itemStack);
        }

        if (TrinketItem.equipItem(user, itemStack)) {
            return TypedActionResult.success(itemStack, world.isClient());
        }

        return TypedActionResult.pass(itemStack);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("Grants the highest item pickup priority")
                .formatted(Formatting.GRAY));
        tooltip.add(Text.literal("Reduces pickup cooldown on dropped items by 33%")
                .formatted(Formatting.GRAY));
        tooltip.add(Text.literal("Killed mobs have a 25% chance to drop gold")
                .formatted(Formatting.GRAY));
        tooltip.add(Text.literal(" ")
                .formatted(Formatting.GRAY));
        super.appendTooltip(stack, context, tooltip, type);
    }
}
