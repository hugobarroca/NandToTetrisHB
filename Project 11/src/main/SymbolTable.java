package main;

import main.enums.Kind;

import java.util.Hashtable;

public class SymbolTable {
    private Hashtable<String, Symbol> classScopeTable;
    private Hashtable<String, Symbol> subroutineScopeTable;
    private int staticIndex;
    private int fieldIndex;
    private int argIndex;
    private int varIndex;

    public SymbolTable() {
        classScopeTable = new Hashtable<String, Symbol>();
        subroutineScopeTable = new Hashtable<String, Symbol>();
        staticIndex = 0;
        fieldIndex = 0;
        argIndex = 0;
        varIndex = 0;
    }

    public void define(String name, String type, Kind kind) {
        if (kind == Kind.STATIC) {
            classScopeTable.put(name, new Symbol(staticIndex, name, type, kind));
            staticIndex++;
        }

        if (kind == Kind.FIELD) {
            classScopeTable.put(name, new Symbol(fieldIndex, name, type, kind));
            fieldIndex++;
        }

        if (kind == Kind.ARG) {
            subroutineScopeTable.put(name, new Symbol(argIndex, name, type, kind));
            argIndex++;
        }

        if (kind == Kind.VAR) {
            subroutineScopeTable.put(name, new Symbol(varIndex, name, type, kind));
            varIndex++;
        }
    }

    public void startSubroutine() {
        subroutineScopeTable.clear();
    }

    public int varCount(Kind kind) {
        if (kind == Kind.STATIC) {
            return staticIndex;
        }
        if (kind == Kind.FIELD) {
            return fieldIndex;
        }
        if (kind == Kind.ARG) {
            return argIndex;
        }
        return varIndex;

    }

    public Kind kindOf(String name) {
        Symbol symbolInSubroutine = subroutineScopeTable.get(name);
        Symbol symbolInClass = classScopeTable.get(name);

        if(symbolInSubroutine != null)
            return symbolInSubroutine.getKind();
        if(symbolInClass != null)
            return symbolInClass.getKind();
        return Kind.NONE;
    }

    public String typeOf(String name) {
        Symbol symbolInSubroutine = subroutineScopeTable.get(name);
        Symbol symbolInClass = classScopeTable.get(name);

        if(symbolInSubroutine != null)
            return symbolInSubroutine.getType();
        return symbolInClass.getType();
    }

    public int indexOf(String name) {
        Symbol symbolInSubroutine = subroutineScopeTable.get(name);
        Symbol symbolInClass = classScopeTable.get(name);

        if(symbolInSubroutine != null)
            return symbolInSubroutine.getIndex();
        return symbolInClass.getIndex();
    }

    public void clearSubroutineTable()
    {
        subroutineScopeTable = new Hashtable<String, Symbol>();
        argIndex = 0;
        varIndex = 0;
    }


}
