package org.AutumnAP;

/**
 * @author ziyuan
 * @since 2024.06
 */

import com.google.auto.service.AutoService;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.reflect.Method;
import java.util.Set;

@AutoService(Processor.class)
@SupportedAnnotationTypes("org.AutumnAP.EnableAutumnFramework")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class FrameworkAnnotationProcessor extends AbstractProcessor {
    private Trees trees;
    private TreeMaker treeMaker;
    private Names names;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        processingEnv = jbUnwrap(ProcessingEnvironment.class, processingEnv);
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        trees = Trees.instance(processingEnv);
        treeMaker = TreeMaker.instance(context);
        names = Names.instance(context);
        System.out.println("Hello AUTUMN Framework! POWERED BY AST");
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(EnableAutumnFramework.class)) {
            TreePath treePath = trees.getPath(annotatedElement);
            treePath.getCompilationUnit().accept(new TreeScanner<Void, Void>() {
                @Override
                public Void visitMethod(MethodTree methodTree, Void aVoid) {
                    if ("main".equals(methodTree.getName().toString())) {
                        JCTree.JCMethodDecl method = (JCTree.JCMethodDecl) methodTree;
                        modifyMethod(method);
                    }
                    return null;
                }
            }, null);
        }
        return true;
    }


    private void modifyMethod(JCTree.JCMethodDecl method) {

        JCTree.JCExpression println = treeMaker.Select(
                treeMaker.Select(
                        treeMaker.Ident(names.fromString("System")),
                        names.fromString("out")
                ),
                names.fromString("println")
        );
        JCTree.JCMethodInvocation printCall = treeMaker.Apply(
                List.nil(),
                println,
                List.of(treeMaker.Literal("Hello AUTUMN Framework! POWERED BY AST"))
        );
        JCTree.JCExpressionStatement printStatement = treeMaker.Exec(printCall);


        JCTree.JCNewClass autumnFrameworkRunnerCreation = treeMaker.NewClass(
                null,
                List.nil(),
                treeMaker.Ident(names.fromString("AutumnFrameworkRunner")),
                List.nil(),
                null
        );
        JCTree.JCVariableDecl autumnFrameworkRunner = treeMaker.VarDef(
                treeMaker.Modifiers(0),
                names.fromString("autumnFrameworkRunner"),
                treeMaker.Ident(names.fromString("AutumnFrameworkRunner")),
                autumnFrameworkRunnerCreation
        );


        JCTree.JCFieldAccess mainClass = treeMaker.Select(
                treeMaker.Ident(names.fromString("Main")),
                names.fromString("class")
        );


        JCTree.JCMethodInvocation runCall = treeMaker.Apply(
                List.nil(),
                treeMaker.Select(
                        treeMaker.Ident(names.fromString("autumnFrameworkRunner")),
                        names.fromString("run")
                ),
                List.of(mainClass)
        );
        JCTree.JCExpressionStatement runStatement = treeMaker.Exec(runCall);


        method.body.stats = method.body.stats.prepend(runStatement);
        method.body.stats = method.body.stats.prepend(printStatement);
        method.body.stats = method.body.stats.prepend(autumnFrameworkRunner);
    }

    private static <T> T jbUnwrap(Class<? extends T> iface, T wrapper) {
        T unwrapped = null;
        try {
            final Class<?> apiWrappers = wrapper.getClass().getClassLoader()
                    .loadClass("org.jetbrains.jps.javac.APIWrappers");
            final Method unwrapMethod = apiWrappers.getDeclaredMethod("unwrap", Class.class, Object.class);
            unwrapped = iface.cast(unwrapMethod.invoke(null, iface, wrapper));
        } catch (Throwable ignored) {
        }
        return unwrapped != null ? unwrapped : wrapper;
    }

}