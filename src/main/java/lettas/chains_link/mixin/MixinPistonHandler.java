package lettas.chains_link.mixin;

import com.google.common.collect.Lists;
import jdk.nashorn.internal.ir.annotations.Ignore;
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

import java.util.List;

import static lettas.chains_link.chains.pushLimit;

@Mixin(PistonHandler.class)
public abstract class MixinPistonHandler {
    @Shadow @Final
    private World world;
    private BlockPos posFrom;
    private boolean retracted;
    private BlockPos posTo;
    private Direction motionDirection;
    private List<BlockPos> movedBlocks = Lists.newArrayList();
    private List<BlockPos> brokenBlocks = Lists.newArrayList();
    private Direction pistonDirection;

    @Overwrite
    private static boolean isBlockSticky(Block block) {
        return block == Blocks.SLIME_BLOCK || block == Blocks.HONEY_BLOCK || block == Blocks.CHAIN;
    }

    private static boolean isAdjacentBlockStuck(BlockState blocks, BlockState block2s, Direction dir) {
        Block block = blocks.getBlock();
        Block block2 = block2s.getBlock();
        if (block == Blocks.HONEY_BLOCK && block2 == Blocks.SLIME_BLOCK) {
            return false;
        } else if (block == Blocks.SLIME_BLOCK && block2 == Blocks.HONEY_BLOCK) {
            return false;
        } else if (block == Blocks.CHAIN && (block2 != Blocks.SLIME_BLOCK && block2 != Blocks.HONEY_BLOCK) && blocks.get(Properties.AXIS) != dir.getAxis()) {
            return false;
        } else if (block2 == Blocks.CHAIN && block2s.get(Properties.AXIS) != dir.getAxis()) {
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
            if (i + this.movedBlocks.size() > pushLimit) {
                return false;
            } else {
                while (isBlockSticky(block)) {
                    BlockPos blockPos = pos.offset(this.motionDirection.getOpposite(), i);
                    BlockState block2s = blockState;
                    blockState = this.world.getBlockState(blockPos);
                    block = blockState.getBlock();
                    if (blockState.isAir() || !isAdjacentBlockStuck(block2s, blockState, this.motionDirection) || !PistonBlock.isMovable(blockState, this.world, blockPos, this.motionDirection, false, this.motionDirection.getOpposite()) || blockPos.equals(this.posFrom)) {
                        break;
                    }

                    ++i;
                    if (i + this.movedBlocks.size() > pushLimit) {
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

                    if (this.movedBlocks.size() >= pushLimit) {
                        return false;
                    }

                    this.movedBlocks.add(blockPos2);
                    ++j;
                    ++l;
                }
            }
        }
    }

    private void setMovedBlocks(int from, int to) {
        List<BlockPos> list = Lists.newArrayList();
        List<BlockPos> list2 = Lists.newArrayList();
        List<BlockPos> list3 = Lists.newArrayList();
        list.addAll(this.movedBlocks.subList(0, to));
        list2.addAll(this.movedBlocks.subList(this.movedBlocks.size() - from, this.movedBlocks.size()));
        list3.addAll(this.movedBlocks.subList(to, this.movedBlocks.size() - from));
        this.movedBlocks.clear();
        this.movedBlocks.addAll(list);
        this.movedBlocks.addAll(list2);
        this.movedBlocks.addAll(list3);
    }

    private boolean canMoveAdjacentBlock(BlockPos pos) {
        BlockState blockState = this.world.getBlockState(pos);
        Direction[] var3 = Direction.values();
        int var4 = var3.length;

        for (int var5 = 0; var5 < var4; ++var5) {
            Direction direction = var3[var5];
            if (direction.getAxis() != this.motionDirection.getAxis()) {
                BlockPos blockPos = pos.offset(direction);
                BlockState blockState2 = this.world.getBlockState(blockPos);
                if (isAdjacentBlockStuck(blockState2, blockState, direction) && !this.tryMove(blockPos, direction)) {
                    return false;
                }
            }
        }

        return true;
    }
}
