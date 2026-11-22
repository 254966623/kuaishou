# kuaishou-farmer —— 纯手机侧刷金币骨架（无障碍架构）

## 架构（对照 adcleaner）
| 层 | adcleaner | 本项目 |
|---|---|---|
| 引擎 | tool/fast.py (uiautomator2) | FarmerService.kt (AccessibilityService) |
| 脚本 | recipes/*.toml | assets/rules.json（词库热更新） |
| 执行器 | adb input swipe | dispatchGesture |
| 定时 | 无 | SignInPlanner（AlarmManager 每日 8/12/20 点） |
| agent 层 | AGENTS.md + CLI | prompts/用法二.md + 聊天 AI 改词库 |

## 获取 APK（零电脑方案）
本项目已配置 GitHub Actions，推送到 GitHub 后自动构建 APK：

1. 手机浏览器打开 github.com，注册/登录
2. 新建仓库（名字任意，如 `kuaishou-farmer`）
3. 把本 ZIP 内所有文件上传到仓库（网页版支持拖拽上传）
4. 进入 Actions 页签，点击「Build APK」→「Run workflow」
5. 约 3 分钟后，Artifacts 里下载 `app-debug.apk`
6. 安卓设置 → 允许安装未知来源 → 安装

## 使用
1. 系统设置 → 无障碍 → 已安装服务 → coin-farmer → 开启
2. 打开快手极速版，进视频流页面；服务自动开始刷
3. 音量下键 = 紧急停止

## 反风控要点（已内置）
- 停留 8–25s 随机、滑动时长 250–650ms 随机、6% 分段滑动、10% 发呆
- 0–7 点停机；危险词（支付/充值）只通知不动作
- 词库外的新弹窗：只走白名单关闭词，关不掉就按返回键，绝不盲点

## 已知边界（诚实声明）
- 快手页面常改版：签到/宝箱路径需按 prompts/用法二 在真机上校准一轮
- 风控风险不因自动化方式消失；单号月收益 3–10 元且递减
- 无障碍手势在个别 ROM 上有延迟，华为/小米需关「误触优化」类选项

## 备用方案：Termux 直接运行
如急需使用且不想等 GitHub Actions，可用 Termux 版 Python 脚本（见 termux 目录）。
需开启无线调试配对，稳定性不如 APK，但零构建零等待。
