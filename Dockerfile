FROM ubuntu:latest
LABEL authors="zooputer"

ENTRYPOINT ["top", "-b"]