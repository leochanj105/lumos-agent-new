package com.agent;  
import java.lang.instrument.Instrumentation;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;
import java.lang.instrument.UnmodifiableClassException;
import edu.brown.cs.systems.dynamicinstrumentation.*;

import java.util.Collection;
import com.google.common.collect.Lists;

public class TimingModification implements DynamicModification {
        public final String className, methodName, tpid;
	public final int line1, line2;
        // public final boolean before;

        public TimingModification(String className, String methodName, String tpid, int line1, int line2) {
            this.className = className;
            this.methodName = methodName;
            this.tpid = tpid;
            this.line1 = line1;
	    this.line2 = line2;
            // this.before = before;
        }
        
        @Override
        public Collection<String> affects() {
            return Lists.newArrayList(className);
        }

        @Override
        public void apply(ClassPool pool) throws NotFoundException, CannotCompileException {
            CtClass cls = pool.get(className);
	    String stname = "start" + tpid;
	    String edname = "end" + tpid;
	    String ins1 = stname + " = System.currentTimeMillis();";
            cls.getDeclaredMethod(methodName).addLocalVariable(stname, CtClass.longType);
            cls.getDeclaredMethod(methodName).addLocalVariable(edname, CtClass.longType);
	    cls.getDeclaredMethod(methodName).insertAt(line1, ins1);
	    System.out.println(ins1);
//	    String ins2 = tname + " = System.currentTimeMillis() - " + tname + "; " + "io.opentelemetry.api.trace.Span.current().addEvent(\"" +tname+ "\"+" +  tname + ");";
	    String ins2 = edname + " = System.currentTimeMillis() - " + stname + ";";
	    //String ins2 = " System.out.println(System.currentTimeMillis() - " + tname + "); ";
	    cls.getDeclaredMethod(methodName).insertAt(line2, ins2);
	    System.out.println(ins2);
        }
    }
