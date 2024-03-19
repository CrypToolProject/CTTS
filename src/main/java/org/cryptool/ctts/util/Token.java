/*
    Copyright 2023 George Lasry & CrypTool 2 Team

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package org.cryptool.ctts.util;

import java.util.ArrayList;
import java.util.TreeMap;

public class Token {
    public String c;
    public String p;
    public Type type;
    public int cIndex;
    int pIndex;
    int count;

    public Token(Type type, String c, String p) {
        this.type = type;
        this.c = c;
        this.p = p;
        this.cIndex = -1;
        this.count = 0;
    }

    public Token(Type type, String c) {
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

    public Token(Type type) {
        this.type = type;
        this.cIndex = -1;
        if (type == Type.NEW_LINE) {
            this.c = "|";
        } else {
            throw new RuntimeException("Unexpected " + type);
        }
    }

    static TreeMap<String, Integer> tokenPlaintextCounts(ArrayList<Token> tokens) {
        return tokenPlaintextCounts(tokens, Type.HOMOPHONE);
    }

    static TreeMap<String, Integer> tokenPlaintextCounts(ArrayList<Token> tokens, Type type) {
        TreeMap<String, Integer> counts = new TreeMap<>();
        for (Token token : tokens) {
            if (token.type == type) {
                counts.put(token.p, counts.getOrDefault(token.p, 0) + 1);
            }
        }
        return counts;
    }

    static TreeMap<String, Double> tokenPlaintextFreq(ArrayList<Token> tokens, Type... types) {
        TreeMap<String, Double> counts = new TreeMap<>();
        int total = 0;
        for (Token token : tokens) {
            boolean found = false;
            for (Type type : types) {
                if (token.type.equals(type)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                counts.put(token.p, counts.getOrDefault(token.p, 0.0) + 1);
                total++;
            }
        }

        for (String p : counts.keySet()) {
            counts.put(p, counts.get(p) / total);
        }
        return counts;
    }

    @Override
    public String toString() {
        return "" + type + " " + c + " " + ((p == null || p.isEmpty()) ? "" : p) + " " + ((cIndex != -1) ? ("" + cIndex) : "");
    }

    public enum Type {HOMOPHONE, OTHER, NEW_LINE}

}
