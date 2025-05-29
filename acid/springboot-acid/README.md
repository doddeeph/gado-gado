# 🔁 Testing the Transaction
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
