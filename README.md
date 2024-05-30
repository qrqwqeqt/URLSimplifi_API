# Link Shortener

## [Link to the design document](https://docs.google.com/document/d/1tOXQjWUA8VQA8GhuJndz-iTPrESmXg4r3Ipj1Rp2qVg/edit?usp=sharing)

### Team:

- [Usenko Anastasia](https://t.me/nastia_u) - Team lead/Back-end developer
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

### Back-end
#### You need to have Docker and Docker compose installed on your computer 

1. [Download JDK 17+](https://www.oracle.com/cis/java/technologies/downloads/)
2. [Download Gradle](https://gradle.org/install/)
3. While in the root folder, enter the command 
```
docker build -t url-shortener .
```
4. Move to the docker folder by entering the command
```
cd docker
```
5. While in the docker folder, enter the command
```
docker-compose up
```

How to run test?

- Run ```./gradlew test``` to start tests
