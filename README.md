# ChromaDress

**ChromaDress** is an advanced full-stack platform designed for the fashion industry. By leveraging Artificial Intelligence, the system analyzes garments to provide intelligent suggestions, semantic tagging, and smart wardrobe management.

---

## Project Overview

This project is built as a **monorepo**, featuring three main services that interact seamlessly:

1. **Angular Frontend**: A modern, responsive user interface for managing your digital wardrobe.
2. **Spring Boot Backend**: The core engine handling business logic, JWT authentication, and data persistence.
3. **Python AI Service**: A microservice dedicated to deep-learning image analysis using models like CLIP and PyTorch.

---

## Installation and Setup

### Prerequisites
* **Docker** and **Docker Compose** installed.
* (Optional) Java 21+, Node.js 24+, Python 3.14+ for local development without containers.

### Quick Start (Docker)
The fastest way to spin up the entire ecosystem is using Docker Compose:

1. **Clone the repository**:
  ```bash
  git clone https://github.com/mlunet/ChromaDress-AI-Fashion-Advisor.git ChromaDress
  cd ChromaDress
  ```
2. **Setup Environment Variables**:
Create a `.env` file in the root directory, adding your `DB_USER`, `DB_PASSWORD` and `JWT_SECRET` key:
  ```plaintext
  DB_USER=your_username
  DB_PASSWORD=your_password
  JWT_SECRET=your_secret_key
  ```
  *Note: The JWT_SECRET must be at least 256 bits (32 characters) for HS256 algorithms. You can use this [JWT Key Generator](https://github.com/mlunet/jwt-key-generator) utility to create a cryptographically secure key.*
  
3. **Run the application**:
  ```bash
  docker compose up --build
  ```
The services will be available at:
* **Frontend:** `http://localhost:80` (Auto-redirects to `/it/` or `/en-US/`)
* **Backend API:** `http://localhost:8080`
* **Python AI Service:** `http://localhost:5000`

---

## Development Configuration
If you prefer to run services individually for debugging:
* **Backend:** Copy `java/ChromaDress/src/main/resources/application.properties.example` to `application.properties` and fill in your local DB credentials.
* **AI Service:** Create a Python virtual environment and install dependencies:
  ```bash
  cd python
  pip install torch torchvision --index-url https://download.pytorch.org/whl/cpu
  pip install -r requirements.txt
  ```

---

## Internationalization (i18n)
The Angular frontend supports **English** (source) and **Italian**.
* **Language Switcher:** Integrated in the Navbar for real-time switching.
* **Local Development:**
  - English: `ng serve`
  - Italian: `ng serve --configuration=it`
* **Docker/Production:** The system builds separate bundles for each locale. Nginx is configured to handle routing and deep-linking for each language subpath.
* **Translations:** Managed via XLIFF files in `src/locale/`. 
