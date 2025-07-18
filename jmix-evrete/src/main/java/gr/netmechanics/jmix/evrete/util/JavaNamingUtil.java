package gr.netmechanics.jmix.evrete.util;

import java.util.Set;
import java.util.TreeSet;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Panos Bariamis (pbaris)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JavaNamingUtil {

    private static final Set<String> JAVA_KEYWORDS = new TreeSet<>(Set.of(
        "abstract","assert","boolean","break","byte","case","catch","char","class","const",
        "continue","default","do","double","else","enum","extends","final","finally","float",
        "for","goto","if","implements","import","instanceof","int","interface","long","native",
        "new","package","private","protected","public","return","short","static","strictfp",
        "super","switch","synchronized","this","throw","throws","transient","try","void",
        "volatile","while","true","false","null"));

    /**
     * Convert arbitrary string to a valid Java package name:
     * - Lowercase only
     * - Remove invalid chars
     * - No Java keywords as parts
     */
    public static String getPackageName(String input) {
        String cleaned = input.toLowerCase().replaceAll("[^a-z0-9]", "");
        if (cleaned.isEmpty()) cleaned = "defaultpkg";
        if (JAVA_KEYWORDS.contains(cleaned)) cleaned = cleaned + "pkg";
        return cleaned;
    }

    /**
     * Convert arbitrary string to valid Java ClassName:
     * - PascalCase
     * - Remove invalid chars
     * - Prefix if starts with digit or keyword
     */
    public static String getClassName(String input) {
        String camel = toCamelCase(input, true);
        if (camel.isEmpty()) camel = "GeneratedClass";
        if (Character.isDigit(camel.charAt(0)) || JAVA_KEYWORDS.contains(camel.toLowerCase())) {
            camel = "Generated" + camel;
        }
        return camel;
    }

    /**
     * Convert arbitrary string to valid Java method name:
     * - camelCase
     * - Remove invalid chars
     * - Prefix if starts with digit or keyword
     */
    public static String getMethodName(String input) {
        String camel = toCamelCase(input, false);
        if (camel.isEmpty()) camel = "generatedMethod";
        if (Character.isDigit(camel.charAt(0)) || JAVA_KEYWORDS.contains(camel.toLowerCase())) {
            camel = "generated" + capitalizeFirstLetter(camel);
        }
        return camel;
    }

    /**
     * Same rules as method name for parameters
     */
    public static String getParameterName(String input) {
        return getMethodName(input);
    }

    private static String toCamelCase(String s, boolean startWithUpper) {
        StringBuilder sb = new StringBuilder();
        boolean capitalizeNext = startWithUpper;
        for (char c : s.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                if (capitalizeNext) {
                    sb.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    sb.append(Character.toLowerCase(c));
                }
            } else {
                capitalizeNext = true;
            }
        }
        return sb.toString();
    }

    private static String capitalizeFirstLetter(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
