package com.github.MeFisto94.jMonkeyDiscordBot;

import com.github.javaparser.ast.body.MethodDeclaration;

import java.util.ArrayList;
import java.util.List;

public class Method {
    private final JavaClass parent;
    private final List<MethodDeclaration> declarationList;

    public Method(JavaClass parent, MethodDeclaration decl) {
        this.parent = parent;
        declarationList = new ArrayList<>();
        declarationList.add(decl);
    }

    public List<MethodDeclaration> getDeclarationList() {
        return declarationList;
    }

    public void addDeclaration(MethodDeclaration decl) {
        declarationList.add(decl);
    }

    public MethodDeclaration getDeclaration(int idx) {
        return declarationList.get(idx);
    }

    public String getDeclarationAsString(int idx) {
        return declarationList.get(idx).getDeclarationAsString(true, true, true);
    }

    /**
     * How many versions of this method (including all overloads) exist.
     * @return The Method Count
     */
    public int getMethodCount() {
        return declarationList.size();
    }

    public JavaClass getParent() {
        return parent;
    }

    public BranchOrCommit getBranchOrCommit() {
        return parent.getBranchOrCommit();
    }
}
