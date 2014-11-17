package com.thoughtstream.audit.utils;

import com.thoughtstream.audit.User;
import com.thoughtstream.audit.anotation.AuditableEntity;
import com.thoughtstream.audit.anotation.AuditableField;
import com.thoughtstream.audit.anotation.AuditableId;
import com.thoughtstream.audit.anotation.AuditableValueObject;

import static com.thoughtstream.audit.utils.SpringReflectionUtils.*;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Sateesh
 * @since 17/11/2014
 */
public class ReflectionUtils {
    private static final Set<Class<?>> WRAPPER_TYPES = getWrapperTypes();

    public static boolean isWrapperType(Class<?> clazz) {
        return WRAPPER_TYPES.contains(clazz);
    }

    private static Set<Class<?>> getWrapperTypes() {
        Set<Class<?>> ret = new HashSet<Class<?>>();
        ret.add(Boolean.class);
        ret.add(Character.class);
        ret.add(Byte.class);
        ret.add(Short.class);
        ret.add(Integer.class);
        ret.add(Long.class);
        ret.add(Float.class);
        ret.add(Double.class);
        ret.add(String.class);
        return ret;
    }

    public static String getEntityType(Object object) {
        return object.getClass().getSimpleName();
    }

    public static String getEntityId(Object object) {
        for (Field field : object.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(AuditableId.class)) {

                return getField(field, object).toString();
            }
        }
        throw new RuntimeException("@AuditableId not found on " + object.getClass());
    }


    public static String getEntityXml(Object object) {
        return getEntityXml(object, false, null);
    }

    public static String getEntityXml(Object object, boolean shallow, String name) {
        if (name == null) {
            name = getEntityType(object);
        }
        String result = "<entity name=\"" + name + "\">";
        result = result + "<primitive name=\"eId\" value=\"" + getEntityId(object) + "\"/>";
        result = result + "<primitive name=\"type\" value=\"" + getEntityType(object) + "\"/>";

        if (!shallow) {
            for (Field field : object.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(AuditableField.class)) {
                    Object fieldObject = getField(field, object);
                    if (fieldObject == null) {
                        result = result + String.format("<primitive name=\"%s\" value=\"%s\"/>", field.getName(), "");
                    } else {
                        if (field.getType().isPrimitive() || isWrapperType(fieldObject.getClass())) {
                            result = result + String.format("<primitive name=\"%s\" value=\"%s\"/>", field.getName(), fieldObject.toString());
                        } else {
                            result = result + getCompositeXml(field, fieldObject);
                        }
                    }
                }
            }
        }
        return result + "</entity>";
    }

    private static String getCompositeXml(Field field, Object fieldObject) {
        String result = "";
        if (fieldObject.getClass().isAnnotationPresent(AuditableEntity.class)) {
            result = result + getEntityXml(fieldObject, true, field.getName());
        } else if (fieldObject.getClass().isAnnotationPresent(AuditableValueObject.class)) {
            result = result + getValueObjectXml(fieldObject, field.getName());
        } else {
            result = result + getCompositeXml(field, fieldObject);
        }
        return result;
    }

    public static String getValueObjectXml(Object object, String name) {
        if (name == null) {
            name = getEntityType(object);
        }
        String result = "<valueObject name=\"" + name + "\">";

        for (Field field : object.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(AuditableField.class)) {
                Object fieldObject = getField(field, object);
                if (fieldObject == null) {
                    result = result + String.format("<primitive name=\"%s\" value=\"%s\"/>", field.getName(), "");
                } else {
                    if (field.getType().isPrimitive() || isWrapperType(fieldObject.getClass())) {
                        result = result + String.format("<primitive name=\"%s\" value=\"%s\"/>", field.getName(), fieldObject.toString());
                    } else {
                        //todo
                    }
                }
            }
        }
        return result + "</valueObject>";
    }

    public static void main(String[] args) throws Exception {
        System.out.println(getEntityXml(new User(123, "tony")));
//        System.out.println(User.class.getField("name").getType().isPrimitive());
    }
}
