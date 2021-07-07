package main;

import main.enums.Command;
import main.enums.Kind;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

public class CompilationEngine {
    JackTokenizer tokenizer;
    SymbolTable symbolTable;
    VMWriter writer;

    File inputFile;
    File outputXMLFile;
    String output;


    public CompilationEngine(File inputFile, File outputXMLFile, File outputVMFile) throws IOException {
        this.inputFile = inputFile;
        this.outputXMLFile = outputXMLFile;
        this.output = "";
        tokenizer = new JackTokenizer(inputFile, true);
        symbolTable = new SymbolTable();
        writer = new VMWriter(outputVMFile);
    }

    public void compileClass() throws IOException {
        output += "<class>";

        var kind = compileKeyword();
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

        output += "(" + kind + ":def:" + "NO)";

        output += "</class>";

        writer.close();
    }

    public void compileClassVarDec() {
        output += "<classVarDec>";

        var kind = compileKeyword();
        var type = compileIdentifierOrKeyword();
        var identifier = compileIdentifier();

        symbolTable.define(identifier, type, Kind.valueOf(kind.toUpperCase(Locale.ROOT)));
        output += "(" + kind + ":def:" + "YES:" + symbolTable.indexOf(identifier) + ")";

        while (tokenizer.symbol().equals(",")) {
            compileSymbol();
            compileIdentifier();
        }
        compileSymbol();

        output += "</classVarDec>";
    }

    public void compileSubroutine() {
        output += "<subroutineDec>";
        String keyword = compileKeyword();





        if (tokenizer.tokenType().equals("identifier")) {
            compileIdentifier();
            output += "(subroutine:use:NO)";
        } else {
            compileKeyword();
        }

        String identifier = compileIdentifier();
        output += "(subroutine:def:NO)";
        compileSymbol();
        int nrOfParameters = compileParameterList();
        compileSymbol();
        output += "<subroutineBody>";
        compileSymbol();
        while (tokenizer.keyWord().equals("var")) {
            compileVarDec();
        }
        compileStatements();
        compileSymbol();
        output += "</subroutineBody>";
        output += "</subroutineDec>";

        if(keyword == "function"){
            writer.writeFunction(identifier, nrOfParameters);
        }
    }

    public int compileParameterList() {
        output += "<parameterList>";
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
        output += "</parameterList>";
        return counter;
    }

    public void compileVarDec() {
        output += "<varDec>";

        var kind = compileKeyword();
        String varIdentifier = null;
        String type;

        if (tokenizer.tokenType().equals("identifier")) {
            type = compileIdentifier();
            varIdentifier = type;
        } else {
            type = compileKeyword();
        }

        var identifier = compileIdentifier();

        symbolTable.define(identifier, type, Kind.valueOf(kind.toUpperCase(Locale.ROOT)));
        if(varIdentifier != null){
            output += "(class:used:NO)";
        }
        output += "(" + kind + ":def:YES:" + symbolTable.indexOf(identifier) +")";

        while (tokenizer.symbol().equals(",")) {
            compileSymbol();
            var identifier2 = compileIdentifier();
            symbolTable.define(identifier2, type, Kind.valueOf(kind.toUpperCase(Locale.ROOT)));
            output += "(" + kind + ":def:YES:" + symbolTable.indexOf(identifier2) +")";
        }
        compileSymbol();
        output += "</varDec>";
    }

    public void compileStatements() {
        output += "<statements>";
        while (isStatement()) {
            switch (tokenizer.keyWord()) {
                case "let" -> compileLet();
                case "if" -> compileIf();
                case "while" -> compileWhile();
                case "do" -> compileDo();
                case "return" -> compileReturn();
            }
        }
        output += "</statements>";
    }

    public void compileDo() {
        output += "<doStatement>";
        String identifier;
        compileKeyword();
        if (tokenizer.nextSymbol().equals("(")) {
            identifier = compileIdentifier();
            output += "(subroutine:use:NO)";
            compileSymbol();
            compileExpressionList();
            compileSymbol();

            //TODO: Figure out if there shouldn't be a loop here
//            writer.writeCall(identifier);
        } else { // next symbol is a "."
            identifier = compileIdentifier();
            output += "(class:use:NO)";
            compileSymbol();
            compileIdentifier();
            output += "(subroutine:use:NO)";
            compileSymbol();
            compileExpressionList();
            compileSymbol();
        }
        compileSymbol();

        output += "</doStatement>";
    }

    public void compileLet() {
        output += "<letStatement>";

        compileKeyword();
        var identifier = compileIdentifier();

        output += "(" + symbolTable.kindOf(identifier) + ":use:YES:" + symbolTable.indexOf(identifier) +")";

        if (tokenizer.symbol().equals("[")) {
            compileSymbol();
            compileExpression();
            compileSymbol();
        }
        compileSymbol();
        compileExpression();
        compileSymbol();

        output += "</letStatement>";
    }

    public void compileWhile() {
        output += "<whileStatement>";
        compileKeyword();
        compileSymbol();
        compileExpression();
        compileSymbol();
        compileSymbol();
        compileStatements();
        compileSymbol();

        output += "</whileStatement>";
    }

    public void compileReturn() {
        output += "<returnStatement>";
        compileKeyword();
        if (!tokenizer.symbol().equals(";")) {
            compileExpression();
        }
        compileSymbol();
        output += "</returnStatement>";
    }

    public void compileIf() {
        output += "<ifStatement>";
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
        output += "</ifStatement>";
    }

    public void compileExpression() {
        output += "<expression>";
        compileTerm();
        while (isOperation()) {
            compileSymbol();
            compileTerm();
        }
        output += "</expression>";
    }

    public void compileTerm() {
        output += "<term>";
        if (tokenizer.tokenType().equals("integerConstant")) {
            output += "<" + tokenizer.tokenType() + ">" + tokenizer.intVal() + "</" + tokenizer.tokenType() + ">";
            tokenizer.advanceToken();
        } else if (tokenizer.tokenType().equals("stringConstant")) {
            output += "<" + tokenizer.tokenType() + ">" + tokenizer.stringVal() + "</" + tokenizer.tokenType() + ">";
            tokenizer.advanceToken();
        } else if (tokenizer.tokenType().equals("keyword")) {
            output += "<" + tokenizer.tokenType() + ">" + tokenizer.keyWord() + "</" + tokenizer.tokenType() + ">";
            tokenizer.advanceToken();
        } else if (tokenizer.tokenType().equals("identifier") && tokenizer.nextSymbol().equals("[")) {
            compileIdentifier();
            output += "(class:use:NO)";
            compileSymbol();
            compileExpression();
            compileSymbol();


        } else if (tokenizer.tokenType().equals("identifier") && (tokenizer.nextSymbol().equals("(") || tokenizer.nextSymbol().equals("."))) {
            if (tokenizer.nextSymbol().equals(".")) {
                compileIdentifier();
                output += "(class:use:NO)";
                compileSymbol();
                compileIdentifier();
                output += "(subroutine:use:NO)";
                compileSymbol();
                compileExpressionList();
                compileSymbol();
            } else {
                compileIdentifier();
                output += "(subroutine:use:NO)";
                compileSymbol();
                compileExpressionList();
                compileSymbol();
            }
        } else if (tokenizer.tokenType().equals("identifier")) {
            var identifier = compileIdentifier();
            output += "(" + symbolTable.kindOf(identifier) + ":use:YES:"  + symbolTable.indexOf(identifier) + ")";
        } else if (tokenizer.symbol().equals("(")) {
            compileSymbol();
            compileExpression();
            compileSymbol();
        } else if (tokenizer.symbol().equals("-") || tokenizer.symbol().equals("~")) {
            compileSymbol();
            compileTerm();
        }
        output += "</term>";
    }

    public void compileExpressionList() {
        output += "<expressionList>";
        if (isExpression()) {
            compileExpression();

            while (tokenizer.symbol().equals(",")) {
                compileSymbol();
                compileExpression();
            }
        }
        output += "</expressionList>";
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
        output += "<symbol>" + symbol + "</symbol>";

        if(symbol == "+"){
            writer.writeArithmetic(Command.ADD);
        }
        if(symbol == "-"){
            writer.writeArithmetic(Command.SUB);
        }
        if(symbol == "&"){
            writer.writeArithmetic(Command.ADD);
        }
        if(symbol == "|"){
            writer.writeArithmetic(Command.OR);
        }

        tokenizer.advanceToken();
    }

    public String compileIdentifier() {
        String identifier = tokenizer.identifier();
        output += "<identifier>" + identifier + "</identifier>";
        tokenizer.advanceToken();
        return identifier;
    }

    public String compileKeyword() {
        String keyword = tokenizer.keyWord();
        output += "<keyword>" + keyword + "</keyword>";
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
    //endregion

    public void writeToOutput() {
        FileWriter xmlWriter;
        try {
            xmlWriter = new FileWriter(outputXMLFile);
            xmlWriter.write(output);
            xmlWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
