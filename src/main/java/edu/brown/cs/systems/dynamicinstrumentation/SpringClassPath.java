package edu.brown.cs.systems.dynamicinstrumentation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javassist.ClassPath;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.NotFoundException;

public class SpringClassPath implements ClassPath {
    Set<String> jarfileEntries;
    String jarfileURL;

    SpringClassPath(String pathname) throws NotFoundException {
        JarFile jarfile = null;
        try {
            jarfile = new JarFile(pathname);
            jarfileEntries = new HashSet<String>();
            for (JarEntry je: Collections.list(jarfile.entries()))
                if (je.getName().endsWith(".class")){
                    jarfileEntries.add(je.getName());
                    System.out.println(je.getName());
                }
            jarfileURL = new File(pathname).getCanonicalFile()
                    .toURI().toURL().toString();
            System.out.println(jarfileURL);
            return;
        } catch (IOException e) {}
        finally {
            if (null != jarfile)
                try {
                    jarfile.close();
                } catch (IOException e) {}
        }
        throw new NotFoundException(pathname);
    }

    @Override
    public InputStream openClassfile(String classname)
            throws NotFoundException
    {
        URL jarURL = find(classname);
        if (null != jarURL)
            try {
                // if (ClassPool.cacheOpenedJarFile)
                    // return jarURL.openConnection().getInputStream();
                // else {
                    java.net.URLConnection con = jarURL.openConnection();
                    con.setUseCaches(false);
                    return con.getInputStream();
                // }
            }
            catch (IOException e) {
                throw new NotFoundException("broken jar file?: "
                        + classname);
            }
        return null;
    }

    @Override
    public URL find(String classname) {
        String modifiedCN = classname;
        if(classname.contains("travel.")) modifiedCN = "BOOT-INF/classes/" + classname;
        String jarname = modifiedCN.replace('.', '/') + ".class";
        if (jarfileEntries.contains(jarname)){
            try {
                System.out.println("[XXX] ---------here " + String.format("jar:%s!/%s", jarfileURL, jarname));
                return new URL(String.format("jar:%s!/%s", jarfileURL, jarname));
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return null;            // not found
    }

    @Override
    public String toString() {
        return jarfileURL == null ? "<null>" : jarfileURL;
    }

    @Override
    public void close(){

    }
}