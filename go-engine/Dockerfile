FROM golang:1.21-alpine

WORKDIR /app

# Copy all files (including the fixed go.mod)
COPY . .

# Ensure dependencies are downloaded and linked
RUN go mod tidy
RUN go mod download

# Build the binary
RUN go build -o main .

CMD ["./main"]