package main;

import main.enums.Command;
import main.enums.Kind;
import main.enums.Segment;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Locale;

public class CompilationEngine {
    JackTokenizer tokenizer;
    SymbolTable symbolTable;
    VMWriter writer;
    File inputFile;

    String currentClassName;
    String subroutineReturnType;
    String subroutineType;

    boolean isMethod;

    int whileLabelCounter;
    int ifLabelCounter;

    public CompilationEngine(File inputFile, File outputVMFile) throws IOException {
        this.inputFile = inputFile;
        tokenizer = new JackTokenizer(inputFile, true);
        symbolTable = new SymbolTable();
        writer = new VMWriter(outputVMFile);

        currentClassName = "";
        subroutineReturnType = "";
        whileLabelCounter = 0;
        ifLabelCounter = 0;
        isMethod = false;
    }

    public void compileClass() throws IOException {

        tokenizer.advanceToken(); //keyword: class
        currentClassName = compileIdentifier();
        tokenizer.advanceToken(); //symbol: {

        while (isSubroutine() || isClassVarDec()) {
            if (isSubroutine()) {
                compileSubroutine();
            } else {
                compileClassVarDec();
            }
        }
        tokenizer.advanceToken(); //symbol: }
        writer.close();
    }

    public void compileClassVarDec() {
        var kind = compileKeyword();
        var type = compileIdentifierOrKeyword();
        var identifier = compileIdentifier();

        symbolTable.define(identifier, type, Kind.valueOf(kind.toUpperCase(Locale.ROOT)));

        while (tokenizer.symbol().equals(",")) {
            tokenizer.advanceToken();         // symbol: ","
            identifier = compileIdentifier();
            symbolTable.define(identifier, type, Kind.valueOf(kind.toUpperCase(Locale.ROOT)));
        }
        tokenizer.advanceToken(); // symbol: ")"
    }

    public void compileSubroutine() {
        String keyword = compileKeyword();  //keyword: method || function || constructor
        //return type
        if (tokenizer.tokenType().equals("identifier")) {
            subroutineReturnType = compileIdentifier();
        } else {
            subroutineReturnType = compileKeyword();
        }


        String identifier = currentClassName + "." + compileIdentifier();
        tokenizer.advanceToken(); //symbol: "("
        compileParameterList();
        tokenizer.advanceToken(); //symbol: ")"
        tokenizer.advanceToken(); //symbol: "{"

        int nrOfLocalVariables = 0;
        while (tokenizer.keyWord().equals("var")) {
            nrOfLocalVariables += compileVarDec();
        }

        writer.writeFunction(identifier, nrOfLocalVariables);

        if (keyword.equals("method")) { //Puts the reference to the method's object in the pointer segment.
            writer.writePush(Segment.ARGUMENT, 0);
            writer.writePop(Segment.POINTER, 0);
        }


        if (keyword.equals("constructor")) { //Allocates memory for the object (depending on the number of fields) and puts it's memory address on pointer 0.
            writer.writePush(Segment.CONSTANT, symbolTable.varCount(Kind.FIELD));
            writer.writeCall("Memory.alloc", 1);
            writer.writePop(Segment.POINTER, 0);
        }

        compileStatements();
        symbolTable.clearSubroutineTable();
        tokenizer.advanceToken(); //symbol: "}"
        subroutineReturnType = "";
    }

    public int compileParameterList() {
        int counter = 0;
        if (tokenizer.tokenType().equals("identifier") || tokenizer.keyWord().equals("int") || tokenizer.keyWord().equals("char") || tokenizer.keyWord().equals("boolean")) {
            var type = compileIdentifierOrKeyword();
            var name = compileIdentifier();
            symbolTable.define(name, type, Kind.ARG);

            while (tokenizer.symbol().equals(",")) {
                tokenizer.advanceToken();       // symbol: ","
                type = compileIdentifierOrKeyword();
                name = compileIdentifier();
                symbolTable.define(name, type, Kind.ARG);
                counter++;
            }
        }
        return counter;
    }

    public int compileVarDec() {
        var kind = compileKeyword();
        int count = 1;
        String type;

        if (tokenizer.tokenType().equals("identifier")) {
            type = compileIdentifier();
        } else {
            type = compileKeyword();
        }

        var identifier = compileIdentifier();

        symbolTable.define(identifier, type, Kind.valueOf(kind.toUpperCase(Locale.ROOT)));


        while (tokenizer.symbol().equals(",")) {
            tokenizer.advanceToken();       // symbol: ","
            identifier = compileIdentifier();
            symbolTable.define(identifier, type, Kind.valueOf(kind.toUpperCase(Locale.ROOT)));
            count++;
        }
        tokenizer.advanceToken(); //symbol: ";"
        return count;

    }

    public void compileStatements() {
        while (isStatement()) {
            switch (tokenizer.keyWord()) {
                case "let" -> compileLet();
                case "if" -> compileIf();
                case "while" -> compileWhile();
                case "do" -> compileDo();
                case "return" -> compileReturn();
            }
        }
    }

    public void compileDo() {
        compileKeyword(); //keyword: do
        if (tokenizer.nextSymbol().equals("(")) {   //IF: It's a method of the current class
            var subroutineName = compileIdentifier(); //identifier: method name
            tokenizer.advanceToken(); //symbol: "("
            writer.writePush(Segment.POINTER, 0);
            int nrOfArguments = compileExpressionList();
            tokenizer.advanceToken(); //symbol ")"
            writer.writeCall(this.currentClassName + "." + subroutineName, nrOfArguments + 1);
        }
        if (tokenizer.nextSymbol().equals(".") && !symbolTable.kindOf(tokenizer.symbol()).toString().equals("NONE")) {                                    //ELSE: It's a method of another class
            var identifier = compileIdentifier(); //identifier: class name
            var className = symbolTable.typeOf(identifier);
            var kind = symbolTable.kindOf(identifier);
            var segment = getSegment(kind);

            tokenizer.advanceToken(); //symbol: "."
            String functionName = compileIdentifier(); //identifier: function name
            tokenizer.advanceToken(); //symbol: "("
            writer.writePush(segment, symbolTable.indexOf(identifier));
            int nrOfArguments = compileExpressionList() + 1;
            tokenizer.advanceToken(); //symbol ")"
            writer.writeCall(className + "." + functionName, nrOfArguments);
        }
        if (tokenizer.nextSymbol().equals(".") && symbolTable.kindOf(tokenizer.symbol()).toString().equals("NONE")) {                                    //ELSE: It's a function
            var className = compileIdentifier(); //identifier: class name
            tokenizer.advanceToken(); //symbol: "."
            String functionName = compileIdentifier(); //identifier: function name
            tokenizer.advanceToken(); //symbol: "("
            int nrOfArguments = compileExpressionList();
            tokenizer.advanceToken(); //symbol ")"
            writer.writeCall(className + "." + functionName, nrOfArguments);
        }
        writer.writePop(Segment.TEMP, 0); //The value returned by a DO statement will never be saved, so we always pop it.
        tokenizer.advanceToken(); //symbol ";"
    }

    public void compileLet() {
        compileKeyword();           //keyword: "let"
        var variableName = compileIdentifier();        //variable name
        var kind = symbolTable.kindOf(variableName);
        var index = symbolTable.indexOf(variableName);

        boolean isArray = false;

        int arrayIndex = 0;
        if (tokenizer.symbol().equals("[")) {
            isArray = true;
            compileOperation();     //symbol: "["
            compileExpression();
            compileOperation();     //symbol: "]"
        }

        if (isArray) {
            if (kind == Kind.STATIC)
                writer.writePush(Segment.STATIC, index);
            if (kind == Kind.VAR)
                writer.writePush(Segment.LOCAL, index);
            if (kind == Kind.ARG){
                if (isMethod) {
                    writer.writePush(Segment.ARGUMENT, index + 1);
                } else {
                    writer.writePush(Segment.ARGUMENT, index);
                }
            }

            if (kind == Kind.FIELD)
                writer.writePush(Segment.THIS, index);
            writer.writeArithmetic(Command.ADD);

            tokenizer.advanceToken();     //symbol: "="
            compileExpression();
            tokenizer.advanceToken();     //symbol: ";"

            writer.writePush(Segment.TEMP, 0);

            writer.writePop(Segment.POINTER, 1);


            writer.writePop(Segment.TEMP, 0);

            writer.writePop(Segment.THAT, 0);
        } else {
            tokenizer.advanceToken();     //symbol: "="
            compileExpression();
            tokenizer.advanceToken();     //symbol: ";"

            if (kind == Kind.STATIC)
                writer.writePop(Segment.STATIC, index);
            if (kind == Kind.VAR)
                writer.writePop(Segment.LOCAL, index);
            if (kind == Kind.ARG)
                writer.writePop(Segment.ARGUMENT, index);
            if (kind == Kind.FIELD)
                writer.writePop(Segment.THIS, index);
        }


    }

    public void compileWhile() {
        whileLabelCounter++;
        var currentWhileLabel = whileLabelCounter;
        writer.writeLabel("WHILE_EXP" + currentWhileLabel);

        compileKeyword();                   //keyword: "while"
        tokenizer.advanceToken();           //symbol: "("
        compileExpression();
        writer.writeArithmetic(Command.NOT);
        writer.writeIf("WHILE_END" + currentWhileLabel);
        tokenizer.advanceToken();           //symbol: ")"
        tokenizer.advanceToken();           //symbol: "{"
        compileStatements();
        tokenizer.advanceToken();           //symbol: "}"
        writer.writeGoto("WHILE_EXP" + currentWhileLabel);
        writer.writeLabel("WHILE_END" + currentWhileLabel);

    }

    public void compileReturn() {
        compileKeyword();
        if (!tokenizer.symbol().equals(";")) {
            compileExpression();
        }
        if (subroutineReturnType.equals("void")) {
            writer.writePush(Segment.CONSTANT, 0);
        }

        tokenizer.advanceToken();           //symbol: ";"
        writer.writeReturn();
    }

    public void compileIf() {
        int currentIfCounter = ifLabelCounter;
        ifLabelCounter++;
        compileKeyword();                   //keyword: "if"
        tokenizer.advanceToken();           //symbol: "("
        compileExpression();
        writer.writeIf("IF_TRUE" + currentIfCounter);
        writer.writeGoto("IF_FALSE" + currentIfCounter);
        writer.writeLabel("IF_TRUE" + currentIfCounter);
        tokenizer.advanceToken();           //symbol: ")"
        tokenizer.advanceToken();           //symbol: "{"

        compileStatements();
        tokenizer.advanceToken();           //symbol: "}"
        writer.writeGoto("IF_END" + currentIfCounter);
        writer.writeLabel("IF_FALSE" + currentIfCounter);
        if (tokenizer.keyWord().equals("else")) {
            compileKeyword();
            tokenizer.advanceToken();           //symbol: "{"
            compileStatements();
            tokenizer.advanceToken();           //symbol: "}"
        }
        writer.writeLabel("IF_END" + currentIfCounter);
    }


    public void compileExpression() {
        compileTerm();
        while (isOperation()) {
            String symbol = tokenizer.symbol();
            tokenizer.advanceToken();
            compileTerm();
            compileOperation(symbol);
        }
    }

    public void compileTerm() {
        if (tokenizer.symbol().equals("(")) {
            tokenizer.advanceToken();
            compileExpression();
            tokenizer.advanceToken();
        } else if (tokenizer.tokenType().equals("integerConstant")) {
            writer.writePush(Segment.CONSTANT, Integer.parseInt(tokenizer.intVal()));
            tokenizer.advanceToken();
        } else if (tokenizer.tokenType().equals("stringConstant")) {
            //IMPLEMENT STRINGS
            var stringConstant = tokenizer.stringVal();
            var nrOfChars = stringConstant.length();
            writer.writePush(Segment.CONSTANT, nrOfChars);
            writer.writeCall("String.new", 1);
            for (int i = 0; i < nrOfChars; i++) {
                char c = stringConstant.charAt(i);
                var asciiCode = (int) c;
                if (asciiCode == 10) { //new line
                    writer.writePush(Segment.CONSTANT, 128);
                }
                if (asciiCode == 8) { //backspace
                    writer.writePush(Segment.CONSTANT, 129);
                }
                if (asciiCode != 8 && asciiCode != 10) {
                    writer.writePush(Segment.CONSTANT, asciiCode);
                }
                writer.writeCall("String.appendChar", 2);

            }
            tokenizer.advanceToken();
        } else if (tokenizer.tokenType().equals("keyword")) {
            if (tokenizer.keyWord().equals("true")) {
                writer.writePush(Segment.CONSTANT, 1);
                writer.writeArithmetic(Command.NEG);
            }
            if (tokenizer.keyWord().equals("false")) {
                writer.writePush(Segment.CONSTANT, 0);
            }
            if (tokenizer.keyWord().equals("this")) {
                writer.writePush(Segment.POINTER, 0);
            }
            tokenizer.advanceToken();
        } else if (tokenizer.tokenType().equals("identifier") && tokenizer.nextSymbol().equals("[")) {
            var identifier = compileIdentifier(); // variable holding the base array reference
            var kind = symbolTable.kindOf(identifier);
            var segment = getSegment(kind);
            var index = symbolTable.indexOf(identifier);
            tokenizer.advanceToken(); // [
            compileExpression(); // expression leading to the index of the array that we're pulling data from
            tokenizer.advanceToken(); // ]
            writer.writePush(segment, index);
            writer.writeArithmetic(Command.ADD);
            writer.writePop(Segment.POINTER, 1);
            writer.writePush(Segment.THAT, 0);
        } else if (tokenizer.tokenType().equals("identifier") && (tokenizer.nextSymbol().equals("(") || tokenizer.nextSymbol().equals("."))) { // It's either a method or a function call
            if (tokenizer.nextSymbol().equals(".")) {
                var identifier = compileIdentifier();  // Either a variable or the name of a class
                String className;
                if(Character.isUpperCase(identifier.charAt(0))){ //If the first letter is uppercase, it must be a classname.
                    className = identifier;
                }else{
                    className = symbolTable.typeOf(identifier);
                }
                tokenizer.advanceToken(); //symbol: "."
                var methodName = compileIdentifier();
                tokenizer.advanceToken(); //symbol "("
                var nrOfArguments = compileExpressionList();
                tokenizer.advanceToken(); //symbol ")"
                writer.writeCall(className + "." + methodName, nrOfArguments);





            } else {
                var methodName = compileIdentifier();
                tokenizer.advanceToken(); //symbol "("
                var nrOfArguments = compileExpressionList();
                tokenizer.advanceToken(); //symbol ")"
                writer.writeCall(currentClassName + "." + methodName, nrOfArguments);
            }
        } else if (tokenizer.tokenType().equals("identifier")) { //variable
            var name = compileIdentifier();
            var kind = symbolTable.kindOf(name);
            if (kind == Kind.STATIC)
                writer.writePush(Segment.STATIC, symbolTable.indexOf(name));
            if (kind == Kind.VAR)
                writer.writePush(Segment.LOCAL, symbolTable.indexOf(name));
            if (kind == Kind.ARG){
                if (isMethod) {
                    writer.writePush(Segment.ARGUMENT, symbolTable.indexOf(name) + 1);
                } else {
                    writer.writePush(Segment.ARGUMENT, symbolTable.indexOf(name));
                }
            }
            if (kind == Kind.FIELD)
                writer.writePush(Segment.THIS, symbolTable.indexOf(name));
        } else if (tokenizer.symbol().equals("-") || tokenizer.symbol().equals("~")) {
            var operation = compileOperation();
            compileTerm();
            if (operation.equals("-"))
                writer.writeArithmetic(Command.NEG);
            if (operation.equals("~"))
                writer.writeArithmetic(Command.NOT);
        }
    }

    public int compileExpressionList() {
        int count = 0;
        if (isExpression()) {
            compileExpression();
            count++;
            while (tokenizer.symbol().equals(",")) {
                tokenizer.advanceToken();
                compileExpression();
                count++;
            }
        }
        return count;
    }

    //region My Functions
    public String compileIdentifierOrKeyword() {
        if (tokenizer.tokenType().equals("identifier")) {
            return compileIdentifier();
        } else {
            return compileKeyword();
        }
    }


    public String compileOperation() {
        var symbol = tokenizer.symbol();
        tokenizer.advanceToken();
        return symbol;
    }

    public void compileOperation(String symbol) {
        if (symbol.equals("+")) {
            writer.writeArithmetic(Command.ADD);
        }
        if (symbol.equals("-")) {
            writer.writeArithmetic(Command.SUB);
        }
        if (symbol.equals("&amp;")) {
            writer.writeArithmetic(Command.AND);
        }
        if (symbol.equals("|")) {
            writer.writeArithmetic(Command.OR);
        }
        if (symbol.equals("&gt;")) {
            writer.writeArithmetic(Command.GT);
        }
        if (symbol.equals("&lt;")) {
            writer.writeArithmetic(Command.LT);
        }
        if (symbol.equals("=")) {
            writer.writeArithmetic(Command.EQ);
        }
        if (symbol.equals("*")) {
            writer.writeCall("Math.multiply", 2);
        }
        if (symbol.equals("/")) {
            writer.writeCall("Math.divide", 2);
        }
    }

    public String compileIdentifier() {
        String identifier = tokenizer.identifier();
        tokenizer.advanceToken();
        return identifier;
    }

    public String compileKeyword() {
        String keyword = tokenizer.keyWord();
        tokenizer.advanceToken();
        return keyword;
    }

    public boolean isSubroutine() {
        return tokenizer.keyWord().equals("function") || tokenizer.keyWord().equals("method") || tokenizer.keyWord().equals("constructor");
    }

    public boolean isClassVarDec() {
        return tokenizer.keyWord().equals("static") || tokenizer.keyWord().equals("field");
    }

    public boolean isStatement() {
        return tokenizer.keyWord().equals("let") || tokenizer.keyWord().equals("if") || tokenizer.keyWord().equals("while") || tokenizer.keyWord().equals("do") || tokenizer.keyWord().equals("return");
    }

    public boolean isExpression() {
        return tokenizer.tokenType().equals("integerConstant") || tokenizer.tokenType().equals("stringConstant") || tokenizer.tokenType().equals("keyword") || tokenizer.tokenType().equals("identifier") || tokenizer.symbol().equals("(") || tokenizer.symbol().equals("-") || tokenizer.symbol().equals("~");
    }

    public boolean isOperation() {
        String current = tokenizer.symbol();
        return current.equals("+") || current.equals("-") || current.equals("*") || current.equals("/") || current.equals("&amp;") || current.equals("|") || current.equals("&lt;") || current.equals("&gt;") || current.equals("=");
    }

    private Segment getSegment(Kind kind) {
        if (kind.toString().equals("STATIC")) {
            return Segment.STATIC;
        }
        if (kind.toString().equals("ARG")) {
            return Segment.ARGUMENT;
        }
        if (kind.toString().equals("VAR")) {
            return Segment.LOCAL;
        }
        if (kind.toString().equals("FIELD")) {
            return Segment.THIS;
        }
        return null;
    }
}
