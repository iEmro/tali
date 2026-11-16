package com.nasser.tali;

import android.content.Context;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class QuranRepo {
    private static QuranRepo instance;
    public final ArrayList<String> texts = new ArrayList<>();
    public final ArrayList<Integer> suras = new ArrayList<>();
    public final ArrayList<Integer> ayas = new ArrayList<>();

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
            instance.load(c.getApplicationContext());
        }
        return instance;
    }

    private void load(Context c) {
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

    public int size() { return texts.size(); }

    public String header(int i) {
        return NAMES[suras.get(i) - 1] + "  ·  " + arabicDigits(ayas.get(i));
    }

    public static String arabicDigits(int n) {
        StringBuilder sb = new StringBuilder();
        for (char ch : String.valueOf(n).toCharArray()) {
            sb.append((char) ('\u0660' + (ch - '0')));
        }
        return sb.toString();
    }
}
