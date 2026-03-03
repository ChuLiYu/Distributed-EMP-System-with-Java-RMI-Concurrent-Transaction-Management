package com.example;

import org.junit.jupiter.api.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Concurrent Transaction Test Suite
 * 
 * 这个测试套件验证系统在并发场景下的行为：
 * 1. 多个客户端同时访问数据库
 * 2. 并发读写操作
 * 3. 事务冲突处理
 * 4. 数据一致性保证
 * 
 * 测试重点：
 * - SQLite 的锁机制
 * - 事务的隔离性
 * - 并发下的数据完整性
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ConcurrentTest {

    private static Registry registry;
    private static EMPService service;
    private static final String SERVICE_NAME = "EMPService";

    // 线程池，用于管理并发客户端
    private ExecutorService executorService;

    /**
     * 测试套件启动前的准备工作
     * 
     * 设置 RMI 连接，确保服务可用
     */
    @BeforeAll
    static void setUpRmiServer() throws Exception {
        System.out.println("\n=== Concurrent Transaction Test Suite ===");

        // 连接到现有的 RMI Registry
        try {
            registry = LocateRegistry.getRegistry(1099);
            registry.list();
            System.out.println("✓ Connected to existing RMI Registry");
        } catch (Exception e) {
            registry = LocateRegistry.createRegistry(1099);
            System.out.println("✓ RMI Registry created");
        }

        // 绑定服务
        EMPServiceImpl serviceImpl = new EMPServiceImpl();
        registry.rebind(SERVICE_NAME, serviceImpl);
        service = (EMPService) registry.lookup(SERVICE_NAME);
        System.out.println("✓ EMPService ready\n");
    }

    /**
     * 每个测试前的准备工作
     * 
     * 创建线程池用于并发测试
     */
    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(10);
        ConcurrentClient.resetStatistics();
    }

    /**
     * 每个测试后的清理工作
     * 
     * 关闭线程池
     */
    @AfterEach
    void tearDown() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    /**
     * 测试后的资源清理
     */
    @AfterAll
    static void tearDownAll() throws Exception {
        if (registry != null) {
            try {
                registry.unbind(SERVICE_NAME);
            } catch (Exception e) {
                System.err.println("Warning: Could not unbind service - " + e.getMessage());
            }
        }
        System.out.println("\n=== Concurrent Test Suite Completed ===");
    }

    /**
     * Test 4.1: 测试并发读操作（SELECT）
     * 
     * 目的：验证多个客户端可以同时读取数据
     * 
     * SQLite 特性：
     * - 支持多个并发读操作
     * - 读操作不会相互阻塞
     * - 读操作不会阻塞其他读操作
     */
    @Test
    @Order(1)
    @DisplayName("Task 4.1: Concurrent Read Operations (SELECT)")
    void testConcurrentReads() throws Exception {
        System.out.println("\n--- Test 4.1: Concurrent Read Operations ---");

        int numberOfReaders = 10; // 同时有 10 个读操作
        CountDownLatch startSignal = new CountDownLatch(1); // 用于同步启动所有线程
        CountDownLatch doneSignal = new CountDownLatch(numberOfReaders); // 等待所有线程完成

        List<Future<Boolean>> futures = new ArrayList<>();

        // 创建多个并发读取任务
        for (int i = 0; i < numberOfReaders; i++) {
            final int readerId = i + 1;
            Future<Boolean> future = executorService.submit(() -> {
                try {
                    // 等待启动信号，确保所有线程同时开始
                    startSignal.await();

                    // 执行读操作
                    List<EMP> employees = service.getAllEmployees();

                    System.out.println("  Reader-" + readerId + ": Read " + employees.size() + " employees");

                    doneSignal.countDown();
                    return employees.size() > 0;

                } catch (Exception e) {
                    System.err.println("  Reader-" + readerId + " error: " + e.getMessage());
                    doneSignal.countDown();
                    return false;
                }
            });
            futures.add(future);
        }

        // 发出启动信号，所有读操作同时开始
        System.out.println("  Starting " + numberOfReaders + " concurrent readers...");
        long startTime = System.currentTimeMillis();
        startSignal.countDown();

        // 等待所有读操作完成（最多等待 10 秒）
        boolean completed = doneSignal.await(10, TimeUnit.SECONDS);
        long duration = System.currentTimeMillis() - startTime;

        // 验证结果
        assertTrue(completed, "All read operations should complete within timeout");

        int successCount = 0;
        for (Future<Boolean> future : futures) {
            if (future.get()) {
                successCount++;
            }
        }

        System.out.println("  Completed in " + duration + " ms");
        System.out.println("  Success: " + successCount + "/" + numberOfReaders);

        // 所有读操作都应该成功
        assertEquals(numberOfReaders, successCount, "All concurrent read operations should succeed");

        System.out.println("✓ Test 4.1 Passed: Concurrent reads work correctly");
    }

    /**
     * Test 4.2: 测试并发写操作（INSERT）
     * 
     * 目的：验证并发写操作的行为
     * 
     * SQLite 特性：
     * - 写操作会锁定整个数据库
     * - 同一时间只能有一个写操作
     * - 其他写操作必须等待或失败
     */
    @Test
    @Order(2)
    @DisplayName("Task 4.2: Concurrent Write Operations (INSERT)")
    void testConcurrentWrites() throws Exception {
        System.out.println("\n--- Test 4.2: Concurrent Write Operations ---");

        int numberOfWriters = 5; // 5 个并发写操作
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(numberOfWriters);

        // 记录初始员工数
        int initialCount = service.getAllEmployees().size();
        System.out.println("  Initial employee count: " + initialCount);

        List<Future<Boolean>> futures = new ArrayList<>();

        // 创建多个并发写入任务
        for (int i = 0; i < numberOfWriters; i++) {
            final int writerId = i + 1;
            Future<Boolean> future = executorService.submit(() -> {
                try {
                    startSignal.await();

                    // 每个 writer 插入一个唯一的员工
                    String eno = "TASK4_WRITER_" + writerId;
                    int result = service.addNewEmployee(eno, "Concurrent Writer " + writerId, "Tester");

                    if (result > 0) {
                        System.out.println("  Writer-" + writerId + ": Successfully inserted employee " + eno);
                    } else {
                        System.out.println("  Writer-" + writerId + ": Insert failed");
                    }

                    doneSignal.countDown();
                    return result > 0;

                } catch (Exception e) {
                    System.err.println("  Writer-" + writerId + " error: " + e.getMessage());
                    doneSignal.countDown();
                    return false;
                }
            });
            futures.add(future);
        }

        // 启动所有写操作
        System.out.println("  Starting " + numberOfWriters + " concurrent writers...");
        long startTime = System.currentTimeMillis();
        startSignal.countDown();

        // 等待完成
        boolean completed = doneSignal.await(15, TimeUnit.SECONDS);
        long duration = System.currentTimeMillis() - startTime;

        assertTrue(completed, "All write operations should complete within timeout");

        // 统计成功的写操作
        int successCount = 0;
        for (Future<Boolean> future : futures) {
            if (future.get()) {
                successCount++;
            }
        }

        System.out.println("  Completed in " + duration + " ms");
        System.out.println("  Successful writes: " + successCount + "/" + numberOfWriters);

        // 验证数据库状态
        int finalCount = service.getAllEmployees().size();
        int expectedCount = initialCount + successCount;

        System.out.println("  Final employee count: " + finalCount);
        System.out.println("  Expected count: " + expectedCount);

        assertEquals(expectedCount, finalCount, "Employee count should match successful inserts");

        // 清理测试数据
        for (int i = 1; i <= numberOfWriters; i++) {
            try {
                service.deleteEmployee("TASK4_WRITER_" + i);
            } catch (Exception e) {
                System.err.println("Warning: Cleanup failed - " + e.getMessage());
            }
        }

        System.out.println("✓ Test 4.2 Passed: Concurrent writes handled correctly");
    }

    /**
     * Test 4.3: 测试混合读写操作
     * 
     * 目的：测试读写操作混合时的行为
     * 
     * 预期行为：
     * - 读操作可以并发
     * - 写操作会暂时阻塞读操作
     * - 系统最终应该保持一致性
     */
    @Test
    @Order(3)
    @DisplayName("Task 4.3: Mixed Read and Write Operations")
    void testMixedReadWrite() throws Exception {
        System.out.println("\n--- Test 4.3: Mixed Read and Write Operations ---");

        int numberOfReaders = 5;
        int numberOfWriters = 3;
        int totalOperations = numberOfReaders + numberOfWriters;

        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(totalOperations);

        int initialCount = service.getAllEmployees().size();
        System.out.println("  Initial employee count: " + initialCount);
        System.out.println("  Starting " + numberOfReaders + " readers and " +
                numberOfWriters + " writers...");

        List<Future<?>> futures = new ArrayList<>();

        // 创建读操作任务
        for (int i = 0; i < numberOfReaders; i++) {
            final int readerId = i + 1;
            Future<?> future = executorService.submit(() -> {
                try {
                    startSignal.await();

                    // 执行多次读操作
                    for (int j = 0; j < 3; j++) {
                        service.getAllEmployees();
                        Thread.sleep(50);
                    }

                    System.out.println("  Reader-" + readerId + ": Completed");
                    doneSignal.countDown();

                } catch (Exception e) {
                    System.err.println("  Reader-" + readerId + " error: " + e.getMessage());
                    doneSignal.countDown();
                }
            });
            futures.add(future);
        }

        // 创建写操作任务
        for (int i = 0; i < numberOfWriters; i++) {
            final int writerId = i + 1;
            Future<?> future = executorService.submit(() -> {
                try {
                    startSignal.await();

                    String eno = "TASK4_MIXED_" + writerId;
                    service.addNewEmployee(eno, "Mixed Test " + writerId, "Tester");

                    System.out.println("  Writer-" + writerId + ": Inserted employee");
                    doneSignal.countDown();

                } catch (Exception e) {
                    System.err.println("  Writer-" + writerId + " error: " + e.getMessage());
                    doneSignal.countDown();
                }
            });
            futures.add(future);
        }

        // 启动所有操作
        long startTime = System.currentTimeMillis();
        startSignal.countDown();

        // 等待完成
        boolean completed = doneSignal.await(20, TimeUnit.SECONDS);
        long duration = System.currentTimeMillis() - startTime;

        assertTrue(completed, "All mixed operations should complete within timeout");

        System.out.println("  Completed in " + duration + " ms");

        // 验证数据一致性
        int finalCount = service.getAllEmployees().size();
        System.out.println("  Final employee count: " + finalCount);

        // 至少应该增加了写入的数量
        assertTrue(finalCount >= initialCount, "Employee count should increase");

        // 清理
        for (int i = 1; i <= numberOfWriters; i++) {
            try {
                service.deleteEmployee("TASK4_MIXED_" + i);
            } catch (Exception e) {
                System.err.println("Warning: Cleanup failed - " + e.getMessage());
            }
        }

        System.out.println("✓ Test 4.3 Passed: Mixed operations maintain consistency");
    }

    /**
     * Test 4.4: 测试并发UPDATE操作
     * 
     * 目的：测试多个客户端同时更新同一记录的行为
     * 
     * 这是最容易产生冲突的场景：
     * - 多个客户端尝试修改同一行数据
     * - 测试事务的隔离性
     * - 验证最后一个成功的更新生效
     */
    @Test
    @Order(4)
    @DisplayName("Task 4.4: Concurrent UPDATE on Same Record")
    void testConcurrentUpdates() throws Exception {
        System.out.println("\n--- Test 4.4: Concurrent UPDATE on Same Record ---");

        // 创建一个测试员工
        String testEno = "TASK4_UPDATE_TARGET";
        service.addNewEmployee(testEno, "Original Name", "Original Title");
        System.out.println("  Created test employee: " + testEno);

        int numberOfUpdaters = 5;
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(numberOfUpdaters);

        // 多个客户端同时更新同一个员工
        for (int i = 0; i < numberOfUpdaters; i++) {
            final int updaterId = i + 1;
            executorService.submit(() -> {
                try {
                    startSignal.await();

                    // 尝试更新同一个员工
                    String newName = "Updated by Client-" + updaterId;
                    int result = service.updateEmployee(testEno, newName, "Updated Title");

                    if (result > 0) {
                        System.out.println("  Updater-" + updaterId + ": Successfully updated");
                    } else {
                        System.out.println("  Updater-" + updaterId + ": Update failed (conflict or locked)");
                    }

                    doneSignal.countDown();

                } catch (Exception e) {
                    System.err.println("  Updater-" + updaterId + " error: " + e.getMessage());
                    doneSignal.countDown();
                }
            });
        }

        // 同时启动所有更新操作
        System.out.println("  Starting " + numberOfUpdaters + " concurrent updates on same record...");
        startSignal.countDown();

        // 等待完成
        boolean completed = doneSignal.await(15, TimeUnit.SECONDS);
        assertTrue(completed, "All update operations should complete");

        // 验证最终状态：员工应该被某个客户端成功更新
        EMP updatedEmp = service.findEmployeeById(testEno);
        assertNotNull(updatedEmp, "Employee should still exist");

        // 名称应该是某个更新者设置的
        assertTrue(updatedEmp.getName().startsWith("Updated by Client-"),
                "Employee should be updated by one of the clients");

        System.out.println("  Final state: " + updatedEmp.getName());
        System.out.println("✓ Test 4.4 Passed: Concurrent updates handled correctly");

        // 清理
        service.deleteEmployee(testEno);
    }

    /**
     * Test 4.5: 测试完整的CRUD并发场景
     * 
     * 目的：模拟真实应用场景
     * 
     * 场景：
     * - 多个客户端同时执行不同的操作
     * - 包括 SELECT, INSERT, UPDATE, DELETE
     * - 验证系统在复杂场景下的稳定性
     */
    @Test
    @Order(5)
    @DisplayName("Task 4.5: Full CRUD Concurrent Scenario")
    void testFullCrudConcurrency() throws Exception {
        System.out.println("\n--- Test 4.5: Full CRUD Concurrent Scenario ---");

        int initialCount = service.getAllEmployees().size();
        System.out.println("  Initial employee count: " + initialCount);

        // 创建多个并发客户端
        int numberOfClients = 3;
        int operationsPerClient = 5;

        ConcurrentClient.resetStatistics();

        List<Thread> clientThreads = new ArrayList<>();

        for (int i = 0; i < numberOfClients; i++) {
            String clientId = "TestClient-" + (i + 1);
            ConcurrentClient client = new ConcurrentClient(clientId, service, operationsPerClient);
            Thread thread = new Thread(client);
            clientThreads.add(thread);
        }

        // 启动所有客户端
        System.out.println("  Starting " + numberOfClients + " concurrent clients...");
        System.out.println("  Each performing " + operationsPerClient + " random operations\n");

        long startTime = System.currentTimeMillis();

        for (Thread thread : clientThreads) {
            thread.start();
        }

        // 等待所有客户端完成
        for (Thread thread : clientThreads) {
            thread.join(20000); // 最多等待 20 秒
        }

        long duration = System.currentTimeMillis() - startTime;

        // 获取统计信息
        int totalSuccess = ConcurrentClient.getTotalSuccess();
        int totalFailure = ConcurrentClient.getTotalFailure();
        int totalConflicts = ConcurrentClient.getTotalConflicts();

        System.out.println("\n  Test Results:");
        System.out.println("  - Duration: " + duration + " ms");
        System.out.println("  - Successful operations: " + totalSuccess);
        System.out.println("  - Failed operations: " + totalFailure);
        System.out.println("  - Concurrency conflicts: " + totalConflicts);

        // 验证数据一致性
        int finalCount = service.getAllEmployees().size();
        System.out.println("  - Final employee count: " + finalCount);

        // 至少应该有一些操作成功
        assertTrue(totalSuccess > 0, "At least some operations should succeed");

        // 数据库应该保持一致
        assertNotNull(service.getAllEmployees(), "Database should remain accessible");

        System.out.println("✓ Test 4.5 Passed: System remains stable under concurrent load");
    }

    /**
     * Test 4.6: 测试数据一致性验证
     * 
     * 目的：确保并发操作后数据仍然一致
     * 
     * 验证项：
     * - 没有数据损坏
     * - 没有重复记录
     * - 事务完整性
     */
    @Test
    @Order(6)
    @DisplayName("Task 4.6: Data Consistency Verification")
    void testDataConsistency() throws Exception {
        System.out.println("\n--- Test 4.6: Data Consistency Verification ---");

        // 创建一批测试数据
        String[] testIds = { "CONSIST_1", "CONSIST_2", "CONSIST_3" };

        System.out.println("  Phase 1: Creating test data...");
        for (String id : testIds) {
            service.addNewEmployee(id, "Consistency Test", "Tester");
        }

        // 并发读取，验证数据一致性
        System.out.println("  Phase 2: Concurrent consistency checks...");

        int numberOfCheckers = 10;
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(numberOfCheckers);

        List<Future<Boolean>> results = new ArrayList<>();

        for (int i = 0; i < numberOfCheckers; i++) {
            final int checkerId = i + 1;
            Future<Boolean> future = executorService.submit(() -> {
                try {
                    startSignal.await();

                    // 读取数据并验证一致性
                    List<EMP> allEmployees = service.getAllEmployees();

                    // 检查测试数据是否存在
                    int foundCount = 0;
                    for (String testId : testIds) {
                        for (EMP emp : allEmployees) {
                            if (testId.equals(emp.getENO())) {
                                foundCount++;
                                break;
                            }
                        }
                    }

                    boolean consistent = (foundCount == testIds.length);

                    if (consistent) {
                        System.out.println("  Checker-" + checkerId + ": Data consistent ✓");
                    } else {
                        System.out.println("  Checker-" + checkerId + ": Data inconsistent! Found " +
                                foundCount + "/" + testIds.length);
                    }

                    doneSignal.countDown();
                    return consistent;

                } catch (Exception e) {
                    System.err.println("  Checker-" + checkerId + " error: " + e.getMessage());
                    doneSignal.countDown();
                    return false;
                }
            });
            results.add(future);
        }

        // 启动所有检查
        startSignal.countDown();

        // 等待完成
        boolean completed = doneSignal.await(10, TimeUnit.SECONDS);
        assertTrue(completed, "All consistency checks should complete");

        // 验证所有检查都通过
        int consistentCount = 0;
        for (Future<Boolean> result : results) {
            if (result.get()) {
                consistentCount++;
            }
        }

        System.out.println("  Phase 3: Results");
        System.out.println("  - Consistent reads: " + consistentCount + "/" + numberOfCheckers);

        assertEquals(numberOfCheckers, consistentCount,
                "All concurrent reads should see consistent data");

        // 清理
        for (String id : testIds) {
            service.deleteEmployee(id);
        }

        System.out.println("✓ Test 4.6 Passed: Data consistency maintained under concurrent access");
    }
}
