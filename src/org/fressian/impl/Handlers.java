//   Copyright (c) Metadata Partners, LLC. All rights reserved.
//   The use and distribution terms for this software are covered by the
//   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
//   which can be found in the file epl-v10.html at the root of this distribution.
//   By using this software in any fashion, you are agreeing to be bound by
//   the terms of this license.
//   You must not remove this notice, or any other, from this software.

package org.fressian.impl;

import org.fressian.*;
import org.fressian.handlers.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Pattern;

import static org.fressian.impl.Fns.*;

public class Handlers {
    public static final Map<String, Integer> tagToCode;
    public static final Map extendedReadHandlers;
    public static final ILookup<Class, Map<String, WriteHandler>> coreWriteHandlers;
    public static final ILookup<Class, Map<String, WriteHandler>> extendedWriteHandlers;

    static {
        HashMap<Class, Map<String, WriteHandler>> handlers = new HashMap();
        WriteHandler intHandler = new WriteHandler() {
            public void write(Writer w, Object instance) throws IOException {
                w.writeInt(instance);
            }
        };
        installHandler(handlers, Short.class, "int", intHandler);
        installHandler(handlers, Integer.class, "int", intHandler);
        installHandler(handlers, Long.class, "int", intHandler);

        installHandler(handlers, Boolean.class, "bool", new WriteHandler() {
            public void write(Writer w, Object instance) throws IOException {
                w.writeBoolean(instance);
            }
        });

        installHandler(handlers, (new byte[0]).getClass(), "bytes", new WriteHandler() {
            public void write(Writer w, Object instance) throws IOException {
                byte[] bytes = (byte[]) instance;
                w.writeBytes(bytes);
            }
        });

        installHandler(handlers, Double.class, "double", new WriteHandler() {
            public void write(Writer w, Object instance) throws IOException {
                w.writeDouble(instance);
            }
        });

        installHandler(handlers, Float.class, "float", new WriteHandler() {
            public void write(Writer w, Object instance) throws IOException {
                w.writeFloat(instance);
            }
        });

        installHandler(handlers, CharSequence.class, "string", new WriteHandler() {
            public void write(Writer w, Object instance) throws IOException {
                w.writeString(instance);
            }
        });

        installHandler(handlers, null, "null", new WriteHandler() {
            public void write(Writer w, Object instance) throws IOException {
                w.writeNull();
            }
        });

        installHandler(handlers, (new int[]{}).getClass(), "int[]", new WriteHandler() {
            public void write(Writer w, Object instance) throws IOException {
                int[] ints = (int[]) instance;
                w.writeTag("int[]", 2);
                w.writeInt(ints.length);
                for (int n = 0; n < ints.length; n++) {
                    w.writeInt(ints[n]);
                }
            }
        });

        installHandler(handlers, (new long[]{}).getClass(), "long[]", new WriteHandler() {
            public void write(Writer w, Object instance) throws IOException {
                long[] longs = (long[]) instance;
                w.writeTag("long[]", 2);
                w.writeInt(longs.length);
                for (int n = 0; n < longs.length; n++) {
                    w.writeInt(longs[n]);
                }
            }
        });

        installHandler(handlers, (new float[]{}).getClass(), "float[]", new WriteHandler() {
            public void write(Writer w, Object instance) throws IOException {
                float[] floats = (float[]) instance;
                w.writeTag("float[]", 2);
                w.writeInt(floats.length);
                for (int n = 0; n < floats.length; n++) {
                    w.writeFloat(floats[n]);
                }
            }
        });

        installHandler(handlers, (new boolean[]{}).getClass(), "boolean[]", new WriteHandler() {
            public void write(Writer w, Object instance) throws IOException {
                boolean[] booleans = (boolean[]) instance;
                w.writeTag("boolean[]", 2);
                w.writeInt(booleans.length);
                for (int n = 0; n < booleans.length; n++) {
                    w.writeBoolean(booleans[n]);
                }
            }
        });

        installHandler(handlers, (new double[]{}).getClass(), "double[]", new WriteHandler() {
            public void write(Writer w, Object instance) throws IOException {
                double[] doubles = (double[]) instance;
                w.writeTag("double[]", 2);
                w.writeInt(doubles.length);
                for (int n = 0; n < doubles.length; n++) {
                    w.writeDouble(doubles[n]);
                }
            }
        });

        installHandler(handlers, (new Object[]{}).getClass(), "Object[]", new WriteHandler() {
            public void write(Writer w, Object instance) throws IOException {
                Object[] objects = (Object[]) instance;
                w.writeTag("Object[]", 2);
                w.writeInt(objects.length);
                for (int n = 0; n < objects.length; n++) {
                    w.writeObject(objects[n]);
                }
            }
        });

        installHandler(handlers, TaggedObject.class, "any", new WriteHandler() {
            public void write(Writer w, Object instance) throws IOException {
                TaggedObject t = (TaggedObject) instance;
                Object[] value = t.getValue();
                w.writeTag(t.getTag(), value.length);
                for (int n = 0; n < value.length; n++) {
                    w.writeObject(value[n]);
                }
            }
        });


        coreWriteHandlers = new InheritanceLookup(new MapLookup(handlers));
    }


    static {
        HashMap builder = new HashMap();
        builder.put("map", Codes.MAP);
        builder.put("set", Codes.SET);
        builder.put("uuid", Codes.UUID);
        builder.put("regex", Codes.REGEX);
        builder.put("uri", Codes.URI);
        builder.put("bigint", Codes.BIGINT);
        builder.put("bigdec", Codes.BIGDEC);
        builder.put("inst", Codes.INST);
        builder.put("sym", Codes.SYM);
        builder.put("key", Codes.KEY);
        builder.put("int[]", Codes.INT_ARRAY);
        builder.put("float[]", Codes.FLOAT_ARRAY);
        builder.put("double[]", Codes.DOUBLE_ARRAY);
        builder.put("long[]", Codes.LONG_ARRAY);
        builder.put("boolean[]", Codes.BOOLEAN_ARRAY);
        builder.put("Object[]", Codes.OBJECT_ARRAY);
        tagToCode = Collections.unmodifiableMap(builder);
    }

    public static Map<Class, Map<String, WriteHandler>> installHandler(Map<Class, Map<String, WriteHandler>> map, Class cls, String tag, WriteHandler handler) {
        map.put(cls, soloMap(tag, handler));
        return map;
    }

    static {
        HashMap handlers = new HashMap();
        handlers.put("set", new ReadHandler() {
            public Object read(Reader r, Object tag, int componentCount) throws IOException {
                Set s = new HashSet();
                s.addAll((List) r.readObject());
                return s;
            }
        });

        handlers.put("map", new ReadHandler() {
            public Object read(Reader r, Object tag, int componentCount) throws IOException {
                Map result = new HashMap();
                List kvs = (List) (RandomAccess) r.readObject();
                for (int i = 0; i < kvs.size(); i += 2) {
                    result.put(kvs.get(i), kvs.get(i + 1));
                }
                return result;
            }
        });

        handlers.put("int[]", new ReadHandler() {
            public Object read(Reader r, Object tag, int componentCount) throws IOException {
                int size = intCast(r.readInt());
                int[] result = new int[size];
                for (int n = 0; n < size; n++) {
                    result[n] = intCast(r.readInt());
                }
                return result;
            }
        });

        handlers.put("long[]", new ReadHandler() {
            public Object read(Reader r, Object tag, int componentCount) throws IOException {
                int size = intCast(r.readInt());
                long[] result = new long[size];
                for (int n = 0; n < size; n++) {
                    result[n] = r.readInt();
                }
                return result;
            }
        });

        handlers.put("float[]", new ReadHandler() {
            public Object read(Reader r, Object tag, int componentCount) throws IOException {
                int size = intCast(r.readInt());
                float[] result = new float[size];
                for (int n = 0; n < size; n++) {
                    result[n] = r.readFloat();
                }
                return result;
            }
        });

        handlers.put("boolean[]", new ReadHandler() {
            public Object read(Reader r, Object tag, int componentCount) throws IOException {
                int size = intCast(r.readInt());
                boolean[] result = new boolean[size];
                for (int n = 0; n < size; n++) {
                    result[n] = r.readBoolean();
                }
                return result;
            }
        });

        handlers.put("double[]", new ReadHandler() {
            public Object read(Reader r, Object tag, int componentCount) throws IOException {
                int size = intCast(r.readInt());
                double[] result = new double[size];
                for (int n = 0; n < size; n++) {
                    result[n] = r.readDouble();
                }
                return result;
            }
        });


        handlers.put("Object[]", new ReadHandler() {
            public Object read(Reader r, Object tag, int componentCount) throws IOException {
                int size = intCast(r.readInt());
                Object[] result = new Object[size];
                for (int n = 0; n < size; n++) {
                    result[n] = r.readObject();
                }
                return result;
            }
        });

        handlers.put("uuid", new ReadHandler() {
            public Object read(Reader r, Object tag, int componentCount) throws IOException {
                byte[] buf = (byte[]) r.readObject();
                if (buf.length != 16) throw new IOException("Invalid uuid buffer size: " + buf.length);
                return byteArrayToUUID(buf);
            }
        });

        handlers.put("regex", new ReadHandler() {
            public Object read(Reader r, Object tag, int componentCount) throws IOException {
                return Pattern.compile((String) r.readObject());
            }
        });

        handlers.put("uri", new ReadHandler() {
            public Object read(Reader r, Object tag, int componentCount) throws IOException {
                try {
                    return new URI((String) r.readObject());
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        handlers.put("bigint", new ReadHandler() {
            public Object read(Reader r, Object tag, int componentCount) throws IOException {
                return new BigInteger((byte[]) r.readObject());
            }
        });

        handlers.put("bigdec", new ReadHandler() {
            public Object read(Reader r, Object tag, int componentCount) throws IOException {
                return new BigDecimal(new BigInteger((byte[]) r.readObject()), (int) r.readInt());
            }
        });

        handlers.put("inst", new ReadHandler() {
            public Object read(Reader r, Object tag, int componentCount) throws IOException {
                return new Date(r.readInt());
            }
        });

        extendedReadHandlers = Collections.unmodifiableMap(handlers);
    }

    static {
        Map handlers = new HashMap();
        installHandler(handlers, List.class, "list", new WriteHandler() {
            public void write(Writer w, Object instance) throws IOException {
                w.writeList(instance);
            }
        });

        installHandler(handlers, Date.class, "inst",
                new WriteHandler() {
                    public void write(Writer w, Object instance) throws IOException {
                        w.writeTag("inst", 1);
                        w.writeInt(((Date) instance).getTime());
                    }
                });

        installHandler(handlers, Set.class, "set",
                new WriteHandler() {
                    public void write(Writer w, Object instance) throws IOException {
                        w.writeTag("set", 1);
                        w.writeList(instance);
                    }
                });

        installHandler(handlers, Map.class, "map", new WriteHandler() {
            public void write(Writer w, Object instance) throws IOException {
                w.writeTag("map", 1);
                Map<Object, Object> m = (Map) instance;
                List l = new ArrayList();
                for (Map.Entry e : m.entrySet()) {
                    l.add(e.getKey());
                    l.add(e.getValue());
                }
                w.writeList(l);
            }
        });

        installHandler(handlers, BigInteger.class, "bigint",
                new WriteHandler() {
                    public void write(Writer w, Object instance) throws IOException {
                        BigInteger b = (BigInteger) instance;
                        w.writeTag("bigint", 1);
                        w.writeBytes(b.toByteArray());
                    }
                });

        installHandler(handlers, BigDecimal.class, "bigdec",
                new WriteHandler() {
                    public void write(Writer w, Object instance) throws IOException {
                        BigDecimal d = (BigDecimal) instance;
                        w.writeTag("bigdec", 2);
                        w.writeBytes(d.unscaledValue().toByteArray());
                        w.writeInt(d.scale());
                    }
                });

        installHandler(handlers, Pattern.class, "regex",
                new WriteHandler() {
                    public void write(Writer w, Object instance) throws IOException {
                        Pattern re = (Pattern) instance;
                        w.writeTag("regex", 1);
                        w.writeString(re.pattern());
                    }
                });

        installHandler(handlers, URI.class, "uri",
                new WriteHandler() {
                    public void write(Writer w, Object instance) throws IOException {
                        URI uri = (URI) instance;
                        w.writeTag("uri", 1);
                        w.writeString(uri.toString());
                    }
                });

        installHandler(handlers, UUID.class, "uuid",
                new WriteHandler() {
                    public void write(Writer w, Object instance) throws IOException {
                        UUID uuid = (UUID) instance;
                        w.writeTag("uuid", 1);
                        w.writeBytes(UUIDtoByteArray(uuid));
                    }
                });

        extendedWriteHandlers = new InheritanceLookup(new MapLookup(Collections.unmodifiableMap(handlers)));

    }

    public static ILookup<Class, Map<String, WriteHandler>> defaultWriteHandlers() {
        return new CachingLookup(new ChainedLookup(coreWriteHandlers, extendedWriteHandlers));
    }

    public static ILookup<Class, Map<String, WriteHandler>>
    customWriteHandlers(ILookup<Class, Map<String, WriteHandler>> userHandlers) {
        if (userHandlers != null) {
            return new CachingLookup(new ChainedLookup(coreWriteHandlers, userHandlers, extendedWriteHandlers));
        } else {
            return defaultWriteHandlers();
        }
    }
}
