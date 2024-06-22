package net.skyprison.skyprisoncore.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.NotificationsUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.paper.PaperCommandManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static net.skyprison.skyprisoncore.utils.players.PlayerManager.checkTotalPurchases;
import static net.skyprison.skyprisoncore.utils.players.PlayerManager.getPlayerId;
import static org.incendo.cloud.parser.standard.DoubleParser.doubleParser;
import static org.incendo.cloud.parser.standard.IntegerParser.integerParser;
import static org.incendo.cloud.parser.standard.StringParser.greedyStringParser;
import static org.incendo.cloud.parser.standard.StringParser.stringParser;

public class StoreCommands {
    private final DatabaseHook db;
    private final PaperCommandManager<CommandSourceStack> manager;
    public StoreCommands(DatabaseHook db, PaperCommandManager<CommandSourceStack> manager) {
        this.db = db;
        this.manager = manager;
        createStoreCommands();
    }
    private void createStoreCommands() {
        manager.command(manager.commandBuilder("donoradd")
                .permission("skyprisoncore.command.donoradd")
                .required("player", stringParser())
                .required("currency", stringParser())
                .required("price", doubleParser())
                .required("date", stringParser())
                .required("time", stringParser())
                .required("amount", integerParser())
                .required("bought", greedyStringParser())
                .handler(c -> {
                    UUID pUUID = getPlayerId(c.get("player"));
                    if(pUUID != null) {
                        double price = c.get("price");
                        double totalDonor = price;

                        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT price FROM donations WHERE user_id = ?")) {
                            ps.setString(1, pUUID.toString());
                            ResultSet rs = ps.executeQuery();
                            while (rs.next()) {
                                totalDonor += rs.getDouble(1);
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        try (Connection conn = db.getConnection();
                             PreparedStatement ps = conn.prepareStatement("INSERT INTO donations (user_id, item_bought, price, currency, amount, date) VALUES (?, ?, ?, ?, ?, ?)")) {
                            ps.setString(1, pUUID.toString());
                            ps.setString(2, c.get("bought"));
                            ps.setDouble(3, price);
                            ps.setString(4, c.get("currency"));
                            ps.setInt(5, c.get("amount"));
                            ps.setString(6, c.get("date") + " " + c.get("time"));
                            ps.executeUpdate();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        Player player = Bukkit.getPlayer(pUUID);
                        if(player != null) {
                            checkTotalPurchases(player, totalDonor);
                        } else {
                            NotificationsUtils.scheduleForOnline(pUUID, "purchase-total-check", String.valueOf(totalDonor));
                        }
                    }

                }));
        manager.command(manager.commandBuilder("purchases")
                .permission("skyprisoncore.command.purchases")
                .handler(c -> {
                    CommandSender sender = c.sender().getSender();
                    if(!(sender instanceof Player player)) {
                        sender.sendMessage(Component.text("You must be a player to use this command!", NamedTextColor.RED));
                        return;
                    }
                    getPurchases(player, player.getUniqueId(), 1);
                }));
        manager.command(manager.commandBuilder("purchases")
                .permission("skyprisoncore.command.purchases.others")
                .required("player", stringParser())
                .handler(c -> {
                    UUID pUUID = getPlayerId(c.get("player"));
                    if(pUUID != null) {
                        getPurchases(c.sender().getSender(), pUUID, 1);
                    }
                }));
    }

    private void getPurchases(CommandSender sender, UUID targetUUID, int page) {
        List<HashMap<String, String>> donations = new ArrayList<>();
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT item_bought, price, date, currency FROM donations WHERE user_id = ?")) {
            ps.setString(1, targetUUID.toString());
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                HashMap<String, String> donation = new HashMap<>();
                donation.put("name", rs.getString(1));
                donation.put("price", String.valueOf(rs.getDouble(2)));
                donation.put("date", rs.getString(3));
                donation.put("currency", rs.getString(4));
                donations.add(donation);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int totalPages = (int) Math.ceil((double) donations.size() / 10);
        if (page > totalPages) {
            page = 1;
        }

        int toDelete = 10 * (page - 1);
        if (toDelete != 0) {
            donations = donations.subList(toDelete, donations.size());
        }

        Component msg = Component.empty();
        msg = msg.append(Component.text("⎯⎯⎯⎯⎯⎯⎯⎯⎯", TextColor.fromHexString("#303050"), TextDecoration.STRIKETHROUGH)
                .append(Component.text(" Purchases ", TextColor.fromHexString("#ff0000"), TextDecoration.BOLD).decoration(TextDecoration.STRIKETHROUGH, false))
                .append(Component.text("⎯⎯⎯⎯⎯⎯⎯⎯⎯", TextColor.fromHexString("#303050"), TextDecoration.STRIKETHROUGH)));

        int i = 1;
        for(HashMap<String, String> donation : donations) {
            if (i == 11) break;
            msg = msg.append(Component.text("\n" + i + ". ", NamedTextColor.GRAY)
                    .append(Component.text(donation.get("name") + " ", TextColor.fromHexString("#ea6c6c"))
                            .append(Component.text(" - ", NamedTextColor.WHITE, TextDecoration.BOLD)
                                    .append(Component.text(donation.get("currency") + " ", TextColor.fromHexString("#565bf0"))
                                            .append(Component.text(donation.get("price") + " ", TextColor.fromHexString("#565bf0")))).decoration(TextDecoration.BOLD, false)))
                    .hoverEvent(HoverEvent.showText(Component.text("Purchased " + donation.get("date"), TextColor.fromHexString("#b83d3d")))));
            i++;
        }
        int prevPage = page - 1;
        int nextPage = page + 1;

        if (page == 1 && page < totalPages) {
            msg = msg.append(Component.text("\n" + page, TextColor.fromHexString("#266d27")).append(Component.text("/", NamedTextColor.GRAY))
                    .append(Component.text(totalPages, TextColor.fromHexString("#266d27")))).append(Component.text(" Next --->", NamedTextColor.GRAY)
                    .hoverEvent(HoverEvent.showText(Component.text(">>>", NamedTextColor.GRAY))).clickEvent(ClickEvent.callback(audience -> getPurchases(sender, targetUUID, nextPage))));
        } else if (page != 1 && page == totalPages) {
            msg = msg.append(Component.text("\n<--- Prev ", NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text("<<<", NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.callback(audience -> getPurchases(sender, targetUUID, prevPage)))).append(Component.text(page, TextColor.fromHexString("#266d27"))
                    .append(Component.text("/", NamedTextColor.GRAY)).append(Component.text(totalPages, TextColor.fromHexString("#266d27"))));
        } else if (page > 1) {
            msg = msg.append(Component.text("\n<--- Prev ", NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text("<<<", NamedTextColor.GRAY)))
                            .clickEvent(ClickEvent.callback(audience -> getPurchases(sender, targetUUID, prevPage))))
                    .append(Component.text(page, TextColor.fromHexString("#266d27")).append(Component.text("/", NamedTextColor.GRAY))
                            .append(Component.text(totalPages, TextColor.fromHexString("#266d27"))))
                    .append(Component.text(" Next --->", NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text(">>>", NamedTextColor.GRAY)))
                            .clickEvent(ClickEvent.callback(audience -> getPurchases(sender, targetUUID, nextPage))));
        }
        sender.sendMessage(msg);
    }
}
