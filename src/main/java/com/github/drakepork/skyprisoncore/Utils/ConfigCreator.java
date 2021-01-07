package com.github.drakepork.skyprisoncore.Utils;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.bukkit.configuration.file.FileConfiguration;
import com.github.drakepork.skyprisoncore.Core;

import java.util.ArrayList;


public class ConfigCreator {
	private Core plugin;

	@Inject
	public ConfigCreator(Core plugin) {
		this.plugin = plugin;
	}

	public void init() {
		FileConfiguration config = plugin.getConfig();
		config.addDefault("lang-file", "en.yml");

		config.addDefault("enable-op-command", true);
		config.addDefault("enable-deop-command", true);
		config.addDefault("deop-on-join", false);
		config.addDefault("builder-worlds", Lists.newArrayList("world"));
		config.addDefault("guard-worlds", Lists.newArrayList("prison"));
		config.addDefault("contrabands", Lists.newArrayList("wooden_sword"));
		config.addDefault("opped-access", Lists.newArrayList(""));
		config.options().copyDefaults(true);

		config.options().copyDefaults(true);
		this.plugin.saveConfig();
	}
}
