package com.yfgooo.flink.course05;

import org.apache.flink.streaming.api.functions.source.SourceFunction;

/**
 * Author: Michael PK
 */
public class JavaCustomNonParallelSourceFunction implements SourceFunction<Long> {
    boolean isRunning = true;
    long count = 1;

    public void run(SourceContext<Long> ctx) throws Exception {
        while (true) {
            ctx.collect(count);
            count += 1;
            Thread.sleep(1000);
        }
    }

    public void cancel() {
        isRunning = false;
    }
}
