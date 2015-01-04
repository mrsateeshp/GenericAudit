package com.thoughtstream.audit.utils;

import com.thoughtstream.audit.anotation.AuditableEntity;
import com.thoughtstream.audit.anotation.AuditableField;
import com.thoughtstream.audit.anotation.AuditableId;
import com.thoughtstream.audit.anotation.AuditableValueObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.thoughtstream.audit.utils.SpringReflectionUtils.*;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * input validation is not performed for private methods. consider validating them when making them public.
 * @author Sateesh
 * @since 17/11/2014
 */

public class GenericAuditUtils {

    final static Logger logger = LoggerFactory.getLogger(GenericAuditUtils.class);

    private static final Set<Class<?>> WRAPPER_TYPES = getWrapperTypes();
    private static final Set<Class<?>> NUMERIC_TYPES = getNumeric();

    public static String getDataSnapshot(Object object) {
        Assert.notNull(object, "input can not be null.");

        if(!object.getClass().isAnnotationPresent(AuditableEntity.class)){
            logger.info("AuditableEntity not found in the passed in object [{}], hence throwing IllegalArgumentException", object);
            throw new IllegalArgumentException("object should be annotated with AuditableEntity");
        }

        String result = getEntityXml(null, object, false);

        logger.info("Generated data-snapshot is: {}", result);

        return result;
    }

    private static boolean isWrapperType(Class<?> clazz) {
        return WRAPPER_TYPES.contains(clazz);
    }

    private static boolean isNumeric(Class classI) {
        return NUMERIC_TYPES.contains(classI);
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

    private static Set<Class<?>> getNumeric() {
        Set<Class<?>> ret = new HashSet<Class<?>>();
        ret.add(Short.class);
        ret.add(Integer.class);
        ret.add(Long.class);
        ret.add(Float.class);
        ret.add(Double.class);

        return ret;
    }

    private static String getEntityType(Object object) {
        return object.getClass().getSimpleName();
    }

    private static String getEntityId(Object object) {
        for (Field field : object.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(AuditableId.class)) {

                return getField(field, object).toString();
            }
        }
        throw new RuntimeException("@AuditableId not found on " + object.getClass());
    }

    private static String getPrimitiveXML(String name, Object value) {
        return getPrimitiveXML(name, value.toString(), isNumeric(value.getClass()));
    }

    private static String getPrimitiveXML(String name, String value) {
        return getPrimitiveXML(name, value, false);
    }

    private static String getPrimitiveXML(String name, String value, boolean isNumeric) {
        if(isNumeric){
            return String.format("<primitive name=\"%s\" value=\"%s\" numeric=\"true\"/>", name, value);
        } else {
            return String.format("<primitive name=\"%s\" value=\"%s\"/>", name, value);
        }
    }

    private static String getCompositeXml(String fieldName, Object fieldObject) {
        if (fieldObject.getClass().isAnnotationPresent(AuditableEntity.class)) {
            return getEntityXml(fieldName, fieldObject, true);
        } else if (fieldObject.getClass().isAnnotationPresent(AuditableValueObject.class)) {
            return getValueObjectXml(fieldName, fieldObject);
        } else if(fieldObject instanceof Iterable){
            return getCollectionXml(fieldName, (Iterable)fieldObject);
        }else {
            //since the field value is not auditable, it takes toString of it.
            return getPrimitiveXML(fieldName, fieldObject);
        }
    }

    private static String beginXMLTag(String tagName, String nameValue){
        return "<"+tagName+" name=\"" + nameValue + "\">";
    }

    private static String endXMLTag(String tagName){
        return "</"+tagName+">";
    }

    private static String getEntityXml(String name, Object object, boolean shallow) {
        if (name == null) {
            name = getEntityType(object);
        }
        String result = beginXMLTag("entity",name);
        result = result + getPrimitiveXML("eId", getEntityId(object));
        result = result + getPrimitiveXML("eType", getEntityType(object));

        if (!shallow) {
            result = result + getXMLForFields(object);
        }
        return result + endXMLTag("entity");
    }

    private static String getCollectionXml(String name, Iterable iterableObject) {

        String result = beginXMLTag("collection",name);

        int count = 1;
        for(Object object : iterableObject){
            result = result+ getXMLForObject(count+"",object);
            count ++;
        }

        return result + endXMLTag("collection");
    }

    private static String getValueObjectXml(String name, Object object) {
        if (name == null) {
            name = getEntityType(object);
        }
        String result = beginXMLTag("valueObject",name);

        result = result + getXMLForFields(object);

        return result + endXMLTag("valueObject");
    }

    private static String getXMLForFields(Object object) {
        String result = "";

        for (Field field : object.getClass().getDeclaredFields()) {
            result = result + getXMLForField(field, object);
        }
        return result;
    }

    private static String getXMLForField(Field field, Object targetObject) {
        if (field.isAnnotationPresent(AuditableField.class)) {
            Object fieldObject = getField(field, targetObject);
            return getXMLForObject(field, fieldObject);
        } else {
            return "";
        }
    }

    private static String getXMLForObject(Field field, Object fieldObject) {
        return getXMLForObject(field.getName(), field.getType().isPrimitive(), fieldObject);
    }

    private static String getXMLForObject(String fieldName, Object fieldObject) {
        return getXMLForObject(fieldName, false, fieldObject);
    }

    private static String getXMLForObject(String fieldName, boolean isFieldPrimitive, Object fieldObject) {
        if (fieldObject == null) {
            return getPrimitiveXML(fieldName, "");
        } else {
            if (isFieldPrimitive || isWrapperType(fieldObject.getClass())) {
                return getPrimitiveXML(fieldName, fieldObject);
            } else {
                return getCompositeXml(fieldName, fieldObject);
            }
        }
    }
}
