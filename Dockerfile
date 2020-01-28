FROM ubuntu
RUN mkdir -pv /opt/native/
WORKDIR /root/
COPY --from=sb-jafu-app-graal-native:latest /tmp/sb-jafu-app/target/sb-jafu-app /opt/native
#COPY target/sb-jafu-app /opt/native
EXPOSE 8080/tcp
ENTRYPOINT ["/opt/native/sb-jafu-app"]
