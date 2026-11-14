package debeski.jurah.domain.schedule

/** The intervals people actually get prescribed; anything else is entered as custom hours. */
enum class IntervalPreset(val minutes: Int, val perDay: Int) {
    FOUR_HOURLY(240, 6),
    SIX_HOURLY(360, 4),
    EIGHT_HOURLY(480, 3),
    TWELVE_HOURLY(720, 2),
    DAILY(1440, 1);

    companion object {
        fun forMinutes(minutes: Int): IntervalPreset? = entries.firstOrNull { it.minutes == minutes }
    }
}
