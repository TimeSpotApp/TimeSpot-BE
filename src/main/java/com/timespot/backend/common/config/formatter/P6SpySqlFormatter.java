package com.timespot.backend.common.config.formatter;

import static com.p6spy.engine.logging.Category.STATEMENT;
import static java.util.Locale.ROOT;
import static org.hibernate.engine.jdbc.internal.FormatStyle.BASIC;
import static org.hibernate.engine.jdbc.internal.FormatStyle.DDL;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Objects;
import java.util.Stack;
import java.util.function.Predicate;

/**
 * PackageName : com.timespot.backend.common.config.formatter
 * FileName    : P6SpySqlFormatter
 * Author      : loadingKKamo21
 * Date        : 26. 3. 7.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 7.     loadingKKamo21       Initial creation
 */
public class P6SpySqlFormatter implements MessageFormattingStrategy {

    private static final String NEW_LINE        = System.lineSeparator();
    private static final String CREATE          = "create";
    private static final String ALTER           = "alter";
    private static final String COMMENT         = "comment";
    private static final String PACKAGE         = "io.p6spy";
    private static final String P6SPY_FORMATTER = "P6SpySqlFormatter";

    @Override
    public String formatMessage(int connectionId,
                                String now,
                                long elapsed,
                                String category,
                                String prepared,
                                String sql, String url) {
        //return "#" + now + " | took " + elapsed + "ms | " + category + " | connection " + connectionId + "| url " + url + "\n" + prepared + "\n" + sql + ";";
        return sqlFormatToUpper(sql, category, getMessage(connectionId, elapsed, getStackBuilder()));
    }

    private String sqlFormatToUpper(final String sql, final String category, final String message) {
        if (Objects.isNull(sql) || sql.isBlank()) return "";
        return new StringBuilder().append(NEW_LINE)
                                  .append(sqlFormatToUpper(sql, category))
                                  .append(message)
                                  .toString();
    }

    private String sqlFormatToUpper(final String sql, final String category) {
        if (isStatementDDL(sql, category))
            return DDL.getFormatter().format(sql).toUpperCase(ROOT).replace("+0900", "");
        return BASIC.getFormatter().format(sql).toUpperCase(ROOT).replace("+0900", "");
    }

    private boolean isStatementDDL(final String sql, final String category) {
        return isStatement(category) && isDDL(sql.trim().toLowerCase(ROOT));
    }

    private boolean isStatement(final String category) {
        return STATEMENT.getName().equals(category);
    }

    private boolean isDDL(final String lowerSql) {
        return lowerSql.startsWith(CREATE) || lowerSql.startsWith(ALTER) || lowerSql.startsWith(COMMENT);
    }

    private String getMessage(final int connectionId, final long elapsed, final StringBuilder callStackBuilder) {
        return new StringBuilder().append(NEW_LINE)
                                  .append(NEW_LINE)
                                  .append("\t").append(String.format("Connection ID: %s", connectionId))
                                  .append(NEW_LINE)
                                  .append("\t").append(String.format("Execution Time: %s ms", elapsed))
                                  .append(NEW_LINE)
                                  .append(NEW_LINE)
                                  .append("\t")
                                  .append(String.format("Call Stack (number 1 is entry point): %s", callStackBuilder))
                                  .append(NEW_LINE)
                                  .append(NEW_LINE)
                                  .append("---------------------------------------------------------------------------------------------------------------------")
                                  .toString();
    }

    private StringBuilder getStackBuilder() {
        Stack<String> callStack = new Stack<>();
        Arrays.stream(new Throwable().getStackTrace())
              .map(StackTraceElement::toString)
              .filter(isExcludeWords())
              .forEach(callStack::push);

        int order = 1;

        StringBuilder callStackBuilder = new StringBuilder();
        while (!callStack.isEmpty())
            callStackBuilder.append(MessageFormat.format("{0}\t\t{1}. {2}", NEW_LINE, order++, callStack.pop()));

        return callStackBuilder;
    }

    private Predicate<String> isExcludeWords() {
        return s -> s.startsWith(PACKAGE) && !s.contains(P6SPY_FORMATTER);
    }

}
