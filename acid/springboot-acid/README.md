## 🔁 Testing the Transaction
1. Start the Spring Boot app.
2. Go to http://localhost:8080/h2-console (JDBC URL: jdbc:h2:mem:bankdb).
3. Check balances:
   ```
   SELECT * FROM ACCOUNTS;
   ```
4. Make a transfer:
   ```
   curl -X POST -H 'Content-Type: application/json' -d '{"from":"Alice","to":"Bob", "amount": 100}' http://localhost:8080/api/v1/transfer
   Transfer successful
   
   curl -X POST -H 'Content-Type: application/json' -d '{"from":"Alice","to":"Bob", "amount": 600}' http://localhost:8080/api/v1/transfer
   Insufficient funds
   ```
5. Refresh H2 console — balances will update if successful.
6. Try forcing a failure by uncommenting the simulated error — check that balances are not changed (atomic rollback).

## ✅ Updated Example: Demonstrating Isolation
### 🧪 Expected Output (in logs):
```
2025-05-29T22:44:25.705+07:00  INFO 33394 --- [springboot-acid] [       Thread-1] com.example.acid.service.BankService     : Thread-1 started
2025-05-29T22:44:25.705+07:00  INFO 33394 --- [springboot-acid] [       Thread-2] com.example.acid.service.BankService     : Thread-2 started
2025-05-29T22:44:25.843+07:00 ERROR 33394 --- [springboot-acid] [       Thread-2] com.example.acid.ConcurrencyTestRunner   : Thread-2 failed -> Insufficient funds
2025-05-29T22:44:27.844+07:00  INFO 33394 --- [springboot-acid] [       Thread-1] com.example.acid.service.BankService     : Thread-1 committed
20
```

✅ Only one thread should succeed, because isolation level `REPEATABLE_READ` prevents dirty/non-repeatable reads of Alice's balance between the two concurrent threads.

### 🔍 Isolation Levels Recap:
| Level             | Prevents                             | Description                     |
| ----------------- | ------------------------------------ | ------------------------------- |
| `READ_COMMITTED`  | Dirty reads                          | Default in many DBs             |
| `REPEATABLE_READ` | Dirty + non-repeatable reads         | Ensures same data is read again |
| `SERIALIZABLE`    | Phantom reads, most strict isolation | Full transaction isolation      |

### ✅ Summary
- The `@Transactional(isolation = Isolation.REPEATABLE_READ)` ensures isolation.
- Simulated concurrency shows transactional conflicts.
- You can switch to `Isolation.READ_COMMITTED` to observe race conditions.
