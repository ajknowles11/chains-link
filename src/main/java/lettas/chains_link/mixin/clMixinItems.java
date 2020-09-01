package lettas.chains_link.mixin;

import lettas.chains_link.ChainItem;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Items.class, priority = 420)
public abstract class clMixinItems {

    public clMixinItems() { super(); }

    @Inject(method = "Lnet/minecraft/item/Items;register(Lnet/minecraft/block/Block;Lnet/minecraft/item/ItemGroup;)Lnet/minecraft/item/Item;", at = @At(value = "HEAD"), cancellable = true)
    private static void redirectChain(Block block, ItemGroup group, CallbackInfoReturnable cir) {
        if (block == Blocks.CHAIN) {
            cir.setReturnValue(register((BlockItem)(new ChainItem(Blocks.CHAIN, (new Item.Settings()).group(ItemGroup.DECORATIONS)))));
        }
    }

    @Shadow
    private static Item register(BlockItem item) {
        return register(item);
    }
}
