FROM node:18-alpine AS build
WORKDIR /app
COPY package.json .
RUN npm install --silent
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/build /usr/share/nginx/html
COPY nginx.conf.template /etc/nginx/templates/default.conf.template
EXPOSE 80
CMD ["/bin/sh","-c","envsubst '$$BACKEND_HOSTPORT' < /etc/nginx/templates/default.conf.template > /etc/nginx/conf.d/default.conf && nginx -g 'daemon off;'"]
