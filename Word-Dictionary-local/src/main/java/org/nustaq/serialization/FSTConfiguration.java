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
package org.nustaq.serialization;

import org.nustaq.offheap.bytez.onheap.HeapBytez;
import org.nustaq.offheap.structs.FSTStruct;
import org.nustaq.serialization.coders.*;
import org.nustaq.serialization.util.FSTInputStream;
import org.nustaq.serialization.util.FSTUtil;
import org.nustaq.serialization.serializers.*;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.io.*;
import java.lang.ref.SoftReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 18.11.12
 * Time: 20:41
 *
 * Holds a serialization configuration/metadata.
 * Reuse this class !!! construction is very expensive. (just keep static instances around or use thread locals)
 *
 */
public class FSTConfiguration {

    static enum ConfType {
        DEFAULT, UNSAFE, MINBIN
    }
    StreamCoderFactory streamCoderFactory = new StreamCoderFactory() {
        @Override
        public FSTEncoder createStreamEncoder() {
            return new FSTStreamEncoder(FSTConfiguration.this);
        }

        @Override
        public FSTDecoder createStreamDecoder() {
            return new FSTStreamDecoder(FSTConfiguration.this);
        }
    };

    ConfType type = ConfType.DEFAULT;
    FSTClazzInfoRegistry serializationInfoRegistry = new FSTClazzInfoRegistry(this);
    HashMap<Class,List<SoftReference>> cachedObjects = new HashMap<Class, List<SoftReference>>(97);
    FSTClazzNameRegistry classRegistry = new FSTClazzNameRegistry(null, this);
    boolean preferSpeed = false; // hint to prefer speed over size in case, currently ignored.
    boolean shareReferences = true;
    ClassLoader classLoader = getClass().getClassLoader();
    boolean forceSerializable = false; // serialize objects which are not instanceof serializable using default serialization scheme.
    FSTClassInstantiator instantiator = new FSTDefaultClassInstantiator();

    public boolean isForceClzInit() {
        return forceClzInit;
    }

    /**
     * always execute default fields init, even if no transients (so would get overwritten anyway)
     * required for lossy codecs (kson)
     *
     * @param forceClzInit
     * @return
     */
    public FSTConfiguration setForceClzInit(boolean forceClzInit) {
        this.forceClzInit = forceClzInit;
        return this;
    }

    public FSTClassInstantiator getInstantiator(Class clazz) {
        return instantiator;
    }

    public void setInstantiator(FSTClassInstantiator instantiator) {
        this.instantiator = instantiator;
    }

    boolean forceClzInit = false; // always execute default fields init, even if no transients

    /////////////////////////////////////
    // cross platform stuff only

    int cpAttrIdCount = 0;
    // contains symbol => full qualified name
    private HashMap<String, String> minbinNames = new HashMap<>();
    // may contain symbol => cached binary output
    private HashMap<String, byte[]> minBinNamesBytez = new HashMap<>();
    // contains full qualified name => symbol
    private HashMap<String, String> minbinNamesReverse = new HashMap<>();
    private boolean crossPlatform = false; // if true do not support writeObject/readObject etc.

    // non-final for testing
    public static boolean isAndroid = System.getProperty("java.runtime.name", "no").toLowerCase().contains("android");

    // end cross platform stuff only
    /////////////////////////////////////

    static AtomicBoolean conflock = new AtomicBoolean(false);
    static FSTConfiguration singleton;
    public static FSTConfiguration getDefaultConfiguration() {
        do { } while ( !conflock.compareAndSet(false, true) );
        try {
            if (singleton == null)
                singleton = createDefaultConfiguration();
            return singleton;
        } finally {
            conflock.set(false);
        }
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Warning: MinBin contains full metainformation (fieldnames,..), so its way slower than the other configs.
     * It should be used in case of cross language (e.g. java - javascript) serialization only.
     * Additionally you can read MinBin serialized streams without access to original classes.
     *
     * See MBPrinter on an example on how to read a MinBin stream without having access to
     * original classes. Useful for cross language serialization/long term archiving.
     *
     * Warning: MinBin serialization ('binary JSon') is much slower than the other
     * serialization configurations.
     *
     * @return a configuration to encode MinBin format.
     */
    public static FSTConfiguration createMinBinConfiguration() {
        final FSTConfiguration res = createDefaultConfiguration();
        res.setCrossPlatform(true);
        res.type = ConfType.MINBIN;
        res.setStreamCoderFactory(new StreamCoderFactory() {
            @Override
            public FSTEncoder createStreamEncoder() {
                return new FSTMinBinEncoder(res);
            }

            @Override
            public FSTDecoder createStreamDecoder() {
                return new FSTMinBinDecoder(res);
            }
        });

        // override some serializers
        FSTSerializerRegistry reg = res.serializationInfoRegistry.serializerRegistry;
        reg.putSerializer(EnumSet.class, new FSTCPEnumSetSerializer(), true);
        reg.putSerializer(Throwable.class, new FSTCPThrowableSerializer(), true);

        // for crossplatform fallback does not work => register default serializers for collections and subclasses
        reg.putSerializer(AbstractCollection.class, new FSTCollectionSerializer(), true); // subclass should register manually
        reg.putSerializer(AbstractMap.class, new FSTMapSerializer(), true); // subclass should register manually

        res.registerCrossPlatformClassMapping(new String[][]{
                {"map", HashMap.class.getName()},
                {"list", ArrayList.class.getName()},
                {"long", Long.class.getName()},
                {"integer", Integer.class.getName()},
                {"short", Short.class.getName()},
                {"byte", Byte.class.getName()},
                {"char", Character.class.getName()},
                {"float", Float.class.getName()},
                {"double", Double.class.getName()},
                {"date", Date.class.getName()},
                {"enumSet", "java.util.RegularEnumSet"},
                {"array", "[Ljava.lang.Object;"},
                {"String[]", "[Ljava.lang.String;"},
                {"Double[]", "[Ljava.lang.Double;"},
                {"Float[]", "[Ljava.lang.Float;"},
                {"double[]", "[D"},
                {"float[]", "[F"}
        });
        return res;
    }

    /**
     *
     * Configuration for use on Android. Its binary compatible with getDefaultConfiguration().
     * So one can write on server with getDefaultConf and read on mobile client with getAndroidConf().
     *
     * @return
     */
    public static FSTConfiguration createAndroidDefaultConfiguration() {
        final Objenesis genesis = new ObjenesisStd();
        FSTConfiguration conf = new FSTConfiguration() {
            @Override
            public FSTClassInstantiator getInstantiator(Class clazz) {
                return new FSTObjenesisInstantiator(genesis,clazz);
            }
        };
        initDefaultFstConfigurationInternal(conf);
        return conf;
    }

    /**
     * the standard FSTConfiguration.
     * - safe (no unsafe r/w)
     * - platform independent byte order
     * - moderate compression
     *
     * note that if you are just read/write from/to byte arrays, its faster
     * to use DefaultCoder.
     *
     * This should be used most of the time.
     *
     * @return
     */
    public static FSTConfiguration createDefaultConfiguration() {
        if (isAndroid) {
            return createAndroidDefaultConfiguration();
        }
        FSTConfiguration conf = new FSTConfiguration();
        return initDefaultFstConfigurationInternal(conf);
    }

    protected static FSTConfiguration initDefaultFstConfigurationInternal(FSTConfiguration conf) {
        conf.addDefaultClazzes();
        // serializers
        FSTSerializerRegistry reg = conf.serializationInfoRegistry.serializerRegistry;
        reg.putSerializer(Class.class, new FSTClassSerializer(), false);
        reg.putSerializer(String.class, new FSTStringSerializer(), false);
        reg.putSerializer(Byte.class, new FSTBigNumberSerializers.FSTByteSerializer(), false);
        reg.putSerializer(Character.class, new FSTBigNumberSerializers.FSTCharSerializer(), false);
        reg.putSerializer(Short.class, new FSTBigNumberSerializers.FSTShortSerializer(), false);
        reg.putSerializer(Float.class, new FSTBigNumberSerializers.FSTFloatSerializer(), false);
        reg.putSerializer(Double.class, new FSTBigNumberSerializers.FSTDoubleSerializer(), false);

        reg.putSerializer(Date.class, new FSTDateSerializer(), false);
        reg.putSerializer(StringBuffer.class, new FSTStringBufferSerializer(), true);
        reg.putSerializer(StringBuilder.class, new FSTStringBuilderSerializer(), true);
        reg.putSerializer(EnumSet.class, new FSTEnumSetSerializer(), true);

        // for most cases don't register for subclasses as in many cases we'd like to fallback to JDK implementation
        // (e.g. TreeMap) in order to guarantee complete serialization
        reg.putSerializer(ArrayList.class, new FSTArrayListSerializer(), false);
        reg.putSerializer(Vector.class, new FSTCollectionSerializer(), false);
        reg.putSerializer(LinkedList.class, new FSTCollectionSerializer(), false); // subclass should register manually
        reg.putSerializer(HashSet.class, new FSTCollectionSerializer(), false); // subclass should register manually
        reg.putSerializer(HashMap.class, new FSTMapSerializer(), false); // subclass should register manually
        reg.putSerializer(LinkedHashMap.class, new FSTMapSerializer(), false); // subclass should register manually
        reg.putSerializer(Hashtable.class, new FSTMapSerializer(), true);
        reg.putSerializer(ConcurrentHashMap.class, new FSTMapSerializer(), true);
        reg.putSerializer(FSTStruct.class, new FSTStructSerializer(), true);

        // serializers for classes failing in fst JDK emulation (e.g. Android<=>JDK)
        reg.putSerializer(BigInteger.class, new FSTBigIntegerSerializer(), true);
        return conf;
    }

    /**
     * Returns a configuration using Unsafe to read write data.
     * - platform dependent byte order
     * - no value compression attempts
     * - makes heavy use of Unsafe, which can be dangerous in case
     *   of version conflicts
     *
     * Use only in case it makes a significant difference and you absolutely need the performance gain.
     * Performance gains depend on data. There are cases where this is even slower,
     * in some scenarios (many native arrays) it can be several times faster.
     * see also OffHeapCoder, OnHeapCoder.
     *
     */
    public static FSTConfiguration createFastBinaryConfiguration() {
        if ( isAndroid )
            throw new RuntimeException("not supported under android platform, use default configuration");
        final FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        conf.type = ConfType.UNSAFE;
        conf.setStreamCoderFactory( new FSTConfiguration.StreamCoderFactory() {
            @Override
            public FSTEncoder createStreamEncoder() {
                return new FSTBytezEncoder(conf, new HeapBytez(new byte[4096]));
            }
            @Override
            public FSTDecoder createStreamDecoder() {
                return new FSTBytezDecoder(conf);
            }
        });
        return conf;
    }


    /**
     * register a custom serializer for a given class or the class and all of its subclasses.
     * Serializers must be configured identical on read/write side and should be set before
     * actually making use of the Configuration.
     *
     * @param clazz
     * @param ser
     * @param alsoForAllSubclasses
     */
    public void registerSerializer(Class clazz, FSTObjectSerializer ser, boolean alsoForAllSubclasses ) {
        serializationInfoRegistry.serializerRegistry.putSerializer(clazz, ser, alsoForAllSubclasses);
    }

    /**
     * special configuration used internally for struct emulation
     * @return
     */
    public static FSTConfiguration createStructConfiguration() {
        FSTConfiguration conf = new FSTConfiguration();
        conf.setStructMode(true);
        return conf;
    }

    protected FSTConfiguration() {

    }

    public StreamCoderFactory getStreamCoderFactory() {
        return streamCoderFactory;
    }

    /**
     * allows to use subclassed stream codecs. Can also be used to change class loading behaviour, as
     * clasForName is part of a codec's interface.
     *
     * e.g. new StreamCoderFactory() {
     *   @Override
     *   public FSTEncoder createStreamEncoder() {
     *      return new FSTStreamEncoder(FSTConfiguration.this);
     *   }
     *
     *   @Override
     *   public FSTDecoder createStreamDecoder() {
     *      return new FSTStreamDecoder(FSTConfiguration.this) { public Class classForName(String name) { ... }  } ;
     *   }
     * };
     *
     * You need to work with thread locals most probably as the factory is ~global (assigned to fstconfiguration shared amongst
     * streams)
     *
     * @param streamCoderFactory
     */
    public void setStreamCoderFactory(StreamCoderFactory streamCoderFactory) {
        this.streamCoderFactory = streamCoderFactory;
    }

    /**
     * reuse heavy weight objects. If a FSTStream is closed, objects are returned and can be reused by new stream instances.
     * the objects are held in soft references, so there should be no memory issues. FIXME: point of contention !
     * @param cached
     */
    public void returnObject( Object cached ) {
        try {
            while (!cacheLock.compareAndSet(false, true)) {
                // empty
            }
            List<SoftReference> li = cachedObjects.get(cached.getClass());
            if ( li == null ) {
                li = new ArrayList<SoftReference>();
                cachedObjects.put(cached.getClass(),li);
            }
            if ( li.size() < 5 )
                li.add(new SoftReference(cached));
        } finally {
            cacheLock.set(false);
        }
    }

    /**
     * ignored currently
     * @return
     */
    public boolean isPreferSpeed() {
        return preferSpeed;
    }

    /**
     * this options lets FST favour speed of encoding over size of the encoded object.
     * Warning: this option alters the format of the written stream, so both reader and writer should have
     * the same setting, else exceptions will occur
     * @param preferSpeed
     */
    public void setPreferSpeed(boolean preferSpeed) {
        this.preferSpeed = preferSpeed;
    }

    /**
     * for optimization purposes, do not use to benchmark processing time or in a regular program as
     * this methods creates a temporary binaryoutputstream and serializes the object in order to measure the
     * size.
     */
    public int calcObjectSizeBytesNotAUtility( Object obj ) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(10000);
        FSTObjectOutput ou = new FSTObjectOutput(bout,this);
        ou.writeObject(obj, obj.getClass());
        ou.close();
        return bout.toByteArray().length;
    }

    /**
     * patch default serializer lookup. set to null to delete.
     * Should be set prior to any serialization going on (serializer lookup is cached).
     * @param del
     */
    public void setSerializerRegistryDelegate( FSTSerializerRegistryDelegate del ) {
        serializationInfoRegistry.setSerializerRegistryDelegate(del);
    }

    AtomicBoolean cacheLock = new AtomicBoolean(false);
    public Object getCachedObject( Class cl ) {
        try  {
            while (!cacheLock.compareAndSet(false, true)) {
                // empty
            }
            List<SoftReference> li = cachedObjects.get(cl);
            if ( li == null ) {
                return null;
            }
            for (int i = li.size()-1; i >= 0; i--) {
                SoftReference softReference = li.get(i);
                Object res = softReference.get();
                li.remove(i);
                if ( res != null ) {
                    return res;
                }
            }
        } finally {
            cacheLock.set(false);
        }
        return null;
    }

    public boolean isForceSerializable() {
        return forceSerializable;
    }

    /**
     * treat unserializable classes same as if they would be serializable.
     * @param forceSerializable
     */
    public FSTConfiguration setForceSerializable(boolean forceSerializable) {
        this.forceSerializable = forceSerializable;
        return this;
    }

    /**
     * clear cached softref's and ThreadLocal. Use if you won't read/write objects anytime soon.
     */
    public void clearCaches() {
        try {
            FSTInputStream.cachedBuffer.set(null);
            while (!cacheLock.compareAndSet(false, true)) {
                // empty
            }
            cachedObjects.clear();
        } finally {
            cacheLock.set( false );
        }
    }

    public boolean isShareReferences() {
        return shareReferences;
    }

    /**
     * if false, identical objects will get serialized twice. Gains speed as long there are no double objects/cyclic references (typical for small snippets as used in e.g. RPC)
     * @param shareReferences
     */
    public void setShareReferences(boolean shareReferences) {
        this.shareReferences = shareReferences;
    }

    /**
     *
     * Preregister a class (use at init time). This avoids having to write class names.
     * Its a very simple and effective optimization (frequently > 2 times faster for small objects).
     *
     * Read and write side need to have classes preregistered in the exact same order.
     *
     * The list does not have to be complete. Just add your most frequently serialized classes here
     * to get significant gains in speed and smaller serialized representation size.
     *
     */
    public void registerClass( Class ... c) {
        for (int i = 0; i < c.length; i++) {
            classRegistry.registerClass(c[i]);
            try {
                Class ac = Class.forName("[L"+c[i].getName()+";");
                classRegistry.registerClass(ac);
            } catch (ClassNotFoundException e) {
                // silent
            }
        }
    }

    void addDefaultClazzes() {
        classRegistry.registerClass(String.class);
        classRegistry.registerClass(Byte.class);
        classRegistry.registerClass(Short.class);
        classRegistry.registerClass(Integer.class);
        classRegistry.registerClass(Long.class);
        classRegistry.registerClass(Float.class);
        classRegistry.registerClass(Double.class);
        classRegistry.registerClass(BigDecimal.class);
        classRegistry.registerClass(BigInteger.class);
        classRegistry.registerClass(Character.class);
        classRegistry.registerClass(Boolean.class);
        classRegistry.registerClass(TreeMap.class);
        classRegistry.registerClass(HashMap.class);
        classRegistry.registerClass(ArrayList.class);
        classRegistry.registerClass(ConcurrentHashMap.class);
        classRegistry.registerClass(URL.class);
        classRegistry.registerClass(Date.class);
        classRegistry.registerClass(java.sql.Date.class);
        classRegistry.registerClass(SimpleDateFormat.class);
        classRegistry.registerClass(TreeSet.class);
        classRegistry.registerClass(LinkedList.class);
        classRegistry.registerClass(SimpleTimeZone.class);
        classRegistry.registerClass(GregorianCalendar.class);
        classRegistry.registerClass(Vector.class);
        classRegistry.registerClass(Hashtable.class);
        classRegistry.registerClass(BitSet.class);
        classRegistry.registerClass(Locale.class);

        classRegistry.registerClass(StringBuffer.class);
        classRegistry.registerClass(StringBuilder.class);

        classRegistry.registerClass(Object.class);
        classRegistry.registerClass(Object[].class);
        classRegistry.registerClass(Object[][].class);
        classRegistry.registerClass(Object[][][].class);
        classRegistry.registerClass(Object[][][][].class);
        classRegistry.registerClass(Object[][][][][].class);
        classRegistry.registerClass(Object[][][][][][].class);
        classRegistry.registerClass(Object[][][][][][][].class);

        classRegistry.registerClass(byte[].class);
        classRegistry.registerClass(byte[][].class);
        classRegistry.registerClass(byte[][][].class);
        classRegistry.registerClass(byte[][][][].class);
        classRegistry.registerClass(byte[][][][][].class);
        classRegistry.registerClass(byte[][][][][][].class);
        classRegistry.registerClass(byte[][][][][][][].class);

        classRegistry.registerClass(char[].class);
        classRegistry.registerClass(char[][].class);
        classRegistry.registerClass(char[][][].class);
        classRegistry.registerClass(char[][][][].class);
        classRegistry.registerClass(char[][][][][].class);
        classRegistry.registerClass(char[][][][][][].class);
        classRegistry.registerClass(char[][][][][][][].class);

        classRegistry.registerClass(short[].class);
        classRegistry.registerClass(short[][].class);
        classRegistry.registerClass(short[][][].class);
        classRegistry.registerClass(short[][][][].class);
        classRegistry.registerClass(short[][][][][].class);
        classRegistry.registerClass(short[][][][][][].class);
        classRegistry.registerClass(short[][][][][][][].class);

        classRegistry.registerClass(int[].class);
        classRegistry.registerClass(int[][].class);
        classRegistry.registerClass(int[][][].class);
        classRegistry.registerClass(int[][][][].class);
        classRegistry.registerClass(int[][][][][].class);
        classRegistry.registerClass(int[][][][][][].class);
        classRegistry.registerClass(int[][][][][][][].class);

        classRegistry.registerClass(float[].class);
        classRegistry.registerClass(float[][].class);
        classRegistry.registerClass(float[][][].class);
        classRegistry.registerClass(float[][][][].class);
        classRegistry.registerClass(float[][][][][].class);
        classRegistry.registerClass(float[][][][][][].class);
        classRegistry.registerClass(float[][][][][][][].class);

        classRegistry.registerClass(double[].class);
        classRegistry.registerClass(double[][].class);
        classRegistry.registerClass(double[][][].class);
        classRegistry.registerClass(double[][][][].class);
        classRegistry.registerClass(double[][][][][].class);
        classRegistry.registerClass(double[][][][][][].class);
        classRegistry.registerClass(double[][][][][][][].class);

    }

    public FSTClazzNameRegistry getClassRegistry() {
        return classRegistry;
    }

    public FSTClazzInfoRegistry getCLInfoRegistry() {
        return serializationInfoRegistry;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public FSTClazzInfo getClassInfo(Class type) {
        return serializationInfoRegistry.getCLInfo(type);
    }

    ThreadLocal<FSTObjectOutput> output = new ThreadLocal<FSTObjectOutput>() {
        @Override
        protected FSTObjectOutput initialValue() {
            if (type == ConfType.DEFAULT) {
                return new FSTObjectOutput(FSTConfiguration.this) {
                    FSTStreamEncoder st;

                    @Override
                    protected void setCodec(FSTEncoder codec) {
                        st = (FSTStreamEncoder) codec;
                    }
                    @Override
                    public FSTStreamEncoder getCodec() {
                        return st; // try to avoid megamorph calls
                    }
                };
            } else if ( type == ConfType.UNSAFE ) {
                return new FSTObjectOutput(FSTConfiguration.this) {
                    FSTBytezEncoder st;

                    @Override
                    protected void setCodec(FSTEncoder codec) {
                        st = (FSTBytezEncoder) codec;
                    }
                    @Override
                    public FSTBytezEncoder getCodec() {
                        return st; // try to avoid megamorph calls
                    }
                };
            } else
                return new FSTObjectOutput(FSTConfiguration.this);
        }
    };

    ThreadLocal<FSTObjectInput> input = new ThreadLocal<FSTObjectInput>() {
        @Override
        protected FSTObjectInput initialValue() {
            try {
                if (type == ConfType.DEFAULT) {
                    return new FSTObjectInput(FSTConfiguration.this){
                        FSTStreamDecoder st;
                        @Override
                        void setCodec(FSTDecoder codec) {
                            st = (FSTStreamDecoder) codec;
                        }

                        @Override
                        public FSTStreamDecoder getCodec() {
                            return st;
                        }
                    };
                } else if ( type == ConfType.UNSAFE ) {
                    return new FSTObjectInput(FSTConfiguration.this){
                        FSTBytezDecoder st;
                        @Override
                        void setCodec(FSTDecoder codec) {
                            st = (FSTBytezDecoder) codec;
                        }

                        @Override
                        public FSTBytezDecoder getCodec() {
                            return st;
                        }
                    };
                } else
                    return new FSTObjectInput(FSTConfiguration.this);
            } catch (Exception e) {
                throw FSTUtil.rethrow(e);
            }
        }
    };

    /**
     * utility for thread safety and reuse. Do not close the resulting stream. However you should close
     * the given InputStream 'in'
     * @param in
     * @return
     */
    public FSTObjectInput getObjectInput( InputStream in ) {
        FSTObjectInput fstObjectInput = input.get();
        try {
            fstObjectInput.resetForReuse(in);
            return fstObjectInput;
        } catch (IOException e) {
            throw FSTUtil.rethrow(e);
        }
    }

    public FSTObjectInput getObjectInput() {
        return getObjectInput((InputStream)null);
    }

    public FSTObjectInput getObjectInput( byte arr[]) {
        return getObjectInput(arr, arr.length);
    }

    /**
     * take the given array as input. the array is NOT copied
     * @param arr
     * @param len
     * @return
     */
    public FSTObjectInput getObjectInput( byte arr[], int len ) {
        FSTObjectInput fstObjectInput = input.get();
        try {
            fstObjectInput.resetForReuseUseArray(arr,len);
            return fstObjectInput;
        } catch (IOException e) {
            throw FSTUtil.rethrow(e);
        }
    }

    /**
     * utility for thread safety and reuse. Do not close the resulting stream. However you should close
     * the given OutputStream 'out'
     * @param out - can be null (temp bytearrays stream is created then)
     * @return
     */
    public FSTObjectOutput getObjectOutput(OutputStream out) {
        FSTObjectOutput fstObjectOutput = output.get();
        fstObjectOutput.resetForReUse(out);
        return fstObjectOutput;
    }

    /**
     * @return a recycled outputstream reusing its last recently used byte[] buffer
     */
    public FSTObjectOutput getObjectOutput() {
        return getObjectOutput((OutputStream)null);
    }

    public FSTObjectOutput getObjectOutput(byte[] outByte) {
        FSTObjectOutput fstObjectOutput = output.get();
        fstObjectOutput.resetForReUse(outByte);
        return fstObjectOutput;
    }

    /**
     * ignores all serialization related interfaces (Serializable, Externalizable) and serializes all classes using the
     * default scheme. Warning: this is a special mode of operation which fail serializing/deserializing many standard
     * JDK classes.
     *
     * @param ignoreSerialInterfaces
     */
    public void setStructMode(boolean ignoreSerialInterfaces) {
        serializationInfoRegistry.setStructMode(ignoreSerialInterfaces);
    }

    /**
     * special for structs
     * @return
     */
    public boolean isStructMode() {
        return serializationInfoRegistry.isStructMode();
    }

    public FSTClazzInfo getClazzInfo(Class rowClass) {
        return getCLInfoRegistry().getCLInfo(rowClass);
    }

    public void setCrossPlatform(boolean crossPlatform) {
        this.crossPlatform = crossPlatform;
    }

    public boolean isCrossPlatform() {
        return crossPlatform;
    }

    public <T> T deepCopy(T metadata) {
        return (T) asObject(asByteArray(metadata));
    }

    public static interface StreamCoderFactory {
        FSTEncoder createStreamEncoder();
        FSTDecoder createStreamDecoder();
    }
    
    public FSTEncoder createStreamEncoder() {
        return streamCoderFactory.createStreamEncoder();
    }

    public FSTDecoder createStreamDecoder() {
        return streamCoderFactory.createStreamDecoder();
    }

    AtomicBoolean cplock = new AtomicBoolean(false);
    public void registerCrossPlatformClassBinaryCache( String fulLQName, byte[] binary ) {
        try {
            while (cplock.compareAndSet(false, true)) { } // spin
            minBinNamesBytez.put(fulLQName, binary);
        } finally {
            cplock.set(false);
        }
    }

    public byte[] getCrossPlatformBinaryCache(String symbolicName) {
        try {
            while ( cplock.compareAndSet(false, true)) { } // spin
            return minBinNamesBytez.get(symbolicName);
        } finally {
            cplock.set(false);
        }
    }

    /**
     * init right after creation of configuration, not during operation as it is not threadsafe regarding mutation
     * currently only for minbin serialization
     *
     * @param keysAndVals { { "symbolicName", "fullQualifiedClazzName" }, .. }
     */
    public void registerCrossPlatformClassMapping( String[][] keysAndVals ) {
        for (int i = 0; i < keysAndVals.length; i++) {
            String[] keysAndVal = keysAndVals[i];
            registerCrossPlatformClassMapping(keysAndVal[0], keysAndVal[1]);
        }
    }

    public void registerCrossPlatformClassMapping( String shortName,  String fqName ) {
        minbinNames.put(shortName, fqName);
        minbinNamesReverse.put(fqName, shortName);
    }

    /**
     * init right after creation of configuration, not during operation as it is not threadsafe regarding mutation
     */
    public void registerCrossPlatformClassMappingUseSimpleName( Class[] classes ) {
        registerCrossPlatformClassMappingUseSimpleName(new ArrayList<>(Arrays.asList(classes)));
    }

    public void registerCrossPlatformClassMappingUseSimpleName( List<Class> classes ) {
        for (int i = 0; i < classes.size(); i++) {
            Class clz = classes.get(i);
            minbinNames.put(clz.getSimpleName(), clz.getName());
            minbinNamesReverse.put(clz.getName(), clz.getSimpleName());
        }
    }

    /**
     * get cross platform symbolic class identifier
     * @param cl
     * @return
     */
    public String getCPNameForClass( Class cl ) {
        String res = minbinNamesReverse.get(cl.getName());
        if (res == null) {
            return cl.getName();
        }
        return res;
    }

    public String getClassForCPName( String name ) {
        String res = minbinNames.get(name);
        if (res == null) {
            return name;
        }
        return res;
    }

    /**
     * convenience
     */
    public Object asObject( byte b[] ) {
        try {
            return getObjectInput(b).readObject();
        } catch (Exception e) {
            throw FSTUtil.rethrow(e);
        }
    }

    /**
     * convenience. (object must be serialiable unless fstconfiguration with appropriate settings is used)
     */
    public byte[] asByteArray( Object object ) {
        FSTObjectOutput objectOutput = getObjectOutput();
        try {
            objectOutput.writeObject(object);
            return objectOutput.getCopyOfWrittenBuffer();
        } catch (IOException e) {
            throw FSTUtil.rethrow(e);
        }
    }

    /**
     * Warning: avoids allocation + copying.
     * The returned byteArray is a direct pointer to underlying buffer.
     * the int length[] is expected to have at least on element.
     * The buffer can be larger than written data, therefore length[0] will contain written length.
     *
     * The buffer content must be used (e.g. sent to network, copied to offheap) before doing another
     * asByteArray on the current Thread.
     */
    public byte[] asSharedByteArray( Object object, int length[] ) {
        FSTObjectOutput objectOutput = getObjectOutput();
        try {
            objectOutput.writeObject(object);
            length[0] = objectOutput.getWritten();
            return objectOutput.getBuffer();
        } catch (IOException e) {
            throw FSTUtil.rethrow(e);
        }
    }

}
