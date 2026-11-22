<div align="center">
    <img src="/HMCL/src/main/resources/assets/img/icon@8x.png" alt="neoHMCL Logo" width="64"/>
</div>

<h1 align="center">neoHMCL</h1>

<p align="center"><strong>更快下载 · 更少打扰 · 与 HMCL 完全兼容的 Minecraft 启动器</strong></p>

<div align="center">

[![GitHub](https://img.shields.io/badge/GitHub-repo-blue?style=flat-square&logo=github)](https://github.com/ddjyx/neoHMCL)
[![License](https://img.shields.io/badge/License-GPL--3.0--or--later-blue?style=flat-square)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17%2B-orange?style=flat-square&logo=openjdk)](https://adoptium.net/)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Linux%20%7C%20macOS-lightgrey?style=flat-square)](#)

</div>

---

**neoHMCL** 是一款基于 [Hello Minecraft! Launcher (HMCL)](https://github.com/HMCL-dev/HMCL) 的 Minecraft 启动器**非官方衍生版本**。它保留 HMCL 的全部能力，并针对**下载体验、更新打扰和私有化反馈**做了增强。

> 与 HMCL 官方团队**没有任何隶属关系**，也不会获得官方支持。请将问题反馈到[本仓库](https://github.com/ddjyx/neoHMCL/issues)。

## 为什么选择 neoHMCL？

- **🚀 单文件分片并发下载**：大文件不再排队走单连接。在服务器支持 HTTP `Range` 时，单文件可切成 1–16 个分片并行下载，显著缩短整合包、模组和游戏文件的等待时间。
- **📊 更真实的下载进度**：分片下载共享同一份进度与速度统计，进度条和速度不会因为多线程而“各说各话”。
- **🔕 没有上游更新打扰**：移除对上游更新服务的自动检查，设置页也不再出现更新选项。版本由你决定，启动器不会偷偷联网问你“要不要更新”。
- **🧩 零迁移成本**：配置格式、游戏实例、整合包与上游完全兼容。可以直接把 neoHMCL 指向现有 HMCL 数据目录，也可以随时换回上游版本。
- **🧰 完整 HMCL 功能**：模组管理、游戏自定义、Forge / NeoForge / Fabric / Quilt / LiteLoader / OptiFine 自动安装、整合包创建与安装、多平台多架构支持，一个不少。
- **💬 独立的反馈渠道**：问题与建议直接在本仓库处理，不必混在上游 Issue 里等待分诊。
- **📜 合规且透明**：遵循 GPLv3 及 HMCL 附加条款，保留原始版权与署名，修改版本以新名称发布。

### 与上游的差异一览

| 能力 | 上游 HMCL | neoHMCL |
| --- | --- | --- |
| HMCL 全部核心功能 | ✅ | ✅ 完整继承 |
| 单文件 HTTP Range 分片并发下载 | — | ✅ 1–16 线程，默认 4 |
| 多分片聚合进度 / 速度 | — | ✅ |
| 上游自动更新检查 | 默认启用 | ❌ 已移除 |
| 配置与实例格式 | HMCL 格式 | ✅ 100% 兼容 |
| 问题反馈位置 | 上游仓库 | 本仓库 |

> 下载加速效果取决于文件服务器是否支持 `Range`、网络带宽与服务器限速。服务器不支持或分片失败时会自动回退到单连接下载，不会让下载变得不可用。

## 核心增强

### 单文件分片并发下载

对于支持 HTTP `Range` 的服务器，大小达到 **200 KiB** 的文件会按「设置 → 下载 → 单文件下载线程数」切成 **1–16 个分片**并发下载，默认 **4 个**，完成后按顺序合并，并照常执行原有的校验和检查。

- 线程数可随时在设置中调整，设为 `1` 即可关闭单文件分片并发。
- 所有分片共享同一个进度与速度统计，界面显示的是整个文件的真实进度。
- 服务器不支持 `Range`、重定向异常或任一分片失败时，自动回退到原有的单连接下载方式。

### 不做上游更新检查

非官方构建不应替上游“代言”，也不该在上游服务上产生额外的更新请求。neoHMCL 已移除上游自动更新检查，设置页不再显示更新选项：

- 启动器不会在后台请求上游更新服务；
- 不会弹出与当前版本不匹配的升级提示；
- 想升级时，自行拉取本仓库的新版本即可。

### 与上游保持同步

代码基线跟随上游 `HMCL-dev/HMCL` 的 `main` 分支，上游的修复与新功能会持续合并进来，同时保留本项目对显示名、更新检查和下载器的修改。

## 当前状态

| 项目 | 说明 |
| --- | --- |
| 代码基线 | 与上游 `HMCL-dev/HMCL` 的 `main` 分支保持一致 |
| 已完成的改动 | 应用显示名改为 neoHMCL（窗口标题、About 页面、认证页面、Linux 包描述）、移除上游自动更新检查、单文件 HTTP Range 分片并发下载与聚合进度、仓库与 Issue 模板整理、移除上游专属的发布/镜像 CI |
| 尚未进行的改动 | 图标、可执行文件名仍沿用上游 |
| 兼容性 | 功能、配置格式与上游完全兼容 |

## 适用人群

- 经常下载大型整合包、模组，希望把带宽跑满的玩家；
- 使用 HMCL 但不希望启动器自动检查更新、被动升级的用户；
- 希望问题在更小、更可控的仓库中被跟进处理的用户；
- 想从 HMCL 平滑迁移、又不想重装或重建实例的玩家。

## 构建

需要 JDK 17 或更高版本（CI 使用 17 / 25 / 27 三个版本进行验证）。

```bash
./gradlew build
```

构建产物位于 `HMCL/build/libs/`：

- `HMCL-<version>.jar` — 可执行 JAR
- `HMCL-<version>.exe` — Windows 安装包
- `HMCL-<version>.sh` / `.deb` — Linux 启动脚本与安装包

只运行启动器（不打包）：

```bash
./gradlew :HMCL:run
```

## 与上游同步

```bash
git fetch upstream
git merge upstream/main
```

`upstream` 已指向 `https://github.com/HMCL-dev/HMCL.git`。合并时请注意保留本项目对显示名、更新检查与下载器的修改。

## 许可证与署名

本项目沿用上游的 **GNU General Public License v3.0 or later**，并附带 HMCL 的附加条款（见 [`LICENSE`](LICENSE)）：

1. 分发修改版时，必须以合理方式修改软件名称或版本号，以区别于原版（GPLv3 §7(c)）——这正是本项目命名为 neoHMCL 的原因；
2. 不得删除软件中显示的版权声明（GPLv3 §7(b)）。

原始版权归 **The HMCL Authors** 所有（`Copyright (C) 2013-2026 The HMCL Authors`），本项目仅在其基础上进行修改。

## 反馈

- 问题与建议请提交到 **本仓库** 的 [Issues](https://github.com/ddjyx/neoHMCL/issues) / [Discussions](https://github.com/ddjyx/neoHMCL/discussions)。
- **请勿** 将 neoHMCL 的问题提交到上游仓库，上游团队无法为衍生版本提供支持。

`docs/` 目录中的文档（包括各语言的 README）来自上游项目，内容描述的是 HMCL 本身。

---

## English

**neoHMCL** is an **unofficial derivative** of [Hello Minecraft! Launcher (HMCL)](https://github.com/HMCL-dev/HMCL) that keeps the full HMCL feature set while improving downloads and reducing update noise.

### Why neoHMCL?

- **🚀 Segmented parallel downloads** — Files of 200 KiB or larger are split into 1–16 HTTP `Range` parts (default 4) when the server supports it, with automatic fallback to a single connection.
- **📊 Accurate aggregate progress** — All parts share one progress and speed counter, so the UI reflects the real file progress.
- **🔕 No upstream update checks** — The launcher never contacts the upstream update service, and the update option is gone from settings. You choose when to upgrade.
- **🧩 Drop-in compatible** — Configs, game instances and modpacks use the upstream format. Point it at your existing HMCL data directory, or switch back anytime.
- **🧰 Full HMCL features** — Mod management, game customization, automatic Forge / NeoForge / Fabric / Quilt / LiteLoader / OptiFine installation, modpack creation, and multi-platform support.
- **💬 Independent support** — Issues are tracked in this repository, not upstream.
- **📜 GPLv3 compliant** — Released under the GPLv3 with HMCL's additional terms; original copyright notices are preserved and the modified build is renamed.

The project is not affiliated with the HMCL team; please report issues to this repository, not upstream. Build with JDK 17+ via `./gradlew build`.
