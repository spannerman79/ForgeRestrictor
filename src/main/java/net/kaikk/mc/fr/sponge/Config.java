package net.kaikk.mc.fr.sponge;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;

public class Config {
	public Config(ForgeRestrictor instance) throws Exception {
		//load defaults
		Asset asset = Sponge.getAssetManager().getAsset(instance, "config.conf").get();
		HoconConfigurationLoader defaultsLoader = HoconConfigurationLoader.builder().setURL(asset.getUrl()).build();
		ConfigurationNode defaults = defaultsLoader.load();

		//load config & merge defaults
		ConfigurationNode rootNode = instance.getConfigManager().load();
		rootNode.mergeValuesFrom(defaults);
		instance.getConfigManager().save(rootNode);


	}
}

