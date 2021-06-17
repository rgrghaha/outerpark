
![CB371771-D06B-4169-9DF1-0393C15AFEDC_4_5005_c](https://user-images.githubusercontent.com/82069747/122333349-f7782680-cf72-11eb-919e-780b81f96e37.jpeg)




# OuterPark(뮤지컬 예약)

## 서비스 시나리오

### 기능적 요구사항
```
1. 공연관리자가 예약가능한 뮤지컬과 좌석수를 등록한다.
1. 고객이 뮤지컬 좌석을 예약한다.
1. 뮤지컬이 예약되면 예약된 좌석수 만큼 예약가능한 좌석수에서 차감된다.
1. 예약이 되면 결제정보를 승인한다.
1. 결제정보가 승인되면 알림메시지를 발송한다. 
1. 고객이 뮤지컬 예약을 취소할 수 있다. 
1. 예약이 취소되면 결제가 취소된다.
1. 결제가 최소되면 알림메시지를 발송한다.
1. 고객이 모든 진행내역을 볼 수 있어야 한다.
```

### 비기능적 요구사항
```
1. 트랜잭션
    1. 예약 가능한 좌석이 부족하면 예약이 되지 않는다. --> Sync 호출
    1. 예약이 취소되면 결제가 취소되고 예약가능한 좌석수가 증가한다. --> SAGA, 보상 트랜젝션
1. 장애격리
    1. 결제가 완료되지 않아도 예약은 365일 24시간 받을 수 있어야 한다. --> Async (event-driven), Eventual Consistency
    1. 결제시스템이 과중되면 주문을 잠시동안 받지 않고 결제를 잠시후에 하도록 유도한다. --> Circuit breaker, fallback
1. 성능
    1. 고객은 My Page에서 뮤지컬 예약 진행 상태를 조회할 수 있다. --> CQRS
```




## 분석/설계

### AS-IS 조직 (Horizontally-Aligned)
  ![image](https://user-images.githubusercontent.com/82069747/122220242-1df27f00-ceeb-11eb-9810-6ba9a4a0d725.png)


### TO-BE 조직 (Vertically-Aligned)
  ![image](https://user-images.githubusercontent.com/82069747/122219980-e388e200-ceea-11eb-8bf0-658518de2f83.png)


### Event Storming 결과
![489546E2-B902-49D4-A6AE-1F4C2BD0E6C2](https://user-images.githubusercontent.com/82069747/122322611-b414bc80-cf60-11eb-8cf9-feba63327fcf.jpeg)


### 헥사고날 아키텍처 다이어그램 도출
![125E8CD7-E916-4451-921A-DB008FE144CE_4_5005_c](https://user-images.githubusercontent.com/82069747/122322801-05bd4700-cf61-11eb-905b-c280023306f2.jpeg)

## 구현
분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각각의 포트넘버는 8081 ~ 8084, 8080 이다)
```
  cd musical
  mvn spring-boot:run  
  
  cd reservation
  mvn spring-boot:run  

  cd payment
  mvn spring-boot:run

  cd notice
  mvn spring-boot:run 

  cd customercenter
  mvn spring-boot:run  

  cd gateway
  mvn spring-boot:run 

```

## DDD 의 적용
msaez.io를 통해 구현한 Aggregate 단위로 Entity를 선언 후, 구현을 진행하였다.

Entity Pattern과 Repository Pattern을 적용하기 위해 Spring Data REST의 RestRepository를 적용하였다.

**Musical 서비스의 musical.java**
```
   소스: JPA DB 
 
```

**Payment 서비스의 PolicyHandler.java**
```
   소스: policy handler 
```

DDD 적용 후 REST API의 테스트를 통하여 정상적으로 동작하는 것을 확인할 수 있었다.

- Musical 예약 후 payment 처리 결과
```
   캡쳐: 뮤지컬 예약 등록 테스트 결과 캡쳐
   
   캡쳐: 뮤지컬 예약에 따른 결제승인 결과 캡쳐
   
```


# Gateway 적용
- gateway > applitcation.yml 설정
![image](https://user-images.githubusercontent.com/84000848/122344337-a6236380-cf81-11eb-83d9-98f2311b4f6a.png)
- gateway 테스트
http POST http://gateway:8080/musicals musicalId=1003 name=HOT reservableSeat=100000 
![image](https://user-images.githubusercontent.com/84000848/122344967-4b3e3c00-cf82-11eb-8bb1-9cd21999a6d3.png)
![image](https://user-images.githubusercontent.com/84000848/122345044-601acf80-cf82-11eb-8b79-14a11fdd838e.png)


      
```


### CQRS/saga/correlation
Materialized View를 구현하여, 타 마이크로서비스의 데이터 원본에 접근없이(Composite 서비스나 조인SQL 등 없이)도 내 서비스의 화면 구성과 잦은 조회가 가능하게 구현해 두었다. 본 프로젝트에서 View 역할은 MyPages 서비스가 수행한다.

예약(reservation) 실행 후 MyPages 화면
```
 캡쳐 : 예약등록 후 mypage    
      
```

예약취소(reservation Cancelled)후 MyPages 화면
```
 캡쳐 : 예약취소 후 mypage   
      
```




# 운영

# Deploy

- 1)네임스페이스 만들기
```
kubectl create ns outerpark
kubectl get ns
```
![image](https://user-images.githubusercontent.com/84000848/122322035-c4786780-cf5f-11eb-904f-48d96217d2a1.png)
- 소스가져오기
```
git clone https://github.com/hyucksookwon/outerpark.git
```
![image](https://user-images.githubusercontent.com/84000848/122329826-0a87f800-cf6d-11eb-927a-688f208fab5a.png)

- 2)빌드하기
```
cd outerpark/reservation
mvn package
```
![image](https://user-images.githubusercontent.com/84000848/122330314-eb3d9a80-cf6d-11eb-82cd-8faf7b0c1de7.png)

- 3)도커라이징: Azure 레지스트리에 도커 이미지 빌드 후 푸시하기
```
az acr build --registry outerparkskacr --image outerparkskacr.azurecr.io/reservation:latest .
```
![image](https://user-images.githubusercontent.com/84000848/122330874-e3cac100-cf6e-11eb-89bf-771e533c66ef.png)
![image](https://user-images.githubusercontent.com/84000848/122330924-f513cd80-cf6e-11eb-9c72-0562a27eabcd.png)
![image](https://user-images.githubusercontent.com/84000848/122331422-c2b6a000-cf6f-11eb-8c6d-88820b5c0e20.png)

- 4)컨테이너라이징: 디플로이 생성 확인
```
kubectl create deploy reservation --image=outerparkskacr.azurecr.io/reservation:latest -n outerpark
kubectl get all -n outerpark
```
![image](https://user-images.githubusercontent.com/84000848/122331554-fb567980-cf6f-11eb-83ac-9578bd657c1c.png)

- 5)컨테이너라이징: 서비스 생성 확인
```
kubectl expose deploy reservation --type="ClusterIP" --port=8080 -n outerpark
kubectl get all -n outerpark
```
![image](https://user-images.githubusercontent.com/84000848/122331656-2771fa80-cf70-11eb-8479-aa6cfe567981.png)
- payment, musical, notice, customercenter, gateway에도 동일한 작업 반복
*최종 결과
![image](https://user-images.githubusercontent.com/84000848/122335324-0dd3b180-cf76-11eb-967a-6ddd4c7aaeaa.png)

- deployment.yml을 사용하여 배포 (reservation의 deployment.yml 추가)
![image](https://user-images.githubusercontent.com/84000848/122332320-2d1c1000-cf71-11eb-8766-b494f157f247.png)
- deployment.yml로 서비스 배포
```
kubectl apply -f kubernetes/deployment.yml
```
