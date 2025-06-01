# 🧰 Sanity Java SDK

A lightweight and type-safe Java SDK for interacting with [Sanity CMS](https://www.sanity.io/).  
Supports GROQ queries, document mutations (create/update/delete), asset uploads, and embedding references in content documents.

## ✅ Features

- 🔎 GROQ query support
- ✏️ Document creation, update, and deletion
- 📤 Upload image and file assets
- 🔗 Helpers to embed asset references in documents
- 🔒 Mockable and testable architecture
- ⚙️ Easy setup via Maven or JitPack

## 🚀 Quick Start

### 📦 Install (JitPack)

Add JitPack to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Then add the dependency:

```xml
<dependency>
    <groupId>com.github.dev-nii-nartey</groupId>
    <artifactId>sanity-java-sdk</artifactId>
    <version>v0.1.0</version>
</dependency>
```

### 🛠 Example Usage

```java
SanityClient client = new SanityClient("yourProjectId", "yourDataset", "yourToken");

// Query posts
String result = client.query("*[_type == 'post']");

// Create a document
Map<String, Object> doc = Map.of("_type", "post", "title", "Hello from Java");
client.createDocument(doc);

// Upload an image
String response = client.uploadImage(new File("path/to/image.jpg"));
String assetId = client.extractAssetIdFromResponse(response);
Map<String, Object> imageRef = client.buildImageReference(assetId);

// Use imageRef in a new post
Map<String, Object> post = Map.of(
    "_type", "post",
    "title", "With an Image",
    "mainImage", imageRef
);
client.createDocument(post);
```

## 🧪 Testing

The SDK includes comprehensive tests for all functionality. The current implementation uses reflection to inject mock HTTP clients for testing.

Run tests with:

```bash
mvn test
```

Example test with mocked HTTP responses:

```java
// Create a mock HttpClient
HttpClient mockHttpClient = Mockito.mock(HttpClient.class);
HttpResponse mockResponse = Mockito.mock(HttpResponse.class);
when(mockResponse.body()).thenReturn("{\"result\": \"test data\"}");
when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
    .thenReturn(mockResponse);

// Create the client
SanityClient client = new SanityClient("projectId", "dataset", "token");

// Inject the mock HttpClient using reflection
java.lang.reflect.Field httpClientField = SanityClient.class.getDeclaredField("httpClient");
httpClientField.setAccessible(true);
httpClientField.set(client, mockHttpClient);

// Now you can test your client
String result = client.query("*[_type == 'test']");
```

## 🗃️ Roadmap

- [x] Queries
- [x] Document mutations
- [x] Image/file asset uploads
- [x] Asset reference helpers
- [x] JUnit test coverage with mock support
- [ ] Realtime subscriptions
- [ ] Fluent query builder
- [ ] Maven Central release

## 🤝 Contributing

1. Fork the repo
2. Create a feature branch
3. Run `mvn test`
4. Submit a pull request

## 🧩 License

MIT License © 2025