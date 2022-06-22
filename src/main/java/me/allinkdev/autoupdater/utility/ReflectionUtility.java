package me.allinkdev.autoupdater.utility;

import com.google.common.reflect.ClassPath;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;

@SuppressWarnings("all")
public class ReflectionUtility {

	private static final ClassLoader classLoader = Bukkit.class.getClassLoader();
	private static final ClassPath classPath;

	static {
		try {
			classPath = ClassPath.from(classLoader);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Set<Class<?>> getSubTypesOf(Class<?> clazz) {
		final Set<Class<?>> classes = new HashSet<>();

		classPath.getAllClasses()
			.stream()
			.peek((classInfo) -> {
				final Class<?> loadedClass;
				try {
					loadedClass = Class.forName(classInfo.getName());
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				}

				if (loadedClass.getSuperclass().getName().equals(clazz.getName())) {
					classes.add(loadedClass);
				}
			})
			.close();

		return Collections.unmodifiableSet(classes);
	}
}
