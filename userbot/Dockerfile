FROM node:20-bookworm-slim AS node

FROM eclipse-temurin:21-jre
WORKDIR /app

# Node.js + npm + npx
COPY --from=node /usr/local/bin/node /usr/local/bin/node
COPY --from=node /usr/local/bin/npm  /usr/local/bin/npm
COPY --from=node /usr/local/bin/npx  /usr/local/bin/npx
COPY --from=node /usr/local/lib/node_modules /usr/local/lib/node_modules
ENV PATH="/usr/local/bin:${PATH}"

# yt-dlp (бинарник) + зависимости
RUN apt-get update \
 && apt-get install -y --no-install-recommends ca-certificates curl ffmpeg \
 && rm -rf /var/lib/apt/lists/* \
 && curl -fL --retry 5 --retry-delay 2 \
      -o /usr/local/bin/yt-dlp \
      https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_linux \
 && chmod 755 /usr/local/bin/yt-dlp \
 && /usr/local/bin/yt-dlp --version

COPY target/userbot-0.0.1-SNAPSHOT.jar /app/app.jar
COPY tdlib/tdlib-db/db.sqlite /app/tdlib/tdlib-db/db.sqlite
COPY tdlib/tdlib-db/td.binlog /app/tdlib/tdlib-db/td.binlog

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
