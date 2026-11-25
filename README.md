# Files API Wrapper

Java client for the Scisbo files service: signed uploads, object metadata and signed download URLs.

## Install (JitPack)

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.ahmtvc</groupId>
    <artifactId>scisbo-files-api-wrapper</artifactId>
    <version>3.0.0</version>
</dependency>
```

Requires Java 17.

## Setup

```java
FilesApiClient client = new FilesApiClient(FilesApiConfig.builder()
        .apiKey(apiKey)          // this service's key; it may use only the buckets it was granted
        .baseUrl(filesBaseUrl)   // e.g. https://host/api-gateway/api/v1/files
        .build());
```

Create one client and reuse it.

## Model

Objects live in **buckets** under a **key** (`folder/sub/name.ext`) and are addressed by **id**.
Store ids, not URLs. Private objects are read through short-lived signed URLs. Public ones (like
avatars) have a permanent URL.

## Upload from a browser (preferred)

```java
UploadTicket ticket = client.createUpload(UploadRequest.builder()
        .bucket("chat")
        .path(groupId + "/messages")
        .filename("holiday.jpg")
        .contentType("image/jpeg")
        .size(sizeInBytes)
        .ownerId(userId)
        .build());

// Return the ticket to the browser. It sends the bytes itself:
//   fetch(ticket.uploadUrl, { method: ticket.method, headers: ticket.headers, body: file })
```

The type and size are checked when the upload is created, and again against the bytes that
arrive. The URL can be retried until `expiresAt`, and only one upload completes. A name that is
already taken gets numbered (`holiday (2).jpg`).

## Upload from a service

```java
FileObject stored = client.uploadDirect(request, inputStream);
```

## Read

```java
List<FileObject> files = client.getObjects(List.of(idA, idB));   // unknown or unreadable ids are left out
Optional<FileObject> file = client.getObject(id);

Map<String, SignedUrls> urls = client.signDownloadUrls(ids, Duration.ofHours(1));
urls.get(id).thumbnail();   // falls back to the original when no thumbnail exists

String avatar = client.publicUrl(photoId, Variants.THUMBNAIL);   // public objects only, no call
```

Image fields (`width`, `height`, `blurhash`, `variants`) are null until processing finishes, and
for anything that is not an image.

## Delete

```java
client.deleteObject(id);   // deleting something already gone is not an error
```

## Errors

Every failure throws `FilesApiException`. Its message is the service's message, and
`getStatusCode()` is the HTTP status (-1 when the service could not be reached).
