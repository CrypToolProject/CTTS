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

package org.cryptool.ota;

import java.io.InputStream;
import java.util.Locale;

enum Language {
    FRENCH, ENGLISH, GERMAN, SPANISH, ITALIAN, LATIN, DUTCH;

    static StringBuilder readFileAZSpaceOnly(Language l) {
        // for static access, uses the class name directly
        final String filename = l.toString().toLowerCase(Locale.ROOT) + ".txt";
        InputStream is = Cryptanalysis.class.getClassLoader().getResourceAsStream(filename);
        if (is == null) {
            System.out.println("Could not open resource file: " + filename);
            System.exit(0);
        }
        byte[] b = new byte[1_000_000];
        try {
            int read = is.read(b);
            String s = new String(b);

            final String RAW_PLAINTEXT_LETTERS = "abcdefghijklmnopqrstuvwxyzàáãåάąäâªªçčðďλěêèéęëįîìíïłňńñöøòóôőõθº°ǫφþřŕš§ťüúűùûů×ýżžź";
            final String PLAINTEXT_LETTERS_MAP = "abcdefghijklmnopqrstuvwxyzaaaaaaaaaaccdddeeeeeeiiiiilnnnooooooooooopprrsstuuuuuuxyzzz";

            StringBuilder sb = new StringBuilder();

            boolean wasSpace = true;
            for (String line : s.split("[\t\n]+")) {
                line = line.toLowerCase();
                for (int i = 0; i < line.length(); i++) {
                    char c = line.charAt(i);
                    int index = RAW_PLAINTEXT_LETTERS.indexOf(c);
                    if (index != -1) {
                        sb.append(PLAINTEXT_LETTERS_MAP.charAt(index));
                        wasSpace = false;
                    } else if (c == 'ß') {
                        sb.append("ss");
                        wasSpace = false;
                    } else if (!wasSpace) {
                        wasSpace = true;
                        sb.append(" ");
                    }
                }
            }

            is.close();
            return sb;

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);

            return null;
        }

    }
}
