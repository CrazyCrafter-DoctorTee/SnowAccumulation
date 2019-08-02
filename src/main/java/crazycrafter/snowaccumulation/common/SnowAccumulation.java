package crazycrafter.snowaccumulation.common;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod(SnowAccumulation.MODID)
public class SnowAccumulation
{   
	public static final String MODID = "snowaccumulation";
	
	public SnowAccumulation(){
		MinecraftForge.EVENT_BUS.register(new SnowAccumulationTickHandler());
	}
}
