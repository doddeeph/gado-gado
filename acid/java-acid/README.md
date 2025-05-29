## 🧪 Output Example:
```
Before transaction:
Bob: $300
Alice: $500
Transfer $100 from Alice to Bob
>>> Starting transaction
>>> Transaction committed
After transaction:
Bob: $400
Alice: $400
```

## 🔄 If transaction fails (e.g., Alice tries to send $600):
```
Before transaction:
Bob: $400
Alice: $400
Transfer $600 from Alice to Bob
>>> Starting transaction
>>> Transaction failed. Rolled back. Reason: Insufficient funds in Alice's account
After transaction:
Bob: $400
Alice: $400
```

## ✅ Output ACID Isolation (Example)
```
[Thread-1] Before transaction:
Bob: $400
Alice: $400
[Thread-1] Transfer $100 from Alice to Bob
[Thread-1] >>> Starting transaction
[Thread-1] >>> Transaction committed
[Thread-1] >>> After transaction:
Bob: $500
Alice: $300
[Thread-2] Before transaction:
Bob: $500
Alice: $300
[Thread-2] Transfer $200 from Bob to Alice
[Thread-2] >>> Starting transaction
[Thread-2] >>> Transaction committed
[Thread-2] >>> After transaction:
Bob: $300
Alice: $500
```