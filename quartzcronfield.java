//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.springframework.scheduling.support;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.support.CronField.Type;
import org.springframework.util.Assert;

final class QuartzCronField extends CronField {
    private static final TemporalAdjuster lastWeekdayOfMonth = (temporal) -> {
        Temporal lastDayOfMonth = TemporalAdjusters.lastDayOfMonth().adjustInto(temporal);
        int dayOfWeek = lastDayOfMonth.get(ChronoField.DAY_OF_WEEK);
        if (dayOfWeek == 6) {
            return lastDayOfMonth.minus(1L, ChronoUnit.DAYS);
        } else {
            return dayOfWeek == 7 ? lastDayOfMonth.minus(2L, ChronoUnit.DAYS) : lastDayOfMonth;
        }
    };
    private final Type rollForwardType;
    private final TemporalAdjuster adjuster;
    private final String value;

    private QuartzCronField(Type type, TemporalAdjuster adjuster, String value) {
        this(type, type, adjuster, value);
    }

    private QuartzCronField(Type type, Type rollForwardType, TemporalAdjuster adjuster, String value) {
        super(type);
        this.adjuster = adjuster;
        this.value = value;
        this.rollForwardType = rollForwardType;
    }

    public static QuartzCronField parseDaysOfMonth(String value) {
        int idx = value.lastIndexOf(76);
        if (idx != -1) {
            if (idx != 0) {
                throw new IllegalArgumentException("Unrecognized characters before 'L' in '" + value + "'");
            } else {
                TemporalAdjuster adjuster;
                if (value.length() == 2 && value.charAt(1) == 'W') {
                    adjuster = lastWeekdayOfMonth;
                } else if (value.length() == 1) {
                    adjuster = TemporalAdjusters.lastDayOfMonth();
                } else {
                    int offset = Integer.parseInt(value.substring(idx + 1));
                    if (offset >= 0) {
                        throw new IllegalArgumentException("Offset '" + offset + " should be < 0 '" + value + "'");
                    }

                    adjuster = lastDayWithOffset(offset);
                }

                return new QuartzCronField(Type.DAY_OF_MONTH, adjuster, value);
            }
        } else {
            idx = value.lastIndexOf(87);
            if (idx != -1) {
                if (idx == 0) {
                    throw new IllegalArgumentException("No day-of-month before 'W' in '" + value + "'");
                } else if (idx != value.length() - 1) {
                    throw new IllegalArgumentException("Unrecognized characters after 'W' in '" + value + "'");
                } else {
                    int dayOfMonth = Integer.parseInt(value.substring(0, idx));
                    dayOfMonth = Type.DAY_OF_MONTH.checkValidValue(dayOfMonth);
                    TemporalAdjuster adjuster = weekdayNearestTo(dayOfMonth);
                    return new QuartzCronField(Type.DAY_OF_MONTH, adjuster, value);
                }
            } else {
                throw new IllegalArgumentException("No 'L' or 'W' found in '" + value + "'");
            }
        }
    }

    public static QuartzCronField parseDaysOfWeek(String value) {
        int idx = value.lastIndexOf(76);
        if (idx != -1) {
            if (idx != value.length() - 1) {
                throw new IllegalArgumentException("Unrecognized characters after 'L' in '" + value + "'");
            } else if (idx == 0) {
                throw new IllegalArgumentException("No day-of-week before 'L' in '" + value + "'");
            } else {
                DayOfWeek dayOfWeek = parseDayOfWeek(value.substring(0, idx));
                TemporalAdjuster adjuster = TemporalAdjusters.lastInMonth(dayOfWeek);
                return new QuartzCronField(Type.DAY_OF_WEEK, Type.DAY_OF_MONTH, adjuster, value);
            }
        } else {
            idx = value.lastIndexOf(35);
            if (idx != -1) {
                if (idx == 0) {
                    throw new IllegalArgumentException("No day-of-week before '#' in '" + value + "'");
                } else if (idx == value.length() - 1) {
                    throw new IllegalArgumentException("No ordinal after '#' in '" + value + "'");
                } else {
                    DayOfWeek dayOfWeek = parseDayOfWeek(value.substring(0, idx));
                    int ordinal = Integer.parseInt(value.substring(idx + 1));
                    TemporalAdjuster adjuster = TemporalAdjusters.dayOfWeekInMonth(ordinal, dayOfWeek);
                    return new QuartzCronField(Type.DAY_OF_WEEK, Type.DAY_OF_MONTH, adjuster, value);
                }
            } else {
                throw new IllegalArgumentException("No 'L' or '#' found in '" + value + "'");
            }
        }
    }

    private static DayOfWeek parseDayOfWeek(String value) {
        int dayOfWeek = Integer.parseInt(value);
        if (dayOfWeek == 0) {
            dayOfWeek = 7;
        }

        try {
            return DayOfWeek.of(dayOfWeek);
        } catch (DateTimeException var4) {
            String msg = var4.getMessage() + " '" + value + "'";
            throw new IllegalArgumentException(msg, var4);
        }
    }

    private static TemporalAdjuster lastDayWithOffset(int offset) {
        Assert.isTrue(offset < 0, "Offset should be < 0");
        return (temporal) -> {
            Temporal lastDayOfMonth = TemporalAdjusters.lastDayOfMonth().adjustInto(temporal);
            return lastDayOfMonth.plus((long)offset, ChronoUnit.DAYS);
        };
    }

    private static TemporalAdjuster weekdayNearestTo(int dayOfMonth) {
        return (temporal) -> {
            int current = Type.DAY_OF_MONTH.get(temporal);
            int dayOfWeek = temporal.get(ChronoField.DAY_OF_WEEK);
            if (current == dayOfMonth && dayOfWeek < 6 || dayOfWeek == 5 && current == dayOfMonth - 1 || dayOfWeek == 1 && current == dayOfMonth + 1 || dayOfWeek == 1 && dayOfMonth == 1 && current == 3) {
                return temporal;
            } else {
                int var4 = 0;

                do {
                    if (var4++ >= 366) {
                        return null;
                    }

                    temporal = Type.DAY_OF_MONTH.elapseUntil(cast(temporal), dayOfMonth);
                    current = Type.DAY_OF_MONTH.get(temporal);
                } while(current != dayOfMonth);

                dayOfWeek = temporal.get(ChronoField.DAY_OF_WEEK);
                if (dayOfWeek == 6) {
                    if (dayOfMonth != 1) {
                        return temporal.minus(1L, ChronoUnit.DAYS);
                    } else {
                        return temporal.plus(2L, ChronoUnit.DAYS);
                    }
                } else if (dayOfWeek == 7) {
                    return temporal.plus(1L, ChronoUnit.DAYS);
                } else {
                    return temporal;
                }
            }
        };
    }

    private static <T extends Temporal & Comparable<? super T>> T cast(Temporal temporal) {
        return temporal;
    }

    public <T extends Temporal & Comparable<? super T>> T nextOrSame(T temporal) {
        T result = this.adjust(temporal);
        if (result != null && ((Comparable)result).compareTo(temporal) < 0) {
            temporal = this.rollForwardType.rollForward(temporal);
            result = this.adjust(temporal);
        }

        return result;
    }

    @Nullable
    private <T extends Temporal & Comparable<? super T>> T adjust(T temporal) {
        return this.adjuster.adjustInto(temporal);
    }

    public int hashCode() {
        return this.value.hashCode();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof QuartzCronField)) {
            return false;
        } else {
            QuartzCronField other = (QuartzCronField)o;
            return this.type() == other.type() && this.value.equals(other.value);
        }
    }

    public String toString() {
        return this.type() + " '" + this.value + "'";
    }
}
