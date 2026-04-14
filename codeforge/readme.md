services:
  mysql:
    image: mysql:8
    container_name: codeforge-mysql
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: root123
      MYSQL_DATABASE: codeforge_db
    ports:
      - "3307:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  redis:
    image: redis:7
    container_name: codeforge-redis
    restart: always
    ports:
      - "6379:6379"

  backend:
    build: .
    container_name: codeforge-backend
    depends_on:
      - mysql
      - redis
    restart: always
    ports:
      - "8081:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/codeforge_db?useSSL=false&allowPublicKeyRetrieval=true
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root123


      SPRING_REDIS_HOST: redis          # ✅ ADD THIS
      SPRING_REDIS_PORT: 6379  
      # ✅ REAL host path — change this to your actual machine path
      HOST_SHARED_PATH: /home/anudeep/Desktop/codeforge/Codeforge-online-judge/codeforge/shared
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
      - ./shared:/shared

volumes:
  mysql_data: