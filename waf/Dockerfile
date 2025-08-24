FROM owasp/modsecurity-crs:4.17.1-nginx-alpine-202508061208

# (선택) Nginx 템플릿 커스터마이즈
#COPY nginx/templates/ /etc/nginx/templates/

# 커스텀 룰을 이미지에 포함 (900/999 + 추가 룰)
COPY rules/*.conf /opt/owasp-crs/rules/
