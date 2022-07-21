//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.springframework.scheduling.support;

import java.time.DateTimeException;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.time.temporal.ValueRange;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

abstract class CronField {
    private static final String[] MONTHS = new String[]{"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
    private static final String[] DAYS = new String[]{"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};
    private final CronField.Type type;

    protected CronField(CronField.Type type) {
        this.type = type;
    }

    public static CronField zeroNanos() {
        return BitsCronField.zeroNanos();
    }

    public static CronField parseSeconds(String value) {
        return BitsCronField.parseSeconds(value);
    }

    public static CronField parseMinutes(String value) {
        return BitsCronField.parseMinutes(value);
    }

    public static CronField parseHours(String value) {
        return BitsCronField.parseHours(value);
    }

    public static CronField parseDaysOfMonth(String value) {
        return (CronField)(!value.contains("L") && !value.contains("W") ? BitsCronField.parseDaysOfMonth(value) : QuartzCronField.parseDaysOfMonth(value));
    }

    public static CronField parseMonth(String value) {
        value = replaceOrdinals(value, MONTHS);
        return BitsCronField.parseMonth(value);
    }

    public static CronField parseDaysOfWeek(String value) {
        value = replaceOrdinals(value, DAYS);
        return (CronField)(!value.contains("L") && !value.contains("#") ? BitsCronField.parseDaysOfWeek(value) : QuartzCronField.parseDaysOfWeek(value));
    }

    private static String replaceOrdinals(String value, String[] list) {
        value = value.toUpperCase();

        for(int i = 0; i < list.length; ++i) {
            String replacement = Integer.toString(i + 1);
            value = StringUtils.replace(value, list[i], replacement);
        }

        return value;
    }

    @Nullable
    public abstract <T extends Temporal & Comparable<? super T>> T nextOrSame(T var1);

    protected CronField.Type type() {
        return this.type;
    }

    protected static enum Type {
        NANO(ChronoField.NANO_OF_SECOND, new ChronoField[0]),
        SECOND(ChronoField.SECOND_OF_MINUTE, new ChronoField[]{ChronoField.NANO_OF_SECOND}),
        MINUTE(ChronoField.MINUTE_OF_HOUR, new ChronoField[]{ChronoField.SECOND_OF_MINUTE, ChronoField.NANO_OF_SECOND}),
        HOUR(ChronoField.HOUR_OF_DAY, new ChronoField[]{ChronoField.MINUTE_OF_HOUR, ChronoField.SECOND_OF_MINUTE, ChronoField.NANO_OF_SECOND}),
        DAY_OF_MONTH(ChronoField.DAY_OF_MONTH, new ChronoField[]{ChronoField.HOUR_OF_DAY, ChronoField.MINUTE_OF_HOUR, ChronoField.SECOND_OF_MINUTE, ChronoField.NANO_OF_SECOND}),
        MONTH(ChronoField.MONTH_OF_YEAR, new ChronoField[]{ChronoField.DAY_OF_MONTH, ChronoField.HOUR_OF_DAY, ChronoField.MINUTE_OF_HOUR, ChronoField.SECOND_OF_MINUTE, ChronoField.NANO_OF_SECOND}),
        DAY_OF_WEEK(ChronoField.DAY_OF_WEEK, new ChronoField[]{ChronoField.HOUR_OF_DAY, ChronoField.MINUTE_OF_HOUR, ChronoField.SECOND_OF_MINUTE, ChronoField.NANO_OF_SECOND});

        private final ChronoField field;
        private final ChronoField[] lowerOrders;

        private Type(ChronoField field, ChronoField... lowerOrders) {
            this.field = field;
            this.lowerOrders = lowerOrders;
        }

        public int get(Temporal date) {
            return date.get(this.field);
        }

        public ValueRange range() {
            return this.field.range();
        }

        public int checkValidValue(int value) {
            if (this == DAY_OF_WEEK && value == 0) {
                return value;
            } else {
                try {
                    return this.field.checkValidIntValue((long)value);
                } catch (DateTimeException var3) {
                    throw new IllegalArgumentException(var3.getMessage(), var3);
                }
            }
        }

        public <T extends Temporal & Comparable<? super T>> T elapseUntil(T temporal, int goal) {
            int current = this.get(temporal);
            if (current < goal) {
                return this.field.getBaseUnit().addTo(temporal, (long)(goal - current));
            } else {
                ValueRange range = temporal.range(this.field);
                long amount = (long)goal + range.getMaximum() - (long)current + 1L - range.getMinimum();
                return this.field.getBaseUnit().addTo(temporal, amount);
            }
        }

        public <T extends Temporal & Comparable<? super T>> T rollForward(T temporal) {
            int current = this.get(temporal);
            ValueRange range = temporal.range(this.field);
            long amount = range.getMaximum() - (long)current + 1L;
            return this.field.getBaseUnit().addTo(temporal, amount);
        }

        public <T extends Temporal> T reset(T temporal) {
            ChronoField[] var2 = this.lowerOrders;
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                ChronoField lowerOrder = var2[var4];
                if (temporal.isSupported(lowerOrder)) {
                    temporal = lowerOrder.adjustInto(temporal, temporal.range(lowerOrder).getMinimum());
                }
            }

            return temporal;
        }

        public String toString() {
            return this.field.toString();
        }
    }
}
