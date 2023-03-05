# placesearch

### HTTP Request file
requests 폴더 참고
- - -
### Library
#### webflux
시스템을 비동기로만들기 위해 사용했습니다.

#### reactor
Reactive 방식을 쓰기 위해 사용했습니다.

#### r2dbc
기존 Spring JPA는 비동기처리를 지원하지 않기 때문에 사용했습니다.

#### caffeine
캐시 지원 목적으로 사용했습니다.

#### resilience4j
Circuit breaker 지원 목적으로 사용했습니다.
- - -
### 부연설명
1. 유지보수 및 확장에 용이한 아키텍처 설계
   * Clean architecture의 내용을 따르고자 했습니다.
   * controller, service, domain으로 layer를 나누고 layer간의 의존성이 한 방향으로만 흐르도록 만들었습니다.
   * 의존성 역전을 이용했습니다
2. 동시성 이슈 방지
   * Optimistic locking 적용
3. 장애 및 연동 오류
   * webflux, 비동기 처리의 특성 상 쓰레드풀이 소진되는 것을 방지합니다. 이로 인해 API의 응답지연으로 인한 장애 발생을 막을 수 있습니다.
   * 기준치 이상으로 오류 발생 시 circuit breaker를 통해서 메세지로 알리고 snapshot이 대신 보이도록 했습니다.
   * 다만 메세지 전송과 스냅샷은 미구현 상태입니다. 
4. Low latency, Scalability, Availability
   * code에선 드러나지 않지만 로드밸런서를 통한 다중화와 DB 다중화를 고려해봐야 할 것입니다.
   * 빠른 개발을 위해서 익숙한 RDBMS 구조의 H2를 사용했으나 join이 필요 없는 데이터이므로 Scalability를 위해 NoSQL을 고려해보는 것도 좋을 것 같습니다.
   * cache를 통해 latency를 낮추고자 했습니다.
   * Circuit breaker, 비동기처리의 적용으로 장애 상황을 방지하고 상황을 빨리 알아차릴 수 있도록 했습니다.
   * 이 밖의 metric이나 로그레벨에 따른 알림 등을 적용할 수 있으나 시간 관계상 구현하지 못했습니다.
5. 새로운 검색 API 추가 시 변경 영역 최소화
   * PlaceSerchApi 인터페이스를 구현하고 bean으로 등록하기만 하면 바로 추가될 수 있도록 구현했습니다.
