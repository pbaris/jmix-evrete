package gr.netmechanics.jmix.evrete.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class for converting arbitrary strings into valid Java naming conventions.
 * Handles package names, class names, method names, and parameter names with proper
 * camelCase formatting and Java keyword avoidance.
 *
 * @author Panos Bariamis (pbaris)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JavaNamingUtil {

    // Java keywords and reserved words that should be avoided
    private static final Set<String> JAVA_KEYWORDS = new HashSet<>(Arrays.asList(
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
        "class", "const", "continue", "default", "do", "double", "else", "enum",
        "extends", "final", "finally", "float", "for", "goto", "if", "implements",
        "import", "instanceof", "int", "interface", "long", "native", "new", "package",
        "private", "protected", "public", "return", "short", "static", "strictfp",
        "super", "switch", "synchronized", "this", "throw", "throws", "transient",
        "try", "void", "volatile", "while", "true", "false", "null"
    ));

    // Pattern to match valid Java identifier characters
    private static final Pattern VALID_CHARS = Pattern.compile("[a-zA-Z0-9_$]");

    // Pattern to split on word boundaries (spaces, special chars, camelCase transitions)
    private static final Pattern WORD_SPLITTER = Pattern.compile("[^a-zA-Z0-9]+|(?<=[a-z])(?=[A-Z])");

    /**
     * Converts input to a valid Java package name.
     * Removes invalid characters and returns a single lowercase word.
     *
     * @param input the input string
     * @return valid package name, or "generatedpackage" if input is null/empty
     */
    public static String getPackageName(final String input) {
        if (input == null || input.trim().isEmpty()) {
            return "generatedpackage";
        }

        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                result.append(Character.toLowerCase(c));
            }
        }

        String packageName = result.toString();

        // Ensure it doesn't start with a digit
        if (packageName.isEmpty() || Character.isDigit(packageName.charAt(0))) {
            packageName = "pkg" + packageName;
        }

        // Avoid keywords
        if (JAVA_KEYWORDS.contains(packageName)) {
            packageName = packageName + "pkg";
        }

        return packageName;
    }

    /**
     * Converts input to a valid Java class name (PascalCase).
     *
     * @param input the input string
     * @return valid class name, or "GeneratedClass" if input is null/empty
     */
    public static String getClassName(final String input) {
        if (input == null || input.trim().isEmpty()) {
            return "GeneratedClass";
        }

        String camelCase = toCamelCase(input, true);
        return camelCase.isEmpty() ? "GeneratedClass" : camelCase;
    }

    /**
     * Converts input to a valid Java method name (camelCase starting with lowercase).
     *
     * @param input the input string
     * @return valid method name, or "generatedMethod" if input is null/empty
     */
    public static String getMethodName(final String input) {
        if (input == null || input.trim().isEmpty()) {
            return "generatedMethod";
        }

        String camelCase = toCamelCase(input, false);
        return camelCase.isEmpty() ? "generatedMethod" : camelCase;
    }

    /**
     * Converts input to a valid Java parameter name (camelCase starting with lowercase).
     *
     * @param input the input string
     * @return valid parameter name, or "generatedParam" if input is null/empty
     */
    public static String getParameterName(final String input) {
        if (input == null || input.trim().isEmpty()) {
            return "generatedParam";
        }

        String camelCase = toCamelCase(input, false);
        return camelCase.isEmpty() ? "generatedParam" : camelCase;
    }

    /**
     * Converts input to camelCase format.
     *
     * @param input           the input string
     * @param capitalizeFirst whether to capitalize the first letter (PascalCase vs camelCase)
     * @return camelCase formatted string
     */
    private static String toCamelCase(final String input, final boolean capitalizeFirst) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }

        // Split into words and filter out empty strings
        String[] words = WORD_SPLITTER.split(input.trim());
        List<String> validWords = new ArrayList<>();

        for (String word : words) {
            String cleanWord = cleanWord(word);
            if (!cleanWord.isEmpty()) {
                validWords.add(cleanWord);
            }
        }

        if (validWords.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < validWords.size(); i++) {
            String word = validWords.get(i);
            if (i == 0) {
                // First word: capitalize if requested, otherwise lowercase
                result.append(capitalizeFirst ? capitalize(word) : word.toLowerCase());
            } else {
                // Subsequent words: always capitalize first letter
                result.append(capitalize(word));
            }
        }

        String finalResult = result.toString();

        // Handle edge cases
        finalResult = ensureValidIdentifier(finalResult);
        finalResult = avoidKeywords(finalResult);

        return finalResult;
    }

    /**
     * Cleans a word by removing invalid characters and ensuring it's valid.
     */
    private static String cleanWord(final String word) {
        if (word == null || word.isEmpty()) {
            return "";
        }

        StringBuilder cleaned = new StringBuilder();
        for (char c : word.toCharArray()) {
            if (Character.isLetterOrDigit(c) || c == '_' || c == '$') {
                cleaned.append(c);
            }
        }

        return cleaned.toString();
    }

    /**
     * Capitalizes the first letter of a word and makes the rest lowercase.
     */
    private static String capitalize(final String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }

        return Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
    }

    /**
     * Ensures the identifier starts with a valid character and doesn't contain invalid sequences.
     */
    private static String ensureValidIdentifier(final String identifier) {
        if (identifier.isEmpty()) {
            return identifier;
        }

        String result = identifier;

        // Ensure it doesn't start with a digit
        if (Character.isDigit(result.charAt(0))) {
            result = "field" + capitalize(result);
        }

        return result;
    }

    /**
     * Avoids Java keywords by appending a suffix.
     */
    private static String avoidKeywords(final String identifier) {
        if (JAVA_KEYWORDS.contains(identifier.toLowerCase())) {
            return identifier + "Value";
        }
        return identifier;
    }
}