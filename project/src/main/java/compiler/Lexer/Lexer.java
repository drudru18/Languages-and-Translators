package compiler.Lexer;

import compiler.Lexer.Symbols.*;
import compiler.Lexer.Symbols.Delimiters.*;
import compiler.Lexer.Symbols.Keywords.*;
import compiler.Lexer.Symbols.Keywords.Types.BoolType;
import compiler.Lexer.Symbols.Keywords.Types.FloatType;
import compiler.Lexer.Symbols.Keywords.Types.IntType;
import compiler.Lexer.Symbols.Keywords.Types.StringType;
import compiler.Lexer.Symbols.Numbers.FloatNumber;
import compiler.Lexer.Symbols.Numbers.IntegerNumber;
import compiler.Lexer.Symbols.Operators.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

public class Lexer {

    /**
     * Map<String, Symbol> tokenList;
     *     public void addSymbol() {
     *         tokenList = new HashMap<>();
     *         tokenList.putIfAbsent("free", Symbol.KEYWORD);
     *         tokenList.putIfAbsent("final", Symbol.KEYWORD);
     *         tokenList.putIfAbsent("rec", Symbol.KEYWORD);
     *         tokenList.putIfAbsent("fun", Symbol.KEYWORD);
     *         tokenList.putIfAbsent("for", Symbol.KEYWORD);
     *         tokenList.putIfAbsent("while", Symbol.KEYWORD);
     *         tokenList.putIfAbsent("if", Symbol.KEYWORD);
     *         tokenList.putIfAbsent("else", Symbol.KEYWORD);
     *         tokenList.putIfAbsent("return", Symbol.KEYWORD);
     *         tokenList.putIfAbsent("true", Symbol.KEYWORD);
     *         tokenList.putIfAbsent("false", Symbol.KEYWORD);
     *         tokenList.putIfAbsent("string", Symbol.STRING); // immutable
     *         tokenList.putIfAbsent("int", Symbol.INTEGER);
     *         tokenList.putIfAbsent("float", Symbol.FLOAT);
     *         tokenList.putIfAbsent("bool", Symbol.BOOLEAN);
     *         tokenList.putIfAbsent("+", Symbol.PLUS); // plus for int and float, concatenation for string
     *         tokenList.putIfAbsent("-", Symbol.MINUS);
     *         tokenList.putIfAbsent("*", Symbol.MULTIPLY);
     *         tokenList.putIfAbsent("/", Symbol.DIVIDE);
     *         tokenList.putIfAbsent("%", Symbol.MODULO); // for int only
     *  ---       tokenList.putIfAbsent("==", Symbol.EQUAL);
     *  ---       tokenList.putIfAbsent("!=", Symbol.DIFFERENT);
     *  ---       tokenList.putIfAbsent("!", Symbol.NOT);
     *  ---       tokenList.putIfAbsent("&&", Symbol.AND); // for boolean
     *  ---       tokenList.putIfAbsent("||", Symbol.OR); // for boolean
     *  ---       tokenList.putIfAbsent("=", Symbol.ASSIGNMENT_OPERATOR);
     *  ---       tokenList.putIfAbsent("<", Symbol.STRICTLY_LOWER);
     *  ---       tokenList.putIfAbsent(">", Symbol.STRICTLY_GREATER);
     *  ---       tokenList.putIfAbsent("<=", Symbol.LOWER_OR_EQUALS);
     *  ---       tokenList.putIfAbsent(">=", Symbol.GREATER_OR_EQUALS);
     *         tokenList.putIfAbsent("(", Symbol.LEFT_PARANTHESIS);
     *         tokenList.putIfAbsent(")", Symbol.RIGHT_PARANTHESIS);
     *         tokenList.putIfAbsent("{", Symbol.LEFT_BRACE);
     *         tokenList.putIfAbsent("}", Symbol.RIGHT_BRACE);
     *         tokenList.putIfAbsent("[", Symbol.LEFT_BRACKET);
     *         tokenList.putIfAbsent("]", Symbol.RIGHT_BRACKET);
     *         tokenList.putIfAbsent(";", Symbol.Semicolon);
     *         tokenList.putIfAbsent(" ", Symbol.SPACE);
     *         tokenList.putIfAbsent("$", Symbol.COMMENT);
     *         tokenList.putIfAbsent(".", Symbol.DOT); // for function access
     *     }
     */

    public int currentInputPosition = 0;
    public String inputString;

    public static final Map<String, Class<?>> stringToKeyword = new HashMap<>();
    static {
        stringToKeyword.put("free", FreeKeyword.class);
        stringToKeyword.put("final", FinalKeyword.class);
        stringToKeyword.put("rec", RecKeyword.class);
        stringToKeyword.put("fun", FunKeyword.class);
        stringToKeyword.put("for", ForKeyword.class);
        stringToKeyword.put("while", WhileKeyword.class);
        stringToKeyword.put("if", IfKeyword.class);
        stringToKeyword.put("else", ElseKeyword.class);
        stringToKeyword.put("return", ReturnKeyword.class);
        stringToKeyword.put("true", TrueKeyword.class);
        stringToKeyword.put("false", FalseKeyword.class);
        stringToKeyword.put("array", ArrayKeyword.class);
        stringToKeyword.put("of", OfKeyword.class);
        stringToKeyword.put("string", StringType.class);
        stringToKeyword.put("int", IntType.class);
        stringToKeyword.put("float", FloatType.class);
        stringToKeyword.put("bool", BoolType.class);
    }

    // There are symbols that are made of only 1 character and cannot be confused with others
    public static final Map<Character, Class<?>> stringToUniqueCharacterClass = new HashMap<>();
    static {
        stringToUniqueCharacterClass.put('+', PlusOperator.class);
        stringToUniqueCharacterClass.put('-', MinusOperator.class);
        stringToUniqueCharacterClass.put('*', MultiplyOperator.class);
        stringToUniqueCharacterClass.put('/', DivideOperator.class);
        stringToUniqueCharacterClass.put('%', ModuloOperator.class);
        stringToUniqueCharacterClass.put('(', LeftParenthesis.class);
        stringToUniqueCharacterClass.put(')', RightParenthesis.class);
        stringToUniqueCharacterClass.put('{', LeftBrace.class);
        stringToUniqueCharacterClass.put('}', RightBrace.class);
        stringToUniqueCharacterClass.put('[', LeftBracket.class);
        stringToUniqueCharacterClass.put(']', RightBracket.class);
        stringToUniqueCharacterClass.put('.', Dot.class);
        stringToUniqueCharacterClass.put(';', Semicolon.class);
        stringToUniqueCharacterClass.put(',', Comma.class);
    }

    public Lexer(Reader input) {
        try{
            if(input.ready()){
                StringBuilder inputStringBuilder = new StringBuilder();
                int charRead = 0;
                while(charRead != -1){
                    charRead = input.read();
                    inputStringBuilder.append((char)charRead);
                }
                this.inputString = inputStringBuilder.toString();
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        };
    }

    public char top(){
        return inputString.charAt(currentInputPosition);
    }

    public void advance(){
        currentInputPosition++;
    }

    public void ignoreSpacesTabsNewlines(){
        while (top() == ' ' || top() == '\t' || top() == '\n'){
            advance();
        }
    }

    public boolean isEndOfFile(){
        return currentInputPosition == inputString.length() - 1;
    }

    public boolean isLowerCase(){
        return top() >= 'a' && top() <= 'z';
    }

    public boolean isUpperCase(){
        return top() >= 'A' && top() <= 'Z';
    }

    public boolean isUnderscore(){
        return top() == '_';
    }

    public boolean isIdentifierOrKeyword(){
        return  isUnderscore() || isLowerCase() || isUpperCase();
    }

    public boolean isNumeric(){
        return top() >= '0' && top() <= '9';
    }

    public boolean isAlphanumeric(){
        return isIdentifierOrKeyword() || isNumeric();
    }

    public Symbol buildIdentifierOrKeyword(){
        StringBuilder valueIdentifierOrKeyword = new StringBuilder();
        do {
            valueIdentifierOrKeyword.append(top());
            advance();
        } while (isAlphanumeric());

        // Check if the string is a keyword
        if(stringToKeyword.containsKey(valueIdentifierOrKeyword.toString())){
            // If yes, return the right subclass of Symbol
            try {
                return (Symbol) stringToKeyword.get(valueIdentifierOrKeyword.toString()).getDeclaredConstructor().newInstance();
            }
            catch (Exception e) {throw new RuntimeException();}
        }
        // Else, just return an identifier
        return new IdentifierType(valueIdentifierOrKeyword.toString());
    }

    public boolean isFloatWithDotFirst(){
        return top() == '.';
    }

    public boolean isNumber(){
        return isNumeric() || isFloatWithDotFirst();
    }

    public Symbol buildNumber(){
        boolean hasDigitsBeforeDot = false;
        while(top() == '0'){
            hasDigitsBeforeDot = true;
            advance();
        }
        StringBuilder valueNumber = new StringBuilder();
        boolean hasNumeric = false;
        while(isNumeric()){
            hasDigitsBeforeDot = true;
            valueNumber.append(top());
            advance();
            hasNumeric = true;
        }
        if(!hasNumeric){
            valueNumber.append("0");
        }
        if(top() != '.'){
            return new IntegerNumber(valueNumber.toString());
        }
        int numberOfNumeric = -1;
        do{
            valueNumber.append(top());
            advance();
            numberOfNumeric++;
        } while (isNumeric());
        if(numberOfNumeric == 0 && !hasDigitsBeforeDot){
            return new Dot();
        }
        if(numberOfNumeric == 0){
            valueNumber.append("0");
        }
        return new FloatNumber(valueNumber.toString());
    }

    public boolean isComment(){
        return top() == '$';
    }

    public void ignoreComment(){
        while(top() != '\n'){
            advance();
        }
    }

    public boolean isMadeOfUnambiguousSingleCharacter(){
        return stringToUniqueCharacterClass.containsKey(top());
    }

    public Symbol buildCorrectUniqueSingleCharacter(){
        char currentChar = top();
        advance();
        try {
            return (Symbol) stringToUniqueCharacterClass.get(currentChar).getDeclaredConstructor().newInstance();
        }
        catch (Exception e) {throw new RuntimeException();}
    }

    public boolean isAssignmentOrEquals(){
        return top() == '=';
    }

    public Symbol buildAssignmentOrEquals(){
        advance();
        if(top() == '='){
            advance();
            return new EqualsOperator();
        }
        else{
            return new Assignment();
        }
    }

    public boolean isLowerOrLowerOrEqual(){
        return top() == '<';
    }

    public Symbol buildLowerOrLowerOrEqual(){
        advance();
        if(top() == '='){
            advance();
            return new LowerOrEqualsOperator();
        }
        else{
            return new StrictlyLowerThanOperator();
        }
    }

    public boolean isGreaterOrGreaterOrEqual(){
        return top() == '>';
    }

    public Symbol buildGreaterOrGreaterOrEqual(){
        advance();
        if(top() == '='){
            advance();
            return new GreaterOrEqualsOperator();
        }
        else{
            return new StrictlyGreaterThanOperator();
        }
    }

    public boolean isNotOrDifferent(){
        return top() == '!';
    }

    public Symbol buildIsNotOrDifferent(){
        advance();
        if(top() == '='){
            advance();
            return new DifferentOperator();
        }
        else{
            return new Not();
        }
    }

    public boolean isAnd(){
        return top() == '&';
    }

    public Symbol buildAnd(){
        advance();
        if(top() == '&'){
            advance();
            return new AndOperator();
        }
        return null;
    }

    public boolean isOr(){
        return top() == '|';
    }

    public Symbol buildOr(){
        advance();
        if(top() == '|'){
            advance();
            return new OrOperator();
        }
        return null;
    }

    public boolean isString(){
        return top() == '"';
    }

    public Symbol buildString(){
        advance();
        StringBuilder valueString = new StringBuilder();
        while(top() != '"'){
            valueString.append(top());
            advance();
        }
        advance();
        return new StringValue(valueString.toString());
    }


    public Symbol getNextSymbol() {
        ignoreSpacesTabsNewlines();
        // END OF FILE
        if(isEndOfFile()){
            return new EndOfInputType();
        }
        // IDENTIFIER or KEYWORD
        if(isIdentifierOrKeyword()){
            return buildIdentifierOrKeyword();
        }
        // NUMBER
        if(isNumber()){
            return buildNumber();
        }
        // COMMENT (ignore it)
        if(isComment()){
            ignoreComment();
            // Call again because next Symbol might be missed
            return getNextSymbol();
        }
        if(isMadeOfUnambiguousSingleCharacter()){
            return buildCorrectUniqueSingleCharacter();
        }
        if(isAssignmentOrEquals()){
            return buildAssignmentOrEquals();
        }
        if(isLowerOrLowerOrEqual()){
            return buildLowerOrLowerOrEqual();
        }
        if(isGreaterOrGreaterOrEqual()){
            return buildGreaterOrGreaterOrEqual();
        }
        if(isNotOrDifferent()){
            return buildIsNotOrDifferent();
        }
        if(isAnd()){
            return buildAnd();
        }
        if(isOr()){
            return buildOr();
        }
        if(isString()){
            return buildString();
        }
        throw new InvalidParameterException("There is an unrecognized symbol");
    }

    /*
    public static void main(String[] args) {
        Lexer lexer = new Lexer(new StringReader("0034 00.013 . fun copyPoints(Point[] p) Point { \n" +
                "\n" +
                "    return Point(p[0].x+p[1].x, p[0].y+p[1].y); \n" +
                "\n" +
                "} "));
        Symbol currentSymbol;
        do{
            currentSymbol = lexer.getNextSymbol();
            if(currentSymbol == null){
                System.out.println("Null");
                break;
            }
            System.out.println(currentSymbol);
            //System.out.println(currentSymbol.getClass());
            //System.out.println(currentSymbol instanceof IdentifierType);
        }while(currentSymbol.getClass() != EndOfInputType.class);
    }*/
}
