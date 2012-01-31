package com.google.code.ssm.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * This annotation is necessary for methods that come from generic class or interface and at least one of the parameter
 * is generic. Because of type erasure in such case types of method's parameters have to be provided.
 * 
 * <pre>
 *     public interface Generic<K,V> {
 *       void set(K key, V value);
 *       V get(K key);
 *       void put(K key, V value, boolean overwrite);
 *     }
 *       
 *    @BridgeMethodMappings({
 *        @BridgeMethodMapping(methodName="set",erasedParamTypes={Object.class, Object.class}, targetParamTypes={Number.class, String.class}),
 *        @BridgeMethodMapping(methodName="get",erasedParamTypes={Object.class}, targetParamTypes={Number.class}),
 *        @BridgeMethodMapping(methodName="put",erasedParamTypes={Object.class, Object.class, boolean.class}, targetParamTypes={Number.class, String.class, boolean.class})
 *    })
 *    public class SubGeneric implements Generic<Number, String> {     
 *     public void set(Number key, String value) {
 *     ..... 
 *     }
 *       
 *     public String get(Number key) {
 *     .... 
 *     }
 * 
 *     public void put(Number key, String value, boolean overwrite) {
 *     .... 
 *     }
 * }
 * 
 * 
 * <pre/>
 * 
 * @author Jakub Białek
 * @since 2.0.0
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE })
public @interface BridgeMethodMapping {

    String methodName();

    Class<?>[] erasedParamTypes();

    Class<?>[] targetParamTypes();

}
