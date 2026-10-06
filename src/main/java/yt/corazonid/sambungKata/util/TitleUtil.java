package yt.corazonid.sambungKata.util;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

import java.time.Duration;

public final class TitleUtil {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private TitleUtil() {}

    public static void sendTitle(Player player, String title, String subtitle,
                                 int fadeInTicks, int stayTicks, int fadeOutTicks) {
        if (player == null) return;
        Title.Times times = Title.Times.times(
                Duration.ofMillis(fadeInTicks * 50L),
                Duration.ofMillis(stayTicks * 50L),
                Duration.ofMillis(fadeOutTicks * 50L)
        );
        player.showTitle(Title.title(
                LEGACY.deserialize(title == null ? "" : title),
                LEGACY.deserialize(subtitle == null ? "" : subtitle),
                times
        ));
    }
}
