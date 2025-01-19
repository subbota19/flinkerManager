package org.flinkerManager.jobs;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class TestJob {

    private static final Logger LOG = LoggerFactory.getLogger(TestJob.class);

    public static void main(String[] args) throws Exception {

        // Initialize the StreamExecutionEnvironment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStream<Integer> source = env.fromElements(1, 2, 3);

        DataStream<String> result = source.flatMap(new FlatMapFunction<Integer, String>() {
            @Override
            public void flatMap(Integer value, Collector<String> out) throws Exception {
                out.collect(value.toString());
            }
        });

        result.print();

        env.execute("Simple Flink Job with Logging");
    }
}
