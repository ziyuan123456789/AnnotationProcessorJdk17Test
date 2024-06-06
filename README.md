# 使用Jdk17创建注解处理器

# 项目使用链接:
访问 [AutumnFramework 项目](https://github.com/ziyuan123456789/AutumnFramework) 查看最新的实际使用代码和文档

## 1. 创建注解

```java

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface EnableAutumnFramework {
}
```
## 2. 创建注解处理器

```java
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
```

## 3.更新POM文件:
```xml
 <build>
        <finalName>AutumnAnP</finalName>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <fork>true</fork>
                    <compilerArgs>
                        <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED</arg>
                        <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED</arg>
                        <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED</arg>
                        <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED</arg>
                        <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED</arg>
                        <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED</arg>
                        <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED</arg>
                        <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED</arg>
                        <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED</arg>
                        <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.jvm=ALL-UNNAMED</arg>
                        <arg>--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED</arg>
                        <arg>--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED</arg>
                        <arg>--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED</arg>
                        <arg>--add-exports=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED</arg>
                        <arg>--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED</arg>
                        <arg>--add-exports=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED</arg>
                        <arg>--add-exports=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED</arg>
                        <arg>--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED</arg>
                        <arg>--add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED</arg>
                        <arg>--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED</arg>
                        <arg>--add-opens=jdk.compiler/com.sun.tools.javac.jvm=ALL-UNNAMED</arg>
                        <arg>--add-opens=jdk.compiler/com.sun.tools.javac.ClassWriter=ALL-UNNAMED</arg>
                    </compilerArgs>
                    <target>${maven.compiler.source}</target>
                    <source>${maven.compiler.target}</source>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>com.google.auto.service</groupId>
                            <artifactId>auto-service</artifactId>
                            <version>${auto-service.version}</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
            <plugin>
                <artifactId>maven-assembly-plugin</artifactId>
                <configuration>
                    <descriptorRefs>
                        <descriptorRef>jar-with-dependencies</descriptorRef>
                    </descriptorRefs>
                </configuration>
            </plugin>
        </plugins>
    </build>
```

## 4.打包为JAR
- `mvn clean install`

## 5.引入项目
- 1.把Jar包放入你的项目中,我放置于libs下,我的Jar包名为AutumnAnP.jar
- 2.把Jar包存入仓库 `mvn install:install-file -Dfile=src/main/resources/libs/AutumnAnP.jar -DgroupId=org.AutumnAP -DartifactId=AutumnAnP -Dversion=1.0-SNAPSHOT -Dpackaging=jar `
- 3.修改POM文件
```xml
<dependency>
    <groupId>org.AutumnAP</groupId>
    <artifactId>AutumnAnP</artifactId>
    <version>1.0-SNAPSHOT</version>
    <scope>compile</scope>
</dependency>
<build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <target>${maven.compiler.target}</target>
                    <source>${maven.compiler.source}</source>
                    <fork>true</fork>
                    <compilerArgs>
                        <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED</arg>
                        <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED</arg>
                        <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED</arg>
                        <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED</arg>
                        <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED</arg>
                        <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED</arg>
                        <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED</arg>
                        <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED</arg>
                        <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED</arg>
                        <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.jvm=ALL-UNNAMED</arg>
                    </compilerArgs>
                    <annotationProcessorPaths>

                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>1.18.32</version>
                        </path>
                     
                        <path>
                            <groupId>org.AutumnAP</groupId>
                            <artifactId>AutumnAnP</artifactId>
                            <version>1.0-SNAPSHOT</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
        </plugins>
    </build>
```
- 4.`mvn clean install`
- 5.在IDEA增加编译参数`-Djps.track.ap.dependencies=false`,位于编译期,Java编译期,附加命令行形参