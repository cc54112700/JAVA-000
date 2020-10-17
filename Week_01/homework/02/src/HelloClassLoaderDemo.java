import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author chu
 * @date 2020-10-17 17:50:48
 */
public class HelloClassLoaderDemo extends ClassLoader {

    public static void main(String[] args) {

        try {

            Class<?> helloClass = new HelloClassLoaderDemo().findClass("Hello");
            Object object = helloClass.newInstance();
            Method method = helloClass.getMethod("hello");
            method.invoke(object);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {

        // 读取Hello.xlass文件内容到Byte[]

        byte[] helloClassBytes = null;
        try {

            helloClassBytes = getFileContent();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 用255减去还原
        byte[] decodedBytes = decodeBytes(helloClassBytes);

        return defineClass(name, decodedBytes, 0, decodedBytes.length);
    }

    private byte[] decodeBytes(byte[] helloClassBytes) {

        int length = helloClassBytes.length;
        byte[] decodeBytes = new byte[length];

        if (null != helloClassBytes && helloClassBytes.length > 0) {

            for (int i = 0; i < helloClassBytes.length; i++) {

                decodeBytes[i] = (byte) (255 - helloClassBytes[i]);
            }
        }

        return decodeBytes;
    }

    private byte[] getFileContent() throws IOException {

        String path = HelloClassLoaderDemo.class.getResource("Hello.xlass").getPath();
        File file = new File(path);
        if (!file.exists()) {

            throw new FileNotFoundException();
        }

        byte[] bytes = null;

        ByteArrayOutputStream bos = new ByteArrayOutputStream((int) file.length());
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        try {

            int buf_size = 1024;
            byte[] buffer = new byte[buf_size];
            int len = 0;
            while (-1 != (len = bis.read(buffer, 0, buf_size))) {

                bos.write(buffer, 0, len);
            }

            bytes = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                bos.close();
                bis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            bos.close();
        }

        return bytes;
    }
}
