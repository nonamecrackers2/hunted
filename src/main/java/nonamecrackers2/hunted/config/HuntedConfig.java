package nonamecrackers2.hunted.config;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;

public class HuntedConfig
{
	public static final HuntedConfig.Client CLIENT;
	public static final ForgeConfigSpec CLIENT_SPEC;
	public static final HuntedConfig.Server SERVER;
	public static final ForgeConfigSpec SERVER_SPEC;
	
	static
	{
		final Pair<HuntedConfig.Client, ForgeConfigSpec> clientSpecPair = new ForgeConfigSpec.Builder().configure(HuntedConfig.Client::new);
		CLIENT_SPEC = clientSpecPair.getRight();
		CLIENT = clientSpecPair.getLeft();
		final Pair<HuntedConfig.Server, ForgeConfigSpec> serverSpecPair = new ForgeConfigSpec.Builder().configure(HuntedConfig.Server::new);
		SERVER_SPEC = serverSpecPair.getRight();
		SERVER = serverSpecPair.getLeft();
	}
	
	public static class Client
	{
		public final BooleanValue horrorElements;
		
		private Client(ForgeConfigSpec.Builder builder)
		{
			this.horrorElements = builder
					.comment("Specifies if horror elements (Hunter's mask, jumpscares, etc) should be active")
					.translation("hunted.config.server.horrorElements")
					.define("horrorElements", true);
		}
	}
	
	public static class Server
	{
		public final BooleanValue buttonHighlighting;
		
		private Server(ForgeConfigSpec.Builder builder)
		{
			this.buttonHighlighting = builder
					.comment("Specifies if buttons should be highlighted during a game")
					.translation("hunted.config.server.buttonHighlighting")
					.define("buttonHighlighting", false);
		}
	}
}
