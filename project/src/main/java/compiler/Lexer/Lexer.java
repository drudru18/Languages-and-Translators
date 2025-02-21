package compiler.Lexer;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class Lexer {


    /**
     * Map<String, Symbol> tokenList;
     *     public void addSymbol() {
     *         tokenList = new HashMap<>();
     *         tokenList.putIfAbsent("var", Symbol.KEYWORD);
     *         tokenList.putIfAbsent("if", Symbol.KEYWORD);
     *         tokenList.putIfAbsent("else", Symbol.KEYWORD);
     *         tokenList.putIfAbsent("for", Symbol.KEYWORD);
     *         tokenList.putIfAbsent("while", Symbol.KEYWORD);
     *         tokenList.putIfAbsent("break", Symbol.KEYWORD);
     *         tokenList.putIfAbsent("return", Symbol.KEYWORD);
     *         tokenList.putIfAbsent("this", Symbol.KEYWORD);
     *         tokenList.putIfAbsent("true", Symbol.KEYWORD);
     *         tokenList.putIfAbsent("false", Symbol.KEYWORD);
     *         tokenList.putIfAbsent("null", Symbol.KEYWORD);
     *         tokenList.putIfAbsent("void", Symbol.KEYWORD);
     *         tokenList.putIfAbsent("string", Symbol.STRING); // immutable
     *         tokenList.putIfAbsent("int", Symbol.INTEGER);
     *         tokenList.putIfAbsent("float", Symbol.FLOAT);
     *         tokenList.putIfAbsent("bool", Symbol.BOOLEAN);
     *         tokenList.putIfAbsent("+", Symbol.PLUS); // plus for int and float, concatenation for string
     *         tokenList.putIfAbsent("-", Symbol.MINUS);
     *         tokenList.putIfAbsent("*", Symbol.MULTIPLY);
     *         tokenList.putIfAbsent("/", Symbol.DIVIDE);
     *         tokenList.putIfAbsent("%", Symbol.MODULO); // for int only
     *         tokenList.putIfAbsent("==", Symbol.EQUAL);
     *         tokenList.putIfAbsent("!=", Symbol.DIFFERENT);
     *         tokenList.putIfAbsent("&&", Symbol.AND); // for boolean
     *         tokenList.putIfAbsent("||", Symbol.OR); // for boolean
     *         tokenList.putIfAbsent("=", Symbol.ASSIGNMENT_OPERATOR);
     *         tokenList.putIfAbsent("<", Symbol.STRICTLY_LOWER);
     *         tokenList.putIfAbsent(">", Symbol.STRICTLY_GREATER);
     *         tokenList.putIfAbsent("<=", Symbol.LOWER_OR_EQUALS);
     *         tokenList.putIfAbsent(">=", Symbol.GREATER_OR_EQUALS);
     *         tokenList.putIfAbsent("(", Symbol.LEFT_PARANTHESIS);
     *         tokenList.putIfAbsent(")", Symbol.RIGHT_PARANTHESIS);
     *         tokenList.putIfAbsent("{", Symbol.LEFT_BRACE);
     *         tokenList.putIfAbsent("}", Symbol.RIGHT_BRACE);
     *         tokenList.putIfAbsent("[", Symbol.LEFT_BRACKET);
     *         tokenList.putIfAbsent("]", Symbol.RIGHT_BRACKET);
     *         tokenList.putIfAbsent(" ", Symbol.SPACE);
     *         tokenList.putIfAbsent("$", Symbol.COMMENT);
     *         tokenList.putIfAbsent(".", Symbol.DOT); // for function access
     *     }
     */ // test


    private final String input;
    private int index = 0;
    private final List<String> keywords = List.of("final", "rec", "fun", "if", "else", "while", "for", "return", "free");
    private final List<String> types = List.of("int", "float", "bool", "string", "Point", "Person");
    private final List<Character> delimiters = List.of('(', ')', '{', '}', ';', ',', '[', ']', '.');
    private final List<String> operators = List.of("+", "-", "*", "/", "%", "==", "!=", "<", ">", "<=", ">=", "&&", "||");

    private char peek() {
        return index < input.length() ? input.charAt(index) : '\0';
    }

    private char advance() {
        return index < input.length() ? input.charAt(index++) : '\0';
    }

    private void skipWhitespace() {
        while (Character.isWhitespace(peek())) {
            advance();
        }
    }

    private Symbol readNumber() {
        StringBuilder num = new StringBuilder();
        boolean isFloat = false;

        while (Character.isDigit(peek()) || peek() == '.') {
            if (peek() == '.') {
                if (isFloat) break; // Only one dot allowed
                isFloat = true;
            }
            num.append(advance());
        }

        return new Symbol(isFloat ? Symbol.Type.FLOAT : Symbol.Type.INTEGER, num.toString());
    }

    private Symbol readIdentifier() {
        StringBuilder id = new StringBuilder();
        while (Character.isLetterOrDigit(peek()) || peek() == '_') {
            id.append(advance());
        }

        String word = id.toString();
        if (keywords.contains(word)) return new Symbol(Symbol.Type.KEYWORD, word);
        if (types.contains(word)) return new Symbol(Symbol.Type.TYPE, word);

        return new Symbol(Symbol.Type.IDENTIFIER, word);
    }

    private Symbol readString() {
        advance(); // Skip opening quote
        StringBuilder str = new StringBuilder();

        while (peek() != '"' && peek() != '\0') {
            str.append(advance());
        }

        if (peek() == '"') advance(); // Skip closing quote

        return new Symbol(Symbol.Type.STRING, str.toString());
    }

    private Symbol readOperatorOrDelimiter() {
        StringBuilder op = new StringBuilder();

        while (!Character.isWhitespace(peek()) && !Character.isLetterOrDigit(peek()) && !delimiters.contains(peek())) {
            op.append(advance());
            if (operators.contains(op.toString())) {
                return new Symbol(Symbol.Type.OPERATOR, op.toString());
            }
        }

        return new Symbol(Symbol.Type.OPERATOR, op.toString());
    }

    private Symbol readComment() {
        advance(); // Skip '$'
        StringBuilder comment = new StringBuilder();

        while (peek() != '\n' && peek() != '\0') {
            comment.append(advance());
        }

        return new Symbol(Symbol.Type.COMMENT, comment.toString());
    }

    public List<Symbol> tokenize() {
        List<Symbol> tokens = new ArrayList<>();

        while (index < input.length()) {
            skipWhitespace();

            char current = peek();

            if (Character.isDigit(current)) {
                tokens.add(readNumber());
            } else if (Character.isLetter(current) || current == '_') {
                tokens.add(readIdentifier());
            } else if (current == '"') {
                tokens.add(readString());
            } else if (delimiters.contains(current)) {
                tokens.add(new Symbol(Symbol.Type.DELIMITER, String.valueOf(advance())));
            } else if (current == '$') {
                tokens.add(readComment());
            } else if (!Character.isWhitespace(current)) {
                tokens.add(readOperatorOrDelimiter());
            } else {
                advance();
            }
        }

        tokens.add(new Symbol(Symbol.Type.END, ""));
        return tokens;
    }

    public Lexer(Reader input) {
        ArrayList<Character> chars = new ArrayList<>();
        try {
            if (input.ready()) {
                int read = 0;
                while (read != -1) {
                    read = input.read();
                    chars.add((char) read);
                }
                input.close();
            }
            chars.remove(chars.size()-1);
            StringBuilder buffer = new StringBuilder();
            for (Character aChar : chars) {
                buffer.append(aChar);
            }
            this.input = buffer.toString();
            List<Symbol> tokens = tokenize();
            for (Symbol token : tokens) {
                System.out.println(token);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // do we have to use it?
    public Symbol getNextSymbol() {
        return null;
    }
}
