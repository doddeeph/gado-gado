# REST API
`REST`, which stands for `Representational State Transfer`, is an architectural style for designing networked applications. A REST `API` (`Application Programming Interface`) is a set of rules and conventions for building and interacting with web services that adhere to the principles of REST.

## Designing a REST API
Designing a REST API involves several key aspects such as defining resources, choosing HTTP methods, handling CRUD operations, error handling, versioning, authentication, and documentation. Here are some steps and considerations for designing a REST API:

1. **Define Resources**:
- Identify the resources your API will expose (e.g., users, products, orders).
- Use nouns to represent resources (e.g., `/users`, `/products`).
2. Choose HTTP Methods:
Use HTTP methods to perform CRUD operations:
   - `GET` for retrieving data.
   - `POST` for creating new resources.
   - `PUT` or `PATCH` for updating existing resources.
   - `DELETE` for removing resources.
3. Resource Endpoints:
- Define clear and consistent endpoint URLs (e.g., `/users/{id}`, `/products/{id}`).
- Use `plural nouns` for collections (e.g., `/users`, `/products`).
4. Request and Response Formats:
- Use JSON or XML for request and response payloads.
- Follow a consistent data format for readability and ease of use.
5. Error Handling:
- Use appropriate HTTP status codes for different scenarios (e.g., 200 for successful requests, 404 for not found, 400 for bad requests, 500 for server errors).
- Provide meaningful error messages in the response body.
6. Authentication and Authorization:
- Implement authentication mechanisms such as OAuth, JWT, or API keys.
- Use authorization to control access to resources based on user roles or permissions.
7. Versioning:
- Consider versioning your API to manage changes and ensure backward compatibility (e.g., `/v1/users`, `/v2/users`).
8. Documentation:
- Create comprehensive API documentation using tools like Swagger, OpenAPI, or Postman.
- Include endpoints, request/response formats, authentication methods, and usage examples.
9. Testing:
- Test your API endpoints thoroughly using tools like Postman or cURL.
- Perform integration testing to validate interactions between components.
10. Security:
- Implement security best practices such as HTTPS for data encryption.
- Validate and sanitize inputs to prevent security vulnerabilities like SQL injection or XSS attacks.

## CRUD
A simple example of a RESTful API for managing a collection of books. In this example, we'll have endpoints for retrieving a list of books, getting information about a specific book, adding a new book, updating an existing book, and deleting a book.
1. Retrieve a List of Books (GET):
```
GET /api/v1/books
```
This endpoint returns a list of all books in the collection.

2. Retrieve Information about a Specific Book (GET):
```
GET /api/v1/books/{bookId}
```
This endpoint returns details about a specific book identified by `{bookId}`.

3. Add a New Book (POST):
```
POST /api/v1/books
```
This endpoint allows you to add a new book to the collection. The book details would be sent in the request body, typically in JSON format.

4. Update an Existing Book (PUT):
```
PUT /api/v1/books/{bookId}
```
This endpoint allows you to update the details of a specific book identified by `{bookId}`. The updated book details would be sent in the request body.

5. Delete a Book (DELETE):
```
DELETE /api/v1/books/{bookId}
```
This endpoint allows you to delete a specific book identified by `{bookId}`.

These are basic examples of RESTful API endpoints. When you make a request to one of these endpoints using standard HTTP methods (GET, POST, PUT, DELETE), the server responds with the appropriate data or performs the requested operation. The responses are often in JSON format for easy parsing and consumption by client applications.

For instance, a response for retrieving a list of books might look like:
```
[
  {
    "id": 1,
    "title": "The Great Gatsby",
    "author": "F. Scott Fitzgerald",
    "publishedYear": 1925
  },
  {
    "id": 2,
    "title": "To Kill a Mockingbird",
    "author": "Harper Lee",
    "publishedYear": 1960
  },
  // ...
]
```
And a response for retrieving information about a specific book:
```
{
  "id": 1,
  "title": "The Great Gatsby",
  "author": "F. Scott Fitzgerald",
  "publishedYear": 1925,
  "genre": "Fiction"
}
```

## Pageable Responses in a RESTful API
When dealing with pageable responses in a RESTful API, it's common to use query parameters to allow clients to request a specific page of data. The typical query parameters for pageable responses include:
- `page`: The page number to retrieve (starting from 0 or 1).
- `size`: The number of items per page.
- `sort`: The sorting criteria for the results.

Here's an example of how you might structure a pageable response using query parameters:
```
GET /api/v1/books?page=1&size=10&sort=title,asc
```

In this example, the client is requesting the second page (`page=1`), with 10 items per page (`size=10`), sorted by the `title` field in `ascending` order (`sort=title,asc`).

Now, let's look at how you might structure the response. Typically, the response would include metadata about the pagination, such as the total number of items, the current page, the number of items per page, etc., along with the actual data.

Here's an example response in JSON format:
```
{
  "content": [
    {
      "id": 11,
      "title": "The Catcher in the Rye",
      "author": "J.D. Salinger",
      "publishedYear": 1951
    },
    {
      "id": 12,
      "title": "1984",
      "author": "George Orwell",
      "publishedYear": 1949
    },
    // ... (more items)
  ],
  "pageable": {
    "pageNumber": 1,
    "pageSize": 10,
    "offset": 10,
    "paged": true,
    "unpaged": false
  },
  "totalPages": 5,
  "totalElements": 50,
  "last": false,
  "first": true,
  "sort": {
    "sorted": true,
    "unsorted": false,
    "empty": false
  },
  "numberOfElements": 10,
  "size": 10,
  "number": 1,
  "empty": false
}
```
Explanation of key fields in the response:
- `content`: An array containing the actual data for the requested page.
- `pageable`: Information about the current page, such as page number, page size, and sorting information.
- `totalPages`: The total number of pages available.
- `totalElements`: The total number of items across all pages.
- `last`: Indicates if the current page is the last one.
- `first`: Indicates if the current page is the first one.
- `sort`: Information about the sorting of the results.
- `numberOfElements`: The number of items in the current page.
- `size`: The number of items per page.
- `number`: The current page number.
- `empty`: Indicates if the current page is empty.

## HTTP PATCH
### 📘 Example Scenario: Update part of a book record
Let's say we have a book resource:

Existing Book (ID = 1)
```
{
  "id": 1,
  "title": "The Great Gatsby",
  "author": "F. Scott Fitzgerald",
  "publishedYear": 1925,
  "genre": "Fiction"
}
```

### ✅ PATCH Request
You want to update just the `genre` of the book with ID `1`.
Request:
```
PATCH /api/v1/books/1
Content-Type: application/json
```
Request Body:
```
{
  "genre": "Classic Literature"
}
```

### 🔁 Expected Response:
```
HTTP/1.1 200 OK
Content-Type: application/json
```
```
{
  "id": 1,
  "title": "The Great Gatsby",
  "author": "F. Scott Fitzgerald",
  "publishedYear": 1925,
  "genre": "Classic Literature"
}
```

### 🛠 In Java Spring Boot (Example Implementation):
```
@PatchMapping("/api/v1/books/{id}")
public ResponseEntity<Book> updateBookPartial(
        @PathVariable Long id,
        @RequestBody Map<String, Object> updates) {

    Book book = bookRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

    updates.forEach((key, value) -> {
        Field field = ReflectionUtils.findField(Book.class, key);
        if (field != null) {
            field.setAccessible(true);
            ReflectionUtils.setField(field, book, value);
        }
    });

    bookRepository.save(book);
    return ResponseEntity.ok(book);
}
```

### 🚨 Notes:
- Use `PATCH` when updating only specific fields.
- Be cautious with partial updates — validate input to avoid corrupting the resource.
- `PATCH` is ideal for performance when you're only changing a small part of a resource.

## HTTP Response Status Codes
HTTP response status codes are three-digit numbers returned by the server to indicate the outcome of a request. They are grouped into categories based on their first digit:

### ✅ 2xx: Success
These codes mean the request was successfully received, understood, and processed.
| Status Code      | Meaning          | Description                                                                        |
| ---------------- | ---------------- | ---------------------------------------------------------------------------------- |
| `200 OK`         | Success          | Standard response for a successful GET, PUT, DELETE, or PATCH request.             |
| `201 Created`    | Resource created | Returned when a new resource is successfully created (e.g., after a POST).         |
| `202 Accepted`   | Request accepted | The request has been accepted for processing, but the processing is not completed. |
| `204 No Content` | No content       | Successful request but no content is returned (common after a DELETE).             |

#### ✅ 2xx Success Responses
`200 OK`
Use Case: Client requests a list of books.
Request:
```
GET /api/v1/books
```
Response:
```
HTTP/1.1 200 OK
Content-Type: application/json
```
```
[
    { "id": 1, "title": "1984", "author": "George Orwell" },
    { "id": 2, "title": "Brave New World", "author": "Aldous Huxley" }
]
```

`201 Created`
Use Case: Client successfully creates a new book.
Request:
```
POST /api/v1/books
Content-Type: application/json
```
```
{
    "title": "The Hobbit",
    "author": "J.R.R. Tolkien"
}
```
Response:
```
HTTP/1.1 201 Created
Location: /api/v1/books/3
```
```
{
    "id": 3,
    "title": "The Hobbit",
    "author": "J.R.R. Tolkien"
}
```

`204 No Content`
Use Case: Client deletes a book.
Request:
```
DELETE /api/v1/books/3
```
Response:
```
HTTP/1.1 204 No Content
```
✅ No response body is sent.

### 🚫 4xx: Client Errors
These codes indicate the client made an error (e.g., malformed request, unauthorized, resource not found).
| Status Code                | Meaning                     | Description                                                                      |
| -------------------------- | --------------------------- | -------------------------------------------------------------------------------- |
| `400 Bad Request`          | Invalid request             | The server couldn't understand the request due to invalid syntax.                |
| `401 Unauthorized`         | Authentication required     | The client must authenticate itself to get the requested response.               |
| `403 Forbidden`            | Access denied               | The client is authenticated but does not have permission to access the resource. |
| `404 Not Found`            | Resource not found          | The server can't find the requested resource.                                    |
| `405 Method Not Allowed`   | Invalid method              | The HTTP method is not allowed for the requested URL.                            |
| `409 Conflict`             | Conflict with current state | Usually due to conflicting updates (e.g., duplicate resource).                   |
| `422 Unprocessable Entity` | Validation failed           | The request is well-formed but has semantic errors (e.g., failed validations).   |

#### 🚫 4xx Client Error Responses
`400 Bad Request`
Use Case: Missing required field in the request.
Request:
```
POST /api/v1/books
Content-Type: application/json
```
```
{
    "title": ""
}
```
Response:
```
HTTP/1.1 400 Bad Request
```
```
{
    "error": "Title must not be empty"
}
```

`401 Unauthorized`
Use Case: Client accesses a protected route without a token.
Request:
```
GET /api/v1/books
```
Response:
```
HTTP/1.1 401 Unauthorized
```
```
{
    "error": "Authentication token is missing or invalid"
}
```

`403 Forbidden`
Use Case: User is authenticated but not an admin.
Request:
```
DELETE /api/v1/books/1
```
Response:
```
HTTP/1.1 403 Forbidden
```
```
{
    "error": "You do not have permission to perform this action"
}
```

`404 Not Found`
Use Case: Client requests a book that doesn't exist.
Request:
```
GET /api/v1/books/999
```
Response:
```
HTTP/1.1 404 Not Found
```
```
{
    "error": "Book not found"
}
```

`409 Conflict`
Use Case: Trying to create a book with a title that already exists (unique constraint).
Request:
```
POST /api/v1/books
```
```
{
    "title": "1984",
    "author": "George Orwell"
}
```
Response:
```
HTTP/1.1 409 Conflict
```
```
{
    "error": "Book with title '1984' already exists"
}
```

### 💥 5xx: Server Errors
These codes indicate the server failed to fulfill a valid request due to some internal error.
| Status Code                 | Meaning                        | Description                                                                                       |
| --------------------------- | ------------------------------ | ------------------------------------------------------------------------------------------------- |
| `500 Internal Server Error` | Generic server error           | An unexpected condition was encountered.                                                          |
| `501 Not Implemented`       | Not supported                  | The server does not support the functionality required to fulfill the request.                    |
| `502 Bad Gateway`           | Invalid response from upstream | Often occurs when a gateway or proxy server receives an invalid response from an upstream server. |
| `503 Service Unavailable`   | Temporarily overloaded         | The server is not ready to handle the request (e.g., maintenance).                                |
| `504 Gateway Timeout`       | Timeout from upstream server   | A gateway or proxy did not receive a timely response from an upstream server.                     |

#### 💥 5xx Server Error Responses
`500 Internal Server Error`
Use Case: Unexpected exception in the server code.
Response:
```
HTTP/1.1 500 Internal Server Error
```
```
{
    "error": "Something went wrong. Please try again later."
}
```

`503 Service Unavailable`
Use Case: Server under maintenance.
Response:
```
HTTP/1.1 503 Service Unavailable
Retry-After: 120
```
```
{
    "error": "The service is temporarily unavailable. Please try again later."
}
```

### 🧠 Tips:
- Always return 2xx for success with meaningful status (`200`, `201`, or `204`).
- Use 4xx to help clients fix problems (e.g., `400` for bad input, `401` for auth issues).
- Only use 5xx for server-side problems — these should be logged and monitored closely.

## Authentication & Authorization
### 🔐 1. What is Authentication?
`Authentication` verifies `who you are` (e.g., `username` & `password` or `token`).

### 🛡️ 2. What is Authorization?
`Authorization` verifies `what you're allowed to do` (e.g., user `roles` and `permissions`).

### 🎯 Example Scenario
We’re building a Book Management API:
- Users must log in to get a JWT token.
- Only users with the `ROLE_ADMIN` can delete books.
- Regular users (`ROLE_USER`) can only view books.

### 🔄 Example Flow
1. Login (Authentication)
```
POST /auth/login
Content-Type: application/json
```
```
{
    "username": "admin",
    "password": "password"
}
```
🔁 Response:
```
{
    "token": "eyJhbGciOiJIUzI1NiIs..."
}
```
2. Access secured endpoint with token (Authorization)
```
GET /api/v1/books
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```
✅ Response:
```
["Book 1", "Book 2"]
```
3. Try DELETE with USER role (Authorization fails)
```
DELETE /api/v1/books/1
Authorization: Bearer <user-token>
```
❌ Response:
```
{
    "error": "Access Denied"
}
```
