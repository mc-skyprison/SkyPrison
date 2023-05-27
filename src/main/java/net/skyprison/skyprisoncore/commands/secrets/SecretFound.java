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
import java.util.Objects;
import java.util.Set;

public class SecretFound implements CommandExecutor {
	private final SkyPrisonCore plugin;
	private final DailyMissions dailyMissions;
	private final DatabaseHook db;

	public SecretFound(SkyPrisonCore plugin, DailyMissions dailyMissions, DatabaseHook db) {
		this.plugin = plugin;
		this.dailyMissions = dailyMissions;
		this.db = db;
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
					try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO secrets_data (user_id, secret_name, secret_amount) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE secret_amount = secret_amount + VALUE(secret_amount)")) {
						ps.setString(1, player.getUniqueId().toString());
						ps.setString(2, args[0]);
						ps.setInt(3, 1);
						ps.executeUpdate();
					} catch (SQLException e) {
						e.printStackTrace();
					}
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

		try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT secret_name FROM secrets_data WHERE user_id = '" + player.getUniqueId() + "'")) {
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				if(rs.getString(1).contains(guiType)) secretsFound += 1;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}


		if (secretsFound == totalSecrets) {
			boolean alreadyDone = false;
			try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT reward_name FROM rewards_data WHERE user_id = '" + player.getUniqueId() + "' AND reward_name = 'first-time-" + guiType + "'")) {
				ResultSet rs = ps.executeQuery();
				while(rs.next()) {
					alreadyDone = true;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

			if(!alreadyDone) {
				try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO rewards_data (user_id, reward_name, reward_collected) VALUES (?, ?, ?)")) {
					ps.setString(1, player.getUniqueId().toString());
					ps.setString(2, "first-time-" + guiType);
					ps.setInt(3, 0);
					ps.executeUpdate();
				} catch (SQLException e) {
					e.printStackTrace();
				}

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

