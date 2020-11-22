import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

public class MyHomeWork {

    public static void main(String[] args) {

        long start= System.currentTimeMillis();
        // 在这里创建一个线程或线程池，
        // 异步执行 下面方法

//        int result = sum(); //这是得到的返回值



        // 1.CountDownLatch
//        int result = method1();
        // 2.让主线程sleep等待计算完毕
//        int result = method2();
        // 3.CyclicBarrier
//        int result = method3();
        // 4.synchronized
//        int result = method4();
        // 5.join
//        int result = method5();

        // 6.ReentrantLock
//        int result = method6();

        // 7.Executor
//        int result = method7();

        // 8.FutureTask
//        int result = method8();

        // 9.FutureTask Callable
//        int result = method9();

        // 10.LockSupport
//        int result = method10();

        // 11.Semaphore
        int result = method11();

        // 确保  拿到result 并输出
        System.out.println("异步计算结果为：" + result);

        System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");

        // 然后退出main线程
    }

    // Semaphore  当Semaphore = 1时 main acquire
    private static int method11() {

        AtomicInteger ati = new AtomicInteger();

        Semaphore semaphore = new Semaphore(0);

        new Thread(() -> {

            ati.set(sum());
            semaphore.release();
        }).start();

        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return ati.get();
    }

    // LockSupport
    private static int method10() {

        AtomicInteger ati = new AtomicInteger();

        Thread threadMain = Thread.currentThread();
        new Thread(() -> {
            ati.set(sum());

            // 算完后解 threadMain
            LockSupport.unpark(threadMain);
        }).start();

        // 阻塞 threadMain
        LockSupport.park();
        return ati.get();
    }

    private static int method9() {

        Callable<Integer> callable = new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return sum();
            }
        };

        FutureTask<Integer> task = new FutureTask<>(callable);
        new Thread(task).start();

        try {
            return task.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return 0;
    }

    // FutureTask
    private static int method8() {

        FutureTask<Integer> task = new FutureTask<>(() -> sum());

        new Thread(task).start();

        try {
            return task.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return 0;
    }

    // Executor Future
    private static int method7() {

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Integer> f = executorService.submit(() -> sum());

        try {

            return f.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {

            executorService.shutdown();
        }

        return 0;
    }

    // ReentrantLock Condition
    private static int method6() {

        AtomicInteger ati = new AtomicInteger();
        ReentrantLock lock = new ReentrantLock();
        Condition condition = lock.newCondition();

        try {

            lock.lock();

            new Thread(() -> {

                lock.lock();
                ati.set(sum());
                condition.signalAll();
                lock.unlock();
            }).start();
            condition.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {

            lock.unlock();
        }
        return ati.get();
    }

    private static int method5() {

        AtomicInteger ati = new AtomicInteger();

        Thread thread = new Thread(() -> {
            ati.set(sum());
        });

        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return ati.get();
    }

    // synchronized
    private static int method4() {

        synchronized (MyHomeWork.class) {

            AtomicInteger result = new AtomicInteger();
            new Thread(() -> {

                synchronized (MyHomeWork.class) {

                    result.set(sum());
                    MyHomeWork.class.notify();
                }
            }).start();

            try {

                MyHomeWork.class.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return result.get();
        }

    }

    // CyclicBarrier
    private static int method3() {

        AtomicInteger ati = new AtomicInteger();
        CyclicBarrier cyc = new CyclicBarrier(2);

        new Thread(() -> {
            ati.set(sum());
            try {
                cyc.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }).start();

        try {
            cyc.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }

        return ati.get();
    }

    // 通过让主线程sleep，等待计算完成
    private static int method2() {

        AtomicInteger ati = new AtomicInteger();

        new Thread(() -> {
            ati.set(sum());
        }).start();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return ati.get();
    }

    private static int method1() {

        AtomicInteger ati = new AtomicInteger();
        CountDownLatch cdl = new CountDownLatch(1);
        new Thread(() -> {
            ati.set(sum());
            // 计算完毕 调用-1
            cdl.countDown();
        }).start();

        try {

            // 进行阻塞，等待数量归0
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return ati.get();
    }


    private static int sum() {

        return fibo(36);
    }

    private static int fibo(int a) {

        if (a < 2) {

            return 1;
        }
        return fibo(a-1) + fibo(a-2);
    }
}
