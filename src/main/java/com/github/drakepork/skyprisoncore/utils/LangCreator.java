package com.github.drakepork.skyprisoncore.utils;

import com.google.inject.Inject;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import com.github.drakepork.skyprisoncore.Core;

import java.io.File;
import java.io.IOException;

public class LangCreator {
	private Core plugin;

	@Inject
	public LangCreator(Core plugin) {
		this.plugin = plugin;
	}

	public void init() {
		File lang = new File(plugin.getDataFolder() + File.separator
				+ "lang" + File.separator + plugin.getConfig().getString("lang-file"));
		try {
			FileConfiguration langConf = YamlConfiguration.loadConfiguration(lang);

			// Global Messages

			langConf.addDefault("global.plugin-prefix", "&f[&cSkyPrison&f] ");

			langConf.options().copyDefaults(true);
			langConf.save(lang);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
