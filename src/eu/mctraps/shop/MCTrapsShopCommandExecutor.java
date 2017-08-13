package eu.mctraps.shop;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public class MCTrapsShopCommandExecutor implements CommandExecutor {
    private final MCTrapsShop plugin;

    public MCTrapsShopCommandExecutor(MCTrapsShop plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("smsshop")) {
            if (args.length == 0) {
                sender.sendMessage("§7Glowne komendy pluginu:");
                sender.sendMessage(" §7/voucher §6<list;add;remove>");

                return true;
            }
            return false;
        }
        if (cmd.getName().equalsIgnoreCase("voucher")) {
            if ((!(sender instanceof org.bukkit.entity.Player)) || (sender.hasPermission("tools.voucher"))) {
                String vTable = this.plugin.config.getString("tables.vouchers");
                String oTable = this.plugin.config.getString("tables.offers");
                String hTable = this.plugin.config.getString("tables.history");

                if (args.length == 0) {
                    return false;
                }
                if (args[0].equalsIgnoreCase("list")) {
                    if (args.length == 1) {
                        try {
                            ResultSet result = this.plugin.statement.executeQuery("SELECT * FROM " + vTable);
                            List<String> vouchers = new ArrayList();
                            while (result.next()) {
                                String code = result.getString("code");
                                int uses = result.getInt("uses");
                                int id = result.getInt("id");
                                String voucher = " §a" + code + " (#" + id + ")";
                                vouchers.add(voucher);
                            }

                            sender.sendMessage("§7Lista aktualnych voucherów:");
                            for (int i = 0; i < vouchers.size(); i++) {
                                sender.sendMessage((String) vouchers.get(i));
                            }
                            sender.sendMessage("§9Aby dowiedziec sie wiecej o danym voucherze, uzyj komendy: §7/voucher info §6<id>");
                        } catch (SQLException e) {
                            e.printStackTrace();
                            sender.sendMessage("§cWystapil blad podczas laczenia z baza danych");
                        }

                        return true;
                    }
                    sender.sendMessage("§cPoprawne uzycie: §7/voucher list");
                    return true;
                }
                if ((args[0].equalsIgnoreCase("info")) &&
                        (args.length == 2)) {
                    try {
                        ResultSet result = this.plugin.statement.executeQuery("SELECT * FROM " + vTable + " WHERE id = '" + args[1] + "' ORDER BY uses DESC LIMIT 1");
                        int id = 0;
                        int uses = 0;
                        int offerid = 0;
                        int timed = 0;
                        String code = "";
                        String timedText = "";
                        String end = "";
                        while (result.next()) {
                            id = result.getInt("id");
                            code = result.getString("code");
                            uses = result.getInt("uses");
                            offerid = result.getInt("offer");
                            timed = result.getInt("timed");
                            timedText = timed == 1 ? "tak" : "nie";
                            end = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(result.getTimestamp("endtime"));
                        }

                        ResultSet offerResult = this.plugin.statement.executeQuery("SELECT * FROM " + oTable + " WHERE id = '" + offerid + "' LIMIT 1");
                        String offername = "Brak usługi o takim ID";
                        while (offerResult.next()) {
                            offername = offerResult.getString("name");
                        }

                        sender.sendMessage("§7Informacje o voucherze §6#" + id);
                        sender.sendMessage(" §9Kod: §7" + code);
                        sender.sendMessage(" §9Pozostale uzycia: §7" + uses);
                        sender.sendMessage(" §9Kod do uslugi: §7#" + offerid + " §7(§c" + offername + "§7)");
                        sender.sendMessage(" §9Oferta ograniczona czasowo?: §7" + timedText);
                        if (timed == 1) {
                            sender.sendMessage(" §9Oferta wazna do: §7" + end);
                        }

                        return true;
                    } catch (SQLException e) {
                        e.printStackTrace();
                        sender.sendMessage("§cWystapil blad w trakcie laczenia z baza danych");
                        return true;
                    }
                }
            } else {
                sender.sendMessage("§cNie masz dostepu! :(");
            }
        }

        return false;
    }
}