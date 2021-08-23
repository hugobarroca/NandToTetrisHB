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
            isMethod = true;
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
        isMethod = false;
        ifLabelCounter = 0;
        whileLabelCounter = 0;
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
            if(segment == Segment.ARGUMENT && isMethod){
                writer.writePush(segment, symbolTable.indexOf(identifier) + 1);
            } else {
                writer.writePush(segment, symbolTable.indexOf(identifier));
            }
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
        var variableName = compileIdentifier();
        var variableKind = symbolTable.kindOf(variableName);
        var variableIndex = symbolTable.indexOf(variableName);

        boolean isArray = false;

        if (tokenizer.symbol().equals("[")) {
            isArray = true;
        }

        if (isArray) {
            compileArray(variableKind, variableIndex);
        } else {
            tokenizer.advanceToken();     //symbol: "="
            compileExpression();
            tokenizer.advanceToken();     //symbol: ";"

            if (variableKind == Kind.STATIC)
                writer.writePop(Segment.STATIC, variableIndex);
            if (variableKind == Kind.VAR)
                writer.writePop(Segment.LOCAL, variableIndex);
            if (variableKind == Kind.ARG){
                if(isMethod){
                    writer.writePop(Segment.ARGUMENT, variableIndex + 1);
                } else {
                    writer.writePop(Segment.ARGUMENT, variableIndex);
                }
            }

            if (variableKind == Kind.FIELD)
                writer.writePop(Segment.THIS, variableIndex);
        }


    }

    private void compileArray(Kind variableKind, int variableIndex){
        pushArrayIndexAddress(variableKind, variableIndex);
        pushExpressionToSetInArray();
        setThatAddress();

        writer.writePop(Segment.THAT, 0);
    }

    private void pushArrayIndexAddress(Kind variableKind, int variableIndex){
        compileExpressionInBrackets();
        compileArrayVariable(variableKind, variableIndex);

        writer.writeArithmetic(Command.ADD);
    }

    private void compileExpressionInBrackets(){
        compileOperation();     //symbol: "["
        compileExpression();
        compileOperation();     //symbol: "]"
    }

    private void compileArrayVariable(Kind variableKind, int variableIndex){
        if (variableKind == Kind.STATIC)
            writer.writePush(Segment.STATIC, variableIndex);
        if (variableKind == Kind.VAR)
            writer.writePush(Segment.LOCAL, variableIndex);
        if (variableKind == Kind.ARG){
            if (isMethod) {
                writer.writePush(Segment.ARGUMENT, variableIndex + 1);
            } else {
                writer.writePush(Segment.ARGUMENT, variableIndex);
            }
        }

        if (variableKind == Kind.FIELD)
            writer.writePush(Segment.THIS, variableIndex);
    }

    private void pushExpressionToSetInArray(){
        tokenizer.advanceToken();     //symbol: "="
        compileExpression();
        tokenizer.advanceToken();     //symbol: ";"
    }

    private void setThatAddress(){
        writer.writePop(Segment.TEMP, 0);
        writer.writePop(Segment.POINTER, 1);
        writer.writePush(Segment.TEMP, 0);
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
            compileExpressionInParenthesis();
        } else if (tokenizer.tokenType().equals("integerConstant")) {
            compileIntegerConstant();
        } else if (tokenizer.tokenType().equals("stringConstant")) {
            compileStringConstant();
        } else if (tokenizer.tokenType().equals("keyword")) {
            compileKeywordInTerm();
        } else if (tokenizer.tokenType().equals("identifier") && tokenizer.nextSymbol().equals("[")) {
            compileArray();
        } else if (tokenizer.tokenType().equals("identifier") && (tokenizer.nextSymbol().equals("(") || tokenizer.nextSymbol().equals("."))) {
            compileMethodOrFunctionCall();
        } else if (tokenizer.tokenType().equals("identifier")) { //variable
            compileVariable();
        } else if (tokenizer.symbol().equals("-") || tokenizer.symbol().equals("~")) {
            compileNegationOrLogicalNot();
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

    private void compileExpressionInParenthesis(){
        tokenizer.advanceToken();
        compileExpression();
        tokenizer.advanceToken();
    }

    private void compileIntegerConstant(){
        writer.writePush(Segment.CONSTANT, Integer.parseInt(tokenizer.intVal()));
        tokenizer.advanceToken();
    }

    private void compileStringConstant(){
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
    }

    private void compileKeywordInTerm(){
        if (tokenizer.keyWord().equals("true")) {
            writer.writePush(Segment.CONSTANT, 0);
            writer.writeArithmetic(Command.NOT);
        }
        if (tokenizer.keyWord().equals("false")) {
            writer.writePush(Segment.CONSTANT, 0);
        }
        if (tokenizer.keyWord().equals("this")) {
            writer.writePush(Segment.POINTER, 0);
        }
        if (tokenizer.keyWord().equals("null")) {
            writer.writePush(Segment.CONSTANT, 0);
        }
        tokenizer.advanceToken();
    }

    private void compileArray(){
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
    }

    private void compileMethodOrFunctionCall(){
        if (tokenizer.nextSymbol().equals(".")) {
            compileMethodOrFunctionCallWithDot();
        } else {
            compileMethodCall();
        }
    }

    private void compileMethodOrFunctionCallWithDot(){
        var identifier = compileIdentifier();
        var className = getClassNameOfIdentifier(identifier);
        var kind = symbolTable.kindOf(identifier);

        if (kind != Kind.NONE) {
            compileMethodCall(identifier);
        } else {
            compileFunctionCall(className);
        }
    }

    private void compileMethodCall(String identifier){
        var className = getClassNameOfIdentifier(identifier);
        var kind = symbolTable.kindOf(identifier);
        var segment = getSegment(kind);


        tokenizer.advanceToken(); //symbol: "."
        var methodName = compileIdentifier();
        tokenizer.advanceToken(); //symbol "("
        var nrOfArguments = compileExpressionList();
        tokenizer.advanceToken(); //symbol ")"

        writer.writePush(segment, symbolTable.indexOf(identifier));

        writer.writeCall(className + "." + methodName, nrOfArguments + 1);
    }

    private void compileFunctionCall(String className){
        tokenizer.advanceToken(); //symbol: "."

        var methodName = compileIdentifier();

        tokenizer.advanceToken(); //symbol "("
        var nrOfArguments = compileExpressionList();
        tokenizer.advanceToken(); //symbol ")"
        writer.writeCall(className + "." + methodName, nrOfArguments);
    }

    private String getClassNameOfIdentifier(String identifier){
        String className;
        if(Character.isUpperCase(identifier.charAt(0))){ //If the first letter is uppercase, it must be a classname.
            return identifier;
        }else{
            return symbolTable.typeOf(identifier);
        }
    }

    private void compileMethodCall(){
        var methodName = compileIdentifier();
        tokenizer.advanceToken(); //symbol "("
        var nrOfArguments = compileExpressionList();
        tokenizer.advanceToken(); //symbol ")"
        writer.writeCall(currentClassName + "." + methodName, nrOfArguments);
    }

    private void compileVariable(){
        var variableName = compileIdentifier();
        var variableKind = symbolTable.kindOf(variableName);

        if (variableKind == Kind.STATIC)
            writer.writePush(Segment.STATIC, symbolTable.indexOf(variableName));
        if (variableKind == Kind.VAR)
            writer.writePush(Segment.LOCAL, symbolTable.indexOf(variableName));
        if (variableKind == Kind.ARG)
            writePushForArgumentVariable(variableName);
        if (variableKind == Kind.FIELD)
            writer.writePush(Segment.THIS, symbolTable.indexOf(variableName));
    }

    private void writePushForArgumentVariable(String variableName){
        if (isMethod) {
            writer.writePush(Segment.ARGUMENT, symbolTable.indexOf(variableName) + 1);
        } else {
            writer.writePush(Segment.ARGUMENT, symbolTable.indexOf(variableName));
        }
    }

    private void compileNegationOrLogicalNot(){
        var operation = compileOperation();
        compileTerm();
        if (operation.equals("-"))
            writer.writeArithmetic(Command.NEG);
        if (operation.equals("~"))
            writer.writeArithmetic(Command.NOT);
    }
}
