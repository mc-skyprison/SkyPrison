package net.skyprison.skyprisoncore.utils;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.mariadb.jdbc.MariaDbPoolDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseHook {
    private final DataSource dataSource;

    public DatabaseHook(SkyPrisonCore plugin) throws SQLException {
        MariaDbPoolDataSource dataSource = new MariaDbPoolDataSource();
        String ip = plugin.getConfig().getString("database.ip");
        if(ip != null && !ip.isEmpty()) {
            String port = plugin.getConfig().getString("database.port");
            String name = plugin.getConfig().getString("database.name");
            String user = plugin.getConfig().getString("database.user");
            String password = plugin.getConfig().getString("database.password");

            dataSource.setUrl("jdbc:mariadb://" + ip + ":" + port + "/" + name + "?user=" + user + "&password=" + password);
        }
        this.dataSource = dataSource;
    }

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
