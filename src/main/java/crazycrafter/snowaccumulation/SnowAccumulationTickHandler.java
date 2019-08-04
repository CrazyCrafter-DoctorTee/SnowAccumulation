package crazycrafter.snowaccumulation;

import java.lang.reflect.Method;
import java.util.Random;

import net.fabricmc.fabric.api.event.world.WorldTickCallback;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowBlock;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.BlockPos;

public class SnowAccumulationTickHandler implements WorldTickCallback{

	int randLCG;
	
	public SnowAccumulationTickHandler(){
		randLCG = (new Random()).nextInt();
	}
	
	@Override
	public void tick(World world) {
		if(world instanceof ServerWorld)
		{
			ServerWorld worldserver = (ServerWorld) world;
	    	if(worldserver.isRaining())
	    	{	
	    		try
	    		{	
	    			Method getChunkHolderIterator;
	    			try{
	    				getChunkHolderIterator = (ThreadedAnvilChunkStorage.class).getMethod("f");
	    			} catch (NoSuchMethodException ex1) {
	    				try
	    				{
	    					getChunkHolderIterator = (ThreadedAnvilChunkStorage.class).getMethod("method_17264");
	    				} catch (NoSuchMethodException ex2) {
	    					getChunkHolderIterator = (ThreadedAnvilChunkStorage.class).getMethod("entryIterator");
	    				}
	    			}
	    			getChunkHolderIterator.setAccessible(true);
	    			@SuppressWarnings("unchecked")
	    			Iterable<ChunkHolder> chunkSet = (Iterable<ChunkHolder>) getChunkHolderIterator.invoke(((ServerChunkManager)worldserver.getChunkManager()).threadedAnvilChunkStorage);
	    			for ( ChunkHolder holder : chunkSet)
		    		{
	    				Chunk chunk = holder.getCompletedChunk();
	    				if (chunk == null || !worldserver.getChunkManager().isChunkLoaded(chunk.getPos().x, chunk.getPos().z))
	    				{
	    					continue;
	    				}
		    			int chunk_min_x = chunk.getPos().getStartX();
		    			int chunk_min_y = chunk.getPos().getStartZ();
		    			//If it can rain here, there is a 1/16 chance of trying to add snow
		    			if (worldserver.random.nextInt(16) == 0 )
		    			{
		    				randLCG = randLCG * 3 + 1013904223;
		    				int j2 = randLCG >> 2;
		    				//Get rain height at random position in chunk, splits the random val j2 to use for both parts of position
		    				BlockPos pos1 = worldserver.getTopPosition(Heightmap.Type.MOTION_BLOCKING, new BlockPos(chunk_min_x + (j2 & 15), 0, chunk_min_y + (j2 >> 8 & 15)));
		    				//Check if block at positioin is a snow layer block
		    				if(worldserver.getBlockState(pos1).getBlock() instanceof SnowBlock)
		    			    {
		    					//Check if valid Y, correct light, and correct temp for snow formation
		    			    	if(pos1.getY() >= 0 && pos1.getY() < 256 && worldserver.getLightLevel(LightType.BLOCK, pos1 ) < 10 &&worldserver.getBiome(pos1).getTemperature(pos1) < 0.15F)
		    			    	{
		    			    		//Calculate mean surrounding block height
		    			    		int height = worldserver.getBlockState(pos1).get(SnowBlock.LAYERS);
		    			    		if(height == 8) return;
		    			    		BlockState north = worldserver.getBlockState(pos1.north());
		    			    		BlockState south = worldserver.getBlockState(pos1.south());
		    			    		BlockState east = worldserver.getBlockState(pos1.east());
		    			    		BlockState west = worldserver.getBlockState(pos1.west());
		    			    		float surroundings = 0;
		    			    		if(north.getBlock() instanceof SnowBlock)
		    			    		{
		    			    			surroundings += north.get(SnowBlock.LAYERS);
		    			    		}else if(north.isSimpleFullBlock(chunk, pos1.north()))
		    			    		{
		    			    			surroundings += 8;
		    			    		}
		    			    		if(south.getBlock() instanceof SnowBlock)
		    			   			{
		    			   				surroundings += south.get(SnowBlock.LAYERS);
		    			   			}else if(south.isSimpleFullBlock(chunk, pos1.south()))
		    			   			{
		    		    				surroundings += 8;
		    		    			}
		    		    			if(east.getBlock() instanceof SnowBlock)
		    		    			{
		    		    				surroundings += east.get(SnowBlock.LAYERS);
		    		    			}else if(east.isSimpleFullBlock(chunk, pos1.east()))
		    		    			{
		    		    				surroundings += 8;
		    		    			}
		    		    			if(west.getBlock() instanceof SnowBlock)
		    		    			{
		    		    				surroundings += west.get(SnowBlock.LAYERS);
		    		    			}else if(west.isSimpleFullBlock(chunk, pos1.west()))
		    		    			{
		    		    				surroundings += 8;
		    		    			}
		    		    			surroundings /= 4;
		    		    			//Done calculating surroundings
		    		    			if(surroundings >= height)
		   			   				{
		   			   					float weight = (surroundings - height) / 2 + 0.05f;
		   			   					if(worldserver.random.nextFloat() <= weight)
		    			   				{
		   			   						//Add layer!
		    		    					worldserver.setBlockState(pos1, Blocks.SNOW.getDefaultState().with(SnowBlock.LAYERS, height + 1));
		    		    				}
		    		    			}	
		    		    		}
		   			    	}
		   			    }
		   			}
	    		} catch (Exception ex) {
	    			System.out.println("COULD NOT ACCESS LOADED CHUNKS!");
	    			System.out.println(ex.getMessage());
	    			ex.printStackTrace();
	    		}
	    	}
		}
	}

}
