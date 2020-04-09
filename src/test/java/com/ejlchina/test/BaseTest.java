package com.ejlchina.test;

public class BaseTest {

    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void println() {
        System.out.println();
    }

    public static void println(Object x) {
        System.out.println(x);
    }

    public static void println(long t0, String str) {
        System.out.println(now() - t0 + "\t" + str);
    }

    public static long now() {
        return System.currentTimeMillis();
    }

    public static class User {

        private int id;
        private String name;

        public void setId(int id) {
            this.id = id;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "User [id=" + id + ", name=" + name + "]";
        }

    }

}
