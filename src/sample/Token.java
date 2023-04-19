package sample;

import java.util.ArrayList;
import java.util.TreeMap;

class Token {


    enum Type {HOMOPHONE, OTHER, NEW_LINE}

    String c;
    String p;
    Type type;
    int cIndex;

    Token(Type type, String c, String p) {
        this.type = type;
        this.c = c;
        this.p = p;
        this.cIndex = -1;
    }

    Token(Type type, String c) {
        this.type = type;
        this.c = c;
        this.cIndex = -1;
        switch (type) {
            case HOMOPHONE:
            case OTHER:
                if (c.equals("_")) {
                    this.p = c;
                } else {
                    this.p = "<" + c + ">";
                }
                break;
            case NEW_LINE:
                this.p = "|";
                break;
            default:
                throw new RuntimeException("Unexpected " + type);
        }
    }

    Token(Type type) {
        this.type = type;
        this.cIndex = -1;
        if (type == Type.NEW_LINE) {
            this.c = "|";
        } else {
            throw new RuntimeException("Unexpected " + type);
        }
    }


    @Override
    public String toString() {
        return "" + type + " " + c + " " + ((p == null || p.isEmpty()) ? "" : p) + " " + ((cIndex != -1) ? ("" + cIndex) : "");
    }

    static TreeMap<String, Integer> tokenPlaintextCounts(ArrayList<Token> tokens) {
        TreeMap<String, Integer> counts = new TreeMap<>();
        for (Token token : tokens) {
            if (token.type == Type.HOMOPHONE) {
                counts.put(token.p, counts.getOrDefault(token.p, 0) + 1);
            }
        }

        return counts;
    }


}
