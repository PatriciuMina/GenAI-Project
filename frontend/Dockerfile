# syntax=docker/dockerfile:1
# Multi-stage build for the React frontend.
# Stage 1 builds the static files with Vite; stage 2 serves them with nginx, which also
# proxies /api to the backend (see nginx.conf), so the browser talks to one origin.

FROM node:24-alpine AS build
WORKDIR /app
# Package files first, so `npm ci` is cached until dependencies change.
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
# The API base URL is relative (/api/v1 by default), so the same build works behind nginx.
RUN npm run build

FROM nginx:1.29-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
