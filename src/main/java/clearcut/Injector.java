package clearcut;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Constructor;

/** Injects dependencies */
public class Injector {
private Ini ini;

private static int MAX_DIRS_UP = 5;

public static Injector INJECTOR = new Injector();

public Injector() {
        load();
}

/** Find the app.ini file in this folder or above and load properties */
private synchronized void load() {
        try {
                File file = new File(".");
                String path = file.getAbsolutePath();
                if (!path.equals(File.separator)) {
                        int dirsUp = 0;
                        while (!(path == null) && !path.equals(File.separator)
                               && !readAppIniFile(path) && ++dirsUp < MAX_DIRS_UP) {
                                file = new File(path);
                                path = chop(path);
                        }
                }
        } catch (Exception x) {
                x.printStackTrace();
        }

}

public String iniPath() {
        return this.ini.path();
}

private String chop(String path) {
        if (path.equals(File.separator))
                return null;
        int pos = path.lastIndexOf(File.separatorChar);
        if (pos == -1)
                return null;
        return path.substring(0, pos);
}

private void setProperties(String path) throws IniException {
        if (this.ini == null) {
                this.ini = new Ini(path);
        }
}

private boolean readAppIniFile(String dir) throws IniException {
        String path = dir + File.separator + Ini.app();
        File file = new File(path);
        if (!file.exists())
                return false;
        else
                setProperties(path);
        return true;
}

@SuppressWarnings("unchecked")
public String fessUp() {
        String ret = null;
        for (String key : this.ini.keySet()) {
                Object value = this.ini.get(key);
                for (String[] entry : (List<String[]>)value)
                        ret += ("\n" + key + ": " + ((String[]) entry)[0] + "=" + ((String[]) entry)[1]);
        }
        return ret;
}

/**
 * Returns all the key-value pairs in a given section, eg. my_name=Jim in
 * section [profile]
 */
@SuppressWarnings("unchecked")
public List<String[]> properties(String section) throws InjectionException {
        Object obj = this.ini.get(section);
        if (obj == null)
                return null;
        if (obj instanceof List)
                return (List<String[]>)obj;
        else
                throw new InjectionException(
                              "Section "
                              + section
                              + " contains something which is not a list of key-value pairs");
}

/**
 * Keys in a section can have multiple entries, eg. logfile:a.log
 * logfile:b.log
 */
public List<String> properties(String section, String key)
throws InjectionException {
        List<String> list = new ArrayList<String>();
        List<String[]> properties = properties(section);
        for (String[] value : properties)
                if (value == null || value.length != 2)
                        throw new InjectionException(
                                      "Problem initializing ["
                                      + section
                                      + "] section from "
                                      + Ini.app()
                                      + ": not all entries in this section are key/value pairs");
                else if (value[0].equals(key))
                        list.add(value[1]);
        return list;
}

/**
 * A key-value pair under a section, eg. realMember=example.biz.Member under
 * [injection] in an ini file. See for example app.ini.
 */
public String property(String section, String key)
throws InjectionException {
        List<String[]> properties = properties(section);
        for (String[] value : properties)
                if (value == null || value.length != 2)
                        throw new InjectionException(
                                      "Problem initializing ["
                                      + section
                                      + "] section from "
                                      + Ini.app()
                                      + ": not all entries in this section are key/value pairs");
                else if (value[0].equals(key))
                        return value[1];
        return null;
}

/**
 * 'NO SECTION' properties are those at the top of the ini file above
 * any 'sections'. See for example app.ini.
 */
public String property(String key) throws InjectionException {
        return property(Ini.NO_SECTION, key);
}

/**
 * This method is the core of Injector. It returns objects constructed by utilising
 * constructors from the ini file -> see app.ini. 'section' is a section of the ini
 * file, e.g. everything under '[injection]'. 'key' is the left-hand of a key-value
 * pair within that section, e.g. in 'person=example.biz.Member' - key is 'person'.
 */
public Object implement(String section, String key)
throws InjectionException {
        if (key == null || key.trim().length() < 1)
                throw new InjectionException(
                              "Needs to be called with a word as its parameter, eg. a_person");
        List<String[]> properties = properties(section);
        if (properties == null || properties.size() < 1)
                throw new InjectionException("Section [" + section
                                             + "] was not found in " + Ini.app());

        String constructor = property(section, key);
        if (constructor == null || constructor.length() < 1)
                throw new InjectionException("Valid " + key
                                             + " implementation not found in " + Ini.app());

        Object result = tryToImplementWithConstructor(constructor, section, key);
        if (null != result) return result;

        throw new InjectionException("Constructor not found for " + constructor
                                     + " from " + Ini.app());
}

/** If implement() is called without a section, use the section [injection] in the ini file */
public Object implement(String key) throws InjectionException {
        return implement("injection", key);
}

private Object tryToImplementWithConstructor(String constructor, String section, String key)
throws InjectionException {
        String className = className(constructor);
        try {
                Object result = tryToImplementWithParametersOrDefault(constructor, section, key);
                if (null != result) return result;
        } catch (ClassNotFoundException c) {
                return implement(className); // recursion
        } catch (Exception x) {
                throw new InjectionException(x);
        }
        return null;
}

private String className(String constructor) {
        String className = new String(constructor);
        boolean hasParameters = constructor.indexOf('(') > -1;
        if (hasParameters) {
                className = constructor.substring(0, constructor.indexOf('('));
        }
        return className;
}

private Object tryToImplementWithParametersOrDefault(String constructor, String section, String key)
throws InjectionException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        Object[] parameters = null;
        String className = new String(constructor);
        boolean hasParameters = constructor.indexOf('(') > -1;
        if (hasParameters) {
                className = className(constructor);
                parameters = parameters(constructor);
                if (parameters.length < 1)
                        hasParameters = false;
        }

        Class[] classes = new Class[0];
        String forName = className.trim();
        Class cla$$ = Class.forName(forName);
        if (cla$$ == null)
                throw new InjectionException("" + key + " implementation "
                                             + constructor + " not found");

        if (!hasParameters) // Maybe it has a default constructor with no params
                return cla$$.newInstance();

        Constructor[] constructors = cla$$.getConstructors();
        Object result = tryToImplementWithConstructors(constructors, parameters, section);
        if (null != result) return result;
        return null;
}

private Object tryToImplementWithConstructors(Constructor[] constructors, Object[] parameters, String section)
throws InjectionException {
        try {
                for (Constructor con : constructors) {
                        Class[] classes = con.getParameterTypes();
                        if (classes.length == parameters.length) {
                                int matches = 0;
                                for (int i = 0; i < classes.length; i++) {
                                        Class cla$$ = classes[i];
                                        Object obj = parameters[i];
                                        try {
                                                parameters[matches++] = cast(section, cla$$, obj);
                                        } catch (InjectionCastException e) {
                                                // Don't use this constructor, try another...
                                        }
                                } // Same no. of params, same(ish) types... let's try it
                                if (matches == parameters.length) {
                                        try {
                                                return con.newInstance(parameters);
                                        } catch (IllegalArgumentException i) {
                                                // Oh well, we tried... let's try another constructor
                                        }
                                }
                        }
                }
        } catch(Exception x) {
                throw new InjectionException(x);
        }
        return null;
}

/**
 * object is the thing you are trying to put into a parameter list,
 * castInto is the type of the thing you are trying to cast it into, and
 * 'section' is a section of the ini file, e.g. everything under '[injection]'
 */
private Object cast(String section, Class castInto, Object object)
throws InjectionException {
        Class castFrom = object.getClass();
        String str = object.toString();
        if (castInto == castFrom)
                return object;
        if (StringBuffer.class == castFrom && str.equals("null"))
                return null;
        if (numeric(castInto, object)) {
                Boolean numeric = Numeric(str);
                if (numeric == null)
                        return Integer.parseInt(str);
                else
                        return Double.parseDouble(str);
        }
        try {
                if (StringBuffer.class == castFrom) {
                        // StringBuffer means something special - see method parameters()
                        try {
                                Object ca$t = implement(str); // implement() calls cast(), and vice-versa
                                return castInto.cast(ca$t);
                        } catch (InjectionException e) {
                                str = property(section, str);
                                if (str == null)
                                        throw e;
                                else
                                        return str;
                        }
                } else
                        return castInto.cast(object);
        } catch (ClassCastException x) {
                throw new InjectionCastException("Class cast exception casting a "
                                                 + castFrom.getName() + " into a " + castInto.getName());
        }

}

private boolean numeric(Class one, Object other) {
        Class two = other.getClass();

        return ((one == int.class || one == Integer.class
                 || one == double.class || one == Double.class
                 || one == float.class || one == Float.class
                 || one == long.class || one == Long.class || one == short.class
                 || one == Short.class || one == byte.class || one == Byte.class
                 || one == char.class || one == Character.class
                 || one == BigInteger.class || one == BigDecimal.class) &&
                ((two == String.class && (Numeric((String) other) == null || Numeric(
                                                  (String) other).booleanValue() == true)) || (two == int.class
                                                                                               || two == Integer.class || two == double.class
                                                                                               || two == Double.class || two == float.class
                                                                                               || two == Float.class || two == long.class || two == Long.class
                                                                                               || two == short.class || two == Short.class
                                                                                               || two == byte.class || two == Byte.class || two == char.class
                                                                                               || two == Character.class || two == BigInteger.class || two == BigDecimal.class)));
}

/**
 * Returns null if parameter is an integer, true if it is floating point,
 * and false otherwise
 */
private Boolean Numeric(String num) {
        try {
                Long.parseLong(num);
                return null;
        } catch (NumberFormatException e) {
                try {
                        Double.parseDouble(num);
                        return new Boolean(true);
                } catch (NumberFormatException x) {
                        return new Boolean(false);
                }
        }
}

public static char DOUBLEQUOTE = '"';
public static char QUOTE = '\'';
public static char UNDERSCORE = '_';
public static char COMMA = ',';

/**
 * Reads the parameters out of a constructor line from app.ini, eg.
 * 'org.foo.Bar("$", 42)'
 */
private Object[] parameters(String constructor) throws InjectionException {
        try {
                List<Object> params = new ArrayList<Object>();
                int pos = constructor.indexOf('(');
                int end = constructor.indexOf(')');
                if (pos < 0 || end < 0 || end < pos + 1)
                        throw new InjectionException(constructor + " from " + Ini.app()
                                                     + " does not have a nice pair of parentheses");

                StringBuffer currentWord = new StringBuffer();
                boolean inDoubleQuote = false;
                StringBuffer currentNumber = new StringBuffer();
                boolean inNumber = false;
                StringBuffer currentSomethingElse = new StringBuffer();
                boolean inQuote = false;
                while (pos++ < constructor.length() - 1) {
                        char ch = constructor.charAt(pos);
                        if (!inDoubleQuote && !inQuote) {
                                if (ch == ')') {
                                        if (currentWord.length() > 0)
                                                params.add(currentWord.toString());
                                        if (currentNumber.length() > 0) {
                                                String number = currentNumber.toString();
                                                if (number.indexOf('.') > -1)
                                                        params.add(Double.parseDouble(number));
                                                else
                                                        params.add(Long.parseLong(number));
                                        }
                                        if (currentSomethingElse.length() > 0) {
                                                String somethingElse = currentSomethingElse
                                                                       .toString();
                                                if (somethingElse.equals("true")
                                                    || somethingElse.equals("false"))
                                                        params.add(new Boolean(somethingElse));
                                                else
                                                        params.add(currentSomethingElse);
                                        }
                                        return params.toArray();
                                }
                                if (ch == DOUBLEQUOTE) {
                                        inDoubleQuote = true;
                                        continue;
                                } // "O'REILLY"
                                if (ch == QUOTE) {
                                        inQuote = true;
                                        continue;
                                }
                                if (!inNumber
                                    && (!Character.isLetterOrDigit(ch) && ch != UNDERSCORE)) {
                                        if (currentSomethingElse.length() > 0) {
                                                String somethingElse = currentSomethingElse
                                                                       .toString();
                                                if (somethingElse.equals("true")
                                                    || somethingElse.equals("false"))
                                                        params.add(new Boolean(somethingElse));
                                                else
                                                        params.add(currentSomethingElse);
                                                currentSomethingElse = new StringBuffer();
                                                continue;
                                        }
                                }
                                if (Character.isWhitespace(ch) || ch == COMMA) {
                                        if (inNumber) {
                                                String number = currentNumber.toString();
                                                if (number.indexOf(".") > -1)
                                                        params.add(Double.parseDouble(number));
                                                else
                                                        params.add(Long.parseLong(number));
                                                currentNumber = new StringBuffer();
                                                inNumber = false;
                                        }
                                        continue;
                                }
                        }

                        if (inDoubleQuote && ch == DOUBLEQUOTE) {
                                params.add(currentWord.toString());
                                currentWord = new StringBuffer();
                                inDoubleQuote = false;
                                continue;
                        }
                        if (inQuote && ch == QUOTE) {
                                params.add(currentWord.toString());
                                currentWord = new StringBuffer();
                                inQuote = false;
                                continue;
                        }
                        if (!inDoubleQuote && !inQuote
                            && currentSomethingElse.length() < 1
                            && "-1234567890.".indexOf(ch) > -1) {
                                if (inNumber && ch == '-'
                                    && currentNumber.indexOf("-") > -1)
                                        throw new InjectionException(
                                                      "Minus sign inside number in " + constructor
                                                      + " in " + Ini.app());
                                if (inNumber && ch == '.'
                                    && currentNumber.indexOf(".") > -1)
                                        throw new InjectionException("Dot inside number in "
                                                                     + constructor + " in " + Ini.app()); // TODO:
                                // make
                                // locale-friendly
                                inNumber = true;
                        }

                        if ((inDoubleQuote && ch != DOUBLEQUOTE)
                            || (inQuote && ch != QUOTE))
                                currentWord.append(ch);
                        else if (inNumber)
                                currentNumber.append(ch);
                        else
                                currentSomethingElse.append(ch);
                }

                if (inNumber)
                        throw new InjectionException("Unterminated constructor "
                                                     + constructor + " in " + Ini.app());
                if (inQuote)
                        throw new InjectionException("Unterminated quote in "
                                                     + constructor + " in " + Ini.app());
                if (inDoubleQuote)
                        throw new InjectionException("Unterminated double quote in "
                                                     + constructor + " in " + Ini.app());
                String err = "Error - parameters() should have returned before this point.\nCurrent parameters: ";
                for (Object obj : params)
                        err += obj.getClass().getName() + " " + obj.toString();
                throw new InjectionException(err);

        } catch (Exception e) {
                throw new InjectionException(constructor, e);
        }
}

}
