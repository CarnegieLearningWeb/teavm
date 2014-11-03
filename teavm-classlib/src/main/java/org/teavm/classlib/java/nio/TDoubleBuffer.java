/*
 *  Copyright 2014 Alexey Andreev.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.teavm.classlib.java.nio;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public abstract class TDoubleBuffer extends TBuffer implements Comparable<TDoubleBuffer> {
    int start;
    double[] array;

    TDoubleBuffer(int start, int capacity, double[] array, int position, int limit) {
        super(capacity);
        this.start = start;
        this.array = array;
        this.position = position;
        this.limit = limit;
    }

    public static TDoubleBuffer allocate(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity is negative: " + capacity);
        }
        return new TDoubleBufferImpl(capacity);
    }

    public static TDoubleBuffer wrap(double[] array, int offset, int length) {
        return new TDoubleBufferImpl(0, array.length, array, offset, offset + length, false);
    }

    public static TDoubleBuffer wrap(double[] array) {
        return wrap(array, 0, array.length);
    }

    public abstract TDoubleBuffer slice();

    public abstract TDoubleBuffer duplicate();

    public abstract TDoubleBuffer asReadOnlyBuffer();

    public abstract double get();

    public abstract TDoubleBuffer put(double b);

    public abstract double get(int index);

    public abstract TDoubleBuffer put(int index, double b);

    public TDoubleBuffer get(double[] dst, int offset, int length) {
        if (offset < 0 || offset >= dst.length) {
            throw new IndexOutOfBoundsException("Offset " + offset + " is outside of range [0;" + dst.length + ")");
        }
        if (offset + length > dst.length) {
            throw new IndexOutOfBoundsException("The last double in dst " + (offset + length) + " is outside " +
                    "of array of size " + dst.length);
        }
        if (remaining() < length) {
            throw new TBufferUnderflowException();
        }
        if (length < 0) {
            throw new IndexOutOfBoundsException("Length " + length + " must be non-negative");
        }
        int pos = position + start;
        for (int i = 0; i < length; ++i) {
            dst[offset++] = array[pos++];
        }
        position += length;
        return this;
    }

    public TDoubleBuffer get(double[] dst) {
        return get(dst, 0, dst.length);
    }

    public TDoubleBuffer put(TDoubleBuffer src) {
        return put(src.array, src.start + src.position, src.remaining());
    }

    public TDoubleBuffer put(double[] src, int offset, int length) {
        if (isReadOnly()) {
            throw new TReadOnlyBufferException();
        }
        if (remaining() < length) {
            throw new TBufferOverflowException();
        }
        if (offset < 0 || offset >= src.length) {
            throw new IndexOutOfBoundsException("Offset " + offset + " is outside of range [0;" + src.length + ")");
        }
        if (offset + length > src.length) {
            throw new IndexOutOfBoundsException("The last double in src " + (offset + length) + " is outside " +
                    "of array of size " + src.length);
        }
        if (length < 0) {
            throw new IndexOutOfBoundsException("Length " + length + " must be non-negative");
        }
        int pos = position + start;
        for (int i = 0; i < length; ++i) {
            array[pos++] = src[offset++];
        }
        position += length;
        return this;
    }

    public final TDoubleBuffer put(double[] src) {
        return put(src, 0, src.length);
    }

    @Override
    public boolean hasArray() {
        return true;
    }

    @Override
    public final double[] array() {
        return array;
    }

    @Override
    public int arrayOffset() {
        return start;
    }

    public abstract TDoubleBuffer compact();

    @Override
    public abstract boolean isDirect();

    @Override
    public String toString() {
        return "[DoubleBuffer position=" + position + ", limit=" + limit + ", capacity=" + capacity + ", mark " +
                (mark >= 0 ? " at " + mark : " is not set") + "]";
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        int pos = position + start;
        for (int i = position; i < limit; ++i) {
            long e = Double.doubleToLongBits(array[pos++]);
            hashCode = 31 * hashCode + (int)e + (int)(e >>> 32);
        }
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TDoubleBuffer)) {
            return false;
        }
        TDoubleBuffer other = (TDoubleBuffer)obj;
        int sz = remaining();
        if (sz != other.remaining()) {
            return false;
        }
        int a = position + start;
        int b = other.position + other.start;
        for (int i = 0; i < sz; ++i) {
            if (array[a++] != other.array[b++]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int compareTo(TDoubleBuffer other) {
        if (this == other) {
            return 0;
        }
        int sz = Math.min(remaining(), other.remaining());
        int a = position + start;
        int b = other.position + other.start;
        for (int i = 0; i < sz; ++i) {
            int r = Double.compare(array[a++], other.array[b++]);
            if (r != 0) {
                return r;
            }
        }
        return Integer.compare(remaining(), other.remaining());
    }
}