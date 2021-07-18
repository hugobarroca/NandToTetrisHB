package main;

import main.enums.Command;
import main.enums.Kind;
import main.enums.Segment;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class CompilationEngine {
    JackTokenizer tokenizer;
    SymbolTable symbolTable;
    VMWriter writer;

    String currentClassName;

    File inputFile;


    public CompilationEngine(File inputFile, File outputVMFile) throws IOException {
        this.inputFile = inputFile;
        tokenizer = new JackTokenizer(inputFile, true);
        symbolTable = new SymbolTable();
        writer = new VMWriter(outputVMFile);

        currentClassName = "";
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
        compileOperation(); // symbol: ")"
    }

    public void compileSubroutine() {
        String keyword = compileKeyword();
        String type;
        if (tokenizer.tokenType().equals("identifier")) {
            type = compileIdentifier();
        } else {
            type = compileKeyword();
        }

        String identifier = currentClassName + "." + compileIdentifier();
        compileOperation();
        int nrOfParameters = compileParameterList();
        compileOperation();
        compileOperation();
        if(keyword.equals("function")){
            writer.writeFunction(identifier, nrOfParameters);
        }
        while (tokenizer.keyWord().equals("var")) {
            compileVarDec();
        }
        compileStatements();
        if(type == "void"){
            writer.writePush(Segment.CONSTANT, 0);
        }
        writer.writeReturn();
        tokenizer.advanceToken(); //symbol: "}"

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

    public void compileVarDec() {
        var kind = compileKeyword();
        String varIdentifier = null;
        String type;

        if (tokenizer.tokenType().equals("identifier")) {
            type = compileIdentifier();
        } else {
            type = compileKeyword();
        }

        var identifier = compileIdentifier();

        symbolTable.define(identifier, type, Kind.valueOf(kind.toUpperCase(Locale.ROOT)));


        while (tokenizer.symbol().equals(",")) {
            compileOperation();
            var identifier2 = compileIdentifier();
            symbolTable.define(identifier2, type, Kind.valueOf(kind.toUpperCase(Locale.ROOT)));
        }
        compileOperation();

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
        if (tokenizer.nextSymbol().equals("(")) {
            compileIdentifier(); //identifier: function name
            tokenizer.advanceToken(); //symbol: "("
            compileExpressionList();
            tokenizer.advanceToken(); //symbol ")"
        } else {
            var className = compileIdentifier(); //identifier: class name
            tokenizer.advanceToken(); //symbol: "."
            String functionName = compileIdentifier(); //identifier: function name
            tokenizer.advanceToken(); //symbol: "("
            int nrOfArguments = compileExpressionList();
            tokenizer.advanceToken(); //symbol ")"
            writer.writeCall(className + "." + functionName, nrOfArguments);
        }
        writer.writePop(Segment.TEMP, 0);
        tokenizer.advanceToken(); //symbol ";"


    }

    public void compileLet() {
        compileKeyword();
        compileIdentifier();
        if (tokenizer.symbol().equals("[")) {
            compileOperation();
            compileExpression();
            compileOperation();
        }
        compileOperation();
        compileExpression();
        compileOperation();
    }

    public void compileWhile() {
        compileKeyword();
        compileOperation();
        compileExpression();
        compileOperation();
        compileOperation();
        compileStatements();
        compileOperation();
    }

    public void compileReturn() {
        compileKeyword();
        if (!tokenizer.symbol().equals(";")) {
            compileExpression();
        }
        compileOperation();
    }

    public void compileIf() {
        compileKeyword();
        compileOperation();
        compileExpression();
        compileOperation();
        compileOperation();
        compileStatements();
        compileOperation();
        if (tokenizer.keyWord().equals("else")) {
            compileKeyword();
            compileOperation();
            compileStatements();
            compileOperation();
        }
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
            tokenizer.advanceToken();
        } else if (tokenizer.tokenType().equals("keyword")) {
            if(tokenizer.keyWord() == "true"){
                writer.writePush(Segment.CONSTANT, 1);
                writer.writeArithmetic(Command.NEG);
            } else if (tokenizer.keyWord() == "false"){
                writer.writePush(Segment.CONSTANT, 0);
            }
            tokenizer.advanceToken();
        } else if (tokenizer.tokenType().equals("identifier") && tokenizer.nextSymbol().equals("[")) {
            compileIdentifier();
            tokenizer.advanceToken();
            compileExpression();
            tokenizer.advanceToken();
        } else if (tokenizer.tokenType().equals("identifier") && (tokenizer.nextSymbol().equals("(") || tokenizer.nextSymbol().equals("."))) {
            if (tokenizer.nextSymbol().equals(".")) {
                compileIdentifier();
                tokenizer.advanceToken();
                compileIdentifier();
                tokenizer.advanceToken();
                compileExpressionList();
                tokenizer.advanceToken();
            } else {
                compileIdentifier();
                tokenizer.advanceToken();
                compileExpressionList();
                tokenizer.advanceToken();
            }
        } else if (tokenizer.tokenType().equals("identifier")) {
            compileIdentifier();
        } else if (tokenizer.symbol().equals("-") || tokenizer.symbol().equals("~")) {
            compileOperation();
            compileTerm();
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


    public void compileOperation() {
        var symbol = tokenizer.symbol();

        if(symbol.equals("+")){
            writer.writeArithmetic(Command.ADD);
        }
        if(symbol.equals("-")){
            writer.writeArithmetic(Command.SUB);
        }
        if(symbol.equals("&")){
            writer.writeArithmetic(Command.AND);
        }
        if(symbol.equals("|")){
            writer.writeArithmetic(Command.OR);
        }

        tokenizer.advanceToken();
    }

    public void compileOperation(String symbol) {
        if(symbol.equals("+")){
            writer.writeArithmetic(Command.ADD);
        }
        if(symbol.equals("-")){
            writer.writeArithmetic(Command.SUB);
        }
        if(symbol.equals("&")){
            writer.writeArithmetic(Command.AND);
        }
        if(symbol.equals("|")){
            writer.writeArithmetic(Command.OR);
        }
        if(symbol.equals("*")){
            writer.writeCall("Math.multiply", 2);
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
}
