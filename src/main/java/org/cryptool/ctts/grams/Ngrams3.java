package org.cryptool.ctts.grams;

import org.cryptool.ctts.util.Token;

import java.util.ArrayList;
import java.util.Arrays;

public class Ngrams3 {
    public static int index(int p1, int p2, int p3, int dim) {
        return ((p1 * dim + p2) * dim + p3);
    }

    static double[] stats(ArrayList<Token> tokens, int dim, boolean removeSpaces) {
        long space = (long) Math.pow(dim, 3);
        if (space > Integer.MAX_VALUE) {
            throw new RuntimeException("Too many types " + dim + " - max allowed " + (int) (Math.exp(Math.log(Integer.MAX_VALUE) / 3.)));
        }
        double[] stats = new double[(int) space];
        int p1 = -1;
        int p2 = -1;
        for (Token token : tokens) {
            if (removeSpaces && token.type == Token.Type.OTHER) {
                continue;
            }
            if (token.type == Token.Type.NEW_LINE) {
                continue;
            }
            int p3 = token.cIndex;
            if (p1 != -1 && p2 != -1 && p3 != -1) {
                stats[index(p1, p2, p3, dim)]++;
            }
            p1 = p2;
            p2 = p3;
        }
        for (p1 = 0; p1 < dim; p1++) {
            for (p2 = 0; p2 < dim; p2++) {
                for (int p3 = 0; p3 < dim; p3++) {
                    final int index = index(p1, p2, p3, dim);
                    double val = stats[index];
                    if (val == 0) {
                        continue;
                    }
                    stats[index] = 10_000.0 * Math.log(1 + val);
                }

            }
        }

        return stats;
    }

    static double score(int[] cToP, int[] cArray, double[] stats, int dimRef, double[] pCounts) {

        Arrays.fill(pCounts, 0.0);
        int totalMonograms = 0;
        int total = 0;
        double score = 0;
        int p1 = -1;
        int p2 = -1;
        for (int c : cArray) {
            int p3 = c == -1 ? -1 : cToP[c];
            if (p3 != -1) {
                pCounts[p3]++;
                totalMonograms++;
                if (p1 != -1 && p2 != -1) {
                    double val = stats[index(p1, p2, p3, dimRef)];
                    score += val;
                    total++;
                }
            }
            p1 = p2;
            p2 = p3;
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
