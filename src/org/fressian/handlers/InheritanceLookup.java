// Copyright (c) Metadata Partners, LLC.
// All rights reserved.

package org.fressian.handlers;

import sun.io.CharToByteJIS0212_Solaris;

import java.util.*;

public class InheritanceLookup <V> implements ILookup <Class, V> {
    private final ILookup<Class, V> lookup;

    public InheritanceLookup(ILookup lookup) {
        this.lookup = lookup;
    }

    public V checkBaseClasses(Class c) {
        for (Class base = c.getSuperclass(); base != Object.class; base = base.getSuperclass()) {
            V val = lookup.valAt(base);
            if (val != null) return val;
        }
        return null;
    }

    public V checkBaseInterfaces(Class c) {
        Map<Class, V> possibles = new HashMap<Class,V>();
        for (Class base = c; base != Object.class; base = base.getSuperclass()) {
            for (Class itf : base.getInterfaces()) {
                V val = lookup.valAt(itf);
                if (val != null) possibles.put(itf, val);
            }
        }
        switch (possibles.size()) {
            case 0: return null;
            case 1: return possibles.values().iterator().next();
            default: throw new RuntimeException("More thane one match for " + c);
        }
    }

    public V valAt(Class c) {
        V val =  lookup.valAt(c);
        if (val == null) {
            val = checkBaseClasses(c);
        }
        if (val == null) {
            val = checkBaseInterfaces(c);
        }
        if (val == null) {
            val = lookup.valAt((Class) Object.class);
        }
        return val;
    }

    public static void main(String[] args) {
        Map m = new HashMap();
        m.put(CharSequence.class, "boo");
        ILookup ih = new InheritanceLookup(new MapLookup(m));
        System.out.println(ih.valAt(String.class));
    }
}
