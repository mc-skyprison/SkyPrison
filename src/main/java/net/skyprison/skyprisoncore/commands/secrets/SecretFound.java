package net.skyprison.skyprisoncore.commands.secrets;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DailyMissions;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class SecretFound implements CommandExecutor {
	private final SkyPrisonCore plugin;
	private final DailyMissions dailyMissions;
	private final DatabaseHook hook;

	public SecretFound(SkyPrisonCore plugin, DailyMissions dailyMissions, DatabaseHook hook) {
		this.plugin = plugin;
		this.dailyMissions = dailyMissions;
		this.hook = hook;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = (Player) sender;
		File f = new File(plugin.getDataFolder() + File.separator + "secrets.yml");
		YamlConfiguration yamlf = YamlConfiguration.loadConfiguration(f);
		Set<String> secrets = yamlf.getKeys(true);
		String secretId;
		String guiType = "";

		for(String secretKey : secrets) {
			if(secretKey.endsWith(".id")) {
				String[] getId = secretKey.split("[.]");
				secretId = getId[0] + "." + getId[1] + "." + getId[2];
				guiType =  getId[1];
				if(Objects.requireNonNull(yamlf.getString(secretId + ".id")).equalsIgnoreCase(args[0])) {
					int amountFound = 0;

					try {
						Connection conn = hook.getSQLConnection();
						PreparedStatement ps = conn.prepareStatement("SELECT secret_amount FROM secrets_data WHERE user_id = '" + player.getUniqueId() + "' AND secret_name = '" + args[0] + "'");
						ResultSet rs = ps.executeQuery();
						while(rs.next()) {
							amountFound = rs.getInt(1);
						}
						hook.close(ps, rs, conn);
					} catch (SQLException e) {
						e.printStackTrace();
					}

					String sql;
					List<Object> params;
					if(amountFound == 0) {
						sql = "INSERT INTO secrets_data (user_id, secret_name, secret_amount) VALUES (?, ?, ?)";
						params = new ArrayList<>() {{
							add(player.getUniqueId());
							add(args[0]);
							add(1);
						}};
					} else {
						sql = "UPDATE secrets_data SET secret_amount = secret_amount + 1 WHERE user_id = ? AND secret_name = ?";
						params = new ArrayList<>() {{
							add(player.getUniqueId().toString());
							add(args[0]);
						}};
					}
					hook.sqlUpdate(sql, params);
					break;
				}
			}
		}
		secrets = Objects.requireNonNull(yamlf.getConfigurationSection("inventory." + guiType)).getKeys(false);

		int secretsFound = 0;
		int totalSecrets = 0;

		for(String secretKey : secrets) {
			if(yamlf.isSet("inventory." + guiType + "." + secretKey + ".id")) {
				totalSecrets += 1;
			}
		}

		try {
			Connection conn = hook.getSQLConnection();
			PreparedStatement ps = conn.prepareStatement("SELECT secret_name FROM secrets_data WHERE user_id = '" + player.getUniqueId() + "'");
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				if(rs.getString(1).contains(guiType)) secretsFound += 1;
			}
			hook.close(ps, rs, conn);
		} catch (SQLException e) {
			e.printStackTrace();
		}


		if (secretsFound == totalSecrets) {
			boolean alreadyDone = false;
			try {
				Connection conn = hook.getSQLConnection();
				PreparedStatement ps = conn.prepareStatement("SELECT reward_name FROM rewards_data WHERE user_id = '" + player.getUniqueId() + "' AND reward_name = 'first-time-" + guiType + "'");
				ResultSet rs = ps.executeQuery();
				while(rs.next()) {
					alreadyDone = true;
				}
				hook.close(ps, rs, conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}

			if(!alreadyDone) {
				String sql = "INSERT INTO rewards_data (user_id, reward_name, reward_collected) VALUES (?, ?, ?)";
				String finalGuiType = guiType;
				List<Object> params = new ArrayList<>() {{
					add(player.getUniqueId().toString());
					add("first-time-" + finalGuiType);
					add(0);
				}};
				hook.sqlUpdate(sql, params);

				player.sendMessage("You have found all secrets in this category! Check out the Rewards GUI in /secrets to collect your reward!");
			}
		}
		for(String mission : dailyMissions.getMissions(player)) {
			if(!dailyMissions.isCompleted(player, mission)) {
				String[] missSplit = mission.split("-");
				if (missSplit[0].equalsIgnoreCase("secrets")) {
					dailyMissions.updatePlayerMission(player, mission);
				}
			}
		}

		return true;
	}
}

