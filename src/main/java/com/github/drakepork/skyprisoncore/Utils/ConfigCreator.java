package com.github.drakepork.skyprisoncore.Utils;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.bukkit.configuration.file.FileConfiguration;
import com.github.drakepork.skyprisoncore.Core;

public class ConfigCreator {
	private Core plugin;

	@Inject
	public ConfigCreator(Core plugin) {
		this.plugin = plugin;
	}

	public void init() {
		FileConfiguration config = plugin.getConfig();
		config.addDefault("lang-file", "en.yml");

		config.addDefault("builder-worlds", Lists.newArrayList("world_staffbuild_second"));
		config.addDefault("guard-worlds", Lists.newArrayList("world_prison"));
		config.options().copyDefaults(true);
		plugin.saveConfig();
	}
}
