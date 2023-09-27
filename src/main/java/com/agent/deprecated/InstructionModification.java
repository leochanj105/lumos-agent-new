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

public class InstructionModification implements DynamicModification {
        public final String className, methodName, instruction;
        // public final boolean before;
        public final int lineNum;

        public InstructionModification(String className, String methodName, String instruction, int lineNum) {
            this.className = className;
            this.methodName = methodName;
            this.instruction = instruction;
            this.lineNum = lineNum;
            // this.before = before;
        }
        
        @Override
        public Collection<String> affects() {
            return Lists.newArrayList(className);
        }

        @Override
        public void apply(ClassPool pool) throws NotFoundException, CannotCompileException {
            CtClass cls = pool.get(className);
            cls.getDeclaredMethod(methodName).insertAt(lineNum, instruction);
        }
    }
