# 学习笔记

## GC总结
### SerialGC串行GC
1. 串行 GC 对年轻代使用 mark-copy（标记-复制），对老年代使用 mark-sweep-compact（标记-清除-整理）
2. 不能并行处理，GC时会STW
3. 只适用于单核CPU

### ParallelGC并行GC
1. 在年轻代使用 标记-复制（mark-copy），在老年代使用 标记-清除-整理（mark-sweepcompact）
2. GC时都会触发STW
3. GC时，所有CPU内核都在并行清理垃圾，所以总暂停时间更短，在两次GC间隔时期，没有GC线程在运行，不会消耗任何系统资源

* YoungGC时，只清理Young区，Old区不清理；
* 存活于Eden区与S0的对象 复制到 S1后，全部清理掉，再部分对象晋升到Old；
* 再次YoungGC时，把Eden区与S1存活的对象 复制到 S0后， 全部清理掉，再部分对象晋升到Old；
* Old区比利过高，发生FullGC, 清理掉Young区和Old区里不活跃的对象；

### ConcMarkSweepGC
1. 对年轻代采用并行STW方式的 mark-copy (标记-复制)，对老年代主要使用并发 mark-sweep (标记-清除)
2. 默认情况下，CMS 使用的并发线程数等于 CPU 核心数的 1/4
3. 调优目标是降低GC停顿导致的系统延迟，推荐使用CMSGC
4. 6个阶段
    1. Initial Mark（初始标记）
        > 这个阶段伴随着 STW 暂停。初始标记的目标是标记所有的根对象，包括根对象直接引用的对象，以及被年轻代中所有存活对象所引用的对象（老年代单独回收）。
    2. Concurrent Mark（并发标记）
        > 在此阶段，CMS GC 遍历老年代，标记所有的存活对象，从前一阶段 “Initial Mark” 找到的根对象开始算起。 “并发标记”阶段，就是与应用程序同时运行，不用暂停的阶段。
    3. Concurrent Preclean（并发预清理）
        > 此阶段同样是与应用线程并发执行的，不需要停止应用线程。 因为前一阶段【并发标记】与程序并发运行，可能有一些引用关系已经发生了改变。如果在并发标记过程中引用关系发生了变化，JVM 会通过“Card（卡片）”的方式将发生了改变的区域标记为“脏”区，这就是所谓的 卡片标记（Card Marking）。
    4. Final Remark（最终标记）
        > 最终标记阶段是此次 GC 事件中的第二次（也是最后一次）STW 停顿。本阶段的目标是完成老年代中所有存活对象的标记. 因为之前的预清理阶段是并发执行的，有可能 GC 线程跟不上应用程序的修改速度。所以需要一次STW 暂停来处理各种复杂的情况。通常 CMS 会尝试在年轻代尽可能空的情况下执行 Final Remark 阶段，以免连续触发多次 STW 事件。
    5. Concurrent Sweep（并发清除）
        > 此阶段与应用程序并发执行，不需要 STW 停顿。JVM 在此阶段删除不再使用的对象，并回收他们占用的内存空间。
    6. Concurrent Reset（并发重置）
        > 此阶段与应用程序并发执行，重置 CMS 算法相关的内部数据，为下一次 GC 循环做准备。
                               
### G1 GC (Garbage-First)
1. 设计目标是：将 STW 停顿的时间和分布，变成可预期且可配置的
2. 堆不再分成年轻代和老年代，而是划分为多个（通常是 2048 个）可以存放对象的 小块堆区域(smaller heap regions)
    1. Eden区 （标记-复制 算法）
    2. Survivor区
        > 所有的 Eden区和 Survivor区合起来就是年轻代
    3. Old (标记-复制-整理 算法)
        > 所有的 Old 区拼在一起那就是老年代
3. 处理步骤
    1. 年轻代模式转移暂停（Evacuation Pause）
        1. 开始G1处于初始fullyyoung模式
        2. 年轻代空间用满后，应用线程会被暂停，存活对象被拷贝到存活区
            > 若无存活区，则任意选择一部分空闲的内存块作为存活区，拷贝的过程称为转移（Evacuation)
    2. 并发标记（Concurrent Marking） 与 CMS 类似
        1. Initial Mark（初始标记）
            > 此阶段标记所有从 GC 根对象直接可达的对象。
        2. Root Region Scan（Root区扫描）
            > 此阶段标记所有从 "根区域" 可达的存活对象。根区域包括：非空的区域，以及在标记过程中不得不收集的区域。
        3. Concurrent Mark（并发标记）
            > 此阶段和 CMS 的并发标记阶段非常类似：只遍历对象图，并在一个特殊的位图中标记能访问到的对象。
        4. Remark（再次标记） 
            > 和 CMS 类似，这是一次 STW 停顿(因为不是并发的阶段)，以完成标记过程。 G1 收集器会短暂地停止应用线程，停止并发更新信息的写入，处理其中的少量信息，并标记所有在并发标记开始时未被标记的存活对象。
        5. Cleanup（清理）
            > 最后这个清理阶段为即将到来的转移阶段做准备，统计小堆块中所有存活的对象，并将小堆块进行排序，以提升GC 的效率，维护并发标记的内部状态。 所有不包含存活对象的小堆块在此阶段都被回收了。有一部分任务是并发的：例如空堆区的回收，还有大部分的存活率计算。此阶段也需要一个短暂的 STW 暂停。
    3. 转移暂停: 混合模式（Evacuation Pause (mixed)）
        > 不只清理年轻代，还将一部分老年代区域也加入到 回收集 中; 混合模式的转移暂停不一定紧跟并发标记阶段, 其中很可能会存在多次 young 模式的转移暂停
4. 注意事项
    1. 并发模式失败
        > G1 启动标记周期，但在 Mix GC 之前，老年代就被填满，这时候 G1 会放弃标记周期。解决办法：增加堆大小， 或者调整周期（例如增加线程数-XX：ConcGCThreads 等）。
    2. 晋升失败
        > 没有足够的内存供存活对象或晋升对象使用，由此触发了 Full GC(to-space exhausted/to-space overflow）。
          解决办法：
          a) 增加 –XX：G1ReservePercent 选项的值（并相应增加总的堆大小）增加预留内存量。
          b) 通过减少 –XX：InitiatingHeapOccupancyPercent 提前启动标记周期。
          c) 也可以通过增加 –XX：ConcGCThreads 选项的值来增加并行标记线程的数目。
    3. 巨型对象分配失败
        > 当巨型对象找不到合适的空间进行分配时，就会启动 Full GC，来释放空间。
          解决办法：增加内存或者增大 -XX：G1HeapRegionSize

### 对比总结
* 串行GC：适合单CPU的Client模式，响应速度优先
* 并行GC：多CPU环境下和CMS配合使用，吞吐量优先
* CMS GC和G1 GC：响应速度优先，适合Web服务使用 


# 1022作业：
## 1、使用 GCLogAnalysis.java 自己演练一遍串行/并行/CMS/G1的案例。

```
java -XX:+UseSerialGC -Xms128m -Xmx128m -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis
```
```
java -XX:+UseParallelGC -Xms512m -Xmx512m -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis
```
```
java -XX:+UseConcMarkSweepGC -Xms512m -Xmx512m -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis
```
```
java -XX:+UseG1GC -Xms512m -Xmx512m -XX:+PrintGC -XX:+PrintGCDateStamps GCLogAnalysis
```

| GC        | 128m   |  512m  |  1g  |  2g  |  4g  |
| --------  | :----: |  :----: | :----: | :----: | :----: |
| SerialGC     | OOM  | 10048 | 11832 | 11616 | 10906 |
| ParallelGC   |   12978      | 11828 | 13020 | 13203 | 12845 |
| ConcMarkSweepGC |    10092    |  10446  | 9617 | 9976| 10329 |
| G1GC        |    8520    |  9917  | 8853 | 9106 | 8926 | 

### 生成对象次数随机性较大  仅供参考 

## 2.使用SuperBenchmarker压测 几种不同启动方式下的gateway-server：
* 结论数据并不明显 感觉不能代表什么
```
java -jar -XX:+UseSerialGC -Xms256m -Xmx256m gateway-server-0.0.1-SNAPSHOT.jar 
---
sb -u http://localhost:8088/api/hello -c 20 -N 60
Starting at 2020/10/29 0:51:03
[Press C to stop the test]
357451  (RPS: 5557.3)
---------------Finished!----------------
Finished at 2020/10/29 0:52:08 (took 00:01:04.5326014)
Status 200:    357453

RPS: 5839 (requests/second)
Max: 178ms
Min: 0ms
Avg: 0.1ms

  50%   below 0ms
  60%   below 0ms
  70%   below 0ms
  80%   below 0ms
  90%   below 0ms
  95%   below 0ms
  98%   below 3ms
  99%   below 3ms
99.9%   below 7ms
```

```
java -jar -XX:+UseSerialGC -Xms512m -Xmx512m gateway-server-0.0.1-SNAPSHOT.jar
---
sb -u http://localhost:8088/api/hello -c 20 -N 60
Starting at 2020/10/29 0:53:12
[Press C to stop the test]
346841  (RPS: 5385.2)
---------------Finished!----------------
Finished at 2020/10/29 0:54:16 (took 00:01:04.4370284)
Status 200:    346846

RPS: 5682.7 (requests/second)
Max: 178ms
Min: 0ms
Avg: 0.1ms

  50%   below 0ms
  60%   below 0ms
  70%   below 0ms
  80%   below 0ms
  90%   below 0ms
  95%   below 0ms
  98%   below 2ms
  99%   below 3ms
99.9%   below 6ms
```

```
java -jar -XX:+UseParallelGC -Xms256m -Xmx256m gateway-server-0.0.1-SNAPSHOT.jar
---
sb -u http://localhost:8088/api/hello -c 20 -N 60
Starting at 2020/10/29 0:57:05
[Press C to stop the test]
358308  (RPS: 5573.5)
---------------Finished!----------------
Finished at 2020/10/29 0:58:09 (took 00:01:04.4483930)
Status 200:    358309

RPS: 5857.9 (requests/second)
Max: 51ms
Min: 0ms
Avg: 0.1ms

  50%   below 0ms
  60%   below 0ms
  70%   below 0ms
  80%   below 0ms
  90%   below 0ms
  95%   below 0ms
  98%   below 1ms
  99%   below 3ms
99.9%   below 5ms
```

```
java -jar -XX:+UseParallelGC -Xms512m -Xmx512m gateway-server-0.0.1-SNAPSHOT.jar
---
sb -u http://localhost:8088/api/hello -c 20 -N 60
Starting at 2020/10/29 0:58:39
[Press C to stop the test]
359764  (RPS: 5595.9)
---------------Finished!----------------
Finished at 2020/10/29 0:59:44 (took 00:01:04.5036669)
Status 200:    359772

RPS: 5877 (requests/second)
Max: 179ms
Min: 0ms
Avg: 0.1ms

  50%   below 0ms
  60%   below 0ms
  70%   below 0ms
  80%   below 0ms
  90%   below 0ms
  95%   below 0ms
  98%   below 2ms
  99%   below 3ms
99.9%   below 5ms
```

```
java -jar -XX:+UseConcMarkSweepGC -Xms256m -Xmx256m gateway-server-0.0.1-SNAPSHOT.jar
---
sb -u http://localhost:8088/api/hello -c 20 -N 60
Starting at 2020/10/29 1:04:15
[Press C to stop the test]
361981  (RPS: 5628.1)
---------------Finished!----------------
Finished at 2020/10/29 1:05:19 (took 00:01:04.5066792)
Status 200:    361982

RPS: 5915.3 (requests/second)
Max: 187ms
Min: 0ms
Avg: 0.1ms

  50%   below 0ms
  60%   below 0ms
  70%   below 0ms
  80%   below 0ms
  90%   below 0ms
  95%   below 0ms
  98%   below 2ms
  99%   below 3ms
99.9%   below 5ms
```

```
java -jar -XX:+UseConcMarkSweepGC -Xms512m -Xmx512m gateway-server-0.0.1-SNAPSHOT.jar
---
sb -u http://localhost:8088/api/hello -c 20 -N 60
Starting at 2020/10/29 1:06:05
[Press C to stop the test]
354578  (RPS: 5514.7)
---------------Finished!----------------
Finished at 2020/10/29 1:07:09 (took 00:01:04.5186000)
Status 200:    354583

RPS: 5791.3 (requests/second)
Max: 196ms
Min: 0ms
Avg: 0.1ms

  50%   below 0ms
  60%   below 0ms
  70%   below 0ms
  80%   below 0ms
  90%   below 0ms
  95%   below 0ms
  98%   below 2ms
  99%   below 3ms
99.9%   below 5ms
```

```
java -jar -XX:+UseG1GC -Xms256m -Xmx256m gateway-server-0.0.1-SNAPSHOT.jar
---
sb -u http://localhost:8088/api/hello -c 20 -N 60
Starting at 2020/10/29 1:09:59
[Press C to stop the test]
374823  (RPS: 5848.1)
---------------Finished!----------------
Finished at 2020/10/29 1:11:03 (took 00:01:04.2946334)
Status 200:    374825

RPS: 6123.8 (requests/second)
Max: 51ms
Min: 0ms
Avg: 0.1ms

  50%   below 0ms
  60%   below 0ms
  70%   below 0ms
  80%   below 0ms
  90%   below 0ms
  95%   below 0ms
  98%   below 1ms
  99%   below 3ms
99.9%   below 4ms
```

```
java -jar -XX:+UseG1GC -Xms512m -Xmx512m gateway-server-0.0.1-SNAPSHOT.jar
---
sb -u http://localhost:8088/api/hello -c 20 -N 60
Starting at 2020/10/29 1:12:23
[Press C to stop the test]
382525  (RPS: 5956.8)
---------------Finished!----------------
Finished at 2020/10/29 1:13:28 (took 00:01:04.4119695)
Status 200:    382527

RPS: 6250.2 (requests/second)
Max: 190ms
Min: 0ms
Avg: 0.1ms

  50%   below 0ms
  60%   below 0ms
  70%   below 0ms
  80%   below 0ms
  90%   below 0ms
  95%   below 0ms
  98%   below 2ms
  99%   below 3ms
99.9%   below 5ms
```