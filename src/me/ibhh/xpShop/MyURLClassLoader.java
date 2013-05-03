/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ibhh.xpShop;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.jar.JarFile;

public class MyURLClassLoader extends URLClassLoader {

    MyURLClassLoader(URL[] urL, ClassLoader parent) {
        super(urL, parent);
    }

    /**
     * Closes all open jar files
     */
    @SuppressWarnings("rawtypes")
	@Override
    public void close() {
        try {
			Class clazz = java.net.URLClassLoader.class;
            Field ucp = clazz.getDeclaredField("ucp");
            ucp.setAccessible(true);
            Object sunMiscURLClassPath = ucp.get(this);
            Field loaders = sunMiscURLClassPath.getClass().getDeclaredField("loaders");
            loaders.setAccessible(true);
            Object collection = loaders.get(sunMiscURLClassPath);
            for (Object sunMiscURLClassPathJarLoader : ((Collection) collection).toArray()) {
                try {
                    Field loader = sunMiscURLClassPathJarLoader.getClass().getDeclaredField("jar");
                    loader.setAccessible(true);
                    Object jarFile = loader.get(sunMiscURLClassPathJarLoader);
                    ((JarFile) jarFile).close();
                } catch (Throwable t) {
                    // if we got this far, this is probably not a JAR loader so skip it
                }
            }
        } catch (Throwable t) {
            // probably not a SUN VM
        }
    }
}
