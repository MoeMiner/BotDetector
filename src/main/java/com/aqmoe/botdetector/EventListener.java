package com.aqmoe.botdetector;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class EventListener implements Listener {

    private final HashMap<UUID, Long> miningStartTimes = new HashMap<>();
    private static long MINING_TIME_LIMIT = 15 * 60 * 1000;

    private final HashMap<UUID, Integer> miningCount = new HashMap<>();
    private HashMap<UUID, Integer> violation = new HashMap<>();
    private final HashMap<UUID, Long> lastMiningReset = new HashMap<>();
    private final int requiredBlocksPerMinute = 30;
    private final int minBanViolenceLimit = 20;
    private final int maxKickViolation = 25;

    private final HashMap<UUID, Integer> noConfidencePlayers = new HashMap<>();


    public EventListener() {
        MINING_TIME_LIMIT = (long) BotDetector.getInstance().getConfig().getInt("constantly-mining-threshold") * 60 * 1000;
        new BukkitRunnable() {
            @Override
            public void run() {
                // 每一分钟执行对每个玩家的基本检查
                for(Player player : Bukkit.getOnlinePlayers()) {
                    UUID playerUUID = player.getUniqueId();
                    if(miningStartTimes.containsKey(playerUUID)) {
                        long miningDuration = System.currentTimeMillis() - miningStartTimes.get(playerUUID);
                        if (miningDuration > MINING_TIME_LIMIT) {
                            // 挖矿时长超过怀疑阈值，则在 noConfidencePlayers 不信任玩家 Map 中建立键值对
                            // 键值对建立后，玩家的所有挖掘事件将被取消
                            // 其中 Integer 值为玩家在此阶段继续挖掘的次数，最大不能超过 maxKickViolation 否则 kick
                            noConfidencePlayers.put(playerUUID, 1);
                            Bukkit.getScheduler().runTaskLaterAsynchronously(BotDetector.getInstance(),
                                    /* 60秒后删除该键值对，意味着玩家正常挖掘 */
                                    () -> noConfidencePlayers.remove(playerUUID),
                                    (20*60)
                            );

                            miningStartTimes.remove(player.getUniqueId());
                        }
                    }
                    if(violation.containsKey(player.getUniqueId())) {
                        if(violation.get(player.getUniqueId()) > minBanViolenceLimit) {
                            // violation 代表玩家非法转头积累的 VL，到达限制后 ban
                            BotDetector.kick(player);
                            BotDetector.ban(player);
                        }
                    }
                }
                violation = new HashMap<>();
            }
        }.runTaskTimer(BotDetector.getInstance(), 20, (20*60));
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        // Bypass players with permission
        if(player.hasPermission("botdetector.bypass")) return;

        if(noConfidencePlayers.containsKey(playerUUID)) {
            event.setCancelled(true);
            BotDetector.message(player, noConfidencePlayers.get(playerUUID), maxKickViolation);
            if(noConfidencePlayers.get(playerUUID) > maxKickViolation) {
                BotDetector.kick(player);
            }

            noConfidencePlayers.put(playerUUID, noConfidencePlayers.get(playerUUID) + 1);
        }

        if (OreBlocksFilter.isOreBlock(event.getBlock())) {
            UUID playerId = player.getUniqueId();
            miningCount.put(playerId, miningCount.getOrDefault(playerId, 0) + 1);
            lastMiningReset.putIfAbsent(playerId, System.currentTimeMillis());

            if (!miningStartTimes.containsKey(player.getUniqueId())) {
                miningStartTimes.put(player.getUniqueId(), System.currentTimeMillis());
            }
        }

        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        // 处理 “玩家持续挖矿” 的判断
        // 玩家每分钟挖矿的方块数量必须超过 requiredBlocksPerMinute 否则持续挖矿状态中断
        if (lastMiningReset.containsKey(playerId) && (currentTime - lastMiningReset.get(playerId)) / 60000 >= 1) {
            if (miningCount.getOrDefault(playerId, 0) >= requiredBlocksPerMinute) {
                miningCount.put(playerId, 0);
                lastMiningReset.put(playerId, currentTime);
            } else {
                miningCount.remove(playerId);
                lastMiningReset.remove(playerId);
                miningStartTimes.remove(playerId);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        miningStartTimes.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerConsumeItem(PlayerItemConsumeEvent event) {
        miningStartTimes.remove(event.getPlayer().getUniqueId());
    }


    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Bypass players with permission
        if(player.hasPermission("botdetector.bypass")) return;

        // 检查玩家是否旋转了头部
        if(event.getTo() == null) return;
        if (event.getFrom().getYaw() != event.getTo().getYaw()) {
            float rotationDifference = Math.abs(event.getFrom().getYaw() - event.getTo().getYaw());
            rotationDifference = (rotationDifference > 180) ? 360 - rotationDifference : rotationDifference;

            // 如果旋转角度大于90度，则认为玩家作弊
            if (rotationDifference > 90) {
                event.setCancelled(true);
                violation.put(player.getUniqueId(), violation.getOrDefault(player.getUniqueId(), 0) + 1);
                if(violation.get(player.getUniqueId()) > minBanViolenceLimit) {
                    // violation 代表玩家非法转头积累的 VL，到达限制后立即 ban
                    BotDetector.kick(player);
                    BotDetector.ban(player);
                }
            }
        }
    }
}
