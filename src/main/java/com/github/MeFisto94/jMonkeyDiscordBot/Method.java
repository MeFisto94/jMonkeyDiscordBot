package com.github.MeFisto94.jMonkeyDiscordBot;

import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Method {
    private final JavaClass parent;
    private final List<MethodDeclaration> declarationList;

    private final List<ConstructorDeclaration> constructorList;

    private Method(JavaClass parent) {
        this.parent = parent;
        declarationList = new ArrayList<>();
        constructorList = new ArrayList<>();
    }

    public Method(JavaClass parent, MethodDeclaration decl) {
        this(parent);
        declarationList.add(decl);
    }

    public Method(JavaClass parent, ConstructorDeclaration decl) {
        this(parent);
        constructorList.add(decl);
    }

    public void addDeclaration(MethodDeclaration decl) {
        declarationList.add(decl);
    }

    public void addDeclaration(ConstructorDeclaration decl) {
        constructorList.add(decl);
    }

    public Collection<MethodDeclaration> getMethodDeclarations() {
        return declarationList;
    }
    public Collection<ConstructorDeclaration> getConstructorDeclarations() {
        return constructorList;
    }

    public String getDeclarationAsString(int idx) {
        return declarationList.get(idx).getDeclarationAsString(true, true, true);
    }

    /**
     * How many versions of this method (including all overloads) exist.
     * <b>Note:</b> This also includes constructors.
     * @return The Method Count
     */
    public int getMethodCount() {
        return declarationList.size() + constructorList.size();
    }

    public JavaClass getParent() {
        return parent;
    }

    public BranchOrCommit getBranchOrCommit() {
        return parent.getBranchOrCommit();
    }
}
