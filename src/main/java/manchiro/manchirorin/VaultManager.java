package manchiro.manchirorin;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * Created by takatronix on 2017/03/04.
 */

public class VaultManager {
    public static Economy economy = null;

    public VaultManager(JavaPlugin plugin) {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Vault plugin is not installed");
            return;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().warning("Can't get vault service");
            return;
        }
        economy = rsp.getProvider();
    }


    /////////////////////////////////////
    //      残高確認
    /////////////////////////////////////
    public double getBalance(OfflinePlayer offline) {
        return economy.getBalance(offline);
    }

    /////////////////////////////////////
    //      残高表示
    /////////////////////////////////////
    public void showBalance(OfflinePlayer offline) {
        double money = getBalance(offline);
        Player player = offline.getPlayer();
        if (offline.isOnline() && player != null) {
            player.sendMessage(ChatColor.YELLOW + "あなたの所持金は$" + (int) money);
        }
    }
    /////////////////////////////////////
    //      引き出し
    /////////////////////////////////////
    public boolean withdraw(OfflinePlayer offline, double money) {
        EconomyResponse resp = economy.withdrawPlayer(offline, money);
        if (!resp.transactionSuccess()) return false;
        Player player = offline.getPlayer();
        if (offline.isOnline() && player != null) {
            player.sendMessage(ChatColor.YELLOW + "$" + money + "支払いました");
        }
        return true;
    }
    /////////////////////////////////////
    //      お金を入れる
    /////////////////////////////////////
    public boolean deposit(OfflinePlayer offline, double money) {
        EconomyResponse resp = economy.depositPlayer(offline, money);
        if (!resp.transactionSuccess()) return false;
        Player player = offline.getPlayer();
        if (offline.isOnline() && player != null) {
            player.sendMessage(ChatColor.YELLOW + "$" + money + "受取りました");
        }
        return true;
    }
}

