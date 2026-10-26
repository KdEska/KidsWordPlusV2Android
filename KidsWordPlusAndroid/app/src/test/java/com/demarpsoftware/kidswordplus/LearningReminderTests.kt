package com.demarpsoftware.kidswordplus

import com.demarpsoftware.kidswordplus.notifications.LearningReminder
import java.time.*
import org.junit.Assert.*
import org.junit.Test

class LearningReminderTests {
    @Test fun independentEntriesPersistWithStableIdsAndFlags() {
        val entries = (0..100).map { LearningReminder(hour = it % 24, minute = it % 60, enabled = it % 3 != 0) }
        assertEquals(entries, LearningReminder.decode(LearningReminder.encode(entries)))
        assertEquals(emptyList<LearningReminder>(), LearningReminder.decode("[]"))
        val edited = entries.mapIndexed { i, item -> if (i == 5) item.copy(hour = 22) else item }
        assertEquals(entries[6], edited[6])
        assertEquals(entries[5].id, edited[5].id)
    }
    @Test fun invalidOrDuplicateRecordsAreRejectedWithoutDiscardingIndividualRows() {
        val entry = LearningReminder()
        assertNull(LearningReminder.decode("not JSON"))
        assertNull(LearningReminder.decode("[{\"id\":\"bad\",\"hour\":8,\"minute\":0,\"enabled\":true}]"))
        assertFalse(entry.copy(hour = 24).valid)
        assertFalse(entry.copy(minute = 60).valid)
        assertFalse(entry.copy(hour = -1).valid)
        val raw = LearningReminder.encode(listOf(entry))
        assertNull(LearningReminder.decode(raw.dropLast(1) + "," + raw.drop(1)))
    }
    @Test fun nextOccurrenceNeverRepeatsTodaysPastTime() {
        val zone = ZoneId.of("Europe/Istanbul")
        val entry = LearningReminder(hour = 18, minute = 30)
        fun time(day: Int, hour: Int, minute: Int) = ZonedDateTime.of(2026, 10, day, hour, minute, 0, 0, zone).toInstant()
        assertEquals(time(21,18,30), entry.nextOccurrence(time(21,18,29),zone))
        assertEquals(time(22,18,30), entry.nextOccurrence(time(21,18,30),zone))
        assertEquals(time(22,18,30), entry.nextOccurrence(time(21,20,0),zone))
    }
    @Test fun daylightSavingAndTimezoneChangesKeepLocalWallClockTimes() {
        val zone = ZoneId.of("America/New_York")
        val entry = LearningReminder(hour = 2, minute = 30)
        val gap = entry.nextOccurrence(Instant.parse("2026-03-08T05:00:00Z"),zone)
        assertEquals(Instant.parse("2026-03-08T07:30:00Z"),gap)
        assertEquals(Instant.parse("2026-03-09T06:30:00Z"),entry.nextOccurrence(gap,zone))
        val evening = LearningReminder(hour = 18)
        val now = Instant.parse("2026-10-21T10:00:00Z")
        assertEquals(18, evening.nextOccurrence(now,ZoneId.of("Asia/Tokyo")).atZone(ZoneId.of("Asia/Tokyo")).hour)
        assertEquals(18, evening.nextOccurrence(now,ZoneId.of("Europe/Istanbul")).atZone(ZoneId.of("Europe/Istanbul")).hour)
        val fall = LearningReminder(hour=1,minute=30)
        assertEquals(Instant.parse("2026-11-02T06:30:00Z"),fall.nextOccurrence(Instant.parse("2026-11-01T05:30:00Z"),zone))
    }
    @Test fun delayedAlarmSurvivesRefreshAndColdStartIncludingMidnight() {
        val zone = ZoneId.of("Europe/Istanbul")
        val entry = LearningReminder(hour = 23, minute = 59)
        val due = Instant.parse("2026-10-21T20:59:00Z")
        val now = due.plusSeconds(180)
        assertEquals(due, entry.scheduledOccurrence(now, zone, due.toEpochMilli(), 0))
        assertEquals(entry.nextOccurrence(now, zone), entry.scheduledOccurrence(now, zone, due.toEpochMilli(), due.toEpochMilli()))
        assertEquals(entry.copy(hour = 10).nextOccurrence(now, zone), entry.copy(hour = 10).scheduledOccurrence(now, zone, due.toEpochMilli(), 0))
        val tomorrow = now.plusSeconds(86400)
        assertEquals(entry.nextOccurrence(tomorrow, zone), entry.scheduledOccurrence(tomorrow, zone, due.toEpochMilli(), 0))
        assertEquals(due, entry.scheduledOccurrence(due.minusSeconds(60), zone, due.toEpochMilli(), 0))
    }
}
