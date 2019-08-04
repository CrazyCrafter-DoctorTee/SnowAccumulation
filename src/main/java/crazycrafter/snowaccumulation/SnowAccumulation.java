package crazycrafter.snowaccumulation;

import java.lang.reflect.Method;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.world.WorldTickCallback;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;

public class SnowAccumulation implements ModInitializer{

	public SnowAccumulationTickHandler tickHandler;
	
	@Override
	public void onInitialize(){
		
		Method getChunkHolder;
		try{
			getChunkHolder = (ThreadedAnvilChunkStorage.class).getDeclaredMethod("f");
			tickHandler = new SnowAccumulationTickHandler(getChunkHolder);
			WorldTickCallback.EVENT.register(tickHandler);
		} catch (NoSuchMethodException ex1) {
			try
			{
				getChunkHolder = (ThreadedAnvilChunkStorage.class).getDeclaredMethod("method_17264");
				tickHandler = new SnowAccumulationTickHandler(getChunkHolder);
				WorldTickCallback.EVENT.register(tickHandler);
			} catch (NoSuchMethodException ex2) {
				try {
					getChunkHolder = (ThreadedAnvilChunkStorage.class).getDeclaredMethod("entryIterator");
					tickHandler = new SnowAccumulationTickHandler(getChunkHolder);
					WorldTickCallback.EVENT.register(tickHandler);
				} catch (NoSuchMethodException ex3) {
					System.err.println("Snow Accumulation failed to apply hooks! Snow Accumulation is not loaded");
				}
			}
		}
	}
}