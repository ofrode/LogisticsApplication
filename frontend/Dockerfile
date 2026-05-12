FROM nginx:1.27-alpine

RUN apk add --no-cache gettext

COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf
COPY src/main/resources/static/ /usr/share/nginx/html/

ENV BACKEND_BASE_URL=http://backend:8080

EXPOSE 80

CMD ["/bin/sh", "-c", "envsubst '$BACKEND_BASE_URL' < /etc/nginx/conf.d/default.conf > /etc/nginx/conf.d/default.rendered.conf && mv /etc/nginx/conf.d/default.rendered.conf /etc/nginx/conf.d/default.conf && nginx -g 'daemon off;'"]
