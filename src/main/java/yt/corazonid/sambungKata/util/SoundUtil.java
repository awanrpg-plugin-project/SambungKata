package yt.corazonid.sambungKata.util;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundUtil {

    public static void playCorrectAnswer(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
    }

    public static void playCorrectAnswerAll(Iterable<Player> players) {
        for (Player p : players) {
            playCorrectAnswer(p);
        }
    }

    public static void playWrongAnswer(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 0.8f);
    }

    public static void playWrongAnswerAll(Iterable<Player> players, Location arenaCenter) {
        for (Player p : players) {
            playWrongAnswer(p);
            if (arenaCenter != null && arenaCenter.getWorld() != null) {
                p.playSound(arenaCenter, Sound.ENTITY_VILLAGER_NO, 1.0f, 0.8f);
            }
        }
    }

    public static void playGameStart(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
    }

    public static void playGameStartAll(Iterable<Player> players) {
        for (Player p : players) {
            playGameStart(p);
        }
    }

    public static void playGachaSpinTick(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 1.5f);
    }

    public static void playGachaResult(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 1.0f);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
    }

    public static void playPlayerEliminated(Iterable<Player> players) {
        for (Player p : players) {
            p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.0f);
        }
    }

    public static void playGameEnd(Iterable<Player> players) {
        for (Player p : players) {
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
    }

    public static void playCountdownTick(Player player, int secondsLeft) {
        float pitch = 0.5f + (5 - secondsLeft) * 0.2f;
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, pitch);
    }

    public static void playHeartLost(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.7f, 1.0f);
    }

    public static void playTurnStart(Iterable<Player> players) {
        for (Player p : players) {
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7f, 1.0f);
        }
    }
}
