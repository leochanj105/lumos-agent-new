package com.agent;

import java.lang.instrument.Instrumentation;
import javassist.ClassPool;
import javassist.LoaderClassPath;

import java.lang.ClassLoader;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import java.net.URLClassLoader;
/**
 * Hello world!
 *
 */
public class LumosAgent
{
    // public static ClassLoader loader;

    // public Agent(ClassLoader loader){
    //     this.loader = loader;
    // }
    public static ClassLoader cloader = null;

    public static void premain(String agentArgs, Instrumentation inst){

        // ClassPool classPool = ClassPool.getDefault();
        // classPool.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
        // System.out.println("[XXXX] " + classPool);
        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(
            ClassLoader loader,
            String className,
            Class<?> classBeingRedefined, // null if class was not previously loaded
            ProtectionDomain protectionDomain,
            byte[] classFileBuffer) {
                // return transformed class file.
                
                if(LumosAgent.cloader == null && loader != null && loader.getClass().getName().contains("LaunchedURLClassLoader")){
		    System.out.println("Hooked " + loader);
                    LumosAgent.cloader = loader;
                }

                // if(className.contains("ServiceImpl")){
                //     System.out.println("[VVVV] " + loader + ": " + new LoaderClassPath(loader));
                //     ClassPool pool = new ClassPool(true);
                //     ClassPool.doPruning = true;
                //     pool.appendClassPath(new LoaderClassPath(loader));
                //     System.out.println("[VVV] " + pool);
                //     try{
                //         System.out.println("[VV] " + pool.get("travel.service.TravelServiceImpl"));
                //     }
                //     catch(Exception e){
                //         e.printStackTrace();
                //     }
                // }
                return classFileBuffer;
            }
        });
        Thread thread = new Thread(new AgentThread(inst));
        thread.start();
    }

    public static void agentmain(String agentArgs, Instrumentation inst){
        Thread thread = new Thread(new AgentThread(inst));
        thread.start();
    }
}
