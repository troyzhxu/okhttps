package com.ejlchina.okhttps;

import java.io.*;

public class RafOutputStream extends OutputStream {

    private final RandomAccessFile raf;


    public RafOutputStream(File file) throws FileNotFoundException {
        this.raf = new RandomAccessFile(file, "rw");
    }

    public RafOutputStream(RandomAccessFile raf) {
        this.raf = raf;
    }

    @Override
    public void write(byte[] b) throws IOException {
        raf.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        raf.write(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
        raf.write(b);
    }

    @Override
    public void close() throws IOException {
        raf.close();
    }

}
