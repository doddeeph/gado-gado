# CSV Product Import System
Let’s design a system to **import products via CSV files** - a common requirement for e-commerce or inventory management platforms.

## 1. Requirements
Functional:
* Users upload CSV files containing product data (name, price, SKU, description, etc.).
* System parses CSV, validates data, and stores products in the database.
* Handle large CSV files (thousands or millions of rows).
* Provide feedback on import status, errors, and success summary.
* Support reprocessing or correction of failed records.

Non-functional:
* Scalable to handle concurrent uploads.
* Reliable and fault-tolerant — partial failures should not break entire import.
* Secure — only authorized users can upload.
* Provide audit trail and logs for imports.

## 2. High-Level Architecture
```
+-----------------+        +------------------+       +--------------------+
|                 |        |                  |       |                    |
|  Web UI / API   +------->+  File Upload     +------>+  Validation &      |
|  (Users Upload) |        |  Service         |       |  Parsing Service   |
|                 |        |                  |       |                    |
+-----------------+        +---------+--------+       +---------+----------+
                                    |                          |
                                    v                          v
                          +------------------+         +--------------------+
                          | Storage (S3 /    |         | Message Queue      |
                          | Blob Storage)    |         | (Kafka / RabbitMQ) |
                          +------------------+         +---------+----------+
                                                                 |
                                                                 v
                                                   +--------------------------+
                                                   | Import Workers           |
                                                   | (Validation + DB Insert) |
                                                   +----------+---------------+
                                                              |
                                                              v
                                                     +--------------------+
                                                     | Database           |
                                                     | (Product Catalog)  |
                                                     +--------------------+

+-----------------------+            +-----------------+
| Monitoring & Alerts   |            | Notification    |
| (Prometheus, Grafana) |            | Service (Email) |
+-----------------------+            +-----------------+
```

## 3. Component Details
### A. Web UI / API
* User uploads CSV files via web interface or API.
* Upload triggers storage and processing pipeline.
* Shows upload progress and import status.

### B. File Upload Service
* Receives the CSV file.
* Performs basic file validation (size, format).
* Stores CSV file temporarily in durable storage like AWS S3 or Azure Blob Storage.
* Publishes a message to a message queue with file location and metadata.

### C. Validation & Parsing Service
* Reads the CSV from storage.
* Parses CSV rows in batches to handle large files without memory overflow.
* Validates data types, mandatory fields, SKU uniqueness, price formats, etc.
* Forwards valid rows to the message queue for insertion.
* Invalid rows logged for error reporting.

### D. Message Queue
* Decouples parsing and database insertion for better scalability.
* Ensures reliable delivery and ordering if needed.

### E. Import Workers
* Consume messages in batches from the queue.
* Insert or update product records in the product catalog database (relational DB).
* Handle transactional integrity — partial failure rollback or continue with error logging.
* Emit status updates to monitoring system.

### F. Database
* Stores product data, indexed by SKU for quick lookups.
* Supports transactions for data consistency.

### G. Monitoring & Alerts
* Track import job progress, errors, throughput.
* Trigger alerts if error rate exceeds thresholds or system is slow.

### H. Notification Service
* Sends summary emails or messages to users on import completion or failure, including error reports.

## 4. Scalability & Reliability
* Batch Processing: Process CSV in manageable chunks to avoid memory spikes.
* Horizontal Scaling: Run multiple import workers in parallel.
* Idempotency: Ensure product inserts/updates are idempotent to handle retries gracefully.
* Retry & Dead-letter Queue: Failed messages are retried; persistent failures are logged for manual intervention.
* Security: Authentication & authorization at API and storage level; encrypted storage for CSV files.

## 5. Example Workflow
* User uploads `products.csv` through UI.
* File Upload Service validates and stores the file in S3.
* A message with file metadata is sent to Kafka.
* Validation & Parsing Service reads CSV in batches, validates rows, and sends valid rows as messages to Kafka.
* Import Workers consume messages and insert/update products in the database.
* On completion, Notification Service emails the user with results.
* Monitoring dashboards show real-time import status and errors.

## Spring Boot solution
Here's a Spring Boot solution for CSV parsing in batches, with validation, error handling, and asynchronous processing. We'll use Spring Batch for batch processing and Spring Validation for input validation.

### Key Components:
1. CSV Parsing: Use a `FlatFileItemReader` for reading CSV files.
2. Validation: Use `@Valid` and custom validators to ensure the data is correct.
3. Error Handling: Use error handling during batch processing to track invalid records.
4. Async Processing: Use Spring’s `@Async` for parallel processing.

### 1. Add Dependencies
First, ensure you have the necessary dependencies in your `pom.xml`:
```
<dependencies>
    <!-- Spring Boot Starter Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring Batch for Batch Processing -->
    <dependency>
        <groupId>org.springframework.batch</groupId>
        <artifactId>spring-batch-core</artifactId>
    </dependency>

    <!-- Spring Data JPA for Database interaction -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- H2 Database for demo, replace with your DB -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Spring Boot Starter Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Spring Boot Starter Async -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-async</artifactId>
    </dependency>
</dependencies>
```

### 2. CSV Parsing Configuration
We use **Spring Batch** for batch processing and **FlatFileItemReader** to read the CSV files.

`CsvItemProcessor.java`
This will validate and process CSV items.
```java
import org.springframework.batch.item.ItemProcessor;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Validator;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.util.Set;

public class CsvItemProcessor implements ItemProcessor<Product, Product> {

    private final Validator validator;

    public CsvItemProcessor() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Override
    public Product process(Product product) throws Exception {
        // Validate the product object
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        if (!violations.isEmpty()) {
            // Handle validation errors
            for (ConstraintViolation<Product> violation : violations) {
                System.out.println("Validation error: " + violation.getMessage());
            }
            return null; // Ignore invalid items
        }

        // Add any custom processing logic here
        return product;
    }
}
```

### 3. Product Model with Validation
`Product.java`
Here we use JSR-303 annotations for validation.
```java
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class Product {

    @NotNull(message = "Product ID cannot be null")
    private Long id;

    @NotEmpty(message = "Product name cannot be empty")
    private String name;

    @NotNull(message = "Price cannot be null")
    private Double price;

    // Getters and setters omitted for brevity
}
```

### 4. FlatFileItemReader for CSV Parsing
`CsvFileReaderConfig.java`
Here, we configure the FlatFileItemReader to read the CSV file in batches.
```java
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;

@Configuration
@EnableBatchProcessing
public class CsvFileReaderConfig {

    @Bean
    public FlatFileItemReader<Product> reader() {
        FlatFileItemReader<Product> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("products.csv")); // path to CSV
        reader.setLineMapper(new DefaultLineMapper<Product>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames("id", "name", "price"); // Map columns to Product fields
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<Product>() {{
                setTargetType(Product.class);
            }});
        }});
        return reader;
    }

    @Bean
    public ItemProcessor<Product, Product> processor() {
        return new CsvItemProcessor();
    }

    @Bean
    public ItemWriter<Product> writer() {
        // Custom writer logic to store processed data to DB or handle errors
        return items -> {
            for (Product item : items) {
                System.out.println("Processed Product: " + item);
            }
        };
    }

    @Bean
    public Step step1(StepBuilderFactory stepBuilderFactory, ItemReader<Product> reader,
                      ItemProcessor<Product, Product> processor, ItemWriter<Product> writer) {
        return stepBuilderFactory.get("step1")
                .<Product, Product>chunk(10)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job importProductJob(JobBuilderFactory jobBuilderFactory, Step step1) {
        return jobBuilderFactory.get("importProductJob")
                .start(step1)
                .build();
    }
}
```

### 5. Error Handling & Async Processing
In case of errors, we need to handle invalid records and retry the process if needed.
Error Handling in `ItemProcessor` and Async Retry Logic
```java
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AsyncRetryService {

    @Async
    public void retryFailedRecords(Product product) {
        // Logic for retrying the failed records asynchronously
        System.out.println("Retrying failed record: " + product.getId());
        // Example retry logic, could also include exponential backoff
    }
}
```

### 6. Application Configuration for Async Processing
Enable asynchronous processing in your Spring Boot application by adding the following configuration.
`Application.java`
```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 7. CSV File Example (products.csv)
```csv
id,name,price
1,Product A,100.0
2,Product B,150.0
3,Product C,200.0
```

### 📌 Summary of Flow:
1. **CSV Parsing**: We use `FlatFileItemReader` to read each row from the CSV and map it to a `Product` object.
2. **Validation**: In the `CsvItemProcessor`, we use **JSR-303 annotations** (like `@NotEmpty`, `@NotNull`) to validate the input and handle invalid records.
3. **Error Handling**: If validation fails, we skip the record and log the error. You can expand this with more sophisticated error-handling strategies (e.g., move invalid rows to a separate file or retry).
4. **Async Processing**: We use `@Async` to retry failed records asynchronously.

##  Sample Java Code Snippets
1. Parsing CSV in batches using a popular library (`OpenCSV`).
```java
import com.opencsv.CSVReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CsvBatchParser {

    private static final int BATCH_SIZE = 100;

    public interface BatchProcessor<T> {
        void processBatch(List<T> batch);
    }

    public static void parseCsvInBatches(String csvFilePath, BatchProcessor<String[]> batchProcessor) throws Exception {
        try (CSVReader reader = new CSVReader(new FileReader(csvFilePath))) {
            List<String[]> batch = new ArrayList<>();
            String[] nextLine;

            // Skip header if needed
            reader.readNext();

            while ((nextLine = reader.readNext()) != null) {
                batch.add(nextLine);
                if (batch.size() == BATCH_SIZE) {
                    batchProcessor.processBatch(batch);
                    batch.clear();
                }
            }

            // Process remaining
            if (!batch.isEmpty()) {
                batchProcessor.processBatch(batch);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        parseCsvInBatches("products.csv", batch -> {
            // Here you can call DB insert function with the batch
            System.out.println("Processing batch of size: " + batch.size());
            // Insert batch to DB or send to message queue
        });
    }
}
```

2. Batch database insertion using JDBC or Spring Data JPA.
Assuming you have a Product entity:
```java
@Entity
public class Product {
    @Id
    private String sku;
    private String name;
    private Double price;
    private String description;

    // getters/setters
}
```

Repository interface:
```java
public interface ProductRepository extends JpaRepository<Product, String> {
}
```

Batch insert service:
```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductBatchService {

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public void saveBatch(List<Product> products) {
        productRepository.saveAll(products);
        // saveAll is optimized to batch insert/update depending on JPA provider
    }
}
```

3. Integrating Both: Parsing + Batch Insert
```java
public class CsvImportService {

    @Autowired
    private ProductBatchService productBatchService;

    public void importCsv(String csvFilePath) throws Exception {
        CsvBatchParser.parseCsvInBatches(csvFilePath, batch -> {
            List<Product> products = new ArrayList<>();
            for (String[] row : batch) {
                Product p = new Product();
                p.setSku(row[0]);
                p.setName(row[1]);
                p.setPrice(Double.parseDouble(row[2]));
                p.setDescription(row[3]);
                products.add(p);
            }
            productBatchService.saveBatch(products);
        });
    }
}
```

### Notes:
* Customize parsing as per CSV columns and validations.
* Handle exceptions and data validation carefully.
* For huge files, consider processing asynchronously (e.g., sending batches to a queue).
* Configure your database and JPA provider (like Hibernate) for batch size to optimize inserts.