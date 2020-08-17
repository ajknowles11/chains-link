package lettas.chains_link.mixin;

import net.minecraft.block.*;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import lettas.chains_link.chains;

@Mixin(LanternBlock.class)
public class clMixinLanternBlock extends Block implements Waterloggable {
    @Shadow public static final BooleanProperty HANGING;

    public clMixinLanternBlock(Settings settings) {
        super(settings);
    }

    @Overwrite
    public PistonBehavior getPistonBehavior(BlockState state) {
        return PistonBehavior.NORMAL;
    }

    @Overwrite
    public static Direction attachedDirection(BlockState state) {
        return (Boolean)state.get(HANGING) ? Direction.DOWN : Direction.UP;
    }

    @Overwrite
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        if (!chains.isLanternGravity)
            return true;
        Direction direction = attachedDirection(state).getOpposite();
        return Block.sideCoversSmallSquare(world, pos.offset(direction), direction.getOpposite());
    }

    static {
        HANGING = Properties.HANGING;
        }
}
