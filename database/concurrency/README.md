# Optimistic Locking vs Pessimistic Locking
Optimistic locking and pessimistic locking are two approaches used in concurrency control to manage access to shared resources, such as database records, in multi-user environments. 
Here's a comparison of the two:

1. Optimistic Locking:
- **Approach**: Optimistic locking assumes that conflicts between concurrent transactions are rare. It allows multiple transactions to access and modify data concurrently without acquiring locks upfront.
- **Mechanism**: Each transaction reads data and notes a version number or timestamp associated with the data it reads. When a transaction wants to update the data, it checks if the version or timestamp has changed since it was read. If no changes are detected, the transaction proceeds with the update. If changes are detected, it indicates a conflict, and the transaction can be retried or aborted.
- **Advantages**:
  - Allows for higher concurrency since transactions do not hold locks during read operations.
  - Reduces contention and waiting time for locks, improving system performance.
- **Disadvantages**:
  - May lead to more aborted transactions and retries in case of conflicts, especially in highly concurrent environments.
  - Requires careful handling of conflicts and retries in application logic.

2. Pessimistic Locking:
- **Approach**: Pessimistic locking assumes that conflicts between concurrent transactions are more likely. It acquires locks on resources before allowing transactions to access or modify them, preventing other transactions from accessing the locked resources until the lock is released.
- **Mechanism**: When a transaction wants to read or update data, it first acquires an exclusive lock (write lock) or a shared lock (read lock) on the resource. Other transactions requesting conflicting locks are forced to wait until the lock is released.
- **Advantages**:
  - Provides stronger consistency guarantees by preventing concurrent access to resources.
  - Reduces the likelihood of conflicts and concurrency-related issues.
- **Disadvantages**:
  - Can lead to increased contention and reduced concurrency, especially in scenarios with frequent concurrent access to the same resources.
  - May cause blocking and waiting for locks, potentially affecting system performance.

In summary, optimistic locking is suitable for scenarios where conflicts are infrequent and high concurrency is desired, but it requires careful handling of conflicts and potential retries. On the other hand, pessimistic locking provides stronger consistency guarantees but may lead to increased contention and reduced concurrency. Choosing the appropriate locking strategy depends on the specific requirements, concurrency patterns, and trade-offs of the application.

# Implementation of both Optimistic and Pessimistic Locking using Spring Boot and JPA/Hibernate
## ✅ 1. Optimistic Locking with Spring Boot
### 💡 Key Idea:
Optimistic locking uses a `@Version` field to detect concurrent modifications. If two transactions try to update the same record, Hibernate throws an `OptimisticLockException`.

### ✅ Entity Example
```
import jakarta.persistence.*;

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int quantity;

    @Version
    private int version; // Optimistic Locking Version

    // Getters and Setters
}
```

### ✅ Repository
```
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
```

### ✅ Service Method
```
import jakarta.persistence.OptimisticLockException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public void updateProductQuantity(Long productId, int newQuantity) {
        try {
            Product product = productRepository.findById(productId).orElseThrow();
            product.setQuantity(newQuantity);
            productRepository.save(product); // JPA checks the version during update
        } catch (OptimisticLockException e) {
            throw new RuntimeException("Conflict: Product was modified by another transaction", e);
        }
    }
}
```

## ✅ 2. Pessimistic Locking with Spring Boot
### 💡 Key Idea:
Use explicit locking (e.g., `PESSIMISTIC_WRITE`) to lock a row and prevent concurrent updates.

### ✅ Modify Repository
```
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);
}
```

### ✅ Service Method
```
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public void updateProductWithLock(Long productId, int newQuantity) {
        Product product = productRepository.findByIdForUpdate(productId)
                                           .orElseThrow();
        product.setQuantity(newQuantity);
        productRepository.save(product);
    }
}
```

## 🆚 Summary: Optimistic vs Pessimistic
| Feature            | Optimistic Locking                    | Pessimistic Locking                      |
| ------------------ | ------------------------------------- | ---------------------------------------- |
| Conflict Detection | At commit time (via version field)    | At read time (lock prevents others)      |
| Locking Strategy   | No actual DB lock; uses version check | Locks DB row until transaction completes |
| Performance        | Better for low contention             | Better for high contention               |
| Exception Handling | `OptimisticLockException`             | May lead to deadlocks or timeouts        |

