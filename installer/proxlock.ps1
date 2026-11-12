# ProxLock one-click installer / uninstaller for Meta Quest (Windows)
param([ValidateSet('install','uninstall')][string]$Mode = 'install')
$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [Text.Encoding]::UTF8
$Here = Split-Path -Parent $MyInvocation.MyCommand.Path
$Pkg  = 'com.proxlock'

# ---------- messages ----------
$lang = 'en'; foreach ($c in @((Get-UICulture), (Get-Culture), [Globalization.CultureInfo]::InstalledUICulture)) { if ($c.TwoLetterISOLanguageName -in 'ko','ja','zh') { $lang = $c.TwoLetterISOLanguageName; break } }
$M = @{
 en = @{ find='Looking for adb...'; dl='adb not found. Downloading Android platform-tools from Google (dl.google.com)...';
   wait='Connect the Quest with a USB cable. Waiting for the headset...';
   auth='Put on the headset and press "Allow" (tick "Always allow from this computer") on the USB debugging prompt.';
   none='No headset found. Check: Developer Mode is ON, the cable supports data, then run again.';
   multi='More than one device is connected. Leave only the Quest connected and run again.';
   inst='Installing ProxLock...'; ok='Done! The proximity sensor is now disabled and will stay disabled after every reboot.';
   warn='Note: the screen will stay on when you take the headset off. Press the power button to sleep it, and keep the lenses out of sunlight.';
   verifyfail='Installed, but the sensor state could not be confirmed. Reboot the headset and check that the screen stays on when removed.';
   unok='ProxLock removed. The proximity sensor works normally again.'; notinst='ProxLock is not installed.'; fail='Failed:' }
 ko = @{ find='adb 찾는 중...'; dl='adb가 없어서 Google(dl.google.com)에서 Android platform-tools를 다운로드합니다...';
   wait='Quest를 USB 케이블로 PC에 연결하세요. 헤드셋을 기다리는 중...';
   auth='헤드셋을 쓰고 USB 디버깅 허용 창에서 "이 컴퓨터에서 항상 허용"을 체크한 뒤 "허용"을 누르세요.';
   none='헤드셋을 찾지 못했습니다. 개발자 모드가 켜져 있는지, 데이터 전송이 되는 케이블인지 확인 후 다시 실행하세요.';
   multi='기기가 여러 대 연결되어 있습니다. Quest만 연결한 뒤 다시 실행하세요.';
   inst='ProxLock 설치 중...'; ok='완료! 근접 센서가 꺼졌고, 재부팅해도 계속 꺼진 상태로 유지됩니다.';
   warn='주의: 헤드셋을 벗어도 화면이 켜져 있습니다. 전원 버튼으로 화면을 끄고, 렌즈에 햇빛이 들지 않게 보관하세요.';
   verifyfail='설치는 됐지만 센서 상태를 확인하지 못했습니다. 헤드셋을 재부팅한 뒤 벗었을 때 화면이 켜져 있는지 확인하세요.';
   unok='ProxLock을 삭제했습니다. 근접 센서가 원래대로 작동합니다.'; notinst='ProxLock이 설치되어 있지 않습니다.'; fail='실패:' }
 ja = @{ find='adb を検索中...'; dl='adb が見つかりません。Google (dl.google.com) から Android platform-tools をダウンロードします...';
   wait='Quest を USB ケーブルで PC に接続してください。ヘッドセットを待っています...';
   auth='ヘッドセットをかぶり、USB デバッグの確認画面で「このコンピューターを常に許可」にチェックして「許可」を押してください。';
   none='ヘッドセットが見つかりません。開発者モードがオンか、データ通信対応ケーブルかを確認して再実行してください。';
   multi='複数のデバイスが接続されています。Quest だけを接続して再実行してください。';
   inst='ProxLock をインストール中...'; ok='完了！近接センサーが無効になり、再起動後も無効のまま維持されます。';
   warn='注意: ヘッドセットを外しても画面は点いたままです。電源ボタンでスリープし、レンズに直射日光が当たらないよう保管してください。';
   verifyfail='インストールは完了しましたが、センサーの状態を確認できませんでした。再起動後、外したときに画面が点いたままか確認してください。';
   unok='ProxLock を削除しました。近接センサーは通常どおり動作します。'; notinst='ProxLock はインストールされていません。'; fail='失敗:' }
 zh = @{ find='正在查找 adb...'; dl='未找到 adb，正在从 Google (dl.google.com) 下载 Android platform-tools...';
   wait='请用 USB 数据线将 Quest 连接到电脑。正在等待头显...';
   auth='请戴上头显，在 USB 调试提示中勾选"始终允许此计算机"并点击"允许"。';
   none='未找到头显。请确认已开启开发者模式、数据线支持数据传输，然后重新运行。';
   multi='连接了多台设备。请只连接 Quest 后重新运行。';
   inst='正在安装 ProxLock...'; ok='完成！距离传感器已禁用，重启后也会保持禁用。';
   warn='注意：摘下头显后屏幕会保持点亮。请按电源键让其休眠，并避免镜片受阳光直射。';
   verifyfail='已安装，但无法确认传感器状态。请重启头显，并确认摘下后屏幕保持点亮。';
   unok='已删除 ProxLock，距离传感器恢复正常。'; notinst='未安装 ProxLock。'; fail='失败：' }
}[$lang]
function Say($k, $c = 'White') { Write-Host $M[$k] -ForegroundColor $c }

# ---------- adb ----------
function Find-Adb {
  $c = @((Get-Command adb -ErrorAction SilentlyContinue).Source,
    "$Here\platform-tools\adb.exe",
    "$env:ProgramFiles\Meta VR Developer Hub\resources\bin\adb.exe",
    "$env:ProgramFiles\Meta Quest Developer Hub\resources\bin\adb.exe",
    "$env:APPDATA\SideQuest\platform-tools\adb.exe",
    "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe")
  foreach ($p in $c) { if ($p -and (Test-Path $p)) { return $p } }
  Say dl Yellow
  $zip = Join-Path $env:TEMP 'proxlock-platform-tools.zip'
  [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
  Invoke-WebRequest 'https://dl.google.com/android/repository/platform-tools-latest-windows.zip' -OutFile $zip -UseBasicParsing
  Expand-Archive $zip -DestinationPath $Here -Force
  Remove-Item $zip -Force
  return "$Here\platform-tools\adb.exe"
}

try {
  Say find
  $adb = Find-Adb
  & $adb start-server *> $null

  Say wait Cyan
  $serial = $null; $authShown = $false
  for ($i = 0; $i -lt 90 -and -not $serial; $i++) {
    $rows = & $adb devices | Select-Object -Skip 1 | Where-Object { $_ -match '\S' }
    $ready = @($rows | Where-Object { $_ -match '\tdevice$' })
    if ($ready.Count -gt 1) { Say multi Red; exit 1 }
    if ($ready.Count -eq 1) { $serial = ($ready[0] -split '\t')[0]; break }
    if (-not $authShown -and ($rows -match 'unauthorized')) { Say auth Yellow; $authShown = $true }
    Start-Sleep 2
  }
  if (-not $serial) { Say none Red; exit 1 }
  $sh = { param($a) & $adb -s $serial shell $a }

  if ($Mode -eq 'uninstall') {
    $has = & $sh "pm list packages $Pkg"
    if (-not ($has -match "package:$Pkg`$")) { Say notinst Yellow; exit 0 }
    & $sh 'am broadcast -a com.oculus.vrpowermanager.automation_disable' *> $null
    & $adb -s $serial uninstall $Pkg | Out-Null
    Say unok Green; exit 0
  }

  Say inst
  $apk = Join-Path $Here 'ProxLock.apk'
  $out = & $adb -s $serial install -r $apk 2>&1 | Out-String
  if ($out -notmatch 'Success') { throw $out.Trim() }
  # Launch once: Android won't deliver BOOT_COMPLETED to an app that has never been opened.
  & $sh "am start -n $Pkg/.MainActivity" *> $null
  Start-Sleep 3
  $state = & $sh 'dumpsys vrpowermanager' | Select-String 'Virtual proximity state'
  if ("$state" -match 'CLOSE') { Say ok Green } else { Say verifyfail Yellow }
  Say warn Yellow
} catch {
  Write-Host "$($M.fail) $_" -ForegroundColor Red; exit 1
}
