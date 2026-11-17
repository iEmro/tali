package com.nasser.tali;

import android.content.Context;
import android.util.Xml;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import org.xmlpull.v1.XmlPullParser;

public class QuranRepo {
    private static QuranRepo instance;
    public final ArrayList<String> texts = new ArrayList<>();
    public final ArrayList<Integer> suras = new ArrayList<>();
    public final ArrayList<Integer> ayas = new ArrayList<>();
    private final int[] suraStart = new int[116];
    private final int[] suraCount = new int[116];
    private int[] juzStarts = new int[0];
    private int[] pageStarts = new int[0];

    public static final String[] NAMES = {
        "الفاتحة", "البقرة", "آل عمران", "النساء", "المائدة", "الأنعام", "الأعراف",
        "الأنفال", "التوبة", "يونس", "هود", "يوسف", "الرعد", "إبراهيم", "الحجر",
        "النحل", "الإسراء", "الكهف", "مريم", "طه", "الأنبياء", "الحج", "المؤمنون",
        "النور", "الفرقان", "الشعراء", "النمل", "القصص", "العنكبوت", "الروم",
        "لقمان", "السجدة", "الأحزاب", "سبأ", "فاطر", "يس", "الصافات", "ص", "الزمر",
        "غافر", "فصلت", "الشورى", "الزخرف", "الدخان", "الجاثية", "الأحقاف", "محمد",
        "الفتح", "الحجرات", "ق", "الذاريات", "الطور", "النجم", "القمر", "الرحمن",
        "الواقعة", "الحديد", "المجادلة", "الحشر", "الممتحنة", "الصف", "الجمعة",
        "المنافقون", "التغابن", "الطلاق", "التحريم", "الملك", "القلم", "الحاقة",
        "المعارج", "نوح", "الجن", "المزمل", "المدثر", "القيامة", "الإنسان",
        "المرسلات", "النبأ", "النازعات", "عبس", "التكوير", "الانفطار", "المطففين",
        "الانشقاق", "البروج", "الطارق", "الأعلى", "الغاشية", "الفجر", "البلد",
        "الشمس", "الليل", "الضحى", "الشرح", "التين", "العلق", "القدر", "البينة",
        "الزلزلة", "العاديات", "القارعة", "التكاثر", "العصر", "الهمزة", "الفيل",
        "قريش", "الماعون", "الكوثر", "الكافرون", "النصر", "المسد", "الإخلاص",
        "الفلق", "الناس"
    };

    public static synchronized QuranRepo get(Context c) {
        if (instance == null) {
            instance = new QuranRepo();
            Context app = c.getApplicationContext();
            instance.loadText(app);
            instance.loadMeta(app);
        }
        return instance;
    }

    private void loadText(Context c) {
        try (InputStream in = c.getAssets().open("quran.txt");
             BufferedReader r = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
            String line;
            while ((line = r.readLine()) != null) {
                line = line.replace("\uFEFF", "").trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int p1 = line.indexOf('|');
                int p2 = line.indexOf('|', p1 + 1);
                if (p1 < 0 || p2 < 0) continue;
                try {
                    int s = Integer.parseInt(line.substring(0, p1));
                    int a = Integer.parseInt(line.substring(p1 + 1, p2));
                    String t = line.substring(p2 + 1);
                    if (s < 1 || s > 114) continue;
                    if (suraCount[s] == 0) suraStart[s] = texts.size();
                    suraCount[s]++;
                    suras.add(s);
                    ayas.add(a);
                    texts.add(t);
                } catch (NumberFormatException e) {
                    // skip bad line
                }
            }
        } catch (Exception e) {
            // file missing
        }
    }

    private int globalIndex(int s, int a) {
        if (s < 1 || s > 114 || suraCount[s] == 0) return -1;
        if (a < 1 || a > suraCount[s]) return -1;
        return suraStart[s] + a - 1;
    }

    private void loadMeta(Context c) {
        ArrayList<Integer> juz = new ArrayList<>();
        ArrayList<Integer> page = new ArrayList<>();
        try (InputStream in = c.getAssets().open("quran-data.xml")) {
            XmlPullParser p = Xml.newPullParser();
            p.setInput(in, "UTF-8");
            int ev = p.getEventType();
            while (ev != XmlPullParser.END_DOCUMENT) {
                if (ev == XmlPullParser.START_TAG) {
                    String n = p.getName();
                    boolean isJuz = "juz".equals(n);
                    boolean isPage = "page".equals(n);
                    if (isJuz || isPage) {
                        try {
                            int s = Integer.parseInt(p.getAttributeValue(null, "sura"));
                            int a = Integer.parseInt(p.getAttributeValue(null, "aya"));
                            int g = globalIndex(s, a);
                            if (g >= 0) {
                                if (isJuz) juz.add(g); else page.add(g);
                            }
                        } catch (Exception e) {
                            // skip bad entry
                        }
                    }
                }
                ev = p.next();
            }
        } catch (Exception e) {
            // metadata missing
        }
        juzStarts = toArray(juz);
        pageStarts = toArray(page);
    }

    private static int[] toArray(ArrayList<Integer> list) {
        int[] arr = new int[list.size()];
        for (int i = 0; i < arr.length; i++) arr[i] = list.get(i);
        return arr;
    }

    private static int find(int[] starts, int idx) {
        int pos = Arrays.binarySearch(starts, idx);
        if (pos >= 0) return pos + 1;
        return Math.max(1, -pos - 1);
    }

    public int size() { return texts.size(); }

    public String header(int i) {
        int s = suras.get(i);
        return NAMES[s - 1] + "  ·  " + arabicDigits(ayas.get(i))
            + " / " + arabicDigits(suraCount[s]);
    }

    public String partLine(int i) {
        if (juzStarts.length == 0 || pageStarts.length == 0) return "";
        return "الجزء " + arabicDigits(find(juzStarts, i))
            + "  ·  الصفحة " + arabicDigits(find(pageStarts, i));
    }

    public static String arabicDigits(int n) {
        StringBuilder sb = new StringBuilder();
        for (char ch : String.valueOf(n).toCharArray()) {
            sb.append((char) ('\u0660' + (ch - '0')));
        }
        return sb.toString();
    }
}
