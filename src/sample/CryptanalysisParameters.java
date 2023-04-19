package sample;

import java.util.*;

class CryptanalysisParameters {
    Language language;
    boolean uToV;
    boolean wToV;
    boolean jToI;
    boolean yToI;
    boolean zToS;
    boolean kToC;
    boolean removeX;
    boolean removeH;
    boolean removeSpaces = true;
    boolean removeDoubles;
    int maxHomophones = 4;
    int minCount = 3;
    boolean ignoreCurrentKey = true;
    int ngrams = 5;

    Map<String, String> lockedHomophones = new TreeMap<>();
    ArrayList<Token> referenceTokens = null;
    int referenceSequenceLengthForLocking = 12;
    Set<String> referenceSequences = null;

    CryptanalysisParameters(Language language) {
        updateLanguage(language);
    }

    public void updateLanguage(Language language) {
        this.language = language;
        uToV = wToV = jToI = yToI = zToS = kToC = removeDoubles = removeX = removeH = false;

        switch (language) {
            case FRENCH:
            case ITALIAN:
                uToV = wToV = jToI = yToI = zToS = removeDoubles = true;
                break;
            case SPANISH:
                uToV = wToV = jToI = yToI = zToS = removeDoubles = removeX = kToC = true;
                break;
            case LATIN:
                uToV = wToV = jToI = yToI = zToS = removeDoubles = kToC = true;
                break;
            case ENGLISH:
            case GERMAN:
                removeDoubles = true;
                break;
            default:
                break;
        }
    }

    public void readTokens() {
        referenceTokens = new ArrayList<>();

        StringBuilder sb = Language.readFileAZSpaceOnly(language);
        char last = ' ';
        for (char c : sb.toString().toCharArray()) {
            switch (c) {
                case 'u': {
                    if (uToV) c = 'v';
                    break;
                }
                case 'w': {
                    if (wToV) c = 'v';
                    break;
                }
                case 'j': {
                    if (jToI) c = 'i';
                    break;
                }
                case 'y': {
                    if (yToI) c = 'i';
                    break;
                }
                case 'z': {
                    if (zToS) c = 's';
                    break;
                }
                case 'k': {
                    if (kToC) c = 'c';
                    break;
                }
                case 'h': {
                    if (removeH) c = ' ';
                    break;
                }
                case 'x': {
                    if (removeX) c = ' ';
                    break;
                }
            }

            if (c == last && removeDoubles) {
                continue;
            }
            if (c == ' ') {
                if (!removeSpaces) {
                    referenceTokens.add(new Token(Token.Type.OTHER, "", " "));
                }
            } else {
                referenceTokens.add(new Token(Token.Type.HOMOPHONE, "", "" + c));
            }
            last = c;
        }

    }

    public void referenceSequences() {
        referenceSequences = new TreeSet<>();
        StringBuilder p = new StringBuilder();
        for (Token t : referenceTokens) {
            p.append(t.p);
        }
        for (int i = 0; i < p.length() - referenceSequenceLengthForLocking; i++) {
            referenceSequences.add(p.substring(i, i + referenceSequenceLengthForLocking));
        }
    }
}
