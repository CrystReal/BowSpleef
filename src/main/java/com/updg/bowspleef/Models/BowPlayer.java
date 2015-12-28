package com.updg.bowspleef.Models;

import com.updg.CR_API.Bungee.Bungee;
import com.updg.CR_API.DataServer.DSUtils;
import com.updg.bowspleef.BowSpleefPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * Created by Alex
 * Date: 15.12.13  13:37
 */
public class BowPlayer {
    private int id;
    private String name;

    private Player bukkitModel;
    private BowPlayerStats stats;

    private double exp = 0;

    private boolean wasInGame = false;

    public BowPlayer(Player p) {
        this.setBukkitModel(p);
        this.name = p.getName();
        this.stats = new BowPlayerStats();
        this.getIdFromBungee();
    }

    private void getIdFromBungee() {
        Bungee.isLogged(getBukkitModel(), getName());
    }

    public Player getBukkitModel() {
        return bukkitModel;
    }

    public void setBukkitModel(Player bukkitModel) {
        this.bukkitModel = bukkitModel;
    }

    public BowPlayerStats getStats() {
        return stats;
    }

    public void provideStaff() {
        ItemStack iStack, iArrow;
        iStack = new ItemStack(Material.BOW, 1);
        iArrow = new ItemStack(Material.ARROW, 1);
        int efId = 51;
        int enLev = 1;
        int efIdS = 50;
        int enLevS = 100;
        Enchantment infin = new EnchantmentWrapper(efId);
        iStack.addUnsafeEnchantment(infin, enLev);
        Enchantment unbreak = new EnchantmentWrapper(efIdS);
        iStack.addUnsafeEnchantment(unbreak, enLevS);
        PlayerInventory inv = getBukkitModel().getInventory();
        inv.setItem(0, iStack);
        inv.setItem(9, iArrow);
    }

    public void sendMessage(String s) {
        getBukkitModel().sendMessage(s);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setSpectator(boolean b) {
        if (b) {
            this.hidePlayer();
            this.bukkitModel.setAllowFlight(true);
        } else {
            this.showPlayer();
            this.bukkitModel.setAllowFlight(false);
        }
    }

    private void hidePlayer() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.hidePlayer(this.bukkitModel);
        }
    }

    private void showPlayer() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showPlayer(this.bukkitModel);
        }
    }

    public boolean isSpectator() {
        return BowSpleefPlugin.game.isSpectator(this);
    }


    public double getExp() {
        String[] out = DSUtils.getExpAndMoney(getBukkitModel());
        this.setExp(Double.parseDouble(out[0]));
        return exp;
    }

    public void setExp(double exp) {
        this.exp = exp;
    }

    public void withdrawExp(double v) {
        String[] out = DSUtils.withdrawPlayerExpAndMoney(getBukkitModel(), v, 0);
        this.setExp(Double.parseDouble(out[0]));
    }

    public void addExp(double v) {
        String[] out = DSUtils.addPlayerExpAndMoney(getBukkitModel(), v, 0);
        this.setExp(Double.parseDouble(out[0]));
    }

    public boolean wasInGame() {
        return wasInGame;
    }

    public void setWasInGame(boolean wasInGame) {
        this.wasInGame = wasInGame;
    }
}
