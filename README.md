# NYT Bestsellers Analytics Platform

A full-stack, reactive Java web application built with **Spring Boot 3 (WebFlux)**. 
The platform dynamically merges real-time bestseller data from the **New York Times Books API** with visual 
metadata and high-resolution cover art from the **Google Books API** and **Open Library CDN**.

---

## Overview & Architecture

This application demonstrates a reactive, non-blocking pipeline using **Spring WebFlux** and 
**Project Reactor**:

1. **Category Retrieval:** Fetches available NYT Bestseller categories via a custom controller endpoint.
2. **Concurrent API Stitching:** Requests the current bestsellers for a selected list from the NYT API,
   then concurrently queries the Google Books API for cover thumbnails using `Flux` stream operators.
4. **Resilient Cover Discovery:** Implements a three-tier fallback mechanism to ensure maximum image
   coverage across all book titles:
   - **Tier 1:** Exact ISBN-13 match via Google Books.
   - **Tier 2:** Title + Author string search via Google Books.
   - **Tier 3:** Direct image fetch via Open Library CDN (`covers.openlibrary.org`).
6. **Editorial Frontend:** Renders a clean, responsive layout utilizing custom serif typography, hover
   states, and an interactive pop-up details drawer.

---

## Tech Stack & Dependencies

- **Language & Runtime:** Java 21
- **Framework:** Spring Boot 3 (Spring WebFlux, DevTools)
- **Asynchronous Execution:** Project Reactor (`Mono`, `Flux`)
- **JSON & Data Binding:** Jackson, Lombok
- **Native Platform Support:** Netty macOS Native DNS Resolver (`netty-resolver-dns-native-macos` for
  Apple Silicon)
- **Frontend:** HTML5, Modern CSS (CSS Grid, CSS Custom Properties), JavaScript (Fetch API)

---

## 📂 Project Structure

```text
book-analytics/
├── src/
│   ├── main/
│   │   ├── java/com/reneeemlee/book_analytics/
│   │   │   ├── config/          # WebClient beans configuration
│   │   │   ├── controller/      # REST Endpoints (/api/books)
│   │   │   ├── dto/             # NYT & Google Books data transfer objects
│   │   │   └── service/         # Business logic & reactive API stitching
│   │   └── resources/
│   │       ├── static/          # index.html (Single Page Application UI)
│   │       └── application.properties
│   └── test/
├── pom.xml                      # Maven build configuration
└── README.md
