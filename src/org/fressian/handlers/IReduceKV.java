package org.fressian.handlers;

public interface IReduceKV<A, R> extends IRead {
    public A init(int length);

    public A step(A acc, Object key, Object value);

    public R complete(A acc);
}