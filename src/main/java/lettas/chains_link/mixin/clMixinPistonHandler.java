package lettas.chains_link.mixin;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.*;
import net.minecraft.block.piston.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(value = PistonHandler.class, priority = 419)
public abstract class clMixinPistonHandler {
    @Shadow @Final private World world;
    @Shadow @Final private BlockPos posFrom;
    @Shadow @Final private Direction motionDirection;
    @Shadow private final List<BlockPos> movedBlocks = Lists.newArrayList();
    @Shadow private final List<BlockPos> brokenBlocks = Lists.newArrayList();

    @Shadow private static boolean isBlockSticky(Block block) {
        return isBlockSticky(block);
    }

    @Inject(method = "isBlockSticky", at = @At(value = "HEAD"), cancellable = true)
    private static void claddChainsSticky(Block block, CallbackInfoReturnable<Boolean> cir){
        if(block == Blocks.CHAIN) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    private static boolean isAdjacentBlockStuck(BlockState blocks, BlockState block2s, Direction dir) {
        Block block = blocks.getBlock();
        Block block2 = block2s.getBlock();
        if (block == Blocks.HONEY_BLOCK && block2 == Blocks.SLIME_BLOCK) {
            return false;
        } else if (block == Blocks.SLIME_BLOCK && block2 == Blocks.HONEY_BLOCK) {
            return false;
        } else if (block == Blocks.CHAIN && blocks.get(Properties.AXIS) != dir.getAxis()) {
            return false;
        } else if (block2 == Blocks.CHAIN && (block != Blocks.SLIME_BLOCK && block != Blocks.HONEY_BLOCK) && block2s.get(Properties.AXIS) != dir.getAxis()) {
            return false;
        } else {
            return isBlockSticky(block) || isBlockSticky(block2);
        }
    }

    @Overwrite
    private boolean tryMove(BlockPos pos, Direction dir) {
        BlockState blockState = this.world.getBlockState(pos);
        Block block = blockState.getBlock();
        if (blockState.isAir()) {
            return true;
        } else if (!PistonBlock.isMovable(blockState, this.world, pos, this.motionDirection, false, dir)) {
            return true;
        } else if (pos.equals(this.posFrom)) {
            return true;
        } else if (this.movedBlocks.contains(pos)) {
            return true;
        } else {
            int i = 1;
            if (i + this.movedBlocks.size() > 12) {
                return false;
            } else {
                while (isBlockSticky(block)) {
                    BlockPos blockPos = pos.offset(this.motionDirection.getOpposite(), i);
                    BlockState blockState1 = blockState;
                    blockState = this.world.getBlockState(blockPos);
                    block = blockState.getBlock();
                    if (blockState.isAir() || !isAdjacentBlockStuck(blockState1, blockState, this.motionDirection) || !PistonBlock.isMovable(blockState, this.world, blockPos, this.motionDirection, false, this.motionDirection.getOpposite()) || blockPos.equals(this.posFrom)) {
                        break;
                    }

                    ++i;
                    if (i + this.movedBlocks.size() > 12) {
                        return false;
                    }
                }

                int j = 0;

                int l;
                for (l = i - 1; l >= 0; --l) {
                    this.movedBlocks.add(pos.offset(this.motionDirection.getOpposite(), l));
                    ++j;
                }

                l = 1;

                while (true) {
                    BlockPos blockPos2 = pos.offset(this.motionDirection, l);
                    int m = this.movedBlocks.indexOf(blockPos2);
                    if (m > -1) {
                        this.setMovedBlocks(j, m);

                        for (int n = 0; n <= m + j; ++n) {
                            BlockPos blockPos3 = (BlockPos) this.movedBlocks.get(n);
                            if (isBlockSticky(this.world.getBlockState(blockPos3).getBlock()) && !this.canMoveAdjacentBlock(blockPos3)) {
                                return false;
                            }
                        }

                        return true;
                    }

                    blockState = this.world.getBlockState(blockPos2);
                    if (blockState.isAir()) {
                        return true;
                    }

                    if (!PistonBlock.isMovable(blockState, this.world, blockPos2, this.motionDirection, true, this.motionDirection) || blockPos2.equals(this.posFrom)) {
                        return false;
                    }

                    if (blockState.getPistonBehavior() == PistonBehavior.DESTROY) {
                        this.brokenBlocks.add(blockPos2);
                        return true;
                    }

                    if (this.movedBlocks.size() >= 12) {
                        return false;
                    }

                    this.movedBlocks.add(blockPos2);
                    ++j;
                    ++l;
                }
            }
        }
    }

    @Overwrite
    private static boolean isAdjacentBlockStuck(Block block, Block block2) {
        // System.out.println("illegal adjblockstuck method called");
        return false;
    }

    @Shadow private boolean canMoveAdjacentBlock(BlockPos pos) {
        return canMoveAdjacentBlock(pos);
    }

    @Shadow private void setMovedBlocks(int from, int to) {}

    @Inject(method = "canMoveAdjacentBlock", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;", ordinal = 1), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void clAdjacentBlockStuck(BlockPos pos, CallbackInfoReturnable<Boolean> cir, BlockState blockState, Direction[] var3, int var4, int var5, Direction direction, BlockPos blockPos, BlockState blockState1) {
        if(isAdjacentBlockStuck(blockState, blockState1, direction) && !this.tryMove(blockPos, direction)) {
            cir.setReturnValue(false);
        }
    }
}
