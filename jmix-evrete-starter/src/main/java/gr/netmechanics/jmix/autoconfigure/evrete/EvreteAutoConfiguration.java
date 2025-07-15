package gr.netmechanics.jmix.autoconfigure.evrete;

import gr.netmechanics.jmix.evrete.EvreteConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({EvreteConfiguration.class})
public class EvreteAutoConfiguration {
}

