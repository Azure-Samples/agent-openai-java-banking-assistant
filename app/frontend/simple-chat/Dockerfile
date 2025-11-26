FROM node:18-alpine AS build

# make the 'app' folder the current working directory
WORKDIR /app

COPY . .

# install project dependencies
RUN npm install
RUN npm run build

FROM nginx:alpine

WORKDIR /usr/share/nginx/html
COPY --from=build /app/build .
COPY --from=build /app/nginx/nginx.conf.template /etc/nginx/conf.d

EXPOSE 80

CMD ["/bin/sh", "-c", "envsubst < /etc/nginx/conf.d/nginx.conf.template > /etc/nginx/conf.d/default.conf && nginx -g \"daemon off;\""]
