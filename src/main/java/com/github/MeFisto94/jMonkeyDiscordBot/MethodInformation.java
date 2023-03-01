package com.github.MeFisto94.jMonkeyDiscordBot;

import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.javadoc.Javadoc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * This Class is responsible for formatting/outputting the information
 *
 */
public class MethodInformation {
    private final Method method;
    private @Nullable MethodDeclaration methodDeclaration;
    private @Nullable ConstructorDeclaration constructorDeclaration;

    public MethodInformation(Method method, @Nonnull MethodDeclaration methodDeclaration) {
        this.method = method;
        this.methodDeclaration = methodDeclaration;
    }

    public MethodInformation(Method method, @Nonnull ConstructorDeclaration constructorDeclaration) {
        this.method = method;
        this.constructorDeclaration = constructorDeclaration;
    }

    public String toString() {
        StringBuilder b = new StringBuilder(extractSignature() + "\n");
        b.append(getContent(5));
        b.append(Main.lineDelimit);
        b.append("[...]");
        b.append(Main.lineDelimit);
        String javaDoc = extractJavaDoc();

        if (javaDoc != null) {
            b.append("JavaDoc: \n");
            b.append(javaDoc);
        }

        return b.toString();
    }

    public String extractSignature() {
        if (methodDeclaration != null) {
            return methodDeclaration.getDeclarationAsString(true, true, true);
        } else {
            return constructorDeclaration.getDeclarationAsString(true, true, true);
        }
    }

    public @Nullable String extractJavaDoc() {
        if (methodDeclaration != null) {
            if (methodDeclaration.hasJavaDocComment()) {
                if (Main.javadocAsCode) {
                    return methodDeclaration.getJavadocComment().map(Object::toString).orElse(null);
                } else {
                    return methodDeclaration.getJavadoc().map(Javadoc::toText).orElse(null);
                }
            } else {
                return null;
            }
        } else if (constructorDeclaration != null) {
            if (constructorDeclaration.hasJavaDocComment()) {
                if (Main.javadocAsCode) {
                    return constructorDeclaration.getJavadocComment().map(Object::toString).orElse(null);
                } else {
                    return constructorDeclaration.getJavadocComment().map(Object::toString).orElse(null);
                }
            } else {
                return null;
            }
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * Returns the Body (Code) of the Method (minus the declaration).
     * @return The Body of the Method
     */
    public String getContent() {
        // @TODO: Fails for Interfaces
        if (methodDeclaration != null) {
            return methodDeclaration.getBody().get().toString();
        } else {
            return constructorDeclaration.getBody().toString();
        }
        //return decl.toString();
    }

    public String getContent(int startingLine, int numLines) {
        return Arrays.stream(getContent().split(Main.lineDelimit))
                .skip(startingLine - 1)
                .limit(numLines)
                .collect(Collectors.joining(Main.lineDelimit));
    }

    public String getContent(int numLines) {
        return getContent(1, numLines);
    }

    public String getGithubLink() {
        // @TODO: Extract the github URL out of the GitRepo, maybe also move these methods out to GitRepo or something.
        BranchOrCommit boc = method.getBranchOrCommit();
        // Example: https://github.com/jMonkeyEngine/jmonkeyengine/blob/master/jme3-core/src/main/java/com/jme3/scene/Spatial.java#L596
        String relPath = method.getParent().getParent().getRootPath().toPath().relativize(method.getParent().getFile().toPath()).toString();
        // We could add #L500 to relPath
        return String.format("https://github.com/jMonkeyEngine/jmonkeyengine/blob/%s/%s", boc.getContent(), relPath);
    }

    public @Nullable String getJavadocLink() {
        BranchOrCommit boc = method.getBranchOrCommit();
        if (boc.isCommit() || !boc.getContent().equals("master")) {
            return null; // Can only operate on branch master
        }

        // @TODO: For overloads we have to care about the parameters, but currently we don't even know Method/JavaClass
        // Example: https://javadoc.jmonkeyengine.org/com/jme3/math/Vector3f.html#addLocal-float-float-float
        String packagePath = method.getParent().getPackage().replace(".", "/") + "/" + method.getParent().getTypeNameOrThrow();
        return String.format("https://javadoc.jmonkeyengine.org/%s.html", packagePath);
    }
}
