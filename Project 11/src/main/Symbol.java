package main;

public class Symbol {
    private int index;
    private String name;
    private String type;
    private String kind;

    public Symbol(int index, String name, String type, String kind){
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

    public String getKind() {
        return kind;
    }

    public String getType() {
        return type;
    }
}
