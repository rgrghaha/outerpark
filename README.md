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


### GateWay 적용
API GateWay를 통하여 마이크로 서비스들의 집입점을 통일할 수 있다. 다음과 같이 GateWay를 적용하였다.

```
 소스: gateway 설정된 내용  
      
```
8080 port로 musical 서비스 정상 호출
```
 캡쳐 : gateway를 통한 서비스 호출   
      
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



# 여기까지 작업함

예약을 하게되면  reservation > payment > notice > MyPage로 주문이 Assigned 되고

주문 취소가 되면 Status가 deliveryCancelled로 Update 되는 것을 볼 수 있다.

또한 Correlation을 Key를 활용하여 Id를 Key값을 하고 원하는 주문하고 서비스간의 공유가 이루어 졌다.

위 결과로 서로 다른 마이크로 서비스 간에 트랜잭션이 묶여 있음을 알 수 있다.

# 폴리글랏
Order 서비스의 DB와 MyPage의 DB를 다른 DB를 사용하여 폴리글랏을 만족시키고 있다.

**Order의 pom.xml DB 설정 코드**

![증빙5](https://github.com/bigot93/forthcafe/blob/main/images/db_conf1.png)

**MyPage의 pom.xml DB 설정 코드**

![증빙6](https://github.com/bigot93/forthcafe/blob/main/images/db_conf2.png)

# 동기식 호출 과 Fallback 처리

분석단계에서의 조건 중 하나로 결재(Pay)와 배송(Delivery) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 호출 프로토콜은 Rest Repository에 의해 노출되어있는 REST 서비스를 FeignClient를 이용하여 호출하도록 한다.

**Pay 서비스 내 external.DeliveryService**
```java
package forthcafe.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@FeignClient(name="Delivery", url="${api.url.delivery}") 
public interface DeliveryService {

    @RequestMapping(method = RequestMethod.POST, path = "/deliveries", consumes = "application/json")
    public void delivery(@RequestBody Delivery delivery);

}
```

**동작 확인**

잠시 Delivery 서비스 중지
![증빙7](https://github.com/bigot93/forthcafe/blob/main/images/%EB%8F%99%EA%B8%B0%ED%99%941.png)

주문 취소 요청시 Pay 서비스 변화 없음
![증빙8](https://github.com/bigot93/forthcafe/blob/main/images/%EB%8F%99%EA%B8%B0%ED%99%942.png)

Delivery 서비스 재기동 후 주문취소
![증빙9](https://github.com/bigot93/forthcafe/blob/main/images/%EB%8F%99%EA%B8%B0%ED%99%943.png)

Pay 서비스 상태를 보면 2번 주문 정상 취소 처리됨
![증빙9](https://github.com/bigot93/forthcafe/blob/main/images/%EB%8F%99%EA%B8%B0%ED%99%944.png)

Fallback 설정
![image](https://user-images.githubusercontent.com/5147735/109755775-f9b7ae80-7c29-11eb-8add-bdb295dc94e1.png)
![image](https://user-images.githubusercontent.com/5147735/109755797-04724380-7c2a-11eb-8fcd-1c5135000ee5.png)


Fallback 결과(Pay service 종료 후 Order 추가 시)
![image](https://user-images.githubusercontent.com/5147735/109755716-dab91c80-7c29-11eb-9099-ba585115a2a6.png)

# 운영

## CI/CD
* 카프카 설치
```
- 헬름 설치
참고 : http://msaschool.io/operation/implementation/implementation-seven/
curl https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3 > get_helm.sh
chmod 700 get_helm.sh
./get_helm.sh

- Azure Only
kubectl patch storageclass managed -p '{"metadata": {"annotations":{"storageclass.kubernetes.io/is-default-class":"true"}}}'

- 카프카 설치
kubectl --namespace kube-system create sa tiller      # helm 의 설치관리자를 위한 시스템 사용자 생성
kubectl create clusterrolebinding tiller --clusterrole cluster-admin --serviceaccount=kube-system:tiller

helm repo add incubator https://charts.helm.sh/incubator
helm repo update
kubectl create ns kafka
helm install my-kafka --namespace kafka incubator/kafka

kubectl get po -n kafka -o wide
```
* Topic 생성
```
kubectl -n kafka exec my-kafka-0 -- /usr/bin/kafka-topics --zookeeper my-kafka-zookeeper:2181 --topic forthcafe --create --partitions 1 --replication-factor 1
```
* Topic 확인
```
kubectl -n kafka exec my-kafka-0 -- /usr/bin/kafka-topics --zookeeper my-kafka-zookeeper:2181 --list
```
* 이벤트 발행하기
```
kubectl -n kafka exec -ti my-kafka-0 -- /usr/bin/kafka-console-producer --broker-list my-kafka:9092 --topic forthcafe
```
* 이벤트 수신하기
```
kubectl -n kafka exec -ti my-kafka-0 -- /usr/bin/kafka-console-consumer --bootstrap-server my-kafka:9092 --topic forthcafe --from-beginning
```

* 소스 가져오기
```
git clone https://github.com/bigot93/forthcafe.git
```

## ConfigMap
* deployment.yml 파일에 설정
```
env:
   - name: SYS_MODE
     valueFrom:
       configMapKeyRef:
         name: systemmode
         key: sysmode
```
* Configmap 생성, 정보 확인
```
kubectl create configmap systemmode --from-literal=sysmode=PRODUCT
kubectl get configmap systemmode -o yaml
```
![image](https://user-images.githubusercontent.com/5147735/109768817-bb77ba80-7c3c-11eb-8856-7fca5213f5b1.png)

* order 1건 추가후 로그 확인
```
kubectl logs {pod ID}
```
![image](https://user-images.githubusercontent.com/5147735/109760887-dc3b1280-7c32-11eb-8284-f4544d7b72b0.png)


## Deploy / Pipeline

* build 하기
```
cd /forthcafe

cd Order
mvn package 

cd ..
cd Pay
mvn package

cd ..
cd Delivery
mvn package

cd ..
cd gateway
mvn package

cd ..
cd MyPage
mvn package
```

* Azure 레지스트리에 도커 이미지 push, deploy, 서비스생성(방법1 : yml파일 이용한 deploy)
```
cd .. 
cd Order
az acr build --registry skteam01 --image skteam01.azurecr.io/order:v1 .
kubectl apply -f kubernetes/deployment.yml 
kubectl expose deploy order --type=ClusterIP --port=8080

cd .. 
cd Pay
az acr build --registry skteam01 --image skteam01.azurecr.io/pay:v1 .
kubectl apply -f kubernetes/deployment.yml 
kubectl expose deploy pay --type=ClusterIP --port=8080

cd .. 
cd Delivery
az acr build --registry skteam01 --image skteam01.azurecr.io/delivery:v1 .
kubectl apply -f kubernetes/deployment.yml 
kubectl expose deploy delivery --type=ClusterIP --port=8080


cd .. 
cd MyPage
az acr build --registry skteam01 --image skteam01.azurecr.io/mypage:v1 .
kubectl apply -f kubernetes/deployment.yml 
kubectl expose deploy mypage --type=ClusterIP --port=8080

cd .. 
cd gateway
az acr build --registry skteam01 --image skteam01.azurecr.io/gateway:v1 .
kubectl create deploy gateway --image=skteam01.azurecr.io/gateway:v1
kubectl expose deploy gateway --type=LoadBalancer --port=8080
```


* Azure 레지스트리에 도커 이미지 push, deploy, 서비스생성(방법2)
```
cd ..
cd Order
az acr build --registry skteam01 --image skteam01.azurecr.io/order:v1 .
kubectl create deploy order --image=skteam01.azurecr.io/order:v1
kubectl expose deploy order --type=ClusterIP --port=8080

cd .. 
cd Pay
az acr build --registry skteam01 --image skteam01.azurecr.io/pay:v1 .
kubectl create deploy pay --image=skteam01.azurecr.io/pay:v1
kubectl expose deploy pay --type=ClusterIP --port=8080


cd .. 
cd Delivery
az acr build --registry skteam01 --image skteam01.azurecr.io/delivery:v1 .
kubectl create deploy delivery --image=skteam01.azurecr.io/delivery:v1
kubectl expose deploy delivery --type=ClusterIP --port=8080


cd .. 
cd gateway
az acr build --registry skteam01 --image skteam01.azurecr.io/gateway:v1 .
kubectl create deploy gateway --image=skteam01.azurecr.io/gateway:v1
kubectl expose deploy gateway --type=LoadBalancer --port=8080

cd .. 
cd MyPage
az acr build --registry skteam01 --image skteam01.azurecr.io/mypage:v1 .
kubectl create deploy mypage --image=skteam01.azurecr.io/mypage:v1
kubectl expose deploy mypage --type=ClusterIP --port=8080

kubectl logs {pod명}
```
* Service, Pod, Deploy 상태 확인
![image](https://user-images.githubusercontent.com/5147735/109769165-2de89a80-7c3d-11eb-8472-2281468fb771.png)


* deployment.yml  참고
```
1. image 설정
2. env 설정 (config Map) 
3. readiness 설정 (무정지 배포)
4. liveness 설정 (self-healing)
5. resource 설정 (autoscaling)
```

![image](https://user-images.githubusercontent.com/5147735/109643506-a8f77580-7b97-11eb-926b-e6c922aa2d1b.png)

## 서킷 브레이킹
* 서킷 브레이킹 프레임워크의 선택: Spring FeignClient + Hystrix 옵션을 사용하여 구현함
* Order -> Pay 와의 Req/Res 연결에서 요청이 과도한 경우 CirCuit Breaker 통한 격리
* Hystrix 를 설정: 요청처리 쓰레드에서 처리시간이 610 밀리가 넘어서기 시작하여 어느정도 유지되면 CB 회로가 닫히도록 (요청을 빠르게 실패처리, 차단) 설정

```
// Order서비스 application.yml

feign:
  hystrix:
    enabled: true

hystrix:
  command:
    default:
      execution.isolation.thread.timeoutInMilliseconds: 610
```


```
// Pay 서비스 Pay.java

 @PostPersist
    public void onPostPersist(){
        Payed payed = new Payed();
        BeanUtils.copyProperties(this, payed);
        payed.setStatus("Pay");
        payed.publishAfterCommit();

        try {
                 Thread.currentThread().sleep((long) (400 + Math.random() * 220));
         } catch (InterruptedException e) {
                 e.printStackTrace();
         }
```

* /home/project/team/forthcafe/yaml/siege.yaml
```
apiVersion: v1
kind: Pod
metadata:
  name: siege
spec:
  containers:
  - name: siege
    image: apexacme/siege-nginx
```

* siege pod 생성
```
/home/project/team/forthcafe/yaml/kubectl apply -f siege.yaml
```

* 부하테스터 siege 툴을 통한 서킷 브레이커 동작 확인: 동시사용자 100명 60초 동안 실시
```
kubectl exec -it pod/siege -c siege -- /bin/bash
siege -c100 -t60S  -v --content-type "application/json" 'http://{EXTERNAL-IP}:8080/orders POST {"memuId":2, "quantity":1}'
siege -c100 -t30S  -v --content-type "application/json" 'http://52.141.61.164:8080/orders POST {"memuId":2, "quantity":1}'
```
![image](https://user-images.githubusercontent.com/5147735/109762408-dd207400-7c33-11eb-8464-325d781867ae.png)
![image](https://user-images.githubusercontent.com/5147735/109762376-d1cd4880-7c33-11eb-87fb-b739aa2d6621.png)



## 오토스케일 아웃
* 앞서 서킷 브레이커(CB) 는 시스템을 안정되게 운영할 수 있게 해줬지만 사용자의 요청을 100% 받아들여주지 못했기 때문에 이에 대한 보완책으로 자동화된 확장 기능을 적용하고자 한다.

* order 서비스 deployment.yml 설정
```
 resources:
            limits:
              cpu: 500m
            requests:
              cpu: 200m
```
* 다시 배포해준다.
```
/home/project/team/forthcafe/Order/mvn package
az acr build --registry skteam01 --image skteam01.azurecr.io/order:v1 .
kubectl apply -f kubernetes/deployment.yml 
kubectl expose deploy order --type=ClusterIP --port=8080
```

* Order 서비스에 대한 replica 를 동적으로 늘려주도록 HPA 를 설정한다. 설정은 CPU 사용량이 15프로를 넘어서면 replica 를 10개까지 늘려준다

```
kubectl autoscale deploy order --min=1 --max=10 --cpu-percent=15
```

* /home/project/team/forthcafe/yaml/siege.yaml
```
apiVersion: v1
kind: Pod
metadata:
  name: siege
spec:
  containers:
  - name: siege
    image: apexacme/siege-nginx
```

* siege pod 생성
```
/home/project/team/forthcafe/yaml/kubectl apply -f siege.yaml
```

* siege를 활용해서 워크로드를 1000명, 1분간 걸어준다. (Cloud 내 siege pod에서 부하줄 것)
```
kubectl exec -it pod/siege -c siege -- /bin/bash
siege -c1000 -t60S  -v --content-type "application/json" 'http://{EXTERNAL-IP}:8080/orders POST {"memuId":2, "quantity":1}'
siege -c1000 -t60S  -v --content-type "application/json" 'http://52.141.61.164:8080/orders POST {"memuId":2, "quantity":1}'
```

* 오토스케일이 어떻게 되고 있는지 모니터링을 걸어둔다
```
kubectl get deploy order -w
```
![image](https://user-images.githubusercontent.com/5147735/109771563-4c9c6080-7c40-11eb-9bf8-1efef17bedee.png)
```
kubectl get pod
```
![image](https://user-images.githubusercontent.com/5147735/109771259-f3ccc800-7c3f-11eb-8ebe-9ff4ab9c2242.png)




## 무정지 재배포 (Readiness Probe)
* 배포전

![image](https://user-images.githubusercontent.com/5147735/109743733-89526280-7c14-11eb-93da-0ddd3cd18e22.png)

* 배포중

![image](https://user-images.githubusercontent.com/5147735/109744076-11386c80-7c15-11eb-849d-6cf4e2c72675.png)
![image](https://user-images.githubusercontent.com/5147735/109744186-3a58fd00-7c15-11eb-8da3-f11b6194fc6b.png)

* 배포후

![image](https://user-images.githubusercontent.com/5147735/109744225-45139200-7c15-11eb-8efa-07ac40162ded.png)




## Self-healing (Liveness Probe)
* order 서비스 deployment.yml   livenessProbe 설정을 port 8089로 변경 후 배포 하여 liveness probe 가 동작함을 확인 
```
    livenessProbe:
      httpGet:
        path: '/actuator/health'
        port: 8089
      initialDelaySeconds: 5
      periodSeconds: 5
```

![image](https://user-images.githubusercontent.com/5147735/109740864-4fcb2880-7c0f-11eb-86ad-2aabb0197881.png)
![image](https://user-images.githubusercontent.com/5147735/109742082-c0734480-7c11-11eb-9a57-f6dd6961a6d2.png)



