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

package org.cryptool.ctts.grams;

import org.cryptool.ctts.util.Token;

import java.util.ArrayList;
import java.util.Arrays;

public class Ngrams6 {

    private static int index(int p1, int p2, int p3, int p4, int p5, int p6, int dim) {
        return (((((p1 * dim + p2) * dim + p3) * dim + p4)) * dim + p5) * dim + p6;
    }

    public static double[] stats(ArrayList<Token> tokens, int dim, boolean removeSpaces) {
        long space = (long) Math.pow(dim, 6);
        if (space > Integer.MAX_VALUE) {
            throw new RuntimeException("Too many types " + dim + " - max allowed " + (int) (Math.exp(Math.log(Integer.MAX_VALUE) / 6.)));
        }
        double[] stats = new double[(int) space];
        int p1 = -1;
        int p2 = -1;
        int p3 = -1;
        int p4 = -1;
        int p5 = -1;
        for (Token token : tokens) {
            if (removeSpaces && token.type == Token.Type.OTHER) {
                continue;
            }
            if (token.type == Token.Type.NEW_LINE) {
                continue;
            }
            int p6 = token.cIndex;
            if (p1 != -1 && p2 != -1 && p3 != -1 && p4 != -1 && p5 != -1 && p6 != -1) {
                stats[index(p1, p2, p3, p4, p5, p6, dim)]++;
            }
            p1 = p2;
            p2 = p3;
            p3 = p4;
            p4 = p5;
            p5 = p6;
        }
        for (p1 = 0; p1 < dim; p1++) {
            for (p2 = 0; p2 < dim; p2++) {
                for (p3 = 0; p3 < dim; p3++) {
                    for (p4 = 0; p4 < dim; p4++) {
                        for (p5 = 0; p5 < dim; p5++) {
                            for (int p6 = 0; p6 < dim; p6++) {
                                final int index = index(p1, p2, p3, p4, p5, p6, dim);
                                double val = stats[index];
                                if (val == 0) {
                                    continue;
                                }
                                stats[index] = 10_000.0 * Math.log(1 + val);
                            }
                        }
                    }
                }
            }
        }
        return stats;
    }

    public static double score(int[] cToP, int[] cArray, double[] stats, int dimRef, double[] pCounts) {

        Arrays.fill(pCounts, 0.0);
        int totalMonograms = 0;
        int total = 0;
        double score = 0;
        int p1 = -1;
        int p2 = -1;
        int p3 = -1;
        int p4 = -1;
        int p5 = -1;
        for (int c : cArray) {
            int p6 = c == -1 ? -1 : cToP[c];
            if (p6 != -1) {
                pCounts[p6]++;
                totalMonograms++;
                if (p1 != -1 && p2 != -1 && p3 != -1 && p4 != -1 && p5 != -1) {
                    double val = stats[index(p1, p2, p3, p4, p5, p6, dimRef)];
                    score += val;
                    total++;
                }
            }
            p1 = p2;
            p2 = p3;
            p3 = p4;
            p4 = p5;
            p5 = p6;
        }
        if (total == 0 || totalMonograms == 0) {
            return 0;
        }

        double ic = 0.0;
        for (double count : pCounts) {
            count /= totalMonograms;
            ic += count * count;
        }
        ic *= dimRef;
        score /= total;

        return 1000.0 * score / ic;
    }
}
