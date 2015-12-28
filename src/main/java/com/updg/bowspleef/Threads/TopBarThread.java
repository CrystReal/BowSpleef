package com.updg.bowspleef.Threads;

import com.updg.bowspleef.BowSpleefPlugin;
import com.updg.bowspleef.Models.enums.GameStatus;
import  com.updg.CR_API.Utils.StringUtil;
import me.confuser.barapi.BarAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Created by Alex
 * Date: 06.12.13  16:14
 */
public class TopBarThread extends Thread implements Runnable {
    public void run() {
        while (true) {
            try {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (BowSpleefPlugin.game.getStatus() == GameStatus.WAITING) {
                        if (BowSpleefPlugin.game.tillGame != 15)
                            BarAPI.setMessage(p, ChatColor.GREEN + "До игры" + StringUtil.plural(BowSpleefPlugin.game.tillGame, " осталась " + BowSpleefPlugin.game.tillGame + " секунда", " осталось " + BowSpleefPlugin.game.tillGame + " секунды", " осталось " + BowSpleefPlugin.game.tillGame + " секунд") + ".", (float) BowSpleefPlugin.game.tillGame / ((float) BowSpleefPlugin.game.tillGameDefault / 100F));
                        else if (BowSpleefPlugin.game.getActivePlayers() < BowSpleefPlugin.game.getMinPlayers())
                            BarAPI.setMessage(p, ChatColor.GREEN + "Ожидаем игроков.", (float) BowSpleefPlugin.game.getActivePlayers() * ((float) BowSpleefPlugin.game.getMinPlayers() / 100F));
                        else
                            BarAPI.setMessage(p, ChatColor.GREEN + "Ожидаем игроков.", 100F);
                    }
                    if (BowSpleefPlugin.game.getStatus() == GameStatus.PRE_GAME) {
                        BarAPI.setMessage(p, ChatColor.RED + "Разбегайся! До резни" + StringUtil.plural(BowSpleefPlugin.game.tillGame, " осталась " + BowSpleefPlugin.game.tillGame + " секунда", " осталось " + BowSpleefPlugin.game.tillGame + " секунды", " осталось " + BowSpleefPlugin.game.tillGame + " секунд") + ".", (float) BowSpleefPlugin.game.tillGame / (10F / 100F));
                    }
                    if (BowSpleefPlugin.game.getStatus() == GameStatus.INGAME) {
                        BarAPI.setMessage(p, ChatColor.GREEN + "Бой", (float) BowSpleefPlugin.game.getActivePlayers() * ((float) BowSpleefPlugin.game.getMinPlayers() / 100F));
                    }
                    if (BowSpleefPlugin.game.getStatus() == GameStatus.POSTGAME) {
                        BarAPI.setMessage(p, ChatColor.AQUA + "Победил " + BowSpleefPlugin.game.winner.getName(), 100F);
                    }
                }
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
