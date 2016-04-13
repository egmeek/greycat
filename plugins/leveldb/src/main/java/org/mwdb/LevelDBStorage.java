package org.mwdb;

import java.io.File;

import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.WriteBatch;
import org.mwdb.chunk.KBuffer;
import org.mwdb.plugin.KStorage;

public class LevelDBStorage implements KStorage {
	
    private static final String _connectedError = "PLEASE CONNECT YOUR DATABASE FIRST";
    private static final byte[] prefixKey = "prefix".getBytes();
	
    private final String storagePath;
    
    private DB db;
    private boolean isConnected;
    private KGraph graph;
	
    public LevelDBStorage(String storagePath){
    	this.isConnected = false;
        this.storagePath = storagePath;
    }
    
    @Override
    public void get(KBuffer[] keys, KCallback<KBuffer[]> callback) {
        if (!isConnected) {
            throw new RuntimeException(_connectedError);
        }
        int nbKeys = keys.length;
        KBuffer[] result = new KBuffer[nbKeys];
        for (int i = 0; i < nbKeys; i++) {
            try {
                byte[] res = db.get(keys[i].data());
                if (res != null) {
                    KBuffer newBuf = graph.newBuffer();
                    int ii = 0;
                    while (ii < res.length) {
                        newBuf.write(res[ii]);
                        ii++;
                    }
                    result[i] = newBuf;
                } else {
                    result[i] = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (callback != null) {
            callback.on(result);
        }
    }

    @Override
    public void put(KBuffer[] keys, KBuffer[] p_values, KCallback<Boolean> callback) {
        if (!isConnected) {
            throw new RuntimeException(_connectedError);
        }
        try {
            int nbKeys = keys.length;
            WriteBatch batch = db.createWriteBatch();        
            for (int i = 0; i < nbKeys; i++) {
                batch.put(keys[i].data(), p_values[i].data());
            }
            db.write(batch);        
            if (callback != null) {
            	callback.on(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (callback != null) {
            	callback.on(false);
            }
        }
    }

    @Override
    public void remove(KBuffer[] keys, KCallback<Boolean> callback) {
        if (!isConnected) {
            throw new RuntimeException(_connectedError);
        }
        try {
            int nbKeys = keys.length;
            WriteBatch batch = db.createWriteBatch();        
            for (int i = 0; i < nbKeys; i++) {
                batch.delete(keys[i].data());
            }
            db.write(batch);        
            if (callback != null) {
                callback.on(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (callback != null) {
                callback.on(false);
            }
        }
    }

    @Override
    public void disconnect(Short prefix, KCallback<Boolean> callback) {
        try {
        	db.close();
        	db = null;
            if (callback != null) {
                callback.on(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (callback != null) {
                callback.on(false);
            }
        }
    }


    @Override
    public void connect(KGraph graph, KCallback<Short> callback) {
        if (isConnected) {
            if (callback != null) {
                callback.on(null);
            }
            return;
        }
        this.graph = graph;
        //by default activate snappy compression of bytes
        Options options = new Options().createIfMissing(true).compressionType(CompressionType.SNAPPY);
        File location = new File(storagePath);
        if (!location.exists()) {
            location.mkdirs();
        }
        File targetDB = new File(location, "data");
        targetDB.mkdirs();
        try {
            db = JniDBFactory.factory.open(targetDB, options);
            isConnected = true;
            byte[] current = db.get(prefixKey);
            if (current == null) {
                current = new String("0").getBytes();
            }
            Short currentPrefix = Short.parseShort(new String(current));
            db.put(prefixKey, ((currentPrefix + 1) + "").getBytes());
            if (callback != null) {
                callback.on(currentPrefix);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (callback != null) {
                callback.on(null);
            }
        }
    }

}