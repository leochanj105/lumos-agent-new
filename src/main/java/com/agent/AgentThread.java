package com.agent;
import java.lang.instrument.Instrumentation;
import java.lang.Thread;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;
import java.lang.instrument.UnmodifiableClassException;
import java.util.Collection;

import java.lang.reflect.Field;
import java.lang.ClassLoader;
import java.util.Vector;
import java.net.URI;

import com.google.common.collect.Lists;
// import edu.brown.cs.systems.dynamicinstrumentation.JVMAgent;
import edu.brown.cs.systems.dynamicinstrumentation.*;

import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.ConstPool;
import org.json.*;
import java.util.Map;
import java.util.HashMap;

public class AgentThread implements Runnable, MessageHandler{
    public Instrumentation inst;
    public JVMAgent agent;
    public DynamicManager manager;
    public WebSocketClient client;
    public String sname = System.getProperty("sname");

    public Map<String, DynamicModification> tpmap;
    // public ClassLoader loader;
    //public static final String CLASSNAME = "com.test.App";
    //public static final String METHODNAME = "yell";
    //public static final String TESTTP = "System.out.println(\"Ha!\");";
    public static final String WITHSPAN = "io.opentelemetry.instrumentation.annotations.WithSpan";
    public static final String CLASSNAME = "travel.service.TravelServiceImpl";
    public static final String METHODNAME = "query";
    public static final String TESTTP=  "io.opentelemetry.api.trace.Span.current().addEvent(\"[LUMOS] HELLO!!!!!!!!\");";
        
    public static final int TESTLINE = 158;

    public AgentThread(Instrumentation inst){
        this.inst = inst;
        this.agent = new JVMAgent(inst);
        this.manager = new DynamicManager(this.agent);
	tpmap = new HashMap<>();
	//System.out.println("SNAME=" + sname);
	
        // this.agent.loader = loader;
    }

    // public static void getLoader(ClassLoader loader){
    //     AgentThread.loader = loader;
    // }

    public void connect(String controllerAddr){
        try {
            this.client = new WebSocketClient(new URI(controllerAddr), this);
        } catch (Exception e) {
            e.printStackTrace();
        }
	
        this.client.send(sname);
    }

    public void handleJSON(String jstr){
        //jstr = "{type:add, tps : [{id:\"1\", cname:a, method:b, tptype:code, line:1, code:xxx}, {id:\"2\", cname:a, method:b, tptype:span}]}";
      
        JSONObject obj = new JSONObject(jstr);
        String x = obj.getString("type");
        if(x.equals("add")){
            JSONArray arr = obj.getJSONArray("tps");
            for(int i = 0; i < arr.length(); i++){
                JSONObject tp = arr.getJSONObject(i);
                String id = tp.getString("id");
                String cname = tp.getString("cname");
                String method = tp.getString("method");
                String tptype = tp.getString("tptype");
		DynamicModification mod = null;
                if(tptype.equals("code")){
                    String code = tp.getString("code");
                    int line = tp.getInt("line");
		    mod = new InstructionModification(cname, method, code, line);
                }
                else if(tptype.equals("span")){
   		    mod = new AnnotationModification(cname, method, WITHSPAN);
                }
		else if(tptype.equals("timing")){
		    int line1 = tp.getInt("line1");
		    int line2 = tp.getInt("line2");		    
		    mod = new TimingModification(cname, method, id, line1, line2);
		}
		/*else if(tptype.equals("var")){
		    String type = tp.getString("type");
		    String vname = tp.getString("vname");
		    CtClass ctype = null;
		    if(type.equals("boolean")){
			 ctype = CtClass.booleanType;
		    }
		    else if(type.equals("int")){
			 ctype = CtClass.intType;
		    }
		    else if(type.equals("long")){
			 ctype = CtClass.longType;
		    }
		    else if(type.equals("float")){
			 ctype = CtClass.floatType;
		    }
		    else if(type.equals("double")){
			 ctype = CtClass.doubleType;
		    }
		    mod = new LocalVarModification(cname, method, vname, ctype);
		}*/
	
		if(mod != null){
  		    tpmap.put(id, mod);
		    this.manager.add(mod);
	    	    try{	    
              	 	this.manager.install();
	    	    }
	    	    catch(Exception e){
            	 	e.printStackTrace();
	   	    }
		}
            }

        }
        else if(x.equals("remove")){
            //System.out.println("removing temporarily not implemented");
    	    JSONArray arr = obj.getJSONArray("tps");
            for(int i = 0; i < arr.length(); i++){
                JSONObject tp = arr.getJSONObject(i);
                String id = tp.getString("id");
		DynamicModification mod = tpmap.get(id);
		if(mod != null){
		    tpmap.remove(id);
		    this.manager.remove(mod);
	    	    try{	    
              	 	this.manager.install();
	    	    }
	    	    catch(Exception e){
            	 	e.printStackTrace();
	   	    }
		}
	    }
        }
	else{
	    System.out.println("Not implemented!");
	}
    }

    public void handleMessage(String message){
        System.out.println("Handling " + message);
	//if(message.equals("keepalive"))
	//    return;
	handleJSON(message);
	/*
        String[] traceArgs = message.split(",", 0);
	System.out.println(traceArgs);
	String classname = traceArgs[0];
	String methodname = traceArgs[1];
	String code = traceArgs[2];
	int ln = Integer.parseInt(traceArgs[3]);
	//DynamicModification mod = new AnnotationModification(classname, methodname, WITHSPAN);
	DynamicModification mod = new InstructionModification(classname, methodname, code, ln);
	try{
	    this.manager.add(mod);
            this.manager.install();
	}
	catch(Exception e){
            e.printStackTrace();
	}
	System.out.println("Instrumented!");
	*/
    }

    @Override
    public void run(){
        // Wait Until we hooked the Spring classloader
        while(LumosAgent.cloader == null) ;


        // Connect to websocket controller server
	connect("ws://lumos:8765");
	
	
/*	
        // A test of adding a tracepoint, then remove it...
        int seconds = 20;
        for(int i = 0; i < seconds; i++){
            try{
                Thread.sleep(1000);
            }
            catch(Exception e){
            	e.printStackTrace();
            }
            System.out.println("[LUMOS] Count " + (seconds - i));
        }
        System.out.println("[LUMOS] Instrumenting...");


        //DynamicModification modifyMethod = new InstructionModification(CLASSNAME, METHODNAME, TESTTP, TESTLINE);
        DynamicModification modifyMethod = new AnnotationModification(CLASSNAME, METHODNAME, WITHSPAN);
        
        try{
            this.manager.add(modifyMethod);
            this.manager.install();
            System.out.println("[LUMOS] Instrumented!");
            //this.client.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
  */     
/**        
        seconds = 60;
        for(int i = 0; i < seconds; i++){
            try{
                Thread.sleep(1000);
            }
            catch(Exception e){
            }
            System.out.println("[LUMOS] Count " + (seconds - i));
        }

        System.out.println("[LUMOS] Uninstrumenting...");
        try{
            this.manager.remove(modifyMethod);
            this.manager.install();
            System.out.println("[LUMOS] Uninstrumented!");

            
        }
        catch(Exception e){
            e.printStackTrace();
        }
**/        
        while(true){

        }
    }
}
