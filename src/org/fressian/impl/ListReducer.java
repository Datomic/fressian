package org.fressian.impl;

import org.fressian.handlers.IReduceList;

import java.util.ArrayList;
import java.util.List;

public class ListReducer implements IReduceList<List, List> {
    public List init(int length) {
        return new ArrayList<>(length);
    }

    public List step(List acc, Object item) {
        acc.add(item);
        return acc;
    }

    public List complete(List acc) {
        return acc;
    }
}
