# PR #21 AWS RDS 초기화 배포 체크리스트

이 문서는 `minsoo/hotfix` PR #21을 그대로 반영하고, 과제/시연용으로 기존 RDS 데이터를 보존하지 않는 배포 절차를 정리한다. 기존 데이터 보존이 필요하면 이 문서대로 진행하지 말고 별도 SQL 마이그레이션을 작성해야 한다.

## 핵심 결정

- 기존 RDS 데이터는 삭제해도 된다.
- PR #21의 기본 과목 seed는 유지한다.
- 첫 배포 때만 `JPA_DDL_AUTO=update`로 새 스키마를 만들고, 성공 후 반드시 `JPA_DDL_AUTO=validate`로 되돌린다.
- RDS TLS는 최종적으로 `DB_SSL_MODE=VERIFY_IDENTITY`가 목표다. 단, RDS CA truststore 준비 전에는 과제 배포를 위해 임시로 `DB_SSL_MODE=REQUIRED`를 사용할 수 있다.

## 절대 하지 말 것

- 실제 `.env`를 Git에 커밋하지 않는다.
- `JPA_DDL_AUTO=update`를 계속 켜둔 채로 운영하지 않는다.
- RDS 인스턴스를 삭제하지 않는다. 데이터만 비우면 된다.
- 스냅샷 없이 DB를 비우지 않는다.
- `DB_SSL_MODE=VERIFY_IDENTITY`를 켠 상태에서 truststore 없이 계속 재시도하지 않는다. 인증서 검증 실패로 백엔드가 뜨지 않을 수 있다.

## 1. AWS 콘솔에서 RDS 스냅샷 생성

AWS 콘솔이 한글인 경우 기준이다.

1. AWS 콘솔 상단 검색창에 `RDS` 입력 후 `RDS`로 이동한다.
2. 왼쪽 메뉴에서 `데이터베이스`를 클릭한다.
3. 사용 중인 DB 인스턴스, 예: `pokemo-db`, 를 클릭한다.
4. 오른쪽 위 `작업` 버튼을 누른다.
5. `스냅샷 생성`을 클릭한다.
6. `스냅샷 이름`에 아래처럼 입력한다.

```text
pre-pr21-reset-YYYYMMDD
```

7. `스냅샷 생성` 버튼을 누른다.
8. 왼쪽 메뉴 `스냅샷`에서 상태가 `사용 가능`이 될 때까지 기다린다.

## 1-1. CD 자동 배포 여부 확인

이 저장소의 CD는 PR 브랜치가 아니라 `main`에 PR이 머지되고 CI가 성공할 때 EC2에서 자동 배포될 수 있다. 따라서 자동 CD가 켜져 있으면 PR merge 전에 RDS 초기화와 EC2 `.env`의 `JPA_DDL_AUTO=update` 준비를 먼저 끝내야 한다.

GitHub에서 확인한다.

1. GitHub 저장소로 이동한다.
2. 상단 `Actions` 탭을 클릭한다.
3. 왼쪽에서 `CD` workflow를 클릭한다.
4. 최근 실행 기록이 있고, repository secrets에 `EC2_HOST`, `EC2_USER`, `EC2_SSH_KEY`, `EC2_PROJECT_DIR`가 설정되어 있으면 자동 배포가 동작한다고 봐야 한다.

권장 순서는 아래 둘 중 하나다.

- 자동 CD 사용: RDS 스냅샷 생성, DB 비우기, EC2 `.env`를 `JPA_DDL_AUTO=update`로 바꾼 뒤 PR을 merge한다. CD는 `origin/main`으로 배포한다.
- 수동 배포 사용: PR을 아직 merge하지 않고 EC2에서 `origin/minsoo/hotfix`를 직접 받아 배포한 뒤, 성공하면 PR을 merge한다. 이 경우 GitHub CD가 아니라 EC2 터미널에서 직접 배포한다.

자동 CD가 있는 상태에서 아무 준비 없이 PR을 merge하면, CD가 `JPA_DDL_AUTO=validate` 상태로 먼저 실행되어 백엔드가 DB 스키마 검증 실패로 내려갈 수 있다.

## 2. RDS 엔드포인트와 보안 그룹 확인

1. `RDS` > `데이터베이스` > DB 인스턴스를 클릭한다.
2. `연결 및 보안` 탭을 연다.
3. `엔드포인트` 값을 복사한다. 이 값이 `.env`의 `DB_HOST`다.
4. 같은 화면의 `포트`가 `3306`인지 확인한다.
5. `VPC 보안 그룹` 링크를 클릭한다.
6. EC2 콘솔의 보안 그룹 화면으로 이동하면 `인바운드 규칙` 탭을 연다.
7. `인바운드 규칙 편집` 버튼을 누른다.
8. `규칙 추가`를 누르고 아래처럼 설정한다.

| 항목 | 값 |
| --- | --- |
| 유형 | `MYSQL/Aurora` |
| 프로토콜 | `TCP` |
| 포트 범위 | `3306` |
| 소스 | EC2 인스턴스의 보안 그룹 ID, 예: `sg-xxxxxxxx` |

9. `규칙 저장`을 누른다.

RDS 보안 그룹의 소스를 `0.0.0.0/0`으로 열지 않는다. 반드시 백엔드가 실행되는 EC2 보안 그룹만 허용한다.

## 3. EC2 접속

로컬 터미널에서 EC2에 접속한다. 사용자 이름은 인스턴스 AMI에 따라 보통 `ubuntu` 또는 `ec2-user`다.

```bash
ssh -i /path/to/key.pem ubuntu@EC2_PUBLIC_IP
```

프로젝트 디렉터리로 이동한다.

```bash
cd /home/ubuntu/pokemo
```

현재 브랜치와 원격 상태를 확인한다.

```bash
git status
git branch --show-current
```

## 4. PR #21 반영 방식 선택

자동 CD를 쓰지 않거나, 이미 RDS 초기화와 `.env` 준비가 끝난 경우에는 GitHub에서 PR #21을 merge한 뒤 EC2에서 아래 명령을 실행한다.

```bash
git fetch origin main
git reset --hard origin/main
```

자동 CD 때문에 merge 순서가 불안하거나, main merge 전 먼저 시연해야 한다면 PR 브랜치로 바로 배포한다.

```bash
git fetch origin minsoo/hotfix
git switch -C deploy-pr21 origin/minsoo/hotfix
```

## 5. RDS DB 비우기

DB 인스턴스를 삭제하지 말고, 애플리케이션 DB만 삭제 후 다시 만든다. 아래 예시는 DB 이름이 `pokemo`인 경우다.

먼저 백엔드를 내려서 DB 연결을 끊는다.

```bash
docker compose -f docker-compose.prod.yml --env-file .env down
```

DB 접속 정보를 환경변수로 입력한다. 비밀번호는 화면에 남기지 않도록 `read -s`로 입력한다.

```bash
export DB_HOST="your-rds-endpoint.ap-northeast-2.rds.amazonaws.com"
export DB_PORT="3306"
export DB_NAME="pokemo"
export DB_USERNAME="pokemo"
read -s DB_PASSWORD
```

Docker의 MySQL 클라이언트로 RDS에 접속해 DB를 재생성한다.

```bash
docker run --rm mysql:8.4 mysql \
  -h "$DB_HOST" \
  -P "$DB_PORT" \
  --ssl-mode=REQUIRED \
  -u "$DB_USERNAME" \
  -p"$DB_PASSWORD" \
  -e "DROP DATABASE IF EXISTS \`$DB_NAME\`; CREATE DATABASE \`$DB_NAME\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

성공하면 출력이 거의 없을 수 있다. 실패하면 에러 메시지를 보고 아래를 확인한다.

- RDS 보안 그룹이 EC2 보안 그룹을 허용하는지
- `DB_HOST`, `DB_USERNAME`, `DB_PASSWORD`가 맞는지
- RDS가 `사용 가능` 상태인지
- DB 사용자에게 `DROP DATABASE`와 `CREATE DATABASE` 권한이 있는지. RDS 마스터 사용자면 보통 가능하다.

## 6. EC2의 `.env` 수정

EC2 프로젝트 루트의 `.env`를 수정한다.

```bash
nano .env
```

최초 PR #21 배포 때는 아래 값을 사용한다.

```env
DB_HOST=your-rds-endpoint.ap-northeast-2.rds.amazonaws.com
DB_PORT=3306
DB_NAME=pokemo
DB_USERNAME=pokemo
DB_PASSWORD=실제_RDS_비밀번호
JPA_DDL_AUTO=update
JWT_SECRET=32자_이상의_긴_랜덤_문자열
CORS_ALLOWED_ORIGINS=https://실제_프론트_도메인
```

현재 `docker-compose.prod.yml`의 프론트엔드는 호스트의 `127.0.0.1:8080`에만 바인딩된다. 즉 외부 브라우저는 보통 Caddy/Nginx/HTTPS 도메인으로 접속해야 한다. 아직 도메인과 프록시가 없다면 `CORS_ALLOWED_ORIGINS`는 실제 브라우저 주소와 반드시 일치시켜야 하며, 프론트를 외부에 직접 열지 여부는 별도로 결정해야 한다.

### 빠른 과제 배포 경로

RDS CA truststore 준비가 아직 안 됐으면 임시로 아래 값을 사용한다.

```env
DB_SSL_MODE=REQUIRED
DB_SSL_PARAMS=
```

이 설정은 전송 암호화는 하지만 서버 인증서 신원 검증은 하지 않는다. 과제 시연을 빨리 끝내기 위한 임시값으로만 사용한다.

### 보안 강화 경로

`VERIFY_IDENTITY`까지 적용하려면 먼저 EC2에서 truststore를 만든다.

```bash
mkdir -p certs
curl -o certs/ap-northeast-2-bundle.pem https://truststore.pki.rds.amazonaws.com/ap-northeast-2/ap-northeast-2-bundle.pem
keytool -importcert -noprompt -alias rds-ca \
  -file certs/ap-northeast-2-bundle.pem \
  -keystore certs/rds-truststore.jks \
  -storepass changeit
```

그 다음 `docker-compose.prod.yml`의 backend `volumes`에서 아래 주석을 해제한다.

```yaml
- ./certs:/app/certs:ro
```

그리고 `.env`에 아래 값을 둔다.

```env
DB_SSL_MODE=VERIFY_IDENTITY
DB_SSL_PARAMS=&trustCertificateKeyStoreUrl=file:/app/certs/rds-truststore.jks&trustCertificateKeyStorePassword=changeit
```

## 7. Compose 설정 검증

실행 전에 compose 설정이 깨지지 않았는지 확인한다.

```bash
docker compose -f docker-compose.prod.yml --env-file .env config >/tmp/pokemo-compose.yml
```

에러가 없으면 다음 단계로 간다.

## 8. 최초 배포 실행

```bash
docker compose -f docker-compose.prod.yml --env-file .env up -d --build
```

컨테이너 상태를 확인한다.

```bash
docker compose -f docker-compose.prod.yml ps
```

백엔드 로그를 확인한다.

```bash
docker compose -f docker-compose.prod.yml logs -f backend
```

정상이라면 로그에 테이블 생성 후 애플리케이션 기동이 완료되고, health check가 성공해야 한다. 컨테이너 내부/EC2 로컬 기준의 필수 확인은 `/api/health`다.

```bash
curl http://localhost:8080/api/health
```

`/actuator/health`는 백엔드 로컬에서는 확인할 수 있지만, 운영 도메인이나 프록시 경유에서는 설정에 따라 막히거나 502가 날 수 있다. 시연 필수 기준은 `https://도메인/api/health`와 실제 화면 동작으로 둔다.

## 9. `JPA_DDL_AUTO`를 validate로 되돌리기

최초 기동이 성공하면 즉시 `.env`를 다시 연다.

```bash
nano .env
```

아래처럼 바꾼다.

```env
JPA_DDL_AUTO=validate
```

백엔드만 재배포한다.

```bash
docker compose -f docker-compose.prod.yml --env-file .env up -d --build backend
```

다시 확인한다.

```bash
docker compose -f docker-compose.prod.yml ps
curl http://localhost:8080/api/health
```

## 10. 브라우저 시연 확인

브라우저에서 서비스 주소로 접속해 아래를 확인한다.

1. 새 계정 회원가입
2. 이메일 인증 코드 확인. SMTP가 꺼져 있으면 backend 로그에 코드가 나온다.
3. 로그인
4. 기본 과목 5개가 보이는지 확인
5. 과목 추가/수정/삭제
6. 노트 작성
7. 일정 추가
8. 퀴즈/오답/AI 기능 확인

SMTP 인증 코드는 아래 명령으로 backend 로그를 보면 된다.

```bash
docker compose -f docker-compose.prod.yml logs --tail=200 backend
```

## 11. 자주 나는 문제

### 백엔드가 DB 연결 실패로 죽는 경우

- RDS 보안 그룹 인바운드에 EC2 보안 그룹이 들어갔는지 확인한다.
- `.env`의 `DB_HOST`가 RDS endpoint인지 확인한다.
- `DB_SSL_MODE=VERIFY_IDENTITY`인데 truststore를 만들지 않았다면 일단 `DB_SSL_MODE=REQUIRED`, `DB_SSL_PARAMS=`로 바꿔서 재기동한다.

### 테이블 검증 실패로 죽는 경우

- `.env`가 아직 `JPA_DDL_AUTO=validate`인지 확인한다.
- 최초 빈 DB 생성 단계에서는 `JPA_DDL_AUTO=update`로 1회 기동해야 한다.
- 성공 후 다시 `validate`로 되돌린다.

### 프론트에서 API가 안 되는 경우

- `CORS_ALLOWED_ORIGINS`가 실제 프론트 주소와 일치하는지 확인한다.
- EC2 또는 리버스 프록시가 `/api`를 백엔드로 전달하는지 확인한다.
- 운영 도메인에서 확인할 때는 `https://도메인/api/health`를 먼저 본다. `http://localhost/api/health`는 EC2에 Caddy/Nginx가 80번 포트에서 돌 때만 맞다.
- 컨테이너 상태와 로그를 확인한다.

```bash
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs --tail=120 backend
docker compose -f docker-compose.prod.yml logs --tail=80 frontend
```

## 최종 완료 조건

- RDS 스냅샷이 있다.
- DB가 새 스키마로 생성됐다.
- `.env`의 `JPA_DDL_AUTO=validate`로 복귀했다.
- `curl http://localhost:8080/api/health`가 성공한다.
- 새 회원가입 후 기본 과목이 보인다.
- 과목/노트/일정/퀴즈 핵심 시나리오가 브라우저에서 동작한다.
