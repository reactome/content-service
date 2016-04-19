package org.reactome.server.tools.utils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.shaded.org.objenesis.strategy.StdInstantiatorStrategy;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;

import java.io.*;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@Scope("singleton")
public class TupleManager {

    private static Logger logger = Logger.getLogger(TupleManager.class.getName());

    private String pathDirectory;

    public void setPathDirectory(String pathDirectory) {
        this.pathDirectory = pathDirectory;
    }

    public Object readToken(String token){
        try {
            return read(pathDirectory + "/" + token + ".bin");
        } catch (FileNotFoundException | ClassCastException e) {
            return null;
        }
    }

    public void saveToken(String token, Object object){
        long start = System.currentTimeMillis();
        try {
            String fileName = pathDirectory + "/" + token + ".bin";

            Kryo kryo = new Kryo();
            kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
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

    private Object read(InputStream file){
        Kryo kryo = new Kryo();
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
        Input input = new Input(file);
        Object obj = kryo.readClassAndObject(input);
        input.close();
        return obj;
    }
}
