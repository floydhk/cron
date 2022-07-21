//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.springframework.scheduling.support;

import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Arrays;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public final class CronExpression {
    static final int MAX_ATTEMPTS = 366;
    private static final String[] MACROS = new String[]{"@yearly", "0 0 0 1 1 *", "@annually", "0 0 0 1 1 *", "@monthly", "0 0 0 1 * *", "@weekly", "0 0 0 * * 0", "@daily", "0 0 0 * * *", "@midnight", "0 0 0 * * *", "@hourly", "0 0 * * * *"};
    private final CronField[] fields;
    private final String expression;

    private CronExpression(CronField seconds, CronField minutes, CronField hours, CronField daysOfMonth, CronField months, CronField daysOfWeek, String expression) {
        this.fields = new CronField[]{CronField.zeroNanos(), seconds, minutes, hours, daysOfMonth, months, daysOfWeek};
        this.expression = expression;
    }

    public static CronExpression parse(String expression) {
        Assert.hasLength(expression, "Expression string must not be empty");
        expression = resolveMacros(expression);
        String[] fields = StringUtils.tokenizeToStringArray(expression, " ");
        if (fields.length != 6) {
            throw new IllegalArgumentException(String.format("Cron expression must consist of 6 fields (found %d in \"%s\")", fields.length, expression));
        } else {
            try {
                CronField seconds = CronField.parseSeconds(fields[0]);
                CronField minutes = CronField.parseMinutes(fields[1]);
                CronField hours = CronField.parseHours(fields[2]);
                CronField daysOfMonth = CronField.parseDaysOfMonth(fields[3]);
                CronField months = CronField.parseMonth(fields[4]);
                CronField daysOfWeek = CronField.parseDaysOfWeek(fields[5]);
                return new CronExpression(seconds, minutes, hours, daysOfMonth, months, daysOfWeek, expression);
            } catch (IllegalArgumentException var8) {
                String msg = var8.getMessage() + " in cron expression \"" + expression + "\"";
                throw new IllegalArgumentException(msg, var8);
            }
        }
    }

    private static String resolveMacros(String expression) {
        expression = expression.trim();

        for(int i = 0; i < MACROS.length; i += 2) {
            if (MACROS[i].equalsIgnoreCase(expression)) {
                return MACROS[i + 1];
            }
        }

        return expression;
    }

    @Nullable
    public <T extends Temporal & Comparable<? super T>> T next(T temporal) {
        return this.nextOrSame(ChronoUnit.NANOS.addTo(temporal, 1L));
    }

    @Nullable
    private <T extends Temporal & Comparable<? super T>> T nextOrSame(T temporal) {
        for(int i = 0; i < 366; ++i) {
            T result = this.nextOrSameInternal(temporal);
            if (result == null || result.equals(temporal)) {
                return result;
            }

            temporal = result;
        }

        return null;
    }

    @Nullable
    private <T extends Temporal & Comparable<? super T>> T nextOrSameInternal(T temporal) {
        CronField[] var2 = this.fields;
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            CronField field = var2[var4];
            temporal = field.nextOrSame(temporal);
            if (temporal == null) {
                return null;
            }
        }

        return temporal;
    }

    public int hashCode() {
        return Arrays.hashCode(this.fields);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof CronExpression) {
            CronExpression other = (CronExpression)o;
            return Arrays.equals(this.fields, other.fields);
        } else {
            return false;
        }
    }

    public String toString() {
        return this.expression;
    }
}
