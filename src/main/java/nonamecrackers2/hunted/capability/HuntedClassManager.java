package nonamecrackers2.hunted.capability;

import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import nonamecrackers2.hunted.ability.Ability;
import nonamecrackers2.hunted.entity.HunterEntity;
import nonamecrackers2.hunted.game.HuntedGame;
import nonamecrackers2.hunted.game.HuntedGameManager;
import nonamecrackers2.hunted.huntedclass.HuntedClass;
import nonamecrackers2.hunted.huntedclass.HuntedClassDataManager;
import nonamecrackers2.hunted.init.HuntedCapabilities;
import nonamecrackers2.hunted.init.HuntedPacketHandlers;
import nonamecrackers2.hunted.map.HuntedMap;
import nonamecrackers2.hunted.packet.UpdatePlayerClassManagerPacket;
import nonamecrackers2.hunted.trigger.Trigger;
import nonamecrackers2.hunted.trigger.TriggerContext;

public class HuntedClassManager implements ServerPlayerClassManager
{
	private final ServerPlayer player;
	private Optional<HuntedClass> huntedClass = Optional.empty();
	private boolean requestsUpdate;
	private @Nullable CompoundTag data;
	private @Nullable ResourceLocation mask;
	
	public HuntedClassManager(ServerPlayer player)
	{
		this.player = player;
	}
	
	@Override
	public void update()
	{
		this.setUpdateRequest(false);
		HuntedPacketHandlers.MAIN.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> this.player), new UpdatePlayerClassManagerPacket(this.player.getId(), this.isInGame(), this.hasEscaped(), this.getCurrentClass(), this.getMask()));
		this.player.refreshDisplayName();
		this.player.refreshTabListName();
	}
	
	@Override
	public void tick(ServerLevel level, HuntedGame game)
	{
		this.getCurrentClass().ifPresent(huntedClass -> huntedClass.tick(level, game, this.player, this.getOrCreateTag()));
	}

	@Override
	public Optional<HuntedClass> getCurrentClass() 
	{
		return this.huntedClass;
	}

	@Override
	public void setClass(HuntedClass huntedClass) 
	{
		this.requestsUpdate = true;
		this.huntedClass = Optional.ofNullable(huntedClass);
	}
	
	@Override
	public void begin(HuntedMap map)
	{
		this.clear();
		this.getCurrentClass().ifPresent(huntedClass -> 
		{
			if (map.startForTypes().containsKey(huntedClass.getType()))
			{
				Vec3 pos = Vec3.atBottomCenterOf(map.startForTypes().get(huntedClass.getType()));
				this.player.moveTo(pos);
			}
			else
			{
				this.player.moveTo(Vec3.atBottomCenterOf(map.defaultStartPos()));
			}
			
			for (Ability ability : huntedClass.getAllAbilities())
			{
				if (!ability.isDisabled())
					ability.assign(this.player);
			}
			huntedClass.assignOutfit(this.player);
			if (huntedClass.supportsMask())
				this.mask = HunterEntity.MASKS[this.player.getRandom().nextInt(HunterEntity.MASKS.length)];
		});
	}
	
	@Override
	public void finish() 
	{
		this.clear();
		this.setClass(null);
		this.setTag(null);
		this.mask = null;
	}

	@Override
	public void clear()
	{
		for (EquipmentSlot slot : EquipmentSlot.values())
		{
			ItemStack stack = this.player.getItemBySlot(slot);
			CompoundTag tag = stack.getOrCreateTag();
			if (tag.contains("HuntedGameData"))
			{
				CompoundTag extra = tag.getCompound("HuntedGameData");
				if (extra.getBoolean("IsOutfitItem"))
					this.player.setItemSlot(slot, ItemStack.EMPTY);
			}
		}
		this.huntedClass.ifPresent(huntedClass ->
		{
			for (Ability ability : huntedClass.getAllAbilities())
				ability.clear(this.player);
		});
		for (ItemStack stack : this.player.getInventory().items)
		{
			CompoundTag tag = stack.getOrCreateTag();
			if (tag.contains("HuntedGameData"))
			{
				CompoundTag extra = tag.getCompound("HuntedGameData");
				if (extra.getBoolean("IsRewardItem") || extra.getBoolean("IsGivenItem"))
					this.player.getInventory().removeItem(stack);
			}
		}
	}
	
	public void use(TriggerContext context)
	{
		if (this.isInGame() && !this.hasEscaped() && context.player().equals(this.player))
		{
			this.getCurrentClass().ifPresent(huntedClass -> 
			{
				for (Ability ability : huntedClass.getAbilities())
				{
					if (ability.triggerCriteria().matches(context) && ability.getTrigger().matches(context))
					{
						if (context.hand() != null)
						{
							ItemStack stack = this.player.getItemInHand(context.hand());
							if (!stack.isEmpty() && stack.getOrCreateTag().contains("HuntedGameData"))
							{
								CompoundTag extra = stack.getTagElement("HuntedGameData");
								if (extra.contains("Ability"))
								{
									ResourceLocation id = new ResourceLocation(extra.getString("Ability"));
									if (ability.id().equals(id))
										ability.use(context, this.getOrCreateTagElement(ability.id().toString()));
								}
							}
						}
					}
				}
				for (Ability ability : huntedClass.getPassiveAbilities())
				{
					if (ability.triggerCriteria().matches(context) && ability.getTrigger().matches(context))
						ability.use(context, this.getOrCreateTagElement(ability.id().toString()));
				}
			});
		}
	}
	
	@Override
	public Trigger.Criteria triggerCriteria()
	{
		return Trigger.criteria().player();
	}
	
	public CompoundTag write()
	{
		CompoundTag tag = new CompoundTag();
		this.getCurrentClass().ifPresent(huntedClass -> 
		{
			tag.putString("Class", huntedClass.id().toString());
			CompoundTag classData = new CompoundTag();
			huntedClass.save(classData);
			tag.put("ClassData", classData);
		});
		if (this.data != null)
			tag.put("ExtraData", this.data);
		return tag;
	}
	
	public void read(CompoundTag tag)
	{
		if (tag.contains("Class"))
		{
			ResourceLocation id = new ResourceLocation(tag.getString("Class"));
			HuntedClass huntedClass = HuntedClassDataManager.INSTANCE.get(id);
			if (huntedClass != null)
			{
				HuntedClass copy = huntedClass.copy();
				copy.read(tag.getCompound("ClassData"));
				this.setClass(copy);
			}
		}
		if (tag.contains("ExtraData"))
			this.data = tag.getCompound("ExtraData");
	}
	
	@Override
	public boolean requestsUpdate()
	{
		return this.requestsUpdate;
	}
	
	@Override
	public void setUpdateRequest(boolean flag)
	{
		this.requestsUpdate = flag;
	}
	
	@Override
	public boolean isInGame()
	{
		HuntedGameManager manager = this.player.level.getCapability(HuntedCapabilities.GAME_MANAGER).orElse(null);
		if (manager != null)
		{
			HuntedGame game = manager.getCurrentGame().orElse(null);
			if (game != null && game.getPlayers().contains(this.player) && !game.isPlayerEliminated(this.player))
				return true;
		}
		return false;
	}
	
	@Override
	public boolean hasEscaped()
	{
		HuntedGameManager manager = this.player.level.getCapability(HuntedCapabilities.GAME_MANAGER).orElse(null);
		if (manager != null)
		{
			HuntedGame game = manager.getCurrentGame().orElse(null);
			if (game != null && game.getPlayers().contains(this.player) && game.hasPlayerEscaped(this.player))
				return true;
		}
		return false;
	}
	
	@Override
	public ResourceLocation getMask()
	{
		return this.mask;
	}
	
//	public static @Nullable HuntedClass getClassForPlayer(ServerPlayer player)
//	{
//		HuntedClass huntedClass = null;
//		PlayerClassManager manager = player.getCapability(HuntedCapabilities.PLAYER_CLASS_MANAGER).orElse(null);
//		if (manager != null)
//			huntedClass = manager.getCurrentClass().orElse(null);
//		return huntedClass;
//	}
	
	@Override
	public CompoundTag getTag()
	{
		return this.data;
	}
	
	@Override
	public void setTag(CompoundTag tag)
	{
		this.data = tag;
	}
}
