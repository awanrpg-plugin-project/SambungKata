package yt.corazonid.sambungKata.util;

import yt.corazonid.sambungKata.SambungKata;
import yt.corazonid.sambungKata.model.GamePlayer;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mannequin;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public final class MannequinUtil {

    private static final String SEAT_TAG_PREFIX = "sambungkata_seat_";

    private MannequinUtil() {}

    public static void spawnForPlayer(SambungKata plugin, GamePlayer gp, Location seatLoc, float yaw) {
        if (seatLoc.getWorld() == null) return;

        float straightYaw = normalizeYaw(yaw);
        String seatTag = seatTag(gp.getUuid());
        Location spawnLoc = seatLoc.clone();

        ArmorStand stand = seatLoc.getWorld().spawn(spawnLoc, ArmorStand.class, entity -> {
            entity.setInvisible(true);
            entity.setSmall(true);
            entity.setInvulnerable(true);
            entity.setGravity(false);
            entity.setBasePlate(false);
            entity.setArms(false);
            entity.setDisabledSlots(EquipmentSlot.values());
            entity.setRotation(straightYaw, 0f);
            entity.addScoreboardTag(seatTag);
        });

        Mannequin mannequin = seatLoc.getWorld().spawn(spawnLoc, Mannequin.class, entity -> {
            entity.setProfile(ResolvableProfile.resolvableProfile(
                    Bukkit.createProfile(gp.getUuid(), gp.getName())));
            entity.setImmovable(true);
            entity.setInvulnerable(true);
            entity.setGravity(false);
            entity.setRotation(straightYaw, 0f);
        });

        stand.addPassenger(mannequin);
        tagEntity(plugin, stand, gp.getName());
        tagEntity(plugin, mannequin, gp.getName());
        gp.setSeatEntity(stand);
        gp.setMannequin(mannequin);
    }

    private static String seatTag(UUID uuid) {
        return SEAT_TAG_PREFIX + uuid.toString().replace("-", "");
    }

    private static float normalizeYaw(float yaw) {
        float n = yaw % 360f;
        if (n < 0) n += 360f;
        return n;
    }

    private static void tagEntity(SambungKata plugin, Entity entity, String name) {
        entity.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "sambungkata_mannequin"),
                PersistentDataType.STRING,
                name
        );
    }
                                                       }
