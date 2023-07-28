package nonamecrackers2.hunted.client.gui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import nonamecrackers2.hunted.HuntedMod;
import nonamecrackers2.hunted.ability.Ability;
import nonamecrackers2.hunted.client.gui.component.FormattedOptionsList;
import nonamecrackers2.hunted.client.gui.component.QueuedPlayerList;
import nonamecrackers2.hunted.client.gui.component.SimpleDataManagerList;
import nonamecrackers2.hunted.client.gui.component.TextElementList;
import nonamecrackers2.hunted.config.HuntedConfig;
import nonamecrackers2.hunted.game.HuntedGame;
import nonamecrackers2.hunted.huntedclass.HuntedClass;
import nonamecrackers2.hunted.huntedclass.HuntedClassDataManager;
import nonamecrackers2.hunted.huntedclass.type.HuntedClassType;
import nonamecrackers2.hunted.init.HuntedClassTypes;
import nonamecrackers2.hunted.init.HuntedPacketHandlers;
import nonamecrackers2.hunted.map.HuntedMap;
import nonamecrackers2.hunted.map.HuntedMapDataManager;
import nonamecrackers2.hunted.packet.BeginGamePacket;
import nonamecrackers2.hunted.packet.JoinGamePacket;
import nonamecrackers2.hunted.packet.LeaveGamePacket;
import nonamecrackers2.hunted.packet.PickGameModePacket;
import nonamecrackers2.hunted.packet.RequestMenuUpdatePacket;
import nonamecrackers2.hunted.packet.SelectMapPacket;
import nonamecrackers2.hunted.packet.SetButtonHighlightingPacket;
import nonamecrackers2.hunted.packet.StopGameCountdownPacket;
import nonamecrackers2.hunted.packet.UpdateGameMenuPacket;
import nonamecrackers2.hunted.registry.HuntedRegistries;
import nonamecrackers2.hunted.resources.SimpleDataManager;
import nonamecrackers2.hunted.rewards.ButtonReward;
import nonamecrackers2.hunted.util.EventType;
import nonamecrackers2.hunted.util.HuntedClassSelector;

public class HuntedGameMenuScreen extends Screen
{
	private static final Logger LOGGER = LogManager.getLogger();
	public static final ResourceLocation WINDOW = HuntedMod.resource("textures/gui/window.png");
	public static final ResourceLocation SIDE_TABS = HuntedMod.resource("textures/gui/side_tabs.png");
	public static final ResourceLocation TAB_ICONS = HuntedMod.resource("textures/gui/icons.png");
	public static final ResourceLocation SCROLL_MENU_WINDOW = HuntedMod.resource("textures/gui/scroll_menu_window.png");
	public static final ResourceLocation LOGO = HuntedMod.resource("textures/gui/logo.png");
	public static final ResourceLocation ARROWS = HuntedMod.resource("textures/gui/arrows.png");
	public static final int SIDE_TAB_DISABLED = 0;
	public static final int SIDE_TAB_ENABLED = 32;
	public static final int SIDE_TAB_TOP = 0;
	public static final int SIDE_TAB_MIDDLE = 28;
	public static final int SIDE_TAB_BOTTOM = 56;
	public static final int SIDE_TAB_LENGTH = 28;
	public static final int SIDE_TAB_WIDTH = 32;
	public static final int SIDE_TAB_HIGHLIGHTED = SIDE_TAB_WIDTH * 2; 
	public static final int WINDOW_WIDTH = 256;
	public static final int WINDOW_HEIGHT = 171;
	public static final int WINDOW_BORDER = 13;
	public static final int SCROLL_WINDOW_WIDTH = 154;
	public static final int SCROLL_WINDOW_HEIGHT = WINDOW_HEIGHT;
	public static final int SCROLL_WINDOW_LARGE_BORDER = 32;
	public static final int LIST_WIDTH = SCROLL_WINDOW_WIDTH - WINDOW_BORDER*2;
	public static final int BUTTON_WINDOW_Y = WINDOW_HEIGHT;
	public static final int BUTTON_WINDOW_HEIGHT = 32;
	public static final int BUTTON_WINDOW_END = BUTTON_WINDOW_Y + BUTTON_WINDOW_HEIGHT;
	public static final int BUTTON_WINDOW_END_WIDTH = 4;
	public static final int LOGO_WIDTH = 200;
	public static final int LOGO_HEIGHT = 96;
	public static final int ARROW_WIDTH = 14;
	public static final int ARROW_HEIGHT = 22;
	public static final int ARROW_HIGHLIGHTED = ARROW_HEIGHT;
	public static final int ARROW_LEFT = ARROW_WIDTH;
	public final List<MenuTab> menus = Lists.newArrayList();
	private @Nullable MenuTab menu;
	private HuntedClassSelector.Builder selector = HuntedClassSelector.builder();
	private @Nullable HuntedMap selectedMap;
	private @Nullable EventType processingEvent;
	private Map<PlayerInfo, HuntedClassSelector> queuedPlayers = Maps.newHashMap();
	private @Nullable UUID vip;
	private boolean gameRunning;
	private boolean gameStarting;
	private boolean buttonHighlighting;
	private HuntedGame.GameMode gameMode = HuntedGame.GameMode.MULTIPLAYER;
	
	public HuntedGameMenuScreen()
	{
		super(Component.translatable("hunted.menu.game.title"));
	}
	
	@Override
	protected void init()
	{
		super.init();
		if (this.menus.isEmpty())
		{
			this.menus.add(new InfoMenu());
			this.menus.add(new ClassSelectMenu());
			this.menus.add(new GameMenu());
			this.menus.add(new SettingsMenu());
		}
		if (this.menu == null)
			this.menu = this.menus.get(0);
		int windowX = this.getWindowX();
		int windowY = this.getWindowY();
		this.menu.onSelected(windowX + WINDOW_BORDER, windowY + WINDOW_BORDER, WINDOW_WIDTH - WINDOW_BORDER*2, WINDOW_HEIGHT - WINDOW_BORDER*2);
	}
	
	private int getWindowX()
	{
		return this.width / 2 - this.menu.getWindowWidth() / 2;
	}
	
	private int getWindowY()
	{
		return this.height / 2 - this.menu.getWindowHeight() / 2;
	}
	
	@Override
	public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(stack);
		super.render(stack, mouseX, mouseY, partialTicks);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.enableBlend();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		int windowX = this.getWindowX();
		int windowY = this.getWindowY();
		RenderSystem.setShaderTexture(0, WINDOW);
		blit(stack, windowX, windowY, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
		this.menu.render(stack, mouseX, mouseY, partialTicks, windowX + WINDOW_BORDER, windowY + WINDOW_BORDER, WINDOW_WIDTH - WINDOW_BORDER*2, WINDOW_HEIGHT - WINDOW_BORDER*2);
		int tabStartX = windowX - SIDE_TAB_WIDTH + 4;
		int tabStartY = windowY + (WINDOW_HEIGHT - this.menus.size() * SIDE_TAB_LENGTH) / 2;
		this.renderTabs(stack, tabStartX, tabStartY, mouseX, mouseY);
	}
	
	protected void renderTabs(PoseStack stack, int x, int y, int mouseX, int mouseY)
	{
		int size = this.menus.size();
		for (int i = 0; i < size; i++)
		{
			MenuTab menu = this.menus.get(i);
			boolean enabled = this.menu == menu;
			int tabY = y + i * SIDE_TAB_LENGTH;
			RenderSystem.setShaderTexture(0, SIDE_TABS);
			menu.drawTab(stack, x, tabY, enabled, this.getTabAt(mouseX, mouseY) == menu, i, size);
			RenderSystem.setShaderTexture(0, TAB_ICONS);
			menu.drawIcon(stack, x, tabY, enabled);
		}
	}
	
	@Override
	public boolean mouseClicked(double x, double y, int clickType)
	{
		MenuTab tab = this.getTabAt((int)x, (int)y);
		if (tab != null)
		{
			this.menu.onUnselected();
			this.menu = tab;
			int windowX = this.getWindowX();
			int windowY = this.getWindowY();
			this.menu.onSelected(windowX + WINDOW_BORDER, windowY + WINDOW_BORDER, WINDOW_WIDTH - WINDOW_BORDER*2, WINDOW_HEIGHT - WINDOW_BORDER*2);
			this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			return true;
		}
		return super.mouseClicked(x, y, clickType);
	}
	
	public @Nullable MenuTab getTabAt(int x, int y)
	{
		for (int i = 0; i < this.menus.size(); i++)
		{
			int tabX = this.getWindowX() - SIDE_TAB_WIDTH;
			int tabY = this.getWindowY() + (WINDOW_HEIGHT - this.menus.size() * SIDE_TAB_LENGTH) / 2 + i * SIDE_TAB_LENGTH;
			int tabMaxX = tabX + SIDE_TAB_WIDTH;
			int tabMaxY = tabY + SIDE_TAB_LENGTH;
			if ((x > tabX && x < tabMaxX) && (y > tabY && y < tabMaxY))
				return this.menus.get(i);
		}
		return null;
	}
	
	@Override
	public boolean isPauseScreen()
	{
		return false;
	}
	
	private void renderButtonBackground(PoseStack stack, int x, int y, int width)
	{
		RenderSystem.setShaderTexture(0, WINDOW);
		blit(stack, x, y, 0, BUTTON_WINDOW_Y, width - BUTTON_WINDOW_END_WIDTH, BUTTON_WINDOW_HEIGHT);
		blit(stack, x + width - BUTTON_WINDOW_END_WIDTH, y, 0, BUTTON_WINDOW_Y + BUTTON_WINDOW_HEIGHT, BUTTON_WINDOW_END_WIDTH, BUTTON_WINDOW_HEIGHT);
	}
	
	protected void doEvent(EventType event)
	{
		if (this.processingEvent == null)
		{
			switch (event)
			{
			case REQUEST:
			{
				HuntedPacketHandlers.MAIN.sendToServer(new RequestMenuUpdatePacket());
				break;
			}
			case JOIN:
			{
				HuntedPacketHandlers.MAIN.sendToServer(new JoinGamePacket(this.selector.build()));
				break;
			}
			case LEAVE:
			{
				HuntedPacketHandlers.MAIN.sendToServer(new LeaveGamePacket());
				break;
			}
			case SELECT_MAP:
			{
				HuntedPacketHandlers.MAIN.sendToServer(new SelectMapPacket(this.selectedMap));
				break;
			}
			case BEGIN:
			{
				HuntedPacketHandlers.MAIN.sendToServer(new BeginGamePacket());
				break;
			}
			case STOP_COUNTDOWN:
			{
				HuntedPacketHandlers.MAIN.sendToServer(new StopGameCountdownPacket());
				break;
			}
			case SET_BUTTON_HIGHLIGHTING:
			{
				HuntedPacketHandlers.MAIN.sendToServer(new SetButtonHighlightingPacket(!this.buttonHighlighting));
				break;
			}
			case PICK_GAME_MODE:
			{
				HuntedPacketHandlers.MAIN.sendToServer(new PickGameModePacket(this.gameMode));
				break;
			}
			}
			this.processingEvent = event;
		}
	}
	
	public void processUpdatePacket(UpdateGameMenuPacket packet)
	{
		if (packet.getProcessed() == this.processingEvent)
			this.processingEvent = null;
		
		this.queuedPlayers = packet.getQueued().entrySet().stream().collect(Collectors.toUnmodifiableMap(entry -> this.minecraft.getConnection().getPlayerInfo(entry.getKey()), Map.Entry::getValue));
		ResourceLocation id = packet.getMap();
		if (id != null)
		{
			HuntedMap map = HuntedMapDataManager.INSTANCE.getSynced(id);
			if (map != null)
				this.selectedMap = map;
			else
				LOGGER.warn("Received unknown map '" + id + "'");
		}
		else
		{
			this.selectedMap = null;
		}
		this.vip = packet.getVip();
		
		if (this.isQueued())
			this.selector = HuntedGameMenuScreen.this.queuedPlayers.get(this.minecraft.getConnection().getPlayerInfo(this.minecraft.player.getUUID())).toBuilder();
		
		this.gameRunning = packet.gameRunning();
		this.gameStarting = packet.gameStarting();
		this.buttonHighlighting = packet.buttonHighlighting();
		
		this.gameMode = packet.getGameMode();
		
		this.menu.onUpdatePacketReceived();
	}
	
	private boolean isVip()
	{
		return this.minecraft.player.getUUID().equals(this.vip);
	}
	
	private boolean canStartGame()
	{
		return this.isVip() && this.isQueued() && this.selectedMap != null && this.queuedPlayers.size() >= this.gameMode.getMinimumPlayerCount() && !this.gameStarting && !this.gameRunning;
	}
	
	private boolean isQueued()
	{
		return this.queuedPlayers.containsKey(this.minecraft.getConnection().getPlayerInfo(this.minecraft.player.getUUID()));
	}
	
	private boolean selectorApplicable()
	{
		return this.selector.contains(HuntedClassTypes.HUNTER.get()) && this.selector.contains(HuntedClassTypes.PREY.get());
	}
//	
//	public static void drawBoundedCenteredString(PoseStack stack, Font font, Component component, int x, int y, int width, int height, int color)
//	{
//		String rawText = component.getString();
//		int texWidth = font.width(component.getVisualOrderText());
//		int divisions = Math.round((float)texWidth / (float)width);
//		List<FormattedCharSequence> text = Lists.newArrayList();
//		for (int i = 0; i < divisions; i++)
//		{
//			text.addAll(font.split(component, color));
//			font.wordWrapHeight(component, i)
//		}
//	}
	
	public class MenuTab extends GuiComponent
	{
		protected final Minecraft mc;
		protected final Component title;
		protected final int icon;
		
		public MenuTab(String title, int icon)
		{
			this.mc = Minecraft.getInstance();
			this.icon = icon;
			this.title = Component.translatable(title).withStyle(Style.EMPTY.withBold(true).withUnderlined(true));
		}
		
		public void drawTab(PoseStack stack, int x, int y, boolean enabled, boolean highlighted, int tabPosition, int totalTabs)
		{
			int texY = SIDE_TAB_MIDDLE;
			if (tabPosition == 0)
				texY = SIDE_TAB_TOP;
			else if (tabPosition == totalTabs-1)
				texY = SIDE_TAB_BOTTOM;
			int texX = SIDE_TAB_DISABLED;
			if (enabled)
				texX = SIDE_TAB_ENABLED;
			if (highlighted)
				texX += SIDE_TAB_HIGHLIGHTED;
			blit(stack, x, y, texX, texY, SIDE_TAB_WIDTH, SIDE_TAB_LENGTH);
		}
		
		public void drawIcon(PoseStack stack, int x, int y, boolean enabled)
		{
			blit(stack, x, y, enabled ? 0 : SIDE_TAB_WIDTH, this.icon * SIDE_TAB_LENGTH, SIDE_TAB_WIDTH, SIDE_TAB_LENGTH);
		}
		
		public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks, int x, int y, int width, int height)
		{
			drawCenteredString(stack, this.mc.font, this.title, x + width / 2, y - 20 - this.mc.font.lineHeight, 16777215);
		}
		
		protected void onSelected(int x, int y, int width, int height) {}
		
		protected void onUnselected() {}
		
		public int getWindowWidth()
		{
			return WINDOW_WIDTH;
		}
		
		public int getWindowHeight()
		{
			return WINDOW_HEIGHT;
		}
		
		protected void onUpdatePacketReceived() {}
	}
	
	public class InfoMenu extends MenuTab
	{
		private final FormattedTextList textList;
		
		public InfoMenu()
		{
			super("hunted.menu.info.title", 0);
			this.textList = new FormattedTextList(this.mc, WINDOW_WIDTH, WINDOW_HEIGHT, 0, WINDOW_HEIGHT, false);
		}
		
		@Override
		public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks, int x, int y, int width, int height)
		{
			RenderSystem.setShaderTexture(0, LOGO);
			blit(stack, x + width / 2 - LOGO_WIDTH / 2, y - 20 - LOGO_HEIGHT, 0, 0, LOGO_WIDTH, LOGO_HEIGHT);
			KioskTutorialStep.renderIfMatches(KioskTutorialStep.START, HuntedGameMenuScreen.this, stack, mouseX, mouseY, partialTicks, x, y, width, height);
		}
		
		@Override
		protected void onSelected(int x, int y, int width, int height)
		{
			this.textList.clear();
			this.textList.updateSize(width, height, y, y + height);
			this.textList.setLeftPos(x);
			this.textList.line(Component.translatable("hunted.game.description"));
			HuntedGameMenuScreen.this.addRenderableWidget(this.textList);
		}
		
		@Override
		protected void onUnselected()
		{
			HuntedGameMenuScreen.this.removeWidget(this.textList);
			this.textList.clear();
			KioskTutorialStep.advanceIfMatches(KioskTutorialStep.START);
		}
		
		@Override
		public int getWindowHeight()
		{
			return WINDOW_HEIGHT - LOGO_HEIGHT;
		}
	}
	
	public class ClassSelectMenu extends MenuTab
	{
		private static final int ICON_SIZE = 32;
		private static final int INFO_TOP_OFFSET = 50;
		private final Map<HuntedClassType, SelectionList<HuntedClass>> lists = Maps.newLinkedHashMap();
		private final FormattedTextList textList;
		private @Nullable SimpleDataManagerList.Entry<HuntedClass> prevEntry;
		private int index;
		private final Button next;
		private final Button prev;
		private final Button select;
		
		public ClassSelectMenu()
		{
			super("hunted.menu.classes.title", 1);
			this.putList(HuntedClassTypes.PREY.get());
			this.putList(HuntedClassTypes.HUNTER.get());
			for (var entry : this.lists.entrySet())
			{
				if (entry.getValue().children().size() == 1)
					HuntedGameMenuScreen.this.selector.setSelected(entry.getKey(), entry.getValue().children().get(0).getObject());
			}
			this.next = new ImageButton(0, 0, ARROW_WIDTH, ARROW_HEIGHT, 0, 0, ARROW_HIGHLIGHTED, ARROWS, button -> 
			{
				HuntedGameMenuScreen.this.removeWidget(this.getListFromIndex(this.index).getValue());
				this.index++;
				if (this.index >= this.lists.size())
					this.index = 0;
				HuntedGameMenuScreen.this.addRenderableWidget(this.getListFromIndex(this.index).getValue());
			});
			this.prev = new ImageButton(0, 0, ARROW_WIDTH, ARROW_HEIGHT, ARROW_LEFT, 0, ARROW_HIGHLIGHTED, ARROWS, button -> 
			{
				HuntedGameMenuScreen.this.removeWidget(this.getListFromIndex(this.index).getValue());
				this.index--;
				if (this.index < 0)
					this.index = this.lists.size() - 1;
				HuntedGameMenuScreen.this.addRenderableWidget(this.getListFromIndex(this.index).getValue());
			});
			this.textList = new FormattedTextList(this.mc, LIST_WIDTH, WINDOW_HEIGHT, 0, WINDOW_HEIGHT, false);
			this.select = Button.builder(Component.translatable("hunted.menu.select.button"), button -> 
			{
				var entry = this.getListFromIndex(this.index);
				if (entry.getValue().getSelected() != null)
				{
					HuntedGameMenuScreen.this.selector.setSelected(entry.getKey(), entry.getValue().getSelected().getObject());
					if (HuntedGameMenuScreen.this.isQueued())
						HuntedGameMenuScreen.this.doEvent(EventType.JOIN);
				}
			}).bounds(0, 0, 80, 20).build();
		}
		
		private void putList(HuntedClassType type)
		{
			this.lists.put(type, new SelectionList<>(HuntedClassDataManager.INSTANCE, huntedClass -> {
				return huntedClass.getType().equals(type); 
			}, HuntedClass::getName, this.mc, LIST_WIDTH, WINDOW_HEIGHT, 0, WINDOW_HEIGHT));
		}
		
		@Override
		public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks, int x, int y, int width, int height)
		{
			super.render(stack, mouseX, mouseY, partialTicks, x, y, width, height);
			RenderSystem.setShaderTexture(0, SCROLL_MENU_WINDOW);
			int windowX = x + width + WINDOW_BORDER + 20 - WINDOW_BORDER;
			int windowY = y - WINDOW_BORDER;
			blit(stack, windowX, windowY, 0, 0, SCROLL_WINDOW_WIDTH, SCROLL_WINDOW_HEIGHT);
			this.prev.render(stack, mouseX, mouseY, partialTicks);
			this.next.render(stack, mouseX, mouseY, partialTicks);
			var entry = this.getListFromIndex(this.index);
			SelectionList<HuntedClass> list = entry.getValue();
			if (list.getSelected() != null)
			{
				if (HuntedGameMenuScreen.this.selector.hasSelected(list.getSelected().getObject()))
				{
					this.select.active = false;
					this.select.setMessage(Component.translatable("hunted.menu.select.button.unactive"));
				}
				else
				{
					this.select.active = true;
					this.select.setMessage(Component.translatable("hunted.menu.select.button"));
				}
			}
			else
			{
				this.select.active = false;
				this.select.setMessage(Component.translatable("hunted.menu.select.button"));
			}
			this.select.render(stack, mouseX, mouseY, partialTicks);
			ResourceLocation id = HuntedRegistries.HUNTED_CLASS_TYPES.get().getKey(entry.getKey());
			drawCenteredString(stack, this.mc.font, Component.translatable(id.getNamespace() + ".class_type." + id.getPath()).withStyle(Style.EMPTY.withBold(true).withUnderlined(true).withColor(entry.getKey().getColor())), windowX + SCROLL_WINDOW_WIDTH / 2, windowY + 12, 16777215);
			int boundX = x + WINDOW_BORDER;
			int boundY = y + WINDOW_BORDER;
			int boundWidth = width - WINDOW_BORDER*2;
			if (list.getSelected() != null)
			{
				HuntedClass huntedClass = list.getSelected().getObject();
				int titleX = boundX;
				int titleY = boundY;
				if (huntedClass.getIcon() != null)
				{
					titleX = x + 4;
					titleY = y + 4;
					RenderSystem.setShaderTexture(0, huntedClass.getIcon());
					blit(stack, titleX, titleY, 0, 0, ICON_SIZE, ICON_SIZE);
					titleX += ICON_SIZE + 16;
					titleY += ICON_SIZE / 2 - this.mc.font.lineHeight / 2;
				}
				this.mc.font.drawWordWrap(stack, huntedClass.getName().copy().withStyle(Style.EMPTY.withBold(true).withUnderlined(true)), titleX, titleY, boundWidth, 16777215);
				if (this.prevEntry != list.getSelected())
					this.buildClassDescription(huntedClass);
			}
			else
			{
				Component component = Component.translatable("hunted.menu.classes.select");
				this.mc.font.drawWordWrap(stack, component, boundX, boundY, boundWidth, 16777215);
				//drawCenteredString(stack, this.mc.font, Component.translatable("hunted.menu.classes.select").getVisualOrderText(), textX, textY, 16777215);
				if (this.prevEntry != null)
					this.textList.clear();
			}
			this.prevEntry = this.getListFromIndex(this.index).getValue().getSelected();
			KioskTutorialStep.renderIfMatches(KioskTutorialStep.CLASS_SELECT, HuntedGameMenuScreen.this, stack, mouseX, mouseY, partialTicks, x, y, width, height);
		}
		
		@Override
		protected void onSelected(int x, int y, int width, int height)
		{
			int listTopY = y - WINDOW_BORDER + SCROLL_WINDOW_LARGE_BORDER;
			int listEndY = WINDOW_HEIGHT + y - WINDOW_BORDER - SCROLL_WINDOW_LARGE_BORDER;
			int selX = x + width + WINDOW_BORDER + 20;
			this.lists.forEach((type, list) -> 
			{
				list.updateSize(LIST_WIDTH, WINDOW_HEIGHT, listTopY, listEndY);
				list.setLeftPos(selX);
			});
			this.prev.setX(selX);
			this.prev.setY(listTopY - ARROW_HEIGHT / 2 - SCROLL_WINDOW_LARGE_BORDER / 2);
			this.next.setX(selX + SCROLL_WINDOW_WIDTH - WINDOW_BORDER * 2 - ARROW_WIDTH);
			this.next.setY(listTopY - ARROW_HEIGHT / 2 - SCROLL_WINDOW_LARGE_BORDER / 2);
			HuntedGameMenuScreen.this.addRenderableWidget(this.getListFromIndex(this.index).getValue());
			HuntedGameMenuScreen.this.addWidget(this.prev);
			HuntedGameMenuScreen.this.addWidget(this.next);
			this.textList.updateSize(width, height, y + INFO_TOP_OFFSET, y + height);
			this.textList.setLeftPos(x);
			HuntedGameMenuScreen.this.addRenderableWidget(this.textList);
			this.select.setX(x + width + 20 + SCROLL_WINDOW_WIDTH / 2 - this.select.getWidth() / 2);
			this.select.setY(listEndY + SCROLL_WINDOW_LARGE_BORDER / 2 - this.select.getHeight() / 2);
			HuntedGameMenuScreen.this.addWidget(this.select);
			HuntedGameMenuScreen.this.doEvent(EventType.REQUEST);
		}
		
		@Override
		protected void onUnselected()
		{
			this.lists.forEach((type, list) -> {
				HuntedGameMenuScreen.this.removeWidget(list);
			});
			HuntedGameMenuScreen.this.removeWidget(this.next);
			HuntedGameMenuScreen.this.removeWidget(this.prev);
			HuntedGameMenuScreen.this.removeWidget(this.textList);
			HuntedGameMenuScreen.this.removeWidget(this.select);
			KioskTutorialStep.advanceIfMatches(KioskTutorialStep.CLASS_SELECT);
		}
		
		protected void onUpdatePacketReceived() 
		{
			var selector = HuntedGameMenuScreen.this.selector.build();
			for (var entry : this.lists.entrySet())
			{
				for (var element : entry.getValue().children())
				{
					if (element.getObject().equals(selector.getFromType(entry.getKey())))
					{
						entry.getValue().setSelected(element);
						break;
					}
				}
			}
		}
		
		@Override
		public int getWindowWidth()
		{
			return WINDOW_WIDTH + this.getListFromIndex(this.index).getValue().getWidth() + 20;
		}
		
		private @Nullable Map.Entry<HuntedClassType, SelectionList<HuntedClass>> getListFromIndex(int index)
		{
			int currentIndex = 0;
			for (var entry : this.lists.entrySet())
			{
				if (currentIndex == index)
					return entry;
				currentIndex++;
			}
			return null;
		}
		
		private void buildClassDescription(HuntedClass huntedClass)
		{
			this.textList.setScrollAmount(0.0D);
			this.textList.clear();
			this.textList.line(huntedClass.getDescription());
			this.textList.blank();
			this.textList.line(Component.translatable("hunted.menu.abilities").withStyle(Style.EMPTY.withBold(true).withUnderlined(true)));
			for (Ability ability : huntedClass.getAllAbilities())
			{
				if (!ability.getItem().equals(Items.AIR))
				{
					this.textList.blank();
					MutableComponent component = ability.getName().copy();
					this.textList.line(component.withStyle(component.getStyle().withItalic(true)));
					this.textList.blank();
					MutableComponent desc = ability.getLore().copy();
					this.textList.line(desc.withStyle(desc.getStyle().withColor(ChatFormatting.GRAY)));
				}
			}
		}
	}
	
	public class GameMenu extends MenuTab
	{
		private static final ResourceLocation BUTTON_HIGHLIGHTING = HuntedMod.resource("textures/gui/button_highlighting.png");
		private static final int BUTTON_WIDTH = 80;
		private static final int BUTTON_HIGHLIGHTING_SIZE = 20;
		private static final int BUTTON_BACKGROUND_WIDTH = BUTTON_WIDTH * 2 + BUTTON_HIGHLIGHTING_SIZE + 20;
		private final SelectionList<HuntedMap> list;
		private final Button select;
		private final QueuedPlayerList playerList;
		private final Button joinLeave;
		private final Button begin;
		private final FormattedTextList mapInfo;
		private @Nullable SimpleDataManagerList.Entry<HuntedMap> prevEntry;
		private final Button buttonHighlighting;
		private final Button next;
		private final Button prev;
		
		public GameMenu()
		{
			super("hunted.menu.game.title", 2);
			this.list = new SelectionList<>(HuntedMapDataManager.INSTANCE, HuntedMap::name, this.mc, LIST_WIDTH, WINDOW_HEIGHT, 0, WINDOW_HEIGHT);
			this.select = Button.builder( Component.translatable("hunted.menu.select.button"), button -> 
			{
				if (this.list.getSelected() != null)
				{
					HuntedGameMenuScreen.this.selectedMap = this.list.getSelected().getObject();
					HuntedGameMenuScreen.this.doEvent(EventType.SELECT_MAP);
				}
			}).bounds(0, 0, 80, 20).build();
			this.select.active = false;
			this.playerList = new QueuedPlayerList(this.mc, WINDOW_WIDTH, WINDOW_HEIGHT, 0, WINDOW_HEIGHT);
			this.joinLeave = Button.builder(Component.translatable("hunted.menu.game.join"), button -> 
			{
				if (!HuntedGameMenuScreen.this.isQueued())
				{
					if (HuntedGameMenuScreen.this.selectorApplicable())
					{
						HuntedGameMenuScreen.this.doEvent(EventType.JOIN);
						button.active = false;
					}
				}
				else
				{
					HuntedGameMenuScreen.this.doEvent(EventType.LEAVE);
					button.active = false;
				}
			}).bounds(0, 0, BUTTON_WIDTH, 20).build();
			this.begin = Button.builder(Component.translatable("hunted.menu.game.begin"), button -> 
			{
				if (HuntedGameMenuScreen.this.canStartGame())
					HuntedGameMenuScreen.this.doEvent(EventType.BEGIN);
				else if (HuntedGameMenuScreen.this.gameStarting && HuntedGameMenuScreen.this.isVip())
					HuntedGameMenuScreen.this.doEvent(EventType.STOP_COUNTDOWN);
				KioskTutorialStep.advanceIfMatches(KioskTutorialStep.GAME_MENU);
			}).bounds(0, 0, BUTTON_WIDTH, 20).build();
			this.joinLeave.active = false;
			this.begin.active = false;
			this.mapInfo = new FormattedTextList(this.mc, LIST_WIDTH, WINDOW_HEIGHT, 0, WINDOW_HEIGHT, false);
			this.buttonHighlighting =  new ImageButton(0, 0, BUTTON_HIGHLIGHTING_SIZE, BUTTON_HIGHLIGHTING_SIZE, 0, 0, BUTTON_HIGHLIGHTING_SIZE, BUTTON_HIGHLIGHTING, 256, 256, button -> {
				HuntedGameMenuScreen.this.doEvent(EventType.SET_BUTTON_HIGHLIGHTING);
			}, CommonComponents.EMPTY);
			this.buttonHighlighting.active = false;
			this.next = new ImageButton(0, 0, ARROW_WIDTH, ARROW_HEIGHT, 0, 0, ARROW_HIGHLIGHTED, ARROWS, button -> 
			{
				HuntedGame.GameMode mode = HuntedGameMenuScreen.this.gameMode;
				if (mode.ordinal() + 1 < HuntedGame.GameMode.values().length)
				{
					HuntedGameMenuScreen.this.gameMode = HuntedGame.GameMode.values()[mode.ordinal() + 1];
					HuntedGameMenuScreen.this.doEvent(EventType.PICK_GAME_MODE);
				}
				else
				{
					HuntedGameMenuScreen.this.gameMode = HuntedGame.GameMode.values()[0];
					HuntedGameMenuScreen.this.doEvent(EventType.PICK_GAME_MODE);
				}
				button.active = false;
			});
			this.prev = new ImageButton(0, 0, ARROW_WIDTH, ARROW_HEIGHT, ARROW_LEFT, 0, ARROW_HIGHLIGHTED, ARROWS, button -> 
			{
				HuntedGame.GameMode mode = HuntedGameMenuScreen.this.gameMode;
				if (mode.ordinal() - 1 >= 0)
				{
					HuntedGameMenuScreen.this.gameMode = HuntedGame.GameMode.values()[mode.ordinal() - 1];
					HuntedGameMenuScreen.this.doEvent(EventType.PICK_GAME_MODE);
				}
				else
				{
					HuntedGameMenuScreen.this.gameMode = HuntedGame.GameMode.values()[HuntedGame.GameMode.values().length - 1];
					HuntedGameMenuScreen.this.doEvent(EventType.PICK_GAME_MODE);
				}
				button.active = false;
			});
			this.next.active = false;
			this.prev.active = false;
		}
		
		@Override
		public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks, int x, int y, int width, int height)
		{
			super.render(stack, mouseX, mouseY, partialTicks, x, y, width, height);
			if (this.canShowMapWindow())
			{
				RenderSystem.setShaderTexture(0, SCROLL_MENU_WINDOW);
				int windowX = x + width + WINDOW_BORDER + 20 - WINDOW_BORDER;
				int windowY = y - WINDOW_BORDER;
				blit(stack, windowX, windowY, 0, 0, SCROLL_WINDOW_WIDTH, SCROLL_WINDOW_HEIGHT);
				if (this.list.getSelected() != null)
				{
					if (this.list.getSelected().getObject().equals(HuntedGameMenuScreen.this.selectedMap))
					{
						this.select.active = false;
						this.select.setMessage(Component.translatable("hunted.menu.select.button.unactive"));
					}
					else
					{
						this.select.active = HuntedGameMenuScreen.this.isVip();
						this.select.setMessage(Component.translatable("hunted.menu.select.button"));
					}
					
					if (this.list.getSelected() != this.prevEntry)
						this.buildMapDescription();
				}
				else
				{
					this.select.active = false;
					this.select.setMessage(Component.translatable("hunted.menu.select.button"));
				}
				this.select.render(stack, mouseX, mouseY, partialTicks);
				drawCenteredString(stack, this.mc.font, Component.translatable("hunted.menu.maps.title").withStyle(Style.EMPTY.withBold(true).withUnderlined(true)), windowX + SCROLL_WINDOW_WIDTH / 2, windowY + 12, 16777215);
				this.prevEntry = this.list.getSelected();
			}
			HuntedGameMenuScreen.this.renderButtonBackground(stack, x + width / 2 - BUTTON_BACKGROUND_WIDTH / 2, y + height + WINDOW_BORDER / 2 - BUTTON_WINDOW_HEIGHT / 2, BUTTON_BACKGROUND_WIDTH);
			this.joinLeave.render(stack, mouseX, mouseY, partialTicks);
			this.begin.render(stack, mouseX, mouseY, partialTicks);
			this.buttonHighlighting.render(stack, mouseX, mouseY, partialTicks);
			if (!HuntedGameMenuScreen.this.buttonHighlighting)
			{
				RenderSystem.setShaderTexture(0, BUTTON_HIGHLIGHTING);
				blit(stack, this.buttonHighlighting.getX(), this.buttonHighlighting.getY(), BUTTON_HIGHLIGHTING_SIZE, 0, this.buttonHighlighting.getWidth(), this.buttonHighlighting.getHeight());
			}
			if (this.playerList.children().isEmpty())
				this.mc.font.drawWordWrap(stack, Component.translatable("hunted.menu.game.noPlayers").withStyle(ChatFormatting.DARK_GRAY), this.playerList.getLeft() + 10, this.playerList.getTop() + this.playerList.getHeight() / 2 - this.mc.font.lineHeight / 2, this.playerList.getWidth(), 16777215);
			HuntedMap highlighted = this.getHighlighted();
			if (highlighted == null)
				this.mc.font.drawWordWrap(stack, Component.translatable("hunted.menu.game.noMap").withStyle(ChatFormatting.DARK_GRAY), this.mapInfo.getLeft() + 10, this.mapInfo.getTop() + this.mapInfo.getHeight() / 2 - this.mc.font.lineHeight / 2, this.mapInfo.getWidth(), 16777215);
			int modeSelectX = x + width / 2 - BUTTON_BACKGROUND_WIDTH / 2;
			int modeSelectY = y + height + WINDOW_BORDER / 2 - BUTTON_WINDOW_HEIGHT / 2 + BUTTON_WINDOW_HEIGHT + 2;
			HuntedGameMenuScreen.this.renderButtonBackground(stack, modeSelectX, modeSelectY, BUTTON_BACKGROUND_WIDTH);
			HuntedGame.GameMode mode = HuntedGameMenuScreen.this.gameMode;
			drawCenteredString(stack, this.mc.font, Component.translatable(mode.getTranslation()).withStyle(Style.EMPTY.withBold(true)), modeSelectX + BUTTON_BACKGROUND_WIDTH / 2, modeSelectY - this.mc.font.lineHeight / 2 + BUTTON_WINDOW_HEIGHT / 2, 16777215);
			this.prev.render(stack, mouseX, mouseY, partialTicks);
			this.next.render(stack, mouseX, mouseY, partialTicks);
			if (mouseX > modeSelectX + BUTTON_BACKGROUND_WIDTH / 2 - 70 && mouseX < modeSelectX + BUTTON_BACKGROUND_WIDTH / 2 + 70 && mouseY > modeSelectY && mouseY < modeSelectY + BUTTON_WINDOW_HEIGHT)
				HuntedGameMenuScreen.this.renderTooltip(stack, Component.translatable(mode.getTranslation() + ".description"), mouseX, mouseY);
			KioskTutorialStep.renderIfMatches(KioskTutorialStep.GAME_MENU, HuntedGameMenuScreen.this, stack, mouseX, mouseY, partialTicks, x, y, width, height);
			
			if (!HuntedGameMenuScreen.this.isVip())
				this.select.setTooltip(Tooltip.create(Component.translatable("hunted.menu.select.button.notVip")));
			else
				this.select.setTooltip(null);
			if (!HuntedGameMenuScreen.this.selectorApplicable() && !HuntedGameMenuScreen.this.isQueued())
				this.joinLeave.setTooltip(Tooltip.create(Component.translatable("hunted.menu.game.join.invalid")));
			else
				this.joinLeave.setTooltip(null);
			if (!HuntedGameMenuScreen.this.isVip())
				this.begin.setTooltip(Tooltip.create(Component.translatable("hunted.menu.game.begin.notVip")));
			else if (HuntedGameMenuScreen.this.gameRunning)
				this.begin.setTooltip(Tooltip.create(Component.translatable("hunted.menu.game.begin.alreadyRunning")));
			else if (HuntedGameMenuScreen.this.queuedPlayers.size() < HuntedGameMenuScreen.this.gameMode.getMinimumPlayerCount())
				this.begin.setTooltip(Tooltip.create(Component.translatable("hunted.menu.game.begin.invalidPlayers")));
			else if (HuntedGameMenuScreen.this.selectedMap == null)
				this.begin.setTooltip(Tooltip.create(Component.translatable("hunted.menu.game.begin.noMapSelected")));
			else
				this.begin.setTooltip(null);
			if (HuntedGameMenuScreen.this.isVip())
				this.buttonHighlighting.setTooltip(Tooltip.create(Component.translatable("hunted.menu.game.buttonHighlighting")));
			else
				this.buttonHighlighting.setTooltip(Tooltip.create(Component.translatable("hunted.menu.select.button.notVip")));
		}
		
		@Override
		protected void onSelected(int x, int y, int width, int height)
		{
			int listTopY = y - WINDOW_BORDER + SCROLL_WINDOW_LARGE_BORDER;
			int listEndY = WINDOW_HEIGHT + y - WINDOW_BORDER - SCROLL_WINDOW_LARGE_BORDER;
			int selX = x + width + WINDOW_BORDER + 20;
			this.list.updateSize(LIST_WIDTH, WINDOW_HEIGHT, listTopY, listEndY);
			this.list.setLeftPos(selX);
			this.select.setX(x + width + 20 + SCROLL_WINDOW_WIDTH / 2 - this.select.getWidth() / 2);
			this.select.setY(listEndY + SCROLL_WINDOW_LARGE_BORDER / 2 - this.select.getHeight() / 2);
			if (this.canShowMapWindow())
			{
				HuntedGameMenuScreen.this.addRenderableWidget(this.list);
				HuntedGameMenuScreen.this.addWidget(this.select);
			}
			HuntedGameMenuScreen.this.doEvent(EventType.REQUEST);
			this.playerList.clear();
			this.playerList.updateSize(width / 2 - 10, height, y, y + height);
			this.playerList.setLeftPos(x);
			HuntedGameMenuScreen.this.addRenderableWidget(this.playerList);
			this.joinLeave.setX(x + width / 2 - 4 - this.joinLeave.getWidth() - BUTTON_HIGHLIGHTING_SIZE / 2);
			this.joinLeave.setY(y + height + WINDOW_BORDER / 2 - this.joinLeave.getHeight() / 2);
			HuntedGameMenuScreen.this.addWidget(this.joinLeave);
			this.begin.setX(x + width / 2 - BUTTON_HIGHLIGHTING_SIZE / 2);
			this.begin.setY(y + height + WINDOW_BORDER / 2 - this.begin.getHeight() / 2);
			HuntedGameMenuScreen.this.addWidget(this.begin);
			this.buttonHighlighting.setX(this.begin.getX() + this.begin.getWidth() + 4);
			this.buttonHighlighting.setY(y + height + WINDOW_BORDER / 2 - this.buttonHighlighting.getHeight() / 2);
			HuntedGameMenuScreen.this.addWidget(this.buttonHighlighting);
			this.mapInfo.updateSize(width / 2, height, y, y + height - 15);
			this.mapInfo.setLeftPos(x + width / 2);
			HuntedGameMenuScreen.this.addRenderableWidget(this.mapInfo);
			int modeSelectX = x + width / 2 - BUTTON_BACKGROUND_WIDTH / 2;
			int modeSelectY = y + height + WINDOW_BORDER / 2 - BUTTON_WINDOW_HEIGHT / 2 + BUTTON_WINDOW_HEIGHT + 2;
			this.prev.setX(modeSelectX + 5);
			this.prev.setY(modeSelectY - ARROW_HEIGHT / 2 + BUTTON_WINDOW_HEIGHT / 2);
			this.next.setX(modeSelectX + BUTTON_BACKGROUND_WIDTH - ARROW_WIDTH - 5);
			this.next.setY(modeSelectY - ARROW_HEIGHT / 2 + BUTTON_WINDOW_HEIGHT / 2);
			HuntedGameMenuScreen.this.addWidget(this.prev);
			HuntedGameMenuScreen.this.addWidget(this.next);
		}
		
		@Override
		protected void onUnselected()
		{
			HuntedGameMenuScreen.this.removeWidget(this.list);
			HuntedGameMenuScreen.this.removeWidget(this.select);
			HuntedGameMenuScreen.this.removeWidget(this.playerList);
			HuntedGameMenuScreen.this.removeWidget(this.mapInfo);
			KioskTutorialStep.advanceIfMatches(KioskTutorialStep.GAME_MENU);
		}
		
		@Override
		protected void onUpdatePacketReceived() 
		{
			this.playerList.clear();
			var players = HuntedGameMenuScreen.this.queuedPlayers.entrySet().stream().sorted((entry, entry1) -> entry.getKey().getProfile().getId().equals(HuntedGameMenuScreen.this.vip) ? -1 : 1).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e, r) -> e, LinkedHashMap::new));
			players.forEach((info, selector) -> {
				this.playerList.addPlayer(info, selector, info.getProfile().getId().equals(HuntedGameMenuScreen.this.vip));
			});
			this.joinLeave.active = HuntedGameMenuScreen.this.selectorApplicable() || HuntedGameMenuScreen.this.isQueued();
			this.joinLeave.setMessage(HuntedGameMenuScreen.this.isQueued() ? Component.translatable("hunted.menu.game.leave") : Component.translatable("hunted.menu.game.join"));
			this.begin.active = HuntedGameMenuScreen.this.canStartGame() || (HuntedGameMenuScreen.this.gameStarting && HuntedGameMenuScreen.this.isVip());
			this.begin.setMessage(HuntedGameMenuScreen.this.gameStarting ? Component.translatable("hunted.menu.game.stop") : Component.translatable("hunted.menu.game.begin"));
			for (var entry : this.list.children())
			{
				if (entry.getObject().equals(HuntedGameMenuScreen.this.selectedMap))
					this.list.setSelected(entry);
			}
			this.buildMapDescription();
			this.buttonHighlighting.active = HuntedGameMenuScreen.this.isVip();
			this.next.active = HuntedGameMenuScreen.this.isVip();
			this.prev.active = HuntedGameMenuScreen.this.isVip();
		}
		
		@Override
		public int getWindowWidth()
		{
			return WINDOW_WIDTH + (this.canShowMapWindow() ? this.list.getWidth() + 20 : 0);
		}
		
		private @Nullable HuntedMap getHighlighted()
		{
			if (this.list.getSelected() != null)
				return this.list.getSelected().getObject();
			else
				return HuntedGameMenuScreen.this.selectedMap;
		}
		
		private void buildMapDescription()
		{
			HuntedMap map = this.getHighlighted();
			this.mapInfo.setScrollAmount(0.0D);
			this.mapInfo.clear();
			if (map != null)
			{
				this.mapInfo.line(Component.translatable(map.id().getNamespace() + ".map." + map.id().getPath()).withStyle(Style.EMPTY.withBold(true).withUnderlined(true)));
				this.mapInfo.line(Component.translatable("hunted.menu.map.info.buttons", Component.literal(String.valueOf(map.buttons().size())).withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.GRAY));
				this.mapInfo.line(Component.translatable("hunted.menu.map.info.rewards").withStyle(ChatFormatting.GRAY));
				List<ButtonReward> rewards = map.rewards().stream().distinct().toList();
				for (int i = 0; i < rewards.size(); i++)
				{
					ButtonReward reward = rewards.get(i);
					this.mapInfo.line(1, Component.literal(i + 1 + ". ").withStyle(ChatFormatting.DARK_GRAY).append(reward.getName()));
				}
				this.mapInfo.line(Component.translatable("hunted.menu.map.info.keyholes", Component.literal(String.valueOf(map.keyholes().size())).withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.GRAY));
				Component nav = map.navigation().isPresent() && !map.navigation().get().nodes().isEmpty() ? Component.translatable("hunted.overlay.reward.collected").withStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.GREEN)) : Component.translatable("hunted.overlay.reward.uncollected").withStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.RED));
				this.mapInfo.line(Component.translatable("hunted.menu.map.info.navigation", nav).withStyle(ChatFormatting.GRAY));
			}
		}
		
		private boolean canShowMapWindow()
		{
			return this.list.children().size() > 1;
		}
	}
	
	public class SettingsMenu extends MenuTab
	{
		private final FormattedOptionsList settings;
		
		public SettingsMenu()
		{
			super("hunted.menu.settings.title", 3);
			this.settings = new FormattedOptionsList(this.mc, WINDOW_WIDTH, WINDOW_HEIGHT, 0, WINDOW_HEIGHT, 25);
		}
		
		@Override
		public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks, int x, int y, int width, int height)
		{
			super.render(stack, mouseX, mouseY, partialTicks, x, y, width, height);
		}
		
		@Override
		protected void onSelected(int x, int y, int width, int height)
		{
			this.settings.updateSize(width, height, y, y + height);
			this.settings.setLeftPos(x);
			HuntedGameMenuScreen.this.addRenderableWidget(this.settings);
			this.addConfigOptions();
		}
		
		@Override
		protected void onUnselected()
		{
			HuntedGameMenuScreen.this.removeWidget(this.settings);
		}
		
		private void addConfigOptions()
		{
			this.settings.clear();
			this.settings.addBig(createBooleanValue("hunted.config.client.horrorElements", Component.translatable("hunted.config.client.horrorElements.description"), HuntedConfig.CLIENT.horrorElements));
		}
		
		private static OptionInstance<Boolean> createBooleanValue(String title, Component desc, BooleanValue value)
		{
			OptionInstance<Boolean> option = OptionInstance.createBoolean(
					title,
					(val) -> Tooltip.create(desc),
					value.get(),
					(newValue) -> value.set(newValue)
			);
			return option;
		}
	}
	
	public static class SelectionList<T> extends SimpleDataManagerList<T>
	{
		public SelectionList(SimpleDataManager<T> manager, Predicate<T> filter, Function<T, Component> nameGetter, Minecraft mc, int width, int height, int top, int bottom)
		{
			super(manager, filter, nameGetter, mc, width, height, top, bottom);
			this.setRenderBackground(false);
			this.setRenderTopAndBottom(false);
			if (this.children().size() == 1)
				this.setSelected(this.getEntry(0));
		}
		
		public SelectionList(SimpleDataManager<T> manager, Function<T, Component> nameGetter, Minecraft mc, int width, int height, int top, int bottom)
		{
			this(manager, t -> true, nameGetter, mc, width, height, top, bottom);
		}

		@Override
		public int getRowWidth()
		{
			return this.getWidth();
		}
		
		@Override
		protected int getScrollbarPosition()
		{
			return this.x0 + this.getWidth() - 6;
		}
		
		@Override
		public boolean mouseClicked(double x, double y, int clickType)
		{
			if (super.mouseClicked(x, y, clickType))
			{
				this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
				return true;
			}
			else
			{
				return false;
			}
		}
	}
	
	public static class FormattedTextList extends TextElementList
	{
		public FormattedTextList(Minecraft mc, int width, int height, int top, int bottom, boolean center)
		{
			super(mc, width, height, top, bottom, center);
			this.setRenderBackground(false);
			this.setRenderTopAndBottom(false);
		}
		
		@Override
		public int getRowWidth()
		{
			return this.getWidth() - 10;
		}
		
		@Override
		protected int getScrollbarPosition()
		{
			return this.x0 + this.getWidth() - 5;
		}
	}
}
