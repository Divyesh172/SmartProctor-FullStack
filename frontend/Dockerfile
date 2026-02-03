# --- STAGE 1: BUILD REACT ---
FROM node:18-alpine as build

WORKDIR /app

# Copy package.json to install dependencies
COPY package.json .
COPY package-lock.json .

# Install dependencies (clean install)
RUN npm ci

# Copy the actual code
COPY . .

# Build the React app for production (creates the 'dist' folder)
RUN npm run build

# --- STAGE 2: SERVE WITH NGINX ---
# We use Nginx (a pro web server) to serve the static files
FROM nginx:alpine

# Copy the build output to Nginx's html folder
COPY --from=build /app/dist /usr/share/nginx/html

# Copy a custom Nginx config (we will create this in a second)
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
