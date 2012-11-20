package org.fressian;

import org.fressian.handlers.ILookup;
import org.fressian.handlers.ReadHandler;
import org.fressian.handlers.WriteHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

public class Examples {
    public static class Person {
        public final String firstName;
        public final String lastName;
        public Person(String fn, String ln) {
            firstName = fn;
            lastName = ln;
        }
        public String toString() {
            return "Person " + firstName + " " + lastName;
        }
    }

    public static void main(String[] args) throws IOException {
        readAndWrite();
        writeCached();
        readAndWritePerson();
    }

    private static void readAndWritePerson() throws IOException {
        System.out.println("\nReading and writing a person object");
        byte[] packet = writePerson();

        System.out.println("\nReading without a handler: ");
        ByteArrayInputStream bais = new ByteArrayInputStream(packet);
        Reader reader = new FressianReader(bais);
        describe(reader.readObject());

        System.out.println("\nReading with a handler: ");
        final String tag = "org.fressian.Examples.Person";
        final ReadHandler handler = new ReadHandler() {
            public Object read(Reader r, Object tag, int componentCount) throws IOException {
                assert(componentCount == 2);
                return new Person((String)r.readObject(), (String)r.readObject());
            }
        };
        bais = new ByteArrayInputStream(packet);
        reader = new FressianReader(bais, new ILookup<Object, ReadHandler>() {
            public ReadHandler valAt(Object key) {
                if (key.equals(tag)) return handler; else return null;
            }
        });
        describe(reader.readObject());
    }

    private static byte[] writePerson() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final String tag = "org.fressian.Examples.Person";
        final WriteHandler handler = new WriteHandler() {

            public void write(Writer w, Object instance) throws IOException {
                w.writeTag(tag, 2);
                Person person = (Person) instance;
                w.writeObject(person.firstName);
                w.writeObject(person.lastName);
            }
        };
        Writer w = new FressianWriter(baos, new ILookup<Class, Map<String, WriteHandler>>() {
            public Map<String, WriteHandler> valAt(Class key) {
                return map(tag, handler);
            }
        });
        w.writeObject(new Person("John", "Doe"));
        return baos.toByteArray();
    }

    private static void writeCached() throws IOException {
        System.out.println("\nWriting cached data");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Writer w = new FressianWriter(baos);
        Set data = bunchOfData();
        w.writeObject(data, true);
        System.out.println("\tSerialized size of one bunch: " + baos.size());
        w.writeObject(data, true);
        System.out.println("\tSerialized size of two bunches: " + baos.size());
    }

    public static Set set(Object... stuff) {
        return Collections.unmodifiableSet(new java.util.HashSet(Arrays.asList(stuff)));
    }

    public static Map map(Object... keyvals) {
        if (keyvals == null) {
            return new HashMap();
        } else if (keyvals.length % 2 != 0) {
            throw new IllegalArgumentException("Map must have an even number of elements");
        } else {
            Map m = new HashMap(keyvals.length / 2);
            for (int i = 0; i < keyvals.length; i += 2) {
                m.put(keyvals[i], keyvals[i + 1]);
            }
            return Collections.unmodifiableMap(m);
        }
    }

    public static Set bunchOfData() {
        Date now = new Date();
        UUID unique = UUID.randomUUID();
        BigInteger big = new BigInteger("9999999999999999999999999999999");
        Set s = set(1, false, "hello", now, unique, big);
        return s;
    }

    private static void readAndWrite() throws IOException {
        System.out.println("\nReading and writing a heterogeneous set");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Writer w = new FressianWriter(baos);
        w.writeObject(bunchOfData());
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Reader r = new FressianReader(bais);
        describe(r.readObject());
    }

    private static void describe(Object o) {
        if (o instanceof Set) {
            System.out.println("Set:");
            Set s = (Set) o;
            for (java.util.Iterator it  = ((Set)o).iterator(); it.hasNext(); ) {
                System.out.println("\t" + it.next());
            }
        } else if (o instanceof Tagged) {
            System.out.println("Tagged object: ");
            Object[] components = (Object[])((Tagged)o).getValue();
            for (int i = 0; i < components.length; i++) {
                System.out.println("\t" + components[i]);
            }
        } else {
            System.out.println(o.getClass() + ": ");
            System.out.println(o);
        }
    }
}
