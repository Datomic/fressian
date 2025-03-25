package org.fressian.handlers;

public interface IReduceList<A, R> extends IRead {
    public A init(int length);

    public A step(A acc, Object item);

    public R complete(A acc);
}