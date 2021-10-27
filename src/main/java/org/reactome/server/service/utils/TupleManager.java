package org.reactome.server.service.utils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import com.googlecode.concurrenttrees.radix.node.util.AtomicReferenceArrayListAdapter;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

import java.io.*;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@Scope("singleton")
public class TupleManager {

    private static Logger logger = LoggerFactory.getLogger("TupleManager");

    private String pathDirectory;

    public void setPathDirectory(String pathDirectory) {
        this.pathDirectory = pathDirectory;
    }

    public Object readToken(String token) {
        try {
            return read(pathDirectory + "/" + token + ".bin");
        } catch (FileNotFoundException | ClassCastException e) {
            return null;
        }
    }

    public void saveToken(String token, Object object) {
        long start = System.currentTimeMillis();
        try {
            String fileName = pathDirectory + "/" + token + ".bin";
            Kryo kryo = new Kryo();
            kryo.setRegistrationRequired(false);
            kryo.setReferences(true);
            kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
            kryo.register(AtomicReferenceArrayListAdapter.class, new FieldSerializer<>(kryo, AtomicReferenceArrayListAdapter.class));
            OutputStream file = new FileOutputStream(fileName);
            Output output = new Output(file);
            kryo.writeClassAndObject(output, object);
            output.close();
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
        }
        long end = System.currentTimeMillis();
        logger.info(String.format("%s saved in %d ms", object.getClass().getSimpleName(), end - start));
    }

    private Object read(String fileName) throws FileNotFoundException {
        InputStream file = new FileInputStream(fileName);
        Object rtn = read(file);
        logger.info(fileName + " retrieved");
        return rtn;
    }

    private Object read(InputStream file) {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        kryo.setReferences(true);
        kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
        kryo.register(AtomicReferenceArrayListAdapter.class, new FieldSerializer<>(kryo, AtomicReferenceArrayListAdapter.class));
        Input input = new Input(file);
        Object obj = kryo.readClassAndObject(input);
        input.close();
        return obj;
    }
}
