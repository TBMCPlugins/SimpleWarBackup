package simpleWarBackup;

import java.io.File;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.server.v1_12_R1.RegionFile;

public class RegionFileCache 
{
	public static final Map<File, RegionFile> files = Maps.newHashMap();
}
