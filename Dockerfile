FROM oracle/graalvm-ce:19.3.0.2-java8
#RUN gu install ruby
RUN mkdir -pv /opt/native/
COPY target/sb-jafu-app /opt/native
EXPOSE 8080/tcp
RUN echo 'Going to start JAFU web app'
CMD /opt/native/sb-jafu-app
