## OuterPark(뮤지컬 예약)

# 서비스 시나리오

기능적 요구사항
1. 공연관리자가 예약가능한 뮤지컬과 좌석수를 등록한다.
1. 고객이 뮤지컬 좌석을 예약한다.
1. 뮤지컬이 예약되면 예약된 좌석수 만큼 예약가능한 좌석수에서 차감된다.
1. 예약이 되면 결제정보를 승인한다.
1. 결제정보가 승인되면 알림메시지를 발송한다. 
1. 고객이 뮤지컬 예약을 취소할 수 있다. 
1. 예약이 취소되면 결제가 취소된다.
1. 결제가 최소되면 알림메시지를 발송한다.
1. 고객이 모든 진행내역을 볼 수 있어야 한다.

비기능적 요구사항
1. 트랜잭션
    1. 예약 가능한 좌석이 부족하면 예약이 되지 않는다.> Sync 호출
    1. 예약이 취소되면 결제가 취소되고 예약가능한 좌석수가 증가한다.> SAGA, 보상 트랜젝션
1. 장애격리
    1. 결제가 완료되지 않아도 예약은 365일 24시간 받을 수 있어야 한다.> Async (event-driven), Eventual Consistency
    1. 결제시스템이 과중되면 주문을 잠시동안 받지 않고 결제를 잠시후에 하도록 유도한다> Circuit breaker, fallback
1. 성능
    1. 고객이 모든 진행내역을 조회 할 수 있도록 성능을 고려하여 별도의 view로 구성한다.> CQRS


# 체크포인트

1. Saga
1. CQRS
1. Correlation
1. Req/Resp
1. Gateway
1. Deploy/ Pipeline
1. Circuit Breaker
1. Autoscale (HPA)
1. Zero-downtime deploy (Readiness Probe)
1. Config Map/ Persistence Volume
1. Polyglot
1. Self-healing (Liveness Probe)


## EventStorming 결과
### 완성된 1차 모형 

![BF4A2D4F-B086-4968-8496-274ED44492BB](https://user-images.githubusercontent.com/82069747/122213616-1aa7c500-cee4-11eb-9df6-952bb76e1e1b.jpeg)

### 1차 완성본에 대한 기능적/비기능적 요구사항을 커버하는지 검증  
![0FCC7390-6077-42FD-92DC-D1056718EDA1](https://user-images.githubusercontent.com/82069747/122057599-c9390080-ce25-11eb-9f66-df2f17b4588c.jpeg)

- 고객이 뮤지컬을 예약한다. (1, ok)
- 예약하려는 좌석수가 예약가능한 좌석수보다 크면 예약이 되지 않는다. 
- 뮤지컬을 예약하면 payment가 승인된다. (2 -> 3, ok)
- payment가 승인되면 알림메시지가 발송된다. (3 -> 4, ok)
- 고객이 뮤지컬 예약을 취소하면 payment가 취소된다. (5 -> 6, ok)
- 고객은 MyPage를 통해 뮤지컬 상태를 확인 할 수 있다. (7, ok)



