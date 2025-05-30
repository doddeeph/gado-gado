# SQL (Structured Query Language)
SQL (`Structured Query Language`) is a powerful language used for managing and manipulating relational databases. Here's a basic tutorial covering some fundamental SQL queries:

## Fundamental SQL Queries
1. SELECT Statement:
   The SELECT statement is used to retrieve data from one or more tables.
```
-- Select all columns from a table
SELECT * FROM TableName;

-- Select specific columns
SELECT column1, column2 FROM TableName;

-- Select with conditions
SELECT * FROM TableName WHERE condition;
```

2. INSERT Statement:
   The INSERT statement is used to add new rows of data to a table.
```
INSERT INTO TableName (column1, column2) VALUES (value1, value2);
```

3. UPDATE Statement:
   The UPDATE statement is used to modify existing records in a table.
```
UPDATE TableName SET column1 = value1 WHERE condition;
```

4. DELETE Statement:
   The DELETE statement is used to remove rows from a table based on a condition.
```
DELETE FROM TableName WHERE condition;
```

5. CREATE TABLE Statement:
   The CREATE TABLE statement is used to create a new table.
```
CREATE TABLE TableName (
    column1 datatype,
    column2 datatype,
    PRIMARY KEY (column1)
);
```

6. ALTER TABLE Statement:
   The ALTER TABLE statement is used to modify an existing table (add, modify, or drop columns).
```
ALTER TABLE TableName
ADD new_column datatype;

ALTER TABLE TableName
MODIFY column_name new_datatype;

ALTER TABLE TableName
DROP COLUMN column_name;
```

7. Basic Joins:
   Joins are used to combine rows from two or more tables based on a related column between them.
```
-- INNER JOIN
SELECT * FROM table1 INNER JOIN table2 ON table1.column = table2.column;

-- LEFT JOIN
SELECT * FROM table1 LEFT JOIN table2 ON table1.column = table2.column;
```

8. Aggregation Functions:
   SQL provides various aggregate functions to perform calculations on a set of values.
```
-- Count rows
SELECT COUNT(column) FROM TableName;

-- Calculate average
SELECT AVG(column) FROM TableName;

-- Calculate sum
SELECT SUM(column) FROM TableName;
```

9. GROUP BY Clause:
   The GROUP BY clause groups rows that have the same values into summary rows.
```
SELECT column, COUNT(*) FROM TableName GROUP BY column;
```

10. ORDER BY Clause:
    The ORDER BY clause is used to sort the result set based on one or more columns.
```
SELECT * FROM TableName ORDER BY column1 ASC, column2 DESC;
```

## Advanced SQL Queries
11. Subqueries:
    A subquery is a query within another query.
```
-- Subquery in SELECT statement
SELECT column1, (SELECT MAX(column2) FROM AnotherTable) AS max_value
FROM TableName;

-- Subquery in WHERE clause
SELECT column1, column2
FROM TableName
WHERE column3 = (SELECT MAX(column3) FROM TableName);
```

12. LIKE Operator:
    The LIKE operator is used in a WHERE clause to search for a specified pattern in a column.
```
-- Select rows where column1 starts with 'prefix'
SELECT * FROM TableName WHERE column1 LIKE 'prefix%';

-- Select rows where column2 contains 'pattern'
SELECT * FROM TableName WHERE column2 LIKE '%pattern%';
```

13. BETWEEN Operator:
    The BETWEEN operator selects values within a given range.
```
-- Select rows where column1 is between 10 and 20
SELECT * FROM TableName WHERE column1 BETWEEN 10 AND 20;
```

14. IN Operator:
    The IN operator is used to specify multiple values in a WHERE clause.
```
-- Select rows where column1 is either 1, 2, or 3
SELECT * FROM TableName WHERE column1 IN (1, 2, 3);
```

15. EXISTS Operator:
    The EXISTS operator is used to check for the existence of rows in a subquery.
```
-- Select rows from TableName where corresponding rows exist in AnotherTable
SELECT * FROM TableName
WHERE EXISTS (SELECT 1 FROM AnotherTable WHERE TableName.id = AnotherTable.id);
```

16. Aggregate Functions with GROUP BY and HAVING:
    Combining aggregate functions with GROUP BY and HAVING clauses.
```
-- Count the number of records for each value in column1
SELECT column1, COUNT(*) as count
FROM TableName
GROUP BY column1
HAVING count > 1;
```

17. JOINs with Aliases:
    Using aliases to simplify table names in queries.
```
-- Inner join with aliases
SELECT t1.column, t2.column
FROM TableName1 AS t1
INNER JOIN TableName2 AS t2 ON t1.id = t2.id;
```

18. UNION Operator:
    The UNION operator is used to combine the result sets of two or more SELECT statements.
```
-- Combine results of two SELECT statements
SELECT column1 FROM TableName1
UNION
SELECT column1 FROM TableName2;
```

19. CASE Statement:
    The CASE statement performs conditional logic within a query.
```
SELECT column1,
    CASE
        WHEN column2 > 10 THEN 'High'
        WHEN column2 > 5 THEN 'Medium'
        ELSE 'Low'
    END AS priority
FROM TableName;
```

20. LIMIT/OFFSET (or FETCH/FIRST) for Pagination:
    Used to limit the number of rows returned and for pagination.
```
-- Retrieve the first 10 rows
SELECT * FROM TableName LIMIT 10;

-- Retrieve the next 10 rows (for pagination)
SELECT * FROM TableName LIMIT 10 OFFSET 10;
```

## SELECT Queries
21. Joining Multiple Tables:
```
-- Inner join on three tables
SELECT t1.column1, t2.column2, t3.column3
FROM Table1 t1
INNER JOIN Table2 t2 ON t1.id = t2.id
INNER JOIN Table3 t3 ON t1.id = t3.id;
```

22. Aggregating Data with GROUP BY:
```
-- Count the number of orders for each customer
SELECT customer_id, COUNT(order_id) AS order_count
FROM Orders
GROUP BY customer_id;
```

23. Filtering Aggregated Data with HAVING:
```
-- List customers with more than 2 orders
SELECT customer_id, COUNT(order_id) AS order_count
FROM Orders
GROUP BY customer_id
HAVING order_count > 2;
```

24. Subquery in FROM Clause:
```
-- Use a subquery to get the average order amount
SELECT customer_id, AVG(order_amount) AS avg_order_amount
FROM (SELECT customer_id, order_amount FROM Orders) AS subquery
GROUP BY customer_id;
```

25. Using DISTINCT to Get Unique Values:
```
-- Retrieve unique values from a column
SELECT DISTINCT column1 FROM TableName;
```
    
26. Sorting Data with ORDER BY and NULLs:
```
-- Order results by column1 in ascending order, NULLs last
SELECT * FROM TableName
ORDER BY column1 ASC NULLS LAST;
```

27. Date and Time Functions:
```
-- Get current date
SELECT CURRENT_DATE;   

-- Calculate age from date of birth
SELECT name, DATE_PART('year', CURRENT_DATE) - DATE_PART('year', date_of_birth) AS age
FROM Employees;
```

28. Using CASE for Conditional Logic:
```
-- Classify customers as 'Preferred' or 'Regular'
SELECT customer_id,
    CASE
        WHEN total_purchases > 1000 THEN 'Preferred'
        ELSE 'Regular'
    END AS customer_type
FROM CustomerSummary;
```

29. Window Functions:
```
-- Rank customers by total purchases
SELECT customer_id, total_purchases,
RANK() OVER (ORDER BY total_purchases DESC) AS purchase_rank
FROM CustomerSummary;
```

30. Self-Join:
```
-- Find employees and their managers
SELECT e.employee_id, e.employee_name, m.employee_name AS manager_name
FROM Employees e
JOIN Employees m ON e.manager_id = m.employee_id;
```

## INNER JOIN vs LEFT JOIN
The main difference between INNER JOIN and LEFT JOIN lies in how they handle unmatched rows between the tables involved in the join.

1. INNER JOIN:
- Returns only the rows where there is a match between the columns in both tables.
- If there is no match for a particular row in either table, that row is excluded from the result set.
- It focuses on the intersection of the tables based on the specified join condition.

Example:
```
SELECT employees.employee_id, employees.employee_name, departments.department_name
FROM employees
INNER JOIN departments ON employees.department_id = departments.department_id;
```

2. LEFT JOIN (or LEFT OUTER JOIN):
- Returns all the rows from the left table and the matched rows from the right table.
- If there is no match for a row in the right table, the result will contain NULL values for columns from the right table.
- It ensures that all rows from the left table are included in the result, even if there is no corresponding match in the right table.

Example:
```
SELECT customers.customer_id, customers.customer_name, orders.order_id
FROM customers
LEFT JOIN orders ON customers.customer_id = orders.customer_id;
```

In summary, while `INNER JOIN` only includes rows with matching values in both tables, `LEFT JOIN` includes all rows from the left table and matching rows from the right table, filling in with NULLs for columns from the right table when there's no match. The choice between them depends on the desired outcome of your query and how you want to handle unmatched rows.

# Spring Data JPA
In Spring Data JPA, you can write SQL queries in a few different ways:

## ✅ 1. Derived Queries (Method Name Queries)
Spring Data JPA can automatically generate queries based on method names.

Example:
```
List<User> findByLastName(String lastName);
List<User> findByAgeGreaterThan(int age);
```

Spring will automatically convert these to SQL like:
```
SELECT * FROM users WHERE last_name = ?;
SELECT * FROM users WHERE age > ?;
```

## ✅ 2. JPQL (Java Persistence Query Language)
You can write custom queries using JPQL (uses entity names, not table names).

Example:
```
@Query("SELECT u FROM User u WHERE u.age > :age")
List<User> findUsersOlderThan(@Param("age") int age);
```

## ✅ 3. Native SQL Queries
Use native SQL if you want to use database-specific features or write raw SQL.

Example:
```
@Query(value = "SELECT * FROM users WHERE email = :email", nativeQuery = true)
User findByEmailNative(@Param("email") String email);
```

## ✅ 4. Modifying Queries (for UPDATE or DELETE)
When updating or deleting, use @Modifying and @Transactional.

Example:
```
@Modifying
@Transactional
@Query("UPDATE User u SET u.active = false WHERE u.lastLogin < :date")
void deactivateOldUsers(@Param("date") LocalDate date);
```

## ✅ 5. Using Projections (selecting specific columns)
Instead of returning entire entities, you can return specific fields via interfaces or DTOs.

Example:
```
public interface UserNameOnly {
String getFirstName();
String getLastName();
}

@Query("SELECT u.firstName AS firstName, u.lastName AS lastName FROM User u")
List<UserNameOnly> findUserNames();
```

## ✅ Example: Repository Interface
```
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByStatus(String status);

    @Query("SELECT u FROM User u WHERE u.email = :email")
    User findByEmail(@Param("email") String email);

    @Query(value = "SELECT * FROM users WHERE age > :age", nativeQuery = true)
    List<User> findUsersOlderThanNative(@Param("age") int age);
}
```

# EntityManager
Example of using EntityManager to run SQL queries in Spring Data JPA.

## ✅ Example: Using EntityManager in Spring Boot
🔹 Step 1: Entity Class
```
@Entity
@Table(name = "users")
public class User {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

    private String firstName;
    private String lastName;
    private String email;
    private int age;

    // Getters and setters
}
```

🔹 Step 2: Create a Custom Repository Interface
```
public interface UserRepositoryCustom {
    List<User> findUsersWithCustomQuery(int minAge);
    User findUserByEmailUsingNative(String email);
}
```

🔹 Step 3: Implementation Using EntityManager
```
@Repository
public class UserRepositoryImpl implements UserRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    // JPQL Query
    @Override
    public List<User> findUsersWithCustomQuery(int minAge) {
        String jpql = "SELECT u FROM User u WHERE u.age > :minAge";
        return entityManager.createQuery(jpql, User.class)
                            .setParameter("minAge", minAge)
                            .getResultList();
    }

    // Native SQL Query
    @Override
    public User findUserByEmailUsingNative(String email) {
        String sql = "SELECT * FROM users WHERE email = :email";
        return (User) entityManager.createNativeQuery(sql, User.class)
                                   .setParameter("email", email)
                                   .getSingleResult();
    }
}
```

🔹 Step 4: Extend Custom Interface in Main Repository
```
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {
    // You can still use Spring Data methods here
}
```

## ✅ Notes:
- JPQL uses entity names and fields.
- Native queries use table names and column names.
- You can also execute UPDATE and DELETE with EntityManager inside a @Transactional method.
