# Spring Boot GraalVM Native App

## Background

This project is inspired by [Spring Fu incubator](https://github.com/spring-projects-experimental/spring-fu)
and leverages an optimized builtin GraalVM feature that discards CGLIB and
`ImageBanner`. 

Class initialization is configured by default at build time except for few Lazy initializations.

## Features

This app has following features: 
- Functional Web Controller
- HTTP Message converter - text, json, xml
- REST error handler - similar of `@ExceptionHandler`
- Jackson
- Functional Spring Security - Based on JWT token and scope verification
- Functional Spring Data MongoDB support

## Quick start

This project can be completely run in docker containers. 
To run this project all you need is `docker` (17.x+) and `docker-compose`. You, do not need any GraalVM based runtime at all.

### Run `docker-compose up`
- This will build the `src` code with a maven build and then trigger GraalVM `native-image` compilation.
- Next, it will create a thin image based on `ubuntu` with the native-image.
- It will start `mongodb` and a DB call - `sb-jafu-app`
- Start the Spring Boot services with bunch of controllers.

## Notes:

- Only first time it will build the full docker image from src code. 
If you want to rebuild the image from src code run - `docker-compose down` and `docker-compose kill`
- Then build - `docker-compose build` and `docker-compose up`

## Security: 

- Security implementation is based on Spring Security Handler Function.
- Uses - `io.jsonwebtoken` library to build and parse stateless JWT token.
- Support for stateful `Jws` is WIP.

### Work in progress for Dsl configs for Spring data mongodb, REST Handler fucntions and Resporitory Dsls.   
 


