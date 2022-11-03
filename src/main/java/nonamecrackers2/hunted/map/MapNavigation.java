package nonamecrackers2.hunted.map;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import nonamecrackers2.hunted.util.HuntedUtil;

public record MapNavigation(List<BlockPos> nodes)
{
	public static MapNavigation fromJson(JsonObject object)
	{
		ImmutableList.Builder<BlockPos> positions = ImmutableList.builder();
		if (object.has("nodes"))
		{
			JsonArray array = GsonHelper.getAsJsonArray(object, "nodes");
			for (int i = 0; i < array.size(); i++)
				positions.add(BlockPos.CODEC.parse(JsonOps.INSTANCE, array.get(i)).resultOrPartial(HuntedUtil::throwJSE).get());
		}
		return new MapNavigation(positions.build());
	}
	
	public void toPacket(FriendlyByteBuf buffer)
	{
		buffer.writeCollection(this.nodes, FriendlyByteBuf::writeBlockPos);
	}
	
	public static MapNavigation fromPacket(FriendlyByteBuf buffer)
	{
		List<BlockPos> path = buffer.readList(FriendlyByteBuf::readBlockPos);
		return new MapNavigation(ImmutableList.copyOf(path));
	}
}
