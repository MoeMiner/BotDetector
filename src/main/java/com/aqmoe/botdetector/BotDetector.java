package com.aqmoe.botdetector;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Date;
import java.util.Objects;

public final class BotDetector extends JavaPlugin {

    static BotDetector instance;

    static final String MESSAGE_PREFIX = ChatColor.RED + "BotDetector" + ChatColor.DARK_GRAY + " | " + ChatColor.RESET;

    public static BotDetector getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        Bukkit.getPluginManager().registerEvents(new EventListener(), this);
        Objects.requireNonNull(getCommand("botdetector")).setExecutor(new Commander());
        Objects.requireNonNull(getCommand("botdetector")).setTabCompleter(new CommandTabCompleter());
        getLogger().info("欢迎使用 BotDetector 自动挖矿检测插件");
    }

    @Override
    public void onDisable() {
        getLogger().info("谢谢使用");
    }

    public static void ban(Player player) {
        Bukkit.getScheduler().runTask(getInstance(), () -> {
            Date date = null;
            int period = getInstance().getConfig().getInt("ban-period");
            if(period != -1) {
                date = new Date(System.currentTimeMillis() + 60L * period * 1000);
            }
            Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), "使用自动挖矿工具", date, null);
            Bukkit.getConsoleSender().sendMessage(MESSAGE_PREFIX + ChatColor.YELLOW + "玩家 " + player.getName() + " 被检测到非人转头行为，已被封禁。");
        });
    }

    public static void kick(Player player) {
        Bukkit.getScheduler().runTask(getInstance(), () -> player.kickPlayer(MESSAGE_PREFIX + ChatColor.RED + "请勿使用自动挖矿工具"));
    }

    public static void message(Player player, int time, int maxAllowed) {
        player.sendMessage(MESSAGE_PREFIX + ChatColor.RED + "请休息一分钟再挖矿吧！" + ((time > 10) ? " （距离踢出还有" + (maxAllowed-time) + "次机会）" : ""));
    }

    public static void debug(Player player, String message) {
        if(getInstance().getConfig().getBoolean("debug")) {
            String method = Thread.currentThread().getStackTrace()[2].getMethodName();
            int line = Thread.currentThread().getStackTrace()[2].getLineNumber();
            player.sendMessage(ChatColor.GOLD + method + "#" + line + ": " + ChatColor.WHITE + message);
        }
    }
}
