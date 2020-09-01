package lettas.chains_link;

import net.fabricmc.api.ModInitializer;
import net.minecraft.state.property.BooleanProperty;

public class chains implements ModInitializer {
	public static boolean isLanternGravity = false;
	public static final BooleanProperty EDIT_DOWN = BooleanProperty.of("edit_down");
	public static final BooleanProperty EDIT_UP = BooleanProperty.of("edit_up");
	public static final BooleanProperty EDIT_NORTH = BooleanProperty.of("edit_north");
	public static final BooleanProperty EDIT_SOUTH = BooleanProperty.of("edit_south");
	public static final BooleanProperty EDIT_WEST = BooleanProperty.of("edit_west");
	public static final BooleanProperty EDIT_EAST = BooleanProperty.of("edit_east");

	@Override
	public void onInitialize() {

	}
}
