# Media-Store

A self-hosted video downloader and organizer that lets you save videos from **YouTube**, **Facebook**, **Instagram**, and **TikTok**, then classify them with custom tags. Built entirely on **Winter-Boot**, a lightweight HTTP framework written from scratch.

## Features

- **Multi-platform downloads** -- paste a link from YouTube, Facebook, Instagram, or TikTok and the app queues it for background download via [yt-dlp](https://github.com/yt-dlp/yt-dlp)
- **WhatsApp chat import** -- upload an exported WhatsApp chat file and the app extracts every video URL automatically
- **Tag system** -- create custom tags and assign them to videos. Filter your library by tag, by duration (short/long), or by tagged/untagged status
- **Video streaming** -- watch your videos directly in the browser with chunked HTTP 206 streaming and range request support
- **Reels mode** -- swipe through your videos TikTok-style with keyboard, mouse wheel, or touch gestures
- **Download to device** -- download any video from your library to your local machine
- **User accounts** -- sign up, log in, change password, delete account. Each user has their own isolated library

https://github.com/user-attachments/assets/c54b0689-10a6-4972-8616-710ad7faaee5

## Architecture

The project is a **two-module Maven monorepo**:

```
media-store/
  winter-boot/          -- custom HTTP framework (the "engine")
  media-store-app/      -- the application itself
```

### Winter-Boot

Winter-Boot is a custom HTTP server framework I built from scratch, inspired by Spring Boot but intentionally much simpler. No dependency injection, no annotation processor, no classpath scanning -- just a clean, functional API.

**What it provides:**

| Component | Description |
|---|---|
| `JonaServer` | HTTP server built on raw `ServerSocket`. Routes requests through inbound filters, endpoint handlers, and outbound filters using `BiConsumer<HttpRequest, HttpResponse>`. Thread pool with configurable concurrency. |
| `HttpRequest` | Parses raw HTTP from the socket: method, path, query params, headers, cookies, body, and `Range` header for video streaming. |
| `HttpResponse` | Builds HTTP responses with support for status codes (200, 206, 302, 401, 404, 409, 500), cookie management, redirects, chunked video streaming (256 KB chunks), and content-disposition for file downloads. |
| `JonaDb` | JDBC wrapper around H2. Provides `insertSingle`, `updateSingle`, `deleteSingle`, `selectSingle`, `selectList`, and `findCount` with parameterized queries and functional row mappers. |
| `Table` | Abstract base class for entities. Handles ID generation and defines the contract (`getInsert`, `getUpdate`, `getDelete`, `getValues`) that each DTO must implement. |
| `StaticPathController` | Serves static files from a configurable directory with MIME type detection and video range-request support. |

**Filter chain flow:**

```
Client Request
      |
  Inbound Filters  (e.g. auth check -- can short-circuit with 401/302)
      |
  Endpoint Handler (or static file serving)
      |
  Outbound Filters (e.g. no-cache headers)
      |
Client Response
```

### Media-Store App

The application layer built on top of Winter-Boot.

**Backend:**
- 6 controllers handling auth, client management, tags, videos, streaming, and video-tag relationships
- Authentication filter using session cookies (UUID-based, stored in-memory)
- No-cache filter for API and public routes
- Background video downloader that polls the database for queued videos
- Platform-specific download strategies (TikTok videos go through an ffmpeg re-encoding pipeline)

**Frontend:**
- Login/signup page
- Dashboard with sidebar (tags, video filters), upload area (single URL + WhatsApp chat import), and video grid
- Reels page with vertical swipe navigation, download, delete, mute, and tag management

**Database (H2):**

```sql
client       (id, email, password)
video        (id, name, link, path, duration_seconds, file_size, video_state, date, client_id)
tag          (id, name, client_id)
video_tag    (video_id, tag_id)   -- many-to-many
```

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| HTTP Server | Winter-Boot (custom, raw sockets) |
| Database | H2 (embedded, TCP mode on port 9092) |
| Video Downloader | yt-dlp + FFmpeg |
| JSON | Gson |
| Logging | SLF4J + Logback |
| Build | Maven (Shade plugin for fat JAR) |
| Code Generation | Lombok |

## Prerequisites

- **Java 21+**
- **Maven 3.8+**
- **[yt-dlp](https://github.com/yt-dlp/yt-dlp)** -- place the binary in `media-store-app/downloaders/`
- **[FFmpeg](https://ffmpeg.org/)** -- required for TikTok downloads (must be on PATH)

## Getting Started

```bash
# Clone the repo
git clone https://github.com/<your-username>/media-store.git
cd media-store

# Build both modules
mvn clean package

# Run the application
java -jar media-store-app/target/media-store-app-1.0-SNAPSHOT.jar
```

The server starts on **http://localhost:8080**. The H2 database is created automatically on first run.

## Configuration

Edit `media-store-app/app.properties`:

```properties
ytDl=downloaders/yt-dlp          # path to yt-dlp binary
videoTemporalPath=tf              # temp folder for downloads in progress
videoOutputPath=downloads         # final destination for downloaded videos
jdbcUrl=jdbc:h2:tcp://localhost:9092/./db/memedb
dbUser=sa
dbPassword=sa
```

## API Overview

| Method | Endpoint | Description |
|---|---|---|
| GET | `/public/login` | Authenticate with email + password |
| GET | `/public/signup` | Create a new account |
| GET | `/api/signout` | Log out (clears session) |
| GET | `/api/addvideobylink` | Queue a video URL for download |
| POST | `/api/processwhatsappchat` | Extract and queue URLs from a WhatsApp chat export |
| GET | `/api/loadvideos` | Get the 10 most recent downloaded videos |
| GET | `/api/streamingvideos` | Stream a video (supports HTTP Range) |
| GET | `/api/getvideosforreel` | Get videos filtered by tag/duration for reels mode |
| GET | `/api/downloadvideo` | Download a video file to device |
| GET | `/api/deletevideo` | Delete a video from library and disk |
| GET | `/api/loadtags` | List all tags for the current user |
| GET | `/api/addtag` | Create a new tag |
| GET | `/api/edittag` | Rename a tag |
| GET | `/api/deletetag` | Delete a tag |
| GET | `/api/gettagsforvideo` | Get tags assigned to a video |
| GET | `/api/addtagtovideo` | Assign a tag to a video |
| GET | `/api/deletetagvideo` | Remove a tag from a video |
| GET | `/api/changepassword` | Change account password |
| GET | `/api/deleteaccount` | Delete account and all associated data |

## Supported Platforms

| Platform | URL patterns |
|---|---|
| YouTube | `youtube.com`, `youtu.be` |
| Instagram | `instagram.com`, `instagr.am` |
| TikTok | `tiktok.com` |
| Facebook | `facebook.com`, `fb.watch` |

## License

This project is for personal and educational use.
