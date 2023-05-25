package net.skyprison.skyprisoncore.utils;

import com.google.common.collect.Lists;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigCreator {
	private final SkyPrisonCore plugin;

	public ConfigCreator(SkyPrisonCore plugin) {
		this.plugin = plugin;
	}

	public void init() {
		FileConfiguration config = plugin.getConfig();
		config.addDefault("lang-file", "en.yml");

		config.addDefault("builder-worlds", Lists.newArrayList("world_staffbuild_second"));
		config.addDefault("guard-worlds", Lists.newArrayList("world_prison"));
		config.addDefault("claim-worlds", Lists.newArrayList("world_free"));
		config.addDefault("discord-token", "");
		config.options().copyDefaults(true);
		plugin.saveConfig();
	}
}
