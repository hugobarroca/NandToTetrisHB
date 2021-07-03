package main;

import main.enums.Kind;

public class Symbol {
    private int index;
    private String name;
    private String type;
    private Kind kind;

    public Symbol(int index, String name, String type, Kind kind){
        this.index = index;
        this.name = name;
        this.type = type;
        this.kind = kind;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public Kind getKind() {
        return kind;
    }

    public String getType() {
        return type;
    }
}
