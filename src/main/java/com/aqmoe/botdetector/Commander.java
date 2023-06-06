package com.aqmoe.botdetector;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Locale;

public class Commander implements CommandExecutor {

    private final boolean reject(CommandSender sender) {
        sender.sendMessage(BotDetector.MESSAGE_PREFIX + ChatColor.DARK_RED + "您无权执行此操作");
        return true;
    }

    private final boolean introduce(CommandSender sender) {
        sender.sendMessage(BotDetector.MESSAGE_PREFIX +
                "BotDetector " + ChatColor.GRAY + BotDetector.getInstance().getDescription().getVersion());
        return true;
    }

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(strings.length > 0) {
            String sub = strings[0];
            switch(sub.toLowerCase(Locale.ROOT)) {
                case "reload":
                    if(commandSender.hasPermission("botdetector.reload")) {
                        BotDetector.getInstance().reloadConfig();
                        commandSender.sendMessage(BotDetector.MESSAGE_PREFIX + ChatColor.GREEN + "插件配置重载成功！");
                        return true;
                    } else {
                        return reject(commandSender);
                    }
            }
            return introduce(commandSender);
        } else {
            return introduce(commandSender);
        }
    }
}
