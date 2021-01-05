package com.ejlchina.okhttps;

public class ParallelProcess {

    // 线程号
    private int index;
    // 线程总数
    private int total;
    // 当前进度
    private Process process;

    public ParallelProcess(int index, int total, Process process) {
        this.index = index;
        this.total = total;
        this.process = process;
    }

    public int getIndex() {
        return index;
    }

    public int getTotal() {
        return total;
    }

    public Process getProcess() {
        return process;
    }

}
