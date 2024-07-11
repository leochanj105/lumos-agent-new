package com.agent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LumosClassLoader extends ClassLoader{
    public Map<String, byte[]> clsMap = new ConcurrentHashMap<>();
    public LumosClassLoader(ClassLoader parent){
        super(parent);
    }
    public LumosClassLoader(){
        super();
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytecode = clsMap.get(name);
        return defineClass(name, bytecode, 0, bytecode.length);
    }
    public void setByteCode(String name, byte[] content){
        clsMap.put(name, content);
    }
}
