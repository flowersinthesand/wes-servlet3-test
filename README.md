# Test projects for wes Servlet3

This repository consists of project that tests whether a given Servlet 3 container is able to work with wes Servlet. It's true that any server properly implementing Servlet 3 works with wes Servlet and that's why there is no test per server in wes repository. But unfortunately some servers are not. By running each test, you can see which server does pass the test and make clear your decision.

---

These test projects are private so you have to clone the repository or download it. 
```
git clone https://github.com/flowersinthesand/wes-servlet3-test.git
cd wes-servlet3-test
```

Unless otherwise stated, the server will be managed as dependency and executed in embedded mode.

```
cd jetty9
mvn test
```
