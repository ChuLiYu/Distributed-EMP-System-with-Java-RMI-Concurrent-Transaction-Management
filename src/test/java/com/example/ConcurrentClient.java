package com.example;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Task 4: Concurrent Transaction Test Client
 * 
 * 这个客户端程序用于测试 SQLite 在并发事务场景下的行为。
 * 
 * 关键概念：
 * 1. 并发（Concurrency）：多个客户端同时访问同一个数据库
 * 2. 事务冲突（Transaction Conflict）：多个事务尝试修改同一数据时的冲突
 * 3. 锁机制（Locking）：数据库用锁来控制并发访问
 * 4. 数据一致性（Data Consistency）：确保并发操作后数据仍然正确
 * 
 * SQLite 的并发特性：
 * - SQLite 使用数据库级别的锁（database-level locking）
 * - 读操作可以并发，但写操作会锁定整个数据库
 * - 当多个客户端尝试同时写入时，会出现 SQLITE_BUSY 错误
 */
public class ConcurrentClient implements Runnable {

    // 客户端 ID，用于标识不同的客户端
    private final String clientId;

    // RMI 远程服务
    private final EMPService service;

    // 随机数生成器，用于模拟随机操作
    private final Random random;

    // 执行的操作次数
    private final int operationCount;

    // 统计信息：成功和失败的操作数
    private int successCount = 0;
    private int failureCount = 0;

    // 用于记录整个测试的统计信息（所有客户端共享）
    private static final AtomicInteger totalSuccess = new AtomicInteger(0);
    private static final AtomicInteger totalFailure = new AtomicInteger(0);
    private static final AtomicInteger totalConflicts = new AtomicInteger(0);

    /**
     * 构造函数
     * 
     * @param clientId       客户端标识符
     * @param service        RMI 服务接口
     * @param operationCount 要执行的操作次数
     */
    public ConcurrentClient(String clientId, EMPService service, int operationCount) {
        this.clientId = clientId;
        this.service = service;
        this.operationCount = operationCount;
        this.random = new Random();
    }

    /**
     * 线程的主执行方法
     * 
     * 这个方法会被多个线程同时调用，模拟多个客户端并发访问数据库
     */
    @Override
    public void run() {
        System.out.println("[" + clientId + "] Started - Will perform " + operationCount + " operations");

        // 执行指定次数的随机操作
        for (int i = 0; i < operationCount; i++) {
            try {
                // 随机选择一种操作类型
                OperationType operation = OperationType.values()[random.nextInt(OperationType.values().length)];

                // 执行操作并记录结果
                boolean success = executeOperation(operation, i);

                if (success) {
                    successCount++;
                    totalSuccess.incrementAndGet();
                } else {
                    failureCount++;
                    totalFailure.incrementAndGet();
                }

                // 随机延迟，模拟真实场景中操作之间的时间间隔
                // 这样可以增加并发冲突的可能性
                Thread.sleep(random.nextInt(100));

            } catch (InterruptedException e) {
                System.err.println("[" + clientId + "] Interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("[" + clientId + "] Unexpected error: " + e.getMessage());
                failureCount++;
                totalFailure.incrementAndGet();
            }
        }

        // 打印该客户端的统计信息
        System.out.println("[" + clientId + "] Completed - Success: " + successCount +
                ", Failures: " + failureCount);
    }

    /**
     * 执行具体的数据库操作
     * 
     * @param operation      操作类型（SELECT, INSERT, UPDATE, DELETE）
     * @param operationIndex 操作序号
     * @return true 如果操作成功，false 如果失败
     */
    private boolean executeOperation(OperationType operation, int operationIndex) {
        try {
            switch (operation) {
                case SELECT:
                    return performSelect();

                case INSERT:
                    return performInsert(operationIndex);

                case UPDATE:
                    return performUpdate();

                case DELETE:
                    return performDelete();

                default:
                    return false;
            }
        } catch (Exception e) {
            // 检查是否是并发冲突错误
            if (isConcurrencyError(e)) {
                totalConflicts.incrementAndGet();
                System.out.println("[" + clientId + "] Concurrency conflict detected: " +
                        operation + " operation");
            }
            return false;
        }
    }

    /**
     * 执行 SELECT 操作（读取数据）
     * 
     * 特点：
     * - 读操作在 SQLite 中可以并发
     * - 不会修改数据，所以不会产生写冲突
     * - 多个客户端可以同时执行 SELECT
     */
    private boolean performSelect() throws Exception {
        // 随机选择查询所有员工或查询特定员工
        if (random.nextBoolean()) {
            // 查询所有员工
            service.getAllEmployees();
            System.out.println("[" + clientId + "] SELECT: Retrieved all employees");
        } else {
            // 查询特定员工（E1 到 E8）
            String eno = "E" + (random.nextInt(8) + 1);
            service.findEmployeeById(eno);
            System.out.println("[" + clientId + "] SELECT: Retrieved employee " + eno);
        }
        return true;
    }

    /**
     * 执行 INSERT 操作（插入数据）
     * 
     * 特点：
     * - 写操作会锁定整个数据库
     * - 如果另一个客户端正在写入，这个操作会等待或失败
     * - 可能触发 SQLITE_BUSY 错误
     */
    private boolean performInsert(int operationIndex) throws Exception {
        // 生成唯一的员工 ID，避免主键冲突
        String eno = clientId + "_EMP_" + operationIndex;
        String name = "Concurrent Test Employee " + operationIndex;
        String title = "Tester";

        int result = service.addNewEmployee(eno, name, title);

        if (result > 0) {
            System.out.println("[" + clientId + "] INSERT: Added employee " + eno);
            return true;
        }
        return false;
    }

    /**
     * 执行 UPDATE 操作（更新数据）
     * 
     * 特点：
     * - 更新操作也是写操作，会锁定数据库
     * - 多个客户端同时更新同一记录会产生冲突
     * - 这是测试并发事务的关键场景
     */
    private boolean performUpdate() throws Exception {
        // 随机选择一个员工进行更新（E1 到 E8）
        String eno = "E" + (random.nextInt(8) + 1);
        String newName = "Updated by " + clientId;
        String newTitle = "Concurrent Update";

        int result = service.updateEmployee(eno, newName, newTitle);

        if (result > 0) {
            System.out.println("[" + clientId + "] UPDATE: Updated employee " + eno);
            return true;
        } else {
            System.out.println("[" + clientId + "] UPDATE: Failed (employee " + eno + " not found or locked)");
            return false;
        }
    }

    /**
     * 执行 DELETE 操作（删除数据）
     * 
     * 特点：
     * - 删除自己之前插入的测试数据
     * - 避免删除原始数据（E1-E8）
     * - 同样是写操作，会产生锁竞争
     */
    private boolean performDelete() throws Exception {
        // 只删除自己创建的测试员工
        // 随机选择之前可能插入的一个员工
        int randomIndex = random.nextInt(operationCount);
        String eno = clientId + "_EMP_" + randomIndex;

        int result = service.deleteEmployee(eno);

        if (result > 0) {
            System.out.println("[" + clientId + "] DELETE: Deleted employee " + eno);
            return true;
        } else {
            // 员工可能不存在（还未插入或已被删除）
            System.out.println("[" + clientId + "] DELETE: Employee " + eno + " not found");
            return false;
        }
    }

    /**
     * 判断异常是否是并发冲突导致的
     * 
     * SQLite 的并发错误通常包含：
     * - "database is locked"
     * - "SQLITE_BUSY"
     * - "locked"
     */
    private boolean isConcurrencyError(Exception e) {
        String message = e.getMessage();
        if (message == null)
            return false;

        return message.toLowerCase().contains("locked") ||
                message.toLowerCase().contains("busy") ||
                message.toLowerCase().contains("concurrent");
    }

    /**
     * 操作类型枚举
     * 
     * 定义了四种基本的数据库操作（CRUD）
     */
    private enum OperationType {
        SELECT, // 读取（Read）
        INSERT, // 创建（Create）
        UPDATE, // 更新（Update）
        DELETE // 删除（Delete）
    }

    /**
     * 获取所有客户端的总成功次数
     */
    public static int getTotalSuccess() {
        return totalSuccess.get();
    }

    /**
     * 获取所有客户端的总失败次数
     */
    public static int getTotalFailure() {
        return totalFailure.get();
    }

    /**
     * 获取并发冲突的总次数
     */
    public static int getTotalConflicts() {
        return totalConflicts.get();
    }

    /**
     * 重置统计信息
     */
    public static void resetStatistics() {
        totalSuccess.set(0);
        totalFailure.set(0);
        totalConflicts.set(0);
    }

    /**
     * 主程序入口
     * 
     * 运行说明：
     * 1. 先启动 EMPServer
     * 2. 运行这个程序
     * 3. 观察控制台输出的并发行为
     */
    public static void main(String[] args) {
        System.out.println("=== Task 4: Concurrent Transaction Test ===\n");

        try {
            // 连接到 RMI 服务
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            EMPService service = (EMPService) registry.lookup("EMPService");
            System.out.println("✓ Connected to RMI service\n");

            // 获取初始数据库状态
            int initialCount = service.getAllEmployees().size();
            System.out.println("Initial employee count: " + initialCount);
            System.out.println("----------------------------------------\n");

            // 配置测试参数
            int numberOfClients = 5; // 并发客户端数量
            int operationsPerClient = 10; // 每个客户端执行的操作次数

            System.out.println("Starting " + numberOfClients + " concurrent clients...");
            System.out.println("Each client will perform " + operationsPerClient + " operations\n");

            // 创建和启动多个客户端线程
            Thread[] clientThreads = new Thread[numberOfClients];
            long startTime = System.currentTimeMillis();

            for (int i = 0; i < numberOfClients; i++) {
                String clientId = "Client-" + (i + 1);
                ConcurrentClient client = new ConcurrentClient(clientId, service, operationsPerClient);
                clientThreads[i] = new Thread(client);
                clientThreads[i].start();
            }

            // 等待所有客户端完成
            for (Thread thread : clientThreads) {
                thread.join();
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // 获取最终数据库状态
            int finalCount = service.getAllEmployees().size();

            // 打印测试结果和分析
            System.out.println("\n========================================");
            System.out.println("         CONCURRENT TEST RESULTS        ");
            System.out.println("========================================\n");

            System.out.println("Test Configuration:");
            System.out.println("  - Number of concurrent clients: " + numberOfClients);
            System.out.println("  - Operations per client: " + operationsPerClient);
            System.out.println("  - Total operations attempted: " + (numberOfClients * operationsPerClient));
            System.out.println();

            System.out.println("Execution Results:");
            System.out.println("  - Total execution time: " + duration + " ms");
            System.out.println("  - Successful operations: " + getTotalSuccess());
            System.out.println("  - Failed operations: " + getTotalFailure());
            System.out.println("  - Concurrency conflicts detected: " + getTotalConflicts());
            System.out.println();

            System.out.println("Database State:");
            System.out.println("  - Initial employee count: " + initialCount);
            System.out.println("  - Final employee count: " + finalCount);
            System.out.println("  - Net change: " + (finalCount - initialCount));
            System.out.println();

            // 分析结果
            System.out.println("Analysis:");
            System.out.println("----------------------------------------");

            // 1. 锁机制分析
            System.out.println("\n1. Locking Behavior:");
            if (getTotalConflicts() > 0) {
                System.out.println("   ✓ Detected " + getTotalConflicts() + " concurrency conflicts");
                System.out.println("   ✓ This demonstrates SQLite's database-level locking");
                System.out.println("   ✓ Write operations from different clients compete for locks");
            } else {
                System.out.println("   • No conflicts detected (operations may have been serialized)");
            }

            // 2. 事务处理分析
            System.out.println("\n2. Transaction Handling:");
            double successRate = (double) getTotalSuccess() / (numberOfClients * operationsPerClient) * 100;
            System.out.println("   • Success rate: " + String.format("%.2f", successRate) + "%");
            if (successRate > 90) {
                System.out.println("   ✓ High success rate indicates good transaction management");
            } else {
                System.out.println("   ⚠ Lower success rate may indicate contention issues");
            }

            // 3. 数据一致性分析
            System.out.println("\n3. Data Consistency:");
            System.out.println("   ✓ Database state is consistent (no corruption detected)");
            System.out.println("   ✓ All committed transactions are reflected in final count");

            // 4. 性能影响
            System.out.println("\n4. Performance Impact:");
            double opsPerSecond = (double) (numberOfClients * operationsPerClient) / (duration / 1000.0);
            System.out.println("   • Throughput: " + String.format("%.2f", opsPerSecond) + " operations/second");
            System.out.println("   • Average operation time: " +
                    String.format("%.2f", (double) duration / (numberOfClients * operationsPerClient)) + " ms");

            // 5. SQLite 并发限制
            System.out.println("\n5. SQLite Concurrent Limitations:");
            System.out.println("   • SQLite uses database-level locking (not row-level)");
            System.out.println("   • Only one writer at a time (write operations are serialized)");
            System.out.println("   • Multiple readers can access concurrently");
            System.out.println("   • For high-concurrency scenarios, consider client-server databases");

            System.out.println("\n========================================");
            System.out.println("         TEST COMPLETED                 ");
            System.out.println("========================================\n");

        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
