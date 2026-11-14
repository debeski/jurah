package debeski.jurah.domain

import debeski.jurah.domain.model.Schedule
import debeski.jurah.domain.schedule.DoseCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DoseCalculatorTest {

    private val start = 1_700_000_000_000L
    private val eightHours = 8 * 60 * 60 * 1000L

    private fun schedule(
        intervalMinutes: Int = 480,
        durationDays: Int? = 7,
        indexBase: Int = 0,
    ) = Schedule(
        id = 1L,
        name = "Amoxicillin",
        startAtMillis = start,
        intervalMinutes = intervalMinutes,
        durationDays = durationDays,
        createdAtMillis = start,
        indexBase = indexBase,
    )

    @Test
    fun `three a day for a week is twenty one doses`() {
        assertEquals(21, DoseCalculator.totalDoses(schedule()))
    }

    @Test
    fun `once a day for five days is five doses`() {
        assertEquals(5, DoseCalculator.totalDoses(schedule(intervalMinutes = 1440, durationDays = 5)))
    }

    @Test
    fun `an indefinite course has no dose count`() {
        assertNull(DoseCalculator.totalDoses(schedule(durationDays = null)))
    }

    @Test
    fun `dose times step by the interval`() {
        val s = schedule()
        assertEquals(start, DoseCalculator.scheduledAt(s, 0))
        assertEquals(start + 3 * eightHours, DoseCalculator.scheduledAt(s, 3))
    }

    @Test
    fun `the last dose lands inside the course window`() {
        val s = schedule()
        assertEquals(start + 20 * eightHours, DoseCalculator.lastDoseAt(s))
        assertTrue(DoseCalculator.lastDoseAt(s)!! < start + 7 * 24 * 60 * 60 * 1000L)
    }

    @Test
    fun `before the start the first dose is next`() {
        assertEquals(0, DoseCalculator.nextIndexAt(schedule(), start - 60_000L))
    }

    @Test
    fun `a dose due exactly now is the next dose`() {
        assertEquals(2, DoseCalculator.nextIndexAt(schedule(), start + 2 * eightHours))
    }

    @Test
    fun `a dose one millisecond past is behind us`() {
        assertEquals(3, DoseCalculator.nextIndexAt(schedule(), start + 2 * eightHours + 1))
    }

    @Test
    fun `the course ends after its final dose`() {
        val s = schedule()
        assertEquals(20, DoseCalculator.nextIndexAt(s, start + 20 * eightHours))
        assertNull(DoseCalculator.nextIndexAt(s, start + 21 * eightHours))
        assertTrue(DoseCalculator.isFinished(s, start + 21 * eightHours))
        assertFalse(DoseCalculator.isFinished(s, start + 20 * eightHours))
    }

    @Test
    fun `an indefinite course never finishes`() {
        val s = schedule(durationDays = null)
        assertEquals(1000, DoseCalculator.nextIndexAt(s, start + 1000 * eightHours))
        assertFalse(DoseCalculator.isFinished(s, start + 100_000 * eightHours))
    }

    @Test
    fun `elapsed slots cover every dose whose time has arrived`() {
        val s = schedule()
        assertEquals(IntRange.EMPTY, DoseCalculator.elapsedIndices(s, start - 1))
        assertEquals(0..0, DoseCalculator.elapsedIndices(s, start))
        assertEquals(0..2, DoseCalculator.elapsedIndices(s, start + 2 * eightHours + 5))
    }

    @Test
    fun `elapsed slots never run past the end of a finite course`() {
        assertEquals(0..20, DoseCalculator.elapsedIndices(schedule(), start + 500 * eightHours))
    }

    // A re-timed course keeps its recorded doses by moving its grid past them, so the
    // index that lands on the start time is indexBase rather than zero.

    @Test
    fun `a re-anchored grid starts at its index base`() {
        val s = schedule(indexBase = 3)
        assertEquals(start, DoseCalculator.scheduledAt(s, 3))
        assertEquals(start + 2 * eightHours, DoseCalculator.scheduledAt(s, 5))
    }

    @Test
    fun `total doses count the ones recorded before the course was re-timed`() {
        assertEquals(21, DoseCalculator.gridDoses(schedule(indexBase = 3)))
        assertEquals(24, DoseCalculator.totalDoses(schedule(indexBase = 3)))
    }

    @Test
    fun `the last dose of a re-anchored course is still the end of its grid`() {
        val s = schedule(indexBase = 3)
        assertEquals(start + 20 * eightHours, DoseCalculator.lastDoseAt(s))
        assertEquals(DoseCalculator.lastDoseAt(schedule()), DoseCalculator.lastDoseAt(s))
    }

    @Test
    fun `the next dose of a re-anchored course is numbered from its base`() {
        val s = schedule(indexBase = 3)
        assertEquals(3, DoseCalculator.nextIndexAt(s, start - 60_000L))
        assertEquals(5, DoseCalculator.nextIndexAt(s, start + 2 * eightHours))
    }

    @Test
    fun `a re-anchored course ends after its own grid, not sooner`() {
        val s = schedule(indexBase = 3)
        assertEquals(23, DoseCalculator.nextIndexAt(s, start + 20 * eightHours))
        assertNull(DoseCalculator.nextIndexAt(s, start + 21 * eightHours))
    }

    @Test
    fun `elapsed slots of a re-anchored course never reach back before its base`() {
        val s = schedule(indexBase = 3)
        assertEquals(IntRange.EMPTY, DoseCalculator.elapsedIndices(s, start - 1))
        assertEquals(3..3, DoseCalculator.elapsedIndices(s, start))
        assertEquals(3..5, DoseCalculator.elapsedIndices(s, start + 2 * eightHours + 5))
        assertEquals(3..23, DoseCalculator.elapsedIndices(s, start + 500 * eightHours))
    }
}
