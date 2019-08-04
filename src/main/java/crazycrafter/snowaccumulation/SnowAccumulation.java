package crazycrafter.snowaccumulation;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.world.WorldTickCallback;

public class SnowAccumulation implements ModInitializer{

	public SnowAccumulationTickHandler tickHandler;
	
	@Override
	public void onInitialize() {
		tickHandler = new SnowAccumulationTickHandler();
		WorldTickCallback.EVENT.register(tickHandler);
	}
	
}