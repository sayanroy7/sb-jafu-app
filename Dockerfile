FROM oracle/graalvm-ce:19.3.0.2-java8 AS builder
RUN gu install native-image
RUN echo $(native-image --version)
RUN echo "moving $PWD to temp"
RUN mkdir /tmp/sb-jafu-app/
COPY . /tmp/sb-jafu-app
WORKDIR /tmp/sb-jafu-app/
RUN echo "Building graal native image"
RUN sh compile.sh
RUN echo "native image generated!"
RUN ls -ltra target/sb-jafu-app
RUN echo "native image generated!"



FROM ubuntu
RUN mkdir -pv /opt/native/
WORKDIR /root/
COPY --from=builder /tmp/sb-jafu-app/target/sb-jafu-app /opt/native
#COPY target/sb-jafu-app /opt/native
EXPOSE 8080/tcp
RUN echo 'Going to start JAFU web app'
ENTRYPOINT ["/opt/native/sb-jafu-app"]
