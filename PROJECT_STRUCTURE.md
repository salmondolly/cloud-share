# Complete folder structure

```text
cloud-share-complete/
├── .env.example
├── .gitignore
├── docker-compose.yml
├── GITHUB_COMMIT_PLAN.md
├── PROJECT_STRUCTURE.md
├── README.md
├── VALIDATION.md
├── scripts/
│   └── smoke-test.sh
├── backend/
│   ├── .dockerignore
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/example/cloudshare/
│       │   │   ├── CloudShareApplication.java
│       │   │   ├── config/
│       │   │   │   ├── CorsConfig.java
│       │   │   │   ├── JwtAuthenticationFilter.java
│       │   │   │   └── SecurityConfig.java
│       │   │   ├── controller/
│       │   │   │   ├── AuthController.java
│       │   │   │   ├── FileController.java
│       │   │   │   ├── PaymentController.java
│       │   │   │   ├── ShareController.java
│       │   │   │   └── UserController.java
│       │   │   ├── dto/
│       │   │   │   ├── auth/
│       │   │   │   │   ├── AuthResponse.java
│       │   │   │   │   ├── LoginRequest.java
│       │   │   │   │   └── RegisterRequest.java
│       │   │   │   ├── common/
│       │   │   │   │   └── ApiErrorResponse.java
│       │   │   │   ├── file/
│       │   │   │   │   ├── FileResponse.java
│       │   │   │   │   └── VisibilityUpdateRequest.java
│       │   │   │   ├── payment/
│       │   │   │   │   ├── CreateOrderRequest.java
│       │   │   │   │   ├── CreateOrderResponse.java
│       │   │   │   │   ├── PaymentResponse.java
│       │   │   │   │   ├── PlanResponse.java
│       │   │   │   │   └── VerifyPaymentRequest.java
│       │   │   │   └── user/
│       │   │   │       └── UserProfileResponse.java
│       │   │   ├── exception/
│       │   │   │   ├── DuplicateResourceException.java
│       │   │   │   ├── FileStorageException.java
│       │   │   │   ├── GlobalExceptionHandler.java
│       │   │   │   ├── InsufficientCreditsException.java
│       │   │   │   ├── InvalidFileException.java
│       │   │   │   ├── PaymentException.java
│       │   │   │   ├── PaymentVerificationException.java
│       │   │   │   ├── ResourceNotFoundException.java
│       │   │   │   └── UnauthorizedOperationException.java
│       │   │   ├── model/
│       │   │   │   ├── DownloadEvent.java
│       │   │   │   ├── DownloadSource.java
│       │   │   │   ├── FileMetadata.java
│       │   │   │   ├── FileVisibility.java
│       │   │   │   ├── Payment.java
│       │   │   │   ├── PaymentStatus.java
│       │   │   │   ├── PlanType.java
│       │   │   │   ├── Role.java
│       │   │   │   └── User.java
│       │   │   ├── repository/
│       │   │   │   ├── DownloadEventRepository.java
│       │   │   │   ├── FileMetadataRepository.java
│       │   │   │   ├── PaymentRepository.java
│       │   │   │   └── UserRepository.java
│       │   │   └── service/
│       │   │       ├── AuthService.java
│       │   │       ├── FileService.java
│       │   │       ├── FileStorageService.java
│       │   │       ├── FileValidationService.java
│       │   │       ├── JwtService.java
│       │   │       ├── LocalFileStorageService.java
│       │   │       ├── PaymentService.java
│       │   │       ├── PaymentSignatureService.java
│       │   │       ├── PlanPolicy.java
│       │   │       ├── PlanPolicyService.java
│       │   │       └── UserService.java
│       │   └── resources/
│       │       └── application.yml
│       └── test/java/com/example/cloudshare/service/
│           ├── PaymentSignatureServiceTest.java
│           └── PlanPolicyServiceTest.java
└── frontend/
    ├── .dockerignore
    ├── Dockerfile
    ├── eslint.config.js
    ├── index.html
    ├── nginx.conf
    ├── package-lock.json
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── App.jsx
        ├── main.jsx
        ├── api/
        │   └── axios.js
        ├── components/
        │   ├── Alert.jsx
        │   ├── EmptyState.jsx
        │   ├── FileTable.jsx
        │   ├── FileUpload.jsx
        │   ├── Navbar.jsx
        │   ├── PageLoader.jsx
        │   ├── PlanCard.jsx
        │   └── ProtectedRoute.jsx
        ├── context/
        │   └── AuthContext.jsx
        ├── pages/
        │   ├── Dashboard.jsx
        │   ├── Login.jsx
        │   ├── MyFiles.jsx
        │   ├── NotFound.jsx
        │   ├── Plans.jsx
        │   ├── Register.jsx
        │   └── SharedFile.jsx
        ├── styles/
        │   └── index.css
        └── utils/
            └── format.js
```
