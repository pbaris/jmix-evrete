package gr.netmechanics.jmix.evrete.util;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration;
import com.github.javaparser.printer.configuration.PrinterConfiguration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Panos Bariamis (pbaris)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JavaFormatter {
    private static final PrinterConfiguration PRINTER_CONFIGURATION = new DefaultPrinterConfiguration();

    public static String format(String sourceCode) {
        return StaticJavaParser.parse(sourceCode).toString(PRINTER_CONFIGURATION);
    }
}
