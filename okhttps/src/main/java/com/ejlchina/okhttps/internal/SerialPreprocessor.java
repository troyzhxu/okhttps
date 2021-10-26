package com.ejlchina.okhttps.internal;

import com.ejlchina.okhttps.Preprocessor;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 串行预处理器
 * @author Troy.Zhou
 */
public class SerialPreprocessor implements Preprocessor {

    // 预处理器
    final Preprocessor preprocessor;
    // 待处理的任务队列
    final Queue<Preprocessor.PreChain> pendings;
    // 是否有任务正在执行
    boolean running = false;

    public SerialPreprocessor(Preprocessor preprocessor) {
        this.preprocessor = preprocessor;
        this.pendings = new LinkedList<>();
    }

    @Override
    public void doProcess(Preprocessor.PreChain chain) {
        boolean should = true;
        synchronized (this) {
            if (running) {
                pendings.add(chain);
                should = false;
            } else {
                running = true;
            }
        }
        if (should) {
            preprocessor.doProcess(chain);
        }
    }

    public void afterProcess() {
        Preprocessor.PreChain chain = null;
        synchronized (this) {
            if (pendings.size() > 0) {
                chain = pendings.poll();
            } else {
                running = false;
            }
        }
        if (chain != null) {
            preprocessor.doProcess(chain);
        }
    }

}
