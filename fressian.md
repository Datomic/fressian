## fressian

Fressian is an extensible binary data notation. Fressian is used by
Datomic and other applications as a data transfer format. This spec
describes Fressian in isolation from those and other specific use
cases, to help facilitate implementation of readers and writers in
other languages, and for other uses.

Fressian supports a rich set of built-in elements, and the definition
of extension elements in terms of the others. Users of data formats
without such facilities must rely on either convention or context to
convey elements not included in the base set. This greatly complicates
application logic, betraying the apparent simplicity of the
format. Fressian is simple, yet powerful enough to meet the demands of
applications without convention or complex context-sensitive logic.

Fressian is a counterpart to
[edn](https://github.com/edn-format/edn), and shares many of the same
design objects. The key additional objective that separates Fressian
from edn is efficiency. To that end Fressian:

* is a binary format
* directly supports platform primitive types
* directly supports platform arrays
* enables inline caching

## General considerations

Fressian is a byte code specification. ...

## Packed representations

    TODO describe packing

## Caching

    TODO describe, very similar to struct definitions below,
         as structs are implicitly autocached

## Extensibility

Fressian writers can define new struct types on the fly. A Struct
definition consists of the `STRUCT_TYPE` byte code, followed be the
struct tag (a string uniquely naming the struct), followed by the
number of fields in the struct (an integer), followed by the fields

When it sees a struct for the first time, the serialization library
assigns it a byte code representation. The first sixteen structs
encountered get single byte codes starting at
`STRUCT_CACHE_PACKED_START`. Subsequent structs are encoded as the
bytecode `STRUCT`, plus the ordinal number of the struct's first
appearance.

Once a struct has been assigned a byte code representation, that
representation is used in subsequent writes instead of the
type+tag+fieldcount.

Because the structs encode a fieldcount, naive readers can read
structs they have never seen before. If a reader does not have any
specific handler for a struct, it can represent the struct via the
following logical interface (shown in Java):

    public interface Tagged {
        public Object getTag();
        public Object getValue();
        public Map getMeta();
    }

## Grammar

    TODO grammar, might use format similar to 
    http://hessian.caucho.com/doc/hessian-serialization.html#anchor2

    should be able to grab from org.fressian.impl.Codes
