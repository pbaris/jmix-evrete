package gr.netmechanics.jmix.evrete.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

import io.jmix.flowui.component.propertyfilter.PropertyFilter;

/**
 * Enhanced utility class for generating Java expression strings based on a given
 * Java type, property path, operation, and value for Evrete rule engine DSL.
 * This version provides more robust handling for date/time types and improves
 * code structure for better maintainability.
 *
 * @author Panos Bariamis (pbaris)
 */
public class ExpressionGenerator {

    // A flexible formatter that can parse various date and datetime formats.
    // This handles ISO_LOCAL_DATE, ISO_LOCAL_DATE_TIME, and ISO_ZONED_DATE_TIME
    // with optional parts to be more lenient.
    private static final DateTimeFormatter FLEXIBLE_DATETIME_FORMATTER = new DateTimeFormatterBuilder()
        .append(DateTimeFormatter.ISO_LOCAL_DATE)
        .optionalStart()
        .appendLiteral('T')
        .append(DateTimeFormatter.ISO_LOCAL_TIME)
        .optionalStart()
        .appendOffsetId()
        .optionalEnd()
        .optionalStart()
        .appendZoneId()
        .optionalEnd()
        .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
        .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
        .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
        .toFormatter();

    /**
     * Generates a Java expression string for Evrete rule engine DSL.
     *
     * @param javaType     The Class<?> object representing the Java type of the property
     * @param propertyPath The path to the property (e.g., "user.name", "active")
     * @param operation    The operation to perform (e.g., EQUAL, CONTAINS)
     * @param value        The value to compare against or use in the operation
     * @return A string representing the Java expression
     */
    public static String generateExpression(final Class<?> javaType, final String propertyPath,
                                            final PropertyFilter.Operation operation, final String value) {

        if (propertyPath == null || propertyPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Property path cannot be null or empty");
        }

        // Handle null values for IS_SET and other operations
        if (value == null && operation != PropertyFilter.Operation.IS_SET) {
            return generateNullComparison(propertyPath, operation);
        }

        return switch (operation) {
            case EQUAL -> generateEqualExpression(javaType, propertyPath, value);
            case NOT_EQUAL -> generateNotEqualExpression(javaType, propertyPath, value);
            case GREATER -> generateComparisonExpression(javaType, propertyPath, value, ">");
            case GREATER_OR_EQUAL -> generateComparisonExpression(javaType, propertyPath, value, ">=");
            case LESS -> generateComparisonExpression(javaType, propertyPath, value, "<");
            case LESS_OR_EQUAL -> generateComparisonExpression(javaType, propertyPath, value, "<=");
            case CONTAINS -> generateStringOperation(propertyPath, value, "contains", false);
            case NOT_CONTAINS -> generateStringOperation(propertyPath, value, "contains", true);
            case STARTS_WITH -> generateStringOperation(propertyPath, value, "startsWith", false);
            case ENDS_WITH -> generateStringOperation(propertyPath, value, "endsWith", false);
            case IS_SET -> generateIsSetExpression(javaType, propertyPath);
            case IN_LIST -> generateInListExpression(javaType, propertyPath, value, false);
            case NOT_IN_LIST -> generateInListExpression(javaType, propertyPath, value, true);
            case IN_INTERVAL -> generateIntervalExpression(javaType, propertyPath, value);
            case IS_COLLECTION_EMPTY -> generateCollectionEmptyExpression(propertyPath);
            case MEMBER_OF_COLLECTION -> generateCollectionMemberExpression(javaType, propertyPath, value, false);
            case NOT_MEMBER_OF_COLLECTION -> generateCollectionMemberExpression(javaType, propertyPath, value, true);
        };
    }

    private static String generateNullComparison(final String propertyPath, final PropertyFilter.Operation operation) {
        return (operation == PropertyFilter.Operation.NOT_EQUAL) ? propertyPath + " != null" : propertyPath + " == null";
    }

    private static String generateEqualExpression(final Class<?> javaType, final String propertyPath, final String value) {
        // Use Objects.equals for consistent null-safe comparison
        return String.format("java.util.Objects.equals(%s, %s)", propertyPath, formatValue(value, javaType));
    }

    private static String generateNotEqualExpression(final Class<?> javaType, final String propertyPath, final String value) {
        return String.format("!java.util.Objects.equals(%s, %s)", propertyPath, formatValue(value, javaType));
    }

    private static String generateComparisonExpression(final Class<?> javaType, final String propertyPath,
                                                       final String value, final String operator) {
        String formattedValue = formatValue(value, javaType);

        if (isComparableType(javaType)) {
            // For Comparable types (including Date and LocalDate), use compareTo
            return String.format("((%s) != null && %s != null && %s.compareTo(%s) %s 0)",
                propertyPath, formattedValue, propertyPath, formattedValue, operator);

        } else if (isNumericType(javaType)) {
            // For primitive numeric types, direct comparison is enough
            return String.format("((%s) != null && %s %s %s)", propertyPath, propertyPath, operator, formattedValue);

        } else {
            // Fallback for other types
            return String.format("/* Unsupported comparison for type: %s */", javaType.getName());
        }
    }

    private static String generateStringOperation(final String propertyPath, final String value, final String method, final boolean negate) {
        String formattedValue = formatValue(value, String.class);
        String expression = String.format("(%s != null && %s.%s(%s))", propertyPath, propertyPath, method, formattedValue);
        return negate ? ("!" + expression) : expression;
    }

    private static String generateIsSetExpression(final Class<?> javaType, final String propertyPath) {
        if (isStringType(javaType) || isCollectionType(javaType) || isMapType(javaType)) {
            return String.format("(%s != null && !%s.isEmpty())", propertyPath, propertyPath);

        } else {
            return propertyPath + " != null";
        }
    }

    private static String generateInListExpression(final Class<?> javaType, final String propertyPath, final String value, final boolean negate) {
        String[] values = value.split(",");
        String formattedList = Arrays.stream(values)
            .map(String::trim)
            .map(v -> formatValue(v, javaType))
            .collect(Collectors.joining(", "));

        String expression = String.format("java.util.Arrays.asList(%s).contains(%s)", formattedList, propertyPath);
        return negate ? ("!" + expression) : expression;
    }

    private static String generateIntervalExpression(final Class<?> javaType, final String propertyPath, final String value) {
        String[] bounds = value.split(",");
        if (bounds.length != 2) {
            return "/* Error: Invalid interval format for IN_INTERVAL. Expected 'lower,upper' */";
        }

        String lowerBound = formatValue(bounds[0].trim(), javaType);
        String upperBound = formatValue(bounds[1].trim(), javaType);

        // All comparable types can be checked using the compareTo method
        if (isComparableType(javaType)) {
            return String.format("(%s != null && %s.compareTo(%s) >= 0 && %s.compareTo(%s) <= 0)",
                propertyPath, propertyPath, lowerBound, propertyPath, upperBound);

        } else if (isNumericType(javaType)) {
            return String.format("(%s != null && %s >= %s && %s <= %s)",
                propertyPath, propertyPath, lowerBound, propertyPath, upperBound);

        } else {
            return String.format("/* Unsupported interval for type: %s */", javaType.getName());
        }
    }

    private static String generateCollectionEmptyExpression(final String propertyPath) {
        return String.format("(%s != null && %s.isEmpty())", propertyPath, propertyPath);
    }

    private static String generateCollectionMemberExpression(final Class<?> javaType, final String propertyPath,
                                                             final String value, final boolean negate) {
        String formattedValue = formatValue(value, javaType);
        String expression = String.format("(%s != null && %s.contains(%s))", propertyPath, propertyPath, formattedValue);
        return negate ? ("!" + expression) : expression;
    }

    /**
     * Formats a given string value into a proper Java literal based on the target type.
     */
    private static String formatValue(final String value, final Class<?> javaType) {
        if (value == null) {
            return "null";
        }

        if (isStringType(javaType)) {
            return formatStringValue(value);
        }

        if (isBooleanType(javaType)) {
            return formatBooleanValue(value);
        }

        if (isNumericType(javaType)) {
            return formatNumericValue(value, javaType);
        }

        if (isDateType(javaType)) {
            return formatDateValue(value, javaType);
        }

        // Default to a string literal for safety if the type is unknown
        return formatStringValue(value);
    }

    private static String formatStringValue(final String value) {
        return "\"" + value.replace("\"", "\\\"") + "\"";
    }

    private static String formatBooleanValue(String value) {
        return Boolean.parseBoolean(value) ? "true" : "false";
    }

    /**
     * Unified method for formatting all date/time types.
     */
    private static String formatDateValue(final String value, final Class<?> javaType) {
        try {
            // Handle LocalDate
            if (LocalDate.class.equals(javaType) || java.sql.Date.class.equals(javaType)) {
                LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
                return String.format("%s.parse(\"%s\")", javaType.getName(), value);
            }

            // Handle LocalDateTime
            if (LocalDateTime.class.equals(javaType) || java.sql.Timestamp.class.equals(javaType)) {
                LocalDateTime.parse(value, FLEXIBLE_DATETIME_FORMATTER);
                return String.format("%s.parse(\"%s\")", javaType.getName(), value);
            }

            // Handle ZonedDateTime and OffsetDateTime
            if (ZonedDateTime.class.equals(javaType)) {
                ZonedDateTime.parse(value, FLEXIBLE_DATETIME_FORMATTER);
                return String.format("java.time.ZonedDateTime.parse(\"%s\")", value);
            }

            if (OffsetDateTime.class.equals(javaType)) {
                OffsetDateTime.parse(value, FLEXIBLE_DATETIME_FORMATTER);
                return String.format("java.time.OffsetDateTime.parse(\"%s\")", value);
            }

            // Handle Instant
            if (Instant.class.equals(javaType)) {
                Instant.parse(value);
                return String.format("java.time.Instant.parse(\"%s\")", value);
            }

            // Handle legacy java.util.Date
            if (Date.class.equals(javaType)) {
                ZonedDateTime zonedDateTime = ZonedDateTime.parse(value, FLEXIBLE_DATETIME_FORMATTER);
                return String.format("java.util.Date.from(%s.toInstant())",
                    String.format("java.time.ZonedDateTime.parse(\"%s\")", value));
            }

        } catch (DateTimeParseException e) {
            // If parsing fails, return an expression that will cause a runtime error with a helpful comment
            return String.format("/* Error parsing date '%s' for type %s */", value, javaType.getSimpleName());
        }

        return formatStringValue(value);
    }

    private static String formatNumericValue(final String value, final Class<?> javaType) {
        try {
            if (int.class.equals(javaType) || Integer.class.equals(javaType)) {
                Integer.parseInt(value);
                return value;

            } else if (long.class.equals(javaType) || Long.class.equals(javaType)) {
                Long.parseLong(value);
                return value + "L";

            } else if (double.class.equals(javaType) || Double.class.equals(javaType)) {
                Double.parseDouble(value);
                return value + "d";

            } else if (float.class.equals(javaType) || Float.class.equals(javaType)) {
                Float.parseFloat(value);
                return value + "f";

            } else if (short.class.equals(javaType) || Short.class.equals(javaType)) {
                Short.parseShort(value);
                return "(short)" + value;

            } else if (byte.class.equals(javaType) || Byte.class.equals(javaType)) {
                Byte.parseByte(value);
                return "(byte)" + value;
            }

        } catch (NumberFormatException e) {
            return String.format("/* Error parsing number '%s' for type %s */", value, javaType.getSimpleName());
        }
        return value;
    }

    // Helper methods for type checking
    private static boolean isStringType(final Class<?> javaType) {
        return String.class.equals(javaType);
    }

    private static boolean isBooleanType(final Class<?> javaType) {
        return Boolean.class.equals(javaType) || boolean.class.equals(javaType);
    }

    private static boolean isNumericType(final Class<?> javaType) {
        return Number.class.isAssignableFrom(javaType)
               || int.class.equals(javaType) || long.class.equals(javaType)
               || double.class.equals(javaType) || float.class.equals(javaType)
               || short.class.equals(javaType) || byte.class.equals(javaType);
    }

    private static boolean isDateType(final Class<?> javaType) {
        return Date.class.equals(javaType)
               || java.sql.Date.class.equals(javaType)
               || java.sql.Timestamp.class.equals(javaType)
               || LocalDate.class.equals(javaType)
               || LocalDateTime.class.equals(javaType)
               || ZonedDateTime.class.equals(javaType)
               || OffsetDateTime.class.equals(javaType)
               || Instant.class.equals(javaType);
    }

    private static boolean isComparableType(final Class<?> javaType) {
        return Comparable.class.isAssignableFrom(javaType)
               || isNumericType(javaType)
               || isDateType(javaType);
    }

    private static boolean isCollectionType(final Class<?> javaType) {
        return Collection.class.isAssignableFrom(javaType);
    }

    private static boolean isMapType(final Class<?> javaType) {
        return Map.class.isAssignableFrom(javaType);
    }
}
