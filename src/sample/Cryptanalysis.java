package sample;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class Cryptanalysis {
    static final StringBuilder keySb = new StringBuilder();
    static final AtomicBoolean started = new AtomicBoolean(false);
    static final AtomicBoolean readUpdate = new AtomicBoolean(true);
    static final AtomicLong updates = new AtomicLong(0);
    static final AtomicLong bestOverall = new AtomicLong(0);
    static final ConcurrentLinkedQueue sequence = new ConcurrentLinkedQueue();

    final private static int[][] MAX_HOMOPHONE_GROUP_SIZE = {
            {-1},                                   //0
            {-1, 26},                              //1
            {-1, 16, 10},                           //2
            {-1,  8,  8, 10},                       //3
            {-1,  5,  5,  6, 10},                   //4
            {-1,  4,  4,  4,  4, 10},               //5
            {-1,  3,  3,  3,  3,  4, 10},           //6
            {-1,  2,  2,  3,  3,  3,  3, 10},       //7

    };

    public static void solve(ArrayList<Token> ciphertextTokens, CryptanalysisParameters parameters, int maxTasks) {
        Set<String> pSet = new TreeSet<>();
        for (Token r : parameters.referenceTokens) {
            if (r.type == Token.Type.HOMOPHONE) {
                pSet.add(r.p);
            }
        }
        ArrayList<String> pList = new ArrayList<>(new TreeSet<>(pSet));
        for (Token r : parameters.referenceTokens) {
            r.cIndex = pList.indexOf(r.p);
        }
        int pListSize = pList.size();
        int[] stats = stats(parameters, pListSize);

        Set<String> cSet = new TreeSet<>();
        for (Token t : ciphertextTokens) {
            if (t.type == Token.Type.HOMOPHONE) {
                cSet.add(t.c);
            }
        }
        ArrayList<String> cList = new ArrayList<>(cSet);
        for (Token t : ciphertextTokens) {
            t.cIndex = cList.indexOf(t.c);
        }

        // Convert the ArrayList of tokens to array of indices
        int[] ciphertextTokenIndices = new int[ciphertextTokens.size()];
        int len = 0;
        for (Token token : ciphertextTokens) {
            if (token.type == Token.Type.NEW_LINE) {
                continue;
            }
            ciphertextTokenIndices[len++] = token.cIndex;
        }
        ciphertextTokenIndices = Arrays.copyOf(ciphertextTokenIndices, len);


        int[] maxHomophonesAll = new int[pListSize];

        int[] pCounts = new int[pListSize];
        for (Token t : parameters.referenceTokens) {
            if (t.cIndex != -1) {
                pCounts[t.cIndex]++;
            }
        }
        ArrayList<Integer> sorted = new ArrayList<>();
        for (int p = 0; p < pListSize; p++) {
            sorted.add(p);
        }
        sorted.sort((o1, o2) -> {
            String p1 = pList.get(o1);
            boolean isP1Wovel = "aeiouv".contains(p1);
            String p2 = pList.get(o2);
            boolean isP2Wovel = "aeiouv".contains(p2);
            if (isP1Wovel == isP2Wovel) {
                return pCounts[o2] - pCounts[o1];
            } else if (isP1Wovel) {
                return -1;
            } else if (isP2Wovel) {
                return 1;
            }
            return 0;
        });

        int i = 0;
        for (int mh = parameters.maxHomophones; mh > 0 && i < pListSize; mh--) {
            final int totalToAssign = MAX_HOMOPHONE_GROUP_SIZE[parameters.maxHomophones][mh];
            for (int count = 0; count < totalToAssign && i < pListSize; count++) {
                maxHomophonesAll[sorted.get(i++)] = mh;
            }
        }

        final int[] _ciphertextTokenIndices = ciphertextTokenIndices;
        int cores = Runtime.getRuntime().availableProcessors();

        int[] cToPforced = new int[cList.size()];
        Arrays.fill(cToPforced, -1);

        if (!parameters.ignoreCurrentKey) {
            for (String c : parameters.lockedHomophones.keySet()) {
                String p = parameters.lockedHomophones.get(c).toLowerCase();
                if (cList.contains(c) && pList.contains(p)) {
                    cToPforced[cList.indexOf(c)] = pList.indexOf(p);
                }
            }
        }

        if (started.get()) {
            stop();
        }

        bestOverall.set(0);
        Cryptanalysis.keySb.setLength(0);
        Cryptanalysis.sequence.clear();
        Cryptanalysis.started.set(true);
        Cryptanalysis.updates.set(0);

        for (int t = 0; t < Math.min(maxTasks, Math.max(1, cores / 4)); t++) {
            final int _t = t;
            new Thread(() -> SA(_t, parameters, _ciphertextTokenIndices, stats, cToPforced, maxHomophonesAll, 100000, 0, 250, ciphertextTokens, cList, pList)).start();
        }

    }

    public static void stop() {
        started.set(false);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //System.out.println("Waiting 500 ms");
    }

    public static int assignable(CryptanalysisParameters parameters, int pListSize) {
        int assignable = 0;
        int i = 0;
        for (int mh = parameters.maxHomophones; mh > 0 && i < pListSize; mh--) {
            final int totalToAssign = MAX_HOMOPHONE_GROUP_SIZE[parameters.maxHomophones][mh];
            for (int count = 0; count < totalToAssign && i < pListSize; count++, i++) {
                assignable += mh;
            }
        }
        return assignable;
    }

    private static void SA(int task, CryptanalysisParameters parameters, int[] ciphertextTokenIndices, int[] stats, int[] cToPforcedExternal, int[] maxHomophonesAll, double maxTemp, double minTemp, int rounds,
                           ArrayList<Token> ciphertextTokens, ArrayList<String> cList, ArrayList<String> pList) {

        int[] cToPforced = Arrays.copyOf(cToPforcedExternal, cToPforcedExternal.length);

        Random random = new Random();

        int cListSize = cList.size();

        // Buffer, so that it is not allocated each time we compute the score.
        int pListSize = pList.size();
        double[] pCounts = new double[pListSize];

        Random r = new Random();
        for (int cycle = 0; ; cycle++) {
            int[] cToP = new int[cListSize];
            int[] homophoneCounts = new int[pListSize];

            randomAssignment(pListSize, cToP, homophoneCounts, maxHomophonesAll, cToPforced);
            long current = score(parameters, cToP, ciphertextTokenIndices, stats, pListSize, pCounts);
            if (current > bestOverall.get()) {
                bestOverall.set(current);
            }
            for (int round = 0; round < rounds; round++) {


                int shift1 = r.nextInt(cListSize);
                double temp = minTemp + r.nextFloat() * (maxTemp - minTemp) * (rounds - round + 1) / rounds;
                for (int c_ = 0; c_ < cListSize; c_++) {
                    int c = (c_ + shift1) % cListSize;

                    int shift2 = r.nextInt(pListSize);
                    for (int p_ = 0; p_ < pListSize; p_++) {
                        if (!started.get()) {
                            //System.out.println("exit SA");
                            return;
                        }
                        int p = (p_ + shift2) % pListSize;
                        if (homophoneCounts[p] >= maxHomophonesAll[p]) {
                            continue;
                        }
                        if (pList.get(p).length() > 1 && homophoneCounts[p] >= 1) {
                            continue;
                        }

                        if (cToPforced[c] != -1 && cToPforced[c] != p) {
                            continue;
                        }

                        int previousP = assign(cToP, homophoneCounts, c, p);
                        long score = score(parameters, cToP, ciphertextTokenIndices, stats, pListSize, pCounts);
                        if (SimulatedAnnealing.accept(score, current, temp, random)) {
                            current = score;


                            if (current > bestOverall.get()) {
                                bestOverall.set(current);
                                int[] keepForced = Arrays.copyOf(cToPforced, cToPforced.length);
                                updateForced(parameters, ciphertextTokens, pList, cToP, cToPforced);
                                StringBuilder keySb = keySb(task, score, cycle, round, rounds, temp, pList, cList, cToP, cToPforced);
                                System.arraycopy(keepForced, 0, cToPforced, 0, cToPforced.length);

                                synchronized (Cryptanalysis.keySb) {
                                    Cryptanalysis.keySb.setLength(0);
                                    Cryptanalysis.keySb.append(keySb);
                                    sequence.offer(keySb.toString());
                                    updates.incrementAndGet();
                                    readUpdate.set(false);
                                }

                            }
                        } else {
                            assign(cToP, homophoneCounts, c, previousP);
                        }
                    }
                }

                for (int c1_ = 0; c1_ < cListSize; c1_++) {
                    int c1 = (c1_ + shift1) % cListSize;

                    for (int c2_ = c1_ + 1; c2_ < cListSize; c2_++) {
                        if (!started.get()) {
                            //System.out.println("exit SA");
                            return;
                        }

                        int c2 = (c2_ + shift1) % cListSize;

                        if (cToP[c1] == cToP[c2]) {
                            continue;
                        }
                        if (cToPforced[c1] != -1 && cToP[c1] == cToPforced[c1]) {
                            continue;
                        }
                        if (cToPforced[c2] != -1 && cToP[c2] == cToPforced[c2]) {
                            continue;
                        }
                        swap(cToP, c1, c2);
                        long score = score(parameters, cToP, ciphertextTokenIndices, stats, pListSize, pCounts);
                        if (SimulatedAnnealing.accept(score, current, temp, random)) {
                            current = score;

                            if (current > bestOverall.get()) {
                                bestOverall.set(current);

                                int[] keepForced = Arrays.copyOf(cToPforced, cToPforced.length);
                                updateForced(parameters, ciphertextTokens, pList, cToP, cToPforced);
                                StringBuilder keySb = keySb(task, score, cycle, round, rounds, temp, pList, cList, cToP, cToPforced);
                                System.arraycopy(keepForced, 0, cToPforced, 0, cToPforced.length);

                                synchronized (Cryptanalysis.keySb) {
                                    Cryptanalysis.keySb.setLength(0);
                                    Cryptanalysis.keySb.append(keySb);
                                    sequence.offer(keySb.toString());
                                    updates.incrementAndGet();
                                    readUpdate.set(false);
                                }
                            }
                        } else {
                            swap(cToP, c1, c2);
                        }
                    }
                }
            }
        }
    }

    private static void updateForced(CryptanalysisParameters params, ArrayList<Token> ciphertextTokens, ArrayList<String> pList, int[] cToP, int[] cToPforced) {

        String pS = "";
        int[] c = new int[ciphertextTokens.size()];


        for (int i = 0; i  < ciphertextTokens.size(); i++) {
            Token t = ciphertextTokens.get(i);
            c[i] = t.cIndex;
            String p;
            if (t.type == Token.Type.HOMOPHONE && t.cIndex != -1 && cToPforced[t.cIndex] == -1 && cToP[t.cIndex] != -1) {
                final String pp = pList.get(cToP[t.cIndex]);
                if (pp.length() == 1) {
                    p = pp;
                } else {
                    p = "%";
                }
            } else if (t.type == Token.Type.HOMOPHONE && t.cIndex != -1 && cToPforced[t.cIndex] != -1) {
                final String pp = pList.get(cToPforced[t.cIndex]);
                if (pp.length() == 1) {
                    p = pp;
                } else {
                    p = "%";
                }
            } else {
                p = "%";
            }
            pS += p;
        }

        for (int i = 0; i  < ciphertextTokens.size() - params.referenceSequenceLengthForLocking; i++) {
            String sequence = pS.substring(i, i + params.referenceSequenceLengthForLocking);
            if (params.referenceSequences.contains(sequence)) {
                for (int z = 0; z < params.referenceSequenceLengthForLocking; z++) {
                    final int cIndex = c[i + z];
                    cToPforced[cIndex] = cToP[cIndex];
                    //System.out.printf("%s %d %d %d => %d\n", sequence, i, z, cIndex, pIndex);

                }
            }
        }


    }

    private static void swap(int[] cToP, int c1, int c2) {
        int keep = cToP[c1];
        cToP[c1] = cToP[c2];
        cToP[c2] = keep;
    }

    private static int assign(int[] cToP, int[] homophoneCount, int c, int p) {
        int previousP = cToP[c];
        homophoneCount[previousP]--;
        cToP[c] = p;
        homophoneCount[p]++;
        return previousP;
    }

    private static void randomAssignment(int pListSize, int[] cToP, int[] homophoneCounts, int[] maxHomophones, int[] cToPforced) {

        int maxAssignableHomophones = 0;
        for (int p = 0; p < pListSize; p++) {
            maxAssignableHomophones += maxHomophones[p];
        }

        Arrays.fill(cToP, -1);
        int cListSize = cToP.length;
        int assignedHomophones = 0;

        if (cToPforced != null) {
            for (int c = 0; c < cListSize; c++) {
                int forcedP = cToPforced[c];
                if (forcedP != -1) {
                    cToP[c] = forcedP;
                    homophoneCounts[forcedP]++;
                    assignedHomophones++;
                }
            }
        }

        while (assignedHomophones < Math.min(maxAssignableHomophones, cListSize)) {
            int c = new Random().nextInt(cListSize);
            if (cToP[c] != -1) {
                continue;
            }
            if (cToPforced != null) {
                int forcedP = cToPforced[c];
                if (forcedP != -1) {
                    continue;
                }
            }
            int p = new Random().nextInt(pListSize);
            while (homophoneCounts[p] >= maxHomophones[p]) {
                p = (p == pListSize - 1) ? 0 : p + 1;
            }
            homophoneCounts[p]++;
            assignedHomophones++;
            cToP[c] = p;
        }

    }

    private static StringBuilder keySb(int task, long score, int cycle, int round, int rounds, double temp, ArrayList<String> pList, ArrayList<String> cList, int[] cToP, int[] cToPforced) {
        StringBuilder sb = new StringBuilder(String.format("Score: %,10d [Task: %d Cycle: %,d Round: %,d/%,d Temp: %.2f Update: %d]\n", score, task, cycle, round, rounds, temp, updates.get() + 1));

        Map<String, Set<String>> homophones = new TreeMap<>();

        for (int cIndex = 0; cIndex < cList.size(); cIndex++) {

            int pIndex = cToP[cIndex];

            int pForcedIndex = cToPforced[cIndex];

            String p;

            if (pForcedIndex != -1) {
                p = pList.get(pForcedIndex).toUpperCase();
            } else if (pIndex != -1) {
                p = pList.get(pIndex);
            } else {
                continue;
            }
            String c = cList.get(cIndex);
            if (homophones.containsKey(p)) {
                homophones.get(p).add(c);
            } else {
                Set<String> set = new TreeSet<>();
                set.add(c);
                homophones.put(p, set);
            }
        }

        for (String p : homophones.keySet()) {
            String s = "";
            for (String c : homophones.get(p)) {
                if (!s.isEmpty()) {
                    s += "|";
                }
                s += c;

            }
            sb.append(s).append(" - ").append(p).append("\n");
        }

        return sb;
    }

    private static int[] stats(CryptanalysisParameters parameters, int pListSize) {
        switch (parameters.ngrams) {
            case 4:
                return Ngrams4.stats(parameters.referenceTokens, pListSize, parameters);
            case 5:
                return Ngrams5.stats(parameters.referenceTokens, pListSize, parameters);
            case 6:
                return Ngrams6.stats(parameters.referenceTokens, pListSize, parameters);

        }
        throw new RuntimeException("Invalid ngrams: " + parameters.ngrams);
    }

    private static long score(CryptanalysisParameters parameters, int[] cToP, int[] ciphertextTokenIndices, int[] stats, int pListSize, double[] pCounts) {

        switch (parameters.ngrams) {
            case 4:
                return Ngrams4.score(cToP, ciphertextTokenIndices, stats, pListSize, pCounts);
            case 5:
                return Ngrams5.score(cToP, ciphertextTokenIndices, stats, pListSize, pCounts);
            case 6:
                return Ngrams6.score(cToP, ciphertextTokenIndices, stats, pListSize, pCounts);

        }
        throw new RuntimeException("Invalid ngrams: " + parameters.ngrams);
    }


}
