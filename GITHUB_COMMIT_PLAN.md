# GitHub commit plan

Do not upload the finished repository as one unexplained commit. Commit it in feature-sized stages so the history shows how the application was built.

## 0. Create the repository

From the project root:

```bash
git init
git branch -M main
git add .gitignore .env.example PROJECT_STRUCTURE.md
git commit -m "chore: initialize cloud share repository"
```

Create an empty GitHub repository named `cloud-share`, then connect it:

```bash
git remote add origin https://github.com/YOUR_USERNAME/cloud-share.git
git push -u origin main
```

Never commit `.env`.

## Recommended staged commits

### Commit 1 — Backend foundation

```bash
git add backend/pom.xml backend/src/main/java/com/example/cloudshare/CloudShareApplication.java backend/src/main/resources/application.yml
git commit -m "build: initialize Spring Boot backend"
```

Explainable result: Java 21 Maven application with MongoDB, Security, validation, JWT, and Razorpay dependencies.

### Commit 2 — Domain models and repositories

```bash
git add backend/src/main/java/com/example/cloudshare/model backend/src/main/java/com/example/cloudshare/repository
git commit -m "feat: add MongoDB domain models and repositories"
```

### Commit 3 — DTOs and error handling

```bash
git add backend/src/main/java/com/example/cloudshare/dto backend/src/main/java/com/example/cloudshare/exception
git commit -m "feat: add API DTO validation and global error handling"
```

### Commit 4 — Authentication and JWT security

```bash
git add backend/src/main/java/com/example/cloudshare/config backend/src/main/java/com/example/cloudshare/service/AuthService.java backend/src/main/java/com/example/cloudshare/service/JwtService.java backend/src/main/java/com/example/cloudshare/controller/AuthController.java backend/src/main/java/com/example/cloudshare/controller/UserController.java
git commit -m "feat: implement BCrypt and JWT authentication"
```

### Commit 5 — Plan and credit policy

```bash
git add backend/src/main/java/com/example/cloudshare/service/PlanPolicy.java backend/src/main/java/com/example/cloudshare/service/PlanPolicyService.java backend/src/main/java/com/example/cloudshare/service/UserService.java
git commit -m "feat: add plan limits and atomic upload credits"
```

### Commit 6 — File storage and validation

```bash
git add backend/src/main/java/com/example/cloudshare/service/FileStorageService.java backend/src/main/java/com/example/cloudshare/service/LocalFileStorageService.java backend/src/main/java/com/example/cloudshare/service/FileValidationService.java
git commit -m "feat: add replaceable local file storage adapter"
```

### Commit 7 — Upload, download, and sharing APIs

```bash
git add backend/src/main/java/com/example/cloudshare/service/FileService.java backend/src/main/java/com/example/cloudshare/controller/FileController.java backend/src/main/java/com/example/cloudshare/controller/ShareController.java
git commit -m "feat: implement secure file upload and sharing"
```

### Commit 8 — Razorpay integration

```bash
git add backend/src/main/java/com/example/cloudshare/service/PaymentService.java backend/src/main/java/com/example/cloudshare/service/PaymentSignatureService.java backend/src/main/java/com/example/cloudshare/controller/PaymentController.java
git commit -m "feat: verify Razorpay payments and apply plan credits"
```

### Commit 9 — Backend unit tests

```bash
git add backend/src/test
git commit -m "test: cover plan policies and payment signatures"
```

### Commit 10 — React application foundation

```bash
git add frontend/package.json frontend/package-lock.json frontend/vite.config.js frontend/eslint.config.js frontend/index.html frontend/src/main.jsx frontend/src/App.jsx frontend/src/styles
git commit -m "build: initialize React Vite Tailwind frontend"
```

### Commit 11 — Frontend authentication

```bash
git add frontend/src/api frontend/src/context frontend/src/components/ProtectedRoute.jsx frontend/src/components/Navbar.jsx frontend/src/pages/Login.jsx frontend/src/pages/Register.jsx
git commit -m "feat: add frontend JWT authentication flow"
```

### Commit 12 — File management UI

```bash
git add frontend/src/components/FileUpload.jsx frontend/src/components/FileTable.jsx frontend/src/components/Alert.jsx frontend/src/components/EmptyState.jsx frontend/src/components/PageLoader.jsx frontend/src/pages/Dashboard.jsx frontend/src/pages/MyFiles.jsx frontend/src/pages/SharedFile.jsx frontend/src/utils
git commit -m "feat: add drag-and-drop file management UI"
```

### Commit 13 — Plans and checkout UI

```bash
git add frontend/src/components/PlanCard.jsx frontend/src/pages/Plans.jsx frontend/src/pages/NotFound.jsx
git commit -m "feat: add plans and Razorpay Checkout UI"
```

### Commit 14 — Containers and documentation

```bash
git add backend/Dockerfile backend/.dockerignore frontend/Dockerfile frontend/.dockerignore frontend/nginx.conf docker-compose.yml scripts README.md GITHUB_COMMIT_PLAN.md
git commit -m "docs: add Docker deployment and project guide"
```

## Validate before pushing

```bash
cd frontend
npm ci
npm run lint
npm run build
cd ../backend
mvn clean test
cd ..
docker compose config
docker compose up --build
```

Then test registration, login, upload, private download, public link, visibility toggle, deletion, insufficient credits, payment order creation, valid verification, and invalid-signature rejection.

## Push

```bash
git status
git log --oneline --decorate -15
git push origin main
```

## Suggested GitHub repository description

```text
Secure full-stack file sharing platform using Java 21, Spring Boot, JWT, MongoDB, React, Docker, and Razorpay verification.
```

## Suggested topics

```text
java spring-boot spring-security jwt mongodb react vite tailwindcss docker razorpay file-sharing
```

## First release tag

After the Docker flow works on your machine:

```bash
git tag -a v0.1.0 -m "Cloud Share MVP"
git push origin v0.1.0
```

Do not tag a release before testing the exact commit that the tag points to.
