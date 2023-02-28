package com.github.MeFisto94.jMonkeyDiscordBot;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.printer.YamlPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class JavaClass extends AbstractProcessableFile {
    File file;
    CompilationUnit compilationUnit;
    HashMap<String, Method> methodMap;
    HashMap<String, Field> fieldMap;
    Module parent;

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaClass.class);

    public JavaClass(Module parent, File file) {
        this.parent = parent;
        this.file = file;
        methodMap = new HashMap<>();
        fieldMap = new HashMap<>();
    }

    @Override
    public void process() {
        try {
            // TODO: cache JavaParser somewhere.
            compilationUnit = new JavaParser().parse(file).getResult() //
                    .orElseThrow(() -> new IllegalStateException("Error when parsing " + file));

            // TODO: In the future, we may want to parse more than just the primary type
            var primaryTypeOpt = compilationUnit.getPrimaryType();
            if (primaryTypeOpt.isEmpty()) {
                LOGGER.warn("Could not get the primary type of {}", file.getName());
            } else {
                parseMethods(primaryTypeOpt.get());
                parseFields(primaryTypeOpt.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public File getFile() {
        return file;
    }

    public Module getParent() {
        return parent;
    }

    public CompilationUnit getCU() {
        if (!isProcessed()) {
            throw new IllegalStateException("This Class has not been processed yet.");
        }

        return compilationUnit;
    }

    public String getPackage() {
        return compilationUnit.getPackageDeclaration().get().getName().asString();
    }

    public String getTypeName() {
        return compilationUnit.getPrimaryTypeName().orElse(null);
    }

    public boolean isEnum() {
        return compilationUnit.getPrimaryType().get() instanceof EnumDeclaration;
    }

    public boolean isInterface() {
        if (compilationUnit.getPrimaryType().get() instanceof ClassOrInterfaceDeclaration) {
            return ((ClassOrInterfaceDeclaration)compilationUnit.getPrimaryType().get()).isInterface();
        }

        return false;
    }

    public boolean isAnnotation() {
        if (compilationUnit.getPrimaryType().get() instanceof AnnotationDeclaration) {
            return ((AnnotationDeclaration)compilationUnit.getPrimaryType().get()).isAnnotationDeclaration();
        }

        return false;
    }

    public boolean isAbstractClass() {
        if (compilationUnit.getPrimaryType().get() instanceof ClassOrInterfaceDeclaration) {
            ClassOrInterfaceDeclaration cod = (ClassOrInterfaceDeclaration)compilationUnit.getPrimaryType().get();
            if (!cod.isInterface()) {
                return cod.getModifiers().contains(Modifier.abstractModifier());
            }
        }

        return false;
    }

    public boolean isRegularClass() {
        if (compilationUnit.getPrimaryType().get() instanceof ClassOrInterfaceDeclaration) {
            ClassOrInterfaceDeclaration cod = (ClassOrInterfaceDeclaration)compilationUnit.getPrimaryType().get();
            if (!cod.isInterface()) {
                return !cod.getModifiers().contains(Modifier.abstractModifier());
            }
        }

        return false;
    }

    public String typeToString() {
        if (isEnum()) {
            return "enum";
        } else if (isInterface()) {
            return "interface";
        } else if (isRegularClass()) {
            return "class";
        } else if (isAnnotation()) {
            return "annotation";
        } else if (isAbstractClass()) {
            return "abstract class";
        } else {
            return "unknown";
        }
    }

    // METHODS
    protected void parseMethods(TypeDeclaration<?> type) {
        // @TODO: There are caveats here: What about inner classes? These are other types than the Primary Type
        // @TODO: Would be a visitor (think SceneGraphVisitor) be an appropriate solution?
        type.getMembers().stream()
                .filter(BodyDeclaration::isMethodDeclaration)
                .map(b -> (MethodDeclaration)b)
                .forEach(this::addMethod);
    }

    protected void addMethod(MethodDeclaration m) {
        if (methodMap.containsKey(m.getNameAsString())) {
            methodMap.get(m.getNameAsString()).addDeclaration(m);
        } else {
            methodMap.put(m.getNameAsString(), new Method(this, m));
        }
    }

    // FIELDS
    protected void parseFields(TypeDeclaration<?> type) {
        // @TODO: There are caveats here: What about inner classes? These are other types than the Primary Type
        // @TODO: Would be a visitor (think SceneGraphVisitor) be an appropriate solution?
            type.getMembers().stream()
                    .filter(BodyDeclaration::isFieldDeclaration)
                    .map(b -> (FieldDeclaration) b)
                    .forEach(this::addField);
    }

    protected void addField(FieldDeclaration f) {
        // "int foo, bar;" is an example of two variables in one field.
        f.getVariables().forEach(variable -> {
            String name = variable.getNameAsString();

            if (fieldMap.containsKey(name)) {
                LOGGER.error("BBBBBBB####################");
                fieldMap.get(name).addDeclaration(f);
            } else {
                fieldMap.put(name, new Field(this, f));
            }
        });

    }

    public String toYAML() {
        return new YamlPrinter(true).output(compilationUnit);
    }

    public String getContent() {
        return compilationUnit.toString();
    }

    public String getContent(int startingLine, int length) {
        return Arrays.stream(getContent().split(Main.lineDelimit))
                .skip(startingLine - 1)
                .limit(length)
                .collect(Collectors.joining(Main.lineDelimit));
    }

    public String getContentFromTo(int startingLine, int endingLine) {
        return getContent(startingLine, endingLine - startingLine);
    }

    public HashMap<String, Method> getMethodMap() {
        return methodMap;
    }

    public HashMap<String, Field> getFieldMap() {
        return fieldMap;
    }

    public @Nullable
    Method findMethodByName(@Nonnull String methodName) {
        return getMethodMap().get(methodName);
    }

    public @Nullable Method findMethodByNameIgnoreCase(@Nonnull String methodName) {
        return getMethodMap()
                .entrySet().stream()
                .filter(e -> e.getKey().equalsIgnoreCase(methodName))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }


    /* This method is designed to reduce the cost in the average case, assuming that most names are typed correctly.
       The cost in the worst case are larger though as every call to findMethodByName() will fail and thus
       findMethodByNameIgnoreCase could be used. If, however, the names are correct, the expensive findIgnoreCase
       can be left out
     */
    public @Nullable Method findMethodBothCases(@Nonnull String methodName) {
        Method m = findMethodByName(methodName);

        if (m == null) {
            m = findMethodByNameIgnoreCase(methodName);
        }

        return m;
    }

    public @Nullable
    Field findFieldByName(@Nonnull String fieldName) {
        return getFieldMap().get(fieldName);
    }

    public @Nullable Field findFieldByNameIgnoreCase(@Nonnull String fieldName) {
        return getFieldMap()
                .entrySet().stream()
                .filter(e -> e.getKey().equalsIgnoreCase(fieldName))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }


    /* This method is designed to reduce the cost in the average case, assuming that most names are typed correctly.
       The cost in the worst case are larger though as every call to findMethodByName() will fail and thus
       findMethodByNameIgnoreCase could be used. If, however, the names are correct, the expensive findIgnoreCase
       can be left out
     */
    public @Nullable Field findFieldBothCases(@Nonnull String fieldName) {
        Field f = findFieldByName(fieldName);

        if (f == null) {
            f = findFieldByNameIgnoreCase(fieldName);
        }

        return f;
    }

    public BranchOrCommit getBranchOrCommit() {
        return getParent().getBranchOrCommit();
    }
}
