# Link Shortener

## [Design document](https://docs.google.com/document/d/1iMQEvyzFKvNWu4vGWAc10u-2C6-AExHxE7dNiprqAkU/edit#heading=h.k57aj13un2t)

### Team:

- [Usenko Anastasiia](https://t.me/nastia_u) - Team lead/Back-end developer
- [Briukhov Arsenii](https://t.me/qrqwqeqt) - Back-end developer
- [Melnyk Georgii](https://t.me/geor9ii) - Front-end - developer

### Short description

The URL Shortener project is a web service designed to convert long URLs into short ones. The user enters a long URL, and our service provides them with a unique shortened URL, which automatically redirects to the original long URL when they click through.

## How to run the project

### Front-end part
You need to have [Node.js](https://nodejs.org/en/download/prebuilt-installer) version 20 and higher installed on your computer

First of all, you should clone front-end repository:
```
git clone https://github.com/nastiausenko/URLFrontend.git
```
Then go to the working directory and run 
```
npm install
``` 
to install all dependencies.
Then you can run 
```
npm run dev
``` 
to start the front-end part.

![image](https://github.com/qrqwqeqt/URLSimplifyAPI/assets/114734522/3cb5b6f3-dcf6-488e-a7c8-62e61091e2e1)

### Back-end
#### You need to have Docker and Docker compose installed on your computer 

1. [Download JDK 17+](https://www.oracle.com/cis/java/technologies/downloads/)
2. [Download Gradle](https://gradle.org/install/)
3. While in the root directory, build a bootJar using command
```
./gradlew bootJar
```
4. While in the root folder, enter the command to build the Docker image
```
docker build -t url-shortener .
```
5. Move to the docker folder by entering the command
```
cd docker
```
6. While in the docker folder, enter the command to run Docker containers with Docker Compose
```
docker-compose up
```
After executing these commands, your URL Shortener app should be launched and available for use.

### [Swagger](http://localhost:8080/url-shortener/swagger-ui/index.html#/)

### Run tests
> **NOTE:** postgres and redis containers must be running

-Run tests from the root directory of the repository using the command:
```
./gradlew test
```
