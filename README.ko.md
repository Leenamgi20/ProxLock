# ProxLock

[English](README.md) · **한국어** · [日本語](README.ja.md) · [中文](README.zh-CN.md)

Meta Quest의 근접 센서를 **재부팅해도 계속 꺼진 상태로 유지**해 주는 앱입니다. 센서를 테이프로 막은 것과 같은 효과입니다.

Meta Quest Developer Hub로는 센서를 2/4/8시간만 끌 수 있고, MQDH·SideQuest·`adb` 등 어떤 방법이든 재부팅하면 원래대로 돌아옵니다. ProxLock은 헤드셋 안에서 돌아가는 아주 작은 앱으로, 부팅할 때마다 그리고 5분마다 설정을 다시 적용합니다.

**왜 필요한가요?** 헤드셋을 벗거나 대기모드로 들어갔다가 돌아오면 Virtual Desktop 같은 PC 스트리밍 앱의 화면이 일렁이고, 앱을 다시 켜야 해결되는 경우가 있습니다. 헤드셋이 잠들지 않게 하면 이 문제를 피할 수 있습니다.

## 지원 기기

| 기기 | 상태 |
|---|---|
| Quest Pro | ✅ 테스트 완료 (재부팅 포함) |
| Quest 3 / 3S / 2 | 같은 시스템 명령을 쓰므로 작동할 것으로 예상되지만 아직 미검증입니다. 테스트 결과를 알려 주세요 |

## 설치 (Windows PC, 원클릭)

1. **개발자 모드 켜기**: 폰의 Meta Horizon 앱 → 기기 → 내 헤드셋 → 헤드셋 설정 → 개발자 모드
2. [Releases](../../releases)에서 `ProxLock-vX.Y.Z.zip`을 받아 압축을 풉니다.
3. Quest를 USB 케이블(데이터 전송이 되는 것)로 PC에 연결합니다.
4. **`install.bat`**을 더블클릭합니다. 헤드셋 안에 창이 뜨면 **"이 컴퓨터에서 항상 허용"**을 체크하고 **허용**을 누릅니다.
5. 설치 프로그램이 안내하면 **헤드셋을 한 번 써 주세요.** ProxLock 창이 뜨면 설정이 끝납니다. 안드로이드는 한 번 실행된 앱만 부팅할 때 자동으로 실행해 줍니다.
6. 끝입니다. 케이블을 빼도 됩니다.

Quest 시스템 업데이트 후에 작동하지 않으면 [Issue](../../issues)로 알려 주세요.

PC에 `adb`가 없으면 설치 프로그램이 Google 공식 서버(`dl.google.com`)에서 Android platform-tools를 자동으로 받습니다. 사용 시 [Android SDK 약관](https://developer.android.com/studio/terms)에 동의하는 것으로 간주됩니다.

## 폰으로 설치 (PC 없이, 미검증)

1. 개발자 모드를 켭니다 (위 1번).
2. 안드로이드 폰에 USB ADB 앱(예: *Bugjaeger*)을 설치하고, `ProxLock.apk`를 폰에 받아 둡니다.
3. 폰과 Quest를 USB-C 케이블로 연결하고, 헤드셋에서 USB 디버깅을 허용한 뒤 앱에서 APK를 설치합니다.
4. **헤드셋에서 ProxLock을 한 번 실행**합니다 (라이브러리 → 필터 → *알 수 없는 출처*). 한 번 실행해야 부팅할 때 자동으로 작동합니다.

## 수동 설치 (adb)

```
adb install -r ProxLock.apk
adb shell am start -n com.proxlock/.MainActivity
```

## 사용법

- 따로 할 일은 없습니다. 부팅하면 알아서 작동합니다.
- 앱 위치: 라이브러리 → 오른쪽 위 필터 → **알 수 없는 출처** → ProxLock
- 앱 안의 **Sensor ON** 버튼을 누르면 센서가 원래대로 돌아오고 자동으로 끄는 것도 멈춥니다. **Sensor OFF**를 누르면 ProxLock이 다시 켜집니다.

## 삭제

`uninstall.bat`을 더블클릭하면 센서를 원래대로 돌리고 앱을 삭제합니다. 또는 헤드셋의 설정 → 앱에서 ProxLock을 삭제한 뒤 재부팅해도 됩니다.

## ⚠️ 주의

- 헤드셋을 벗어도 **화면이 켜진 채로 있습니다.** 전원 버튼으로 화면을 끄지 않으면 배터리가 닳습니다.
- **렌즈에 햇빛이 들지 않게 보관하세요.** 화면이 켜져 있으니 더 주의해야 합니다.
- MQDH나 SideQuest로 센서를 다시 켜도 ProxLock이 5분 안에 다시 끕니다. 센서를 켜려면 앱의 **Sensor ON** 버튼을 쓰세요.
- 나중에 Meta 업데이트로 이 기능이 막히면 ProxLock은 그냥 효과가 없어집니다.

## 작동 원리 / 안전성

- MQDH와 Meta 공식 문서에서 쓰는 것과 같은 `com.oculus.vrpowermanager.prox_close` 명령을 부팅할 때(`BOOT_COMPLETED`)와 5분마다 보냅니다. 5분 알람은 잠든 기기를 깨우지 않는 방식입니다.
- 권한은 `RECEIVE_BOOT_COMPLETED` 하나뿐입니다. 인터넷·저장소 등 다른 권한은 없습니다. 용량은 약 10KB이고, 백그라운드에 상주하지 않습니다.
- 소스는 `app/` 폴더에 있습니다. `build.sh`로 빌드합니다 (Android SDK + JDK, Gradle 불필요).

## 면책

Meta와 관련이 없으며 Meta의 승인을 받지 않았습니다. "Meta Quest"는 Meta Platforms, Inc.의 상표입니다. 사용에 따른 책임은 사용자에게 있습니다.

## 라이선스

[MIT](LICENSE)
