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
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.ConstPool;
import com.google.common.collect.Lists;

public class AnnotationModification implements DynamicModification {
        public final String className, methodName, annotation;
        // public final boolean before;
        // public final int line_num;

        public AnnotationModification(String className, String methodName, String annotation) {
            this.className = className;
            this.methodName = methodName;
            this.annotation = annotation;
            // this.instruction = instruction;
            // this.line_num = line_num;
            // this.before = before;
        }
        
        @Override
        public Collection<String> affects() {
            return Lists.newArrayList(className);
        }

        @Override
        public void apply(ClassPool pool) throws NotFoundException, CannotCompileException {
            CtClass cls = pool.get(className);
            // cls.getDeclaredMethod(methodName).insertAt(line_num, instruction);
            ConstPool cpool = cls.getClassFile().getConstPool();
            AnnotationsAttribute attr = new AnnotationsAttribute(cpool, AnnotationsAttribute.visibleTag);
            Annotation annot = new Annotation(annotation, cpool);
            attr.addAnnotation(annot);
            cls.getDeclaredMethod(methodName).getMethodInfo().addAttribute(attr);
        }
    }
