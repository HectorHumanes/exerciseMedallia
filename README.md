<!--
*** Thanks for checking out the Best-README-Template. If you have a suggestion
*** that would make this better, please fork the repo and create a pull request
*** or simply open an issue with the tag "enhancement".
*** Thanks again! Now go create something AMAZING! :D
-->



<!-- PROJECT SHIELDS -->
<!--
*** I'm using markdown "reference style" links for readability.
*** Reference links are enclosed in brackets [ ] instead of parentheses ( ).
*** See the bottom of this document for the declaration of the reference variables
*** for contributors-url, forks-url, etc. This is an optional, concise syntax you may use.
*** https://www.markdownguide.org/basic-syntax/#reference-style-links
-->
[![LinkedIn][linkedin-shield]][linkedin-url]

<!-- ABOUT THE PROJECT -->
## Organization of the repository

* In this repository I've placed a folder containing the Java Spring Boot projects I've created, which contains the code and the tests.
* The "collection", "processing" and "test" folders contain all the necessary stuff to compile and execute the components as a Docker images. The "test" container is, in fact, the testing JAR you provided.
* There is a docker-compose.yml file to build and deploy all the needed stack to test the application. To do so, just type the following in the same directory as the docker-compose file:
  ```sh
  docker-compose up -d
  ```
  This will spawn the components I've developed plus an ArangoDB application as a DB.  
* The "Solution Overview" PDF shows the proposed architecture to deploy this exercise in AWS plus an additional architecture proposal of my own which I think it's more interesting regarding the potential benefits of using it.

### Tasks completed

The completed tasks regarding the proposed exercise are:

* Collection :heavy_check_mark:
* Processing :heavy_check_mark:
* API (inside the processing component) :heavy_check_mark:
* Frontend

<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?style=for-the-badge&logo=linkedin&colorB=555
[linkedin-url]: https://www.linkedin.com/in/hectorhumanes/
