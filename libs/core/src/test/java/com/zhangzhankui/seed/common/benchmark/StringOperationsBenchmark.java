package com.zhangzhankui.seed.common.benchmark;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * 性能基准测试 - 字符串操作
 *
 * <p>使用 JMH (Java Microbenchmark Harness) 进行微基准测试
 *
 * <p>运行方式： mvn clean install -DskipTests java -jar target/benchmarks.jar StringOperationsBenchmark
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(
    value = 2,
    jvmArgs = {"-Xms2G", "-Xmx2G"})
public class StringOperationsBenchmark {

  private String testString;
  private String[] strings;

  @Setup
  public void setup() {
    testString = "hello_world_test_string_for_benchmark";
    strings = new String[100];
    for (int i = 0; i < 100; i++) {
      strings[i] = "item_" + i;
    }
  }

  @Benchmark
  public void stringConcat(Blackhole bh) {
    String result = "";
    for (String s : strings) {
      result = result + s;
    }
    bh.consume(result);
  }

  @Benchmark
  public void stringBuilder(Blackhole bh) {
    StringBuilder sb = new StringBuilder();
    for (String s : strings) {
      sb.append(s);
    }
    bh.consume(sb.toString());
  }

  @Benchmark
  public void stringJoin(Blackhole bh) {
    String result = String.join("", strings);
    bh.consume(result);
  }

  @Benchmark
  public void camelToUnderscoreManual(Blackhole bh) {
    String input = "getUserInfoByUsername";
    StringBuilder result = new StringBuilder();
    for (char c : input.toCharArray()) {
      if (Character.isUpperCase(c)) {
        result.append('_').append(Character.toLowerCase(c));
      } else {
        result.append(c);
      }
    }
    bh.consume(result.toString());
  }

  @Benchmark
  public void camelToUnderscoreRegex(Blackhole bh) {
    String input = "getUserInfoByUsername";
    String result = input.replaceAll("([A-Z])", "_$1").toLowerCase();
    bh.consume(result);
  }

  @Benchmark
  public void substringWithSubstring(Blackhole bh) {
    String result = testString.substring(0, 10);
    bh.consume(result);
  }

  @Benchmark
  public void substringWithCharArray(Blackhole bh) {
    char[] chars = new char[10];
    testString.getChars(0, 10, chars, 0);
    String result = new String(chars);
    bh.consume(result);
  }

  public static void main(String[] args) throws RunnerException {
    Options opt =
        new OptionsBuilder().include(StringOperationsBenchmark.class.getSimpleName()).build();
    new Runner(opt).run();
  }
}
