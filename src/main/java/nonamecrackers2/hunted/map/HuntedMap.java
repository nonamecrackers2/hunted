package nonamecrackers2.hunted.map;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import nonamecrackers2.hunted.huntedclass.type.HuntedClassType;
import nonamecrackers2.hunted.map.event.MapEventHolder;
import nonamecrackers2.hunted.map.overlay.HuntedOverlay;
import nonamecrackers2.hunted.registry.HuntedRegistries;
import nonamecrackers2.hunted.rewards.ButtonReward;

public record HuntedMap(ResourceLocation id, Component name, Map<HuntedClassType, BlockPos> startForTypes, BlockPos defaultStartPos, List<BlockPos> buttons, List<ButtonReward> rewards, AABB boundary, List<AABB> preyExits, List<MapEventHolder> events, List<BlockPos> keyholes, Optional<HuntedOverlay.ConfiguredOverlay<?>> overlay, List<BlockPos> revivalPositions, int buttonPressingDelay, Optional<AmbienceSettings> ambience, Optional<MapNavigation> navigation) 
{
	public void toPacket(FriendlyByteBuf buffer)
	{
		buffer.writeResourceLocation(this.id);
		buffer.writeComponent(this.name);
		buffer.writeVarInt(this.startForTypes.size());
		this.startForTypes.forEach((type, pos) -> 
		{
			buffer.writeRegistryId(HuntedRegistries.HUNTED_CLASS_TYPES.get(), type);
			buffer.writeBlockPos(pos);
		});
		buffer.writeBlockPos(this.defaultStartPos);
		buffer.writeVarInt(this.buttons.size());
		this.buttons.forEach(buffer::writeBlockPos);
		aabbToPacket(this.boundary, buffer);
		buffer.writeCollection(this.preyExits, (b, aabb) -> aabbToPacket(aabb, b));
		buffer.writeVarInt(this.keyholes.size());
		this.keyholes.forEach(buffer::writeBlockPos);
		buffer.writeVarInt(this.revivalPositions.size());
		this.revivalPositions.forEach(buffer::writeBlockPos);
		buffer.writeVarInt(this.buttonPressingDelay);
		buffer.writeBoolean(this.ambience.isPresent());
		this.ambience.ifPresent(ambience -> ambience.toPacket(buffer));
		buffer.writeOptional(this.navigation, (b, nav) -> nav.toPacket(b));
	}
	
	public static HuntedMap fromPacket(FriendlyByteBuf buffer)
	{
		ResourceLocation id = buffer.readResourceLocation();
		Component name = buffer.readComponent();
		ImmutableMap.Builder<HuntedClassType, BlockPos> startForTypes = ImmutableMap.builder();
		int startForTypesSize = buffer.readVarInt();
		for (int i = 0; i < startForTypesSize; i++)
			startForTypes.put(buffer.readRegistryId(), buffer.readBlockPos());
		BlockPos defaultStart = buffer.readBlockPos();
		ImmutableList.Builder<BlockPos> buttons = ImmutableList.builder();
		int buttonsSize = buffer.readVarInt();
		for (int i = 0; i < buttonsSize; i++)
			buttons.add(buffer.readBlockPos());
		AABB boundary = packetToAabb(buffer);
		List<AABB> preyExits = ImmutableList.copyOf(buffer.readList(b -> packetToAabb(b)));
		ImmutableList.Builder<BlockPos> keyholes = ImmutableList.builder();
		int keyholesSize = buffer.readVarInt();
		for (int i = 0; i < keyholesSize; i++)
			keyholes.add(buffer.readBlockPos());
		ImmutableList.Builder<BlockPos> revivalPositions = ImmutableList.builder();
		int revivalPositionsSize = buffer.readVarInt();
		for (int i = 0; i < revivalPositionsSize; i++)
			revivalPositions.add(buffer.readBlockPos());
		int buttonPressingDelay = buffer.readVarInt();
		Optional<AmbienceSettings> ambience = Optional.empty();
		if (buffer.readBoolean())
			ambience = Optional.of(AmbienceSettings.fromPacket(buffer));
		Optional<MapNavigation> nav = buffer.readOptional(MapNavigation::fromPacket);
		return new HuntedMap(id, name, startForTypes.build(), defaultStart, buttons.build(), ImmutableList.of(), boundary, preyExits, ImmutableList.of(), keyholes.build(), Optional.empty(), revivalPositions.build(), buttonPressingDelay, ambience, nav);
	}
	
	private static void aabbToPacket(AABB box, FriendlyByteBuf buffer)
	{
		buffer.writeDouble(box.minX);
		buffer.writeDouble(box.minY);
		buffer.writeDouble(box.minZ);
		buffer.writeDouble(box.maxX);
		buffer.writeDouble(box.maxY);
		buffer.writeDouble(box.maxZ);
	}
	
	private static AABB packetToAabb(FriendlyByteBuf buffer)
	{
		return new AABB(buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
	}
	
	public HuntedMap copyWithRewards(List<ButtonReward> rewards)
	{
		return new HuntedMap(this.id, this.name, this.startForTypes, this.defaultStartPos, this.buttons, ImmutableList.copyOf(rewards), this.boundary, this.preyExits, this.events, this.keyholes, this.overlay, this.revivalPositions, this.buttonPressingDelay, this.ambience, this.navigation);
	}
}
