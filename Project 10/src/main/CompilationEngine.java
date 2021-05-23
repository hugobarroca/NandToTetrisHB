package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class CompilationEngine {
    File inputFile;
    File outputFile;
    JackTokenizer tokenizer;
    String output;


    public CompilationEngine(File inputFile, File outputFile) throws FileNotFoundException {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.output = "";
        tokenizer = new JackTokenizer(inputFile, true);
    }

    public void compileClass() {
        output += "<class>";

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

        output += "</class>";
    }

    public void compileClassVarDec() {
        output += "<classVarDec>";

        compileKeyword();
        compileType();
        compileIdentifier();
        while (tokenizer.symbol() == ",") {
            compileSymbol();
            compileIdentifier();
        }
        compileSymbol();

        output += "</classVarDec>";
    }

    public void compileSubroutine() {
        output += "<subroutineDec>";
        compileKeyword();
        compileType();
        compileIdentifier();
        compileSymbol();
        compileParameterList();
        compileSymbol();
        output += "<subroutineBody>";
        compileSymbol();
        while (tokenizer.keyWord() == "var") {
            compileVarDec();
        }
        compileStatements();
        compileSymbol();
        output += "</subroutineBody>";
        output += "</subroutineDec>";
    }

    public void compileParameterList() {
        output += "<parameterList>";
        while (tokenizer.tokenType() == "identifier" || tokenizer.keyWord() == "int" || tokenizer.keyWord() == "char" || tokenizer.keyWord() == "boolean") {
            compileType();
            compileIdentifier();
            while (tokenizer.symbol() == ",") {
                compileSymbol();
                compileType();
                compileIdentifier();
            }
            compileSymbol();
        }
        output += "</parameterList>";
    }

    public void compileVarDec() {
        output += "<varDec>";
        compileKeyword();
        compileType();
        compileIdentifier();
        while (tokenizer.symbol() == ",") {
            compileSymbol();
            compileIdentifier();
        }
        compileSymbol();
        output += "</varDec>";
    }

    public void compileStatements() {
        output += "<statements>";
        while (isStatement()) {
            if (tokenizer.keyWord() == "let") {
                compileLet();
            } else if (tokenizer.keyWord() == "if") {
                compileIf();
            } else if (tokenizer.keyWord() == "while") {
                compileWhile();
            } else if (tokenizer.keyWord() == "do") {
                compileDo();
            } else if (tokenizer.keyWord() == "return") {
                compileReturn();
            }
        }
        output += "</statements>";
    }

    public void compileDo() {
        output += "<doStatement>";
        compileKeyword();
        if (tokenizer.nextSymbol() == "(") {
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

        output += "</doStatement>";
    }

    public void compileLet() {
        output += "<letStatement>";

        compileKeyword();
        compileIdentifier();

        if (tokenizer.symbol() == "[") {
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
        if (tokenizer.symbol() != ";") {
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
        if (tokenizer.keyWord() == "else") {
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
        if (tokenizer.tokenType() == "int_const") {
            output += "<" + tokenizer.tokenType() + ">" + tokenizer.intVal() + "</" + tokenizer.tokenType() + ">";
        } else if (tokenizer.tokenType() == "string_const") {
            output += "<" + tokenizer.tokenType() + ">" + tokenizer.stringVal() + "</" + tokenizer.tokenType() + ">";
        } else if (tokenizer.tokenType() == "keyword") {
            output += "<" + tokenizer.tokenType() + ">" + tokenizer.keyWord() + "</" + tokenizer.tokenType() + ">";
        } else if (tokenizer.tokenType() == "identifier" && tokenizer.nextSymbol() == "[") {
            compileIdentifier();
            compileSymbol();
            compileExpression();
            compileSymbol();
        } else if (tokenizer.tokenType() == "identifier" && (tokenizer.nextSymbol() == "(" || tokenizer.nextSymbol() == ".")) {
            if (tokenizer.nextSymbol() == ".") {
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
        } else if (tokenizer.tokenType() == "identifier") {
            compileIdentifier();
        } else if (tokenizer.symbol() == "(") {
            compileSymbol();
            compileExpression();
            compileSymbol();
        } else if (tokenizer.symbol() == "-" || tokenizer.symbol() == "~") {
            compileSymbol();
            compileTerm();
        }
        output += "</term>";
    }

    public void compileExpressionList() {
        output += "<expressionList>";
        if (isExpression()) {
            compileExpression();
            while (tokenizer.symbol() == ",") {
                compileSymbol();
                isExpression();
            }
        }
        output += "</expressionList>";
    }

    //My Own
    public void compileType() {
        if (tokenizer.tokenType() == "identifier") {
            output += "<identifier>" + tokenizer.identifier() + "</identifier>";
        } else {
            output += "<keyword>" + tokenizer.keyWord() + "</keyword>";
        }
        tokenizer.advanceToken();
    }

    public void compileSymbol() {
        output += "<symbol>" + tokenizer.symbol() + "</symbol>";
        tokenizer.advanceToken();
    }

    public void compileIdentifier() {
        output += "<identifier>" + tokenizer.identifier() + "</identifier>";
        tokenizer.advanceToken();
    }

    public void compileKeyword() {
        output += "<keyword>" + tokenizer.keyWord() + "</keyword>";
        tokenizer.advanceToken();
    }

    public boolean isSubroutine() {
        return tokenizer.keyWord() == "function" || tokenizer.keyWord() == "method" || tokenizer.keyWord() == "constructor";
    }

    public boolean isClassVarDec() {
        return tokenizer.keyWord() == "static" || tokenizer.keyWord() == "field";
    }

    public boolean isStatement() {
        return tokenizer.keyWord() == "let" || tokenizer.keyWord() == "if" || tokenizer.keyWord() == "while" || tokenizer.keyWord() == "do" || tokenizer.keyWord() == "return";
    }

    public boolean isExpression() {
        return tokenizer.tokenType() == "int_const" || tokenizer.tokenType() == "string_const" || tokenizer.tokenType() == "keyword" || tokenizer.tokenType() == "identifier" || tokenizer.symbol() == "(" || tokenizer.symbol() == "-" || tokenizer.symbol() == "~";
    }

    public boolean isOperation() {
        String current = tokenizer.symbol();
        return current == "+" || current == "-" || current == "*" || current == "/" || current == "&" || current == "|" || current == "<" || current == ">" || current == "=";
    }

    public void writeToOutput() {
        FileWriter xmlWriter = null;
        try {
            xmlWriter = new FileWriter(outputFile);
            xmlWriter.write(output);
            xmlWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
