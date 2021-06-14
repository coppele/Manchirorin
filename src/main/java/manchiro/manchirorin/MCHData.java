package manchiro.manchirorin;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Iterator;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MCHData {
    public static final SecureRandom random = new SecureRandom();
    public static Iterator<MCHStatus> iterator;
    public static Manchirorin plugin;

    public static void loadEnable(Manchirorin plugin){
        MCHData.plugin = plugin;
    }

    public static void gameStart(Player p, double bet, int hito) {
        if (plugin.owner != null) return;
        plugin.maxAmount = hito;
        plugin.bet = bet;
        plugin.owner = new MCHStatus(p, bet * 5 * hito);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(plugin.PREFIX + " §a§l" + plugin.owner.getName() + "§f§lさんにより§d§l" + plugin.maxAmount + "§f§l人募集の§e§l" + plugin.bet + "円§f§lマンチロが開始されました！§a§l: /mch" + "§e参加する(必要: " + plugin.bet * 5 + ")" + "/mch join");
        }
        plugin.mch = false;
        plugin.timer.betTime();
        plugin.open = true;
    }
    public static void gameStart(Player p, double bet, int hito, int... def) {
       gameStart(p, bet, hito);
       plugin.owner.def = def;
    }

    public static void reset(boolean insertable){
        //plugin.totalBet.transferBalanceToPlayer(p,plugin.bet*5,TransactionCategory.GAMBLE,TransactionType.DEPOSIT,"mcr cancel: "+Bukkit.getPlayer(uuid).getName());
        iterator.forEachRemaining(status -> {
            if (status.quit) return;
            MCHData.deposit(status);
        });
        if (insertable) insert(plugin);
        plugin.children.clear();
        plugin.bet = -1;
        plugin.owner = null;
        plugin.maxAmount = -1;
        plugin.open = false;
        plugin.mch = false;
        Bukkit.broadcastMessage(plugin.PREFIX +" §a§lマンチロが終了しました。");
        plugin.PREFIX = "§f[§d§lマ§a§lン§f§lチロ§r]";
    }

    public static void timeEnd() {
        deposit(plugin.owner);
        Bukkit.broadcastMessage(plugin.PREFIX +" §4§l子が集まらなかったため中止しました。");
        iterator = plugin.children.iterator();
        reset(false);
    }

    public static void cancel(){
        deposit(plugin.owner);
        iterator = plugin.children.iterator();
        reset(false);
    }

    public static void gamePush(){
        plugin.mch = true;
        plugin.open = false;
        iterator = plugin.children.iterator();
        Bukkit.broadcastMessage(plugin.PREFIX +" §a§lマンチロがスタートしました！");
        sendKankeisya(" §a§l"+plugin.owner.getName()+"§f§lさん(親)がサイコロを振っています…§e§l§kaaa");
        int[] dice = new int[3];
        for (int i = 0; i < 3; i++) {
            dice[i] = plugin.owner.def != null ? plugin.owner.def[i] : random.nextInt(6) + 1;
        }
        new BukkitRunnable(){
            @Override
            public void run() {
                if (!plugin.mch) return;
                String result = Stream.of(dice).map(Arrays::toString).collect(Collectors.joining("・"));
                sendKankeisya(" §a§lﾊﾟｶｯ！  §f§l " + result + " ！！");
                MCHRole role = MCHRole.getRole(dice[0], dice[1], dice[2]);
                plugin.owner.role = role;
                if (role == MCHRole.NONE) {
                    sendKankeisya(" §a§l役無し (ﾟ∀ﾟ)ｷﾀｺﾚ!!");
                    childturn();
                } else if (role == MCHRole.DAN5) {
                    sendKankeisya(" §6§lダンゴ ｷﾀ━━━━(ﾟ∀ﾟ)━━━━!!");
                    oyaLose(2.0);
                } else if (role == MCHRole.MAN10) {
                    sendKankeisya(" §c§lマンジュウ (ﾟ∀ﾟ)ｷﾀｺﾚ!!");
                    oyaWin(2.0);
                } else if (role == MCHRole.DOUBLET) {
                    sendKankeisya(" §4§lゾロメ ｷﾀ━━━━(ﾟ∀ﾟ)━━━━!!");
                    oyaWin(3.0);
                } else if (role == MCHRole.JACKPOT) {
                    Bukkit.broadcastMessage(plugin.PREFIX + "§0§l§kaaaaa§4§lJ§6§lA§e§lC§a§lK§2§lP§b§lO§3§lT§0§l§kaaaaa§6§l§n§o ｷﾀ━━━━(ﾟ∀ﾟ)━━━━!!");
                    Bukkit.broadcastMessage(plugin.PREFIX + "§e§l結果: §a§l親の勝利！！");
                    double jack = Math.min(plugin.jackpot, plugin.bet * 10);
                    plugin.takeJackpot(jack);
                    plugin.owner.balance += jack;
                    //plugin.vault.givePlayerMoney(plugin.parent,jack,TransactionType.DEPOSIT,"mcr jackpot!! user: "+Bukkit.getPlayer(plugin.parent).getName());
                    sendTitle("§0§l§kaaaaa§4§lJ§6§lA§e§lC§a§lK§2§lP§b§lO§3§lT§0§l§kaaaaa","§e§l当選者: §f§l"+plugin.owner.getName()+" §6§l当選金額: §f§l"+jack+" 円",100);
                    deposit(plugin.owner);
                    //plugin.vault.transferMoneyPoolToPlayer(plugin.totalBet.getId(),plugin.parent,plugin.parentbal,TransactionCategory.GAMBLE,TransactionType.WIN,"mcr jackpot!! user: "+Bukkit.getPlayer(plugin.parent).getName());
                    Bukkit.broadcastMessage(plugin.PREFIX +" §a§l"+plugin.owner.getName()+"§f§l: §e§l"+plugin.owner.wager +" 円 → "+plugin.owner.balance+" 円");
                    reset(true);
                } else if (role == MCHRole.SAIKO) {
                    sendKankeisya(" §a§l§nサイコー (ﾟ∀ﾟ)ｷﾀｺﾚ!!");
                    oyaWin(4);
                } else if (role == MCHRole.MAN5RORIN) {
                    sendKankeisya(" §4§l§nマンコロリン 来ちゃった(∀｀*ゞ)ﾃﾍｯ");
                    gotoJackpot();
                } else if (role == MCHRole.HIFUMI) {
                    if (plugin.owner.getName().equals("Mohaimen_Ksr")) {
                        Bukkit.broadcastMessage(plugin.PREFIX +" §d§l"+plugin.owner.getName()+"が§d§lｻｷﾒｻﾝを出した(+･`ω･´)ｷﾘｯ");
                    }
                    sendKankeisya(" §a§lﾀﾞｰ!!");
                    oyaWin(2.5);
                } else if (role.isNumberRole()) {
                    sendKankeisya(" §f§l" + role + "が役に決まりました！");
                    childturn();
                } else {
                    Bukkit.broadcastMessage(plugin.PREFIX +"§4エラー発生。未知の目です。");
                    reset(false);
                }
            }
        }.runTaskLater(plugin,100);
    }

    public static void childturn(){
        sendKankeisya(" §c§l子のターンが開始されました！");
        new BukkitRunnable(){
            @Override
            public void run() {
                while (plugin.mch && iterator.hasNext()) {
                    MCHStatus status = iterator.next();
                    if (status.quit) continue;
                    childbattle(status);
                    return;
                }
                cancel();
                if (!plugin.mch) return;
                Bukkit.broadcastMessage(plugin.PREFIX + " §a§l" + plugin.owner.getName() + "§f§l: §e§l" + plugin.bet * 5 * plugin.maxAmount + " 円 → " + plugin.owner.balance + " 円§e(うち手数料" + plugin.owner.balance / 100 + " 円)");
                plugin.owner.balance -= plugin.owner.balance / 100;
                deposit(plugin.owner);
                //plugin.vault.transferMoneyPoolToPlayer(plugin.totalBet.getId(),plugin.parent,plugin.parentbal - (plugin.parentbal/100),TransactionCategory.GAMBLE,TransactionType.DEPOSIT,"mcr parent deposit: "+Bukkit.getPlayer(plugin.parent).getName());
                plugin.addJackpot(plugin.owner.wager / 100);
                reset(true);
            }
        }.runTaskTimer(plugin,20,120);
    }

    public static void childbattle(MCHStatus status){
        sendKankeisya(" §c§l"+status.getName()+"§f§lさん(子)がサイコロを振っています…§e§l§kaaa");
        int[] dice = new int[3];
        for (int i = 0; i < 3; i++) {
            dice[i] = status.def != null ? status.def[i] : random.nextInt(6) + 1;
        }
        new BukkitRunnable(){
            @Override
            public void run() {
                if (!plugin.mch || status.quit) return;
                String result = Stream.of(dice).map(Arrays::toString).collect(Collectors.joining("・"));
                sendKankeisya(" §a§lﾊﾟｶｯ！  §f§l " + result + " ！！");
                MCHRole role = MCHRole.getRole(dice[0], dice[1], dice[2]);
                status.role = role;
                if (role == MCHRole.NONE || role == MCHRole.SAIKO) {
                    sendKankeisya(" §a§l役無し (ﾟ∀ﾟ)ｷﾀｺﾚ!!");
                    if (plugin.owner.role.role == 0) draw(status);
                    else vsOya(true, status, 1.0);
                } else if (role == MCHRole.DAN5) {
                    sendKankeisya(" §6§lダンゴ ｷﾀ━━━━(ﾟ∀ﾟ)━━━━!!");
                    vsOya(true,status,2.0);
                } else if (role == MCHRole.MAN10) {
                    sendKankeisya(" §c§lマンジュウ (ﾟ∀ﾟ)ｷﾀｺﾚ!!");
                    vsOya(false,status,2.0);
                } else if (role == MCHRole.DOUBLET) {
                    sendKankeisya(" §4§lゾロメ ｷﾀ━━━━(ﾟ∀ﾟ)━━━━!!");
                    vsOya(false,status,3.0);
                } else if (role == MCHRole.JACKPOT) {
                    Bukkit.broadcastMessage(plugin.PREFIX +" §0§l§kaaaaa§4§lJ§6§lA§e§lC§a§lK§2§lP§b§lO§3§lT§0§l§kaaaaa§6§l§n§o ｷﾀ━━━━(ﾟ∀ﾟ)━━━━!!");
                    Bukkit.broadcastMessage(plugin.PREFIX +" §e§l結果: §a§l子の勝利！！");
                    double jack = Math.min(plugin.jackpot, plugin.bet * 10);
                    plugin.takeJackpot(jack);
                    status.balance += jack;
                    deposit(status);
                    //plugin.vault.givePlayerMoney(uuid,jack,TransactionType.WIN,"mcr jackpot!! deposit: "+Bukkit.getPlayer(uuid).getName());
                    sendTitle("§0§l§kaaaaa§4§lJ§6§lA§e§lC§a§lK§2§lP§b§lO§3§lT§0§l§kaaaaa","§e§l当選者: §f§l"+status.getName()+" §6§l当選金額: §f§l"+jack+" 円",100);
                    //plugin.vault.transferMoneyPoolToPlayer(plugin.totalBet.getId(),uuid,plugin.onebet*5,TransactionCategory.GAMBLE,TransactionType.DEPOSIT,"mcr jackpot!! deposit: "+Bukkit.getPlayer(uuid).getName());
                    Bukkit.broadcastMessage(plugin.PREFIX +" §a§l"+status.getName()+"§f§l: §e§l"+status.wager+" 円 → "+ status.balance +" 円");
                    reset(true);
                } else if (role == MCHRole.MAN5RORIN) {
                    sendKankeisya(" §4§l§nマンコロリン 来ちゃった(∀｀*ゞ)ﾃﾍｯ");
                    gotoJackpot();
                } else if (role == MCHRole.HIFUMI) {
                    if (status.getName().equals("Mohaimen_Ksr")) {
                        Bukkit.broadcastMessage(plugin.PREFIX +" §d§lMohaimen_Ksrがｻｷﾒｻﾝを出した(+･`ω･´)ｷﾘｯ");
                    }
                    sendKankeisya(" §a§lﾀﾞｰ!!");
                    vsOya(false,status,2.5);
                } else if (role.isNumberRole()) {
                    if (role.role == 1 || role.role == 6) sendKankeisya(" §c§l" + role + " (ﾟ∀ﾟ)ｷﾀｺﾚ!!");
                    else sendKankeisya(" §f§l" + role + "が役に決まりました！");
                    if (plugin.owner.role.role == 6) draw(status);
                    else vsOya(plugin.owner.role.role > role.role, status, 1.0);
                } else {
                    Bukkit.broadcastMessage(plugin.PREFIX +"§4エラー発生。未知の目です。");
                    reset(false);
                }
            }
        }.runTaskLater(plugin,100);
    }

    public static void oyaWin(double bairitu){
        Bukkit.broadcastMessage(plugin.PREFIX +" §e§l結果: §a§l親の勝利！！");
        double retn = plugin.bet / 100;
        iterator.forEachRemaining(status -> {
            status.balance -= plugin.bet * bairitu;
            Bukkit.broadcastMessage(plugin.PREFIX +" §c§l"+status.getName()+"§f§l: §e§l"+status.wager+" 円 → "+ status.balance +" 円§e(うち手数料"+retn+" 円)");
            status.balance -= retn;
            deposit(status);
            //plugin.vault.transferMoneyPoolToPlayer(plugin.totalBet.getId(),uuid,retn,TransactionCategory.GAMBLE,TransactionType.DEPOSIT,"mcr lose return: "+Bukkit.getPlayer(uuid).getName());
            plugin.addJackpot(retn);
        });
        plugin.owner.balance += plugin.bet * bairitu * plugin.maxAmount;
        double with = plugin.owner.balance / 100;
        //plugin.vault.transferMoneyPoolToPlayer(plugin.totalBet.getId(),plugin.parent,plugin.parentbal - (plugin.parentbal/100),TransactionCategory.GAMBLE,TransactionType.DEPOSIT,"mcr parent deposit: "+Bukkit.getPlayer(plugin.parent).getName());
        Bukkit.broadcastMessage(plugin.PREFIX +" §a§l"+plugin.owner.getName()+"§f§l: §e§l"+plugin.owner.wager +" 円 → "+plugin.owner.balance +" 円§e(うち手数料"+with+" 円)");
        plugin.addJackpot(with);
        plugin.owner.balance -= with;
        deposit(plugin.owner);
        reset(true);
    }
    public static void oyaLose(double bairitu){
        Bukkit.broadcastMessage(plugin.PREFIX +" §e§l結果: §c§l子の勝利！！");
        double retn = plugin.bet * bairitu;
        iterator.forEachRemaining(status -> {
            status.balance += retn;
            Bukkit.broadcastMessage(plugin.PREFIX +" §c§l"+status.getName()+"§f§l: §e§l"+status.wager+ " 円 → "+ status.balance +" 円§e(うち手数料"+ plugin.bet/100+" 円)");
            status.balance -= plugin.bet / 100;
            plugin.addJackpot(status.balance);
            deposit(status);
            //plugin.vault.transferMoneyPoolToPlayer(plugin.totalBet.getId(),uuid,with + (plugin.onebet * 5) - (plugin.onebet/100) ,TransactionCategory.GAMBLE,TransactionType.DEPOSIT,"mcr win: "+Bukkit.getPlayer(uuid).getName());
        });
        plugin.owner.balance -= retn * plugin.maxAmount;
        double with = plugin.owner.balance /100;
        //plugin.vault.transferMoneyPoolToPlayer(plugin.totalBet.getId(),plugin.parent,plugin.parentbal - (plugin.parentbal/100),TransactionCategory.GAMBLE,TransactionType.DEPOSIT,"mcr parent deposit: "+Bukkit.getPlayer(plugin.parent).getName());
        Bukkit.broadcastMessage(plugin.PREFIX +" §a§l"+plugin.owner.getName() +"§f§l: §e§l"+ plugin.owner.wager +" 円 → "+plugin.owner.balance +" 円§e(うち手数料"+with+" 円)");
        plugin.addJackpot(with);
        plugin.owner.balance -= with;
        deposit(plugin.owner);
        reset(true);
    }

    public static void vsOya(boolean Oyawin,MCHStatus status,double bairitu){
        double with = plugin.bet * bairitu;
        if(Oyawin){
            Bukkit.broadcastMessage(plugin.PREFIX +" §e§l結果: §a§l親の勝利！！");
            plugin.owner.balance += with;
            status.balance -= with;
            //plugin.vault.transferMoneyPoolToPlayer(plugin.totalBet.getId(),uuid,retn - (plugin.onebet/100),TransactionCategory.GAMBLE,TransactionType.DEPOSIT,"mcr lose return: "+Bukkit.getPlayer(uuid).getName());
        }else{
            Bukkit.broadcastMessage(plugin.PREFIX +" §e§l結果: §c§l子の勝利！！");
            plugin.owner.balance -= with;
            status.balance += with;
            //plugin.vault.transferMoneyPoolToPlayer(plugin.totalBet.getId(),uuid,with + (plugin.onebet * 5) - (plugin.onebet/100),TransactionCategory.GAMBLE,TransactionType.WIN,"mcr win: "+Bukkit.getPlayer(uuid).getName());
        }
        Bukkit.broadcastMessage(plugin.PREFIX +" §c§l"+status.getName()+"§f§l: §e§l"+ status.wager +" 円 → "+ status.balance +" 円§e(うち手数料"+ plugin.bet/100 +" 円)");
        status.balance -= plugin.bet/100;
        deposit(status);
        plugin.addJackpot(plugin.bet/100);
    }

    public static void draw(MCHStatus status){
        Bukkit.broadcastMessage(plugin.PREFIX +" §e§l結果: §a§l引き分け！！");
        Bukkit.broadcastMessage(plugin.PREFIX +" §c§l"+status.getName()+"§f§l: §e§l"+ status.wager +" 円 → "+ status.balance+" 円");
        deposit(status);
        //plugin.vault.transferMoneyPoolToPlayer(plugin.totalBet.getId(),uuid,plugin.onebet*5,TransactionCategory.GAMBLE,TransactionType.DEPOSIT,"mcr draw: "+Bukkit.getPlayer(uuid).getName());
    }

    public static void sendTitle(String main,String sub, int inout, int time, String sound){
        for(Player player : Bukkit.getOnlinePlayers()){
            player.playSound(player.getLocation(), sound,1,1);
            player.sendTitle(main,sub,inout, time,inout);
        }
    }
    public static void sendTitle(String main,String sub,int time){
        for(Player player : Bukkit.getOnlinePlayers()){
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN,1,1);
            player.sendTitle(main,sub,10, time,10);
        }
    }

    public static void sendKankeisya(String message){
        for (MCHStatus status : plugin.children) {
            status.player.sendMessage(plugin.PREFIX + message);
        }
        plugin.owner.player.sendMessage(plugin.PREFIX + message);
    }

    public static void gotoJackpot(){
        Bukkit.broadcastMessage(plugin.PREFIX +" §e§l結果: §4§l全 員 敗 北");
        while (iterator.hasNext()) iterator.next();
        for (MCHStatus status : plugin.children) {
            status.role = MCHRole.MAN5RORIN;
            Bukkit.broadcastMessage(plugin.PREFIX +" §c§l"+status.getName()+"§f§l: §e§l"+status.wager+"円 → "+"§e(全額手数料" + status.balance + "円)");
            plugin.addJackpot(status.wager);
            status.balance = 0;
        }
        Bukkit.broadcastMessage(plugin.PREFIX +" §a§l"+plugin.owner.getName()+"§f§l: §e§l"+plugin.owner.wager +"円 → "+"§e(全額手数料"+ plugin.owner.balance +"円)");
        plugin.addJackpot(plugin.owner.wager);
        sendTitle("§4§k§laa§r§4§l全 員 敗 北§4§k§laa§r", "", 20, 200, "slot.dq_zenmetsu");
        plugin.owner.balance = 0;
        reset(true);
    }

    private static void insert(Manchirorin plugin) {
        double bet = plugin.owner.wager;
        double wager = plugin.bet * plugin.maxAmount + bet;
        boolean debug = plugin.PREFIX.contains("Debug");
        String mcid = plugin.owner.getName();
        UUID uuid = plugin.owner.getUniqueId();
        double balance = plugin.owner.balance;
        String role = plugin.owner.role == null ? MCHRole.NONE.name() : plugin.owner.role.name();
        String ko = plugin.children.stream().map(MCHStatus::toString).collect(Collectors.joining(", "));
        new BukkitRunnable() {
            @Override
            public void run() {
                MySQLManager mysql = new MySQLManager(plugin);
                mysql.execute("insert into manchiro_log (wager, oya_mcid, oya_uuid, oya_bet, oya_bal, oya_role, ko, debug) values(" +
                        wager + ", '" + mcid + "', '" + uuid + "', " + bet + ", " + balance + ", '" + role + "', '" + ko + "', " + debug + ")");
                mysql.close();
            }
        }.runTask(plugin);
    }

    public static void deposit(MCHStatus status) {
        if (status.balance == 0) return;
        plugin.vault.deposit(status.player, status.balance);
    }
}
