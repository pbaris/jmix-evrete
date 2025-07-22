package gr.netmechanics.jmix.evrete.util;

import io.jmix.flowui.component.propertyfilter.PropertyFilter;

/**
 * A utility class for generating Java expression strings based on a given
 * Java type, property path, operation, and value.
 *
 * @author Panos Bariamis (pbaris)
 */
public class ExpressionGenerator {

    /**
     * Generates a Java expression string.
     *
     * @param javaType     The Class<?> object representing the Java type of the property (e.g., String.class, boolean.class).
     * @param propertyPath The path to the property (e.g., "user.name", "active").
     *                     This will be used directly as the variable name in the expression.
     * @param operation    The operation to perform (e.g., EQUAL, CONTAINS).
     * @param value        The value to compare against or use in the operation.
     *                     For list operations, it should be comma-separated. For interval, "lower,upper".
     * @return A string representing the Java expression.
     */
    public static String generateExpression(final Class<?> javaType, final String propertyPath,
                                            final PropertyFilter.Operation operation, final String value) {

        // Format the value based on its Java type for proper inclusion in the expression.
        String formattedValue = formatValue(value, javaType);
        String expression = "";

        // Use a switch statement to handle different operations.
        switch (operation) {
            case EQUAL:
                if (isStringType(javaType)) {
                    // For String equality, use .equals() method.
                    expression = String.format("%s.equals(%s)", propertyPath, formattedValue);
                } else if (isBooleanType(javaType)) {
                    // For Boolean equality, use direct property path for 'true', and negation for 'false'.
                    expression = value.equalsIgnoreCase("true") ? propertyPath : String.format("!%s", propertyPath);
                } else {
                    // For other types (numbers, etc.), use '==' operator.
                    expression = String.format("%s == %s", propertyPath, formattedValue);
                }
                break;
            case NOT_EQUAL:
                if (isStringType(javaType)) {
                    // For String inequality, use !.equals() method.
                    expression = String.format("!%s.equals(%s)", propertyPath, formattedValue);
                } else if (isBooleanType(javaType)) {
                    // For Boolean inequality, negate the boolean expression.
                    expression = value.equalsIgnoreCase("true") ? String.format("!%s", propertyPath) : propertyPath;
                } else {
                    // For other types, use '!=' operator.
                    expression = String.format("%s != %s", propertyPath, formattedValue);
                }
                break;
            case GREATER:
                if (isComparableType(javaType)) {
                    // For comparable types (e.g., Date, custom objects), use compareTo method.
                    expression = String.format("%s.compareTo(%s) > 0", propertyPath, formattedValue);
                } else { // Assuming numeric for other cases
                    expression = String.format("%s > %s", propertyPath, formattedValue);
                }
                break;
            case GREATER_OR_EQUAL:
                if (isComparableType(javaType)) {
                    expression = String.format("%s.compareTo(%s) >= 0", propertyPath, formattedValue);
                } else {
                    expression = String.format("%s >= %s", propertyPath, formattedValue);
                }
                break;
            case LESS:
                if (isComparableType(javaType)) {
                    expression = String.format("%s.compareTo(%s) < 0", propertyPath, formattedValue);
                } else {
                    expression = String.format("%s < %s", propertyPath, formattedValue);
                }
                break;
            case LESS_OR_EQUAL:
                if (isComparableType(javaType)) {
                    expression = String.format("%s.compareTo(%s) <= 0", propertyPath, formattedValue);
                } else {
                    expression = String.format("%s <= %s", propertyPath, formattedValue);
                }
                break;
            case CONTAINS:
                // For String type, use the .contains() method.
                expression = String.format("%s.contains(%s)", propertyPath, formattedValue);
                break;
            case NOT_CONTAINS:
                // For String type, use negation with .contains() method.
                expression = String.format("!%s.contains(%s)", propertyPath, formattedValue);
                break;
            case STARTS_WITH:
                // For String type, use the .startsWith() method.
                expression = String.format("%s.startsWith(%s)", propertyPath, formattedValue);
                break;
            case ENDS_WITH:
                // For String type, use the .endsWith() method.
                expression = String.format("%s.endsWith(%s)", propertyPath, formattedValue);
                break;
            case IS_SET:
                // Check if the property is not null and not empty (for Strings/Collections).
                if (isStringType(javaType) || isCollectionType(javaType)) {
                    expression = String.format("%s != null && !%s.isEmpty()", propertyPath, propertyPath);
                } else {
                    expression = String.format("%s != null", propertyPath);
                }
                break;
            case IN_LIST:
                // Assumes 'value' is a comma-separated string (e.g., "item1,item2").
                String[] values = value.split(",");
                StringBuilder listBuilder = new StringBuilder();
                for (int i = 0; i < values.length; i++) {
                    listBuilder.append(formatValue(values[i].trim(), javaType));
                    if (i < values.length - 1) {
                        listBuilder.append(", ");
                    }
                }
                // Uses java.util.Arrays.asList().contains() for list checking.
                expression = String.format("java.util.Arrays.asList(%s).contains(%s)", listBuilder.toString(), propertyPath);
                break;
            case NOT_IN_LIST:
                // Assumes 'value' is a comma-separated string.
                String[] notInValues = value.split(",");
                StringBuilder notInListBuilder = new StringBuilder();
                for (int i = 0; i < notInValues.length; i++) {
                    notInListBuilder.append(formatValue(notInValues[i].trim(), javaType));
                    if (i < notInValues.length - 1) {
                        notInListBuilder.append(", ");
                    }
                }
                // Uses negation with java.util.Arrays.asList().contains().
                expression = String.format("!java.util.Arrays.asList(%s).contains(%s)", notInListBuilder.toString(), propertyPath);
                break;
            case IN_INTERVAL:
                // Assumes 'value' is in "lower,upper" format.
                String[] bounds = value.split(",");
                if (bounds.length == 2) {
                    String lowerBound = formatValue(bounds[0].trim(), javaType);
                    String upperBound = formatValue(bounds[1].trim(), javaType);
                    // Generates a logical AND expression for the interval.
                    expression = String.format("%s >= %s && %s <= %s", propertyPath, lowerBound, propertyPath, upperBound);
                } else {
                    expression = "// Error: Invalid interval format for IN_INTERVAL. Expected 'lower,upper'";
                }
                break;
            case IS_COLLECTION_EMPTY:
                // Checks if a collection is empty using .isEmpty().
                expression = String.format("%s.isEmpty()", propertyPath);
                break;
            case MEMBER_OF_COLLECTION:
                // Checks if a value is a member of a collection using .contains().
                expression = String.format("%s.contains(%s)", propertyPath, formattedValue);
                break;
            case NOT_MEMBER_OF_COLLECTION:
                // Checks if a value is NOT a member of a collection using !.contains().
                expression = String.format("!%s.contains(%s)", propertyPath, formattedValue);
                break;
            default:
                expression = "/* Unsupported operation: " + operation.name() + " */";
                break;
        }
        return expression;
    }

    /**
     * Formats the given value according to its Java type for inclusion in an expression.
     * This method now ensures that string literals are escaped with backslashes
     * so they can be correctly embedded within another Java string literal.
     *
     * @param value    The raw string value.
     * @param javaType The Class<?> object representing the Java type.
     * @return The formatted string value (e.g., "panos" becomes "\"panos\"", true becomes "true").
     */
    private static String formatValue(final String value, final Class<?> javaType) {
        if (value == null) {
            return "null";
        }
        if (isStringType(javaType)) {
            // Escape any internal double quotes and then escape the outer double quotes
            // so the resulting string can be embedded as a literal within another string.
            return "\\\"" + value.replace("\"", "\\\"") + "\\\"";
        } else if (isBooleanType(javaType)) {
            // Convert to lowercase "true" or "false".
            return value.toLowerCase();
        }
        // For numeric types, dates, etc., return the value as is.
        // It's assumed that the context evaluating the expression can parse these values.
        return value;
    }

    /**
     * Helper method to check if the given Java type is a String.
     */
    private static boolean isStringType(final Class<?> javaType) {
        return String.class.equals(javaType);
    }

    /**
     * Helper method to check if the given Java type is a Boolean (primitive or wrapper).
     */
    private static boolean isBooleanType(final Class<?> javaType) {
        return Boolean.class.equals(javaType) || boolean.class.equals(javaType);
    }

    /**
     * Helper method to check if the given Java type is a numeric type.
     */
    private static boolean isNumericType(final Class<?> javaType) {
        return Number.class.isAssignableFrom(javaType)
               || int.class.equals(javaType) || long.class.equals(javaType)
               || double.class.equals(javaType) || float.class.equals(javaType)
               || short.class.equals(javaType) || byte.class.equals(javaType);
    }

    /**
     * Helper method to check if the given Java type is a comparable type (numeric, Date, LocalDateTime, etc.).
     * This is a simplified check; a more robust solution might inspect class hierarchy.
     */
    private static boolean isComparableType(final Class<?> javaType) {
        return isNumericType(javaType)
               || java.util.Date.class.equals(javaType)
               || java.time.LocalDate.class.equals(javaType)
               || java.time.LocalDateTime.class.equals(javaType)
               || java.time.ZonedDateTime.class.equals(javaType);
    }

    /**
     * Helper method to check if the given Java type is a common collection type.
     */
    private static boolean isCollectionType(final Class<?> javaType) {
        // Check if it's assignable from Collection, List, Set, or Map interfaces
        return java.util.Collection.class.isAssignableFrom(javaType)
               || java.util.List.class.isAssignableFrom(javaType)
               || java.util.Set.class.isAssignableFrom(javaType)
               || java.util.Map.class.isAssignableFrom(javaType);
    }
}
