package com.usman.miqaat.data

import android.content.Context

/**
 * A short, hand-checked set of ṣaḥīḥ narrations. Numbers follow sunnah.com
 * (Ṣaḥīḥ al-Bukhārī / Ṣaḥīḥ Muslim in-book numbering). Nothing here is AI-written;
 * edit this file to add, remove or correct entries.
 */
data class Hadith(
    val id: Int,
    val arabic: String,
    val english: String,
    val narrator: String,
    val source: String
)

object Duas {
    /** Said after the azaan. Ṣaḥīḥ al-Bukhārī 614 (Jābir ibn ʿAbdillāh). */
    const val AFTER_AZAAN_AR =
        "اللَّهُمَّ رَبَّ هَذِهِ الدَّعْوَةِ التَّامَّةِ، وَالصَّلاَةِ الْقَائِمَةِ، آتِ مُحَمَّدًا الْوَسِيلَةَ وَالْفَضِيلَةَ، وَابْعَثْهُ مَقَامًا مَحْمُودًا الَّذِي وَعَدْتَهُ"
    const val AFTER_AZAAN_EN =
        "O Allah, Lord of this perfect call and of the prayer about to be established, grant Muhammad al-wasīlah and al-faḍīlah, and raise him to the praised station You have promised him."
    const val AFTER_AZAAN_SRC = "Ṣaḥīḥ al-Bukhārī 614"
    const val AFTER_AZAAN_NOTE = "Whoever says this after the azaan, my intercession becomes due for him"
}

object HadithLibrary {

    val all: List<Hadith> = listOf(
        Hadith(1, "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى",
            "Actions are judged only by intentions, and every person will have only what they intended.",
            "ʿUmar ibn al-Khaṭṭāb", "Ṣaḥīḥ al-Bukhārī 1 · Ṣaḥīḥ Muslim 1907"),
        Hadith(2, "لاَ يُؤْمِنُ أَحَدُكُمْ حَتَّى يُحِبَّ لأَخِيهِ مَا يُحِبُّ لِنَفْسِهِ",
            "None of you truly believes until he loves for his brother what he loves for himself.",
            "Anas ibn Mālik", "Ṣaḥīḥ al-Bukhārī 13 · Ṣaḥīḥ Muslim 45"),
        Hadith(3, "مَنْ كَانَ يُؤْمِنُ بِاللَّهِ وَالْيَوْمِ الآخِرِ فَلْيَقُلْ خَيْرًا أَوْ لِيَصْمُتْ",
            "Whoever believes in Allah and the Last Day, let him speak good or remain silent.",
            "Abū Hurayrah", "Ṣaḥīḥ al-Bukhārī 6018 · Ṣaḥīḥ Muslim 47"),
        Hadith(4, "خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ",
            "The best of you are those who learn the Qur'an and teach it.",
            "ʿUthmān ibn ʿAffān", "Ṣaḥīḥ al-Bukhārī 5027"),
        Hadith(5, "الْمُسْلِمُ مَنْ سَلِمَ الْمُسْلِمُونَ مِنْ لِسَانِهِ وَيَدِهِ",
            "The Muslim is the one from whose tongue and hand other Muslims are safe.",
            "ʿAbdullāh ibn ʿAmr", "Ṣaḥīḥ al-Bukhārī 10"),
        Hadith(6, "لَيْسَ الشَّدِيدُ بِالصُّرَعَةِ، إِنَّمَا الشَّدِيدُ الَّذِي يَمْلِكُ نَفْسَهُ عِنْدَ الْغَضَبِ",
            "The strong one is not the one who overcomes others in wrestling; the strong one is the one who controls himself when angry.",
            "Abū Hurayrah", "Ṣaḥīḥ al-Bukhārī 6114 · Ṣaḥīḥ Muslim 2609"),
        Hadith(7, "أَنَّ رَجُلاً قَالَ لِلنَّبِيِّ ﷺ: أَوْصِنِي. قَالَ: لاَ تَغْضَبْ. فَرَدَّدَ مِرَارًا، قَالَ: لاَ تَغْضَبْ",
            "A man said to the Prophet ﷺ: \"Advise me.\" He said: \"Do not become angry.\" The man repeated his request several times, and he said: \"Do not become angry.\"",
            "Abū Hurayrah", "Ṣaḥīḥ al-Bukhārī 6116"),
        Hadith(8, "إِنَّ اللَّهَ لاَ يَنْظُرُ إِلَى صُوَرِكُمْ وَأَمْوَالِكُمْ، وَلَكِنْ يَنْظُرُ إِلَى قُلُوبِكُمْ وَأَعْمَالِكُمْ",
            "Allah does not look at your appearance or your wealth, but He looks at your hearts and your deeds.",
            "Abū Hurayrah", "Ṣaḥīḥ Muslim 2564"),
        Hadith(9, "وَاللَّهُ فِي عَوْنِ الْعَبْدِ مَا كَانَ الْعَبْدُ فِي عَوْنِ أَخِيهِ",
            "Allah is helping the servant as long as the servant is helping his brother.",
            "Abū Hurayrah", "Ṣaḥīḥ Muslim 2699"),
        Hadith(10, "مَنْ سَلَكَ طَرِيقًا يَلْتَمِسُ فِيهِ عِلْمًا سَهَّلَ اللَّهُ لَهُ بِهِ طَرِيقًا إِلَى الْجَنَّةِ",
            "Whoever travels a path in search of knowledge, Allah makes easy for him a path to Paradise.",
            "Abū Hurayrah", "Ṣaḥīḥ Muslim 2699"),
        Hadith(11, "أَحَبُّ الأَعْمَالِ إِلَى اللَّهِ أَدْوَمُهَا وَإِنْ قَلَّ",
            "The most beloved deeds to Allah are those done consistently, even if they are small.",
            "ʿĀʾishah", "Ṣaḥīḥ al-Bukhārī 6464 · Ṣaḥīḥ Muslim 783"),
        Hadith(12, "نِعْمَتَانِ مَغْبُونٌ فِيهِمَا كَثِيرٌ مِنَ النَّاسِ: الصِّحَّةُ وَالْفَرَاغُ",
            "There are two blessings in which many people are cheated: health and free time.",
            "Ibn ʿAbbās", "Ṣaḥīḥ al-Bukhārī 6412"),
        Hadith(13, "مَا نَقَصَتْ صَدَقَةٌ مِنْ مَالٍ، وَمَا زَادَ اللَّهُ عَبْدًا بِعَفْوٍ إِلاَّ عِزًّا، وَمَا تَوَاضَعَ أَحَدٌ لِلَّهِ إِلاَّ رَفَعَهُ اللَّهُ",
            "Charity does not decrease wealth; Allah increases a servant in honour through forgiveness; and no one humbles himself for Allah except that Allah raises him.",
            "Abū Hurayrah", "Ṣaḥīḥ Muslim 2588"),
        Hadith(14, "الْمُسْلِمُ أَخُو الْمُسْلِمِ، لاَ يَظْلِمُهُ وَلاَ يُسْلِمُهُ، وَمَنْ كَانَ فِي حَاجَةِ أَخِيهِ كَانَ اللَّهُ فِي حَاجَتِهِ",
            "The Muslim is the brother of the Muslim: he does not wrong him nor abandon him. Whoever meets the need of his brother, Allah will meet his need.",
            "ʿAbdullāh ibn ʿUmar", "Ṣaḥīḥ al-Bukhārī 2442 · Ṣaḥīḥ Muslim 2580"),
        Hadith(15, "مَثَلُ الْمُؤْمِنِينَ فِي تَوَادِّهِمْ وَتَرَاحُمِهِمْ وَتَعَاطُفِهِمْ مَثَلُ الْجَسَدِ، إِذَا اشْتَكَى مِنْهُ عُضْوٌ تَدَاعَى لَهُ سَائِرُ الْجَسَدِ بِالسَّهَرِ وَالْحُمَّى",
            "The believers in their mutual love, mercy and compassion are like one body: when one limb aches, the whole body responds with sleeplessness and fever.",
            "an-Nuʿmān ibn Bashīr", "Ṣaḥīḥ al-Bukhārī 6011 · Ṣaḥīḥ Muslim 2586"),
        Hadith(16, "الْبِرُّ حُسْنُ الْخُلُقِ، وَالإِثْمُ مَا حَاكَ فِي صَدْرِكَ وَكَرِهْتَ أَنْ يَطَّلِعَ عَلَيْهِ النَّاسُ",
            "Righteousness is good character, and sin is what wavers in your heart and you would dislike people to know of.",
            "an-Nawwās ibn Samʿān", "Ṣaḥīḥ Muslim 2553"),
        Hadith(17, "إِنَّ الدِّينَ يُسْرٌ، وَلَنْ يُشَادَّ الدِّينَ أَحَدٌ إِلاَّ غَلَبَهُ، فَسَدِّدُوا وَقَارِبُوا وَأَبْشِرُوا",
            "Indeed this religion is ease, and no one makes the religion hard on himself except that it overwhelms him. So be moderate, come as close as you can, and give glad tidings.",
            "Abū Hurayrah", "Ṣaḥīḥ al-Bukhārī 39"),
        Hadith(18, "لاَ تَحْقِرَنَّ مِنَ الْمَعْرُوفِ شَيْئًا، وَلَوْ أَنْ تَلْقَى أَخَاكَ بِوَجْهٍ طَلْقٍ",
            "Do not belittle any good deed, even meeting your brother with a cheerful face.",
            "Abū Dharr", "Ṣaḥīḥ Muslim 2626"),
        Hadith(19, "مَنْ أَحَقُّ النَّاسِ بِحُسْنِ صَحَابَتِي؟ قَالَ: أُمُّكَ، قَالَ: ثُمَّ مَنْ؟ قَالَ: أُمُّكَ، قَالَ: ثُمَّ مَنْ؟ قَالَ: أُمُّكَ، قَالَ: ثُمَّ مَنْ؟ قَالَ: أَبُوكَ",
            "A man asked: \"Who among people most deserves my good companionship?\" He ﷺ said: \"Your mother.\" \"Then who?\" \"Your mother.\" \"Then who?\" \"Your mother.\" \"Then who?\" \"Your father.\"",
            "Abū Hurayrah", "Ṣaḥīḥ al-Bukhārī 5971 · Ṣaḥīḥ Muslim 2548"),
        Hadith(20, "مَثَلُ الَّذِي يَذْكُرُ رَبَّهُ وَالَّذِي لاَ يَذْكُرُ رَبَّهُ مَثَلُ الْحَيِّ وَالْمَيِّتِ",
            "The example of the one who remembers his Lord and the one who does not is like the living and the dead.",
            "Abū Mūsā al-Ashʿarī", "Ṣaḥīḥ al-Bukhārī 6407"),
        Hadith(21, "كُلُّ سُلاَمَى مِنَ النَّاسِ عَلَيْهِ صَدَقَةٌ كُلَّ يَوْمٍ تَطْلُعُ فِيهِ الشَّمْسُ، يَعْدِلُ بَيْنَ الاِثْنَيْنِ صَدَقَةٌ، وَيُعِينُ الرَّجُلَ عَلَى دَابَّتِهِ صَدَقَةٌ، وَالْكَلِمَةُ الطَّيِّبَةُ صَدَقَةٌ",
            "Every joint of a person owes a charity each day the sun rises: to judge justly between two people is charity, to help a man onto his mount is charity, and a good word is charity.",
            "Abū Hurayrah", "Ṣaḥīḥ al-Bukhārī 2989 · Ṣaḥīḥ Muslim 1009"),
        Hadith(22, "عَلَيْكُمْ بِالصِّدْقِ، فَإِنَّ الصِّدْقَ يَهْدِي إِلَى الْبِرِّ، وَإِنَّ الْبِرَّ يَهْدِي إِلَى الْجَنَّةِ",
            "Hold fast to truthfulness, for truthfulness leads to righteousness, and righteousness leads to Paradise.",
            "ʿAbdullāh ibn Masʿūd", "Ṣaḥīḥ al-Bukhārī 6094 · Ṣaḥīḥ Muslim 2607"),
        Hadith(23, "كُنْ فِي الدُّنْيَا كَأَنَّكَ غَرِيبٌ أَوْ عَابِرُ سَبِيلٍ",
            "Be in this world as if you were a stranger or a traveller passing through.",
            "ʿAbdullāh ibn ʿUmar", "Ṣaḥīḥ al-Bukhārī 6416"),
        Hadith(24, "عَجَبًا لأَمْرِ الْمُؤْمِنِ، إِنَّ أَمْرَهُ كُلَّهُ خَيْرٌ، وَلَيْسَ ذَاكَ لأَحَدٍ إِلاَّ لِلْمُؤْمِنِ، إِنْ أَصَابَتْهُ سَرَّاءُ شَكَرَ فَكَانَ خَيْرًا لَهُ، وَإِنْ أَصَابَتْهُ ضَرَّاءُ صَبَرَ فَكَانَ خَيْرًا لَهُ",
            "How wonderful is the affair of the believer, for all of it is good, and that is for no one but the believer: if ease comes to him he is grateful, and that is good for him; and if hardship comes to him he is patient, and that is good for him.",
            "Ṣuhayb ar-Rūmī", "Ṣaḥīḥ Muslim 2999"),
        Hadith(25, "لاَ تَدْخُلُونَ الْجَنَّةَ حَتَّى تُؤْمِنُوا، وَلاَ تُؤْمِنُوا حَتَّى تَحَابُّوا، أَوَلاَ أَدُلُّكُمْ عَلَى شَيْءٍ إِذَا فَعَلْتُمُوهُ تَحَابَبْتُمْ؟ أَفْشُوا السَّلاَمَ بَيْنَكُمْ",
            "You will not enter Paradise until you believe, and you will not believe until you love one another. Shall I not tell you of something which, if you do it, you will love one another? Spread the greeting of peace among yourselves.",
            "Abū Hurayrah", "Ṣaḥīḥ Muslim 54"),
        Hadith(26, "لاَ يَدْخُلُ الْجَنَّةَ مَنْ كَانَ فِي قَلْبِهِ مِثْقَالُ ذَرَّةٍ مِنْ كِبْرٍ … الْكِبْرُ بَطَرُ الْحَقِّ وَغَمْطُ النَّاسِ",
            "No one who has an atom's weight of pride in his heart will enter Paradise. Pride is rejecting the truth and looking down on people.",
            "ʿAbdullāh ibn Masʿūd", "Ṣaḥīḥ Muslim 91"),
        Hadith(27, "إِنَّ مِنْ خِيَارِكُمْ أَحْسَنَكُمْ أَخْلاَقًا",
            "The best among you are those with the best character.",
            "ʿAbdullāh ibn ʿAmr", "Ṣaḥīḥ al-Bukhārī 3559 · Ṣaḥīḥ Muslim 2321"),
        Hadith(28, "أَنَا عِنْدَ ظَنِّ عَبْدِي بِي، وَأَنَا مَعَهُ إِذَا ذَكَرَنِي",
            "Allah says: \"I am as My servant thinks of Me, and I am with him when he remembers Me.\"",
            "Abū Hurayrah", "Ṣaḥīḥ al-Bukhārī 7405 · Ṣaḥīḥ Muslim 2675"),
        Hadith(29, "كُلُّ مَعْرُوفٍ صَدَقَةٌ",
            "Every act of kindness is charity.",
            "Jābir ibn ʿAbdillāh", "Ṣaḥīḥ al-Bukhārī 6021"),
        Hadith(30, "دَعْوَةُ الْمَرْءِ الْمُسْلِمِ لأَخِيهِ بِظَهْرِ الْغَيْبِ مُسْتَجَابَةٌ، عِنْدَ رَأْسِهِ مَلَكٌ مُوَكَّلٌ كُلَّمَا دَعَا لأَخِيهِ بِخَيْرٍ قَالَ الْمَلَكُ الْمُوَكَّلُ بِهِ: آمِينَ وَلَكَ بِمِثْلٍ",
            "The supplication of a Muslim for his brother in his absence is answered. At his head is an angel who says, whenever he prays for good for his brother: \"Āmīn, and for you the same.\"",
            "Abū ad-Dardāʾ", "Ṣaḥīḥ Muslim 2733"),
        Hadith(31, "الطُّهُورُ شَطْرُ الإِيمَانِ، وَالْحَمْدُ لِلَّهِ تَمْلأُ الْمِيزَانَ",
            "Purification is half of faith, and \"al-ḥamdu lillāh\" fills the scale.",
            "Abū Mālik al-Ashʿarī", "Ṣaḥīḥ Muslim 223"),
        Hadith(32, "الْحَلاَلُ بَيِّنٌ وَالْحَرَامُ بَيِّنٌ، وَبَيْنَهُمَا مُشَبَّهَاتٌ … فَمَنِ اتَّقَى الْمُشَبَّهَاتِ اسْتَبْرَأَ لِدِينِهِ وَعِرْضِهِ",
            "The lawful is clear and the unlawful is clear, and between them are doubtful matters. Whoever guards against the doubtful has protected his religion and his honour.",
            "an-Nuʿmān ibn Bashīr", "Ṣaḥīḥ al-Bukhārī 52 · Ṣaḥīḥ Muslim 1599"),
        Hadith(33, "مَنْ صَلَّى الْبَرْدَيْنِ دَخَلَ الْجَنَّةَ",
            "Whoever prays the two cool prayers (Fajr and ʿAṣr) will enter Paradise.",
            "Abū Mūsā al-Ashʿarī", "Ṣaḥīḥ al-Bukhārī 574 · Ṣaḥīḥ Muslim 635"),
        Hadith(34, "الصَّلَوَاتُ الْخَمْسُ، وَالْجُمُعَةُ إِلَى الْجُمُعَةِ، كَفَّارَةٌ لِمَا بَيْنَهُنَّ مَا لَمْ تُغْشَ الْكَبَائِرُ",
            "The five daily prayers, and Friday to Friday, are an expiation for what is between them, so long as major sins are avoided.",
            "Abū Hurayrah", "Ṣaḥīḥ Muslim 233"),
        Hadith(35, "أَقْرَبُ مَا يَكُونُ الْعَبْدُ مِنْ رَبِّهِ وَهُوَ سَاجِدٌ، فَأَكْثِرُوا الدُّعَاءَ",
            "The closest a servant is to his Lord is while in prostration, so make much supplication then.",
            "Abū Hurayrah", "Ṣaḥīḥ Muslim 482"),
        Hadith(36, "مَنْ غَدَا إِلَى الْمَسْجِدِ أَوْ رَاحَ أَعَدَّ اللَّهُ لَهُ فِي الْجَنَّةِ نُزُلاً كُلَّمَا غَدَا أَوْ رَاحَ",
            "Whoever goes to the mosque in the morning or evening, Allah prepares for him a place in Paradise each time he goes.",
            "Abū Hurayrah", "Ṣaḥīḥ al-Bukhārī 662 · Ṣaḥīḥ Muslim 669")
    )

    /** One per azaan, no repeats until the whole set has been shown. */
    /** Narrations that are specifically about Fajr and ʿAṣr: they are only offered after those two prayers, so the "hadith after Dhuhr" heading is never contradicted by the text. */
    /** Entries whose own text is a narrative about the Prophet ﷺ (ids 7 and 19), not his direct words. */
    val narrativeFraming = setOf(7, 19)

    private val fajrAsrOnly = setOf(33)

    fun eligible(h: Hadith, prayer: Prayer?): Boolean = h.id !in fajrAsrOnly || prayer == null || prayer == Prayer.FAJR || prayer == Prayer.ASR

    fun next(ctx: Context, prayer: Prayer? = null): Hadith {
        val prefs = ctx.getSharedPreferences("miqaat_hadith", Context.MODE_PRIVATE)
        val seen = prefs.getString("seen", "")!!.split(',').mapNotNull { it.toIntOrNull() }.toMutableList()
        val allowed = all.filter { eligible(it, prayer) }
        var pool = allowed.filter { it.id !in seen }
        if (pool.isEmpty()) { seen.clear(); pool = allowed }
        val pick = pool.random()
        seen += pick.id
        prefs.edit().putString("seen", seen.joinToString(",")).putInt("last", pick.id).apply()
        return pick
    }

    fun byId(id: Int) = all.firstOrNull { it.id == id } ?: all.first()
}
