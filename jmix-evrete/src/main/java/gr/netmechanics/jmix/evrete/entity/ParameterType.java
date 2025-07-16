/*
 * Copyright 2024 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gr.netmechanics.jmix.evrete.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import lombok.Getter;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
public enum ParameterType implements EnumClass<Integer> {

    BOOLEAN(10),
    NUMERIC(20),
    TEXT(30),
    DATE(40),
    TIME(50),
    DATETIME(60),
    ENUMERATION(70),
    ENTITY(90);

    private final Integer id;

    ParameterType(final Integer id) {
        this.id = id;
    }

    public static ParameterType fromId(final Integer id) {
        for (ParameterType type : ParameterType.values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return null;
    }
}
