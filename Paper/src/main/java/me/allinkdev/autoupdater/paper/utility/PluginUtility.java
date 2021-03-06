package me.allinkdev.autoupdater.paper.utility;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import me.allinkdev.autoupdater.common.artifact.ArtifactIdentity;
import me.allinkdev.autoupdater.common.utility.ReflectionUtility;
import me.allinkdev.autoupdater.paper.Main;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.PluginClassLoader;

@SuppressWarnings("unchecked")
public class PluginUtility {

	private static final MinecraftServer SERVER = MinecraftServer.getServer();
	private static final CommandDispatcher<CommandSourceStack> COMMAND_DISPATCHER;
	private static final CommandNode<CommandSourceStack> ROOT_COMMAND_NODE;
	private static final SimplePluginManager PLUGIN_MANAGER = (SimplePluginManager) Bukkit.getPluginManager();
	private static final Class<? extends SimplePluginManager> PLUGIN_MANAGER_CLASS = PLUGIN_MANAGER.getClass();
	private static final Set<Class<?>> EVENTS;
	private static final Path PLUGIN_DIRECTORY = Main.getInstance().getPluginDirectory();

	static {
		try {
			COMMAND_DISPATCHER = SERVER.vanillaCommandDispatcher.getDispatcher();
			ROOT_COMMAND_NODE = COMMAND_DISPATCHER.getRoot();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static {
		EVENTS = ReflectionUtility.getSubTypesOf(Event.class);
	}

	public static Path getPluginDirectory() {
		return PLUGIN_DIRECTORY;
	}

	public static Plugin reloadPlugin(Plugin plugin)
		throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvalidPluginException, InvalidDescriptionException {

		final File pluginFile = getPluginFile(plugin);

		unload(plugin);
		return load(pluginFile);
	}

	public static File getPluginFile(Plugin plugin)
		throws NoSuchFieldException, IllegalAccessException {
		final PluginClassLoader classLoader = (PluginClassLoader) plugin.getClass()
			.getClassLoader();
		return (File) PluginClassLoader.class.getDeclaredField("file").get(classLoader);
	}

	public static Plugin load(File file)
		throws InvalidPluginException, InvalidDescriptionException {
		final Plugin plugin = Bukkit.getPluginManager().loadPlugin(file);

		assert plugin != null : "Plugin " + file.getName() + " was null!";

		plugin.onLoad();
		PLUGIN_MANAGER.enablePlugin(plugin);
		return plugin;
	}

	public static void unload(Plugin plugin)
		throws NoSuchMethodException {

		// Unregister commands
		final List<String> commands = new ArrayList<>(Bukkit.getCommandMap()
			.getKnownCommands()
			.values()
			.stream()
			.filter(uncastedCommand -> {
				if (!(uncastedCommand instanceof final PluginIdentifiableCommand command)) {
					return false;
				}

				return command.getPlugin().getName().equalsIgnoreCase(plugin.getName());
			})
			.map(Command::getName)
			.collect(Collectors.toUnmodifiableSet()));

		for (String command : commands) {
			ROOT_COMMAND_NODE.removeCommand(command);
		}

		for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
			onlinePlayer.updateCommands();
		}

		// Unregister event listeners
		for (HandlerList handler : getHandlers()) {
			handler.unregister(plugin);
		}

		Bukkit.getPluginManager().disablePlugin(plugin);
	}

	private static List<HandlerList> getHandlers()
		throws NoSuchMethodException {
		final List<HandlerList> handlers = new ArrayList<>();
		final Method getEventListenersMethod = PLUGIN_MANAGER_CLASS.getDeclaredMethod(
			"getEventListeners",
			Class.class);

		EVENTS
			.stream()
			.peek(e -> {
				try {
					handlers.add((HandlerList) getEventListenersMethod.invoke(PLUGIN_MANAGER, e));
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			})
			.close();

		return Collections.unmodifiableList(handlers);
	}

	public static File getPluginFileFor(ArtifactIdentity identity) {
		final Path filePath = PLUGIN_DIRECTORY.resolve(identity.getArtifactName() + ".jar");

		return new File(filePath.toString());
	}
}
