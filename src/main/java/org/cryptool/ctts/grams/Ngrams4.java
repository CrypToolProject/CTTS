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

import java.util.ArrayList;
import java.util.Arrays;

import org.cryptool.ctts.cryptanalysis.CryptanalysisParameters;
import org.cryptool.ctts.util.Token;

public class Ngrams4 {

    public static int index(int p1, int p2, int p3, int p4, int dim) {
        return (((p1 * dim + p2) * dim + p3) * dim + p4);
    }

    public static int[] stats(ArrayList<Token> tokens, int dim, CryptanalysisParameters p) {
        int[] stats = new int[(int) Math.pow(dim, 4)];
        int p1 = -1;
        int p2 = -1;
        int p3 = -1;
        for (Token token : tokens) {
            if (p != null && p.removeSpaces && token.type == Token.Type.OTHER) {
                continue;
            }
            if (token.type == Token.Type.NEW_LINE) {
                continue;
            }
            int p4 = token.cIndex;
            if (p1 != -1 && p2 != -1 && p3 != -1 && p4 != -1) {
                stats[index(p1, p2, p3, p4, dim)]++;
            }
            p1 = p2;
            p2 = p3;
            p3 = p4;
        }
        for (p1 = 0; p1 < dim; p1++) {
            for (p2 = 0; p2 < dim; p2++) {
                for (p3 = 0; p3 < dim; p3++) {
                    for (int p4 = 0; p4 < dim; p4++) {
                        final int index = index(p1, p2, p3, p4, dim);
                        int val = stats[index];
                        if (val == 0) {
                            continue;
                        }
                        stats[index] = (int) (10000 * Math.log(1 + val));
                    }
                }
            }
        }

        return stats;
    }

    public static long score(int[] cToP, int[] cArray, int[] stats, int dimRef, double[] pCounts) {

        Arrays.fill(pCounts, 0.0);
        int totalMonograms = 0;
        int total = 0;
        long score = 0;
        int p1 = -1;
        int p2 = -1;
        int p3 = -1;
        for (int c : cArray) {
            int p4 = c == -1 ? -1 : cToP[c];
            if (p4 != -1) {
                pCounts[p4]++;
                totalMonograms++;
                if (p1 != -1 && p2 != -1 && p3 != -1) {
                    int val = stats[index(p1, p2, p3, p4, dimRef)];
                    score += val;
                    total++;
                }
            }
            p1 = p2;
            p2 = p3;
            p3 = p4;
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

        return (long) (1000 * score / ic);
    }
}