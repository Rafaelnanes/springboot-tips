package org.example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Findings:
 * - When there is a lot of processing to wait for response the virtual threads excels in his job.
 * - When there is no blocking code virtual threads are very slower
 */
public class VirtualThreadsTest {

  @BeforeEach
  void setUp() {
    MyVirtualThreadExample.isSleepingEnabled = true;
    MyVirtualThreadExample.MAX = 5_000;
  }

  /**
   * Due the Thread.sleep the virtual threads do a better job by doing another task while waiting for the current task
   */
  @Test
  void virtualAreFaster_readFiles() throws InterruptedException {
    Runnable action = MyVirtualThreadExample::readFileLines;
    final int availableProcessors = Runtime.getRuntime().availableProcessors();

    final long virtualThreads = MyVirtualThreadExample.runUsingVirtualThreads(action);
    final long fixedThreadPool = MyVirtualThreadExample.runUsingFixedThreadPool(action, availableProcessors);

    System.out.println("virtualThreads: " + virtualThreads);
    System.out.println("fixedThreadPool: " + fixedThreadPool);
    Assertions.assertTrue(virtualThreads < fixedThreadPool);
  }

  /**
   * The time is very similar and sometimes the virtual threads are slower
   */
  @Test
  void virtualAreSlower_readFiles() throws InterruptedException {
    MyVirtualThreadExample.isSleepingEnabled = false;

    Runnable action = MyVirtualThreadExample::readFileLines;
    final int availableProcessors = Runtime.getRuntime().availableProcessors();

    final long virtualThreads = MyVirtualThreadExample.runUsingVirtualThreads(action);
    final long fixedThreadPool = MyVirtualThreadExample.runUsingFixedThreadPool(action, availableProcessors);

    System.out.println("virtualThreads: " + virtualThreads);
    System.out.println("fixedThreadPool: " + fixedThreadPool);
    Assertions.assertTrue(virtualThreads > fixedThreadPool);
  }

  /**
   * Increasing the amount of fixedThread pool makes the execution slower
   */
  @Test
  void fixed_increasedFixed_readFiles() throws InterruptedException {
    MyVirtualThreadExample.isSleepingEnabled = false;

    Runnable action = MyVirtualThreadExample::readFileLines;
    final int availableProcessors = Runtime.getRuntime().availableProcessors();

    final long fixedThreadPool = MyVirtualThreadExample.runUsingFixedThreadPool(action, availableProcessors);
    final long moreFixedThreadPool = MyVirtualThreadExample.runUsingFixedThreadPool(action, availableProcessors * 500);

    System.out.println("fixedThreadPool: " + fixedThreadPool);
    System.out.println("moreFixedThreadPool: " + moreFixedThreadPool);
    Assertions.assertTrue(fixedThreadPool < moreFixedThreadPool);
  }

  /**
   * Increasing the amount of fixedThread pool makes the execution slower
   */
  @Test
  void fixed_increasedFixed_httpRequest() throws InterruptedException {
    MyVirtualThreadExample.isSleepingEnabled = false;
    MyVirtualThreadExample.MAX = 5_000;

    Runnable action = MyVirtualThreadExample::sendHttpRequest;
    final int availableProcessors = Runtime.getRuntime().availableProcessors();

    final long fixedThreadPool = MyVirtualThreadExample.runUsingFixedThreadPool(action, availableProcessors);
    final long moreFixedThreadPool = MyVirtualThreadExample.runUsingFixedThreadPool(action, availableProcessors * 500);

    System.out.println("fixedThreadPool: " + fixedThreadPool);
    System.out.println("moreFixedThreadPool: " + moreFixedThreadPool);
    Assertions.assertTrue(fixedThreadPool < moreFixedThreadPool);
  }

  /**
   * Due the Thread.sleep the virtual threads do a better job by doing another task while waiting for the current task
   */
  @Test
  void virtualAreFaster_httpRequest() throws InterruptedException {
    Runnable action = MyVirtualThreadExample::sendHttpRequest;
    final int availableProcessors = Runtime.getRuntime().availableProcessors();

    final long virtualThreads = MyVirtualThreadExample.runUsingVirtualThreads(action);
    final long fixedThreadPool = MyVirtualThreadExample.runUsingFixedThreadPool(action, availableProcessors);

    System.out.println("virtualThreads: " + virtualThreads);
    System.out.println("fixedThreadPool: " + fixedThreadPool);
    Assertions.assertTrue(virtualThreads < fixedThreadPool);
  }

  public static class MyVirtualThreadExample {
    public static int MAX = 25;
    public static boolean isSleepingEnabled = true;

    public static long runUsingFixedThreadPool(Runnable task, int value) throws InterruptedException {
      return run(Executors.newFixedThreadPool(value), task);
    }

    public static long runUsingVirtualThreads(Runnable task) throws InterruptedException {
      return run(Executors.newVirtualThreadPerTaskExecutor(), task);
    }

    public static long run(ExecutorService executors, Runnable task) throws InterruptedException {
      long startFixed = System.currentTimeMillis();
      for (int i = 0; i < MAX; i++) {
        executors.submit(task);
      }
      executors.shutdown();
      executors.awaitTermination(10, TimeUnit.SECONDS);
      long endFixed = System.currentTimeMillis();
      return endFixed - startFixed;
    }

    public static void readFileLines() {
      try {
        Files.lines(new File("src/test/java/org/example/VirtualThreadsTest.java").toPath());
        if (isSleepingEnabled) {
          Thread.sleep(500);
        }
      } catch (Exception e) {

      }
    }

    public static void sendHttpRequest() {
      try {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(URI.create("http://localhost:3000/template"))
                                         .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (isSleepingEnabled) {
          Thread.sleep(500);
        }
      } catch (IOException | InterruptedException e) {
      }
    }

  }

}


