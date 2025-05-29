# Design SOLID Principles with Strategy Pattern

## 1. Interface: PaymentStrategy
- S: **Single Responsibility Principle** – This interface has only one responsibility: define a common contract for payment strategies.
- O: **Open/Closed Principle** – New payment methods can be added without modifying existing code.
```
public interface PaymentStrategy {
    void pay(double amount);
}
```

## 2. Concrete Strategy: CreditCardPayment
- Implements the PaymentStrategy interface.
- Encapsulates Credit Card-specific logic.
```
public class CreditCardPayment implements PaymentStrategy {
private String cardNumber;
private String cardHolder;
private String cvv;

    public CreditCardPayment(String cardNumber, String cardHolder, String cvv) {
        this.cardNumber = cardNumber;
        this.cardHolder = cardHolder;
        this.cvv = cvv;
    }

    @Override
    public void pay(double amount) {
        System.out.println("Paid $" + amount + " using Credit Card.");
    }
}
```

## 3. Concrete Strategy: GopayPayment
```
public class GopayPayment implements PaymentStrategy {
    private String gopayId;

    public GopayPayment(String gopayId) {
        this.gopayId = gopayId;
    }

    @Override
    public void pay(double amount) {
        System.out.println("Paid $" + amount + " using GoPay.");
    }
}
```

## 4. Concrete Strategy: QrisPayment
```
public class QrisPayment implements PaymentStrategy {
    private String qrisCode;

    public QrisPayment(String qrisCode) {
        this.qrisCode = qrisCode;
    }

    @Override
    public void pay(double amount) {
        System.out.println("Paid $" + amount + " using QRIS.");
    }
}
```

## 5. Context: PaymentProcessor
- **D: Dependency Inversion Principle** – The context depends on the abstraction PaymentStrategy, not concrete classes.
- **L: Liskov Substitution Principle** – Any subclass of PaymentStrategy can replace another without affecting functionality.
```
public class PaymentProcessor {
    private PaymentStrategy paymentStrategy;

    public PaymentProcessor(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }

    public void processPayment(double amount) {
        paymentStrategy.pay(amount);
    }

    public void setPaymentStrategy(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }
}
```

## Usage Example
```
public class Main {
    public static void main(String[] args) {
        PaymentProcessor processor = new PaymentProcessor(new CreditCardPayment("123456789", "John Doe", "123"));
        processor.processPayment(100.0);

        processor.setPaymentStrategy(new GopayPayment("gopay-123"));
        processor.processPayment(50.0);

        processor.setPaymentStrategy(new QrisPayment("qris-code-xyz"));
        processor.processPayment(75.0);
    }
}
```

This design:
- Uses Strategy Pattern effectively.
- Adheres to all SOLID principles.
- Is extensible and maintainable.

# Uses Strategy Pattern effectively
The Strategy Pattern is a behavioral design pattern that enables selecting an algorithm's behavior at runtime. It is used to define a family of algorithms (or strategies), encapsulate each one, and make them interchangeable. The key goal is to decouple the algorithm from the context where it is used.

## ✅ How Our Example Uses the Strategy Pattern Effectively
Let's break it down with the classes you asked about:

### 📌 1. Define a common interface:
```
public interface PaymentStrategy {
    void pay(double amount);
}
```
- This defines a family of interchangeable behaviors (pay) without specifying how each one works.
- It sets the contract that all concrete strategies must follow.

### 📌 2. Implement multiple strategies:
Each of the following classes implements the PaymentStrategy interface:
```
public class CreditCardPayment implements PaymentStrategy { ... }

public class GopayPayment implements PaymentStrategy { ... }

public class QrisPayment implements PaymentStrategy { ... }
```
- These classes encapsulate specific payment logic.
- They are independent and reusable.
- You can add new payment methods without changing the existing code (**Open/Closed Principle**).

### 📌 3. Use strategy dynamically in context:
```
public class PaymentProcessor {
    private PaymentStrategy paymentStrategy;

    public PaymentProcessor(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }

    public void processPayment(double amount) {
        paymentStrategy.pay(amount);
    }

    public void setPaymentStrategy(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }
}
```
- The PaymentProcessor (context) delegates payment logic to the strategy object.
- You can change strategies at runtime using setPaymentStrategy().

### 📌 4. Runtime flexibility:
```
processor.setPaymentStrategy(new GopayPayment("gopay-id"));
processor.processPayment(50.0);
```
- This makes the system flexible, as different payment methods can be chosen on the fly.
- There's no need for conditional logic (like if-else or switch) inside the PaymentProcessor class.

## 📦 Benefits of Using the Strategy Pattern Here:
| Benefit                    | Description                                                                                    |
| -------------------------- | ---------------------------------------------------------------------------------------------- |
| ✅ **Decoupling**           | `PaymentProcessor` doesn't care how payment is processed, only that the `pay()` method exists. |
| ✅ **Open for extension**   | You can add PayPal, ApplePay, etc., without modifying the processor.                           |
| ✅ **Avoids conditionals**  | No need for if-else or switch statements based on payment type.                                |
| ✅ **Improves testability** | Each payment method can be tested independently.                                               |

## 🧠 Summary
Your implementation uses the Strategy Pattern effectively because it:
- Defines a common interface (PaymentStrategy)
- Encapsulates different behaviors (CreditCardPayment, GopayPayment, etc.)
- Delegates behavior in a loosely coupled way (PaymentProcessor)
- Allows runtime changes and scaling without modifying existing code

# The Strategy Pattern approach with a non-strategy (bad practice) implementation

## 🔴 Bad Practice (Without Strategy Pattern)
In this example, all payment logic is stuffed into one class, using if-else or switch statements:
```
public class PaymentProcessor {
    public void processPayment(String paymentType, double amount) {
        if ("creditcard".equalsIgnoreCase(paymentType)) {
            System.out.println("Paid $" + amount + " using Credit Card.");
        } else if ("gopay".equalsIgnoreCase(paymentType)) {
            System.out.println("Paid $" + amount + " using GoPay.");
        } else if ("qris".equalsIgnoreCase(paymentType)) {
            System.out.println("Paid $" + amount + " using QRIS.");
        } else {
            System.out.println("Unsupported payment type.");
        }
    }
}
```

## ⚠️ Problems with This Design
| Problem                                              | Explanation                                                            |
| ---------------------------------------------------- | ---------------------------------------------------------------------- |
| ❌ **Violates Single Responsibility Principle (SRP)** | `PaymentProcessor` does too much—it knows about all types of payments. |
| ❌ **Violates Open/Closed Principle (OCP)**           | Every time a new payment method is added, this class must be modified. |
| ❌ **Not scalable**                                   | Adding more options creates longer and more complex if-else logic.     |
| ❌ **Hard to test**                                   | You can’t test individual payment behaviors separately.                |
| ❌ **Tight coupling**                                 | All logic is tightly coupled inside one class.                         |

## ✅ Good Practice (With Strategy Pattern)
Here’s how we improve it using the Strategy Pattern:
```
PaymentProcessor processor = new PaymentProcessor(new CreditCardPayment("123", "John", "999"));
processor.processPayment(100.0);

processor.setPaymentStrategy(new GopayPayment("gopay-123"));
processor.processPayment(50.0);
```

## ✅ Benefits of This Approach
| Benefit                        | Explanation                                                            |
| ------------------------------ | ---------------------------------------------------------------------- |
| ✅ **Clean and modular**        | Each payment method is its own class.                                  |
| ✅ **Follows SOLID principles** | Especially SRP, OCP, and DIP.                                          |
| ✅ **Easily extendable**        | Add new strategies without changing the processor.                     |
| ✅ **Flexible and testable**    | You can test each strategy independently.                              |
| ✅ **Decoupled architecture**   | `PaymentProcessor` doesn't depend on specific payment implementations. |

## 🔚 Summary
| Feature             | Without Strategy                | With Strategy        |
| ------------------- | ------------------------------- | -------------------- |
| Code readability    | Poor                            | Clear and modular    |
| Scalability         | Hard                            | Easy                 |
| Testing             | Hard                            | Easy                 |
| Maintenance         | Error-prone                     | Safe and clean       |
| New payment methods | Requires changing existing code | Just add a new class |


# The Interface Segregation Principle (ISP)

## 🔍 What is Interface Segregation Principle (ISP)?
```
"Clients should not be forced to depend on methods they do not use."
```
In other words, interfaces should be small, focused, and specific to what a class actually needs.

## 🧾 Your Current PaymentStrategy Interface
```
public interface PaymentStrategy {
    void pay(double amount);
}
```
This interface has only one method and represents a cohesive contract: a payment operation.

## ✅ ISP is already satisfied here because:
- The interface is minimal and focused (pay() method only).
- Each implementation (CreditCardPayment, GopayPayment, QrisPayment) only implements what they actually need.

## 📌 When ISP Would Be Violated
Imagine you expanded the interface like this:
```
public interface PaymentStrategy {
    void pay(double amount);
    void validateCard();
    void scanQRCode();
    void authenticateGopayUser();
}
```
Now, not all payment types use all methods:
- CreditCardPayment uses validateCard().
- GopayPayment uses authenticateGopayUser().
- QrisPayment uses scanQRCode().

### ⚠️ This violates ISP:
- Classes are forced to implement irrelevant methods.
- Leads to empty implementations or dummy methods, which is bad design.

## ✅ Applying ISP Properly (if needed)
You can split interfaces based on type of payment:
```
public interface PaymentStrategy {
void pay(double amount);
}

public interface CardPaymentStrategy extends PaymentStrategy {
void validateCard();
}

public interface QRPaymentStrategy extends PaymentStrategy {
void scanQRCode();
}

public interface GopayPaymentStrategy extends PaymentStrategy {
void authenticateGopayUser();
}
```
Then:
```
public class CreditCardPayment implements CardPaymentStrategy {
    public void pay(double amount) { ... }
    public void validateCard() { ... }
}
```
```
public class GopayPayment implements GopayPaymentStrategy {
    public void pay(double amount) { ... }
    public void authenticateGopayUser() { ... }
}
```
```
public class QrisPayment implements QRPaymentStrategy {
    public void pay(double amount) { ... }
    public void scanQRCode() { ... }
}
```

### 💡 But in your current case:
Your interface is already perfectly lean and doesn't require this split—unless your payment strategies start needing very different sets of behaviors.

## 🧠 Summary
| Concept        | In Your Design                                            |
| -------------- | --------------------------------------------------------- |
| ISP            | ✅ Already satisfied (interface is minimal)                |
| When to split? | Only if multiple unrelated methods exist in one interface |
| Danger sign    | Empty or irrelevant method implementations in classes     |


# How to refactor your payment system toward the Interface Segregation Principle (ISP) when more behaviors are introduced—such as:
- refund()
- getTransactionHistory()
- validateCard()
- scanQRCode()
- authenticateUser()

## 🔴 Problem with a Fat Interface (ISP Violation)
Suppose we expand the PaymentStrategy like this:
```
public interface PaymentStrategy {
    void pay(double amount);
    void refund(double amount);
    void validateCard();               // Only for credit card
    void scanQRCode();                 // Only for QRIS
    void authenticateUser();           // Only for GoPay
    void getTransactionHistory();      // Maybe only some implement this
}
```

This leads to:
- Unused methods in most classes
- Empty/dummy method implementations
- Tight coupling and poor modularity

### 🔻 Example of ISP violation:
```
public class QrisPayment implements PaymentStrategy {
    public void pay(double amount) { /* ... */ }
    public void refund(double amount) { /* ... */ }
    public void validateCard() { /* unused */ }
    public void scanQRCode() { /* QR specific */ }
    public void authenticateUser() { /* unused */ }
    public void getTransactionHistory() { /* ... */ }
}
```

## ✅ Refactored Design (Following ISP)
### 1. Break interfaces into specific ones
```
public interface PaymentStrategy {
   void pay(double amount);
}
```
```
public interface Refundable {
    void refund(double amount);
}
```
```
public interface CardValidatable {
    void validateCard();
}
```
```
public interface QRScannable {
    void scanQRCode();
}
```
```
public interface UserAuthenticatable {
    void authenticateUser();
}
```
```
public interface TransactionHistory {
    void getTransactionHistory();
}
```

### 2. Each class implements only what it needs
#### ✅ CreditCardPayment:
```
public class CreditCardPayment implements PaymentStrategy, Refundable, CardValidatable, TransactionHistory {
   public void pay(double amount) { /*...*/ }
   public void refund(double amount) { /*...*/ }
   public void validateCard() { /*...*/ }
   public void getTransactionHistory() { /*...*/ }
} 
```

#### ✅ GopayPayment:
```
public class GopayPayment implements PaymentStrategy, Refundable, UserAuthenticatable {
   public void pay(double amount) { /*...*/ }
   public void refund(double amount) { /*...*/ }
   public void authenticateUser() { /*...*/ }
}
```

#### ✅ QrisPayment:
```
public class QrisPayment implements PaymentStrategy, QRScannable {
   public void pay(double amount) { /*...*/ }
   public void scanQRCode() { /*...*/ }
}
```

## 🧠 Benefits of This Refactor
| Benefit                | Explanation                                      |
| ---------------------- | ------------------------------------------------ |
| ✅ ISP compliance       | Classes only implement what they actually use.   |
| ✅ Better modularity    | Interfaces are small, focused, and reusable.     |
| ✅ Extensibility        | Easy to add more behaviors with minimal impact.  |
| ✅ Improved testability | You can test only relevant behavior per class.   |
| ✅ Cleaner code         | Avoids empty method stubs and conditional logic. |

## 🔚 Summary
- ✅ Use multiple small interfaces based on behavior (pay, refund, scan, validate, etc.)
- ✅ Each class implements only what it truly supports
- ✅ Helps you follow both ISP and SRP (Single Responsibility Principle)
- ✅ Leads to a cleaner, more maintainable payment system
