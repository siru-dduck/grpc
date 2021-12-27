# Remote Procedure Call
RPC는 원격에 존재하는 프로시저(함수)를 로컬에서 함수(메소드)를 호출하듯이 호출할 수 있는 방법이다.

원격에서 서로다른 시스템, 프로세스간의 규격이 다르기 때문에 미리 정의된 Stub을 정의해서 데이터/호출규약을 정해야한다.



gRPC뿐만 아니라 CORBA - IIOP, Java RMI, XML-RPC와 같은 rpc구현체가 있다.

하지만

높은 복잡도
급격한 학습곡선
낮은 개발 생산성
으로 인해 잘쓰이지 않았다...

하지만 분산환경에서 RPC의 필요성은 여전하다.

MSA의 유행으로 주목받고있다.

---

# gRPC
RPC 구현체
오픈소스 BSD 라이센스
다양한 언어 지원


## 장점 => 가볍다. 
- Protobuf
    - 바이너리 프로토콜 사용 (protocol buffers 3)
    - text보다 더 적은 메모리 공간 사용
    - cpu 효율이 좋음
- HTTP/2
    - 헤더압축
    - 양방향 스트리밍
    - 멀티플렉싱
- JSON, XML등으로 변환가능


## 단점
- 브라우저에서 grpc 사용불가
- 데이터가 binary이기 때문에 사람이 읽기 어렵다.

---

# gRPC vs REST API

- gRPC는 REST API보다 초당 처리량이 3배이다.
- cpu사용률 고려 시 처리량이 11배이다.
- proto파일 하나만 작성하면 다양한 언어에서 rpc작성이 가능하다.

---

# gRPC 4가지 스트리밍 방식
- Unary 단반향
- 서버 스트리밍
- 클라이언트 스트리밍
- 양방향 스트리밍

---

# gRPC 3가지 Stub
- Blocking Stub
- (Async) Stub → 4가지 스트리밍 방식 모두 지원
- Future Stub