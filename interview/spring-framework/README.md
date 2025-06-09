# Mock Interview

## Part 1: Understanding the Concepts

1. Explain how dependency injection works in Spring.
   (Hint: IoC container, `@Autowired`, constructor vs setter)

`Dependency Injection (DI)` is a design pattern where an object receives its dependencies (usually other objects) from the outside world, rather than creating them itself. 
In Spring, this is managed by the **IoC (Inversion of Control) Container**, which is responsible for creating, managing, and injecting dependencies.

Spring provides 3 main ways to do dependency injection:
- **Constructor Injection** (recommended because it is immutable and testable)
- **Setter Injection**
- **Field Injection** (not recommended for testability)

Spring uses annotations such as:
- `@Component`, `@Service`, `@Repository`, `@Controller` → to declare beans
- `@Autowired`, `@Inject`, or constructor injection → to inject dependencies

Example:
```
@Service
public class UserService {
private final UserRepository userRepository;

    public UserService(UserRepository userRepository) { // Constructor injection
        this.userRepository = userRepository;
    }
}
```

Spring's IoC Container will scan the annotated classes, instantiate them, and inject beans according to the defined dependencies.

2. What is the difference between `@RestController` and `@Controller`?
   (Hint: `@RestController` =` @Controller` + `@ResponseBody`)

- `@Controller` is used in classes that handle web requests and usually returns the name of a view (e.g., JSP, Thymeleaf) that will be rendered to the user.
- `@RestController` is a shortcut for `@Controller` + `@ResponseBody`, which means that each method will directly return `data` (usually `JSON`) to the client, not the view.

Example of using `@Controller`:
```
@Controller
public class HomeController {
    @GetMapping("/")
    public String homePage() {
        return "index"; // nama view (ex: index.html)
    }
}
```

Example of using `@RestController`:
```
@RestController
@RequestMapping("/api")
public class UserController {
    @GetMapping("/users")
    public List<User> getUsers() {
        return userService.getAllUsers(); // otomatis dikonversi ke JSON
    }
}
```

Conclusion:
Use `@RestController` to create `REST API`, while `@Controller` is used when we want to `display a page/view` to the user.

3. How to set different configurations for dev, staging, and production?
   (Hint: application-`{profile}`.yml + `@Profile`)

Spring Boot supports environment profile-based configuration through configuration files such as:
- application-dev.yml
- application-staging.yml
- application-prod.yml

Then, enable the profile in one of the following ways:
1. application.yml (default file):
```
spring:
    profiles:
        active: dev
```

2. Command-line at run:
```
java -jar app.jar --spring.profiles.active=prod
```

Using `@Profile`:
If you want to make a bean only available in a specific profile:
```
@Service
@Profile("dev")
public class DevEmailService implements EmailService { ... }

@Service
@Profile("prod")
public class ProdEmailService implements EmailService { ... }
```

Conclusion:
With this mechanism, we can separate database configuration, credentials, logging, and other behaviors according to the environment.

4. What is `@Transactional` and how does it work in Spring?
   (Hint: proxy-based, rollback, isolation, propagation)

`@Transactional` is an annotation in Spring that is used to manage database transactions declaratively. When a method is given this annotation, Spring will automatically:
- Open a transaction before the method is executed,
- Commit if the method is successful,
- Rollback if an exception occurs.

How it works technically:
Spring uses a `proxy (AOP)` to wrap methods that are given `@Transactional`. Transactions are only active if the method is called from outside (not self-invocation).

Usage example:
```
@Service
public class OrderService {

    @Transactional
    public void placeOrder(Order order) {
        orderRepository.save(order);
        paymentService.charge(order);
    }
}
```

If an exception occurs in `charge(order)`, then all transactions including `save(order)` will be rolled back.

Other important features:
- `propagation` (how new transactions are created or continued)
- `isolation` (transaction isolation level)
- `rollbackFor` (specific exceptions that trigger rollback)

Conclusion:
`@Transactional` helps maintain `data consistency` without having to write manual transaction logic.

5. How do you handle global exceptions in a REST application?
   (Hint: @ControllerAdvice, @ExceptionHandler, custom response)

To handle errors globally and return consistent JSON responses, Spring provides `@ControllerAdvice` which acts as a global exception handler.

Steps:
- Create a class with `@ControllerAdvice` annotation
- Add a method annotated with @`ExceptionHandler`
- Return `ResponseEntity` or custom response class

Example:
```
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse("NOT_FOUND", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        ErrorResponse error = new ErrorResponse("INTERNAL_ERROR", "Something went wrong");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

ErrorResponse class:
```
public class ErrorResponse {
    private String code;
    private String message;
    // constructor, getters, setters
}
```

Benefits:
- Error responses become standardized
- No need to handle errors in every controller
- Easy to map to appropriate HTTP status

Conclusion:
Use `@ControllerAdvice` and `@ExceptionHandler` to handle errors globally and provide a clean and structured API experience.

## Part 2: Coding (Quick Exercise)

Question 1 – Create a simple REST API (CRUD) for the Product entity
Description:
Create an API using Spring Boot with features:
- Get all products: `GET /products`
- Get product by ID: `GET /products/{id}`
- Create new product: `POST /products`
- Update product: `PUT /products/{id`}
- Delete product: `DELETE /products/{id}`

Entity:
```
public class Product {
    private Long id;
    private String name;
    private Double price;
}
```

Challenge:
- Use Spring annotations such as @RestController, @Service, @Repository, and @Autowired
- Simulate data with an in-memory list (without DB first)
- Add error handling if the product is not found


## Part 3: Architecture Case Study
Case Study:
An application has many users and files, and you are asked to build a system to:
- Upload files
- Validate files (size, format)
- Store metadata
- Scalable

Questions:
- How is the service layer structured?
- Where do you put file validation?
- How do you store large files?
- What is the scalability strategy?
- What do you log and monitor in production?