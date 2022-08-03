package net.skyprison.skyprisoncore.listeners;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.maxgamer.quickshop.api.event.ShopSuccessPurchaseEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class ShopSuccessPurchase implements Listener {
    private final SkyPrisonCore plugin;

    public ShopSuccessPurchase(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    // Date ; other user ; withdraw/deposit ; amount ; was Quickshop ; what bought/sold if quickshop

    @EventHandler
    public void onShopSuccessPurchase(ShopSuccessPurchaseEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            UUID purchaser = event.getPurchaser();
            UUID shopOwner = event.getShop().getOwner();
            File f = new File(plugin.getDataFolder() + File.separator + "logs" + File.separator + "transactions" + File.separator + purchaser + ".log");
            File f2 = new File(plugin.getDataFolder() + File.separator + "logs" + File.separator + "transactions" + File.separator + shopOwner + ".log");
            FileWriter fData = null;
            FileWriter fData2 = null;
            try {
                fData = new FileWriter(f, true);
                fData2 = new FileWriter(f2, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            PrintWriter pData = new PrintWriter(fData);
            PrintWriter pData2 = new PrintWriter(fData2);

            Date date = new Date();
            SimpleDateFormat DateFor = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
            String stringDate = DateFor.format(date);

            if (event.getShop().isBuying()) {
                pData.println(stringDate + ";" + shopOwner + ";deposit;" + event.getBalance() + ";true;" + event.getShop().getItem() + ";" + event.getAmount());
                pData2.println(stringDate + ";" + purchaser + ";withdraw;" + event.getBalance() + ";true;" + event.getShop().getItem() + ";" + event.getAmount());
            } else {
                pData.println(stringDate + ";" + shopOwner + ";withdraw;" + event.getBalance() + ";true;" + event.getShop().getItem() + ";" + event.getAmount());
                pData2.println(stringDate + ";" + purchaser + ";deposit;" + event.getBalance() + ";true;" + event.getShop().getItem() + ";" + event.getAmount());
            }

            pData.flush();
            pData.close();
            pData2.flush();
            pData2.close();
        });
    }
}
