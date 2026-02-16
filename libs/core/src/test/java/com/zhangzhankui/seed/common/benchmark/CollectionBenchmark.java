package com.zhangzhankui.seed.common.benchmark;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * 性能基准测试 - 集合操作
 *
 * <p>比较不同集合类型和操作方式的性能差异
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(
    value = 2,
    jvmArgs = {"-Xms2G", "-Xmx2G"})
public class CollectionBenchmark {

  private List<Integer> arrayList;
  private List<Integer> linkedList;
  private Set<Integer> hashSet;
  private Set<Integer> treeSet;
  private Map<Integer, String> hashMap;
  private Map<Integer, String> concurrentHashMap;

  @Param({"100", "1000", "10000"})
  private int size;

  @Setup
  public void setup() {
    arrayList = new ArrayList<>(size);
    linkedList = new LinkedList<>();
    hashSet = new HashSet<>();
    treeSet = new TreeSet<>();
    hashMap = new HashMap<>();
    concurrentHashMap = new ConcurrentHashMap<>();

    for (int i = 0; i < size; i++) {
      arrayList.add(i);
      linkedList.add(i);
      hashSet.add(i);
      treeSet.add(i);
      hashMap.put(i, "value_" + i);
      concurrentHashMap.put(i, "value_" + i);
    }
  }

  // ==================== 列表遍历比较 ====================

  @Benchmark
  public void arrayListForLoop(Blackhole bh) {
    for (int i = 0; i < arrayList.size(); i++) {
      bh.consume(arrayList.get(i));
    }
  }

  @Benchmark
  public void arrayListForEach(Blackhole bh) {
    for (Integer item : arrayList) {
      bh.consume(item);
    }
  }

  @Benchmark
  public void arrayListStream(Blackhole bh) {
    arrayList.stream().forEach(bh::consume);
  }

  @Benchmark
  public void arrayListParallelStream(Blackhole bh) {
    arrayList.parallelStream().forEach(bh::consume);
  }

  @Benchmark
  public void linkedListForLoop(Blackhole bh) {
    for (int i = 0; i < linkedList.size(); i++) {
      bh.consume(linkedList.get(i));
    }
  }

  @Benchmark
  public void linkedListIterator(Blackhole bh) {
    for (Integer item : linkedList) {
      bh.consume(item);
    }
  }

  // ==================== Set 查找比较 ====================

  @Benchmark
  public void hashSetContains(Blackhole bh) {
    bh.consume(hashSet.contains(size / 2));
  }

  @Benchmark
  public void treeSetContains(Blackhole bh) {
    bh.consume(treeSet.contains(size / 2));
  }

  // ==================== Map 操作比较 ====================

  @Benchmark
  public void hashMapGet(Blackhole bh) {
    bh.consume(hashMap.get(size / 2));
  }

  @Benchmark
  public void concurrentHashMapGet(Blackhole bh) {
    bh.consume(concurrentHashMap.get(size / 2));
  }

  @Benchmark
  public void hashMapIterate(Blackhole bh) {
    for (Map.Entry<Integer, String> entry : hashMap.entrySet()) {
      bh.consume(entry.getValue());
    }
  }

  // ==================== 集合创建比较 ====================

  @Benchmark
  public void createArrayListLoop(Blackhole bh) {
    List<Integer> list = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      list.add(i);
    }
    bh.consume(list);
  }

  @Benchmark
  public void createArrayListStream(Blackhole bh) {
    List<Integer> list = IntStream.range(0, size).boxed().collect(Collectors.toList());
    bh.consume(list);
  }

  // ==================== 过滤操作比较 ====================

  @Benchmark
  public void filterWithLoop(Blackhole bh) {
    List<Integer> result = new ArrayList<>();
    for (Integer item : arrayList) {
      if (item % 2 == 0) {
        result.add(item);
      }
    }
    bh.consume(result);
  }

  @Benchmark
  public void filterWithStream(Blackhole bh) {
    List<Integer> result = arrayList.stream().filter(i -> i % 2 == 0).collect(Collectors.toList());
    bh.consume(result);
  }

  @Benchmark
  public void filterWithParallelStream(Blackhole bh) {
    List<Integer> result =
        arrayList.parallelStream().filter(i -> i % 2 == 0).collect(Collectors.toList());
    bh.consume(result);
  }

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder().include(CollectionBenchmark.class.getSimpleName()).build();
    new Runner(opt).run();
  }
}
