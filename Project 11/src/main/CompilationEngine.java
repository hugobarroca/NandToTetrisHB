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
    File inputFile;

    String currentClassName;
    String subroutineReturnType;

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
        String keyword = compileKeyword();  //keyword: method || function
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

        if(keyword.equals("function")){
            writer.writeFunction(identifier, nrOfLocalVariables);
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
        if (tokenizer.nextSymbol().equals("(")) {   //IF: It's a function
            var subroutineName = compileIdentifier(); //identifier: function name
            tokenizer.advanceToken(); //symbol: "("
            int nrOfArguments = compileExpressionList();
            tokenizer.advanceToken(); //symbol ")"
            writer.writeCall(this.currentClassName + "." + subroutineName, nrOfArguments);
        } else {                                    //ELSE: It's a method
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
        if (tokenizer.symbol().equals("[")) {
            compileOperation();     //symbol: "["
            compileExpression();
            compileOperation();     //symbol: "]"
        }
        tokenizer.advanceToken();     //symbol: "="
        compileExpression();
        tokenizer.advanceToken();     //symbol: ";"

        var kind = symbolTable.kindOf(variableName);
        var index = symbolTable.indexOf(variableName);
        if(kind == Kind.STATIC)
            writer.writePop(Segment.STATIC, index);
        if(kind == Kind.VAR)
            writer.writePop(Segment.LOCAL, index);
        if(kind == Kind.ARG)
            writer.writePop(Segment.ARGUMENT, index);
    }

    public void compileWhile() {
        writer.writeLabel("WHILE_EXP" + whileLabelCounter);

        compileKeyword();                   //keyword: "while"
        tokenizer.advanceToken();           //symbol: "("
        compileExpression();
        writer.writeArithmetic(Command.NOT);
        writer.writeIf("WHILE_END" + whileLabelCounter);
        tokenizer.advanceToken();           //symbol: ")"
        tokenizer.advanceToken();           //symbol: "{"
        compileStatements();
        tokenizer.advanceToken();           //symbol: "}"
        writer.writeGoto("WHILE_EXP" + whileLabelCounter);
        writer.writeLabel("WHILE_END" + whileLabelCounter);
        whileLabelCounter++;
    }

    public void compileReturn() {
        compileKeyword();
        if (!tokenizer.symbol().equals(";")) {
            compileExpression();
        }
        if(subroutineReturnType.equals("void")){
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
            tokenizer.advanceToken();
        } else if (tokenizer.tokenType().equals("keyword")) {
            if(tokenizer.keyWord().equals("true")){
                writer.writePush(Segment.CONSTANT, 1);
                writer.writeArithmetic(Command.NEG);
            } else if (tokenizer.keyWord().equals("false")){
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
                var className = compileIdentifier();
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
            var kind= symbolTable.kindOf(name);
            if(kind == Kind.STATIC)
                writer.writePush(Segment.STATIC, symbolTable.indexOf(name));
            if(kind == Kind.VAR)
                writer.writePush(Segment.LOCAL, symbolTable.indexOf(name));
            if(kind == Kind.ARG)
                writer.writePush(Segment.ARGUMENT, symbolTable.indexOf(name));
        } else if (tokenizer.symbol().equals("-") || tokenizer.symbol().equals("~")) {
            var operation = compileOperation();
            compileTerm();
            if(operation.equals("-"))
                writer.writeArithmetic(Command.NEG);
            if(operation.equals("~"))
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
        if(symbol.equals("+")){
            writer.writeArithmetic(Command.ADD);
        }
        if(symbol.equals("-")){
            writer.writeArithmetic(Command.SUB);
        }
        if(symbol.equals("&amp;")){
            writer.writeArithmetic(Command.AND);
        }
        if(symbol.equals("|")){
            writer.writeArithmetic(Command.OR);
        }
        if(symbol.equals("&gt;")){
            writer.writeArithmetic(Command.GT);
        }
        if(symbol.equals("&lt;")){
            writer.writeArithmetic(Command.LT);
        }
        if(symbol.equals("=")){
            writer.writeArithmetic(Command.EQ);
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
