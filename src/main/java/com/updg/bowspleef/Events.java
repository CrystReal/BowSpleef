package com.updg.bowspleef;

import com.updg.CR_API.Events.BungeeReturnIdEvent;
import com.updg.CR_API.Events.LobbyUpdateCheckEvent;
import com.updg.bowspleef.Models.BowPlayer;
import com.updg.bowspleef.Models.enums.GameStatus;
import  com.updg.CR_API.Utils.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Alex
 * Date: 17.06.13  19:46
 */
public class Events implements Listener {

    int tid = 0;
    int count = 10;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(final PlayerLoginEvent event) {
        Player user = event.getPlayer();
        BowPlayer p = BowSpleefPlugin.game.getPlayer(user.getName());
        if (p == null) {
            p = new BowPlayer(user);
            if (BowSpleefPlugin.game.getStatus() == GameStatus.WAITING) {
                if (BowSpleefPlugin.game.getActivePlayers() < BowSpleefPlugin.game.getMaxPlayers())
                    BowSpleefPlugin.game.addPlayer(p);
                else
                    BowSpleefPlugin.game.addSpectator(p);
            } else {
                BowSpleefPlugin.game.addSpectator(p);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.setJoinMessage(null);
        e.getPlayer().getInventory().clear();
        final BowPlayer p = BowSpleefPlugin.game.getPlayer(e.getPlayer().getName());
        Bukkit.getScheduler().runTaskLaterAsynchronously(BowSpleefPlugin.getInstance(), new Runnable() {
            public void run() {
                int i = 0;
                while (true) {
                    i++;
                    if (i >= 50) {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(BowSpleefPlugin.getInstance(), new Runnable() {
                            public void run() {
                                p.getBukkitModel().kickPlayer("Not logged");
                                BowSpleefPlugin.game.removePlayer(p);
                            }
                        });
                        return;
                    }
                    if (p.getId() == 0)
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    else
                        break;
                }
            }
        }, 1);
        e.getPlayer().teleport(BowSpleefPlugin.game.getLobby());
        if (BowSpleefPlugin.game.getStatus() != GameStatus.WAITING) {
            p.sendMessage(BowSpleefPlugin.prefix + "Игра уже началась.");
        } else {
            if (p.isSpectator()) {
                p.sendMessage(BowSpleefPlugin.prefix + "В игре нет свободных мест. Вы зашли как налюбдающий!");
            } else {
                e.setJoinMessage(BowSpleefPlugin.prefix + e.getPlayer().getName() + " вошел на арену. " + Bukkit.getOnlinePlayers().length + "/" + BowSpleefPlugin.game.getMinPlayers());
                if (BowSpleefPlugin.game.isAbleToStart()) {
                    BowSpleefPlugin.game.preGame();
                } else {
                    Game.sendUpdatesToLobby();
                    e.getPlayer().sendMessage(BowSpleefPlugin.prefix + "Игра начнется когда наберется " + BowSpleefPlugin.game.getMinPlayers() + " " + StringUtil.plural(BowSpleefPlugin.game.getMinPlayers(), "игрок", "игрока", "игроков"));
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        e.setQuitMessage(null);
        if (BowSpleefPlugin.game.getPlayers().containsKey(e.getPlayer().getName())) {
            BowSpleefPlugin.game.killPlayer(e.getPlayer());
        }
        BowPlayer p = BowSpleefPlugin.game.getPlayer(e.getPlayer().getName());
        if (p != null && BowSpleefPlugin.game.getStatus() == GameStatus.WAITING) {
            BowSpleefPlugin.game.removePlayer(p);
            BowSpleefPlugin.game.removeSpectator(p);
        }
        Game.sendUpdatesToLobby();
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();

            if (!p.isDead()) {
                if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
                    BowSpleefPlugin.game.killPlayer(p);
                } else {
                    e.setCancelled(true);
                    p.setHealth(p.getMaxHealth());
                }
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onPickUp(PlayerPickupItemEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        e.setDeathMessage(null);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Block at_foot = e.getPlayer().getWorld().getBlockAt(e.getTo());
        BowPlayer p = BowSpleefPlugin.game.getPlayer(e.getPlayer().getName());
        if (p.isSpectator() && at_foot.getY() == BowSpleefPlugin.floor) {
            Location loc = e.getTo();
            loc.setY(BowSpleefPlugin.floor + 1);
            e.getPlayer().teleport(loc);
            e.getPlayer().sendMessage("Ты не можешь опускаться ниже");
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }

        ((Player) event.getEntity().getShooter()).getInventory().addItem(new ItemStack(Material.ARROW, 1));
        ((Player) event.getEntity().getShooter()).updateInventory();
        BowSpleefPlugin.game.getPlayer(((Player) event.getEntity().getShooter()).getName()).getStats().addShot();
    }

    @EventHandler
    public void onChangeHunger(FoodLevelChangeEvent e) {
        e.setFoodLevel(20);
    }

    @EventHandler
    public void onGetID(BungeeReturnIdEvent e) {
        BowSpleefPlugin.game.getPlayer(e.getUsername()).setId(e.getId());
    }

    @EventHandler
    public void onReq(LobbyUpdateCheckEvent e){
        Game.sendUpdatesToLobby();
    }
}
