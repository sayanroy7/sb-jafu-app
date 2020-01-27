FROM oracle/graalvm-ce:19.3.0.2-java8
#RUN gu install ruby
WORKDIR /target
RUN echo 'Going to start JAFU web app'
target/.sb-jafu-app