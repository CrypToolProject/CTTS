package sample;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditedRecord {
    final String text;
    final String filename;
    final int lineNumber;

    static Map<String, Map<Integer, EditedRecord>> filenameToRecords = new TreeMap<>();
    static boolean changed = false;

    static void add(String filename, int lineNumber, String text) {
        filename = ImageUtils.removeImageFormat(filename);
        lineNumber++;
        EditedRecord eNew = new EditedRecord(filename, lineNumber, text);

        Map<Integer, EditedRecord> records = filenameToRecords.getOrDefault(filename, new TreeMap<>());
        filenameToRecords.put(filename, records);

        if (!text.isEmpty()) {
            records.put(lineNumber, eNew);
        } else {
            records.remove(lineNumber);
        }
        changed = true;
    }

    static String get(String filename, int lineNumber){
        filename = ImageUtils.removeImageFormat(filename);
        lineNumber++;
        final Map<Integer, EditedRecord> records = filenameToRecords.get(filename);
        if (records == null) {
            return null;
        }
        final EditedRecord e = records.get(lineNumber);
        if (e == null) {
            return null;
        }
        return e.text;
    }

    EditedRecord(String filename, int lineNumber, String text) {
        this.text = text;
        this.filename = filename;
        this.lineNumber = lineNumber;
    }

    static EditedRecord parse(String filename, String s) {

// specify that we want to search for two groups in the string
        Pattern p = Pattern.compile("([0-9]+)\\|(.+)");
        Matcher m = p.matcher(s);

        // if our pattern matches the string, we can try to extract our groups
        if (m.find())
        {
            // get the two groups we were looking for
            String lineNumberString = m.group(1);
            String text = m.group(2);

            try {
                int lineNumber = Integer.parseInt(lineNumberString);
                return new EditedRecord(filename, lineNumber, text);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }


    @Override
    public String toString() {
        String s = filename + "|" + lineNumber;

        if (!text.isEmpty()) {
            s = s + "|" + text;
        }

        return s;
    }

    public String toStringNoFilename() {
        String s = "" + lineNumber;

        if (!text.isEmpty()) {
            s = s + "|" + text;
        }

        return s;
    }

    public static void save(){
        if (changed) {
            StringBuilder all = new StringBuilder();
            int count = 0;
            final ArrayList<String> set = new ArrayList<>(filenameToRecords.keySet());
            set.sort((o1, o2) -> {

                String p1 = o1;
                while (p1.charAt(p1.length() - 1) < '0' || p1.charAt(p1.length() - 1) > '9') {
                    p1 = p1.substring(0, p1.length() - 1);
                }
                String p2 = o2;
                while (p2.charAt(p2.length() - 1) < '0' || p2.charAt(p2.length() - 1) > '9') {
                    p2 = p2.substring(0, p2.length() - 1);
                }
                if (p1.equals(p2)) {
                    return o1.compareTo(o2);
                }
                return p1.compareTo(p2);
            });
            for (String filename : set) {
                all.append(filename).append("\n");
                StringBuilder sbf = new StringBuilder();
                int subcount = 0;
                for (EditedRecord e : filenameToRecords.get(filename).values()) {
                    sbf.append(e.toStringNoFilename()).append("\n");
                    subcount++;
                }
                final String textFilename = ImageUtils.removeImageFormat(filename) + "_edits.txt";
                FileUtils.writeTextFile("edited", textFilename, sbf.toString());
                System.out.printf("Saved %s - %d edited records\n", textFilename, subcount);
                count += subcount;
                all.append(sbf);
            }
            FileUtils.writeTextFile("edited", "edits", all.toString());
            System.out.printf("Saved %d edited records\n", count);
            changed = false;
        }

    }
    public static void restore(){
        filenameToRecords.clear();
        changed = false;
        int count = 0;

        ArrayList<String> filenames = FileUtils.textFilesInDirectory("edited");

        for (String filename : filenames) {
            if (filename.equals("edits.txt")) {
                continue;
            }
            String f = FileUtils.readTextFile("edited", filename);
            if (f == null) {
                return;
            }
            final String filenameShort = filename.replaceAll("_edits.txt", "");
            int subCount = 0;
            for (String line : f.split("\n")) {

                EditedRecord e = EditedRecord.parse(filenameShort, line);
                //System.out.println(line);
                if (e == null) {
                    System.out.printf("Bad edited record: %s\n", line);
                    continue;
                }
                Map<Integer, EditedRecord> records = filenameToRecords.getOrDefault(filenameShort, new TreeMap<>());
                filenameToRecords.put(filenameShort, records);

                if (!e.text.isEmpty()) {
                    records.put(e.lineNumber, e);
                    subCount++;
                }

            }
            count += subCount;
            System.out.printf("Read %s - %d edited records\n", filename, subCount);
        }
        System.out.printf("Read %d edited records %d files\n", count, filenameToRecords.size());


    }
    public static void main(String[] args) {

        for (String s : new String[] {
                "t1.png|1|test1",
                "t1.png|1",

            }) {
            EditedRecord e = EditedRecord.parse("image.png", s);
            String es = e.toString();
            if (!s.equals(es)) {
                System.out.printf("%s\n%s\n\n", s, es);
            }
        }

    }
}
