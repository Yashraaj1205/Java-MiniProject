# LendFlow: Technical Architecture & Project Guide

LendFlow is a professional-grade lending management system built with **Java** and the **Spring Boot** framework. This document provides a deep dive into the technical details, implementation of OOP principles, and a guide for project presentation.

---

## 1. Technologies & Tools

| Category            | Technology / Tool        | Rationale                                                                 |
|---------------------|--------------------------|---------------------------------------------------------------------------|
| **Core Framework**  | Spring Boot 3.2.4        | Used for its robust dependency injection, auto-configuration, and rapid development capabilities. |
| **Java Version**    | JDK 21                   | Utilized record types and modern language features for clean code.        |
| **Frontend**        | Thymeleaf + Bootstrap 5  | Server-side rendering for security and fast SEO-friendly page loads.       |
| **Database**        | PostgreSQL / JPA         | Relational data integrity is critical for financial records and transactions. |
| **Security**        | Admin Interceptors       | Custom authentication layer to protect dashboard access without heavy overhead. |
| **Notifications**   | Firebase Admin SDK       | Real-time push notifications to keep the lending manager updated on-the-go. |
| **Messaging**       | Java Mail Sender (SMTP)  | Automated loan issuance and due-date alerts sent directly to borrowers.   |
| **Build Tool**      | Maven                    | Standardized dependency management and build lifecycle.                    |

---

## 2. Implementation of OOP Concepts

The project strictly adheres to **Object-Oriented Programming (OOP)** principles to ensure maintainability and scalability.

### A. Abstraction
We used **Interfaces** to separate the "What" from the "How."
- **Example**: `IBorrowerService` and `ILoanService`.
- **Rationale**: The controller doesn't need to know the database logic; it only calls methods defined in the interface. This allows us to switch implementations (e.g., from JPA to a Mock service) without changing the controller code.

### B. Polymorphism
Achieved through **Method Overriding** and **Interface Implementation**.
- **Example**: `BorrowerServiceImpl` implements `IBorrowerService`.
- **Rationale**: Different services (Notification, Recovery) handle diverse logic while appearing uniform to the rest of the application.

### C. Encapsulation
Ensured data security by making entity fields **private** and exposing them via **Getters and Setters**.
- **Example**: The `Loan` entity hides its internal state (like `principalAmount`) and only allows changes through controlled methods.
- **Rationale**: Prevents accidental data corruption from outside the class.

### D. Inheritance
Extensive use of **Extension** and **Composition**.
- **Example**: All repositories (e.g., `LoanRepository`) extend `JpaRepository`.
- **Rationale**: Inherits hundreds of database methods (save, delete, find) for free, reducing boilerplate code significantly.

---

## 3. Error Handling Stratregy

LendFlow implements a tiered error management system:

1.  **Global Controller Advice**: Uses `@ControllerAdvice` to catch exceptions across the entire application and display user-friendly error pages instead of technical stack traces.
2.  **Data Integrity**: JPA `@Column` constraints (e.g., `nullable=false`) ensure the database never stores incomplete loan records.
3.  **UI Verification**: Bootstrap form validation provides real-time feedback to the user before the request even reaches the server.

---

## 4. Why Use These Tools?

- **Spring Boot**: It handles the "plumbing" (server setup, JSON parsing) so we can focus on the business logic of money lending.
- **Lombok**: Used for `@Data` and `@Slf4j` annotations to keep the code clean and free of hundreds of lines of boilerplate getters/setters.
- **FontAwesome**: Provides the professional iconography used for the "Risk Profile" and "Dashboard" indicators.

---

## 5. Presentation Guide (Pitching the Project)

When presenting, focus on these three pillars:

1.  **The "Problem"**: Manual ledger-keeping for individual lenders is prone to errors, missed collections, and poor organization.
2.  **The "Solution"**: LendFlow provides a unified dashboard that automates payment tracking, recovery actions, and professional communication via Email.
3.  **The "Tech Highlight"**: Mention the **Firebase integration** for real-time alerts—this shows that you've integrated a cloud-based external service, which is a major engineering plus.

> [!TIP]
> **Pro Tip for Evaluators**: Always show the **"Delete Borrower" with the confirmation modal**. Highlighting data safety shows that you thought about the "User Experience" (UX) and "Data Integrity," not just the code.

2026
