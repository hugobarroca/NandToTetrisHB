package main;

import main.enums.Command;
import main.enums.Kind;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class CompilationEngine {
    JackTokenizer tokenizer;
    SymbolTable symbolTable;
    VMWriter writer;

    File inputFile;


    public CompilationEngine(File inputFile, File outputVMFile) throws IOException {
        this.inputFile = inputFile;
        tokenizer = new JackTokenizer(inputFile, true);
        symbolTable = new SymbolTable();
        writer = new VMWriter(outputVMFile);
    }

    public void compileClass() throws IOException {

        compileKeyword();
        compileIdentifier();
        compileSymbol();

        while (isSubroutine() || isClassVarDec()) {
            if (isSubroutine()) {
                compileSubroutine();
            } else {
                compileClassVarDec();
            }
        }
        compileSymbol();
        writer.close();
    }

    public void compileClassVarDec() {
        var kind = compileKeyword();
        var type = compileIdentifierOrKeyword();
        var identifier = compileIdentifier();

        symbolTable.define(identifier, type, Kind.valueOf(kind.toUpperCase(Locale.ROOT)));

        while (tokenizer.symbol().equals(",")) {
            compileSymbol();
            compileIdentifier();
        }
        compileSymbol();
    }

    public void compileSubroutine() {
        String keyword = compileKeyword();

        if (tokenizer.tokenType().equals("identifier")) {
            compileIdentifier();
        } else {
            compileKeyword();
        }

        String identifier = compileIdentifier();
        compileSymbol();
        int nrOfParameters = compileParameterList();
        compileSymbol();
        compileSymbol();
        while (tokenizer.keyWord().equals("var")) {
            compileVarDec();
        }
        compileStatements();
        compileSymbol();
        if(keyword.equals("function")){
            writer.writeFunction(identifier, nrOfParameters);
        }
    }

    public int compileParameterList() {
        int counter = 0;
        if (tokenizer.tokenType().equals("identifier") || tokenizer.keyWord().equals("int") || tokenizer.keyWord().equals("char") || tokenizer.keyWord().equals("boolean")) {
            compileIdentifierOrKeyword();
            compileIdentifier();
            while (tokenizer.symbol().equals(",")) {
                compileSymbol();
                compileIdentifierOrKeyword();
                compileIdentifier();
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
            compileSymbol();
            var identifier2 = compileIdentifier();
            symbolTable.define(identifier2, type, Kind.valueOf(kind.toUpperCase(Locale.ROOT)));
        }
        compileSymbol();

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
        compileKeyword();
        if (tokenizer.nextSymbol().equals("(")) {
            compileIdentifier();
            compileSymbol();
            compileExpressionList();
            compileSymbol();
        } else {
            compileIdentifier();
            compileSymbol();
            compileIdentifier();
            compileSymbol();
            compileExpressionList();
            compileSymbol();
        }
        compileSymbol();
    }

    public void compileLet() {
        compileKeyword();
        compileIdentifier();
        if (tokenizer.symbol().equals("[")) {
            compileSymbol();
            compileExpression();
            compileSymbol();
        }
        compileSymbol();
        compileExpression();
        compileSymbol();
    }

    public void compileWhile() {
        compileKeyword();
        compileSymbol();
        compileExpression();
        compileSymbol();
        compileSymbol();
        compileStatements();
        compileSymbol();
    }

    public void compileReturn() {
        compileKeyword();
        if (!tokenizer.symbol().equals(";")) {
            compileExpression();
        }
        compileSymbol();
    }

    public void compileIf() {
        compileKeyword();
        compileSymbol();
        compileExpression();
        compileSymbol();
        compileSymbol();
        compileStatements();
        compileSymbol();
        if (tokenizer.keyWord().equals("else")) {
            compileKeyword();
            compileSymbol();
            compileStatements();
            compileSymbol();
        }
    }

    public void compileExpression() {
        compileTerm();
        while (isOperation()) {
            compileSymbol();
            compileTerm();
        }
    }

    public void compileTerm() {
        if (tokenizer.tokenType().equals("integerConstant")) {
            tokenizer.advanceToken();
        } else if (tokenizer.tokenType().equals("stringConstant")) {
            tokenizer.advanceToken();
        } else if (tokenizer.tokenType().equals("keyword")) {
            tokenizer.advanceToken();
        } else if (tokenizer.tokenType().equals("identifier") && tokenizer.nextSymbol().equals("[")) {
            compileIdentifier();
            compileSymbol();
            compileExpression();
            compileSymbol();


        } else if (tokenizer.tokenType().equals("identifier") && (tokenizer.nextSymbol().equals("(") || tokenizer.nextSymbol().equals("."))) {
            if (tokenizer.nextSymbol().equals(".")) {
                compileIdentifier();
                compileSymbol();
                compileIdentifier();
                compileSymbol();
                compileExpressionList();
                compileSymbol();
            } else {
                compileIdentifier();
                compileSymbol();
                compileExpressionList();
                compileSymbol();
            }
        } else if (tokenizer.tokenType().equals("identifier")) {
            compileIdentifier();
        } else if (tokenizer.symbol().equals("(")) {
            compileSymbol();
            compileExpression();
            compileSymbol();
        } else if (tokenizer.symbol().equals("-") || tokenizer.symbol().equals("~")) {
            compileSymbol();
            compileTerm();
        }
    }

    public void compileExpressionList() {
        if (isExpression()) {
            compileExpression();

            while (tokenizer.symbol().equals(",")) {
                compileSymbol();
                compileExpression();
            }
        }
    }

    //region My Functions
    public String compileIdentifierOrKeyword() {
        if (tokenizer.tokenType().equals("identifier")) {
            return compileIdentifier();
        } else {
            return compileKeyword();
        }
    }


    public void compileSymbol() {
        var symbol = tokenizer.symbol();

        if(symbol.equals("+")){
            writer.writeArithmetic(Command.ADD);
        }
        if(symbol.equals("-")){
            writer.writeArithmetic(Command.SUB);
        }
        if(symbol.equals("&")){
            writer.writeArithmetic(Command.ADD);
        }
        if(symbol.equals("|")){
            writer.writeArithmetic(Command.OR);
        }

        tokenizer.advanceToken();
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
