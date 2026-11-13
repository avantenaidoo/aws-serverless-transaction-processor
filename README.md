# AWS Serverless Transaction Processor

A Java-based serverless transaction processing application built with AWS Lambda and Amazon DynamoDB.

The project demonstrates serverless application development, transaction validation, idempotency, database persistence, automated testing and AWS deployment.

## Architecture

The application uses AWS Lambda to execute transaction processing logic and Amazon DynamoDB to persist transaction records.

The Lambda function handles incoming transaction requests, validates the supplied data, and uses an idempotency key to prevent duplicate transactions.

See [Architecture Diagram](docs/architecture.mmd).

## Technology Stack

- **Java 25** — application development
- **AWS Lambda** — serverless execution
- **Amazon DynamoDB** — transaction persistence
- **AWS SDK for Java 2.x** — DynamoDB integration
- **Maven** — build and dependency management
- **JUnit 5** — unit testing
- **Mockito** — dependency mocking
- **Amazon CloudWatch** — execution logging

## Features

- Transaction request validation
- Support for monetary amounts and currency codes
- Unique transaction reference generation
- DynamoDB transaction persistence
- Idempotency key-based duplicate detection
- Existing transaction returned when a duplicate request is received
- Automated Java unit tests
- CloudWatch execution monitoring

## Project Structure

```text
aws-serverless-transaction-processor/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/fintech/processor/
│   └── test/
│       └── java/
├── docs/
│   ├── architecture.mmd
│   └── screenshots/
├── infrastructure/
│   └── iam-policy.json
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

## Build and Test

The project uses the Maven Wrapper.

Run the following command from the repository root:

```bash
./mvnw clean package
```

This compiles the application, executes the automated tests and packages the application and its dependencies into a deployable JAR.

### Unit Test Results

The Java test suite contains 20 tests.

- Tests run: 20
- Failures: 0
- Errors: 0
- Skipped: 0

## AWS Deployment

The application was deployed to AWS Lambda using the Java 25 runtime.

The deployed function uses:

- Amazon DynamoDB table: `transactions`
- Environment variable: `TRANSACTIONS_TABLE=transactions`
- Execution role with restricted DynamoDB permissions
- CloudWatch logging permissions
- CloudWatch log retention of 7 days

The deployment package was uploaded directly through the AWS Lambda Console.

## Transaction Testing

The application was tested using Lambda test events.

Example request:

```json
{
  "amount": 125.50,
  "currency": "ZAR",
  "idempotencyKey": "aws-test-transaction-001"
}
```

### Successful Transaction

The first invocation returned:

- Status: `ACCEPTED`
- A generated transaction reference
- The transaction amount and currency

The transaction was successfully persisted in DynamoDB.

### Duplicate Transaction

The same request was submitted again using the original idempotency key.

The application returned:

- Status: `DUPLICATE`
- The original transaction reference
- The existing transaction details

This verified that repeating the request did not create a second transaction.

## AWS Monitoring

Amazon CloudWatch was used to inspect Lambda execution logs.

The successful invocations were verified through their `INIT_START`, `START`, `END` and `REPORT` events.

## Project Evidence

Screenshots of the deployment, transaction testing, DynamoDB persistence, build and test results are stored in:

`docs/screenshots/`

## Security and Permissions

The Lambda execution role follows a restricted-permission approach.

It has access to the required DynamoDB operations on the project's transaction table and the permissions needed to publish execution logs to CloudWatch.

No public Lambda Function URL was enabled.

## Future Improvements

Potential extensions include:

- API Gateway integration
- Additional transaction status handling
- Expanded integration testing
- Infrastructure as Code deployment
- Additional monitoring and operational metrics

## Author

Developed as a practical portfolio project demonstrating Java development and AWS serverless application architecture.
