package com.github.MeFisto94.jMonkeyDiscordBot;

import com.github.javaparser.ast.body.FieldDeclaration;

import java.util.ArrayList;
import java.util.List;

public class Field {
    private final JavaClass parent;
    private final List<FieldDeclaration> declarationList;

    public Field(JavaClass parent, FieldDeclaration decl) {
        declarationList = new ArrayList<>();
        declarationList.add(decl);
        this.parent = parent;
    }

    public List<FieldDeclaration> getDeclarationList() {
        return declarationList;
    }

    public void addDeclaration(FieldDeclaration decl) {
        declarationList.add(decl);
    }

    public String getDeclarationAsString(int idx) {
        // This is a bit buggy still...
        return declarationList.get(idx).getVariables().get(0).toString();
    }

    public FieldDeclaration getDeclaration(int idx) {
        return declarationList.get(idx);
    }

    /**
     * How many versions of this field (including all overloads) exist.
     * @return The fieldcount
     */
    public int getFieldCount() {
        return declarationList.size();
    }

    public JavaClass getParent() {
        return parent;
    }

    public BranchOrCommit getBranchorCommit() {
        return parent.getBranchOrCommit();
    }
}
