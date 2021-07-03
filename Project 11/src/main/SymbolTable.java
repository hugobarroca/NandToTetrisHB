package main;

import java.util.Hashtable;

public class SymbolTable {
    private Hashtable<Integer, Symbol> classScopeTable;
    private Hashtable<Integer, Symbol> subroutineScopeTable;
    private int currentIndex;

    public SymbolTable(){
        classScopeTable = new Hashtable<Integer, Symbol>();
        subroutineScopeTable = null;
        currentIndex = 0;
    }

    public void define(String name, String type, String kind){
        if(subroutineScopeTable == null){
            classScopeTable.put(currentIndex, new Symbol(currentIndex, name, type, kind));
        }else{
            subroutineScopeTable.put(currentIndex, new Symbol(currentIndex, name, type, kind));
        }
        currentIndex++;
    }

    public void startSubroutine(){
        subroutineScopeTable.clear();
    }

    public int varCount(String kind){
        int count = 0;
        int i = 0;
        while (i < currentIndex){
            Symbol symbol = classScopeTable.get(i);
            if(symbol.getKind() == kind){
                count += 1;
            }
            i++;
        }
        while (i < currentIndex){
            Symbol symbol = subroutineScopeTable.get(i);
            if(symbol.getKind() == kind){
                count += 1;
            }
            i++;
        }
        return count;
    }

    public String kindOf(String name){
        int i = 0;
        while (i < currentIndex){
            Symbol symbol = classScopeTable.get(i);
            if(symbol.getName() == name){
                return symbol.getKind();
            }
            i++;
        }
        i=0;
        while (i < currentIndex){
            Symbol symbol = subroutineScopeTable.get(i);
            if(symbol.getName() == name){
                return symbol.getKind();
            }
            i++;
        }
        return "NONE";
    }

    public String typeOf(String name){
        int i = 0;
        while (i < currentIndex){
            Symbol symbol = classScopeTable.get(i);
            if(symbol.getName() == name){
                return symbol.getType();
            }
            i++;
        }
        i=0;
        while (i < currentIndex){
            Symbol symbol = subroutineScopeTable.get(i);
            if(symbol.getName() == name){
                return symbol.getType();
            }
            i++;
        }
        return "NONE";
    }

    public int indexOf(String name){
        int i = 0;
        while (i < currentIndex){
            Symbol symbol = classScopeTable.get(i);
            if(symbol.getName() == name){
                return symbol.getIndex();
            }
            i++;
        }
        i=0;
        while (i < currentIndex){
            Symbol symbol = subroutineScopeTable.get(i);
            if(symbol.getName() == name){
                return symbol.getIndex();
            }
            i++;
        }
        return -1;
    }




}
