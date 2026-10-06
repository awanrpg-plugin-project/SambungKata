package yt.corazonid.sambungKata;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import yt.corazonid.sambungKata.command.ModeratorCommand;
import yt.corazonid.sambungKata.listener.ChatListener;
import yt.corazonid.sambungKata.listener.ProtectionListener;
import yt.corazonid.sambungKata.manager.BuildManager;
import yt.corazonid.sambungKata.manager.GameManager;
import yt.corazonid.sambungKata.manager.WordManager;

public final class SambungKata extends JavaPlugin {

    private GameManager gameManager;
    private BuildManager buildManager;
    private WordManager wordManager;

    @Override
    public void onEnable() {
        wordManager = new WordManager(this);
        buildManager = new BuildManager(this);
        gameManager = new GameManager(this, buildManager, wordManager);

        ModeratorCommand modCmd = new ModeratorCommand(this, gameManager);
        String[] cmds = {"regis", "unregis", "listplayer", "listscore", "mode",
                "start", "nextround", "endgame", "resetgame", "skip", "commandinfo"};
        for (String cmd : cmds) {
            PluginCommand pc = getCommand(cmd);
            if (pc != null) {
                pc.setExecutor(modCmd);
                pc.setTabCompleter(modCmd);
            }
        }

        getServer().getPluginManager().registerEvents(new ChatListener(this, gameManager), this);
        getServer().getPluginManager().registerEvents(new ProtectionListener(gameManager), this);

        getLogger().info("SambungKata v" + getPluginMeta().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        if (gameManager != null) {
            gameManager.cleanup();
        }
        getLogger().info("SambungKata plugin disabled!");
    }

    public GameManager getGameManager() { return gameManager; }
    public BuildManager getBuildManager() { return buildManager; }
    public WordManager getWordManager() { return wordManager; }
}
