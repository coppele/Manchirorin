package manchiro.manchirorin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Timer {
    private final Manchirorin plugin;
    public Timer(Manchirorin plugin) {
        this.plugin = plugin;
    }
    public void betTime(){
        new BukkitRunnable(){
            public int time = plugin.config.getInt("time",120);
            @Override
            public void run() {
                if(!plugin.open || plugin.children.size() == plugin.maxAmount) {
                    cancel();
                    return;
                }

                if (time == 0){
                    MCHData.timeEnd();
                    cancel();
                    return;
                }

                if (time % 60 == 0&&3600 > time){
                    for(Player p: Bukkit.getOnlinePlayers()){
                        p.sendMessage(plugin.PREFIX + " §6募集終了まで残り§e§l" + time/60 + "分" + "§e参加する(必要: "+ (plugin.bet * 5)+")" + "/mch join");
                    }
                }else if ((time % 10 == 0&&60 > time) || time <= 5 ){
                    for(Player p:Bukkit.getOnlinePlayers()){
                        p.sendMessage(plugin.PREFIX + " §6募集終了まで残り§e§l" + time + "秒" + "§e参加する(必要: "+ (plugin.bet * 5)+")" + "/mch join");
                    }
                }

                time--;

            }
        }.runTaskTimer(plugin,0,20);
    }
}
