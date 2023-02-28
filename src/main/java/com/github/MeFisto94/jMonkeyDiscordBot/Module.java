package com.github.MeFisto94.jMonkeyDiscordBot;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Module {
    File rootPath;
    File folder;
    File srcDir;
    List<JavaClass> javaClasses;
    HashMap<String, JavaClass> classNameMap;
    BranchOrCommit boc;

    public Module(File rootPath, File folder, BranchOrCommit boc) {
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("A Module has to consist of a Directory");
        }

        srcDir = new File(folder, "src");
        if (!srcDir.exists() || !srcDir.isDirectory()) {
            throw new IllegalArgumentException("Could not find Module's src directory");
        }

        this.folder = folder;
        this.rootPath = rootPath;
        this.boc = boc;
        javaClasses = new ArrayList<>();
        classNameMap = new HashMap<>();
    }

    public String getName() {
        return folder.getName();
    }

    public File getRootPath() {
        return rootPath;
    }

    public void parseModule() {
        try {
            Files.walk(srcDir.toPath())
                .filter(f -> f.toFile().isFile())
                .filter(f -> !f.toString().contains("checkers"))
                .filter(f -> f.getFileName().toString().endsWith(".java"))
                .filter(f -> !f.getFileName().toString().equalsIgnoreCase("package-info.java"))
                .filter(f -> !Main.Dev_Powersave || f.getFileName().toString().equals("Vector3f.java"))
                //.limit(10)
                .forEach(f -> {
                    if (f.getFileName().toString().endsWith(".java")) {
                        //if (javaClasses.isEmpty())
                            javaClasses.add(new JavaClass(this, f.toFile()));
                    }
                });

            javaClasses.forEach(ProcessableFile::processFile);

            /*javaClasses.forEach(jc -> {
                System.out.println(jc.getFile().getName());
                CompilationUnit cu = jc.getCU();

                System.out.println("Package: " + jc.getPackage());
                System.out.println("Type: " + jc.typeToString());
                System.out.println("TypeName: " + jc.getTypeName());

                System.out.println(cu.toString());
            });*/

            /*javaClasses.forEach(jc -> {
                System.out.println("Klasse " + jc.getTypeName() + " in " + jc.getPackage() + " type: " +  jc.typeToString());
                System.out.println("METHODS: ");
                jc.getMethodMap().values().forEach(m -> System.out.println(m.getDeclarationAsString(0)));
                System.out.println("FIELDS: ");
                jc.getFieldMap().values().forEach(f -> System.out.println(f.getDeclarationAsString(0)));
            });*/

            javaClasses.forEach(jc -> classNameMap.put(jc.getTypeName(), jc));
        } catch (IOException io) {
            System.out.println("IOException in parseModule()");
        }
    }

    public List<JavaClass> getJavaClasses() {
        return javaClasses;
    }

    public @Nullable JavaClass findClassByName(@Nonnull String className) {
        return classNameMap.get(className);
    }

    public @Nullable JavaClass findClassByNameIgnoreCase(@Nonnull String className) {
        return classNameMap
                .entrySet().stream()
                .filter(e -> e.getKey().equalsIgnoreCase(className))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }


    /* This method is designed to reduce the cost in the average case, assuming that most names are typed correctly.
       The cost in the worst case are larger though as every call to findClassByName() will fail and thus
       findClassByNameIgnoreCase could be used. If, however, the names are correct, the expensive findIgnoreCase
       can be left out
     */
    public @Nullable JavaClass findClassBothCases(@Nonnull String className) {
        JavaClass c = findClassByName(className);

        if (c == null) {
            c = findClassByNameIgnoreCase(className);
        }

        return c;
    }

    public BranchOrCommit getBranchOrCommit() {
        return boc;
    }
}
