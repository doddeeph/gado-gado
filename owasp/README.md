# OWASP

## 🔐 What is OWASP?
`OWASP` stands for `Open Worldwide Application Security Project`.
It is a non-profit organization focused on `improving the security of software`.

OWASP provides:
- Free tools
- Educational resources
- Community-driven standards
…all to help developers, security professionals, and organizations build secure applications.

## 🔟 The OWASP Top 10 (2021 Edition – Latest as of now)
The `OWASP Top 10` is a regularly updated list of the `most critical web application security risks`.

Here’s a breakdown of each one:

1. Broken Access Control
- Flaws that allow users to `act outside their intended permissions`.
- Example: A regular user accessing admin-only features.

2. Cryptographic Failures (formerly `“Sensitive Data Exposure”`)
- Inadequate protection of sensitive data (e.g., no encryption, poor algorithms).
- Example: Storing passwords in plain text.

3. Injection
- Attacks that inject malicious input into queries (e.g., `SQL injection`, `LDAP injection`).
- Example: `DROP TABLE users;` in a login form.

4. Insecure Design
- Flawed security architecture from the beginning (no threat modeling, poor authentication flow).
- Solution: Think “secure by design.”

5. Security Misconfiguration
- Default settings, unnecessary services, error messages revealing too much.
- Example: Open ports, directory listing, stack traces in error pages.

6. Vulnerable and Outdated Components
- Using libraries/frameworks with known vulnerabilities.
- Example: Running an old version of Spring Boot with known exploits.

7. Identification and Authentication Failures
- Weak authentication systems (e.g., no MFA, predictable passwords).
- Example: Brute-forcing weak logins.

8. Software and Data Integrity Failures
- Lack of checks on integrity of code, updates, and CI/CD processes.
- Example: Using unsigned packages or tampered artifacts.

9. Security Logging and Monitoring Failures
- No alerting/logging for suspicious activity → delayed detection of breaches.
- Example: SQL injection attack goes unnoticed.

10. Server-Side Request Forgery (SSRF)
- Attacker tricks the server into making a request to an internal resource.
- Example: Accessing internal AWS metadata via a crafted URL.

## 🧰 Tools & Resources Provided by OWASP
1. `OWASP ZAP (Zed Attack Proxy)` – Open-source tool for finding security vulnerabilities automatically.
2. `OWASP Dependency-Check` – Scans libraries for known vulnerabilities.
3. `Cheat Sheets` – Short security guidelines for developers.
4. `ASVS (Application Security Verification Standard)` – A framework to test security controls.
5. `Security Knowledge Framework (SKF)` – Helps design secure applications from the start.

## 📚 Learn More
- Website: https://owasp.org
- Tools: https://owasp.org/www-projects
- Top 10 Detailed: [OWASP Top 10 2021](https://owasp.org/Top10/)

# Implementing OWASP in Spring Boot 
Implementing **OWASP** (Open Web Application Security Project) guidelines in a Spring Boot application means protecting your application against the most critical web security risks as identified by the [OWASP Top 10](https://owasp.org/www-project-top-ten/). 
Below is a comprehensive guide on how to implement OWASP best practices in a Spring Boot application.

## ✅ OWASP Top 10 Implementation in Spring Boot
1. 🔐 Broken Access Control
Problem: Unauthorized users accessing restricted resources.
Solution:
- Use Spring Security to enforce role-based access control.
- Annotate controller methods:
```
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin/dashboard")
public String adminDashboard() {
    return "Admin Content";
}
```

2. 🔑 Cryptographic Failures
Problem: Sensitive data not encrypted properly.
Solution:
- Store passwords using `BCrypt`:
```
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```
- Use `HTTPS` for all communications (Spring Security can enforce this).
```
http.requiresChannel().anyRequest().requiresSecure();
```
- Encrypt sensitive config using tools like `Vault, Jasypt`.

3. 🔍 Injection (SQL, Command, etc.)
Problem: Attacker injects malicious input.
Solution:
- Use Spring Data JPA or parameterized queries:
```
@Query("SELECT u FROM User u WHERE u.email = :email")
User findByEmail(@Param("email") String email);
```
- Avoid string concatenation in queries.

4. 🔒 Insecure Design
Problem: Lack of secure architecture decisions.
Solution:
- Conduct `threat modeling` early.
- Use `security by design`: validation, authorization checks at every layer.
- Use OWASP `ASVS (Application Security Verification Standard)`.

5. 🧪 Security Misconfiguration
Problem: Default credentials, exposed ports, etc.
Solution:
- Disable actuator endpoints in production:
```
management.endpoints.web.exposure.include=health,info
```
- Use secure HTTP headers:
```
http.headers()
    .contentSecurityPolicy("script-src 'self'")
    .xssProtection().and()
    .frameOptions().deny();
```

6. 🔍 Vulnerable and Outdated Components
Problem: Using insecure libraries.
Solution:
- Use `OWASP Dependency-Check` or `Snyk` plugin:
```
mvn org.owasp:dependency-check-maven:check
```
- Automate scans in CI/CD (GitHub Actions, Jenkins, etc.)

7. 🧬 Identification and Authentication Failures
Problem: Weak or broken authentication mechanisms.
Solution:
- Use `Spring Security` login mechanisms.
- Enforce `multi-factor authentication` if possible.
- Secure JWT tokens with proper signing & expiry:
```
JWT.create()
    .withSubject("user")
    .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION))
    .sign(Algorithm.HMAC512(SECRET));
```

8. 🧱 Software and Data Integrity Failures
Problem: Untrusted data/code is executed.
Solution:
- Use `code signing`.
- Validate any YAML, JSON, or file uploads.
- Use `Spring Boot DevTools` only in development.

9. 📜 Security Logging and Monitoring Failures
Problem: Lack of auditing/logs for attacks.
Solution:
- Use `Spring Boot Actuator` for metrics and health checks.
- Log security-relevant events:
```
log.warn("Failed login attempt for user: {}", username);
```
- Integrate with tools like `ELK`, `Grafana`, or `SIEM`.

10. 📂 Server-Side Request Forgery (SSRF)
Problem: Application makes insecure internal calls.
Solution:
- Whitelist URLs or domains in RestTemplate or WebClient.
- Validate and sanitize URLs.
```
URI uri = new URI(url);
if (!allowedDomains.contains(uri.getHost())) {
    throw new SecurityException("Invalid URL");
}
```

## 🛡️ Bonus: OWASP-Proven Tools for Spring Boot
| Tool                         | Usage                                         |
| ---------------------------- | --------------------------------------------- |
| **Spring Security**          | Core library for authentication/authorization |
| **OWASP Dependency-Check**   | Scans for vulnerable dependencies             |
| **CSRF Protection**          | Enabled by default in Spring Security         |
| **Content Security Policy**  | Configured via Spring Security headers        |
| **XSS/Clickjacking Defense** | Built into Spring Security headers            |
| **Jasypt**                   | Encrypt application properties                |


## 🧪 Sample Integration of OWASP Dependency-Check in Maven
```
<plugin>
  <groupId>org.owasp</groupId>
  <artifactId>dependency-check-maven</artifactId>
  <version>8.4.0</version>
  <executions>
    <execution>
      <goals>
        <goal>check</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

## Spring Boot Example Follows OWASP Top 10
```
package com.example.securitydemo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;

@SpringBootApplication
@Validated
public class SecurityDemoApplication {
public static void main(String[] args) {
SpringApplication.run(SecurityDemoApplication.class, args);
}

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf().and()
            .headers(headers -> headers
                .xssProtection().and()
                .frameOptions().deny()
                .contentSecurityPolicy("script-src 'self'")
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin().and()
            .httpBasic();
        return http.build();
    }
}

@RestController
@RequestMapping("/api")
class DemoController {

    @GetMapping("/public")
    public String publicEndpoint() {
        return "This is a public endpoint.";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "This is a secure admin dashboard.";
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody UserRegistrationDto userDto) {
        // Simulate registration logic
        return ResponseEntity.ok("User registered: " + userDto.getEmail());
    }
}

class UserRegistrationDto {
@NotBlank(message = "Email must not be blank")
@Email(message = "Email should be valid")
private String email;

    @NotBlank(message = "Password must not be blank")
    private String password;

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

// application.properties
// server.ssl.enabled=true
// server.ssl.key-store=classpath:keystore.p12
// server.ssl.key-store-password=changeit
// server.ssl.key-store-type=PKCS12
// server.ssl.key-alias=tomcat

// Add Jasypt and OWASP Dependency-Check in pom.xml
// <dependency>
//   <groupId>com.github.ulisesbocchio</groupId>
//   <artifactId>jasypt-spring-boot-starter</artifactId>
//   <version>3.0.4</version>
// </dependency>

// <plugin>
//   <groupId>org.owasp</groupId>
//   <artifactId>dependency-check-maven</artifactId>
//   <version>8.4.0</version>
//   <executions>
//     <execution>
//       <goals>
//         <goal>check</goal>
//       </goals>
//     </execution>
//   </executions>
// </plugin>
```

Here's a complete Spring Boot example implementing key OWASP recommendations:
- `CSRF protection` is enabled.
- `XSS protection` and `Content Security Policy (CSP)` headers are configured.
- `Role-based access control` using Spring Security.
- `Password encoding` with `BCrypt`.
- `TLS/SSL configuration` sample in `application.properties`.
- `Dependency vulnerability scanning` via OWASP Dependency-Check plugin in `pom.xml`.
- `Jasypt` for encrypted configuration values.

# Jasypt
Jasypt (`Java Simplified Encryption`) is a library that helps you `secure sensitive information` like passwords, secrets, API keys, or tokens in your application—especially within configuration files such as `application.properties` or `

## 🔐 Why Use Jasypt?
- To `encrypt sensitive values` in configuration files
- To `avoid storing secrets in plain text`
- To `decrypt them at runtime` without changing existing Spring Boot logic

## ✅ Key Features of Jasypt
- Easy integration with `Spring Boot`
- Supports `strong encryption algorithms` (e.g., AES, PBEWithMD5AndDES, etc.)
- Transparent decryption of values at runtime
- Secure handling of encrypted properties

## 🔧 How to Use Jasypt in Spring Boot
1. Add Jasypt Dependency
In pom.xml:
```
<dependency>
    <groupId>com.github.ulisesbocchio</groupId>
    <artifactId>jasypt-spring-boot-starter</artifactId>
    <version>3.0.4</version>
</dependency>
```

2. Encrypt a Property
For example, encrypt a database password:
```
# Example using Jasypt CLI or online tool:
ENC(nKk+X9yzx4vXQWkREtixUw==)
```
Use the encrypted value in application.properties:
```
spring.datasource.password=ENC(nKk+X9yzx4vXQWkREtixUw==)
```

3. Provide Encryption Password (Secret Key)
You must pass the decryption password to the application. Common options:
- As an environment variable:
```
export JASYPT_ENCRYPTOR_PASSWORD=mySecretKey
```
- Or in the command line:
```
java -DJASYPT_ENCRYPTOR_PASSWORD=mySecretKey -jar app.jar
```

4. (Optional) Customize Algorithm
In application.properties:
```
jasypt.encryptor.algorithm=PBEWithMD5AndDES
jasypt.encryptor.password=${JASYPT_ENCRYPTOR_PASSWORD}
```

## 📌 Best Practices
- Store `JASYPT_ENCRYPTOR_PASSWORD` securely (not in version control).
- Prefer `AES-based algorithms` (e.g., `PBEWITHHMACSHA512ANDAES_256`) for stronger security.
- Combine with tools like `Vault`, `AWS Secrets Manager`, or `Azure Key Vault` in production.

## 🧪 Example in Action
If you write in your config:
```
email.service.token=ENC(Vv5lBvLoPdkA59D2v9ZPXA==)
```
And you provide `JASYPT_ENCRYPTOR_PASSWORD=secret123`, your app will automatically decrypt it at runtime.