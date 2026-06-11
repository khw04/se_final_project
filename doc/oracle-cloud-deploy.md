# Oracle Cloud 배포 가이드 (VM 전체 스택)

OCI Compute VM 한 대에 `docker compose`로 **MySQL + 백엔드 + 프론트엔드** 전체 스택을 올리는 절차입니다.
nginx가 `/api/`·`/actuator/`를 백엔드로 프록시하므로 외부에는 **프론트엔드 포트(80) 하나만** 열면 됩니다.

---

## 0. 사전 준비 (로컬)

이 브랜치(`minsoo/db`)의 변경을 원격에 올려야 VM에서 `git clone`/`git pull`로 받을 수 있습니다.

```powershell
git push origin minsoo/db
```

> 푸시 대신 코드를 직접 전송하려면 4단계의 `scp` 대안을 참고하세요.

---

## 1. OCI Compute 인스턴스 생성 (웹 콘솔)

OCI 콘솔 → **Compute → Instances → Create instance**

| 항목 | 값 |
|------|-----|
| Image | **Canonical Ubuntu 22.04** |
| Shape | **VM.Standard.A1.Flex (Ampere/ARM)** · 2 OCPU · 12 GB — Always Free 범위 |
| SSH key | 아래에서 만든 공개키 업로드 |
| Networking | 새 VCN 자동 생성, **Assign public IPv4 address** 체크 |

> ⚠️ AMD Micro(`E2.1.Micro`, RAM 1GB)는 Maven/Vite 빌드 중 OOM이 납니다. **반드시 Ampere A1**을 쓰세요. 모든 베이스 이미지는 arm64를 지원합니다.

### SSH 키 생성 (로컬, 없으면)

```powershell
ssh-keygen -t ed25519 -f $env:USERPROFILE\.ssh\oci_pokemo -C "pokemo-deploy"
```

생성된 `oci_pokemo.pub` 내용을 인스턴스 생성 화면의 SSH 키에 붙여넣습니다.

---

## 2. 네트워크 포트 개방 (가장 흔한 함정 ⚠️)

OCI는 **방화벽이 2겹**입니다. 둘 다 열어야 외부 접속이 됩니다.

### (a) VCN Security List

VCN → Subnet → Security List → **Add Ingress Rules**

| Source CIDR | Protocol | Dest Port |
|-------------|----------|-----------|
| 0.0.0.0/0 | TCP | 22 |
| 0.0.0.0/0 | TCP | 80 |

### (b) 인스턴스 내부 iptables (Ubuntu 기본값이 막고 있음)

SSH 접속 후:

```bash
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 80 -j ACCEPT
sudo netfilter-persistent save
```

---

## 3. VM 접속 & Docker 설치

```powershell
ssh -i $env:USERPROFILE\.ssh\oci_pokemo ubuntu@<VM_PUBLIC_IP>
```

VM 안에서:

```bash
# Docker Engine + Compose 플러그인 설치
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER
newgrp docker   # 또는 재로그인
docker compose version   # 확인
```

---

## 4. 코드 가져오기

```bash
git clone -b minsoo/db <레포_URL> pokemo
cd pokemo
```

> **scp 대안 (푸시 없이):** 로컬 PowerShell에서
> ```powershell
> scp -i $env:USERPROFILE\.ssh\oci_pokemo -r `
>   "C:\Users\XXX\Documents\GitHub\se_final_project" `
>   ubuntu@<VM_PUBLIC_IP>:~/pokemo
> ```
> (`node_modules`, `target` 등이 같이 가면 느리니 가급적 git 권장)

---

## 5. 운영 `.env` 작성

```bash
cp .env.example .env
nano .env
```

최소 다음 값을 실제로 채웁니다:

```env
COMPOSE_PROJECT_NAME=pokemo

DB_NAME=pokemo
DB_USERNAME=pokemo
DB_PASSWORD=<강력한_비밀번호>
MYSQL_ROOT_PASSWORD=<강력한_root_비밀번호>

JWT_SECRET=<32바이트 이상 랜덤 문자열>

GEMINI_API_KEY=<실제_키>
GEMINI_MODEL=gemini-2.5-flash-lite

FRONTEND_PORT=80
CORS_ALLOWED_ORIGINS=http://<VM_PUBLIC_IP>
```

> 랜덤 시크릿 생성: `openssl rand -base64 48`

---

## 6. 빌드 & 실행

```bash
docker compose -p pokemo up --build -d
docker compose -p pokemo ps          # 상태 확인
docker compose -p pokemo logs -f backend   # 백엔드 로그
```

첫 빌드는 Maven/npm 의존성 다운로드로 수 분 걸립니다.

---

## 7. 동작 확인

브라우저 또는 로컬에서:

```powershell
# 프론트엔드
start http://<VM_PUBLIC_IP>
# 백엔드 health (nginx 프록시 경유)
curl http://<VM_PUBLIC_IP>/api/health
curl http://<VM_PUBLIC_IP>/actuator/health
```

---

## 운영 메모

- **DB 프로파일**: compose는 backend를 `SPRING_PROFILES_ACTIVE=dev`로 띄웁니다(`ddl-auto: update` → 첫 실행 시 테이블 자동 생성). 스키마가 안정되면 `prod` 프로파일(`validate`)로 전환을 고려하세요.
- **데이터 영속성**: MySQL 데이터는 `mysql-data` 볼륨, 업로드 파일은 `backend-uploads` 볼륨에 보존됩니다. `down` 해도 볼륨은 남습니다(`down -v`는 삭제).
- **업데이트 배포**: `git pull && docker compose -p pokemo up --build -d`
- **HTTPS**: 도메인이 있으면 Caddy/Nginx + Let's Encrypt를 프론트 앞단에 두는 것을 권장. 그때 `CORS_ALLOWED_ORIGINS`도 `https://도메인`으로 변경.
- **OAuth**: Google/Kakao 콘솔의 Redirect URI에 VM 주소를 등록해야 로그인이 동작합니다.
