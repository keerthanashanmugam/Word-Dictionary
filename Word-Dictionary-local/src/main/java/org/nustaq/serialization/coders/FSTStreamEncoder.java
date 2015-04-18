/*
 * Copyright 2014 Ruediger Moeller.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nustaq.serialization.coders;

import org.nustaq.serialization.FSTClazzInfo;
import org.nustaq.serialization.FSTClazzNameRegistry;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTEncoder;
import org.nustaq.serialization.util.FSTOutputStream;
import org.nustaq.serialization.util.FSTUtil;

import java.io.IOException;
import java.io.OutputStream;

/**
 * serializes into a binary stream
 */
public class FSTStreamEncoder implements FSTEncoder {

    private final FSTConfiguration conf;
    
    private FSTClazzNameRegistry clnames;
    private FSTOutputStream buffout;
    private byte[] ascStringCache;

    public FSTStreamEncoder(FSTConfiguration conf) {
        this.conf = conf;
        clnames = (FSTClazzNameRegistry) conf.getCachedObject(FSTClazzNameRegistry.class);
        if ( clnames == null ) {
            clnames = new FSTClazzNameRegistry(conf.getClassRegistry(), conf);
        } else {
            clnames.clear();
        }
    }

    void writeFBooleanArr(boolean[] arr, int off, int len) throws IOException {
        for (int i = off; i < off+len; i++)
            writeFByte(arr[i] ? 1 : 0);
    }

    public void writeFFloatArr(float[] arr, int off, int len) throws IOException {
        for (int i = off; i < off+len; i++)
            writeFFloat(arr[i]);
    }

    public void writeFDoubleArr(double[] arr, int off, int len) throws IOException {
        for (int i = off; i < off+len; i++)
            writeFDouble(arr[i]);
    }

    public void writeFShortArr(short[] arr, int off, int len) throws IOException {
        for (int i = off; i < off+len; i++)
            writeFShort(arr[i]);
    }

    public void writeFCharArr(char[] arr, int off, int len) throws IOException {
        for (int i = off; i < off+len; i++)
            writeFChar(arr[i]);
    }

    void writeFIntArr(int[] arr, int off, int len) throws IOException {
        int byteLen = arr.length * 4;
        buffout.ensureFree(byteLen);
        byte buf[] = buffout.buf;
        int count = buffout.pos;
        for (int i = off; i < off+len; i++) {
            long anInt = arr[i];
            buf[count++] = (byte) (anInt >>> 0);
            buf[count++] = (byte) (anInt >>> 8);
            buf[count++] = (byte) (anInt >>> 16);
            buf[count++] = (byte) (anInt >>> 24);
        }
        buffout.pos+= byteLen;
    }

    // compressed version
    public void _writeFIntArr(int v[], int off, int len) throws IOException {
        final int free = 5 * len;
        buffout.ensureFree(free);
        final byte[] buf = buffout.buf;
        int count = buffout.pos;
        for (int i = off; i < off+len; i++) {
            final int anInt = v[i];
            if ( anInt > -127 && anInt <=127 ) {
                buffout.buf[count++] = (byte)anInt;
            } else
            if ( anInt >= Short.MIN_VALUE && anInt <= Short.MAX_VALUE ) {
                buf[count++] = -128;
                buf[count++] = (byte) ((anInt >>>  0) & 0xFF);
                buf[count++] = (byte) ((anInt >>> 8) & 0xFF);
            } else {
                buf[count++] = -127;
                buf[count++] = (byte) ((anInt >>>  0) & 0xFF);
                buf[count++] = (byte) ((anInt >>>  8) & 0xFF);
                buf[count++] = (byte) ((anInt >>> 16) & 0xFF);
                buf[count++] = (byte) ((anInt >>> 24) & 0xFF);
            }
        }
        buffout.pos = count;
    }

    void writeFLongArr(long[] arr, int off, int len) throws IOException {
        int byteLen = arr.length * 8;
        buffout.ensureFree(byteLen);
        byte buf[] = buffout.buf;
        int count = buffout.pos;
        for (int i = off; i < off+len; i++) {
            long anInt = arr[i];
            buf[count++] = (byte) (anInt >>> 0);
            buf[count++] = (byte) (anInt >>> 8);
            buf[count++] = (byte) (anInt >>> 16);
            buf[count++] = (byte) (anInt >>> 24);
            buf[count++] = (byte) (anInt >>> 32);
            buf[count++] = (byte) (anInt >>> 40);
            buf[count++] = (byte) (anInt >>> 48);
            buf[count++] = (byte) (anInt >>> 56);
        }
        buffout.pos+= byteLen;
    }

    /**
     * write prim array no len no tag
     *
     *
     * @param array
     * @throws IOException
     */
    public void writePrimitiveArray(Object array, int off, int len) throws IOException {
        Class<?> componentType = array.getClass().getComponentType();
        if ( componentType == byte.class ) {
            writeRawBytes((byte[]) array, off, len);
        } else
        if ( componentType == char.class ) {
            writeFCharArr((char[]) array, off, len);
        } else
        if ( componentType == short.class ) {
            writeFShortArr((short[]) array, off, len);
        } else
        if ( componentType == int.class ) {
            writeFIntArr((int[]) array, off, len);
        } else
        if ( componentType == double.class ) {
            writeFDoubleArr((double[]) array, off, len);
        } else
        if ( componentType == float.class ) {
            writeFFloatArr((float[]) array, off, len);
        } else
        if ( componentType == long.class ) {
            writeFLongArr((long[]) array, off, len);
        } else
        if ( componentType == boolean.class ) {
            writeFBooleanArr((boolean[]) array, off, len);
        } else {
            throw new RuntimeException("expected primitive array");
        }
    }
    
    
    /**
     * does not write length, just plain bytes
     *
     * @param array
     * @param length
     * @throws java.io.IOException
     */
    public void writeRawBytes(byte[] array, int start, int length) throws IOException {
        buffout.ensureFree(length);
        System.arraycopy(array, start, buffout.buf, buffout.pos, length);
        buffout.pos += length;
    }

    @Override
    public void writeStringUTF(String str) throws IOException {
        final int strlen = str.length();

        writeFInt(strlen);
        buffout.ensureFree(strlen*3);
        final byte[] bytearr = buffout.buf;
        int count = buffout.pos;
        for (int i=0; i<strlen; i++) {
            final char c = str.charAt(i);
            bytearr[count++] = (byte)c;
            if ( c >= 255) {
                bytearr[count-1] = (byte)255;
                bytearr[count++] = (byte) ((c >>> 0) & 0xFF);
                bytearr[count++] = (byte) ((c >>> 8) & 0xFF);
            }
        }
        buffout.pos = count;
    }
    
    /**
     * length < 127 !!!!!
     *
     * @param name
     * @throws java.io.IOException
     */
    void writeStringAsc(String name) throws IOException {
        int len = name.length();
        if ( len >= 127 ) {
            throw new RuntimeException("Ascii String too long");
        }
        writeFByte((byte) len);
        buffout.ensureFree(len);
        if (ascStringCache == null || ascStringCache.length < len)
            ascStringCache = new byte[len];
        name.getBytes(0, len, ascStringCache, 0);
        writeRawBytes(ascStringCache, 0, len);
    }

    @Override
    public void writeFShort(short c) throws IOException {
        if (c < 255 && c >= 0) {
            writeFByte(c);
        } else {
            writeFByte(255);
            writePlainShort(c);
        }
    }

    public void writeAttributeName(FSTClazzInfo.FSTFieldInfo subInfo) {
        
    }
    
    public boolean writeTag(byte tag, Object info, long somValue, Object toWrite) throws IOException {
        writeFByte(tag);
        return false;
    }

    @Override
    public void writeFChar(char c) throws IOException {
        // -128 = short byte, -127 == 4 byte
        if (c < 255 && c >= 0) {
            buffout.ensureFree(1);
            buffout.buf[buffout.pos++] = (byte) c;
        } else {
            buffout.ensureFree(3);
            byte[] buf = buffout.buf;
            int count = buffout.pos;
            buf[count++] = (byte) 255;
            buf[count++] = (byte) (c >>> 0);
            buf[count++] = (byte) (c >>> 8);
            buffout.pos += 3;
        }
    }

    @Override
    public final void writeFByte(int v) throws IOException {
        buffout.ensureFree(1);
        buffout.buf[buffout.pos++] = (byte) v;
    }

    @Override
    public void writeFInt(int anInt) throws IOException {
        // -128 = short byte, -127 == 4 byte
        if (anInt > -127 && anInt <= 127) {
            if (buffout.buf.length <= buffout.pos + 1) {
                buffout.ensureFree(1);
            }
            buffout.buf[buffout.pos++] = (byte) anInt;
        } else if (anInt >= Short.MIN_VALUE && anInt <= Short.MAX_VALUE) {
            ensureFree(3);
            byte[] buf = buffout.buf;
            int count = buffout.pos;
            buf[count++] = (byte) -128;
            buf[count++] = (byte) (anInt >>> 0);
            buf[count++] = (byte) (anInt >>> 8);
            buffout.pos = count;
        } else {
            buffout.ensureFree(5);
            byte[] buf = buffout.buf;
            int count = buffout.pos;
            buf[count++] = (byte) -127;
            buf[count++] = (byte) ((anInt >>> 0) & 0xFF);
            buf[count++] = (byte) ((anInt >>>  8) & 0xFF);
            buf[count++] = (byte) ((anInt >>> 16) & 0xFF);
            buf[count++] = (byte) ((anInt >>> 24) & 0xFF);
            buffout.pos = count;
        }
    }

    @Override
    public void writeFLong(long anInt) throws IOException {
// -128 = short byte, -127 == 4 byte
        if (anInt > -126 && anInt <= 127) {
            writeFByte((int) anInt);
        } else if (anInt >= Short.MIN_VALUE && anInt <= Short.MAX_VALUE) {
            ensureFree(3);
            byte[] buf = buffout.buf;
            int count = buffout.pos;
            buf[count++] = (byte) -128;
            buf[count++] = (byte) (anInt >>> 0);
            buf[count++] = (byte) (anInt >>> 8);
            buffout.pos = count;
        } else if (anInt >= Integer.MIN_VALUE && anInt <= Integer.MAX_VALUE) {
            buffout.ensureFree(5);
            byte[] buf = buffout.buf;
            int count = buffout.pos;
            buf[count++] = (byte) -127;
            buf[count++] = (byte) ((anInt >>> 0) & 0xFF);
            buf[count++] = (byte) ((anInt >>>  8) & 0xFF);
            buf[count++] = (byte) ((anInt >>> 16) & 0xFF);
            buf[count++] = (byte) ((anInt >>> 24) & 0xFF);
            buffout.pos = count;
        } else {
            buffout.ensureFree(9);
            byte[] buf = buffout.buf;
            int count = buffout.pos;
            buf[count++] = (byte) -126;
            buf[count++] = (byte) (anInt >>> 0);
            buf[count++] = (byte) (anInt >>> 8);
            buf[count++] = (byte) (anInt >>> 16);
            buf[count++] = (byte) (anInt >>> 24);
            buf[count++] = (byte) (anInt >>> 32);
            buf[count++] = (byte) (anInt >>> 40);
            buf[count++] = (byte) (anInt >>> 48);
            buf[count++] = (byte) (anInt >>> 56);
            buffout.pos = count;
        }
    }

    /**
     * Writes a 4 byte float.
     */
    @Override
    public void writeFFloat(float value) throws IOException {
        writePlainInt(Float.floatToIntBits(value));
    }

    @Override
    public void writeFDouble(double value) throws IOException {
        writePlainLong(Double.doubleToLongBits(value));
    }

    @Override
    public int getWritten() {
        return buffout.pos-buffout.getOff();
    }

    /**
     * close and flush to underlying stream if present. The stream is also closed
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        buffout.close();
        conf.returnObject(clnames);
    }

    @Override
    public void reset(byte[] out) {
        if (out==null) {
            buffout.reset();
        } else {
            buffout.reset(out);
        }
        clnames.clear();
    }

    @Override
    public void skip(int i) {
        buffout.pos+=i;
    }

    /**
     * used to write uncompressed int (guaranteed length = 4) at a (eventually recent) position
     * @param position
     * @param v
     */
    @Override
    public void writeInt32At(int position, int v) {
        buffout.buf[position] = (byte)  (v >>> 0);
        buffout.buf[position+1] = (byte) (v >>> 8);
        buffout.buf[position+2] = (byte) (v >>> 16);
        buffout.buf[position+3] = (byte) (v >>> 24);
    }

    /**
     * if output stream is null, just encode into a byte array 
     * @param outstream
     */
    @Override
    public void setOutstream(OutputStream outstream) {
        if ( buffout == null ) 
        {
            // try reuse
            buffout = (FSTOutputStream) conf.getCachedObject(FSTOutputStream.class);
            if ( buffout == null ) // if fail, alloc
                buffout = new FSTOutputStream(1000, outstream);
            else
                buffout.reset(); // reset resued fstoutput
        }
        if ( outstream == null )
            buffout.setOutstream(buffout);
        else
            buffout.setOutstream(outstream);
    }
    
    /**
     * writes current buffer to underlying output and resets buffer. 
     * @throws IOException
     */
    @Override
    public void flush() throws IOException {
        buffout.flush();
    }

    @Override
    public void ensureFree(int bytes) throws IOException {
        buffout.ensureFree(bytes);
    }

    @Override
    public byte[] getBuffer() {
        return buffout.buf;
    }

    public void registerClass(Class possible) {
        clnames.registerClass(possible);
    }

    @Override
    public final void writeClass(Class cl) {
        try {
            clnames.encodeClass(this,cl);
        } catch ( IOException e) {
            throw FSTUtil.rethrow(e);
        }
    }

    @Override
    public final void writeClass(FSTClazzInfo clInf) {
        try {
            clnames.encodeClass(this, clInf);
        } catch ( IOException e) {
            throw FSTUtil.rethrow(e);
        }
    }
    

    private void writePlainLong(long v) throws IOException {
        buffout.ensureFree(8);
        byte[] buf = buffout.buf;
        int count = buffout.pos;
        buf[count++] = (byte) (v >>> 0);
        buf[count++] = (byte) (v >>> 8);
        buf[count++] = (byte) (v >>> 16);
        buf[count++] = (byte) (v >>> 24);
        buf[count++] = (byte) (v >>> 32);
        buf[count++] = (byte) (v >>> 40);
        buf[count++] = (byte) (v >>> 48);
        buf[count++] = (byte) (v >>> 56);
        buffout.pos += 8;
    }

    private void writePlainShort(int v) throws IOException {
        buffout.ensureFree(2);
        byte[] buf = buffout.buf;
        int count = buffout.pos;
        buf[count++] = (byte) (v >>> 0);
        buf[count++] = (byte) (v >>> 8);
        buffout.pos += 2;
    }

    private void writePlainChar(int v) throws IOException {
        buffout.ensureFree(2);
        byte[] buf = buffout.buf;
        int count = buffout.pos;
        buf[count++] = (byte) (v >>> 0);
        buf[count++] = (byte) (v >>> 8);
        buffout.pos += 2;
    }

    private void writePlainInt(int v) throws IOException {
        buffout.ensureFree(4);
        byte[] buf = buffout.buf;
        int count = buffout.pos;
        buf[count++] = (byte) ((v >>> 0) & 0xFF);
        buf[count++] = (byte) ((v >>>  8) & 0xFF);
        buf[count++] = (byte) ((v >>> 16) & 0xFF);
        buf[count++] = (byte) ((v >>> 24) & 0xFF);
        buffout.pos = count;
    }

    public void externalEnd(FSTClazzInfo clz) {
    }

    @Override
    public boolean isWritingAttributes() {
        return false;
    }

    public boolean isPrimitiveArray(Object array, Class<?> componentType) {
        return componentType.isPrimitive();
    }

    public boolean isTagMultiDimSubArrays() {
        return false;
    }

    @Override
    public void writeVersionTag(int version) throws IOException {
        writeFByte(version);
    }

    @Override
    public boolean isByteArrayBased() {
        return true;
    }

}

