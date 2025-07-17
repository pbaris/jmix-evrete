package gr.netmechanics.jmix.evrete.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.text.ParseException;

import io.jmix.core.DataManager;
import io.jmix.core.Entity;
import io.jmix.core.Id;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaProperty;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * @author Panos Bariamis (pbaris)
 */
@RequiredArgsConstructor
@Component("evrete_ObjectToStringConverter")
public class ObjectToStringConverter {

    private final DatatypeRegistry datatypeRegistry;
    private final MetadataTools metadataTools;
    private final DataManager dataManager;

    /**
     * Serialized the passed object to a string depending on its class type.
     *
     * @param object object to be serialized
     * @return the serialized object, or {@code null} if the passed object is {@code null}
     */
    @Nullable
    public String convertToString(@Nullable final Object object) {
        if (object == null) {
            return null;

        } else if (String.class.isAssignableFrom(object.getClass())) {
            return (String) object;

        } else if (EntityValues.isEntity(object)) {
            return String.valueOf(EntityValues.getId(object));
        }

        Datatype<?> datatype = datatypeRegistry.find(object.getClass());
        if (datatype != null) {
            return datatype.format(object);
        }

        return String.valueOf(object);
    }

    /**
     * Deserialized the passed string into an object of the specified {@code objectClass} type.
     *
     * @param objectClass  {@link Class JavaClass} for the deserialization result object
     * @param objectString the string to deserialize
     * @param <T>          the type of object to deserialize
     * @return the deserialized object of the required type
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T convertFromString(final Class<T> objectClass, @Nullable final String objectString) {
        if (objectString == null) {
            return null;

        } else if (String.class.isAssignableFrom(objectClass)) {
            return (T) objectString;

        } else if (Entity.class.isAssignableFrom(objectClass)) {
            MetaProperty idProperty = metadataTools.getPrimaryKeyProperty(objectClass);
            if (idProperty == null) {
                return null;
            }

            if (idProperty.getRange().isClass()) {
                throw new IllegalArgumentException("Unsupported composite primary key in [%s] with value [%s]"
                    .formatted(objectClass.getSimpleName(), objectString));
            }

            if (idProperty.getRange().isDatatype()) {
                Object idValue;
                try {
                    idValue = idProperty.getRange().asDatatype().parse(objectString);

                } catch (ParseException e) {
                    throw new IllegalArgumentException("Couldn't read id from [%s] with value [%s] and datatype [%s]"
                        .formatted(objectClass.getSimpleName(), objectString, idProperty.getRange().asDatatype()));
                }

                if (idValue != null) {
                    return dataManager.load(objectClass)
                        .id(Id.of(idValue, objectClass))
                        .optional()
                        .orElse(null);
                }
            }

            return null;
        }

        Datatype<T> datatype = datatypeRegistry.find(objectClass);
        if (datatype != null) {
            try {
                return datatype.parse(objectString);

            } catch (ParseException e) {
                throw new IllegalArgumentException("Couldn't read value [%s] with datatype [%s]"
                    .formatted(objectString, datatype));
            }
        }

        return convertFromStringUnresolved(objectClass, objectString);
    }

    protected <T> T convertFromStringUnresolved(final Class<T> parameterClass, final String objectString) {
        try {
            Constructor<T> constructor = ConstructorUtils.getAccessibleConstructor(parameterClass, String.class);
            if (constructor != null) {
                return constructor.newInstance(objectString);

            } else {
                Method valueOf = MethodUtils.getAccessibleMethod(parameterClass, "valueOf", String.class);
                if (valueOf != null) {
                    //noinspection unchecked
                    return ((T) valueOf.invoke(null, objectString));
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Couldn't read value [%s] with class [%s]"
                .formatted(objectString, parameterClass.getSimpleName()));
        }

        throw new IllegalArgumentException("Unable to deserialize [%s] with class [%s]"
            .formatted(objectString, parameterClass.getSimpleName()));
    }
}
