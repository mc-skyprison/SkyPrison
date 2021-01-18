package com.github.drakepork.skyprisoncore.Utils;

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

			// Chat Messages

			langConf.addDefault("chat.admin.format", "&8&l(&c&lADMIN&8&l) &b[name]: &a[message]");
			langConf.addDefault("chat.admin.wrong-usage", "&cWrong Usage! /a <message>");

			langConf.addDefault("chat.guard.format", "&8&l(&4&lRoyal&9&lGuard&8&l) &6[name]&7: &3[message]");
			langConf.addDefault("chat.guard.wrong-usage", "&cWrong Usage! /k <message>");

			langConf.addDefault("chat.discord.format", "&8&l(&9&lDISCORD&8&l) &8[name]: &7[message]");
			langConf.addDefault("chat.discord.wrong-usage", "&cWrong Usage! /d <message>");

			langConf.addDefault("chat.build.format", "&8&l(&a&lBUILDER&8&l) &7[name]: &9[message]");
			langConf.addDefault("chat.build.wrong-usage", "&cWrong Usage! /b <message>");

			langConf.addDefault("chat.staff.format", "&8&l(&a&lSTAFF&8&l) &7[name]: &9[message]");
			langConf.addDefault("chat.staff.wrong-usage", "&cWrong Usage! /s <message>");

			langConf.addDefault("chat.discordSRV.format", "**[name]**: [message]");
			langConf.addDefault("chat.stickied.enabled", "&aEnabled sticky chat for [chat]");
			langConf.addDefault("chat.stickied.disabled", "&aDisabled sticky chat for [chat]");
			langConf.addDefault("chat.stickied.swapped", "&aSwapped sticky chat from [oldchat] to [newchat]");

			langConf.options().copyDefaults(true);
			langConf.save(lang);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
