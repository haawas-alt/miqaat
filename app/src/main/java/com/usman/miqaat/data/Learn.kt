package com.usman.miqaat.data

import android.content.Context

/**
 * Learn Salah: the structure of a prayer (rakʿahs → actions), built from the reviewed words in [Adhkar.salah].
 * Content is the same reviewed text; this file only arranges it into Fajr (2), Maghrib (3) and the 4-rakʿah prayers.
 * Pending qualified review: ISLAMIC_REVIEW_PACK.md § J7.
 */
object Learn {
    enum class Posture(val label: String, val describe: String) {
        STANDING("Standing", "Standing upright, feet a little apart, gaze toward the place of prostration"),
        BOWING("Bowing · rukūʿ", "Bent forward from the hips, back level, palms on the knees"),
        RISING("Rising", "Standing upright again after bowing, arms at the sides"),
        PROSTRATING("Prostration · sujūd", "Forehead and nose on the ground, palms flat beside the head, knees and toes on the ground"),
        SITTING("Sitting", "Seated on the folded legs, hands resting on the thighs"),
        SALAM("Salām", "Seated, turning the head to the right shoulder, then to the left")
    }

    enum class Lesson(val title: String, val rakat: Int, val subtitle: String) {
        FAJR("Fajr", 2, "2 rakʿahs · dawn"),
        MAGHRIB("Maghrib", 3, "3 rakʿahs · sunset"),
        FOUR("Dhuhr · ʿAsr · Isha", 4, "4 rakʿahs")
    }

    data class Action(val rakah: Int, val step: Adhkar.Step, val posture: Posture, val cue: String)

    private val S = Adhkar.salah   // 0 takbīr, 1 opening, 2 taʿawwudh, 3 Fātiḥah, 4 sūrah, 5 rukūʿ, 6 rising, 7 sujūd, 8 sitting, 9 tashahhud, 10 ṣalawāt, 11 salām

    /** The full sequence of actions for a lesson, rakʿah by rakʿah. */
    fun actions(l: Lesson): List<Action> {
        val out = mutableListOf<Action>()
        for (r in 1..l.rakat) {
            val first = r == 1; val last = r == l.rakat; val recitesSurah = r <= 2
            if (first) {
                out += Action(r, S[0], Posture.STANDING, "Face the Qibla, intend the prayer, raise the hands")
                out += Action(r, S[1], Posture.STANDING, "Quietly, hands folded")
                out += Action(r, S[2], Posture.STANDING, "Quietly, before al-Fātiḥah")
            } else out += Action(r, S[0].copy(position = "Standing · rakʿah $r", note = "Rise saying Allāhu akbar and stand upright before reciting."), Posture.STANDING, "Stand for rakʿah $r")
            out += Action(r, S[3], Posture.STANDING, "Every rakʿah; say āmīn at the end")
            if (recitesSurah) out += Action(r, S[4], Posture.STANDING, "A sūrah after al-Fātiḥah in the first two rakʿahs")
            out += Action(r, S[5], Posture.BOWING, "Say Allāhu akbar going down; three times in rukūʿ")
            out += Action(r, S[6], Posture.RISING, "Stand fully upright")
            out += Action(r, S[7], Posture.PROSTRATING, "First prostration · three times")
            out += Action(r, S[8], Posture.SITTING, "Sit calmly between the two prostrations")
            out += Action(r, S[7].copy(position = "Second prostration", note = "Exactly like the first. Ṣaḥīḥ al-Bukhārī 812."), Posture.PROSTRATING, "Second prostration · three times")
            if (r == 2 && !last) out += Action(r, S[9], Posture.SITTING, "First sitting: tashahhud only, then rise for rakʿah 3")
            if (last) {
                out += Action(r, S[9], Posture.SITTING, "Final sitting")
                out += Action(r, S[10], Posture.SITTING, "After the tashahhud")
                out += Action(r, S[11], Posture.SALAM, "Right, then left — the prayer is complete")
            }
        }
        return out
    }

    /** "Practise the words": the twelve reviewed texts on their own. */
    val words: List<Adhkar.Step> get() = S

    // ---- progress, on device only, no scores or streaks
    private const val PREFS = "miqaat_learn"
    data class Progress(val lesson: Lesson?, val index: Int, val completed: Set<Lesson>)
    fun progress(ctx: Context): Progress {
        val p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val l = p.getString("lesson", null)?.let { runCatching { Lesson.valueOf(it) }.getOrNull() }
        val done = p.getStringSet("done", emptySet()).orEmpty().mapNotNull { runCatching { Lesson.valueOf(it) }.getOrNull() }.toSet()
        return Progress(l, p.getInt("index", 0), done)
    }
    fun save(ctx: Context, lesson: Lesson, index: Int) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString("lesson", lesson.name).putInt("index", index).apply()
    }
    fun complete(ctx: Context, lesson: Lesson) {
        val p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        p.edit().putStringSet("done", p.getStringSet("done", emptySet()).orEmpty() + lesson.name).remove("lesson").remove("index").apply()
    }
    fun clear(ctx: Context) { ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply() }
}
