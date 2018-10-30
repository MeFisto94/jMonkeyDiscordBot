package com.github.MeFisto94.jMonkeyDiscordBot;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * This Class is responsible for formatting/outputting the information
 *
 */
public class FieldInformation {
    FieldDeclaration decl;
    Field field;

    public FieldInformation(Field field, FieldDeclaration decl) {
        this.field = field;
        this.decl = decl;
    }

    public FieldInformation(Field field, int idx) {
        this(field, field.getDeclaration(idx));
    }

    public String toString() {
        StringBuilder b = new StringBuilder(extractSignature() + "\n");
        b.append(Main.lineDelimit);
        String javaDoc = extractJavaDoc();

        if (javaDoc != null) {
            b.append("JavaDoc: \n");
            b.append(javaDoc);
        }

        return b.toString();
    }

    public String extractSignature() {
        // @TODO: What if variables.count > 1?
        //@TODO: Where does that "Optional[]" come from with the modifiers.
        return decl.getModifiers().stream().map(Modifier::asString).reduce(String::join) +
        " " + decl.getVariable(0).getType().asString() + " " +
        decl.getVariable(0).getName().asString() + ";";
    }

    public @Nullable String extractJavaDoc() {
        if (decl.hasJavaDocComment()) {
            if (Main.javadocAsCode) {
                return decl.getJavadocComment().get().toString();
            } else {
                return decl.getJavadoc().get().toText();
            }
        } else {
            return null;
        }
    }

    /**
     * Returns the Body (Code) of the Method (minus the declaration).
     * @return The Body of the Method
     */
    public String getContent() {
        return ""; // No Content, we only have a declaration
        //return decl.getBody().get().toString();
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
        BranchOrCommit boc = field.getBranchorCommit();
        // Example: https://github.com/jMonkeyEngine/jmonkeyengine/blob/master/jme3-core/src/main/java/com/jme3/scene/Spatial.java#L596
        String relPath = field.getParent().getParent().getRootPath().toPath().relativize(field.getParent().getFile().toPath()).toString();
        // We could add #L500 to relPath
        return String.format("https://github.com/jMonkeyEngine/jmonkeyengine/blob/%s/%s", boc.getContent(), relPath);
    }

    public @Nullable String getJavadocLink() {
        BranchOrCommit boc = field.getBranchorCommit();
        if (boc.isCommit() || !boc.getContent().equals("master")) {
            return null; // Can only operate on branch master
        }

        // @TODO: For overloads we have to care about the parameters, but currently we don't even know Method/JavaClass
        // Example: https://javadoc.jmonkeyengine.org/com/jme3/math/Vector3f.html#addLocal-float-float-float
        String packagePath = field.getParent().getPackage().replace(".", "/") + "/" + field.getParent().getTypeName();
        return String.format("https://javadoc.jmonkeyengine.org/%s.html", packagePath);
    }
}
