# neoHMCL interface design

## Scope

This document covers the selectable PCL home layout. Existing Monet color themes and the classic HMCL home remain supported.

## PCL home contract

- Primary action: 启动/启动游戏. It reuses the existing MainPage launch handler, including its empty-instance install flow.
- Information priority: current account, selected instance and its launch action, download/settings navigation.
- Required content: PCL wordmark; 启动/下载/设置/更多 navigation; account type; avatar; username; launch button and selected instance; 版本选择/版本设置.
- Left out on purpose: a duplicate classic sidebar, a second implementation of launching or account management, decorative cards, and the reference user's name/avatar as static placeholders.
- Layout: PCL home - fixed account sidebar with independent content workspace -> fixed-sidenav-shell (https://raw.githubusercontent.com/changeroa/StyleGallery/main/patterns/viewport-shell/fixed-sidenav-shell.md); scroll owner: each existing page's own scroll pane; anti-patterns: duplicate navigation, replaced launch behavior, layout-owned nested scrolling.
- Layout selection: `uiLayout=PCL` selects this home; `uiLayout=HMCL` selects the existing home. The selection is independent from Monet theme packs and persists through LauncherSettings.
- Visual tokens: white account sidebar; pale blue `#D6E4F5` workspace; accent/title color from the active Monet theme; 6px control and 20px pill radii; 8px base spacing; 16px group spacing; 14px body, 16px launch label, 24px PCL wordmark.

## State table

| State | Visible result | Action |
|---|---|---|
| No account | default avatar and the existing missing-account text | open the existing account page |
| Account selected | real account type, avatar, profile name | open the existing account page |
| No instance | existing empty-launch text, no invented version | existing install-then-launch flow |
| Instance selected | launch text and real instance ID | existing launch flow; version settings enabled |
| Settings page | existing settings shell and back control | select PCL or HMCL under appearance |
| Theme color or brightness change | existing color roles update without changing layout selection | no navigation reset |
