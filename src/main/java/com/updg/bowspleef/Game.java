package com.updg.bowspleef;

import com.updg.CR_API.MQ.senderUpdatesToCenter;
import com.updg.bowspleef.Models.BowPlayer;
import com.updg.bowspleef.Models.enums.GameStatus;
import com.updg.bowspleef.Threads.TopBarThread;
import com.updg.bowspleef.Utils.EconomicSettings;
import com.updg.bowspleef.Utils.L;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created by Alex
 * Date: 17.06.13  20:26
 */
public class Game {
    private HashMap<String, BowPlayer> players = new HashMap<String, BowPlayer>();
    private HashMap<String, BowPlayer> spectators = new HashMap<String, BowPlayer>();

    private Location lobby;
    private Location spawn;
    private GameStatus status;
    private int minPlayers = 0;
    private int maxPlayers = 12;

    public int tillGameDefault = 15;
    public int tillGame = 15;

    public BowPlayer winner;
    private int tid;

    private long timeStart = 0;
    private long timeEnd = 0;

    public Game() {
        this.lobby = BowSpleefPlugin.getInstance().stringToLoc(BowSpleefPlugin.getInstance().getConfig().getString("lobby"));
        this.spawn = BowSpleefPlugin.getInstance().stringToLoc(BowSpleefPlugin.getInstance().getConfig().getString("spawn"));
        this.minPlayers = BowSpleefPlugin.getInstance().getConfig().getInt("minPlayers");
        this.maxPlayers = BowSpleefPlugin.getInstance().getConfig().getInt("maxPlayers");
    }

    public static void sendUpdatesToLobby() {
        String s = GameStatus.WAITING.toString();
        if (BowSpleefPlugin.game.getMaxPlayers() <= BowSpleefPlugin.game.getActivePlayers())
            s = "IN_GAME";
        if (BowSpleefPlugin.game.getStatus() == GameStatus.WAITING) {
            if (BowSpleefPlugin.game.tillGame < BowSpleefPlugin.game.tillGameDefault)
                senderUpdatesToCenter.send(BowSpleefPlugin.serverId + ":" + s + ":" + "В ОЖИДАНИИ" + ":" + BowSpleefPlugin.game.getActivePlayers() + ":" + BowSpleefPlugin.game.getMaxPlayers() + ":До игры " + BowSpleefPlugin.game.tillGame + " c.");
            else
                senderUpdatesToCenter.send(BowSpleefPlugin.serverId + ":" + s + ":" + "В ОЖИДАНИИ" + ":" + BowSpleefPlugin.game.getActivePlayers() + ":" + BowSpleefPlugin.game.getMaxPlayers() + ":Набор игроков");
        } else if (BowSpleefPlugin.game.getStatus() == GameStatus.PRE_GAME)
            senderUpdatesToCenter.send(BowSpleefPlugin.serverId + ":IN_GAME:" + "НАЧАЛО" + ":" + BowSpleefPlugin.game.getActivePlayers() + ":" + BowSpleefPlugin.game.getMaxPlayers());
        else if (BowSpleefPlugin.game.getStatus() == GameStatus.POSTGAME) {
            senderUpdatesToCenter.send(BowSpleefPlugin.serverId + ":IN_GAME:" + "ИГРА ОКОНЧЕНА" + ":" + BowSpleefPlugin.game.getActivePlayers() + ":" + BowSpleefPlugin.game.getMaxPlayers() + ":Победил " + BowSpleefPlugin.game.winner.getName());
        } else if (BowSpleefPlugin.game.getStatus() == GameStatus.INGAME || BowSpleefPlugin.game.getStatus() == GameStatus.POSTGAME)
            senderUpdatesToCenter.send(BowSpleefPlugin.serverId + ":IN_GAME:" + "ИГРА" + ":" + BowSpleefPlugin.game.getActivePlayers() + ":" + BowSpleefPlugin.game.getMaxPlayers() + ":Бой");
        else if (BowSpleefPlugin.game.getStatus() == GameStatus.RELOAD)
            senderUpdatesToCenter.send(BowSpleefPlugin.serverId + ":DISABLED:" + "ОФФЛАЙН" + ":0:0:");

    }

    public boolean isAbleToStart() {
        return this.status == GameStatus.WAITING && this.players.size() >= this.minPlayers;
    }

    public void getReady() {
        this.status = GameStatus.WAITING;
        new TopBarThread().start();
        sendUpdatesToLobby();
    }

    public void preGame() {
        if (status != GameStatus.WAITING)
            return;
        status = GameStatus.PRE_GAME;
        tid = Bukkit.getScheduler().scheduleSyncRepeatingTask(BowSpleefPlugin.getInstance(), new Runnable() {
            public void run() {
                if (Bukkit.getOnlinePlayers().length < getMinPlayers()) {
                    Bukkit.broadcastMessage(BowSpleefPlugin.prefix + "Старт игры отменен так как игрок(и) покинули сервер.");
                    sendUpdatesToLobby();
                    Bukkit.getScheduler().cancelTask(tid);
                    status = GameStatus.WAITING;
                    tid = 0;
                    tillGame = tillGameDefault;
                    sendUpdatesToLobby();
                } else if (tillGame > 0) {
                    tillGame--;
                } else {
                    Bukkit.getScheduler().cancelTask(tid);
                    BowSpleefPlugin.game.startGame();
                }
            }
        }, 0, 20);
    }

    public void startGame() {
        this.status = GameStatus.PRE_GAME;
        for (BowPlayer p : this.players.values()) {
            p.getBukkitModel().setGameMode(GameMode.SURVIVAL);
            p.getBukkitModel().setFlying(false);
            p.getBukkitModel().setAllowFlight(false);
            p.getBukkitModel().teleport(getSpawn());
            p.getBukkitModel().getInventory().clear();
            p.setWasInGame(true);
        }
        Bukkit.broadcastMessage(BowSpleefPlugin.prefix + ChatColor.RED + "Разбегайся! До начала резни всего 10 секунд!");
        this.tillGame = 10;
        Bukkit.getScheduler().cancelTask(tid);
        tid = Bukkit.getScheduler().scheduleSyncRepeatingTask(BowSpleefPlugin.getInstance(), new Runnable() {
            public void run() {
                tillGame--;
                if (tillGame == 0) {
                    for (BowPlayer p : players.values()) {
                        p.provideStaff();
                    }
                    Bukkit.broadcastMessage(BowSpleefPlugin.prefix + ChatColor.RED + "БОЙ!");
                    timeStart = System.currentTimeMillis() / 1000;
                    status = GameStatus.INGAME;
                    Bukkit.getScheduler().cancelTask(tid);
                }
            }
        }, 0, 20);
        sendUpdatesToLobby();
    }

    public Location getSpawn() {
        spawn.setPitch(12);
        spawn.setYaw(-90);
        return spawn;
    }

    public void endGame() {
        if (this.status == GameStatus.INGAME && this.players.size() < 2) {
            this.timeEnd = System.currentTimeMillis() / 1000L;
            for (BowPlayer p : this.players.values()) {
                p.sendMessage(BowSpleefPlugin.prefix + "Ты выиграл бой!");
                p.sendMessage(BowSpleefPlugin.prefixMoney + "+" + EconomicSettings.win + " опыта.");
                p.getStats().setInGameTime(System.currentTimeMillis() / 1000L - this.timeStart);
                winner = p;
                winner.addExp(EconomicSettings.win);
            }
            for (Player p1 : Bukkit.getOnlinePlayers()) {
                if (winner != null && !p1.getName().equals(winner.getName())) {
                    p1.sendMessage(BowSpleefPlugin.prefix + "Игрок " + winner.getName() + " выиграл!");
                }
            }
        } else {
            for (BowPlayer p : this.players.values()) {
                p.sendMessage(BowSpleefPlugin.prefix + "Игра остановлена системой.");
            }
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(BowSpleefPlugin.prefix + "Сервер перезагрузится через 15 секунд.");
            p.getInventory().clear();
        }
        new Thread(
                new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(5000);
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                p.sendMessage(BowSpleefPlugin.prefix + "Сервер перезагрузится через 10 секунд.");
                            }
                            Thread.sleep(5000);
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                p.sendMessage(BowSpleefPlugin.prefix + "Сервер перезагрузится через 5 секунд.");
                            }
                            Thread.sleep(5000);
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                p.sendMessage(BowSpleefPlugin.prefix + "Сервер перезагружается.");
                            }
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        BowSpleefPlugin.getInstance().sendStats();
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        sendUpdatesToLobby();
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");
                    }
                }).start();
    }

    public void killPlayer(Player p) {
        if ((this.status == GameStatus.INGAME || this.status == GameStatus.PRE_GAME) && this.players.containsKey(p.getName())) {
            BowPlayer pl = this.players.get(p.getName());
            for (Player p1 : Bukkit.getOnlinePlayers()) {
                if (!p1.getName().equals(p.getName())) {
                    p1.sendMessage(BowSpleefPlugin.prefix + p.getName() + " выбыл.");
                }
            }
            pl.sendMessage(BowSpleefPlugin.prefix + "Ты погиб в бою.");
            pl.getStats().setPosition(this.players.size());
            pl.getStats().setInGameTime(System.currentTimeMillis() / 1000L - this.timeStart);
            pl.getBukkitModel().closeInventory();
            pl.getBukkitModel().getInventory().clear();
            this.players.remove(pl.getName());
            this.spectators.put(pl.getName(), pl);
            pl.setSpectator(true);
            pl.getBukkitModel().teleport(getLobby());

            for (BowPlayer p1 : this.players.values()) {
                p1.addExp(EconomicSettings.anotherDie);
                p1.sendMessage(BowSpleefPlugin.prefixMoney + "+" + EconomicSettings.anotherDie + " опыта.");
            }

            if (this.players.size() < 2) {
                BowSpleefPlugin.game.endGame();
            }
        } else {
            p.teleport(getLobby());
        }
        sendUpdatesToLobby();
    }

    public Location getLobby() {
        return this.lobby;
    }

    public GameStatus getStatus() {
        return status;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public int getActivePlayers() {
        return this.players.size();
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public BowPlayer getPlayer(String name) {
        if (this.players.containsKey(name))
            return this.players.get(name);
        if (this.spectators.containsKey(name))
            return this.spectators.get(name);
        return null;
    }

    public void addSpectator(BowPlayer p) {
        this.spectators.put(p.getName(), p);
    }

    public boolean isSpectator(BowPlayer p) {
        return this.spectators.containsKey(p.getName());
    }

    public void addPlayer(BowPlayer p) {
        this.players.put(p.getName(), p);
    }

    public HashMap<String, BowPlayer> getPlayers() {
        return players;
    }

    public void setSpawn(Location spawn) {
        this.spawn = spawn;
    }

    public long getTimeStart() {
        return timeStart;
    }

    public long getTimeEnd() {
        return timeEnd;
    }

    public Collection<BowPlayer> getActivePlayersArray() {
        return this.players.values();
    }

    public Collection<BowPlayer> getSpectatorsArray() {
        return this.spectators.values();
    }

    public void removeSpectator(BowPlayer p) {
        if (p.isSpectator())
            this.spectators.remove(p.getName());
    }

    public void removePlayer(BowPlayer p) {
        if (this.players.containsKey(p.getName()))
            this.players.remove(p.getName());
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }
}
