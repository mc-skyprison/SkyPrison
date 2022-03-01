package net.skyprison.skyprisoncore.listeners;

import com.Zrips.CMI.Containers.CMIUser;
import com.Zrips.CMI.events.CMIUserBalanceChangeEvent;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CMIUserBalanceChange implements Listener {
    private SkyPrisonCore plugin;

    public CMIUserBalanceChange(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCMIUserBalanceChange(CMIUserBalanceChangeEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            CMIUser toUser = event.getUser();
            CMIUser fromUser = event.getSource();
            if (toUser != null && fromUser != null) {
                File f = new File(plugin.getDataFolder() + File.separator + "logs" + File.separator + "transactions" + File.separator + toUser.getUniqueId() + ".log");
                FileWriter fData = null;
                try {
                    fData = new FileWriter(f, true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                PrintWriter pData = new PrintWriter(fData);

                Date date = new Date();
                SimpleDateFormat DateFor = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
                String stringDate = DateFor.format(date);
                double amount = event.getTo() - event.getFrom();
                if (event.getActionType().equalsIgnoreCase("withdraw")) {
                    amount = event.getFrom() - event.getTo();
                }
                pData.println(stringDate + ";" + fromUser.getUniqueId() + ";" + event.getActionType().toLowerCase() + ";" + amount + ";false;null;null");

                pData.flush();
                pData.close();
            }
        });
    }
}
