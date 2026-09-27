package com.usman.miqaat.data

/**
 * Morning and evening adhkār. Every entry carries its source; gradings other than
 * Bukhārī/Muslim are stated. Edit this file to add or remove entries.
 */
data class Dhikr(
    val id: String,
    val title: String,
    val arabic: String,
    val english: String,
    val count: Int,
    val source: String,
    val morningOnly: Boolean = false,
    val eveningOnly: Boolean = false
)

object Adhkar {

    private const val AYAT_AL_KURSI =
        "اللَّهُ لاَ إِلَهَ إِلاَّ هُوَ الْحَيُّ الْقَيُّومُ، لاَ تَأْخُذُهُ سِنَةٌ وَلاَ نَوْمٌ، لَهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الأَرْضِ، مَنْ ذَا الَّذِي يَشْفَعُ عِنْدَهُ إِلاَّ بِإِذْنِهِ، يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ، وَلاَ يُحِيطُونَ بِشَيْءٍ مِنْ عِلْمِهِ إِلاَّ بِمَا شَاءَ، وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالأَرْضَ، وَلاَ يَئُودُهُ حِفْظُهُمَا، وَهُوَ الْعَلِيُّ الْعَظِيمُ"

    private const val IKHLAS = "قُلْ هُوَ اللَّهُ أَحَدٌ ۝ اللَّهُ الصَّمَدُ ۝ لَمْ يَلِدْ وَلَمْ يُولَدْ ۝ وَلَمْ يَكُنْ لَهُ كُفُوًا أَحَدٌ"
    private const val FALAQ = "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ ۝ مِنْ شَرِّ مَا خَلَقَ ۝ وَمِنْ شَرِّ غَاسِقٍ إِذَا وَقَبَ ۝ وَمِنْ شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ ۝ وَمِنْ شَرِّ حَاسِدٍ إِذَا حَسَدَ"
    private const val NAS = "قُلْ أَعُوذُ بِرَبِّ النَّاسِ ۝ مَلِكِ النَّاسِ ۝ إِلَهِ النَّاسِ ۝ مِنْ شَرِّ الْوَسْوَاسِ الْخَنَّاسِ ۝ الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ ۝ مِنَ الْجِنَّةِ وَالنَّاسِ"

    val all: List<Dhikr> = listOf(
        Dhikr("kursi", "Āyat al-Kursī", AYAT_AL_KURSI,
            "Allah – there is no god but He, the Ever-Living, the Sustainer of all. Neither drowsiness nor sleep overtakes Him. To Him belongs whatever is in the heavens and the earth… and He is the Most High, the Most Great.",
            1, "Qur'an 2:255 · recited morning and evening: an-Nasāʾī, al-Kubrā (ṣaḥīḥ)"),
        Dhikr("ikhlas", "Sūrat al-Ikhlāṣ", IKHLAS,
            "Say: He is Allah, the One. Allah, the Eternal Refuge. He neither begets nor is born, nor is there to Him any equivalent.",
            3, "Qur'an 112 · three times morning and evening: Abū Dāwūd 5082, at-Tirmidhī 3575 (ḥasan ṣaḥīḥ)"),
        Dhikr("falaq", "Sūrat al-Falaq", FALAQ,
            "Say: I seek refuge in the Lord of daybreak, from the evil of what He created, from the evil of darkness when it settles, from the evil of the blowers in knots, and from the evil of an envier when he envies.",
            3, "Qur'an 113 · Abū Dāwūd 5082, at-Tirmidhī 3575 (ḥasan ṣaḥīḥ)"),
        Dhikr("nas", "Sūrat an-Nās", NAS,
            "Say: I seek refuge in the Lord of mankind, the King of mankind, the God of mankind, from the evil of the retreating whisperer who whispers in the breasts of mankind, from among jinn and mankind.",
            3, "Qur'an 114 · Abū Dāwūd 5082, at-Tirmidhī 3575 (ḥasan ṣaḥīḥ)"),
        Dhikr("asbahna", "Aṣbaḥnā", "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هَذَا الْيَوْمِ وَخَيْرَ مَا بَعْدَهُ، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هَذَا الْيَوْمِ وَشَرِّ مَا بَعْدَهُ",
            "We have entered the morning and the dominion belongs to Allah. Praise be to Allah; there is no god but Allah alone, without partner. His is the dominion and His is the praise, and He is able to do all things. My Lord, I ask You for the good of this day and the good after it, and I seek refuge in You from the evil of this day and the evil after it.",
            1, "Ṣaḥīḥ Muslim 2723", morningOnly = true),
        Dhikr("amsayna", "Amsaynā", "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هَذِهِ اللَّيْلَةِ وَخَيْرَ مَا بَعْدَهَا، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هَذِهِ اللَّيْلَةِ وَشَرِّ مَا بَعْدَهَا",
            "We have entered the evening and the dominion belongs to Allah. Praise be to Allah; there is no god but Allah alone, without partner. His is the dominion and His is the praise, and He is able to do all things. My Lord, I ask You for the good of this night and the good after it, and I seek refuge in You from the evil of this night and the evil after it.",
            1, "Ṣaḥīḥ Muslim 2723", eveningOnly = true),
        Dhikr("sayyid", "Sayyid al-Istighfār", "اللَّهُمَّ أَنْتَ رَبِّي لاَ إِلَهَ إِلاَّ أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ لَكَ بِذَنْبِي فَاغْفِرْ لِي، فَإِنَّهُ لاَ يَغْفِرُ الذُّنُوبَ إِلاَّ أَنْتَ",
            "O Allah, You are my Lord; there is no god but You. You created me and I am Your servant, and I keep Your covenant and promise as far as I am able. I seek refuge in You from the evil I have done. I acknowledge Your favour upon me and I acknowledge my sin, so forgive me, for none forgives sins but You.",
            1, "Ṣaḥīḥ al-Bukhārī 6306"),
        Dhikr("bika", "Bika aṣbaḥnā", "اللَّهُمَّ بِكَ أَصْبَحْنَا، وَبِكَ أَمْسَيْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ، وَإِلَيْكَ النُّشُورُ",
            "O Allah, by You we enter the morning and by You we enter the evening; by You we live and by You we die, and to You is the resurrection.",
            1, "at-Tirmidhī 3391 (ḥasan ṣaḥīḥ)", morningOnly = true),
        Dhikr("bika_e", "Bika amsaynā", "اللَّهُمَّ بِكَ أَمْسَيْنَا، وَبِكَ أَصْبَحْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ، وَإِلَيْكَ الْمَصِيرُ",
            "O Allah, by You we enter the evening and by You we enter the morning; by You we live and by You we die, and to You is the final return.",
            1, "at-Tirmidhī 3391 (ḥasan ṣaḥīḥ)", eveningOnly = true),
        Dhikr("bismillah", "Bismillāh alladhī", "بِسْمِ اللَّهِ الَّذِي لاَ يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الأَرْضِ وَلاَ فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            "In the name of Allah, with whose name nothing on earth or in the heavens can cause harm, and He is the All-Hearing, the All-Knowing.",
            3, "Abū Dāwūd 5088, at-Tirmidhī 3388 (ṣaḥīḥ)"),
        Dhikr("radeetu", "Raḍītu billāhi", "رَضِيتُ بِاللَّهِ رَبًّا، وَبِالإِسْلاَمِ دِينًا، وَبِمُحَمَّدٍ ﷺ نَبِيًّا",
            "I am pleased with Allah as Lord, with Islam as religion, and with Muhammad ﷺ as Prophet.",
            3, "Abū Dāwūd 5072, at-Tirmidhī 3389 (ḥasan)"),
        Dhikr("afini", "Allāhumma ʿāfinī", "اللَّهُمَّ عَافِنِي فِي بَدَنِي، اللَّهُمَّ عَافِنِي فِي سَمْعِي، اللَّهُمَّ عَافِنِي فِي بَصَرِي، لاَ إِلَهَ إِلاَّ أَنْتَ",
            "O Allah, grant my body health; O Allah, grant my hearing health; O Allah, grant my sight health. There is no god but You.",
            3, "Abū Dāwūd 5090 (ḥasan)"),
        Dhikr("audhu", "Aʿūdhu bi-kalimāt", "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
            "I seek refuge in the perfect words of Allah from the evil of what He has created.",
            3, "Ṣaḥīḥ Muslim 2709", eveningOnly = true),
        Dhikr("tahlil", "Lā ilāha illallāh…", "لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
            "There is no god but Allah alone, without partner. His is the dominion and His is the praise, and He is able to do all things.",
            100, "Ṣaḥīḥ al-Bukhārī 3293 · Ṣaḥīḥ Muslim 2691"),
        Dhikr("tasbih", "Subḥānallāhi wa bi-ḥamdih", "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
            "Glory be to Allah and praise be to Him.",
            100, "Ṣaḥīḥ Muslim 2692"),
        Dhikr("salawat", "Ṣalawāt", "اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ",
            "O Allah, send blessings and peace upon our Prophet Muhammad.",
            10, "Whoever sends one blessing upon me, Allah sends ten upon him: Ṣaḥīḥ Muslim 408")
    )

    fun morning() = all.filter { !it.eveningOnly }
    fun evening() = all.filter { !it.morningOnly }
}

object Ramadan {
    /** Dua at iftar. Abū Dāwūd 2357, graded ḥasan by al-Albānī. */
    const val IFTAR_AR = "ذَهَبَ الظَّمَأُ، وَابْتَلَّتِ الْعُرُوقُ، وَثَبَتَ الأَجْرُ إِنْ شَاءَ اللَّهُ"
    const val IFTAR_EN = "The thirst is gone, the veins are moistened, and the reward is confirmed, if Allah wills."
    const val IFTAR_SRC = "Abū Dāwūd 2357 (ḥasan)"
    /** Sūrat al-Kahf on Friday: al-Ḥākim 2/399 and al-Bayhaqī, graded ṣaḥīḥ by al-Albānī. Ṣalawāt on Friday: Abū Dāwūd 1047 (ṣaḥīḥ). */
    const val KAHF_NOTE = "Whoever reads Sūrat al-Kahf on Friday, a light shines for him between the two Fridays"
    const val KAHF_SRC = "al-Ḥākim · al-Bayhaqī (ṣaḥīḥ, al-Albānī)"
    const val SALAWAT_NOTE = "Increase your ṣalawāt upon me on Friday"
    const val SALAWAT_SRC = "Abū Dāwūd 1047 (ṣaḥīḥ)"
}
