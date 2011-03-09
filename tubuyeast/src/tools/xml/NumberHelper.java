/*******************************************************************************
 * NumberHelper.java
 * <p>
 * Created on 5-Feb-2005
 * <p>
 * Created by tedmunds
 ******************************************************************************/
package tools.xml;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.w3c.dom.Element;

/**
 * This helper class helps with type-agnostic handling of
 * {@link java.lang.Number}subclasses.
 * 
 * @author tedmunds
 */
public class NumberHelper
{
    /**
     * This class is not intended for instanciation. Hence the
     * <code>private</code> default constructor.
     */
    private NumberHelper()
    {
        // Do nothing
    }

    /**
     * Loads the desired number class from the specified attribute of the
     * provided element. Note that this is not intended for discovery; you
     * should already know the type parameter <code>&lt;N&gt;</code> that you
     * wish to use. This method is provided for situations where although the
     * type is known, you need the type's class in order to create instances.
     * 
     * @param <N>
     *        the type that you are expecting to find as the class
     * @param dataElement
     *        the element from which to load the class attribute
     * @param classAttributeName
     * @param loaderClass
     *        the class attempting to do the loading
     * @return the loaded class
     * @throws XMLException
     *         if the class attribute cannot be loaded as a class
     * @throws InitializationException
     *         if the class attribute contains clearly invalid data
     */
    @SuppressWarnings("unchecked")
    public static <N extends Number> Class<N> loadNumberClass(Element dataElement,
                                                              String classAttributeName,
                                                              Class loaderClass)
        throws XMLException,
            InitializationException
    {
        Class unknownClass = XMLHelper.getClassAttribute(dataElement,
                                                         classAttributeName,
                                                         loaderClass);
        if (!Number.class.isAssignableFrom(unknownClass))
        {
            throw new InitializationException("Invalid data.  The "
                                              + classAttributeName
                                              + "attribute of a "
                                              + loaderClass
                                              + " element must be a subclass of"
                                              + " java.lang.Number (actually,"
                                              + " it must be the correct"
                                              + " subclass, but we can't check"
                                              + " for that).");
        }
        // This cast is still dangerous, in that you could have a number
        // sub-class that does not match the N type parameter
        return unknownClass;
    }

    /**
     * Casts the provided value from a double to an instance of the specified
     * <code>Number</code> class. This may involve rounding. Note that
     * <code>numberClass</code> must be one of the known sub-classes of
     * {@link Number}. If <code>numberClass</code> is some other sub-class of
     * <code>Number</code>, <code>null</code> will be returned.
     * 
     * @param <N>
     *        the type of <code>Number</code> desired
     * @param value
     * @param numberClass
     *        the class that will be used (indirectly) to cast
     *        <code>value</code> to an instance of type <code>N</code>
     * @return the result of casting <code>value</code> to an instance of
     *         <code>N</code>
     */
    public static <N extends Number> N cast(double value, Class<N> numberClass)
    {
        if (numberClass == AtomicInteger.class)
        {
            return numberClass.cast(new AtomicInteger((int)value));
        }
        if (numberClass == AtomicLong.class)
        {
            return numberClass.cast(new AtomicLong((long)value));
        }
        if (numberClass == BigDecimal.class)
        {
            return numberClass.cast(new BigDecimal(value));
        }
        if (numberClass == BigInteger.class)
        {
            return numberClass.cast(new BigDecimal(value).toBigInteger());
        }
        if (numberClass == Byte.class || numberClass == Byte.TYPE)
        {
            return numberClass.cast((byte)value);
        }
        if (numberClass == Double.class || numberClass == Double.TYPE)
        {
            return numberClass.cast(value);
        }
        if (numberClass == Float.class || numberClass == Float.TYPE)
        {
            return numberClass.cast((float)value);
        }
        if (numberClass == Integer.class || numberClass == Integer.TYPE)
        {
            return numberClass.cast((int)value);
        }
        if (numberClass == Long.class || numberClass == Long.TYPE)
        {
            return numberClass.cast((long)value);
        }
        if (numberClass == Short.class || numberClass == Short.TYPE)
        {
            return numberClass.cast((short)value);
        }
        return null;
    }

    /**
     * Parses the provided string into a number of the specified class. Note
     * that <code>numberClass</code> must be one of the known sub-classes of
     * {@link Number}. If <code>numberClass</code> is some other sub-class of
     * <code>Number</code>, <code>null</code> will be returned.
     * 
     * @param <N>
     *        the type of number
     * @param numberString
     *        the string to be parsed
     * @param numberClass
     *        the class of the desired number type
     * @return the parsed number.
     * @throws NumberFormatException
     *         if the provided string does not contain a parsable number of the
     *         required type.
     */
    public static <N extends Number> N parse(String numberString,
                                             Class<N> numberClass)
    {
        if (numberClass == AtomicInteger.class)
        {
            return numberClass.cast(new AtomicInteger(Integer
                .parseInt(numberString)));
        }
        if (numberClass == AtomicLong.class)
        {
            return numberClass
                .cast(new AtomicLong(Long.parseLong(numberString)));
        }
        if (numberClass == BigDecimal.class)
        {
            return numberClass.cast(new BigDecimal(numberString));
        }
        if (numberClass == BigInteger.class)
        {
            return numberClass.cast(new BigInteger(numberString));
        }
        if (numberClass == Byte.class || numberClass == Byte.TYPE)
        {
            return numberClass.cast(Byte.parseByte(numberString));
        }
        if (numberClass == Double.class || numberClass == Double.TYPE)
        {
            return numberClass.cast(Double.parseDouble(numberString));
        }
        if (numberClass == Float.class || numberClass == Float.TYPE)
        {
            return numberClass.cast(Float.parseFloat(numberString));
        }
        if (numberClass == Integer.class || numberClass == Integer.TYPE)
        {
            return numberClass.cast(Integer.parseInt(numberString));
        }
        if (numberClass == Long.class || numberClass == Long.TYPE)
        {
            return numberClass.cast(Long.parseLong(numberString));
        }
        if (numberClass == Short.class || numberClass == Short.TYPE)
        {
            return numberClass.cast(Short.parseShort(numberString));
        }
        return null;
    }

    /**
     * Parses the provided string into a number of the specified class. Note
     * that the returned value will not necessarily be of the same class as the
     * example instance; the returned value will be one of the known sub-classes
     * of {@link Number}. If <code>exampleInstance</code> is a sub-class of
     * one of the known sub-classes, the returned value will be an instance of
     * that known sub-class; if <code>exampleInstance</code> is some other
     * sub-class of <code>Number</code>, <code>null</code> will be
     * returned.
     * 
     * @param <N>
     *        the type of number (one of the known sub-classes of
     *        <code>Number</code>
     * @param <S>
     *        the type of the example instance (a sub-type of <code>N</code>)
     * @param numberString
     *        the string to be parsed
     * @param exampleInstance
     *        an example of the desired number type
     * @return the parsed number.
     * @throws NumberFormatException
     *         if the provided string does not contain a parsable number of the
     *         required type.
     */
    @SuppressWarnings({"unchecked"}) 
    // NOTE: necessary casts... take care in adding to this function!
    public static <N extends Number, S extends N> N parse(String numberString,
                                                          S exampleInstance)
    {
        if (exampleInstance instanceof AtomicInteger)
        {
            // By the instanceof, we know that this is actually a safe cast
            return (N)new AtomicInteger(Integer.parseInt(numberString));
        }
        if (exampleInstance instanceof AtomicLong)
        {
            // See comment above
            return (N)new AtomicLong(Long.parseLong(numberString));
        }
        if (exampleInstance instanceof BigDecimal)
        {
            // See comment above
            return (N)new BigDecimal(numberString);
        }
        if (exampleInstance instanceof BigInteger)
        {
            // See comment above
            return (N)new BigInteger(numberString);
        }
        if (exampleInstance instanceof Byte)
        {
            // See comment above
            return (N)Byte.valueOf(numberString);
        }
        if (exampleInstance instanceof Double)
        {
            // See comment above
            return (N)Double.valueOf(numberString);
        }
        if (exampleInstance instanceof Float)
        {
            // See comment above
            return (N)Float.valueOf(numberString);
        }
        if (exampleInstance instanceof Integer)
        {
            // See comment above
            return (N)Integer.valueOf(numberString);
        }
        if (exampleInstance instanceof Long)
        {
            // See comment above
            return (N)Long.valueOf(numberString);
        }
        if (exampleInstance instanceof Short)
        {
            // See comment above
            return (N)Short.valueOf(numberString);
        }
        return null;
    }
}
