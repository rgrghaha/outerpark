
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




## Saga, CQRS, Correlation, Req/Resp
뮤지컬 예약 시스템은 각 마이크로 서비스가 아래와 같은 기능으로 구성되어 있으며,
마이크로 서비스간 통신은 기본적으로 Pub/Sub 을 통한 Event Driven 구조로 동작하도록 구성하였음.
![image](https://user-images.githubusercontent.com/84003381/122408528-6da17b00-cfbd-11eb-9651-49f754758615.png)

![image](https://user-images.githubusercontent.com/84003381/122410244-b574d200-cfbe-11eb-8b49-3dad0dafe79b.png)

### <구현기능별 요약>

**Saga**
- 마이크로 서비스간 통신은 Kafka를 통해 Pub/Sub 통신하도록 구성함. 이를 통해 Event Driven 구조로 각 단계가 진행되도록 함
- 아래 테스트 시나리오의 전 구간 참조

**CQRS**
- customercenter (myPage) 서비스의 경우의 경우, 각 마이크로 서비스로부터 Pub/Sub 구조를 통해 받은 데이터를 이용하여 자체 DB로 View를 구성함.
- 이를 통해 여러 마이크로 서비스에 존재하는 DB간의 Join 등이 필요 없으며, 성능에 대한 이슈없이 빠른 조회가 가능함.
- 테스트 시나리오의 3.4 과 5.4 항목에 해당

**Correlation**
- 예약을 하게되면 reservation > payment > notice > MyPage로 주문이 Assigned 되고, 주문 취소가 되면 Status가 deliveryCancelled로 Update 되는 것을 볼 수 있다.
- 또한 Correlation을 Key를 활용하여 Id를 Key값을 하고 원하는 주문하고 서비스간의 공유가 이루어 졌다.
- 이 결과로 서로 다른 마이크로 서비스 간에 트랜잭션이 묶여 있음을 알 수 있다.

**Req/Resp**
- musical 마이크로서비스의 잔여좌석수를 초과한 예약 시도시에는, reservation 마이크로서비스에서 예약이 되지 않도록 처리함
- FeignClient 를 이용한 Req/Resp 연동
- 테스트 시나리오의 2.1, 2.2, 2.3 항목에 해당하며, 동기호출 결과는 3.1(예약성공시)과 5.1(예약실패시)에서 확인할 수 있다.


![image](https://user-images.githubusercontent.com/84003381/122410244-b574d200-cfbe-11eb-8b49-3dad0dafe79b.png)

### <구현기능 점검을 위한 테스트 시나리오>

### 1. MD가 뮤지컬 정보 등록
- http POST http://localhost:8081/musicals musicalId="1" name="Frozen" reservableSeat="100"

![image](https://user-images.githubusercontent.com/84000853/122401028-316b1c00-cfb7-11eb-9f20-32f02f150fc9.png)



### 2. 사용자가 뮤지컬 예약
2.1 정상예약 #1
- http POST http://localhost:8082/reservations musicalId="1" seats="10" price="50000"

2.2 정상예약 #2
- http POST http://localhost:8082/reservations musicalId="1" seats="15" price="50000"

![image](https://user-images.githubusercontent.com/84000853/122401281-6aa38c00-cfb7-11eb-82f1-e86f114466c5.png)

2.3 MD가 관리하는 뮤지컬 정보상의 좌석수(잔여좌석수)를 초과한 예약 시도시에는 예약이 되지 않도록 처리함
- FeignClient를 이용한 Req/Resp 연동
- http POST http://localhost:8082/reservations musicalId="1" seats="200" price="50000"

![image](https://user-images.githubusercontent.com/84000853/122401363-7bec9880-cfb7-11eb-88b6-4fb3febc23f7.png)



### 3. 뮤지컬 예약 후, 각 마이크로 서비스내 Pub/Sub을 통해 변경된 데이터 확인
3.1 뮤지컬 정보 조회 (좌석수량 차감여부 확인)  --> 좌석수가 75로 줄어듦
- http GET http://localhost:8081/musicals/1
![image](https://user-images.githubusercontent.com/84000853/122401410-87d85a80-cfb7-11eb-96a2-a63c95ebba9d.png)
   
3.2 요금결제 내역 조회     --> 2 Row 생성 : Reservation 생성 2건
- http GET http://localhost:8083/payments
![image](https://user-images.githubusercontent.com/84000853/122401517-a50d2900-cfb7-11eb-814f-a8eb7789d8a6.png)

       
3.3 알림 조회              --> 2 Row 생성 : PaymentApproved 생성 2건
- http GET http://localhost:8084/notices
![image](https://user-images.githubusercontent.com/84000853/122401559-af2f2780-cfb7-11eb-903e-faf850510de7.png)

       
3.4 마이페이지 조회        --> 2 Row 생성 : Reservation 생성 2건 후 > PaymentApproved 로 업데이트됨
- http GET http://localhost:8085/myPages
![image](https://user-images.githubusercontent.com/84000853/122401619-bb1ae980-cfb7-11eb-874c-af75fc0fde93.png)



### 4. 사용자가 뮤지컬 예약 취소
4.1 예약번호 #1을 취소함
- http DELETE http://localhost:8082/reservations/1
![image](https://user-images.githubusercontent.com/84000853/122401687-c837d880-cfb7-11eb-983f-7b653ebe25da.png)

   
4.2 취소내역 확인 (#2만 남음)
- http GET http://localhost:8082/reservations
![image](https://user-images.githubusercontent.com/84000853/122401728-d128aa00-cfb7-11eb-9eb1-9b08498328ea.png)



### 5. 뮤지컬 예약 취소 후, 각 마이크로 서비스내 Pub/Sub을 통해 변경된 데이터 확인
5.1 뮤지컬 정보 조회 (좌석수량 증가여부 확인)  --> 좌석수가 85로 늘어남
- http GET http://localhost:8081/musicals/1
![image](https://user-images.githubusercontent.com/84000853/122401785-e1408980-cfb7-11eb-95f9-31487e09c955.png)

5.2 요금결제 내역 조회    --> 1번 예약에 대한 결제건이 paymentCancelled 로 변경됨 (UPDATE)
- http GET http://localhost:8083/payments
![image](https://user-images.githubusercontent.com/84000853/122401809-e69dd400-cfb7-11eb-8216-8fb55d87c36f.png)

5.3 알림 조회             --> 1번 예약에 대한 예약취소건이 paymentCancelled 로 1 row 추가됨 (INSERT)
- http GET http://localhost:8084/notices
![image](https://user-images.githubusercontent.com/84000853/122401844-eef60f00-cfb7-11eb-8303-52bd835137ce.png)

5.4 마이페이지 조회       --> 1 Row 추가 생성 : PaymentCancelled 생성 1건
- http GET http://localhost:8085/myPages
![image](https://user-images.githubusercontent.com/84000853/122401898-f87f7700-cfb7-11eb-86ee-7e5b7ce2d814.png)

       




