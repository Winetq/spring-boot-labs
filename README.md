## Introduction  
I started developing this project during laboratories on my studies but then I have developed it by myself. Its
assumption is that we have coaches and swimmers and every swimmer can have one coach, but coaches can have
many swimmers. In connection with this we have two microservices. They publish REST API that enables different
operations on coaches and swimmers. Apart from that I implemented frontend (js,css and html) and API gateway
applications. Each component is also dockerized including two database servers for microservices. I also provided
a CI pipeline for microservices using GitHub Actions. This pipeline runs mvn verify in order to include also
integration tests (TestNG, REST Assured, Mockito). Next future improvements like a CD pipeline or Jenkinsfile are
covered as issues. Below section shows an architecture diagram with branches names for every component.

## Architecture Diagram  

![ArchitectureDiagram](https://user-images.githubusercontent.com/62242952/153720955-fdfe94a0-b889-46f8-b912-a367673621e9.png)

## How it works  

In order to run this application you have to build a docker-compose.yml file (lab5_docker) using this command:
```
docker-compose up --build -d
```

Below I recorded a gif that shows how it works and presents several functionalities that are enabled by the
frontend application. Moreover all endpoints are described by Swagger for every microservices.

![video](https://user-images.githubusercontent.com/62242952/153720975-c64cb204-d289-4746-8283-774cc222a8fc.gif)
