/*
 *  Copyleft (c) 2014 InterCoding Project
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *
 *  Author: Marco Antonio Pinto O. <pinto.marco@live.com>
 *  URL: https://github.com/marcoapintoo
 *  License: LGPL
 */
package org.pinto.intercoding.intermediate

import java.lang.reflect.Field
import java.lang.reflect.Method

class ClassInfo {
    Class<?> type
    private HashMap<String, Object> _beanProperties = null

    private ClassInfo(Class<?> type) {
        this.type = type
    }

    boolean isEnumeration() {
        //object.class.enum
        type.enum
    }

    String getSimpleName() {
        //String name = object.class.name
        String name = type.name
        name[(name.lastIndexOf(".") + 1)..(name.length() - 1)]
    }

    String getFullName() {
        //object.class.name
        type.name
    }

    HashMap<String, PropertyInfo> getBeanProperties() {
        if (_beanProperties != null) return _beanProperties
        _beanProperties = [:]
        //Field[] fields = type.fields.findAll {it.isAnnotationPresent(ModelMember.class)}
        //if(type.superclass!=null&&ModelType.class.isAssignableFrom(type.superclass)){
        if (type.superclass != null && BaseModel.class.isAssignableFrom(type.superclass)) {
            _beanProperties.putAll(ClassInfo.of(type.superclass).beanProperties)
        }
        Field[] fields = type.declaredFields
        for (field in fields) {
            //if(!field.isAnnotationPresent(ModelMember.class)) continue
            String fieldName = field.name[0].toUpperCase() + field.name.substring(1)
            def fieldType = field.type
            def preffixGetter = fieldType == Boolean.class ? "is" : "get"
            def getter
            def setter
            try {
                getter = type.getMethod(preffixGetter + fieldName)
                //setter = type.getMethod("set" + fieldName, fieldType)
                setter = type.methods.find { it.name == "set" + fieldName }
            } catch (e) {
                //e.printStackTrace()
                continue
            }
            def o = new PropertyInfo(
                    name: field.name,
                    type: fieldType,
                    getter: getter,
                    setter: setter,
            )
            _beanProperties.put(field.name, o)
        }
        return _beanProperties
    }

    private static HashMap<Class<?>, ClassInfo> cache = [:];

    static ClassInfo of(Object object) {
        of(object.class)
    }

    static ClassInfo of(Class<?> type) {
        if (cache.containsKey(type))
            return cache.get(type)
        def analyzer = new ClassInfo(type)
        cache.put(type, analyzer)
        return analyzer
    }
}

class PropertyInfo {
    String name
    Class<?> type
    Method getter
    Method setter

    boolean isIterable() {
        type.array || (Collection.isAssignableFrom(type) && !String.isAssignableFrom(type))
    }

    class PropertyWrapper {
        private Object target
        private Method getter
        private Method setter

        boolean isIterable() {
            def type = getter.returnType
            type.array || (Collection.isAssignableFrom(type) && !String.isAssignableFrom(type))
        }

        Object getValue() {
            getter.invoke(target)
        }

        void setValue(Object value) {
            try {
                setter.invoke(target, value)
            } catch (IllegalArgumentException e) {
                System.err.println("I can't assign ${value.class} when ${getter.returnType} was expected.")
                throw e
            }
        }
    }

    def applyTo(Object o) {
        new PropertyWrapper(
                target: o,
                getter: getter,
                setter: setter,
        )
    }

}
