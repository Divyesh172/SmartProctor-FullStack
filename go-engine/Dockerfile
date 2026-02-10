# Build Stage
FROM golang:1.21-alpine AS builder
WORKDIR /app
COPY go.mod ./
RUN go mod tidy
COPY . .
RUN CGO_ENABLED=0 GOOS=linux go build -o mobile-sentinel main.go

# Runtime Stage
FROM alpine:latest
WORKDIR /root/
COPY --from=builder /app/mobile-sentinel .

# Environment Defaults
ENV PORT=8081
ENV JAVA_BACKEND_URL="http://localhost:8080"
ENV PROCTOR_API_KEY="PROCTOR_SECURE_123"

EXPOSE 8081
CMD ["./mobile-sentinel"]