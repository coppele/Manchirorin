package manchiro.manchirorin;

import org.bukkit.entity.Player;

import java.util.UUID;

public class MCHStatus {
    public final Player player;
    public final double wager;
    public double balance;
    public MCHRole role = null;
    public int[] def = null; // debug専用です

    public MCHStatus(Player player, double wager) {
        this.player = player;
        this.wager = wager;
        this.balance = wager;
    }

    public String getName() {
        return player.getName();
    }
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public String toString() {
        return String.format("{name=%s, uuid=%s, bet=%s, bal=%s, role=%s}",
                getName(), getUniqueId(), wager, balance, role == null ? MCHRole.NONE : role.name());
    }
}
