package crazycrafter.snowaccumulation.common;

import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowBlock;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ChunkManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class SnowAccumulationTickHandler {
	
	private static final Method getLoadedChunkIterator;
	private static final Logger LOGGER = LogManager.getLogger();
	private static boolean disabled = false;
	
	static {
		Method tmpglci = null;
		try {
			tmpglci = ObfuscationReflectionHelper.findMethod(ChunkManager.class, "func_223491_f");
			tmpglci.setAccessible(true);
		} catch (Exception ex) {
			tmpglci = null;
		}
		getLoadedChunkIterator = tmpglci;
	}
	
	int randLCG;
	
	public SnowAccumulationTickHandler() {
		randLCG = (new Random()).nextInt();
	}
	
	@SubscribeEvent
	public void worldTickEvent(WorldTickEvent e){
		if (disabled) {
			return;
		}
		if (null == getLoadedChunkIterator) {
			LOGGER.fatal("Reflection failure");
			disabled = true;
			return;
		}
	    if (e.world instanceof ServerWorld) {
	    	ServerWorld worldserver = (ServerWorld) e.world;
	    	if (worldserver.isRaining()) {	
	    		IProfiler profiler = worldserver.getProfiler();
	    		profiler.startSection("snowaccumulation");
	    		try {
	    			@SuppressWarnings("unchecked")
	    			Iterable<ChunkHolder> chunkContainer = (Iterable<ChunkHolder>) getLoadedChunkIterator.invoke(worldserver.getChunkProvider().chunkManager);
	    			for (ChunkHolder chunkHolder : chunkContainer) {
	    				Chunk chunk = chunkHolder.getChunkIfComplete();
	    				if (chunk == null || !worldserver.getChunkProvider().isChunkLoaded(chunk.getPos())) {
	    					continue;
	    				}
		    			int chunk_min_x = chunk.getPos().getXStart();
		    			int chunk_min_z = chunk.getPos().getZStart();
		    			//If it can rain here, there is a 1/16 chance of trying to add snow
		    			if (worldserver.dimension.canDoRainSnowIce(chunk) && worldserver.rand.nextInt(16) == 0 ) {
		    				randLCG = randLCG * 3 + 1013904223;
		    				int j2 = randLCG >> 2;
		    				//Get rain height at random position in chunk, splits the random val j2 to use for both parts of position
		    				BlockPos pos1 = worldserver.getHeight(Heightmap.Type.MOTION_BLOCKING, new BlockPos(chunk_min_x + (j2 & 15), 0, chunk_min_z + (j2 >> 8 & 15)));
		    				//Check if block at positioin is a snow layer block
		    				if (worldserver.getBlockState(pos1).getBlock() instanceof SnowBlock) {
		    					//Check if valid Y, correct light, and correct temp for snow formation
		    			    	if(pos1.getY() >= 0 && pos1.getY() < 256 && worldserver.getLightFor(LightType.BLOCK, pos1 ) < 10 &&worldserver.getBiome(pos1).getTemperature(pos1) < 0.15F) {
		    			    		//Calculate mean surrounding block height
		    			    		int height = worldserver.getBlockState(pos1).get(SnowBlock.LAYERS);
		    			    		if (height == 8) {
		    			    			return;
		    			    		}
		    			    		BlockState north = worldserver.getBlockState(pos1.north());
		    			    		BlockState south = worldserver.getBlockState(pos1.south());
		    			    		BlockState east = worldserver.getBlockState(pos1.east());
		    			    		BlockState west = worldserver.getBlockState(pos1.west());
		    			    		float surroundings = 0;
		    			    		if (north.getBlock() instanceof SnowBlock) {
		    			    			surroundings += north.get(SnowBlock.LAYERS);
		    			    		} else if(north.isSolid()) {
		    			    			surroundings += 8;
		    			    		}
		    			    		if (south.getBlock() instanceof SnowBlock) {
		    			   				surroundings += south.get(SnowBlock.LAYERS);
		    			   			} else if(south.isSolid()) {
		    		    				surroundings += 8;
		    		    			}
		    		    			if (east.getBlock() instanceof SnowBlock) {
		    		    				surroundings += east.get(SnowBlock.LAYERS);
		    		    			} else if(east.isSolid()) {
		    		    				surroundings += 8;
		    		    			}
		    		    			if (west.getBlock() instanceof SnowBlock) {
		    		    				surroundings += west.get(SnowBlock.LAYERS);
		    		    			} else if(west.isSolid()) {
		    		    				surroundings += 8;
		    		    			}
		    		    			surroundings /= 4;
		    		    			//Done calculating surroundings
		    		    			if (surroundings >= height) {
		   			   					float weight = (surroundings - height) / 2 + 0.05f;
		   			   					if (worldserver.rand.nextFloat() <= weight) {
		   			   						//Add layer!
		    		    					worldserver.setBlockState(pos1, Blocks.SNOW.getDefaultState().with(SnowBlock.LAYERS, height + 1));
		    		    				}
		    		    			}	
		    		    		}
		   			    	}
		   			    }
		   			}
	    		} catch (Exception ex) {
	    			LOGGER.fatal("Error accumulating snow");
	    			LOGGER.fatal(ex.getMessage());
	    		} finally {
	    			profiler.endSection();
	    		}
	    	}
	    }
	}
}
