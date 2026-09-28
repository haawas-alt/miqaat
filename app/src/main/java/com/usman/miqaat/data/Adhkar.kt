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
            1, "Qur'an 2:255 · recited morning and evening: an-Nasāʾī, al-Kubrā 10729; al-Ḥākim 1:562 (ṣaḥīḥ per al-Albānī, Ṣaḥīḥ at-Targhīb 655)"),
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
            1, "Abū Dāwūd 5068 (ṣaḥīḥ) · at-Tirmidhī 3391", morningOnly = true),
        Dhikr("bika_e", "Bika amsaynā", "اللَّهُمَّ بِكَ أَمْسَيْنَا، وَبِكَ أَصْبَحْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ، وَإِلَيْكَ الْمَصِيرُ",
            "O Allah, by You we enter the evening and by You we enter the morning; by You we live and by You we die, and to You is the final return.",
            1, "Abū Dāwūd 5068 (ṣaḥīḥ) · at-Tirmidhī 3391", eveningOnly = true),
        Dhikr("bismillah", "Bismillāh alladhī", "بِسْمِ اللَّهِ الَّذِي لاَ يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الأَرْضِ وَلاَ فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            "In the name of Allah, with whose name nothing on earth or in the heavens can cause harm, and He is the All-Hearing, the All-Knowing.",
            3, "Abū Dāwūd 5088, at-Tirmidhī 3388 (ṣaḥīḥ)"),
        Dhikr("radeetu", "Raḍītu billāhi", "رَضِيتُ بِاللَّهِ رَبًّا، وَبِالإِسْلاَمِ دِينًا، وَبِمُحَمَّدٍ ﷺ نَبِيًّا",
            "I am pleased with Allah as Lord, with Islam as religion, and with Muhammad ﷺ as Prophet.",
            3, "Abū Dāwūd 5072, at-Tirmidhī 3389 · three times is in the narration · grading disputed: ḥasan gharīb (at-Tirmidhī), ḍaʿīf (al-Albānī), ḥasan (Ibn Bāz)"),
        Dhikr("afini", "Allāhumma ʿāfinī", "اللَّهُمَّ عَافِنِي فِي بَدَنِي، اللَّهُمَّ عَافِنِي فِي سَمْعِي، اللَّهُمَّ عَافِنِي فِي بَصَرِي، لاَ إِلَهَ إِلاَّ أَنْتَ",
            "O Allah, grant my body health; O Allah, grant my hearing health; O Allah, grant my sight health. There is no god but You.",
            3, "Abū Dāwūd 5090 (ḥasan)"),
        Dhikr("audhu", "Aʿūdhu bi-kalimāt", "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
            "I seek refuge in the perfect words of Allah from the evil of what He has created.",
            3, "Ṣaḥīḥ Muslim 2709 (the words) · at-Tirmidhī 3604 (three times in the evening, ḥasan)", eveningOnly = true),
        Dhikr("tahlil", "Lā ilāha illallāh…", "لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
            "There is no god but Allah alone, without partner. His is the dominion and His is the praise, and He is able to do all things.",
            100, "Ṣaḥīḥ al-Bukhārī 3293 · Ṣaḥīḥ Muslim 2691 · one hundred in a day, not a separate hundred each session"),
        Dhikr("tasbih", "Subḥānallāhi wa bi-ḥamdih", "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
            "Glory be to Allah and praise be to Him.",
            100, "Ṣaḥīḥ Muslim 2692"),
        Dhikr("salawat", "Ṣalawāt", "اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ",
            "O Allah, send blessings and peace upon our Prophet Muhammad.",
            10, "Whoever sends one blessing upon me, Allah sends ten upon him: Ṣaḥīḥ Muslim 408 · ten is the app's suggested count, not a prescribed number")
    )

    fun morning() = all.filter { !it.eveningOnly }
    fun evening() = all.filter { !it.morningOnly }

    /** Said after every obligatory prayer. */
    val postPrayer: List<Dhikr> = listOf(
        Dhikr("pp_istighfar", "Astaghfirullāh", "أَسْتَغْفِرُ اللَّهَ", "I seek the forgiveness of Allah.", 3, "Ṣaḥīḥ Muslim 591"),
        Dhikr("pp_salam", "Allāhumma anta s-salām", "اللَّهُمَّ أَنْتَ السَّلاَمُ وَمِنْكَ السَّلاَمُ، تَبَارَكْتَ يَا ذَا الْجَلاَلِ وَالإِكْرَامِ",
            "O Allah, You are Peace and from You is peace. Blessed are You, O Owner of majesty and honour.", 1, "Ṣaḥīḥ Muslim 591"),
        Dhikr("pp_tahlil", "Lā ilāha illallāh…", "لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، اللَّهُمَّ لاَ مَانِعَ لِمَا أَعْطَيْتَ، وَلاَ مُعْطِيَ لِمَا مَنَعْتَ، وَلاَ يَنْفَعُ ذَا الْجَدِّ مِنْكَ الْجَدُّ",
            "There is no god but Allah alone, without partner; His is the dominion and His is the praise, and He is able to do all things. O Allah, none can withhold what You give, none can give what You withhold, and no wealth avails its owner against You.", 1, "Ṣaḥīḥ al-Bukhārī 844 · Ṣaḥīḥ Muslim 593"),
        Dhikr("pp_tasbih", "Subḥānallāh", "سُبْحَانَ اللَّهِ", "Glory be to Allah.", 33, "Ṣaḥīḥ Muslim 596"),
        Dhikr("pp_hamd", "Al-ḥamdu lillāh", "الْحَمْدُ لِلَّهِ", "Praise be to Allah.", 33, "Ṣaḥīḥ Muslim 596"),
        Dhikr("pp_takbir", "Allāhu akbar", "اللَّهُ أَكْبَرُ", "Allah is the Greatest.", 34, "Ṣaḥīḥ Muslim 596 (33 · 33 · 34)"),
        Dhikr("pp_kursi", "Āyat al-Kursī", AYAT_AL_KURSI, "Allah – there is no god but He, the Ever-Living, the Sustainer of all…", 1, "Qur'an 2:255 · after each prayer: an-Nasāʾī, al-Kubrā 9848 (ṣaḥīḥ per al-Albānī, Ṣaḥīḥ al-Jāmiʿ 6464)"),
        Dhikr("pp_ikhlas", "Al-Ikhlāṣ, al-Falaq, an-Nās", IKHLAS + "\n\n" + FALAQ + "\n\n" + NAS, "The three Quls, once each (three times after Fajr and Maghrib).", 1, "After each prayer: Abū Dāwūd 1523, at-Tirmidhī 2903 (ṣaḥīḥ) · three times after Fajr and Maghrib: Abū Dāwūd 5082 (the morning/evening narration)")
    )

    /**
     * The words of the prayer, one position at a time, for Learn Salah (children and adult beginners).
     * One common form is shown. Where the four schools differ (hand position, raising the hands, what is
     * said in the opening, the finger in tashahhud) the note says so instead of presenting one as the only way.
     * Pending qualified review: see ISLAMIC_REVIEW_PACK.md § Learn Salah.
     */
    data class Step(val position: String, val emoji: String, val arabic: String, val transliteration: String, val meaning: String, val note: String)
    val salah: List<Step> = listOf(
        Step("Standing · start", "🧍", "اللَّهُ أَكْبَرُ", "Allāhu akbar", "Allah is the Greatest", "The opening takbīr. Raise both hands (to the shoulders or ears — both are reported), then place them on the chest or below the navel according to your school."),
        Step("Standing · opening", "🧍", "سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ، وَتَبَارَكَ اسْمُكَ، وَتَعَالَى جَدُّكَ، وَلاَ إِلَهَ غَيْرُكَ", "Subḥānaka llāhumma wa bi-ḥamdik, wa tabāraka smuk, wa taʿālā jadduk, wa lā ilāha ghayruk", "Glory be to You, O Allah, and praise; blessed is Your name, exalted is Your majesty, and there is no god but You", "Said quietly, only in the first rakʿah. This is one of several reported opening supplications. Abū Dāwūd 775, at-Tirmidhī 243."),
        Step("Standing · seek refuge", "🧍", "أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ ۝ بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "Aʿūdhu billāhi mina sh-shayṭāni r-rajīm · Bismillāhi r-raḥmāni r-raḥīm", "I seek refuge in Allah from the accursed devil · In the name of Allah, the Most Gracious, the Most Merciful", "Quietly, before al-Fātiḥah; whether the basmalah is said aloud differs by school."),
        Step("Standing · al-Fātiḥah", "🧍", "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ ۝ الرَّحْمَٰنِ الرَّحِيمِ ۝ مَالِكِ يَوْمِ الدِّينِ ۝ إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ ۝ اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ ۝ صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", "Al-ḥamdu lillāhi rabbi l-ʿālamīn · ar-raḥmāni r-raḥīm · māliki yawmi d-dīn · iyyāka naʿbudu wa iyyāka nastaʿīn · ihdina ṣ-ṣirāṭa l-mustaqīm · ṣirāṭa lladhīna anʿamta ʿalayhim ghayri l-maghḍūbi ʿalayhim wa la ḍ-ḍāllīn", "All praise is for Allah, Lord of the worlds… Guide us to the straight path", "Every rakʿah when praying alone; behind an imam, follow your school (the Ḥanafī view is that the imam's recitation suffices). Then āmīn (aloud or quietly, by school). Ṣaḥīḥ al-Bukhārī 756."),
        Step("Standing · a sūrah", "🧍", "قُلْ هُوَ اللَّهُ أَحَدٌ ۝ اللَّهُ الصَّمَدُ ۝ لَمْ يَلِدْ وَلَمْ يُولَدْ ۝ وَلَمْ يَكُنْ لَهُ كُفُوًا أَحَدٌ", "Qul huwa llāhu aḥad · Allāhu ṣ-ṣamad · lam yalid wa lam yūlad · wa lam yakun lahu kufuwan aḥad", "Say: He is Allah, the One…", "In the first two rakʿahs, any sūrah or verses after al-Fātiḥah. Sūrat al-Ikhlāṣ is a good one to learn first."),
        Step("Bowing · rukūʿ", "🙇", "اللَّهُ أَكْبَرُ · سُبْحَانَ رَبِّيَ الْعَظِيمِ", "Allāhu akbar · Subḥāna rabbiya l-ʿaẓīm (×3)", "Glory to my Lord, the Most Great", "Say Allāhu akbar while going down; back flat, hands on knees. The words: Ṣaḥīḥ Muslim 772; three times as the recommended minimum: Abū Dāwūd 886, at-Tirmidhī 261."),
        Step("Rising from rukūʿ", "🧍", "سَمِعَ اللَّهُ لِمَنْ حَمِدَهُ · رَبَّنَا وَلَكَ الْحَمْدُ", "Samiʿa llāhu liman ḥamidah · Rabbanā wa laka l-ḥamd", "Allah hears the one who praises Him · Our Lord, and to You is the praise", "Stand fully upright before going down. Ṣaḥīḥ al-Bukhārī 789."),
        Step("Prostration · sujūd", "🧎", "اللَّهُ أَكْبَرُ · سُبْحَانَ رَبِّيَ الأَعْلَى", "Allāhu akbar · Subḥāna rabbiya l-aʿlā (×3)", "Glory to my Lord, the Most High", "Forehead, nose, both palms, both knees and the toes touch the ground: seven bones (Ṣaḥīḥ al-Bukhārī 812). The words: Ṣaḥīḥ Muslim 772; three times as the recommended minimum: Abū Dāwūd 886."),
        Step("Sitting between the two sujūd", "🧎", "اللَّهُ أَكْبَرُ · رَبِّ اغْفِرْ لِي، رَبِّ اغْفِرْ لِي", "Allāhu akbar · Rabbi ghfir lī, rabbi ghfir lī", "My Lord, forgive me", "Sit up calmly; then a second prostration like the first. Abū Dāwūd 874."),
        Step("Sitting · tashahhud", "🧎", "التَّحِيَّاتُ لِلَّهِ وَالصَّلَوَاتُ وَالطَّيِّبَاتُ، السَّلاَمُ عَلَيْكَ أَيُّهَا النَّبِيُّ وَرَحْمَةُ اللَّهِ وَبَرَكَاتُهُ، السَّلاَمُ عَلَيْنَا وَعَلَى عِبَادِ اللَّهِ الصَّالِحِينَ، أَشْهَدُ أَنْ لاَ إِلَهَ إِلاَّ اللَّهُ وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ", "At-taḥiyyātu lillāhi wa ṣ-ṣalawātu wa ṭ-ṭayyibāt, as-salāmu ʿalayka ayyuha n-nabiyyu wa raḥmatu llāhi wa barakātuh, as-salāmu ʿalaynā wa ʿalā ʿibādi llāhi ṣ-ṣāliḥīn, ashhadu an lā ilāha illa llāh, wa ashhadu anna Muḥammadan ʿabduhu wa rasūluh", "All greetings, prayers and good things are for Allah. Peace be upon you, O Prophet… I bear witness that there is no god but Allah and that Muhammad is His servant and Messenger", "After every second rakʿah and at the end (text: Ṣaḥīḥ al-Bukhārī 831). Pointing with the index finger during the tashahhud: Ṣaḥīḥ Muslim 580; when it is raised and whether it moves differs by school."),
        Step("Sitting · ṣalāh upon the Prophet ﷺ", "🧎", "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ، كَمَا صَلَّيْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ، إِنَّكَ حَمِيدٌ مَجِيدٌ، اللَّهُمَّ بَارِكْ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ، كَمَا بَارَكْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ، إِنَّكَ حَمِيدٌ مَجِيدٌ", "Allāhumma ṣalli ʿalā Muḥammad wa ʿalā āli Muḥammad, kamā ṣallayta ʿalā Ibrāhīm wa ʿalā āli Ibrāhīm, innaka ḥamīdun majīd. Allāhumma bārik ʿalā Muḥammad wa ʿalā āli Muḥammad, kamā bārakta ʿalā Ibrāhīm wa ʿalā āli Ibrāhīm, innaka ḥamīdun majīd", "O Allah, send prayers upon Muhammad and the family of Muhammad, as You sent prayers upon Ibrāhīm…", "In the final sitting, after the tashahhud. Ṣaḥīḥ al-Bukhārī 3370."),
        Step("Finishing · salām", "🧎", "السَّلاَمُ عَلَيْكُمْ وَرَحْمَةُ اللَّهِ", "As-salāmu ʿalaykum wa raḥmatu llāh", "Peace and the mercy of Allah be upon you", "Turn the face to the right, then to the left, saying it each time. Ṣaḥīḥ Muslim 582; the words: Abū Dāwūd 996.")
    )
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
