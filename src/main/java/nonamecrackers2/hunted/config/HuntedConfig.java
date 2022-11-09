package nonamecrackers2.hunted.config;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;
import net.minecraftforge.fml.DistExecutor;
import nonamecrackers2.hunted.client.gui.KioskTutorialStep;

public class HuntedConfig
{
	public static @Nullable HuntedConfig.Client CLIENT;
	public static @Nullable ForgeConfigSpec CLIENT_SPEC;
	public static final HuntedConfig.Server SERVER;
	public static final ForgeConfigSpec SERVER_SPEC;
	
	static
	{
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> 
		{
			final Pair<HuntedConfig.Client, ForgeConfigSpec> clientSpecPair = new ForgeConfigSpec.Builder().configure(HuntedConfig.Client::new);
			CLIENT_SPEC = clientSpecPair.getRight();
			CLIENT = clientSpecPair.getLeft();
		});
		final Pair<HuntedConfig.Server, ForgeConfigSpec> serverSpecPair = new ForgeConfigSpec.Builder().configure(HuntedConfig.Server::new);
		SERVER_SPEC = serverSpecPair.getRight();
		SERVER = serverSpecPair.getLeft();
	}
	
	public static class Client
	{
		public final BooleanValue horrorElements;
		public final EnumValue<KioskTutorialStep> tutorialStep;
		
		private Client(ForgeConfigSpec.Builder builder)
		{
			this.horrorElements = builder
					.comment("Specifies if horror elements (Hunter's mask, jumpscares, etc) should be active")
					.translation("hunted.config.server.horrorElements")
					.define("horrorElements", true);
			
			this.tutorialStep = builder
					.defineEnum("tutorialStep", KioskTutorialStep.START);
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
