package gr.netmechanics.jmix.evrete.util;

import static gr.netmechanics.jmix.evrete.EvreteProperties.Mode.ANNOTATED;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import gr.netmechanics.jmix.evrete.EvreteProperties;
import gr.netmechanics.jmix.evrete.annotation.EvreteEntity;
import gr.netmechanics.jmix.evrete.annotation.EvreteProperty;
import io.jmix.core.MessageTools;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author Panos Bariamis (pbaris)
 */
@RequiredArgsConstructor
@Component("evrete_RulePropertyConditionsHelper")
public class RulePropertyConditionsHelper {

    private final Metadata metadata;
    private final MetadataTools metadataTools;
    private final MessageTools messageTools;
    private final EvreteProperties evreteProperties;

    private final Map<String, Collection<MetaProperty>> propertiesCache = new HashMap<>();

    public Map<String, String> getEntityMetaClasses() {
        Map<String, String> metaClassesItemssMap = new TreeMap<>();

        metadata.getSession().getClasses().stream()
            .filter(this::isEligibleEntity)
            .forEach(clazz -> metaClassesItemssMap.put(clazz.getName(), messageTools.getEntityCaption(clazz)));

        return metaClassesItemssMap;
    }

    public Collection<MetaProperty> getMetaProperties(final MetaClass metaClass) {
        return propertiesCache.computeIfAbsent(metaClass.getName(), k -> metaClass.getOwnProperties().stream()
            .filter(metaProperty -> isEligibleProperty(metaClass, metaProperty))
            .toList());
    }

    private boolean isEligibleEntity(final MetaClass metaClass) {
        if (metadataTools.isSystemLevel(metaClass)) {
            return false;
        }

        if (evreteProperties.getEntitySelectionMode() == ANNOTATED && !isEvreteEntity(metaClass)) {
            return false;
        }

        return !getMetaProperties(metaClass).isEmpty();
    }

    private boolean isEligibleProperty(final MetaClass metaClass, final MetaProperty metaProperty) {
        if (metadataTools.isSystemLevel(metaProperty) || metaProperty.getRange().getCardinality() != Range.Cardinality.NONE) {
            return false;
        }

        if (evreteProperties.getEntitySelectionMode() == ANNOTATED) {
            return !useAnnotatedPropertiesOnly(metaClass) || isEvreteProperty(metaProperty);
        }

        return true;

        //TODO do we need to support references to other entities
//                if (mp.getRange().getCardinality().isMany()) {
//                    try {
//                        String viewId = viewRegistry.getAvailableLookupViewId(mp.getRange().asClass());
//                        return viewRegistry.hasView(viewId);
//
//                    } catch (IllegalStateException e) {
//                        return false;
//                    }
//                }
    }

    private boolean isEvreteEntity(final MetaClass metaClass) {
        return metaClass != null && metaClass.getJavaClass().getAnnotation(EvreteEntity.class) != null;
    }

    private boolean isEvreteProperty(final MetaProperty metaProperty) {
        return metaProperty != null
               && metaProperty.getDeclaringClass() != null
               && metadataTools.isAnnotationPresent(metaProperty.getDeclaringClass(), metaProperty.getName(), EvreteProperty.class);
    }

    private boolean useAnnotatedPropertiesOnly(final MetaClass metaClass) {
        Objects.requireNonNull(metaClass, "metaClass is null");
        EvreteEntity annotation = metaClass.getJavaClass().getAnnotation(EvreteEntity.class);
        return annotation != null && annotation.annotatedPropertiesOnly();
    }
}
