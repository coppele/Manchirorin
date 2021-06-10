package manchiro.manchirorin;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.ArrayList;
import java.util.List;

public final class Manchirorin extends JavaPlugin implements Listener {

    String PREFIX = "§f[§d§lマ§a§lン§f§lチロ§r]";
    String PERM = "manchiro.op", SWITCH = "manchiro.switch";
    boolean mch = false, game = false, power = true;
    MCHStatus owner;
    List<MCHStatus> children;
    int maxAmount;
    double bet, jackpot;
    Timer timer;
    VaultManager vault;
    MySQLManager mysql;
    FileConfiguration config;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("起動しました");
        MCHData.loadEnable(this);
        children = new ArrayList<>();
        vault = new VaultManager(this);
        mysql = new MySQLManager(this);
        if (VaultManager.economy == null || !mysql.isConnected()) return;
        timer = new Timer(this);
        saveDefaultConfig();
        config = getConfig();
        jackpot = config.getDouble("jackpot");
        Bukkit.getServer().getPluginManager().registerEvents(this,this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§a§lクライアントでのみ実行できます");
            return true;
        }
        double totalBet = children.size() * bet;
        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("mch")) {
            if (args.length == 0) {
                player.sendMessage("§f==========" + PREFIX + "§f==========");
                if(owner == null) player.sendMessage("§4§l現在マンチロは行われていません");
                else {
                    player.sendMessage("§e§lマンチロが行われています！！ §a§l親: " + owner.getName());
                    player.sendMessage("§eベット金額: " + bet + " 円" + " 必要金額: " + bet * 5 + " 円");
                    player.sendMessage("§a§l募集人数: §e" + maxAmount + "人 §2§l参加人数: §e" + children.size() + "人 §e§l合計賭け金: " + totalBet + " 円");
                }
                player.sendMessage("§a§l/mch new §e§l[金額] [人数]§r: §f§l親としてマンチロを開始します");
                player.sendMessage("§a§l/mch join §r: §f§l子として開催中のマンチロに参加します");
                player.sendMessage("§a§l/mch rule §r: §f§lマンチロのルールを表示します");
                player.sendMessage("§4§lJ§6§lA§e§lC§a§lK§2§lP§b§lO§3§lT§f§l: §e§l"+ jackpot +"円");
                player.sendMessage("§d§lCreated by Mohaimen_Ksr");
                if (player.hasPermission(PERM)) {
                    player.sendMessage("§f========== §c§lOP §r§f==========");
                    player.sendMessage("§c§l/mch cancel : 現在開催しているゲームをキャンセルします");
                }
                if (player.hasPermission(SWITCH)) player.sendMessage("§c§l/mch on/off : マンチロを使用できるようにするかしないか");
                if (player.hasPermission(PERM)) {
                    player.sendMessage("§c§l/mch reset : 親と子をnullにします");
                    player.sendMessage("§c§l/mch new [金額] [人数] [ダイス1] [ダイス2] [ダイス3] : 予め出るダイスを設定します(デバック)");
                    player.sendMessage("§c§l/mch join [ダイス1] [ダイス2] [ダイス3] : 予め出るダイスを設定します(デバック)");
                    player.sendMessage("§c§l/mch debug : 親が子としてマンチロに参加します");
                    player.sendMessage("debugコマンドはかなり不安定・お金が増えるので多用は非推奨");
                }
                return true;
            }

            if (args[0].equals("rule")) {
                player.sendMessage("§f==========" + PREFIX + "§f==========");
                player.sendMessage("§6役一覧: [1:1:1 jackpotチャンス] [それ以外の三つ揃い ゾロメ]");
                player.sendMessage("§6[出目合計10 man10] [1・2・3 イチ・ニ・サン・ﾀﾞｰ!!] [出目合計5 dan5]");
                player.sendMessage("§6役一覧: [二つそろって残りが・・ その数字が強さ]");
                player.sendMessage("");
                player.sendMessage("§e配当率: 『サイコー!!:4倍勝(親のみ)』");
                player.sendMessage("§e配当率: 『イチ・ニ・サン・ﾀﾞｰ!! 2.5倍勝』 『ゾロメ:3倍勝』");
                player.sendMessage("§e『man10:2倍勝』『dan5:2倍負』 通常:1倍負/勝");
                player.sendMessage("§ejackpotの払い出し金額: 賭け金x10 or jackpotすべて のどちらか金額が低いほう");
                return true;
            }

            //new マンチロのゲームを開始↓
            if (args[0].equals("new")) {
                if (!power) {
                    player.sendMessage(PREFIX + " §c現在マンチロがストップしています");
                    return true;
                }
                if (args.length < 3 && !hasPerm(PERM, player)) {
                    player.sendMessage(PREFIX + " §c引数の数が違っています");
                    return true;
                }
                if (game) {
                    player.sendMessage(PREFIX + " §c現在マンチロが開始されています");
                    return true;
                }
                try {
                    bet = Double.parseDouble(args[1]);
                    maxAmount = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    player.sendMessage(PREFIX + " §c金額と人数は数字で入力してください");
                    return true;
                }
                if (bet < 50000) {
                    player.sendMessage(PREFIX + " §c金額は50000円以上で入力してください");
                    return true;
                }
                if (vault.getBalance(player) < bet * maxAmount * 5) {
                    if (maxAmount <= 0 || maxAmount >= 11) {
                        player.sendMessage(PREFIX + " §c募集人数は1人以上10人以下で入力してください");
                        return true;
                    }
                    player.sendMessage(PREFIX + " §c必要金額を持っていません §r必要金額: " + "§r" + bet * 5 * maxAmount + "円");
                    return true;
                }
                if (maxAmount <= 0 || maxAmount >= 11) {
                    player.sendMessage(PREFIX + " §c募集人数は1人以上10人以下で入力してください");
                    return true;
                }
                if (args.length == 6) {
                    PREFIX += "§a§l#Debug§r";
                    MCHData.gameStart(player, bet, maxAmount, Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]));
                } else MCHData.gameStart(player, bet, maxAmount);
                vault.withdraw(player, bet * 5 * maxAmount);
                return true;
            }

            //join マンチロのゲームに参加↓
            if (args[0].equals("join")) {
                if (!power) {
                    player.sendMessage(PREFIX + " §c現在マンチロがストップしています");
                    return true;
                }
                if (mch) {
                    player.sendMessage(PREFIX + " §c現在ゲーム中です");
                    return true;
                }
                if (!game) {
                    player.sendMessage(PREFIX + " §c現在マンチロは開催されていません");
                    return true;
                }
                if (owner.player.equals(player)) {
                    player.sendMessage(PREFIX + " §cあなたは親のため参加できません");
                    return true;
                }
                if (children.stream().map(MCHStatus::getUniqueId).anyMatch(player.getUniqueId()::equals)) {
                    player.sendMessage(PREFIX + " §cあなたは既に参加しています");
                    return true;
                }
                if (vault.getBalance(player) < bet * 5) {
                    player.sendMessage(PREFIX + " §c所持金が足りません §r必要金額: " + bet * 5 + "円");
                    return true;
                }
                MCHStatus status = new MCHStatus(player, bet * 5);
                String suffix;
                if (PREFIX.contains("Debug")) {
                    suffix = "さんがマンチロ#Debugに参加しました！";
                    if (args.length == 4 && hasPerm(PERM, player)) {
                        status.def = new int[]{Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3])};
                        player.sendMessage(PREFIX + "あなたのダイスは " + status.def[0] + "・" + status.def[1] + "・" + status.def[2] + "です");
                    }
                } else suffix = "さんがマンチロに参加しました！";
                children.add(status);
                vault.withdraw(player, bet * 5);
                Bukkit.broadcastMessage(PREFIX + " " + player.getName() + suffix);
                if (children.size() == maxAmount) {
                    MCHData.gamePush();
                }
                return true;
            }

            //cancel ゲームの中断 op専用
            if (args[0].equals("cancel") && hasPerm(PERM, player)) {
                if (game) {
                    player.sendMessage(PREFIX + " キャンセルしました");
                    MCHData.cancel();
                } else {
                    player.sendMessage(PREFIX + " 既にキャンセルしています");
                }
                return true;
            }

            //on マンチロ起動 op専用
            if (args[0].equals("on") && hasPerm(SWITCH, player)) {
                if (!power) {
                    player.sendMessage(PREFIX + " マンチロをONにしました");
                    power = true;
                    return true;
                }
                player.sendMessage("既にONになっています");
                return true;
            }

            //off マンチロ停止 op専用
            if (args[0].equals("off") && hasPerm(SWITCH, player)) {
                if (power) {
                    player.sendMessage(PREFIX + " マンチロをOFFにしました");
                    power = false;
                    return true;
                }
                player.sendMessage("既にOFFになっています");
                return true;
            }

            //debug 親が子としてゲームに参加 op専用 #不安定#
            if (args[0].equals("debug") && hasPerm(PERM, player)) {
                children.add(new MCHStatus(player, bet * 5));
                Bukkit.broadcastMessage(player.getDisplayName() + "さんがマンチロに参加しました！");
                MCHData.gamePush();
                return true;
            }

            //reset 親と子をnullにする op専用
            if (args[0].equals("reset") && hasPerm(PERM, player)) {
                MCHData.reset();
            } else {
                player.sendMessage(PREFIX + " §c使い方が間違っています");
                player.sendMessage(PREFIX + " §c/mch と入力するとコマンド一覧が見れます");
                return true;
            }
        }
        return true;
    }

    @EventHandler
    //サーバーから抜けたときゲームをキャンセル
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if (owner.player.equals(player)) {
            Bukkit.broadcastMessage(PREFIX +"§4§l親("+player.getDisplayName()+"§4§l)がサーバーから退出したためキャンセルします");
            MCHData.cancel();
        } else if (children.stream().map(MCHStatus::getUniqueId).anyMatch(player.getUniqueId()::equals)) {
            Bukkit.broadcastMessage(PREFIX +"§4§l子("+player.getDisplayName()+"§4§l)がサーバーから退出したためキャンセルします");
            MCHData.cancel();
        }
    }

    //上からジャックポットに追加・ジャックポットから引く
    public void addJackpot(Double d){
        config.set("jackpot",jackpot+d);
        saveConfig();
        jackpot = jackpot + d;
    }

    public void takeJackpot(Double d){
        config.set("jackpot",jackpot-d);
        saveConfig();
        jackpot = jackpot - d;
    }

    //Permissionがないときの処理
    public boolean hasPerm(String perm, Player player) {
        if (player.hasPermission(perm)) return true;
        player.sendMessage(PREFIX + " §cあなたには権限がありません");
        return false;
    }
}