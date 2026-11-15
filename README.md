<div align="center">
    <img src="/HMCL/src/main/resources/assets/img/icon@8x.png" alt="neoHMCL Logo" width="64"/>
</div>

<h1 align="center">neoHMCL</h1>

<div align="center">

[![GitHub](https://img.shields.io/badge/GitHub-repo-blue?style=flat-square&logo=github)](https://github.com/ddjyx/neoHMCL)

</div>

---

**neoHMCL** 是一款基于 [Hello Minecraft! Launcher (HMCL)](https://github.com/HMCL-dev/HMCL) 的 Minecraft 启动器的**非官方衍生版本**。

它继承了 HMCL 的全部功能：模组管理、游戏自定义、自动安装 Forge / NeoForge / Fabric / Quilt / LiteLoader / OptiFine、整合包创建与安装、多平台与多架构支持等。本项目与 HMCL 官方团队**没有任何隶属关系**，也不会获得官方支持。

## 当前状态

| 项目 | 说明 |
| --- | --- |
| 代码基线 | 与上游 `HMCL-dev/HMCL` 的 `main` 分支保持一致 |
| 已完成的改动 | 应用显示名改为 neoHMCL（窗口标题、About 页面、认证页面、Linux 包描述）、禁用上游自动更新检查（非官方构建不再请求上游更新服务，设置页不再显示更新选项）、支持大文件 HTTP Range 分片并发下载、仓库与 Issue 模板整理、移除上游专属的发布/镜像 CI |
| 尚未进行的改动 | 图标、可执行文件名仍沿用上游 |

因此目前的功能、配置格式与上游完全兼容。

### 分片并发下载

对于支持 HTTP `Range` 的服务器，大小达到 1 MiB 的文件会默认切成 4 个分片并发下载，再按顺序合并并执行原有的校验和检查。服务器不支持 Range、重定向异常或分片下载失败时，会自动回退到原有的单连接下载方式。

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

`upstream` 已指向 `https://github.com/HMCL-dev/HMCL.git`。合并时请注意保留本项目对显示名的修改。

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

**neoHMCL** is an **unofficial derivative** of [Hello Minecraft! Launcher (HMCL)](https://github.com/HMCL-dev/HMCL). It keeps all upstream features and, for now, only changes the user-facing product name plus repository metadata. It is licensed under the GPLv3 with HMCL's additional terms (a modified version must be renamed or re-numbered, and copyright notices must be preserved). The project is not affiliated with the HMCL team; please report issues to this repository, not upstream.
