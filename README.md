# Distributed EMP System — Java RMI & Concurrent Transaction Management

A distributed employee management system built with **Java RMI**, demonstrating client-server architecture, JDBC transaction management, and concurrent database access patterns on **SQLite**.

> Course project for **CSCI 7785: Distributed Database Systems** | M.Sc. Computer Science, FDU

---

## 🏗️ Architecture Overview

```
┌──────────────────────┐        Java RMI         ┌──────────────────────────┐
│   RMI Client 1       │ ◄───────────────────────► │                         │
│   RMI Client 2       │ ◄───────────────────────► │   RMI Server             │
│   RMI Client N       │ ◄───────────────────────► │   (EMPServiceImpl)        │
└──────────────────────┘                          │          │               │
                                                  │          ▼               │
                                                  │   JDBC + SQLite (db)     │
                                                  └──────────────────────────┘
```

---

## ✨ Core Features

### **Task 1: Non-Distributed Baseline**
- Local JDBC queries on EMP table
- Direct database access via DAO pattern
- Supports CRUD operations (Create, Read, Update, Delete)

### **Task 2: Java RMI Distributed System**
- Remote method invocation for employee queries
- Object serialization (EMP, List<EMP>)
- Support for multiple concurrent clients
- RMI registry management (port 1099)

### **Task 3: Manual Transaction Control**
- Explicit `conn.setAutoCommit(false)` on all operations
- Manual `commit()` on successful operations
- Automatic `rollback()` on errors
- Transaction logging and monitoring

### **Task 4: Concurrent Transaction Testing**
- Multi-threaded client simulation
- Concurrent read/write operation stress testing
- Observation of SQLite locking behavior
- Data consistency validation under load
- Analysis of transaction conflicts and serialization

---

## 📦 Project Structure

```
├── src/
│   ├── main/java/com/example/
│   │   ├── EMPService.java           # Remote interface definition
│   │   ├── EMPServiceImpl.java        # Remote implementation
│   │   ├── EMPServer.java            # RMI registry & bootstrap
│   │   ├── EMPClient.java            # Interactive client
│   │   ├── EMPDAO.java               # Data access object
│   │   ├── EMPController.java        # Business logic controller
│   │   ├── DBConnection.java         # SQLite connection factory
│   │   ├── EMP.java                  # Data model (Serializable)
│   │   └── EmpDBConsoleApp.java      # Local console app
│   │
│   └── test/java/com/example/
│       ├── RmiTest.java              # Task 2: RMI functionality tests
│       ├── RmiSerializationTest.java # Object serialization tests
│       ├── TransactionTest.java      # Task 3: Transaction & rollback tests
│       ├── ConcurrentTest.java       # Task 4: Concurrent access tests
│       ├── ConcurrentClient.java     # Concurrent client implementation
│       ├── SimpleDbDemo.java         # Basic database test
│       ├── DirectDbDemo.java         # Direct JDBC test (no RMI)
│       └── ManualTransactionDemo.java # Transaction demo
│
├── pom.xml                           # Maven configuration (Java 17)
├── CSCI7785_database.db              # SQLite database
└── README.md
```

---

## 🛠️ Tech Stack

| Component | Technology | Version |
|-----------|------------|---------|
| **Language** | Java | 17 |
| **Build Tool** | Maven | 3.9.12 |
| **RMI Framework** | Java RMI | Built-in |
| **Database** | SQLite | 3.x |
| **JDBC Driver** | sqlite-jdbc | 3.49.1.0 |
| **Testing** | JUnit 5 | 5.9.3 |

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.9+
- SQLite 3.x

### 1. Clone & Build

```bash
git clone https://github.com/ChuLiYu/Distributed-EMP-System-with-Java-RMI-Concurrent-Transaction-Management.git
cd demo

# Compile
mvn clean compile

# Package
mvn package
```

### 2. Start RMI Server

```bash
# Terminal 1: Start RMI Server
mvn exec:java -Dexec.mainClass="com.example.EMPServer"
# Output:
# ✓ RMI Registry started on port 1099
# ✓ EMPService bound to registry
# ✓ Server is ready...
```

### 3. Run Client(s)

```bash
# Terminal 2: Run Interactive Client
mvn exec:java -Dexec.mainClass="com.example.EMPClient"

# Or connect to remote server:
mvn exec:java -Dexec.mainClass="com.example.EMPClient" -Dexec.args="localhost"
```

### 4. Run Tests

```bash
# Run all tests
mvn test

# Run specific test suite
mvn test -Dtest=RmiTest
mvn test -Dtest=TransactionTest
mvn test -Dtest=ConcurrentTest

# Run with coverage
mvn test jacoco:report
```

---

## 📋 Test Suites

### **Task 2: RMI Tests** (`RmiTest.java`)
```
✓ Test 2.1: RMI Server Setup
✓ Test 2.2: Single EMP Object Serialization
✓ Test 2.3: List<EMP> Serialization
✓ Test 2.4: Remote CRUD Operations
✓ Test 2.5: Multiple Client Access
✓ Test 2.6: RMI Exception Handling
```

### **Task 3: Transaction Tests** (`TransactionTest.java`)
```
✓ Test 3.1: Verify Auto-Commit is Disabled
✓ Test 3.2: Manual Commit on Insert
✓ Test 3.3: Manual Commit on Update
✓ Test 3.4: Manual Commit on Delete
✓ Test 3.5: Rollback on Error (Duplicate Key)
✓ Test 3.6: Transaction Isolation
✓ Test 3.7: Data Consistency
✓ Test 3.8: Commit/Rollback Logging
```

### **Task 4: Concurrency Tests** (`ConcurrentTest.java`)
```
✓ Test 4.1: Concurrent Read Operations
✓ Test 4.2: Concurrent Write Operations
✓ Test 4.3: Mixed Read and Write Operations
✓ Test 4.4: Concurrent UPDATE on Same Record
✓ Test 4.5: Full CRUD Concurrent Scenario
✓ Test 4.6: Data Consistency Verification
```

---

## 🔄 Transaction Management

All write operations implement manual transaction control:

```java
Connection conn = DBConnection.getConnection();  // auto-commit = false
try {
    // Execute SQL statements
    PreparedStatement pstmt = conn.prepareStatement(...);
    pstmt.executeUpdate(...);
    
    conn.commit();  // Task 3: Manual commit
    System.out.println("✓ Transaction committed");
} catch (SQLException e) {
    conn.rollback();  // Task 3: Rollback on error
    System.out.println("✗ Transaction rolled back");
    throw e;
} finally {
    conn.close();
}
```

---

## 📊 Concurrency Observations

SQLite exhibits the following behavior under concurrent load:

| Scenario | SQLite Behavior | Implementation |
|----------|-----------------|-----------------|
| **Multiple concurrent reads** | ✅ Allowed simultaneously | Readers can proceed in parallel |
| **Concurrent read + write** | ⚠️ Write blocks readers | Write lock prevents read access |
| **Concurrent writes** | 🔒 Serialized (database lock) | Only one writer at a time |
| **Transaction conflicts** | Re-raises `SQLException` | Caught & logged, causes retry |
| **Data consistency** | ✅ ACID guaranteed | Rollback on conflict |

### Key Insights
- **Database-level locking**: SQLite locks the entire database for writes (not row-level)
- **Serialization**: Concurrent writes are serialized; second writer must wait or fail
- **Read scalability**: Multiple readers don't block each other
- **Isolation level**: Read-Committed (implicit in SQLite's design)

---

## 🎯 Usage Examples

### Interactive Client Menu
```
=== EMP Client Menu ===
1. List all employees
2. Find employee by ID
3. Add new employee
4. Update employee
5. Delete employee
6. Exit
```

### Programmatic Usage
```java
// Lookup remote service
Registry registry = LocateRegistry.getRegistry("localhost", 1099);
EMPService service = (EMPService) registry.lookup("EMPService");

// CRUD operations
List<EMP> employees = service.getAllEmployees();
EMP emp = service.findEmployeeById("E1");
service.addNewEmployee("E99", "New Employee", "Junior Developer");
service.updateEmployee("E99", "Senior Developer", "Manager");
service.deleteEmployee("E99");
```

### Concurrent Stress Test
```bash
mvn exec:java -Dexec.mainClass="com.example.ConcurrentClient"
# Outputs detailed statistics on:
# - Successful operations
# - Failed operations
# - Concurrency conflicts detected
# - Throughput (operations/second)
```

---

## 📈 Test Results Summary

After running the full test suite:

```
Test Configuration:
  - Java RMI: ✓ Functional
  - Object Serialization: ✓ Verified
  - Manual Transactions: ✓ Working
  - Concurrent Access: ✓ Stable
  - Data Consistency: ✓ Maintained

Concurrency Metrics:
  - Throughput: ~1000-2000 ops/sec
  - Lock contention: Medium (expected for SQLite)
  - Data corruption: None detected
  - Transaction rollbacks: Proper handling
```

---

## 🔧 Configuration

### Database URL
Set via environment variable or system property:

```bash
# Environment variable
export EMP_DB_URL="jdbc:sqlite:CSCI7785_database.db"

# Or system property
mvn test -Demp.db.url="jdbc:sqlite:CSCI7785_database.db"
```

### RMI Registry
- **Port**: 1099 (configurable in `EMPServer.java`)
- **Service Name**: `EMPService`

---

## 📚 Learning Outcomes

This project demonstrates:

1. **RMI Programming**
   - Remote interface design
   - Object serialization over network
   - Registry-based service lookup

2. **JDBC & Database Transactions**
   - Connection management
   - Manual transaction control
   - Error handling & rollback

3. **Concurrent Systems**
   - Multi-threaded client simulation
   - Lock contention analysis
   - Data consistency under load
   - Performance profiling

4. **Distributed Systems Patterns**
   - Client-server architecture
   - Service registry pattern
   - Stateless remote objects
   - Timeout & exception handling

---

## 📝 License

MIT License © 2026 [Lui Chu](https://github.com/ChuLiYu)

---

## 🤝 Contributing

This is an academic project. For improvements or questions, please open an issue on GitHub.

---

## 📞 Notes

- The system is designed for educational purposes to understand distributed systems concepts
- SQLite is suitable for demonstrations but not recommended for high-concurrency production systems
- For production, consider PostgreSQL, MySQL, or other enterprise databases
- RMI is Legacy Java technology; modern alternatives include gRPC, Spring Cloud, or REST APIs
