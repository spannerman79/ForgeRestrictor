package net.kaikk.mc.fr.sponge;

import java.io.IOException;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;

import com.google.inject.Inject;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

@Plugin(id=PluginInfo.id, name = PluginInfo.name, version = PluginInfo.version, description = PluginInfo.description)
public class ForgeRestrictor {
	private static ForgeRestrictor instance;
	private Config config;

	@Inject
	@DefaultConfig(sharedRoot = false)
	private ConfigurationLoader<CommentedConfigurationNode> configManager;

	@Inject
	@ConfigDir(sharedRoot = false)
	private Path configDir;

	@Inject
	private Logger logger;

	public void load() throws Exception {
		this.config = new Config(this);
	}

	@Listener
	public void onServerStart(GameStartedServerEvent event) throws Exception {
		instance = this;

		this.load();
	}

	@Listener
	public void onServerStart(GameReloadEvent event) throws Exception {
		this.load();
	}

	public static ForgeRestrictor instance() {
		return instance;
	}

	public Config config() {
		return config;
	}

	public Logger logger() {
		return logger;
	}

	public static void log(String message) {
		if (instance.logger()!=null) {
			instance.logger().info(message);
		} else {
			System.out.println(message);
		}
	}

	public ConfigurationLoader<CommentedConfigurationNode> getConfigManager() {
		return configManager;
	}

	public Path getConfigDir() {
		return configDir;
	}

	public HoconConfigurationLoader getDataLoader() {
		return HoconConfigurationLoader.builder().setPath(this.configDir.resolve("data.conf")).build();
	}

	synchronized public void loadData() throws IOException, ObjectMappingException {
		HoconConfigurationLoader loader = getDataLoader();
		ConfigurationNode rootNode = loader.load();

	}

	synchronized public void saveData() throws IOException, ObjectMappingException {
		HoconConfigurationLoader loader = getDataLoader();
		ConfigurationNode rootNode = loader.load();


		loader.save(rootNode);
	}
}
